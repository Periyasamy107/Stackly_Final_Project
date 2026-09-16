package com.example.bank.common.audit;

import com.example.bank.security.authentication.CustomUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl
        implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication
                instanceof AnonymousAuthenticationToken) {

            return Optional.empty();
        }

        Object principal =
                authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {

            return Optional.of(
                    userDetails.getUsername()
            );
        }

        if (principal instanceof String username
                && !username.isBlank()
                && !"anonymousUser".equals(username)) {

            return Optional.of(username);
        }

        return Optional.empty();
    }
}