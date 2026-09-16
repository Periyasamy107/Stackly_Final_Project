package com.example.bank.admin.service;

import com.example.bank.common.enums.Role;
import com.example.bank.common.enums.UserStatus;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.common.exception.UnauthorizedException;
import com.example.bank.user.entity.User;
import com.example.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAccountServiceImpl
        implements AdminAccountService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void deactivateUser(Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User id is required"
            );
        }

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new UnauthorizedException(
                    "Authenticated administrator is required"
            );
        }

        User targetUser =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: "
                                                + userId
                                )
                        );


        String currentUsername =
                authentication.getName();


        if (currentUsername.equals(
                targetUser.getUsername())) {

            throw new UnauthorizedException(
                    "An administrator cannot deactivate their own account"
            );
        }


        boolean isAdministrator =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                ("ROLE_" + Role.ADMIN.name())
                                        .equals(
                                                authority.getAuthority()
                                        )
                        );

        if (!isAdministrator) {
            throw new UnauthorizedException(
                    "Administrator privileges are required"
            );
        }


        if (targetUser.getStatus()
                == UserStatus.INACTIVE) {

            return;
        }

        targetUser.setStatus(
                UserStatus.INACTIVE
        );

        userRepository.save(targetUser);
    }
}