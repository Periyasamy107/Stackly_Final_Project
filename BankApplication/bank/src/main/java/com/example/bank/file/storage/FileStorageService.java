package com.example.bank.file.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileStorageService {

    String store(
            MultipartFile file,
            String storedFileName
    ) throws IOException;

    InputStream load(
            String storagePath
    ) throws IOException;

    void delete(
            String storagePath
    ) throws IOException;
}