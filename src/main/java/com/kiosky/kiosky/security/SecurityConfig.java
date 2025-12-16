package com.kiosky.kiosky.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
            .authorizeHttpRequests(auth -> auth
                // Permitir acceso público a todos los GET mappings
                .requestMatchers(HttpMethod.GET, "/**").permitAll()

                // Permitir acceso a endpoints comunes sin autenticación
                .requestMatchers("/actuator/**").permitAll() // Actuator endpoints
                .requestMatchers("/swagger-ui/**").permitAll() // Swagger UI
                .requestMatchers("/v3/api-docs/**").permitAll() // OpenAPI docs
                .requestMatchers("/favicon.ico").permitAll() // Favicon
                .requestMatchers("/error").permitAll() // Error page

                // Requerir autenticación para otros métodos HTTP (POST, PUT, DELETE, etc.)
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {}); // Usar autenticación HTTP Basic para métodos protegidos

        return http.build();
    }
}
