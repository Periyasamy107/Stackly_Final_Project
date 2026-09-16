package com.example.bank.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "bank.file-storage")
public class FileStorageProperties {

    private Path rootLocation =
            Path.of("storage/files");

    private long maxFileSizeBytes =
            10 * 1024 * 1024;

    private int maxFilesPerOwner =
            100;

    private List<String> allowedExtensions =
            new ArrayList<>(List.of(
                    "pdf",
                    "jpg",
                    "jpeg",
                    "png",
                    "docx",
                    "xlsx"
            ));

    private boolean createDirectoryIfMissing =
            true;
}