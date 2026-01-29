package com.kiosky.kiosky.dto;

import com.kiosky.kiosky.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la respuesta de login y registro exitoso.
 *
 * Contiene:
 * - message: Mensaje descriptivo del resultado
 * - email: Email del usuario autenticado
 * - fullName: Nombre completo del usuario
 * - role: Rol del usuario (ADMIN, OWNER, EMPLOYEE, CUSTOMER)
 * - token: JWT token para autenticacion en peticiones posteriores
 *
 * Ejemplo de respuesta:
 * {
 *   "message": "Login exitoso",
 *   "fullName": "Ana Garcia",
 *   "email": "ana@kiosky.com",
 *   "role": "OWNER",
 *   "token": "eyJhbGciOiJIUzI1NiJ9..."
 * }
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
    private String token;
}
