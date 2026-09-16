package com.example.bank.auth.service;

import com.example.bank.auth.dto.request.ChangePasswordRequest;
import com.example.bank.auth.dto.request.LoginRequest;
import com.example.bank.auth.dto.response.LoginResponse;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.common.exception.UnauthorizedException;
import com.example.bank.common.exception.ValidationException;
import com.example.bank.security.authentication.AuthenticationService;
import com.example.bank.security.authentication.CustomUserDetails;
import com.example.bank.security.jwt.JwtService;
import com.example.bank.user.entity.User;
import com.example.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl
        implements AuthService {

    private final AuthenticationService authenticationService;

    private final JwtService jwtService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    @Value("${bank.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Override
    @Transactional
    public LoginResponse login(
            LoginRequest request) {

        final Authentication authentication;

        try {

            authentication =
                    authenticationService.authenticate(
                            request.getUsername(),
                            request.getPassword()
                    );

        } catch (AuthenticationException exception) {

            throw new UnauthorizedException(
                    "Invalid username or password"
            );
        }

        CustomUserDetails userDetails =
                (CustomUserDetails)
                        authentication.getPrincipal();

        User user =
                userRepository.findById(
                                userDetails.getUserId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: "
                                                + userDetails.getUserId()
                                )
                        );

        String accessToken =
                jwtService.generateAccessToken(
                        authentication
                );

        String refreshToken =
                refreshTokenService.createRefreshToken(
                        user
                );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .refreshTokenExpiresIn(
                        refreshTokenService
                                .getRefreshTokenExpiration()
                )
                .build();
    }

    @Override
    @Transactional
    public void changePassword(
            ChangePasswordRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new UnauthorizedException(
                    "Authenticated user is required"
            );
        }

        String username =
                authentication.getName();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found: "
                                                + username
                                )
                        );

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash())) {

            throw new UnauthorizedException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPasswordHash())) {

            throw new ValidationException(
                    "New password must be different from the current password"
            );
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new ValidationException(
                    "New password and confirmation password do not match"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        refreshTokenService.revokeAll(user);
    }

    @Override
    public LoginResponse refresh(
            String refreshToken) {

        return refreshTokenService.refresh(
                refreshToken
        );
    }

    @Override
    public void logout(
            String refreshToken) {

        refreshTokenService.revoke(
                refreshToken
        );
    }
}