package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.dto.CreateProductRequest;
import com.kiosky.kiosky.dto.ProductResponse;
import com.kiosky.kiosky.dto.UpdateProductRequest;
import com.kiosky.kiosky.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Obtiene todos los productos
     * @return Lista de productos
     */
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
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Obtiene un producto por slug
     * @param slug Slug del producto
     * @return Producto encontrado
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        ProductResponse product = productService.getBySlug(slug);
        return ResponseEntity.ok(product);
    }

    /**
     * Obtiene todos los productos de una categoría específica
     * @param categoryId ID de la categoría
     * @return Lista de productos de la categoría
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategoryId(@PathVariable Long categoryId) {
        List<ProductResponse> products = productService.getByCategoryId(categoryId);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtiene todos los productos de una tienda específica
     * @param storeId ID de la tienda
     * @return Lista de productos de la tienda
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProductResponse>> getProductsByStoreId(@PathVariable Long storeId) {
        List<ProductResponse> products = productService.getByStoreId(storeId);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtiene todos los productos visibles de una tienda específica
     * @param storeId ID de la tienda
     * @return Lista de productos visibles de la tienda
     */
    @GetMapping("/store/{storeId}/visible")
    public ResponseEntity<List<ProductResponse>> getVisibleProductsByStoreId(@PathVariable Long storeId) {
        List<ProductResponse> products = productService.getVisibleByStoreId(storeId);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca productos por nombre en una tienda específica
     * @param storeId ID de la tienda
     * @param name Término de búsqueda
     * @return Lista de productos que coinciden con la búsqueda
     */
    @GetMapping("/store/{storeId}/search")
    public ResponseEntity<List<ProductResponse>> searchProductsInStore(
            @PathVariable Long storeId,
            @RequestParam String name) {
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
    @GetMapping("/store/{storeId}/price-range")
    public ResponseEntity<List<ProductResponse>> getProductsByPriceRange(
            @PathVariable Long storeId,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<ProductResponse> products = productService.getByPriceRangeInStore(storeId, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca un producto por slug dentro de una tienda específica
     * @param storeId ID de la tienda
     * @param slug Slug del producto
     * @return Producto encontrado
     */
    @GetMapping("/store/{storeId}/slug/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlugAndStoreId(
            @PathVariable Long storeId,
            @PathVariable String slug) {
        ProductResponse product = productService.getBySlugAndStoreId(slug, storeId);
        return ResponseEntity.ok(product);
    }

    /**
     * Crea un nuevo producto
     * @param createProductRequest Datos del nuevo producto
     * @return Producto creado
     */
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
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
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
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ProductResponse> toggleProductVisibility(
            @PathVariable Long id,
            @RequestParam Boolean isVisible) throws AccessDeniedException {
        ProductResponse updatedProduct = productService.toggleVisibility(id, isVisible);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Elimina un producto por ID
     * @param id ID del producto a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) throws AccessDeniedException {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica si existe un producto con el slug dado en una tienda específica
     * @param storeId ID de la tienda
     * @param slug Slug a verificar
     * @return true si existe, false en caso contrario
     */
    @GetMapping("/store/{storeId}/slug/{slug}/exists")
    public ResponseEntity<Boolean> checkProductSlugExists(
            @PathVariable Long storeId,
            @PathVariable String slug) {
        boolean exists = productService.existsBySlugAndStoreId(slug, storeId);
        return ResponseEntity.ok(exists);
    }
}
