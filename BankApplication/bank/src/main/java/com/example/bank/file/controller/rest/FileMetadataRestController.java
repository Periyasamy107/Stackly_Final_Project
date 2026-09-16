package com.example.bank.file.controller.rest;

import com.example.bank.common.enums.FileDocumentType;
import com.example.bank.file.dto.response.FileMetadataResponse;
import com.example.bank.file.service.FileMetadataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(
        name = "Files",
        description = "File metadata and file download operations"
)
public class FileMetadataRestController {

    private final FileMetadataService fileMetadataService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FileMetadataResponse> uploadFile(
            @RequestParam Long customerId,
            @RequestParam FileDocumentType documentType,
            @RequestPart("file") MultipartFile file) {

        return ResponseEntity.ok(
                fileMetadataService.uploadFile(
                        customerId,
                        documentType,
                        file
                )
        );
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileMetadataResponse> getFileMetadata(
            @PathVariable Long fileId) {

        return ResponseEntity.ok(
                fileMetadataService.getFileMetadata(fileId)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<FileMetadataResponse>> getCustomerFiles(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                fileMetadataService.getCustomerFiles(
                        customerId
                )
        );
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @PathVariable Long fileId) {

        byte[] file =
                fileMetadataService.downloadFile(fileId);

        String contentType =
                fileMetadataService
                        .getDownloadContentType(fileId);

        String fileName =
                fileMetadataService
                        .getDownloadFileName(fileId);

        ContentDisposition disposition =
                ContentDisposition
                        .attachment()
                        .filename(fileName)
                        .build();

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                contentType
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .contentLength(file.length)
                .body(
                        new ByteArrayResource(file)
                );
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId) {

        fileMetadataService.deleteFile(fileId);

        return ResponseEntity.noContent().build();
    }
}