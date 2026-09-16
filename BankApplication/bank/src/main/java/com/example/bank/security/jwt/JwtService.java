package com.example.bank.security.jwt;

import org.springframework.security.core.Authentication;

public interface JwtService {

    String generateAccessToken(Authentication authentication);

}
