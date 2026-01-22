package com.kiosky.kiosky.dto;

import com.kiosky.kiosky.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 🎉 DTO para la respuesta de login exitoso
 *
 * Contiene:
 * - message: Mensaje de éxito
 * - email: Email del usuario autenticado
 * - role: Rol del usuario (ADMIN, OWNER, EMPLOYEE, CUSTOMER)
 *
 * Ejemplo de respuesta:
 * {
 *   "message": "Login exitoso",
 *   "email": "ana@kiosky.com",
 *   "role": "OWNER"
 * }
 *
 * Nota: Más adelante agregarás el token JWT aquí
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    private String message;
    private String fullName;
    private String email;
    private Role role;

    // 🔮 En el futuro agregarás:
    // private String token;  // JWT token
}
