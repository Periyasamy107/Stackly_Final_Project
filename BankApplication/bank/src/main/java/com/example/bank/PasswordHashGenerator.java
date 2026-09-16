package com.example.bank;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {

        String password = "Admin@123";

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String encodedPassword = encoder.encode(password);

        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + encodedPassword);
    }

//  ==============================
//    Initial admin password
//  ==============================
//    Password: Admin@123
//    BCrypt Hash: $2a$10$1PIAJHrn8kLtJtR2qMRfMeyay5BqmhniVOiuS5ZX3389H9IiEQ3ni

}