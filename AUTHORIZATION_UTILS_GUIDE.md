# AuthorizationUtils - Guía de Uso

## Descripción

`AuthorizationUtils` es un componente Spring reutilizable que proporciona métodos para verificar los permisos del usuario actualmente autenticado sobre recursos de la aplicación (tiendas, categorías y productos).

## Características

- ✅ **Inyectable**: Es un `@Component` de Spring, se puede inyectar en cualquier servicio
- ✅ **Reutilizable**: Métodos genéricos que funcionan en cualquier contexto
- ✅ **Basado en Spring Security**: Utiliza el `SecurityContextHolder` para obtener el usuario autenticado
- ✅ **Seguro**: Maneja excepciones y casos de usuarios no autenticados
- ✅ **Flexible**: Múltiples sobrecargas de métodos para diferentes casos de uso

## Instalación

El componente ya está creado en `src/main/java/com/kiosky/kiosky/util/AuthorizationUtils.java`.

Para usarlo, simplemente inyéctalo en tu servicio:

```java
@Service
@AllArgsConstructor
public class MiServicio {
    private final AuthorizationUtils authorizationUtils;
    // ... otros campos
}
```

## Métodos Principales

### 1. Obtener Usuario Autenticado

```java
// Obtiene el usuario actualmente autenticado desde SecurityContext
AppUser currentUser = authorizationUtils.getCurrentAuthenticatedUser();

// Obtiene el usuario desde un objeto Authentication específico
AppUser user = authorizationUtils.getAuthenticatedUser(authentication);
```

### 2. Verificar Permisos sobre Tiendas

```java
// Verificar si el usuario puede modificar una tienda (por ID)
boolean canModify = authorizationUtils.canModifyStore(storeId);

// Verificar con la entidad Store completa
boolean canModify = authorizationUtils.canModifyStore(store);

// Verificar con un usuario específico
boolean canModify = authorizationUtils.canModifyStore(user, storeId);
```

**Reglas de Negocio:**
- `ADMIN`: puede modificar cualquier tienda
- `OWNER`: solo puede modificar su propia tienda
- `EMPLOYEE/CUSTOMER`: no pueden modificar tiendas

### 3. Verificar Permisos sobre Categorías

```java
// Verificar si el usuario puede modificar una categoría
boolean canModify = authorizationUtils.canModifyCategory(categoryId, storeId);

// Verificar con la entidad Category completa
boolean canModify = authorizationUtils.canModifyCategory(category);

// Verificar con un usuario específico
boolean canModify = authorizationUtils.canModifyCategory(user, storeId);
```

**Reglas de Negocio:**
- `ADMIN`: puede modificar cualquier categoría
- `OWNER`: solo puede modificar categorías de su tienda
- `EMPLOYEE`: solo puede modificar categorías de su tienda
- `CUSTOMER`: no puede modificar categorías

### 4. Verificar Permisos sobre Productos

```java
// Verificar si el usuario puede modificar un producto
boolean canModify = authorizationUtils.canModifyProduct(productId, storeId);

// Verificar con la entidad Product completa
boolean canModify = authorizationUtils.canModifyProduct(product);

// Verificar con un usuario específico
boolean canModify = authorizationUtils.canModifyProduct(user, storeId);
```

**Reglas de Negocio:**
- `ADMIN`: puede modificar cualquier producto
- `OWNER`: solo puede modificar productos de su tienda
- `EMPLOYEE`: solo puede modificar productos de su tienda
- `CUSTOMER`: no puede modificar productos

### 5. Métodos de Verificación de Roles

```java
// Verificar si el usuario es ADMIN
boolean isAdmin = authorizationUtils.isAdmin();

// Verificar si el usuario es OWNER
boolean isOwner = authorizationUtils.isOwner();

// Verificar si el usuario es OWNER de una tienda específica
boolean isOwnerOfStore = authorizationUtils.isOwnerOfStore(storeId);

// Verificar si el usuario es EMPLOYEE
boolean isEmployee = authorizationUtils.isEmployee();

// Obtener el ID de la tienda del usuario (si tiene una)
Long storeId = authorizationUtils.getCurrentUserStoreId();
```

## Ejemplos de Uso en Servicios

### Ejemplo 1: CategoryService - Actualizar Categoría

```java
@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AuthorizationUtils authorizationUtils;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        // Obtener la categoría
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // ✅ VERIFICAR PERMISOS
        if (!authorizationUtils.canModifyCategory(category)) {
            throw new SecurityException("No tienes permisos para modificar esta categoría");
        }

        // Actualizar la categoría
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(updatedCategory);
    }
}
```

### Ejemplo 2: ProductService - Crear Producto

```java
@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorizationUtils authorizationUtils;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        // Obtener la categoría
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // ✅ VERIFICAR PERMISOS - verificar que el usuario puede modificar esta categoría
        if (!authorizationUtils.canModifyCategory(category)) {
            throw new SecurityException("No tienes permisos para crear productos en esta categoría");
        }

        // Crear el producto
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponseDto(savedProduct);
    }
}
```

### Ejemplo 3: StoreService - Eliminar Tienda

```java
@Service
@AllArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final AuthorizationUtils authorizationUtils;

    @Transactional
    public void deleteStore(Long id) {
        // Verificar que la tienda existe
        if (!storeRepository.existsById(id)) {
            throw new EntityNotFoundException("Tienda no encontrada");
        }

        // ✅ VERIFICAR PERMISOS
        if (!authorizationUtils.canModifyStore(id)) {
            throw new SecurityException("No tienes permisos para eliminar esta tienda");
        }

        storeRepository.deleteById(id);
    }
}
```

### Ejemplo 4: ProductService - Listar Productos del Usuario

```java
@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final AuthorizationUtils authorizationUtils;
    private final ProductMapper productMapper;

    /**
     * Obtiene todos los productos de la tienda del usuario autenticado
     */
    public List<ProductResponse> getMyProducts() {
        // ✅ Obtener el ID de la tienda del usuario actual
        Long storeId = authorizationUtils.getCurrentUserStoreId();
        
        if (storeId == null) {
            throw new IllegalStateException("El usuario no tiene una tienda asignada");
        }

        // Obtener productos de esa tienda
        List<Product> products = productRepository.findByStoreId(storeId);
        return productMapper.toResponseDtoList(products);
    }
}
```

### Ejemplo 5: Uso con Authentication en Controladores

Si prefieres pasar el objeto `Authentication` desde el controlador:

```java
@RestController
@RequestMapping("/kiosky/categories")
@AllArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final AuthorizationUtils authorizationUtils;

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateCategoryRequest request,
            Authentication authentication) {
        
        // Obtener el usuario desde Authentication
        AppUser user = authorizationUtils.getAuthenticatedUser(authentication);
        
        // Pasar el usuario al servicio si es necesario
        return ResponseEntity.ok(categoryService.update(id, request, user));
    }
}
```

## Manejo de Excepciones

El componente lanza las siguientes excepciones:

- `IllegalStateException`: Cuando no hay usuario autenticado o no se encuentra en la BD
- `SecurityException`: (Recomendado usar en tus servicios) Cuando el usuario no tiene permisos

Es recomendable capturar estas excepciones en tu `GlobalExceptionHandler`:

```java
@ExceptionHandler(SecurityException.class)
public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e) {
    return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("Acceso Denegado", e.getMessage()));
}
```

## Ventajas de usar AuthorizationUtils

1. **Centralización**: Toda la lógica de autorización está en un solo lugar
2. **Reutilización**: No necesitas repetir código en cada servicio
3. **Mantenibilidad**: Si cambian las reglas de negocio, solo modificas un archivo
4. **Testeable**: Fácil de mockear en pruebas unitarias
5. **Type-Safe**: Trabaja con entidades y tipos de Java, no con strings
6. **Consistencia**: Garantiza que las reglas de autorización se apliquen de forma consistente

## Testing

Ejemplo de cómo mockear en pruebas:

```java
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    
    @Mock
    private AuthorizationUtils authorizationUtils;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private CategoryService categoryService;
    
    @Test
    void shouldThrowExceptionWhenUserCannotModifyCategory() {
        // Given
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(authorizationUtils.canModifyCategory(category)).thenReturn(false);
        
        // When & Then
        assertThrows(SecurityException.class, () -> 
            categoryService.update(categoryId, new UpdateCategoryRequest())
        );
    }
}
```

## Próximos Pasos

Para integrar completamente este componente:

1. Actualiza tus servicios existentes para usar `AuthorizationUtils`
2. Agrega manejo de `SecurityException` en tu `GlobalExceptionHandler`
3. Escribe pruebas unitarias para tus servicios usando el componente
4. Considera agregar logging para auditoría de accesos

## Notas Importantes

- Este componente **NO reemplaza** la configuración de Spring Security en `SecurityConfig`
- Spring Security maneja la autenticación a nivel de HTTP, `AuthorizationUtils` maneja autorización a nivel de lógica de negocio
- Es importante verificar permisos en la capa de servicio, no solo en los controladores
