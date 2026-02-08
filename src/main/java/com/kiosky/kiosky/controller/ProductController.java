package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.dto.CreateProductRequest;
import com.kiosky.kiosky.dto.ProductResponse;
import com.kiosky.kiosky.dto.UpdateProductRequest;
import com.kiosky.kiosky.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/products")
@AllArgsConstructor
@Tag(name = "Productos", description = "Gestión del catálogo de productos")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {

    private final ProductService productService;

    /**
     * Obtiene todos los productos
     * @return Lista de productos
     */
    @Operation(summary = "Listar todos los productos", description = "Obtiene la lista completa de productos del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAll();
        return ResponseEntity.ok(products);
    }

    /**
     * Obtiene un producto por ID
     * @param id ID del producto
     * @return Producto encontrado
     */
    @Operation(summary = "Obtener producto por ID", description = "Busca y retorna un producto específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        ProductResponse product = productService.getById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Obtiene un producto por slug
     * @param slug Slug del producto
     * @return Producto encontrado
     */
    @Operation(summary = "Obtener producto por slug", description = "Busca y retorna un producto específico por su slug (URL amigable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(
            @Parameter(description = "Slug del producto") @PathVariable String slug) {
        ProductResponse product = productService.getBySlug(slug);
        return ResponseEntity.ok(product);
    }

    /**
     * Obtiene todos los productos de una categoría específica
     * @param categoryId ID de la categoría
     * @return Lista de productos de la categoría
     */
    @Operation(summary = "Listar productos por categoría", description = "Obtiene todos los productos de una categoría específica")
    @ApiResponse(responseCode = "200", description = "Lista de productos de la categoría",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategoryId(
            @Parameter(description = "ID de la categoría") @PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtiene todos los productos de una tienda específica
     * @param storeId ID de la tienda
     * @return Lista de productos de la tienda
     */
    @Operation(summary = "Listar productos por tienda", description = "Obtiene todos los productos de una tienda específica")
    @ApiResponse(responseCode = "200", description = "Lista de productos de la tienda",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProductResponse>> getProductsByStoreId(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId) {
        List<ProductResponse> products = productService.getByStoreId(storeId);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtiene todos los productos visibles de una tienda específica
     * @param storeId ID de la tienda
     * @return Lista de productos visibles de la tienda
     */
    @Operation(summary = "Listar productos visibles por tienda", description = "Obtiene solo los productos visibles/activos de una tienda")
    @ApiResponse(responseCode = "200", description = "Lista de productos visibles de la tienda",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    @GetMapping("/store/{storeId}/visible")
    public ResponseEntity<List<ProductResponse>> getVisibleProductsByStoreId(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId) {
        List<ProductResponse> products = productService.getVisibleByStoreId(storeId);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca productos por nombre en una tienda específica
     * @param storeId ID de la tienda
     * @param name Término de búsqueda
     * @return Lista de productos que coinciden con la búsqueda
     */
    @Operation(summary = "Buscar productos en tienda", description = "Busca productos por nombre dentro de una tienda específica")
    @ApiResponse(responseCode = "200", description = "Resultados de búsqueda",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    @GetMapping("/store/{storeId}/search")
    public ResponseEntity<List<ProductResponse>> searchProductsInStore(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId,
            @Parameter(description = "Término de búsqueda") @RequestParam String name) {
        List<ProductResponse> products = productService.searchByNameInStore(storeId, name);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca productos por rango de precios en una tienda específica
     * @param storeId ID de la tienda
     * @param minPrice Precio mínimo
     * @param maxPrice Precio máximo
     * @return Lista de productos en el rango de precios
     */
    @Operation(summary = "Filtrar productos por rango de precios", description = "Obtiene productos de una tienda dentro de un rango de precios")
    @ApiResponse(responseCode = "200", description = "Lista de productos en el rango de precios",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    @GetMapping("/store/{storeId}/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsByPriceRange(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId,
            @Parameter(description = "Precio mínimo") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Precio máximo") @RequestParam BigDecimal maxPrice) {
        List<ProductResponse> products = productService.getByPriceRangeInStore(storeId, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca un producto por slug dentro de una tienda específica
     * @param storeId ID de la tienda
     * @param slug Slug del producto
     * @return Producto encontrado
     */
    @Operation(summary = "Obtener producto por slug y tienda", description = "Busca un producto específico por su slug dentro de una tienda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/store/{storeId}/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlugAndStoreId(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId,
            @Parameter(description = "Slug del producto") @PathVariable String slug) {
        ProductResponse product = productService.getBySlugAndStoreId(slug, storeId);
        return ResponseEntity.ok(product);
    }

    /**
     * Crea un nuevo producto
     * @param createProductRequest Datos del nuevo producto
     * @return Producto creado
     */
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el catálogo de la tienda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para crear productos")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest createProductRequest) throws AccessDeniedException {
        ProductResponse createdProduct = productService.create(createProductRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Actualiza un producto existente
     * @param id ID del producto a actualizar
     * @param updateProductRequest Nuevos datos del producto
     * @return Producto actualizado
     */
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos para actualizar este producto")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest updateProductRequest) throws AccessDeniedException {
        ProductResponse updatedProduct = productService.update(id, updateProductRequest);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Cambia la visibilidad de un producto
     * @param id ID del producto
     * @param isVisible Nueva visibilidad
     * @return Producto actualizado
     */
    @Operation(summary = "Cambiar visibilidad del producto", description = "Activa o desactiva la visibilidad de un producto en el catálogo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Visibilidad actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ProductResponse> toggleProductVisibility(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Parameter(description = "Nueva visibilidad") @RequestParam Boolean isVisible) throws AccessDeniedException {
        ProductResponse updatedProduct = productService.toggleVisibility(id, isVisible);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Elimina un producto por ID
     * @param id ID del producto a eliminar
     * @return Respuesta sin contenido
     */
    @Operation(summary = "Eliminar producto", description = "Elimina un producto del catálogo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID del producto") @PathVariable Long id) throws AccessDeniedException {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica si existe un producto con el slug dado en una tienda específica
     * @param storeId ID de la tienda
     * @param slug Slug a verificar
     * @return true si existe, false en caso contrario
     */
    @Operation(summary = "Verificar existencia de slug", description = "Verifica si ya existe un producto con el slug dado en una tienda")
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    @GetMapping("/store/{storeId}/slug/{slug}/exists")
    public ResponseEntity<Boolean> checkProductSlugExists(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId,
            @Parameter(description = "Slug a verificar") @PathVariable String slug) {
        boolean exists = productService.existsBySlugAndStoreId(slug, storeId);
        return ResponseEntity.ok(exists);
    }
}
