package com.example.bank.auth.controller.rest;

import com.example.bank.auth.dto.request.ChangePasswordRequest;
import com.example.bank.auth.dto.request.LoginRequest;
import com.example.bank.auth.dto.request.RefreshTokenRequest;
import com.example.bank.auth.dto.response.LoginResponse;
import com.example.bank.auth.service.AuthService;
import com.example.bank.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Authentication and credential lifecycle operations"
)
public class AuthRestController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user and returns access and refresh tokens."
    )
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Changes the authenticated user's password and revokes existing refresh tokens."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Password changed successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "423",
                    description = "User account is locked or inactive"
            )
    })
    @SecurityRequirement(name = "")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody
            ChangePasswordRequest request) {

        authService.changePassword(
                request
        );

        return ResponseEntity.noContent()
                .build();
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Rotates the refresh token and issues a new access token."
    )
    @SecurityRequirement(name = "")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody
            RefreshTokenRequest request) {

        LoginResponse response =
                authService.refresh(
                        request.getRefreshToken()
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody
            RefreshTokenRequest request) {

        authService.logout(
                request.getRefreshToken()
        );

        return ResponseEntity.noContent()
                .build();
    }
}