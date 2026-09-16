package com.example.bank.file.service;

import com.example.bank.common.enums.FileDocumentType;
import com.example.bank.common.enums.FileMetadataStatus;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.customer.entity.Customer;
import com.example.bank.customer.repository.CustomerRepository;
import com.example.bank.file.dto.response.FileMetadataResponse;
import com.example.bank.file.entity.FileMetadata;
import com.example.bank.file.mapper.FileMetadataMapper;
import com.example.bank.file.repository.FileMetadataRepository;
import com.example.bank.file.storage.FileStorageService;
import com.example.bank.security.authorization.FileMetadataAuthorizationService;
import com.example.bank.user.entity.User;
import com.example.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FileMetadataServiceImpl
        implements FileMetadataService {

    private static final String SHA_256 = "SHA-256";

    private final FileMetadataRepository fileMetadataRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final FileMetadataMapper fileMetadataMapper;
    private final FileStorageService fileStorageService;
    private final FileMetadataAuthorizationService authorizationService;

    private final Tika tika = new Tika();

    @Value("${bank.file-storage.max-file-size-bytes:2097152}")
    private long maxFileSize;

    @Override
    @Transactional
    public FileMetadataResponse uploadFile(
            Long customerId,
            FileDocumentType documentType,
            MultipartFile file) {

        validateUploadRequest(
                customerId,
                documentType,
                file
        );

        if (!authorizationService
                .canUploadForCustomer(customerId)) {

            throw new BusinessException(
                    "User is not authorized to upload files for this customer"
            );
        }

        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Customer not found: "
                                                + customerId
                                ));

        User uploadedByUser =
                getAuthenticatedUser();

        String detectedContentType =
                detectContentType(file);

        validateDetectedContentType(
                detectedContentType
        );

        String checksum =
                calculateChecksum(file);

        if (fileMetadataRepository
                .existsByCustomerIdAndChecksum(
                        customerId,
                        checksum)) {

            throw new BusinessException(
                    "The same file already exists for this customer"
            );
        }

        String storedFileName =
                generateStoredFileName(
                        detectedContentType
                );

        String storagePath;

        try {

            storagePath =
                    fileStorageService.store(
                            file,
                            storedFileName
                    );

        } catch (IOException exception) {

            log.error(
                    "Unable to store file for customer {}",
                    customerId,
                    exception
            );

            throw new BusinessException(
                    "Unable to store uploaded file"
            );
        }

        try {

            FileMetadata metadata =
                    FileMetadata.builder()
                            .customer(customer)
                            .uploadedByUser(uploadedByUser)
                            .documentType(documentType)
                            .originalFileName(
                                    sanitizeOriginalFileName(
                                            file.getOriginalFilename()
                                    )
                            )
                            .storedFileName(storedFileName)
                            .storagePath(storagePath)
                            .contentType(detectedContentType)
                            .fileSize(file.getSize())
                            .checksum(checksum)
                            .status(FileMetadataStatus.ACTIVE)
                            .createdAt(DateTimeUtil.nowUtc())
                            .updatedAt(DateTimeUtil.nowUtc())
                            .version(0L)
                            .build();

            FileMetadata saved =
                    fileMetadataRepository.save(metadata);

            registerRollbackCleanup(storagePath);

            return fileMetadataMapper.toResponse(saved);

        } catch (RuntimeException exception) {

            cleanupStoredFile(
                    storagePath,
                    customerId
            );

            throw exception;
        }
    }

    @Override
    public FileMetadataResponse getFileMetadata(
            Long fileId) {

        if (!authorizationService
                .canAccessFile(fileId)) {

            throw new BusinessException(
                    "User is not authorized to access this file"
            );
        }

        FileMetadata metadata =
                getActiveMetadata(fileId);

        return fileMetadataMapper.toResponse(metadata);
    }

    @Override
    public List<FileMetadataResponse> getCustomerFiles(
            Long customerId) {

        if (!authorizationService
                .canAccessCustomerFiles(customerId)) {

            throw new BusinessException(
                    "User is not authorized to access customer files"
            );
        }

        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(
                    "Customer not found: " + customerId
            );
        }

        return fileMetadataRepository
                .findByCustomerIdAndStatusOrderByCreatedAtDesc(
                        customerId,
                        FileMetadataStatus.ACTIVE
                )
                .stream()
                .map(fileMetadataMapper::toResponse)
                .toList();
    }

    @Override
    public byte[] downloadFile(
            Long fileId) {

        if (!authorizationService
                .canAccessFile(fileId)) {

            throw new BusinessException(
                    "User is not authorized to download this file"
            );
        }

        FileMetadata metadata =
                getActiveMetadata(fileId);

        try (InputStream inputStream =
                     fileStorageService.load(
                             metadata.getStoragePath()
                     )) {

            return inputStream.readAllBytes();

        } catch (IOException exception) {

            log.error(
                    "Unable to read file metadata id {}",
                    fileId,
                    exception
            );

            throw new BusinessException(
                    "Unable to read stored file"
            );
        }
    }

    @Override
    public String getDownloadContentType(
            Long fileId) {

        if (!authorizationService
                .canAccessFile(fileId)) {

            throw new BusinessException(
                    "User is not authorized to access this file"
            );
        }

        return getActiveMetadata(fileId)
                .getContentType();
    }

    @Override
    public String getDownloadFileName(
            Long fileId) {

        if (!authorizationService
                .canAccessFile(fileId)) {

            throw new BusinessException(
                    "User is not authorized to access this file"
            );
        }

        return getActiveMetadata(fileId)
                .getOriginalFileName();
    }

    @Override
    @Transactional
    public void deleteFile(
            Long fileId) {

        if (!authorizationService
                .canDeleteFile(fileId)) {

            throw new BusinessException(
                    "User is not authorized to delete this file"
            );
        }

        FileMetadata metadata =
                fileMetadataRepository
                        .findByIdForUpdate(fileId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "File not found: " + fileId
                                ));

        if (metadata.getStatus()
                != FileMetadataStatus.ACTIVE) {

            throw new BusinessException(
                    "File is already inactive"
            );
        }

        metadata.setStatus(
                FileMetadataStatus.DELETED
        );

        fileMetadataRepository.save(metadata);
    }

    private FileMetadata getActiveMetadata(
            Long fileId) {

        FileMetadata metadata =
                fileMetadataRepository.findById(fileId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "File not found: " + fileId
                                ));

        if (metadata.getStatus()
                != FileMetadataStatus.ACTIVE) {

            throw new BusinessException(
                    "File is not active"
            );
        }

        return metadata;
    }


    private void registerRollbackCleanup(
            String storagePath) {

        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {

            log.warn(
                    "Transaction synchronization is not active " +
                            "for stored file {}. Rollback cleanup cannot " +
                            "be automatically registered.",
                    storagePath
            );

            return;
        }

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCompletion(
                                    int status) {

                                if (status ==
                                        TransactionSynchronization
                                                .STATUS_ROLLED_BACK
                                        || status ==
                                        TransactionSynchronization
                                                .STATUS_UNKNOWN) {

                                    cleanupStoredFile(
                                            storagePath,
                                            null
                                    );
                                }
                            }
                        }
                );
    }


    private void cleanupStoredFile(
            String storagePath,
            Long customerId) {

        if (storagePath == null
                || storagePath.isBlank()) {

            return;
        }

        try {

            fileStorageService.delete(
                    storagePath
            );

            if (customerId != null) {

                log.info(
                        "Cleaned up orphaned uploaded file " +
                                "for customer {}: {}",
                        customerId,
                        storagePath
                );

            } else {

                log.info(
                        "Cleaned up uploaded file after " +
                                "transaction rollback: {}",
                        storagePath
                );
            }

        } catch (IOException cleanupException) {

            if (customerId != null) {

                log.error(
                        "Failed to clean up orphaned uploaded " +
                                "file for customer {}: {}",
                        customerId,
                        storagePath,
                        cleanupException
                );

            } else {

                log.error(
                        "Failed to clean up uploaded file " +
                                "after transaction rollback: {}",
                        storagePath,
                        cleanupException
                );
            }
        }
    }

    private void validateUploadRequest(
            Long customerId,
            FileDocumentType documentType,
            MultipartFile file) {

        if (customerId == null) {
            throw new BusinessException(
                    "Customer ID is required"
            );
        }

        if (documentType == null) {
            throw new BusinessException(
                    "Document type is required"
            );
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    "File is required"
            );
        }

        if (file.getSize() <= 0) {
            throw new BusinessException(
                    "File must contain data"
            );
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException(
                    "File size exceeds the maximum allowed size of "
                            + maxFileSize
                            + " bytes"
            );
        }
    }

    private String detectContentType(
            MultipartFile file) {

        try (InputStream inputStream =
                     file.getInputStream()) {

            String detectedType =
                    tika.detect(
                            inputStream,
                            file.getOriginalFilename()
                    );

            if (detectedType == null
                    || detectedType.isBlank()) {

                throw new BusinessException(
                        "Unable to determine file content type"
                );
            }

            return detectedType;

        } catch (IOException exception) {

            throw new BusinessException(
                    "Unable to inspect uploaded file"
            );
        }
    }

    private void validateDetectedContentType(
            String contentType) {

        if (contentType.equalsIgnoreCase(
                "application/pdf")) {
            return;
        }

        if (contentType.equalsIgnoreCase(
                "image/jpeg")) {
            return;
        }

        if (contentType.equalsIgnoreCase(
                "image/png")) {
            return;
        }

        if (contentType.equalsIgnoreCase(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return;
        }

        if (contentType.equalsIgnoreCase(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return;
        }

        if (contentType.equalsIgnoreCase(
                "application/msword")) {
            return;
        }

        if (contentType.equalsIgnoreCase(
                "application/vnd.ms-excel")) {
            return;
        }

        throw new BusinessException(
                "Unsupported file type: " + contentType
        );
    }

    private String calculateChecksum(
            MultipartFile file) {

        try (InputStream inputStream =
                     file.getInputStream()) {

            MessageDigest digest =
                    MessageDigest.getInstance(SHA_256);

            byte[] buffer = new byte[8192];

            int bytesRead;

            while ((bytesRead =
                    inputStream.read(buffer)) != -1) {

                digest.update(
                        buffer,
                        0,
                        bytesRead
                );
            }

            return HexFormat.of()
                    .formatHex(digest.digest());

        } catch (IOException exception) {

            throw new BusinessException(
                    "Unable to calculate file checksum"
            );

        } catch (NoSuchAlgorithmException exception) {

            throw new BusinessException(
                    "SHA-256 algorithm is unavailable"
            );
        }
    }

    private String generateStoredFileName(
            String contentType) {

        String extension =
                extensionForContentType(
                        contentType
                );

        return UUID.randomUUID()
                + extension;
    }

    private String extensionForContentType(
            String contentType) {

        return switch (contentType.toLowerCase()) {

            case "application/pdf" ->
                    ".pdf";

            case "image/jpeg" ->
                    ".jpg";

            case "image/png" ->
                    ".png";

            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    ".docx";

            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                    ".xlsx";

            case "application/msword" ->
                    ".doc";

            case "application/vnd.ms-excel" ->
                    ".xls";

            default ->
                    throw new BusinessException(
                            "Unsupported file type: "
                                    + contentType
                    );
        };
    }

    private String sanitizeOriginalFileName(
            String originalFileName) {

        if (originalFileName == null
                || originalFileName.isBlank()) {

            throw new BusinessException(
                    "Original file name is required"
            );
        }

        String fileName =
                java.nio.file.Paths
                        .get(originalFileName)
                        .getFileName()
                        .toString();

        if (fileName.length() > 255) {
            throw new BusinessException(
                    "Original file name is too long"
            );
        }

        return fileName;
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new BusinessException(
                    "Authenticated user is required"
            );
        }

        Long userId = null;

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {

            Object userIdClaim =
                    jwtAuthenticationToken
                            .getToken()
                            .getClaims()
                            .get("userId");

            if (userIdClaim instanceof Number number) {

                userId = number.longValue();

            } else if (userIdClaim instanceof String value) {

                try {

                    userId = Long.valueOf(value);

                } catch (NumberFormatException ignored) {

                    throw new BusinessException(
                            "Invalid authenticated user ID"
                    );
                }
            }

        } else if (authentication.getPrincipal()
                instanceof com.example.bank.security.authentication.CustomUserDetails details) {

            userId = details.getUserId();
        }

        if (userId == null) {

            throw new BusinessException(
                    "Unable to determine authenticated user"
            );
        }

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Authenticated user could not be resolved"
                        ));
    }

    private User findUserFromPrincipal(
            Long userId) {

        throw new BusinessException(
                "Authenticated user could not be resolved"
        );
    }
}