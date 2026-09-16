package com.example.bank.auth.dto.request;

import com.example.bank.common.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(
            min = 8,
            max = 72,
            message = "New password must be between 8 and 72 characters"
    )
    @StrongPassword
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Size(
            min = 8,
            max = 72,
            message = "Password confirmation must be between 8 and 72 characters"
    )
    private String confirmPassword;
}