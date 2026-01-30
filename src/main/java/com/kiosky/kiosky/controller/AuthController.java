package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.dto.LoginRequest;
import com.kiosky.kiosky.dto.LoginResponse;
import com.kiosky.kiosky.dto.RegisterAppUserRequest;
import com.kiosky.kiosky.dto.RegisterStoreWithUserRequest;
import com.kiosky.kiosky.dto.AppUserResponse;
import com.kiosky.kiosky.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Autenticación
 *
 * Endpoints:
 * - POST /kiosky/auth/register → Registrar usuario CUSTOMER
 * - POST /kiosky/auth/register-owner → Registrar usuario OWNER con tienda
 * - POST /kiosky/auth/login → Autenticar usuario
 *
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para registro y login de usuarios")
public class AuthController {

        private final AuthService authService;

        // ENDPOINT: OBTENER USUARIO AUTENTICADO
        /**
         * 🎯 Obtiene los detalles del usuario actualmente autenticado
         *
         * Request:
         * GET /kiosky/auth/me
         * Headers:
         * Authorization: Bearer <token_jwt>
         */
        @Operation(summary = "Obtener usuario autenticado", description = "Retorna los detalles del usuario actualmente autenticado usando el token JWT.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Usuario autenticado obtenido", content = @Content(schema = @Schema(implementation = AppUserResponse.class))),
                        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido")
        })
        @GetMapping("/me")
        @SecurityRequirement(name = "Bearer Authentication")
        public ResponseEntity<AppUserResponse> getAuthenticatedUser(Authentication authentication) {
                AppUserResponse response = authService.getAuthenticatedUser(authentication.getName());
                return ResponseEntity.ok(response);
        }

        // ══════════════════════════════════════════════════════════════
        // 📝 ENDPOINT: REGISTRO DE USUARIO SIMPLE (CUSTOMER)
        // ══════════════════════════════════════════════════════════════
        /**
         * 🎯 Registra un nuevo usuario con rol CUSTOMER
         *
         * Request:
         * POST /kiosky/auth/register
         * {
         * "fullName": "Juan Pérez",
         * "email": "juan@example.com",
         * "password": "miPassword123"
         * }
         *
         * Response exitosa (201):
         * {
         * "message": "Usuario registrado exitosamente",
         * "fullName": "Juan Pérez",
         * "email": "juan@example.com",
         * "role": "CUSTOMER"
         * }
         */
        @Operation(summary = "Registrar usuario cliente", description = "Registra un nuevo usuario con rol CUSTOMER. No requiere autenticación.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
                        @ApiResponse(responseCode = "409", description = "El email ya está registrado")
        })
        @PostMapping("/register")
        public ResponseEntity<LoginResponse> registerCustomer(@Valid @RequestBody RegisterAppUserRequest request) {
                LoginResponse response = authService.registerCustomer(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // ══════════════════════════════════════════════════════════════
        // 🏪 ENDPOINT: REGISTRO DE USUARIO CON TIENDA (OWNER)
        // ══════════════════════════════════════════════════════════════
        /**
         * 🎯 Registra un usuario con tienda (rol OWNER)
         *
         * Request:
         * POST /kiosky/auth/register-owner
         * {
         * "fullName": "María García",
         * "email": "maria@example.com",
         * "password": "miPassword123",
         * "domain": "tienda-maria",
         * "themeSettings": "{\"color\":\"blue\"}"
         * }
         *
         * Response exitosa (201):
         * {
         * "message": "Usuario y tienda registrados exitosamente",
         * "fullName": "María García",
         * "email": "maria@example.com",
         * "role": "OWNER"
         * }
         */
        @Operation(summary = "Registrar propietario con tienda", description = "Registra un nuevo usuario con rol OWNER y crea su tienda asociada. No requiere autenticación.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Usuario y tienda creados exitosamente", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
                        @ApiResponse(responseCode = "409", description = "El email o dominio ya están registrados")
        })
        @PostMapping("/register-owner")
        public ResponseEntity<LoginResponse> registerOwner(@Valid @RequestBody RegisterStoreWithUserRequest request) {
                LoginResponse response = authService.registerOwnerWithStore(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // ══════════════════════════════════════════════════════════════
        // 🔑 ENDPOINT: LOGIN DE USUARIO
        // ══════════════════════════════════════════════════════════════
        /**
         * 🎯 Autentica un usuario existente
         *
         * Request:
         * POST /kiosky/auth/login
         * {
         * "email": "juan@example.com",
         * "password": "miPassword123"
         * }
         *
         * Response exitosa (200):
         * {
         * "message": "Login exitoso",
         * "fullName": "Juan Pérez",
         * "email": "juan@example.com",
         * "role": "CUSTOMER"
         * }
         */
        @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT para usar en requests subsecuentes.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login exitoso", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
                        @ApiResponse(responseCode = "400", description = "Datos de login inválidos")
        })
        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
                LoginResponse response = authService.login(request);
                return ResponseEntity.ok(response);
        }
}
