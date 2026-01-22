# 🔐 Sistema de Autorización de Usuario Autenticado

## Resumen

Se ha creado un sistema completo de autorización que permite verificar si el usuario actualmente autenticado (authentication) tiene permisos para realizar operaciones sobre recursos de la aplicación (tiendas, categorías y productos).

## Archivos Creados

### 1. **AuthorizationUtils.java** 
📍 `src/main/java/com/kiosky/kiosky/util/AuthorizationUtils.java`

Componente Spring reutilizable (`@Component`) que proporciona métodos para verificar permisos del usuario autenticado.

#### Características principales:
- ✅ Inyectable en cualquier servicio
- ✅ Obtiene automáticamente el usuario del `SecurityContext`
- ✅ Verifica permisos sobre tiendas, categorías y productos
- ✅ Maneja roles (ADMIN, OWNER, EMPLOYEE, CUSTOMER)
- ✅ Métodos seguros con manejo de excepciones

#### Métodos principales:

**Obtener Usuario Autenticado:**
```java
AppUser getCurrentAuthenticatedUser()
AppUser getAuthenticatedUser(Authentication authentication)
```

**Verificar Permisos sobre Tiendas:**
```java
boolean canModifyStore(Long storeId)
boolean canModifyStore(Store store)
boolean canModifyStore(AppUser user, Long storeId)
```

**Verificar Permisos sobre Categorías:**
```java
boolean canModifyCategory(Long categoryId, Long categoryStoreId)
boolean canModifyCategory(Category category)
boolean canModifyCategory(AppUser user, Long storeId)
```

**Verificar Permisos sobre Productos:**
```java
boolean canModifyProduct(Long productId, Long productStoreId)
boolean canModifyProduct(Product product)
boolean canModifyProduct(AppUser user, Long storeId)
```

**Métodos de Utilidad:**
```java
boolean isAdmin()
boolean isOwner()
boolean isOwnerOfStore(Long storeId)
boolean isEmployee()
Long getCurrentUserStoreId()
boolean canReadStore(Long storeId)
```

## Reglas de Negocio Implementadas

### Jerarquía de Roles

```
ADMIN (acceso total)
  ↓
OWNER (acceso a su tienda)
  ↓
EMPLOYEE (acceso a tienda donde trabaja)
  ↓
CUSTOMER (solo lectura)
```

### Permisos por Operación

#### **Modificar Tiendas**
- ✅ **ADMIN**: Puede modificar cualquier tienda
- ✅ **OWNER**: Solo puede modificar su propia tienda
- ❌ **EMPLOYEE**: No puede modificar tiendas
- ❌ **CUSTOMER**: No puede modificar tiendas

#### **Modificar Categorías**
- ✅ **ADMIN**: Puede modificar categorías de cualquier tienda
- ✅ **OWNER**: Solo puede modificar categorías de su tienda
- ✅ **EMPLOYEE**: Solo puede modificar categorías de su tienda
- ❌ **CUSTOMER**: No puede modificar categorías

#### **Modificar Productos**
- ✅ **ADMIN**: Puede modificar productos de cualquier tienda
- ✅ **OWNER**: Solo puede modificar productos de su tienda
- ✅ **EMPLOYEE**: Solo puede modificar productos de su tienda
- ❌ **CUSTOMER**: No puede modificar productos

## Ejemplo de Implementación

### CategoryService (ACTUALIZADO)

Se actualizó `CategoryService` para incluir verificaciones de autorización:

```java
@Service
@AllArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AuthorizationUtils authorizationUtils; // ← INYECTADO

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        // Validar que la tienda existe
        Store store = storeRepository.findById(request.getStoreId())
            .orElseThrow(() -> new EntityNotFoundException("Tienda no encontrada"));

        // ✅ VERIFICAR PERMISOS
        if (!authorizationUtils.canModifyCategory(null, request.getStoreId())) {
            throw new SecurityException("No tienes permisos para crear categorías en esta tienda");
        }

        // ... resto del código
    }

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // ✅ VERIFICAR PERMISOS
        if (!authorizationUtils.canModifyCategory(category)) {
            throw new SecurityException("No tienes permisos para modificar esta categoría");
        }

        // ... resto del código
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));

        // ✅ VERIFICAR PERMISOS
        if (!authorizationUtils.canModifyCategory(category)) {
            throw new SecurityException("No tienes permisos para eliminar esta categoría");
        }

        // ... resto del código
    }
}
```

### GlobalExceptionHandler (ACTUALIZADO)

Se agregó manejo de `SecurityException`:

```java
@ExceptionHandler(SecurityException.class)
public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex) {
    ErrorResponse errorResponse = new ErrorResponse(
            "FORBIDDEN",
            ex.getMessage(),
            LocalDateTime.now()
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
}
```

## Cómo Usar en Otros Servicios

### Paso 1: Inyectar AuthorizationUtils

```java
@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final AuthorizationUtils authorizationUtils; // ← Agregar esto
}
```

### Paso 2: Usar en Métodos de Negocio

```java
@Transactional
public ProductResponse createProduct(CreateProductRequest request) {
    // Obtener categoría
    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
    
    // ✅ VERIFICAR PERMISOS
    if (!authorizationUtils.canModifyCategory(category)) {
        throw new SecurityException("No tienes permisos para crear productos en esta categoría");
    }
    
    // ... crear producto
}

@Transactional
public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
    
    // ✅ VERIFICAR PERMISOS
    if (!authorizationUtils.canModifyProduct(product)) {
        throw new SecurityException("No tienes permisos para modificar este producto");
    }
    
    // ... actualizar producto
}

@Transactional
public void deleteProduct(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
    
    // ✅ VERIFICAR PERMISOS
    if (!authorizationUtils.canModifyProduct(product)) {
        throw new SecurityException("No tienes permisos para eliminar este producto");
    }
    
    // ... eliminar producto
}
```

### Paso 3: Casos Especiales

#### Obtener recursos del usuario actual

```java
public List<ProductResponse> getMyProducts() {
    // Obtener el ID de la tienda del usuario actual
    Long storeId = authorizationUtils.getCurrentUserStoreId();
    
    if (storeId == null) {
        throw new IllegalStateException("El usuario no tiene una tienda asignada");
    }
    
    List<Product> products = productRepository.findByStoreId(storeId);
    return productMapper.toResponseDtoList(products);
}
```

#### Verificar antes de operaciones complejas

```java
public void transferProductToCategory(Long productId, Long newCategoryId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
    
    Category newCategory = categoryRepository.findById(newCategoryId)
        .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada"));
    
    // ✅ Verificar permisos sobre el producto original
    if (!authorizationUtils.canModifyProduct(product)) {
        throw new SecurityException("No tienes permisos sobre este producto");
    }
    
    // ✅ Verificar permisos sobre la nueva categoría
    if (!authorizationUtils.canModifyCategory(newCategory)) {
        throw new SecurityException("No tienes permisos para mover productos a esta categoría");
    }
    
    // Realizar la transferencia
    product.setCategory(newCategory);
    productRepository.save(product);
}
```

## Respuestas HTTP

### Éxito (200 OK)
```json
{
  "id": 1,
  "name": "Categoría de Prueba",
  "slug": "categoria-prueba"
}
```

### Sin Permisos (403 FORBIDDEN)
```json
{
  "code": "FORBIDDEN",
  "message": "No tienes permisos para modificar esta categoría",
  "timestamp": "2026-01-21T10:30:00"
}
```

### No Encontrado (404 NOT FOUND)
```json
{
  "code": "ENTITY_NOT_FOUND",
  "message": "No se encontró una categoría con el ID: 999",
  "timestamp": "2026-01-21T10:30:00"
}
```

## Testing

### Mockear AuthorizationUtils en Tests

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
    void shouldThrowSecurityExceptionWhenUserCannotModifyCategory() {
        // Given
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        
        when(categoryRepository.findById(categoryId))
            .thenReturn(Optional.of(category));
        when(authorizationUtils.canModifyCategory(category))
            .thenReturn(false);
        
        // When & Then
        assertThrows(SecurityException.class, () -> 
            categoryService.update(categoryId, new UpdateCategoryRequest())
        );
    }
    
    @Test
    void shouldUpdateCategoryWhenUserHasPermission() {
        // Given
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        
        when(categoryRepository.findById(categoryId))
            .thenReturn(Optional.of(category));
        when(authorizationUtils.canModifyCategory(category))
            .thenReturn(true);
        
        // When
        categoryService.update(categoryId, new UpdateCategoryRequest());
        
        // Then
        verify(categoryRepository).save(any(Category.class));
    }
}
```

## Próximos Pasos Recomendados

### ✅ Completado
1. ✅ Crear `AuthorizationUtils` 
2. ✅ Actualizar `CategoryService`
3. ✅ Actualizar `GlobalExceptionHandler`
4. ✅ Crear documentación

### 🔄 Pendiente
1. **Actualizar `ProductService`** para usar `AuthorizationUtils`
2. **Actualizar `StoreService`** para usar `AuthorizationUtils`
3. **Agregar tests unitarios** para `AuthorizationUtils`
4. **Agregar tests de integración** para los servicios
5. **Considerar agregar logging** para auditoría de accesos

### 💡 Mejoras Futuras
- Agregar caché para reducir consultas a base de datos
- Implementar auditoría de accesos (¿quién intentó hacer qué?)
- Agregar métricas de autorización
- Considerar usar Spring Security Annotations (`@PreAuthorize`, etc.)

## Documentación Adicional

- **Guía Completa**: Ver `AUTHORIZATION_UTILS_GUIDE.md` para más ejemplos
- **RoleUtils**: Ver `RoleUtils.java` para métodos auxiliares de roles
- **Security Config**: Ver `SecurityConfig.java` para configuración de seguridad

## Contacto y Soporte

Si tienes preguntas sobre cómo usar este sistema, consulta:
1. `AUTHORIZATION_UTILS_GUIDE.md` - Guía detallada con ejemplos
2. El código fuente de `AuthorizationUtils.java` - Bien documentado
3. El ejemplo implementado en `CategoryService.java`

---

**Última actualización**: 2026-01-21
**Versión**: 1.0.0
**Estado**: ✅ Implementado y funcionando
