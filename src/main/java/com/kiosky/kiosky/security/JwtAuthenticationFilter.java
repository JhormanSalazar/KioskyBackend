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
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Prefijo estandar para tokens Bearer en el header Authorization
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    // Lista de rutas que NO requieren procesamiento de JWT
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/register-owner",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    );

    /**
     * Verifica si la ruta actual es pública y no requiere procesamiento de JWT.
     */
    private boolean isPublicPath(String requestPath) {
        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }

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
        
        // Si es una ruta pública, saltar el procesamiento JWT completamente
        String requestPath = request.getRequestURI();
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el header Authorization
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        // Si no hay header o no empieza con "Bearer ", continuar sin autenticacion
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
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
        } catch (Exception e) {
            // En caso de error procesando el JWT, simplemente continuar sin autenticar
            // Spring Security se encargará de rechazar el request si el endpoint requiere autenticación
            logger.debug("Error procesando JWT: " + e.getMessage());
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}

