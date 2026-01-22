# Tutorial de Spring Security para Principiantes

## 🔍 Estado Actual del Proyecto

### Configuración Encontrada:
Tu proyecto actualmente tiene una configuración **básica** de Spring Security con las siguientes características:

1. **Dependencia**: `spring-boot-starter-security` ✅
2. **Configuración actual**: Clase `SecurityConfig` con configuración básica
3. **Autenticación**: HTTP Basic con credenciales hardcodeadas en `application.properties`
4. **Autorización**: Todos los GET públicos, otros métodos requieren autenticación

### Problemas de la Configuración Actual:
❌ **Usuario hardcodeado** en properties (no seguro para producción)  
❌ **Solo HTTP Basic** (no es user-friendly)  
❌ **Sin gestión de roles** basada en base de datos  
❌ **Sin JWT tokens** (no escalable para APIs REST)  
❌ **CSRF deshabilitado** globalmente  

---

## 🎯 ¿Qué es Spring Security y Para Qué Sirve?

Spring Security es un framework que proporciona **autenticación** y **autorización** para aplicaciones Java.

### 🔐 Conceptos Clave:

**1. AUTENTICACIÓN** = "¿Quién eres?"
- Verificar que el usuario es quien dice ser
- Login con usuario/contraseña, tokens, etc.

**2. AUTORIZACIÓN** = "¿Qué puedes hacer?"
- Verificar qué recursos puede acceder un usuario
- Basado en roles (ADMIN, USER) o permisos específicos

**3. PRINCIPALES COMPONENTES:**

#### 🛡️ SecurityFilterChain
**¿Qué es?** Una cadena de filtros que procesa cada petición HTTP  
**¿Para qué sirve?** Intercepta requests antes de llegar a tus controllers  
**Ejemplo:** Verificar si el usuario está autenticado

#### 🔑 AuthenticationManager
**¿Qué es?** El "jefe" que coordina la autenticación  
**¿Para qué sirve?** Decide si las credenciales son válidas  

#### 👤 UserDetailsService
**¿Qué es?** Servicio que busca información del usuario  
**¿Para qué sirve?** Conectar Spring Security con tu base de datos  

#### 🎭 PasswordEncoder
**¿Qué es?** Encriptador de contraseñas  
**¿Para qué sirve?** Nunca guardar contraseñas en texto plano  

---

## 📚 Tutorial Paso a Paso

### PASO 1: Configurar PasswordEncoder

El primer paso es **NUNCA** guardar contraseñas en texto plano.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**¿Por qué BCrypt?**
- Algoritmo muy seguro
- Incluye "salt" automáticamente
- Estándar en la industria

### PASO 2: Crear UserDetailsService Personalizado

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserService appUserService; // Tu servicio existente

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar usuario en tu base de datos
        AppUser user = appUserService.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword()) // Ya debe estar encriptada
            .authorities(getAuthorities(user.getRole()))
            .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
```

### PASO 3: Configurar AuthenticationProvider

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }
}
```

### PASO 4: Configurar SecurityFilterChain Completa

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // Para APIs REST
        .authorizeHttpRequests(auth -> auth
            // Endpoints públicos
            .requestMatchers("/kiosky/auth/**").permitAll() // Login/Register
            .requestMatchers(HttpMethod.GET, "/kiosky/stores/**").permitAll() // Ver tiendas
            .requestMatchers(HttpMethod.GET, "/kiosky/categories/**").permitAll() // Ver categorías
            .requestMatchers(HttpMethod.GET, "/kiosky/products/**").permitAll() // Ver productos
            
            // Endpoints para usuarios autenticados
            .requestMatchers(HttpMethod.POST, "/kiosky/stores/**").hasRole("USER") // Crear tienda
            .requestMatchers(HttpMethod.PUT, "/kiosky/stores/**").hasRole("OWNER") // Editar tienda
            
            // Endpoints solo para admins
            .requestMatchers("/kiosky/admin/**").hasRole("ADMIN")
            
            // Todo lo demás requiere autenticación
            .anyRequest().authenticated()
        )
        .authenticationProvider(authenticationProvider())
        .httpBasic(httpBasic -> {}); // Mantener HTTP Basic por ahora

    return http.build();
}
```

### PASO 5: Implementar JWT (Opcional pero Recomendado)

Para APIs REST modernas, JWT es mejor que HTTP Basic:

#### 5.1 Agregar dependencia JWT:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

#### 5.2 Crear JwtService:
```java
@Service
public class JwtService {

    private static final String SECRET_KEY = "tu-clave-secreta-muy-larga-y-segura";
    private static final int EXPIRATION_TIME = 86400000; // 24 horas

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ... más métodos
}
```

#### 5.3 Crear JwtAuthenticationFilter:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

#### 5.4 Agregar JWT Filter a SecurityConfig:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // ... configuración anterior ...
        )
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

### PASO 6: Crear Endpoints de Autenticación

```java
@RestController
@RequestMapping("/kiosky/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(), 
                request.getPassword()
            )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String jwt = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(jwt));
    }
}
```

---

## 🔧 Mejores Prácticas

### ✅ DO (Hacer):
1. **Siempre encriptar contraseñas** con BCrypt o Argon2
2. **Usar JWT para APIs REST** (mejor que sessions)
3. **Implementar roles granulares** (ADMIN, OWNER, USER)
4. **Validar tokens en cada request**
5. **Usar HTTPS en producción**
6. **Configurar CORS apropiadamente**
7. **Logs de seguridad** para auditoría

### ❌ DON'T (No Hacer):
1. **Nunca hardcodear credenciales** en código
2. **No usar contraseñas en texto plano**
3. **No exponer información sensible** en logs
4. **No confiar solo en el frontend** para seguridad
5. **No usar secretos débiles** para JWT

---

## 🏗️ Estructura de Roles Recomendada

```java
public enum Role {
    ADMIN,    // Administrador del sistema
    OWNER,    // Dueño de tienda
    EMPLOYEE, // Empleado de tienda
    CUSTOMER  // Cliente/Usuario normal
}
```

### Permisos por Rol:

**CUSTOMER:**
- ✅ Ver productos/categorías/tiendas
- ✅ Registrarse

**EMPLOYEE:**
- ✅ Todo lo de CUSTOMER
- ✅ Gestionar productos de su tienda

**OWNER:**
- ✅ Todo lo de EMPLOYEE
- ✅ Gestionar su tienda
- ✅ Gestionar empleados

**ADMIN:**
- ✅ Todo lo anterior
- ✅ Gestionar todas las tiendas
- ✅ Gestionar usuarios

---

## 🚀 Próximos Pasos

1. **Implementar la configuración paso a paso**
2. **Probar cada endpoint** con Postman
3. **Agregar validaciones adicionales**
4. **Implementar refresh tokens**
5. **Configurar rate limiting**
6. **Auditoría y logging**

---

## 🆘 Problemas Comunes y Soluciones

**Error: "Bad credentials"**
- Verificar que la contraseña esté encriptada correctamente
- Comprobar el UserDetailsService

**Error: "Access Denied"**
- Verificar que el usuario tenga el rol correcto
- Comprobar la configuración de authorizeHttpRequests

**JWT no funciona**
- Verificar que el header Authorization esté presente
- Comprobar que el token no haya expirado
- Verificar la clave secreta

---

*¡Recuerda: La seguridad es un proceso continuo, no un destino!* 🔒
