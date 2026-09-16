package com.example.bank.user.dto.request;

import com.example.bank.common.enums.Role;
import com.example.bank.common.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    private Long id;

    @NotBlank(message = "Username is required")
    @Size(
            min = 4,
            max = 100,
            message = "Username must be between 4 and 100 characters"
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 72,
            message = "Password must be between 8 and 72 characters"
    )
    @StrongPassword
    private String password;

    @NotNull(message = "Role is required")
    private Role role;
}