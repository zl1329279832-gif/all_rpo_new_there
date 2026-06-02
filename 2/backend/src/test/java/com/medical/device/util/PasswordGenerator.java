package com.medical.device.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encoded = encoder.encode(rawPassword);
        System.out.println("Raw password: " + rawPassword);
        System.out.println("BCrypt hash: " + encoded);
        System.out.println("Matches: " + encoder.matches(rawPassword, encoded));
    }
}
