package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.dto.LoginRequest;
import com.kiosky.kiosky.dto.LoginResponse;
import com.kiosky.kiosky.dto.RegisterAppUserRequest;
import com.kiosky.kiosky.dto.RegisterStoreWithUserRequest;
import com.kiosky.kiosky.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de Autenticación
 *
 * Endpoints:
 * - POST /kiosky/auth/register          → Registrar usuario CUSTOMER
 * - POST /kiosky/auth/register-owner    → Registrar usuario OWNER con tienda
 * - POST /kiosky/auth/login             → Autenticar usuario
 *
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ══════════════════════════════════════════════════════════════
    // 📝 ENDPOINT: REGISTRO DE USUARIO SIMPLE (CUSTOMER)
    // ══════════════════════════════════════════════════════════════
    /**
     * 🎯 Registra un nuevo usuario con rol CUSTOMER
     *
     * Request:
     * POST /kiosky/auth/register
     * {
     *   "fullName": "Juan Pérez",
     *   "email": "juan@example.com",
     *   "password": "miPassword123"
     * }
     *
     * Response exitosa (201):
     * {
     *   "message": "Usuario registrado exitosamente",
     *   "fullName": "Juan Pérez",
     *   "email": "juan@example.com",
     *   "role": "CUSTOMER"
     * }
     */
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
     *   "fullName": "María García",
     *   "email": "maria@example.com",
     *   "password": "miPassword123",
     *   "domain": "tienda-maria",
     *   "themeSettings": "{\"color\":\"blue\"}"
     * }
     *
     * Response exitosa (201):
     * {
     *   "message": "Usuario y tienda registrados exitosamente",
     *   "fullName": "María García",
     *   "email": "maria@example.com",
     *   "role": "OWNER"
     * }
     */
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
     *   "email": "juan@example.com",
     *   "password": "miPassword123"
     * }
     *
     * Response exitosa (200):
     * {
     *   "message": "Login exitoso",
     *   "fullName": "Juan Pérez",
     *   "email": "juan@example.com",
     *   "role": "CUSTOMER"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
