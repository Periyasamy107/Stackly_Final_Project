package com.example.bank.auth.service;

import com.example.bank.auth.dto.response.LoginResponse;
import com.example.bank.user.entity.User;

public interface RefreshTokenService {

    String createRefreshToken(User user);

    LoginResponse refresh(String refreshToken);

    void revoke(String refreshToken);

    void revokeAll(User user);

    long getRefreshTokenExpiration();

    void cleanupExpiredTokens();
}