package com.example.bank;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class JwtKeyGenerator {

    public static void main(String[] args) throws Exception {

        KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("RSA");

        keyPairGenerator.initialize(2048);

        KeyPair keyPair =
                keyPairGenerator.generateKeyPair();

        String privateKeyBase64 =
                Base64.getEncoder()
                        .encodeToString(
                                keyPair.getPrivate().getEncoded()
                        );

        String publicKeyBase64 =
                Base64.getEncoder()
                        .encodeToString(
                                keyPair.getPublic().getEncoded()
                        );

        System.out.println();
        System.out.println("==================================================");
        System.out.println("JWT RSA KEY PAIR");
        System.out.println("==================================================");
        System.out.println();

        System.out.println("JWT_PRIVATE_KEY_BASE64=");
        System.out.println(privateKeyBase64);

        System.out.println();
        System.out.println("JWT_PUBLIC_KEY_BASE64=");
        System.out.println(publicKeyBase64);

        System.out.println();
        System.out.println("==================================================");
        System.out.println("IMPORTANT:");
        System.out.println("1. Do not add quotation marks.");
        System.out.println("2. Do not add PEM headers/footers.");
        System.out.println("3. Keep each value as one continuous Base64 string.");
        System.out.println("4. Do not expose the private key.");
        System.out.println("==================================================");
    }
}