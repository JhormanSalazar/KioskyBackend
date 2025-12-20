package com.kiosky.kiosky.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class PasswordUtils {

    private final PasswordEncoder passwordEncoder;

    // Patrón para validar contraseñas: mínimo 8 caracteres, al menos una mayúscula, una minúscula y un número
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$"
    );

    public PasswordUtils() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Valida si una contraseña cumple con los criterios de seguridad
     *
     * @param password La contraseña a validar
     * @return true si la contraseña es válida, false en caso contrario
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Hashea una contraseña usando BCrypt
     *
     * @param rawPassword La contraseña en texto plano
     * @return La contraseña hasheada
     * @throws IllegalArgumentException si la contraseña no es válida
     */
    public String hashPassword(String rawPassword) {
        if (!isValidPassword(rawPassword)) {
            throw new IllegalArgumentException(
                "La contraseña debe tener al menos 8 caracteres, incluyendo una mayúscula, una minúscula y un número"
            );
        }
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con la hasheada
     *
     * @param rawPassword La contraseña en texto plano
     * @param hashedPassword La contraseña hasheada
     * @return true si las contraseñas coinciden, false en caso contrario
     */
    public boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    /**
     * Obtiene los criterios de validación de contraseña como mensaje
     *
     * @return String con los criterios de validación
     */
    public String getPasswordCriteria() {
        return "La contraseña debe tener al menos 8 caracteres, incluyendo una mayúscula, una minúscula y un número";
    }
}
