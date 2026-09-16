package com.example.bank.security.jwt;

import com.example.bank.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtTokenService implements JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${bank.security.jwt.issuer}")
    private String issuer;

    @Value("${bank.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Override
    public String generateAccessToken(
            Authentication authentication) {

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        Instant issuedAt = Instant.now();

        Instant expiresAt =
                issuedAt.plusSeconds(accessTokenExpiration);

        List<String> authorities =
                authentication.getAuthorities()
                        .stream()
                        .map(authority ->
                                authority.getAuthority())
                        .toList();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(issuer)
                        .subject(user.getUsername())
                        .issuedAt(issuedAt)
                        .expiresAt(expiresAt)
                        .claim("userId", user.getUserId())
                        .claim("role", user.getRole())
                        .claim("authorities", authorities)
                        .build();

        JwsHeader header =
                JwsHeader.with(SignatureAlgorithm.RS256)
                        .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}