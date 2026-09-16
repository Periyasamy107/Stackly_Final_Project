package com.example.bank.file.storage;

import com.example.bank.common.exception.BusinessException;
import com.example.bank.config.FileStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private final Path storageRoot;

    public LocalFileStorageService(
            FileStorageProperties properties) {

        this.storageRoot = properties
                .getRootLocation()
                .toAbsolutePath()
                .normalize();

        log.info(
                "File storage initialized at {}",
                this.storageRoot
        );
    }

    @Override
    public String store(
            MultipartFile file,
            String storedFileName) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                    "File cannot be empty"
            );
        }

        Path target = storageRoot
                .resolve(storedFileName)
                .normalize();

        validatePath(target);

        try (InputStream inputStream = file.getInputStream()) {

            Files.copy(
                    inputStream,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return target.toString();
    }

    @Override
    public InputStream load(
            String storagePath) throws IOException {

        Path path = validateAndResolve(storagePath);

        if (!Files.exists(path)
                || !Files.isRegularFile(path)) {

            throw new BusinessException(
                    "Stored file does not exist"
            );
        }

        return Files.newInputStream(
                path,
                StandardOpenOption.READ
        );
    }

    @Override
    public void delete(
            String storagePath) throws IOException {

        Path path = validateAndResolve(storagePath);

        Files.deleteIfExists(path);
    }

    private Path validateAndResolve(
            String storagePath) {

        if (storagePath == null
                || storagePath.isBlank()) {

            throw new BusinessException(
                    "Storage path is required"
            );
        }

        Path path = Paths
                .get(storagePath)
                .toAbsolutePath()
                .normalize();

        validatePath(path);

        return path;
    }

    private void validatePath(Path path) {

        if (!path.startsWith(storageRoot)) {
            throw new BusinessException(
                    "Invalid file storage path"
            );
        }
    }
}