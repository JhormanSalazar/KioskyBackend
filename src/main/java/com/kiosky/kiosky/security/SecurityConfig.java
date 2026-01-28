package com.kiosky.kiosky.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor  // 🔥 Genera constructor con campos final
public class SecurityConfig {

    // 📌 Inyección de dependencia por constructor (inmutable)
    private final CustomUserDetailsService customUserDetailsService;

    // ══════════════════════════════════════════════════════════════
    // 🔐 PASO 1: PasswordEncoder Bean
    // ══════════════════════════════════════════════════════════════
    /**
     * 🛡️ Encriptador de contraseñas con BCrypt
     *
     * ¿Por qué BCrypt?
     * - Incluye "salt" automático (protección contra rainbow tables)
     * - Es lento a propósito (protección contra fuerza bruta)
     * - Estándar de la industria
     *
     * Ejemplo:
     * - Input: "miPassword123"
     * - Output: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ══════════════════════════════════════════════════════════════
    // 🔑 PASO 2: AuthenticationProvider Bean
    // ══════════════════════════════════════════════════════════════
    /**
     * 🎯 Proveedor de Autenticación - El "verificador de credenciales"
     *
     * ¿Qué hace?
     * 1. Recibe username + password del login
     * 2. Usa CustomUserDetailsService para buscar usuario en BD
     * 3. Usa PasswordEncoder para comparar contraseñas
     * 4. Si todo coincide → Autenticación exitosa ✅
     *
     * Flujo:
     * Login("ana@kiosky.com", "pass123")
     *   → DaoAuthenticationProvider
     *   → CustomUserDetailsService.loadUserByUsername("ana@kiosky.com")
     *   → PasswordEncoder.matches("pass123", "$2a$10$...")
     *   → ✅ Usuario autenticado
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder());
        provider.setUserDetailsService(customUserDetailsService);  // Cómo buscar usuarios
        return provider;
    }

    // ══════════════════════════════════════════════════════════════
    // 👮 PASO 3: AuthenticationManager Bean
    // ══════════════════════════════════════════════════════════════
    /**
     * 🎭 Manager de Autenticación - El "jefe" que coordina todo
     *
     * ¿Para qué sirve?
     * - Necesario para el endpoint de LOGIN
     * - Coordina uno o más AuthenticationProviders
     * - Lo usarás en tu AuthController
     *
     * Uso en AuthController:
     * authenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken(email, password)
     * );
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ══════════════════════════════════════════════════════════════
    // 🛡️ PASO 4: SecurityFilterChain - Configuración de Seguridad
    // ══════════════════════════════════════════════════════════════

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
            .authorizeHttpRequests(auth -> auth
                // ...existing code...
                .anyRequest().authenticated()
            )
            // 🔌 Conectar el AuthenticationProvider que configuramos arriba
            .authenticationProvider(authenticationProvider())
            // 🔐 HTTP Basic (temporal - después usarás JWT)
            .httpBasic(httpBasic -> {});

        return http.build();
    }
}
