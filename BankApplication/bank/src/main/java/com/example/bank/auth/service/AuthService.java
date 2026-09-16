package com.example.bank.auth.service;

import com.example.bank.auth.dto.request.ChangePasswordRequest;
import com.example.bank.auth.dto.request.LoginRequest;
import com.example.bank.auth.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void changePassword(ChangePasswordRequest request);

    LoginResponse refresh(String refreshToken);

    void logout(String refreshToken);
}