package dev.crt.user.service.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String plainPassword){
        return encoder.encode(plainPassword);
    }

    public static boolean matches (String plainPassword, String hashedPassword){
        return encoder.matches(plainPassword, hashedPassword);
    }
}
