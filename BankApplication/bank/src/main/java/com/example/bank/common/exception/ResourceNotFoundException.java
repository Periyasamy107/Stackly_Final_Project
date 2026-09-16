package com.example.bank.common.exception;

public class ResourceNotFoundException
        extends ApplicationException {

    public static final String ERROR_CODE =
            "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(
            String message) {

        super(
                message,
                ERROR_CODE
        );
    }

    public ResourceNotFoundException(
            String resourceName,
            Object resourceId) {

        super(
                resourceName
                        + " not found: "
                        + resourceId,
                ERROR_CODE
        );
    }
}