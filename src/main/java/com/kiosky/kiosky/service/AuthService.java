package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.entity.Role;
import com.kiosky.kiosky.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 🔐 Servicio de Autenticación y Registro
 *
 * Centraliza toda la lógica relacionada con autenticación:
 * - Registro de usuario simple (CUSTOMER)
 * - Registro de usuario + tienda (OWNER)
 * - Login con validación de credenciales
 *
 * Este servicio es la capa de negocio entre AuthController y los servicios específicos
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserService appUserService;
    private final StoreService storeService;
    private final AuthenticationManager authenticationManager;

    // ══════════════════════════════════════════════════════════════
    // 📝 REGISTRO: Usuario Simple (CUSTOMER)
    // ══════════════════════════════════════════════════════════════
    /**
     * 🎯 Registra un usuario con rol CUSTOMER (sin tienda)
     *
     * Uso típico: Cliente que solo quiere comprar/navegar
     *
     * Flujo:
     * 1. Valida que el email no exista
     * 2. Crea usuario con rol CUSTOMER
     * 3. Hashea password automáticamente
     *
     * @param request Datos del usuario (fullName, email, password)
     * @return LoginResponse con datos básicos del usuario
     * @throws IllegalArgumentException si el email ya existe
     */
    @Transactional
    public LoginResponse registerCustomer(RegisterAppUserRequest request) {
        // Validar si el email ya existe
        if (appUserService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email: " + request.getEmail());
        }

        // Crear el usuario (AppUserService se encarga del hash)
        AppUserResponse createdUser = appUserService.createUser(request);

        // Construir respuesta
        return LoginResponse.builder()
                .message("Usuario registrado exitosamente")
                .fullName(createdUser.getFullName())
                .email(createdUser.getEmail())
                .role(Role.CUSTOMER)
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    // 🏪 REGISTRO: Usuario + Tienda (OWNER)
    // ══════════════════════════════════════════════════════════════
    /**
     * 🎯 Registra un usuario con tienda (rol OWNER)
     *
     * Uso típico: Dueño de negocio que quiere vender en la plataforma
     *
     * Flujo:
     * 1. Valida email único
     * 2. Valida dominio único
     * 3. Crea usuario con rol OWNER
     * 4. Crea tienda asociada al usuario
     * 5. Todo en una transacción (rollback si falla algo)
     *
     * @param request Datos del usuario y tienda (fullName, email, password, domain, themeSettings)
     * @return LoginResponse con datos del usuario propietario
     * @throws IllegalArgumentException si email o dominio ya existen
     */
    @Transactional
    public LoginResponse registerOwnerWithStore(RegisterStoreWithUserRequest request) {
        // 1. Validar que el email no esté en uso
        if (appUserService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email: " + request.getEmail());
        }

        // 2. Validar que el dominio no esté en uso
        if (storeService.existsByDomain(request.getDomain())) {
            throw new IllegalArgumentException("Ya existe una tienda con este dominio: " + request.getDomain());
        }

        // 3. Crear el usuario PRIMERO (sin tienda)
        RegisterAppUserRequest userRequest = new RegisterAppUserRequest();
        userRequest.setFullName(request.getFullName());
        userRequest.setEmail(request.getEmail());
        userRequest.setPassword(request.getPassword());

        AppUserResponse createdUserResponse = appUserService.createUser(userRequest);

        // 4. Obtener la entidad del usuario creado
        AppUser createdUser = appUserService.getUserEntityById(createdUserResponse.getId());

        // 5. Actualizar el rol a OWNER (ya que tendrá tienda)
        createdUser.setRole(Role.OWNER);
        appUserService.getUserEntityById(createdUser.getId()); // Sincronizar cambios

        // 6. Crear la tienda asociada al usuario
        RegisterStoreRequest storeRequest = new RegisterStoreRequest();
        storeRequest.setDomain(request.getDomain());
        storeRequest.setThemeSettings(request.getThemeSettings());

        storeService.createStore(storeRequest, createdUser);

        // 7. Retornar respuesta con datos actualizados
        AppUserResponse updatedUser = appUserService.getById(createdUser.getId());

        return LoginResponse.builder()
                .message("Usuario y tienda registrados exitosamente")
                .fullName(updatedUser.getFullName())
                .email(updatedUser.getEmail())
                .role(Role.OWNER)
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    // 🔑 LOGIN: Autenticación de Usuario
    // ══════════════════════════════════════════════════════════════
    /**
     * 🎯 Autentica un usuario existente
     *
     * Flujo:
     * 1. AuthenticationManager valida credenciales
     *    → Llama a CustomUserDetailsService
     *    → Compara password con BCrypt
     * 2. Si es correcto, obtiene datos del usuario
     * 3. Devuelve respuesta con información del usuario
     *
     * @param request Credenciales (email, password)
     * @return LoginResponse con datos del usuario autenticado
     * @throws BadCredentialsException si las credenciales son incorrectas
     * @throws AuthenticationException si hay otro error de autenticación
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 🔐 PASO 1: Autenticar con Spring Security
        // Esto valida internamente:
        // - Usuario existe (CustomUserDetailsService)
        // - Password coincide (PasswordEncoder)
        // - Cuenta no está bloqueada, etc.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // ✅ Si llegamos aquí, las credenciales son correctas

        // 📌 PASO 2: Obtener datos completos del usuario desde BD
        AppUser user = appUserService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + request.getEmail()));

        // 🎉 PASO 3: Construir respuesta exitosa
        return LoginResponse.builder()
                .message("Login exitoso")
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                // 🔮 En el futuro agregarás:
                // .token(jwtService.generateToken(authentication))
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    // 🔍 MÉTODOS AUXILIARES
    // ══════════════════════════════════════════════════════════════

    /**
     * Verifica si un email ya está registrado
     */
    public boolean emailExists(String email) {
        return appUserService.existsByEmail(email);
    }

    /**
     * Verifica si un dominio de tienda ya está registrado
     */
    public boolean domainExists(String domain) {
        return storeService.existsByDomain(domain);
    }
}
