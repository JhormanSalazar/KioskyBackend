# 📚 Guía de OpenAPI / Swagger UI

## 🎯 Descripción

Este proyecto incluye **SpringDoc OpenAPI** para documentación automática de la API REST.
Swagger UI proporciona una interfaz interactiva para explorar y probar los endpoints.

---

## 🔗 URLs de Acceso

Una vez el servidor esté corriendo:

| Recurso | URL |
|---------|-----|
| **Swagger UI** | http://localhost:8080/kiosky/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/kiosky/v3/api-docs |
| **OpenAPI YAML** | http://localhost:8080/kiosky/v3/api-docs.yaml |

---

## 📦 Importar en Postman

### Opción 1: Importar desde URL (Recomendado)

1. Abre Postman
2. Click en **Import** (esquina superior izquierda)
3. Selecciona la pestaña **Link**
4. Pega: `http://localhost:8080/kiosky/v3/api-docs`
5. Click en **Continue** → **Import**

### Opción 2: Importar archivo JSON

1. Accede a `http://localhost:8080/kiosky/v3/api-docs`
2. Copia todo el contenido JSON
3. Guarda como `kiosky-api.json`
4. En Postman: **Import** → **File** → selecciona el archivo

---

## 🔐 Autenticación JWT

La API usa autenticación JWT Bearer Token:

### Flujo de Autenticación:

1. **Registrar usuario** (sin autenticación):
   ```
   POST /kiosky/auth/register
   POST /kiosky/auth/register-owner
   ```

2. **Iniciar sesión**:
   ```
   POST /kiosky/auth/login
   ```
   Response incluye el `token` JWT.

3. **Usar el token**:
   - En Swagger UI: Click en **Authorize** → pega `Bearer <tu_token>`
   - En Postman: Header `Authorization: Bearer <tu_token>`

---

## 📁 Estructura de Archivos

```
src/main/java/com/kiosky/kiosky/
├── config/
│   └── OpenApiConfig.java      # Configuración central de OpenAPI
├── controller/
│   ├── AuthController.java     # @Tag: Autenticación
│   ├── AppUserController.java  # @Tag: Usuarios
│   ├── StoreController.java    # @Tag: Tiendas
│   ├── CategoryController.java # @Tag: Categorías
│   └── ProductController.java  # @Tag: Productos
└── ...

src/main/resources/
└── application.properties      # Configuración de springdoc
```

---

## ⚙️ Configuración (application.properties)

```properties
## OpenAPI / Swagger Configuration
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.operations-sorter=method
springdoc.swagger-ui.tags-sorter=alpha
springdoc.swagger-ui.try-it-out-enabled=true
springdoc.swagger-ui.filter=true
springdoc.swagger-ui.doc-expansion=none
```

---

## 🔧 Opciones de Configuración Avanzada

### Deshabilitar en Producción

```properties
# En application-prod.properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

### Agrupar Endpoints por Módulo

```properties
# Crear grupos separados
springdoc.group-configs[0].group=auth
springdoc.group-configs[0].paths-to-match=/auth/**

springdoc.group-configs[1].group=products
springdoc.group-configs[1].paths-to-match=/api/products/**
```

---

## 🏷️ Anotaciones OpenAPI Utilizadas

| Anotación | Propósito |
|-----------|-----------|
| `@Tag` | Agrupa endpoints en categorías |
| `@Operation` | Documenta un endpoint específico |
| `@ApiResponse` | Define respuestas esperadas |
| `@Parameter` | Documenta parámetros de path/query |
| `@Schema` | Define estructura de DTOs |
| `@SecurityRequirement` | Indica autenticación requerida |

---

## 📝 Ejemplo de Documentación en Controlador

```java
@Tag(name = "Productos", description = "Gestión del catálogo")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
public class ProductController {

    @Operation(
        summary = "Crear producto",
        description = "Crea un nuevo producto en el catálogo"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> create(...) { }
}
```

---

## 🚀 Comandos Útiles

```bash
# Compilar el proyecto
./mvnw compile

# Ejecutar la aplicación
./mvnw spring-boot:run

# Verificar que Swagger está activo
curl http://localhost:8080/kiosky/v3/api-docs | head
```

---

## 📚 Referencias

- [SpringDoc OpenAPI](https://springdoc.org/)
- [OpenAPI Specification](https://spec.openapis.org/oas/v3.1.0)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
