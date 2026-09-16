package com.example.bank.security.authorization;

import com.example.bank.customer.repository.CustomerRepository;
import com.example.bank.file.entity.FileMetadata;
import com.example.bank.file.repository.FileMetadataRepository;
import com.example.bank.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileMetadataAuthorizationService {

    private final FileMetadataRepository fileMetadataRepository;
    private final CustomerRepository customerRepository;

    public boolean canUploadForCustomer(
            Long customerId) {

        Authentication authentication =
                getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (hasRole(authentication, "ADMIN")
                || hasRole(authentication, "EMPLOYEE")) {

            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        CustomUserDetails details =
                getUserDetails(authentication);

        if (details == null) {
            return false;
        }

        return customerRepository
                .findById(customerId)
                .map(customer ->
                        customer.getUser()
                                .getId()
                                .equals(details.getUserId()))
                .orElse(false);
    }

    public boolean canAccessFile(
            Long fileId) {

        Authentication authentication =
                getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (hasRole(authentication, "ADMIN")
                || hasRole(authentication, "EMPLOYEE")) {

            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        CustomUserDetails details =
                getUserDetails(authentication);

        if (details == null) {
            return false;
        }

        return fileMetadataRepository
                .findById(fileId)
                .map(FileMetadata::getCustomer)
                .map(customer ->
                        customer.getUser()
                                .getId()
                                .equals(details.getUserId()))
                .orElse(false);
    }

    public boolean canAccessCustomerFiles(
            Long customerId) {

        Authentication authentication =
                getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (hasRole(authentication, "ADMIN")
                || hasRole(authentication, "EMPLOYEE")) {

            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        CustomUserDetails details =
                getUserDetails(authentication);

        if (details == null) {
            return false;
        }

        return customerRepository
                .findById(customerId)
                .map(customer ->
                        customer.getUser()
                                .getId()
                                .equals(details.getUserId()))
                .orElse(false);
    }

    public boolean canDeleteFile(
            Long fileId) {

        Authentication authentication =
                getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (hasRole(authentication, "ADMIN")) {
            return true;
        }

        if (hasRole(authentication, "EMPLOYEE")) {
            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        CustomUserDetails details =
                getUserDetails(authentication);

        if (details == null) {
            return false;
        }

        return fileMetadataRepository
                .findById(fileId)
                .map(FileMetadata::getCustomer)
                .map(customer ->
                        customer.getUser()
                                .getId()
                                .equals(details.getUserId()))
                .orElse(false);
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }

    private boolean isAuthenticated(
            Authentication authentication) {

        return authentication != null
                && authentication.isAuthenticated();
    }

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_" + role));
    }

    private CustomUserDetails getUserDetails(
            Authentication authentication) {

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof CustomUserDetails details) {
            return details;
        }

        return null;
    }
}