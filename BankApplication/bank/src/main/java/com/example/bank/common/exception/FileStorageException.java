package com.example.bank.common.exception;

public class FileStorageException
        extends ApplicationException {

    public static final String ERROR_CODE =
            "FILE_STORAGE_ERROR";

    public FileStorageException(
            String message) {

        super(
                message,
                ERROR_CODE
        );
    }

    public FileStorageException(
            String message,
            Throwable cause) {

        super(
                message,
                ERROR_CODE,
                cause
        );
    }
}