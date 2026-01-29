package com.kiosky.kiosky.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticacion JWT.
 * 
 * Este filtro se ejecuta una vez por cada peticion HTTP y es responsable de:
 * 1. Extraer el token JWT del header Authorization
 * 2. Validar el token
 * 3. Cargar los detalles del usuario
 * 4. Establecer la autenticacion en el SecurityContext
 * 
 * Flujo del filtro:
 * Request -> JwtAuthenticationFilter -> SecurityFilterChain -> Controller
 * 
 * Si el token es valido, el usuario queda autenticado para el resto de la peticion.
 * Si el token es invalido o no existe, la peticion continua sin autenticacion
 * y sera rechazada por Spring Security si el endpoint requiere autenticacion.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Prefijo estandar para tokens Bearer en el header Authorization
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Metodo principal del filtro que procesa cada peticion.
     * 
     * @param request La peticion HTTP entrante
     * @param response La respuesta HTTP
     * @param filterChain Cadena de filtros para continuar el procesamiento
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Extraer el header Authorization
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        // Si no hay header o no empieza con "Bearer ", continuar sin autenticacion
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token (quitar "Bearer " del inicio)
        final String jwt = authHeader.substring(BEARER_PREFIX.length());
        
        // Extraer el email (username) del token
        final String userEmail = jwtService.extractUsername(jwt);

        // Validar que tenemos un email y que no hay autenticacion previa en el contexto
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Cargar los detalles del usuario desde la base de datos
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Validar el token contra los detalles del usuario
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // Crear el objeto de autenticacion
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No necesitamos credenciales, ya validamos el token
                        userDetails.getAuthorities()
                );

                // Agregar detalles adicionales de la peticion
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer la autenticacion en el SecurityContext
                // Esto hace que el usuario este autenticado para el resto de la peticion
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}

