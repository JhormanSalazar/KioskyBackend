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
                // Permitir acceso público a endpoints de lectura
                .requestMatchers(HttpMethod.GET, "/kiosky/stores/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/kiosky/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/kiosky/categories/**").permitAll()

                // Permitir registro sin autenticación
                .requestMatchers(HttpMethod.POST, "/kiosky/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/kiosky/auth/login").permitAll()

                // Endpoints administrativos - Solo ADMIN
                .requestMatchers("/kiosky/admin/**").hasRole("ADMIN")

                // Gestión de tiendas - OWNER o superior
                .requestMatchers(HttpMethod.POST, "/kiosky/stores/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/kiosky/stores/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/kiosky/stores/**").hasAnyRole("OWNER", "ADMIN")

                // Gestión de productos - EMPLOYEE o superior
                .requestMatchers(HttpMethod.POST, "/kiosky/products/**").hasAnyRole("EMPLOYEE", "OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/kiosky/products/**").hasAnyRole("EMPLOYEE", "OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/kiosky/products/**").hasAnyRole("EMPLOYEE", "OWNER", "ADMIN")

                // Gestión de categorías - EMPLOYEE o superior
                .requestMatchers(HttpMethod.POST, "/kiosky/categories/**").hasAnyRole("EMPLOYEE", "OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/kiosky/categories/**").hasAnyRole("EMPLOYEE", "OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/kiosky/categories/**").hasAnyRole("EMPLOYEE", "OWNER", "ADMIN")

                // Permitir acceso a endpoints comunes sin autenticación
                .requestMatchers("/actuator/**").permitAll() // Actuator endpoints
                .requestMatchers("/swagger-ui/**").permitAll() // Swagger UI
                .requestMatchers("/v3/api-docs/**").permitAll() // OpenAPI docs
                .requestMatchers("/favicon.ico").permitAll() // Favicon
                .requestMatchers("/error").permitAll() // Error page

                // Requerir autenticación para otros métodos HTTP
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {}); // Usar autenticación HTTP Basic para métodos protegidos

        return http.build();
    }
}
