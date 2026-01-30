package com.kiosky.kiosky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 🌐 Configuración de CORS (Cross-Origin Resource Sharing) para Kiosky API
 *
 * Esta configuración permite que aplicaciones frontend ejecutándose en diferentes
 * dominios/puertos puedan acceder a la API.
 *
 * Configuración actual:
 * - Frontend React/Vue/Angular en localhost:5173 (Vite)
 * - Permite métodos HTTP: GET, POST, PUT, DELETE, OPTIONS
 * - Permite headers personalizados incluyendo Authorization para JWT
 */
@Configuration
public class CorsConfig {

    /**
     * Configuración global de CORS para toda la aplicación.
     * 
     * @return CorsConfigurationSource configurado para desarrollo
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir orígenes específicos (Frontend)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173"  // Vite dev server Vue
        ));
        
        // Permitir métodos HTTP
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Permitir headers (incluyendo Authorization para JWT)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Exponer headers en la respuesta (útil para paginación, etc.)
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Permitir credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests (OPTIONS)
        configuration.setMaxAge(3600L);
        
        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}