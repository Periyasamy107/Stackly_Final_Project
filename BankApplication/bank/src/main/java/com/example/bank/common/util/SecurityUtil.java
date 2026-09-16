package com.example.bank.common.util;

import com.example.bank.security.authentication.CustomUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class SecurityUtil {

    private static final String ROLE_PREFIX = "ROLE_";

    private static final String USER_ID_CLAIM = "userId";

    private static final String ROLE_CLAIM = "role";

    private SecurityUtil() {
    }


    public static Optional<Authentication> getAuthentication() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null) {
            return Optional.empty();
        }

        return Optional.of(authentication);
    }


    public static boolean isAuthenticated() {

        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .filter(authentication ->
                        !(authentication
                                instanceof AnonymousAuthenticationToken))
                .isPresent();
    }


    public static boolean isAnonymous() {

        return !isAuthenticated();
    }


    public static Optional<String> getCurrentUsername() {

        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .filter(authentication ->
                        !(authentication
                                instanceof AnonymousAuthenticationToken))
                .map(Authentication::getName)
                .filter(name ->
                        name != null && !name.isBlank());
    }


    public static Optional<Long> getCurrentUserId() {

        Optional<Authentication> authentication =
                getAuthentication()
                        .filter(Authentication::isAuthenticated)
                        .filter(current ->
                                !(current
                                        instanceof AnonymousAuthenticationToken));

        if (authentication.isEmpty()) {
            return Optional.empty();
        }

        Object principal =
                authentication.get().getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.ofNullable(
                    userDetails.getUserId()
            );
        }

        if (principal instanceof Jwt jwt) {
            return extractLongClaim(
                    jwt,
                    USER_ID_CLAIM
            );
        }

        return Optional.empty();
    }


    public static Optional<Object> getCurrentPrincipal() {

        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .filter(authentication ->
                        !(authentication
                                instanceof AnonymousAuthenticationToken))
                .map(Authentication::getPrincipal)
                .filter(Objects::nonNull);
    }


    public static Optional<String> getCurrentRole() {

        Optional<Authentication> authentication =
                getAuthentication()
                        .filter(Authentication::isAuthenticated)
                        .filter(current ->
                                !(current
                                        instanceof AnonymousAuthenticationToken));

        if (authentication.isEmpty()) {
            return Optional.empty();
        }

        Authentication current =
                authentication.get();

        Object principal =
                current.getPrincipal();

        if (principal instanceof Jwt jwt) {

            String role =
                    jwt.getClaimAsString(ROLE_CLAIM);

            if (role != null && !role.isBlank()) {
                return Optional.of(normalizeRole(role));
            }
        }

        return current.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .filter(SecurityUtil::isRoleAuthority)
                .map(SecurityUtil::normalizeRole)
                .findFirst();
    }


    public static boolean hasRole(
            String role) {

        if (role == null || role.isBlank()) {
            return false;
        }

        String normalizedRole =
                normalizeRole(role);

        return getCurrentRole()
                .map(currentRole ->
                        currentRole.equalsIgnoreCase(
                                normalizedRole
                        ))
                .orElse(false);
    }


    public static boolean hasAuthority(
            String authority) {

        if (authority == null || authority.isBlank()) {
            return false;
        }

        String expected =
                authority.trim();

        return getAuthentication()
                .map(Authentication::getAuthorities)
                .stream()
                .flatMap(Collection::stream)
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(current ->
                        current.equals(expected));
    }


    public static boolean hasAnyRole(
            String... roles) {

        if (roles == null || roles.length == 0) {
            return false;
        }

        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }

        return false;
    }


    public static boolean hasAllRoles(
            String... roles) {

        if (roles == null || roles.length == 0) {
            return false;
        }

        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }

        return true;
    }


    public static String requireCurrentUsername() {

        return getCurrentUsername()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No authenticated user is available"
                        ));
    }


    public static Long requireCurrentUserId() {

        return getCurrentUserId()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user ID is unavailable"
                        ));
    }


    public static String requireCurrentRole() {

        return getCurrentRole()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user role is unavailable"
                        ));
    }

    private static Optional<Long> extractLongClaim(
            Jwt jwt,
            String claimName) {

        Object value =
                jwt.getClaim(claimName);

        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Number number) {
            return Optional.of(
                    number.longValue()
            );
        }

        if (value instanceof String stringValue) {

            try {
                return Optional.of(
                        Long.parseLong(
                                stringValue
                        )
                );
            } catch (NumberFormatException exception) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private static boolean isRoleAuthority(
            String authority) {

        return authority.startsWith(
                ROLE_PREFIX
        );
    }

    private static String normalizeRole(
            String role) {

        String normalized =
                role.trim()
                        .toUpperCase();

        if (normalized.startsWith(ROLE_PREFIX)) {
            return normalized.substring(
                    ROLE_PREFIX.length()
            );
        }

        return normalized;
    }
}