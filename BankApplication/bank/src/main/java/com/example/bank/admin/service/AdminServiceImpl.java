package com.example.bank.admin.service;

import com.example.bank.admin.dto.request.AdminRequest;
import com.example.bank.admin.dto.response.AdminResponse;
import com.example.bank.common.enums.Role;
import com.example.bank.common.enums.UserStatus;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.user.entity.User;
import com.example.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdminResponse createAdmin(
            AdminRequest request) {

        if (userRepository.existsByUsername(
                request.getUsername())) {

            throw new BusinessException(
                    "Username already exists: "
                            + request.getUsername()
            );
        }

        User admin = User.builder()
                .username(request.getUsername())
                .passwordHash(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        User savedAdmin =
                userRepository.save(admin);

        return toResponse(savedAdmin);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    "userById",
                    "userByUsername"
            },
            allEntries = true
    )
    public AdminResponse updateAdmin(
            Long id,
            AdminRequest request) {

        User admin =
                findAdmin(id);

        if (!admin.getUsername()
                .equals(request.getUsername())
                && userRepository.existsByUsername(
                request.getUsername())) {

            throw new BusinessException(
                    "Username already exists: "
                            + request.getUsername()
            );
        }

        admin.setUsername(
                request.getUsername()
        );

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            admin.setPasswordHash(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        return toResponse(admin);
    }

    @Override
    public AdminResponse getAdminById(Long id) {

        return toResponse(findAdmin(id));
    }

    @Override
    public List<AdminResponse> getAllAdmins() {

        return userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() == Role.ADMIN)
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    "userById",
                    "userByUsername"
            },
            allEntries = true
    )
    public void deactivateAdmin(Long id) {

        User admin = findAdmin(id);

        admin.setStatus(UserStatus.INACTIVE);
    }

    private User findAdmin(Long id) {

        User user =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Admin not found with id: "
                                                + id
                                ));

        if (user.getRole() != Role.ADMIN) {

            throw new ResourceNotFoundException(
                    "Admin not found with id: " + id
            );
        }

        return user;
    }

    private AdminResponse toResponse(User user) {

        return AdminResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}