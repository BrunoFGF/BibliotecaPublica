package com.backend.biblioteca.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String plainPassword = "password123";
        String hashedPassword = encoder.encode(plainPassword);

        System.out.println("Contraseña plana: " + plainPassword);
        System.out.println("Contraseña hasheada: " + hashedPassword);

        // Verificar que el hash funciona
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("¿Coincide? " + matches);

        // Probar con el hash que tienes en la BD
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P2.nRZ.Ud5zdtS";
        boolean matchesExisting = encoder.matches(plainPassword, existingHash);
        System.out.println("¿Coincide con el hash existente? " + matchesExisting);
    }
}