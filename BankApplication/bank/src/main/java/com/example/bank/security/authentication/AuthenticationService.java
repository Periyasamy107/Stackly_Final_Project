package com.example.bank.security.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    public Authentication authenticate(
            String username,
            String password) {

        return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken
                        .unauthenticated(username, password)
        );
    }
}