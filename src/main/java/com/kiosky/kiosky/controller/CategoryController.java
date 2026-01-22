package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.dto.CategoryResponse;
import com.kiosky.kiosky.dto.CreateCategoryRequest;
import com.kiosky.kiosky.dto.UpdateCategoryRequest;
import com.kiosky.kiosky.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Obtiene todas las categorías
     * @return Lista de categorías
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAll();
        return ResponseEntity.ok(categories);
    }

    /**
     * Obtiene una categoría por ID
     * @param id ID de la categoría
     * @return Categoría encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Obtiene una categoría por slug
     * @param slug Slug de la categoría
     * @return Categoría encontrada
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse category = categoryService.getBySlug(slug);
        return ResponseEntity.ok(category);
    }

    /**
     * Obtiene todas las categorías de una tienda específica
     * @param storeId ID de la tienda
     * @return Lista de categorías de la tienda
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByStoreId(@PathVariable Long storeId) {
        List<CategoryResponse> categories = categoryService.getByStoreId(storeId);
        return ResponseEntity.ok(categories);
    }

    /**
     * Busca una categoría por slug dentro de una tienda específica
     * @param storeId ID de la tienda
     * @param slug Slug de la categoría
     * @return Categoría encontrada
     */
    @GetMapping("/store/{storeId}/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlugAndStoreId(
            @PathVariable Long storeId,
            @PathVariable String slug) {
        CategoryResponse category = categoryService.getBySlugAndStoreId(slug, storeId);
        return ResponseEntity.ok(category);
    }

    /**
     * Crea una nueva categoría
     * @param createCategoryRequest Datos de la nueva categoría
     * @return Categoría creada
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest createCategoryRequest) throws AccessDeniedException {
        CategoryResponse createdCategory = categoryService.create(createCategoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    /**
     * Actualiza una categoría existente
     * @param id ID de la categoría a actualizar
     * @param updateCategoryRequest Nuevos datos de la categoría
     * @return Categoría actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest) {
        CategoryResponse updatedCategory = categoryService.update(id, updateCategoryRequest);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Elimina una categoría por ID
     * @param id ID de la categoría a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica si existe una categoría con el slug dado en una tienda específica
     * @param storeId ID de la tienda
     * @param slug Slug a verificar
     * @return true si existe, false en caso contrario
     */
    @GetMapping("/store/{storeId}/slug/{slug}/exists")
    public ResponseEntity<Boolean> checkCategorySlugExists(
            @PathVariable Long storeId,
            @PathVariable String slug) {
        boolean exists = categoryService.existsBySlugAndStoreId(slug, storeId);
        return ResponseEntity.ok(exists);
    }
}
