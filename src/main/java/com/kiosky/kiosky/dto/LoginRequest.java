package com.kiosky.kiosky.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 🔑 DTO para el request de login
 *
 * Ejemplo de uso:
 * POST /kiosky/auth/login
 * {
 *   "email": "ana@kiosky.com",
 *   "password": "miPassword123"
 * }
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
