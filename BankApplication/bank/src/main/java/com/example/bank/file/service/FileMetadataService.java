package com.example.bank.file.service;

import com.example.bank.common.enums.FileDocumentType;
import com.example.bank.file.dto.response.FileMetadataResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileMetadataService {

    FileMetadataResponse uploadFile(
            Long customerId,
            FileDocumentType documentType,
            MultipartFile file
    );

    FileMetadataResponse getFileMetadata(
            Long fileId
    );

    List<FileMetadataResponse> getCustomerFiles(
            Long customerId
    );

    byte[] downloadFile(
            Long fileId
    );

    String getDownloadContentType(
            Long fileId
    );

    String getDownloadFileName(
            Long fileId
    );

    void deleteFile(
            Long fileId
    );
}