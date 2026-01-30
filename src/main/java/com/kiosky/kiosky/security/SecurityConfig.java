package com.kiosky.kiosky.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize, @PostAuthorize, etc.
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    // ══════════════════════════════════════════════════════════════
    // PASO 1: PasswordEncoder Bean
    // ══════════════════════════════════════════════════════════════
    /**
     * Encriptador de contraseñas con BCrypt.
     * 
     * Caracteristicas de BCrypt:
     * - Incluye "salt" automatico (proteccion contra rainbow tables)
     * - Es lento a proposito (proteccion contra fuerza bruta)
     * - Estandar de la industria para almacenamiento seguro de contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ══════════════════════════════════════════════════════════════
    // PASO 2: AuthenticationProvider Bean
    // ══════════════════════════════════════════════════════════════
    /**
     * Proveedor de Autenticacion - Verificador de credenciales.
     * 
     * Proceso de autenticacion:
     * 1. Recibe username + password del login
     * 2. Usa CustomUserDetailsService para buscar usuario en BD
     * 3. Usa PasswordEncoder para comparar contraseñas
     * 4. Si coinciden, autenticacion exitosa
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ══════════════════════════════════════════════════════════════
    // PASO 3: AuthenticationManager Bean
    // ══════════════════════════════════════════════════════════════
    /**
     * Manager de Autenticacion - Coordina el proceso de autenticacion.
     * 
     * Se utiliza en el endpoint de login para autenticar usuarios
     * antes de generar el token JWT.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ══════════════════════════════════════════════════════════════
    // PASO 4: SecurityFilterChain - Configuracion de Seguridad
    // ══════════════════════════════════════════════════════════════

    // Rutas publicas para OpenAPI/Swagger
    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    // Rutas publicas de autenticacion
    private static final String[] AUTH_WHITELIST = {
            "/auth/login",
            "/auth/register",
            "/auth/register-owner"
    };

    /**
     * Configura la cadena de filtros de seguridad.
     * 
     * Configuracion:
     * - CSRF deshabilitado (no necesario para APIs REST stateless)
     * - Sesiones stateless (no se mantiene estado en el servidor)
     * - Rutas publicas definidas en whitelist
     * - Todas las demas rutas requieren autenticacion
     * - Filtro JWT antes del filtro de autenticacion por usuario/password
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Habilitar CORS con la configuración personalizada
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Deshabilitar CSRF - No es necesario para APIs REST que usan tokens
            .csrf(csrf -> csrf.disable())
            
            // Configurar autorizacion de peticiones
            .authorizeHttpRequests(auth -> auth
                // Permitir acceso a Swagger UI y documentacion OpenAPI
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                // Permitir acceso a endpoints de autenticacion
                .requestMatchers(AUTH_WHITELIST).permitAll()
                // Todas las demas peticiones requieren autenticacion
                .anyRequest().authenticated()
            )
            
            // Configurar manejo de sesiones como stateless
            // El servidor no mantiene estado de sesion, cada peticion debe incluir el token
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configurar el proveedor de autenticacion
            .authenticationProvider(authenticationProvider())
            
            // Agregar el filtro JWT antes del filtro de autenticacion estandar
            // Esto permite que el JWT sea procesado primero
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
