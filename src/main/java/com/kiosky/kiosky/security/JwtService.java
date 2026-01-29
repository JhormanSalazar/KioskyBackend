package com.kiosky.kiosky.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para la gestion de JWT (JSON Web Tokens).
 * 
 * Responsabilidades:
 * - Generar tokens JWT para usuarios autenticados
 * - Validar tokens recibidos en las peticiones
 * - Extraer informacion (claims) de los tokens
 * 
 * El token generado contiene:
 * - subject: email del usuario (identificador unico)
 * - issuedAt: fecha de creacion
 * - expiration: fecha de expiracion
 * - claims adicionales: rol del usuario
 */
@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ══════════════════════════════════════════════════════════════
    // GENERACION DE TOKENS
    // ══════════════════════════════════════════════════════════════

    /**
     * Genera un token JWT para el usuario especificado.
     * 
     * @param userDetails Detalles del usuario autenticado
     * @return Token JWT como String
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token JWT con claims adicionales.
     * 
     * @param extraClaims Claims adicionales para incluir en el token
     * @param userDetails Detalles del usuario autenticado
     * @return Token JWT como String
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Construye el token JWT con todos los parametros especificados.
     * 
     * Estructura del token:
     * - Header: algoritmo de firma (HS256)
     * - Payload: claims (subject, iat, exp, claims adicionales)
     * - Signature: firma digital con la clave secreta
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        long currentTimeMillis = System.currentTimeMillis();
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(currentTimeMillis))
                .expiration(new Date(currentTimeMillis + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ══════════════════════════════════════════════════════════════
    // VALIDACION DE TOKENS
    // ══════════════════════════════════════════════════════════════

    /**
     * Valida si un token es valido para el usuario especificado.
     * 
     * Un token es valido si:
     * 1. El subject (email) coincide con el username del UserDetails
     * 2. El token no ha expirado
     * 3. La firma es correcta (verificado implicitamente al parsear)
     * 
     * @param token Token JWT a validar
     * @param userDetails Detalles del usuario contra el que validar
     * @return true si el token es valido, false en caso contrario
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Verifica si el token ha expirado.
     * 
     * @param token Token JWT a verificar
     * @return true si el token ha expirado, false si aun es valido
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ══════════════════════════════════════════════════════════════
    // EXTRACCION DE CLAIMS
    // ══════════════════════════════════════════════════════════════

    /**
     * Extrae el username (email) del token.
     * 
     * @param token Token JWT
     * @return Email del usuario contenido en el token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiracion del token.
     * 
     * @param token Token JWT
     * @return Fecha de expiracion
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim especifico del token usando una funcion extractora.
     * 
     * @param token Token JWT
     * @param claimsResolver Funcion para extraer el claim deseado
     * @return Valor del claim extraido
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token.
     * 
     * Este metodo parsea y verifica el token:
     * - Verifica la firma con la clave secreta
     * - Valida el formato del token
     * - Extrae el payload (claims)
     * 
     * @param token Token JWT
     * @return Claims contenidos en el token
     * @throws ExpiredJwtException si el token ha expirado
     * @throws JwtException si el token es invalido o la firma no coincide
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ══════════════════════════════════════════════════════════════
    // UTILIDADES
    // ══════════════════════════════════════════════════════════════

    /**
     * Obtiene la clave de firma para los tokens.
     * 
     * La clave se deriva del secretKey configurado en application.properties.
     * Se utiliza el algoritmo HMAC-SHA256 para la firma.
     * 
     * @return SecretKey para firmar/verificar tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Obtiene el tiempo de expiracion configurado.
     * 
     * @return Tiempo de expiracion en milisegundos
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }
}

