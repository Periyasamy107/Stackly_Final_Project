package com.example.bank.config;

import com.example.bank.common.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class FileStorageInitializer {

    private final FileStorageProperties properties;

    @PostConstruct
    public void initialize() {
        Path rootLocation = properties.getRootLocation()
                .toAbsolutePath()
                .normalize();

        validateProperties();

        if (!properties.isCreateDirectoryIfMissing()) {
            validateExistingDirectory(rootLocation);
            return;
        }

        try {
            Files.createDirectories(rootLocation);
        } catch (IOException exception) {
            throw new FileStorageException(
                    "Unable to initialize file storage directory",
                    exception
            );
        }

        validateDirectory(rootLocation);
    }

    private void validateProperties() {
        if (properties.getRootLocation() == null) {
            throw new FileStorageException(
                    "File storage root location must be configured"
            );
        }

        if (properties.getMaxFileSizeBytes() <= 0) {
            throw new FileStorageException(
                    "File storage maximum file size must be greater than zero"
            );
        }

        if (properties.getMaxFilesPerOwner() <= 0) {
            throw new FileStorageException(
                    "Maximum files per owner must be greater than zero"
            );
        }

        if (properties.getAllowedExtensions() == null
                || properties.getAllowedExtensions().isEmpty()) {
            throw new FileStorageException(
                    "At least one allowed file extension must be configured"
            );
        }
    }

    private void validateExistingDirectory(Path path) {
        if (!Files.exists(path)) {
            throw new FileStorageException(
                    "Configured file storage directory does not exist: "
                            + path
            );
        }

        validateDirectory(path);
    }

    private void validateDirectory(Path path) {
        if (!Files.isDirectory(path)) {
            throw new FileStorageException(
                    "Configured file storage location is not a directory: "
                            + path
            );
        }

        if (!Files.isWritable(path)) {
            throw new FileStorageException(
                    "Configured file storage directory is not writable: "
                            + path
            );
        }
    }
}