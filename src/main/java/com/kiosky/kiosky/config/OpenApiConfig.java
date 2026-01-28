package com.kiosky.kiosky.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * 📚 Configuración de OpenAPI/Swagger para Kiosky API
 *
 * Esta configuración define:
 * - Información general de la API (título, versión, descripción)
 * - Esquema de seguridad JWT Bearer Token
 * - Servidores disponibles (desarrollo, producción)
 *
 * URLs de acceso:
 * - Swagger UI: http://localhost:8080/kiosky/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/kiosky/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/kiosky/v3/api-docs.yaml
 *
 * Para importar en Postman:
 * 1. Abrir Postman → Import → Link
 * 2. Pegar: http://localhost:8080/kiosky/v3/api-docs
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Kiosky API",
                version = "1.0.0",
                description = """
                        🏪 **Kiosky** - Sistema de Gestión de Tiendas Multi-Inquilino
                        
                        API RESTful para la gestión de tiendas, productos, categorías y usuarios.
                        
                        ## Características principales:
                        - 🔐 Autenticación JWT
                        - 🏪 Gestión multi-tienda
                        - 📦 Catálogo de productos
                        - 👥 Roles de usuario (ADMIN, OWNER, CUSTOMER)
                        
                        ## Autenticación:
                        1. Registrar usuario en `/auth/register` o `/auth/register-owner`
                        2. Iniciar sesión en `/auth/login` para obtener el token JWT
                        3. Usar el token en el header: `Authorization: Bearer <token>`
                        """,
                contact = @Contact(
                        name = "Kiosky Team",
                        email = "support@kiosky.com",
                        url = "https://github.com/JhormanSalazar/KioskyBackend"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        description = "Servidor de Desarrollo",
                        url = "http://localhost:8080/kiosky"
                ),
                @Server(
                        description = "Servidor de Producción",
                        url = "https://api.kiosky.com/kiosky"
                )
        },
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        description = """
                🔐 Autenticación JWT Bearer Token
                
                Para autenticarte:
                1. Haz login en `/auth/login` con tus credenciales
                2. Copia el token del response
                3. Haz clic en "Authorize" y pega: Bearer <tu_token>
                """,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // La configuración se realiza mediante anotaciones
    // No se requiere código adicional aquí
    
    /*
     * 📝 NOTAS DE ESCALABILIDAD:
     * 
     * 1. Para añadir más servidores (staging, QA):
     *    - Agregar más @Server en el array servers
     * 
     * 2. Para documentación por grupos/módulos:
     *    - Usar @Tag en los controladores
     *    - Configurar springdoc.group-configs en application.properties
     * 
     * 3. Para múltiples esquemas de autenticación:
     *    - Añadir más @SecurityScheme
     *    - Referenciar en @SecurityRequirement
     * 
     * 4. Para personalización avanzada, crear un bean OpenAPI:
     *    @Bean
     *    public OpenAPI customOpenAPI() {
     *        return new OpenAPI()
     *            .components(new Components())
     *            .info(new Info().title("API").version("1.0"));
     *    }
     */
}
