package com.example.bank.file.dto.request;

import com.example.bank.common.enums.FileDocumentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadataRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Document type is required")
    private FileDocumentType documentType;

    @NotNull(message = "Original file name is required")
    @Size(
            min = 1,
            max = 255,
            message = "Original file name must be between 1 and 255 characters"
    )
    private String originalFileName;

    @NotNull(message = "Content type is required")
    @Size(
            min = 1,
            max = 100,
            message = "Content type must be between 1 and 100 characters"
    )
    private String contentType;

    @NotNull(message = "File size is required")
    @Positive(message = "File size must be greater than zero")
    private Long fileSize;
}