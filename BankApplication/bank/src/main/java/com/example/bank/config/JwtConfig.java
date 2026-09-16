package com.example.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Value("${bank.security.jwt.private-key}")
    private String privateKey;

    @Value("${bank.security.jwt.public-key}")
    private String publicKey;

    @Value("${bank.security.jwt.issuer}")
    private String issuer;

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        try {
            byte[] decoded =
                    Base64.getDecoder().decode(privateKey);

            PKCS8EncodedKeySpec keySpec =
                    new PKCS8EncodedKeySpec(decoded);

            return (RSAPrivateKey) KeyFactory
                    .getInstance("RSA")
                    .generatePrivate(keySpec);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load JWT private key",
                    exception
            );
        }
    }

    @Bean
    public RSAPublicKey rsaPublicKey() {
        try {
            byte[] decoded =
                    Base64.getDecoder().decode(publicKey);

            X509EncodedKeySpec keySpec =
                    new X509EncodedKeySpec(decoded);

            return (RSAPublicKey) KeyFactory
                    .getInstance("RSA")
                    .generatePublic(keySpec);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load JWT public key",
                    exception
            );
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(
            RSAPrivateKey privateKey,
            RSAPublicKey publicKey) {

        return NimbusJwtEncoder
                .withKeyPair(publicKey, privateKey)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            RSAPublicKey publicKey) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withPublicKey(publicKey)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        issuer
                )
        );

        return decoder;
    }
}