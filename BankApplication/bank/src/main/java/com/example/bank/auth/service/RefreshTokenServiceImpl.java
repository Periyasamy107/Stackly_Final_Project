package com.example.bank.auth.service;

import com.example.bank.auth.dto.response.LoginResponse;
import com.example.bank.auth.entity.RefreshToken;
import com.example.bank.auth.repository.RefreshTokenRepository;
import com.example.bank.common.exception.UnauthorizedException;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.security.authentication.CustomUserDetails;
import com.example.bank.security.jwt.JwtService;
import com.example.bank.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private static final int REFRESH_TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtService jwtService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Value("${bank.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${bank.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Override
    @Transactional
    public String createRefreshToken(User user) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "User is required to create a refresh token"
            );
        }

        String rawToken =
                generateRawToken();

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .user(user)
                        .tokenHash(
                                hashToken(rawToken)
                        )
                        .expiresAt(
                                DateTimeUtil.nowUtc()
                                        .plusSeconds(
                                                refreshTokenExpiration
                                        )
                        )
                        .build();

        refreshTokenRepository.save(
                refreshToken
        );

        return rawToken;
    }

    @Override
    @Transactional
    public LoginResponse refresh(
            String rawRefreshToken) {

        if (rawRefreshToken == null
                || rawRefreshToken.isBlank()) {

            throw new UnauthorizedException(
                    "Refresh token is required"
            );
        }

        String tokenHash =
                hashToken(rawRefreshToken);

        RefreshToken currentToken =
                refreshTokenRepository
                        .findByTokenHashForUpdate(
                                tokenHash
                        )
                        .orElseThrow(() ->
                                new UnauthorizedException(
                                        "Invalid refresh token"
                                )
                        );

        /*
         * A refresh token is single-use after rotation.
         */
        if (currentToken.isRevoked()) {

            revokeAll(
                    currentToken.getUser()
            );

            throw new UnauthorizedException(
                    "Refresh token reuse detected"
            );
        }

        if (currentToken.isExpired()) {

            currentToken.revoke();

            refreshTokenRepository.save(
                    currentToken
            );

            throw new UnauthorizedException(
                    "Refresh token has expired"
            );
        }

        User user =
                currentToken.getUser();

        if (user == null
                || user.getStatus() == null
                || !"ACTIVE".equals(
                user.getStatus().name()
        )) {

            throw new UnauthorizedException(
                    "User account is not active"
            );
        }

        /*
         * Generate replacement refresh token.
         */
        String newRawRefreshToken =
                generateRawToken();

        RefreshToken newRefreshToken =
                RefreshToken.builder()
                        .user(user)
                        .tokenHash(
                                hashToken(
                                        newRawRefreshToken
                                )
                        )
                        .expiresAt(
                                DateTimeUtil.nowUtc()
                                        .plusSeconds(
                                                refreshTokenExpiration
                                        )
                        )
                        .build();

        refreshTokenRepository.save(
                newRefreshToken
        );

        /*
         * Revoke the old token and link it
         * to the replacement token.
         */
        currentToken.revoke();

        currentToken.setReplacedByToken(
                newRefreshToken
        );

        refreshTokenRepository.save(
                currentToken
        );

        /*
         * IMPORTANT:
         *
         * JwtTokenService currently expects
         * CustomUserDetails as the principal.
         *
         * Therefore we must NOT create an
         * authentication object with a String
         * principal here.
         */
        CustomUserDetails userDetails =
                new CustomUserDetails(user);

        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String accessToken =
                jwtService.generateAccessToken(
                        authentication
                );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(
                        newRawRefreshToken
                )
                .tokenType("Bearer")
                .expiresIn(
                        accessTokenExpiration
                )
                .refreshTokenExpiresIn(
                        refreshTokenExpiration
                )
                .build();
    }

    @Override
    @Transactional
    public void revoke(
            String rawRefreshToken) {

        if (rawRefreshToken == null
                || rawRefreshToken.isBlank()) {
            return;
        }

        String tokenHash =
                hashToken(rawRefreshToken);

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional
    public void revokeAll(User user) {

        if (user == null
                || user.getId() == null) {
            return;
        }

        refreshTokenRepository.revokeAllByUserId(
                user.getId(),
                DateTimeUtil.nowUtc()
        );
    }

    @Override
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    private String generateRawToken() {

        byte[] bytes =
                new byte[REFRESH_TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(
            String rawToken) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to hash refresh token",
                    exception
            );
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {

        int deletedCount =
                refreshTokenRepository.deleteExpiredTokens(
                        DateTimeUtil.nowUtc()
                );

        log.info(
                "Refresh token cleanup completed. Deleted {} expired tokens",
                deletedCount
        );
    }
}