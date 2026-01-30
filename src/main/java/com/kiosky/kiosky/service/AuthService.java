package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.entity.Role;
import com.kiosky.kiosky.dto.*;
import com.kiosky.kiosky.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Servicio de Autenticacion y Registro.
 *
 * Centraliza toda la logica relacionada con autenticacion:
 * - Registro de usuario simple (CUSTOMER)
 * - Registro de usuario + tienda (OWNER)
 * - Login con validacion de credenciales y generacion de JWT
 *
 * Este servicio es la capa de negocio entre AuthController y los servicios especificos.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserService appUserService;
    private final StoreService storeService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Obtener usuario auntenticado
  public AppUserResponse getAuthenticatedUser(String email) {
    AppUser user = appUserService.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario autenticado no encontrado: " + email));
    
    Long storeId = (user.getStore() != null) ? user.getStore().getId() : null;
    
    return new AppUserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            storeId
    );
}
    
    // ══════════════════════════════════════════════════════════════
    // REGISTRO: Usuario Simple (CUSTOMER)
    // ══════════════════════════════════════════════════════════════
    /**
     * Registra un usuario con rol CUSTOMER (sin tienda).
     *
     * Uso tipico: Cliente que solo quiere comprar/navegar
     *
     * Flujo:
     * 1. Valida que el email no exista
     * 2. Crea usuario con rol CUSTOMER
     * 3. Hashea password automaticamente
     * 4. Genera token JWT para el nuevo usuario
     *
     * @param request Datos del usuario (fullName, email, password)
     * @return LoginResponse con datos del usuario y token JWT
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

        // Generar token JWT para el nuevo usuario
        String token = generateTokenForUser(createdUser.getEmail(), Role.CUSTOMER);

        // Construir respuesta
        return LoginResponse.builder()
                .message("Usuario registrado exitosamente")
                .fullName(createdUser.getFullName())
                .email(createdUser.getEmail())
                .role(Role.CUSTOMER)
                .token(token)
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    // REGISTRO: Usuario + Tienda (OWNER)
    // ══════════════════════════════════════════════════════════════
    /**
     * Registra un usuario con tienda (rol OWNER).
     *
     * Uso tipico: Dueño de negocio que quiere vender en la plataforma
     *
     * Flujo:
     * 1. Valida email unico
     * 2. Valida dominio unico
     * 3. Crea usuario con rol OWNER
     * 4. Crea tienda asociada al usuario
     * 5. Genera token JWT
     * 6. Todo en una transaccion (rollback si falla algo)
     *
     * @param request Datos del usuario y tienda (fullName, email, password, domain, themeSettings)
     * @return LoginResponse con datos del usuario propietario y token JWT
     * @throws IllegalArgumentException si email o dominio ya existen
     */
    @Transactional
    public LoginResponse registerOwnerWithStore(RegisterStoreWithUserRequest request) {
        // 1. Validar que el email no este en uso
        if (appUserService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email: " + request.getEmail());
        }

        // 2. Validar que el dominio no este en uso (normalizando primero)
        String normalizedDomain = storeService.normalizeDomain(request.getDomain());
        if (storeService.existsByDomain(normalizedDomain)) {
            throw new IllegalArgumentException("Ya existe una tienda con este dominio: " + normalizedDomain);
        }

        // 3. Crear el usuario PRIMERO (sin tienda)
        RegisterAppUserRequest userRequest = new RegisterAppUserRequest();
        userRequest.setFullName(request.getFullName());
        userRequest.setEmail(request.getEmail());
        userRequest.setPassword(request.getPassword());

        AppUserResponse createdUserResponse = appUserService.createUser(userRequest);

        // 4. Obtener la entidad del usuario creado
        AppUser createdUser = appUserService.getUserEntityById(createdUserResponse.getId());

        // 5. Actualizar el rol a OWNER (ya que tendra tienda)
        createdUser.setRole(Role.OWNER);
        appUserService.getUserEntityById(createdUser.getId()); // Sincronizar cambios

        // 6. Crear la tienda asociada al usuario
        RegisterStoreRequest storeRequest = new RegisterStoreRequest();
        storeRequest.setDomain(request.getDomain());
        storeRequest.setThemeSettings(request.getThemeSettings());

        storeService.createStore(storeRequest, createdUser);

        // 7. Obtener datos actualizados del usuario
        AppUserResponse updatedUser = appUserService.getById(createdUser.getId());

        // 8. Generar token JWT
        String token = generateTokenForUser(updatedUser.getEmail(), Role.OWNER);

        return LoginResponse.builder()
                .message("Usuario y tienda registrados exitosamente")
                .fullName(updatedUser.getFullName())
                .email(updatedUser.getEmail())
                .role(Role.OWNER)
                .token(token)
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    // LOGIN: Autenticacion de Usuario
    // ══════════════════════════════════════════════════════════════
    /**
     * Autentica un usuario existente y genera un token JWT.
     *
     * Flujo:
     * 1. AuthenticationManager valida credenciales
     *    - Llama a CustomUserDetailsService
     *    - Compara password con BCrypt
     * 2. Si es correcto, obtiene datos del usuario
     * 3. Genera token JWT
     * 4. Devuelve respuesta con informacion del usuario y token
     *
     * @param request Credenciales (email, password)
     * @return LoginResponse con datos del usuario autenticado y token JWT
     * @throws BadCredentialsException si las credenciales son incorrectas
     * @throws AuthenticationException si hay otro error de autenticacion
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // PASO 1: Autenticar con Spring Security
        // Esto valida internamente:
        // - Usuario existe (CustomUserDetailsService)
        // - Password coincide (PasswordEncoder)
        // - Cuenta no esta bloqueada, etc.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Si llegamos aqui, las credenciales son correctas

        // PASO 2: Obtener datos completos del usuario desde BD
        AppUser user = appUserService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + request.getEmail()));

        // PASO 3: Generar token JWT
        String token = generateTokenForUser(user.getEmail(), user.getRole());

        // PASO 4: Construir respuesta exitosa
        return LoginResponse.builder()
                .message("Login exitoso")
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }

    // ══════════════════════════════════════════════════════════════
    // METODOS AUXILIARES
    // ══════════════════════════════════════════════════════════════

    /**
     * Genera un token JWT para el usuario especificado.
     * 
     * @param email Email del usuario
     * @param role Rol del usuario
     * @return Token JWT generado
     */
    private String generateTokenForUser(String email, Role role) {
        UserDetails userDetails = User.builder()
                .username(email)
                .password("") // No necesario para generar el token
                .authorities("ROLE_" + role.name())
                .build();
        return jwtService.generateToken(userDetails);
    }

    /**
     * Verifica si un email ya esta registrado.
     */
    public boolean emailExists(String email) {
        return appUserService.existsByEmail(email);
    }

    /**
     * Verifica si un dominio de tienda ya esta registrado.
     */
    public boolean domainExists(String domain) {
        String normalizedDomain = storeService.normalizeDomain(domain);
        return storeService.existsByDomain(normalizedDomain);
    }
}
