package com.example.bank.user.service;

import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.user.dto.request.UserRequest;
import com.example.bank.user.dto.response.UserResponse;
import com.example.bank.user.entity.User;
import com.example.bank.user.mapper.UserMapper;
import com.example.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                    "Username already exists: " + request.getUsername()
            );
        }

        User user = userMapper.toEntity(request);

        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
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
    public UserResponse updateUser(
            Long id,
            UserRequest request
    ) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {

            throw new BusinessException(
                    "Username already exists: "
                            + request.getUsername()
            );
        }
        userMapper.updateEntity(request, user);

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {

            user.setPasswordHash(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }


    @Override
    @Cacheable(
            cacheNames = "userById",
            key = "#id"
    )
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );

        return userMapper.toResponse(user);
    }

    @Override
    @Cacheable(
            cacheNames = "userByUsername",
            key = "#username"
    )
    public UserResponse getUserByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with username: "
                                        + username
                        )
                );

        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
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
    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "User not found with id: " + id
            );
        }

        throw new BusinessException(
                "Physical deletion of users is not allowed"
        );
    }

}
