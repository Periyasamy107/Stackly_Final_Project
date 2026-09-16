package com.example.bank.idempotency.filter;

import com.example.bank.common.exception.IdempotencyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IdempotencyKeyFilter
        extends OncePerRequestFilter {

    public static final String HEADER =
            "Idempotency-Key";

    public static final String REQUEST_ATTRIBUTE =
            "IDEMPOTENCY_KEY";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String method =
                request.getMethod();


        boolean mutating =
                "POST".equalsIgnoreCase(method)
                        || "PUT".equalsIgnoreCase(method)
                        || "PATCH".equalsIgnoreCase(method)
                        || "DELETE".equalsIgnoreCase(method);

        boolean restApi =
                request.getRequestURI()
                        .startsWith("/api/v1/");

        if (!mutating || !restApi) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String uri =
                request.getRequestURI();

        if (uri.startsWith("/api/v1/auth/")
                || uri.startsWith("/api/v1/audit-logs/")
                || uri.equals("/api/v1/audit-logs")
                || uri.startsWith("/api/v1/notifications")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String key =
                request.getHeader(HEADER);

        if (key != null) {

            key = key.trim();

            if (key.isBlank()
                    || key.length() > 100) {

                throw new IdempotencyException(
                        "Idempotency-Key must contain between "
                                + "1 and 100 characters"
                );
            }

            request.setAttribute(
                    REQUEST_ATTRIBUTE,
                    key
            );
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}