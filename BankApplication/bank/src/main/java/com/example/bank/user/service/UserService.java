package com.example.bank.user.service;

import com.example.bank.user.dto.request.UserRequest;
import com.example.bank.user.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long id, UserRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    List<UserResponse> getAllUsers();

    void deleteUser(Long id);

}
