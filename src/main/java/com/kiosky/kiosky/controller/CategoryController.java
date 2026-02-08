package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.dto.CategoryResponse;
import com.kiosky.kiosky.dto.CreateCategoryRequest;
import com.kiosky.kiosky.dto.UpdateCategoryRequest;
import com.kiosky.kiosky.service.CategoryService;
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

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/categories")
@AllArgsConstructor
@Tag(name = "Categorías", description = "Gestión de categorías de productos")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Obtiene todas las categorías
     * @return Lista de categorías
     */
    @Operation(summary = "Listar todas las categorías", description = "Obtiene la lista completa de categorías del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
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
    @Operation(summary = "Obtener categoría por ID", description = "Busca y retorna una categoría específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        CategoryResponse category = categoryService.getById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Obtiene una categoría por slug
     * @param slug Slug de la categoría
     * @return Categoría encontrada
     */
    @Operation(summary = "Obtener categoría por slug", description = "Busca y retorna una categoría específica por su slug")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @Parameter(description = "Slug de la categoría") @PathVariable String slug) {
        CategoryResponse category = categoryService.getBySlug(slug);
        return ResponseEntity.ok(category);
    }

    /**
     * Obtiene todas las categorías de una tienda específica
     * @param storeId ID de la tienda
     * @return Lista de categorías de la tienda
     */
    @Operation(summary = "Listar categorías por tienda", description = "Obtiene todas las categorías de una tienda específica")
    @ApiResponse(responseCode = "200", description = "Lista de categorías de la tienda",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByStoreId(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId) {
        List<CategoryResponse> categories = categoryService.getByStoreId(storeId);
        return ResponseEntity.ok(categories);
    }

    /**
     * Busca una categoría por slug dentro de una tienda específica
     * @param storeId ID de la tienda
     * @param slug Slug de la categoría
     * @return Categoría encontrada
     */
    @Operation(summary = "Obtener categoría por slug y tienda", description = "Busca una categoría específica por su slug dentro de una tienda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/store/{storeId}/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlugAndStoreId(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId,
            @Parameter(description = "Slug de la categoría") @PathVariable String slug) {
        CategoryResponse category = categoryService.getBySlugAndStoreId(slug, storeId);
        return ResponseEntity.ok(category);
    }

    /**
     * Crea una nueva categoría
     * @param createCategoryRequest Datos de la nueva categoría
     * @return Categoría creada
     */
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría para organizar productos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
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
    @Operation(summary = "Actualizar categoría", description = "Actualiza los datos de una categoría existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "ID de la categoría") @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest) {
        CategoryResponse updatedCategory = categoryService.update(id, updateCategoryRequest);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Elimina una categoría por ID
     * @param id ID de la categoría a eliminar
     * @return Respuesta sin contenido
     */
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica si existe una categoría con el slug dado en una tienda específica
     * @param storeId ID de la tienda
     * @param slug Slug a verificar
     * @return true si existe, false en caso contrario
     */
    @Operation(summary = "Verificar existencia de slug", description = "Verifica si ya existe una categoría con el slug dado en una tienda")
    @ApiResponse(responseCode = "200", description = "Resultado de la verificación")
    @GetMapping("/store/{storeId}/slug/{slug}/exists")
    public ResponseEntity<Boolean> checkCategorySlugExists(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId,
            @Parameter(description = "Slug a verificar") @PathVariable String slug) {
        boolean exists = categoryService.existsBySlugAndStoreId(slug, storeId);
        return ResponseEntity.ok(exists);
    }
}
