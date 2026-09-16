package com.example.bank.file.dto.response;

import com.example.bank.common.enums.FileDocumentType;
import com.example.bank.common.enums.FileMetadataStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadataResponse {

    private Long id;

    private Long customerId;

    private Long uploadedByUserId;

    private FileDocumentType documentType;

    private String originalFileName;

    private String contentType;

    private Long fileSize;

    private String checksum;

    private FileMetadataStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}