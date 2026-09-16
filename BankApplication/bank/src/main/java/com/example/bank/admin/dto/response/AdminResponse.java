package com.example.bank.admin.dto.response;

import com.example.bank.common.enums.UserStatus;
import lombok.*;

        import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResponse {

    private Long id;
    private String username;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}