package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.Category;
import com.kiosky.kiosky.domain.entity.Store;
import com.kiosky.kiosky.domain.repository.CategoryRepository;
import com.kiosky.kiosky.domain.repository.StoreRepository;
import com.kiosky.kiosky.dto.CategoryResponse;
import com.kiosky.kiosky.dto.CreateCategoryRequest;
import com.kiosky.kiosky.dto.UpdateCategoryRequest;
import com.kiosky.kiosky.mappers.CategoryMapper;
import com.kiosky.kiosky.util.AuthorizationUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final CategoryMapper categoryMapper;
    private final AuthorizationUtils authUtils;

    /**
     * Obtiene todas las categorías
     * @return Lista de CategoryResponse
     */
    public List<CategoryResponse> getAll() {
        return categoryMapper.toResponseDtoList(categoryRepository.findAll());
    }

    /**
     * Obtiene una categoría por ID
     * @param id ID de la categoría
     * @return CategoryResponse
     * @throws EntityNotFoundException si no se encuentra la categoría
     */
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una categoría con el ID: " + id));
        return categoryMapper.toResponseDto(category);
    }

    /**
     * Obtiene una categoría por slug
     * @param slug slug de la categoría
     * @return CategoryResponse
     * @throws EntityNotFoundException si no se encuentra la categoría
     */
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una categoría con el slug: " + slug));
        return categoryMapper.toResponseDto(category);
    }

    /**
     * Obtiene todas las categorías de una tienda específica
     * @param storeId ID de la tienda
     * @return Lista de CategoryResponse
     */
    public List<CategoryResponse> getByStoreId(Long storeId) {
        return categoryMapper.toResponseDtoList(categoryRepository.findByStoreId(storeId));
    }

    /**
     * Crea una nueva categoría
     * @param createCategoryRequest datos de la nueva categoría
     * @return CategoryResponse de la categoría creada
     * @throws EntityNotFoundException si no se encuentra la tienda
     * @throws IllegalArgumentException si el slug ya existe en la tienda
     * @throws SecurityException si el usuario no tiene permisos para crear categorías en esta tienda
     */
    @Transactional
    public CategoryResponse create(CreateCategoryRequest createCategoryRequest) throws AccessDeniedException {
        // Validar que la tienda existe
        Store store = storeRepository.findById(createCategoryRequest.getStoreId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una tienda con el ID: " + createCategoryRequest.getStoreId()));

        // Validar permisos
        if (!authUtils.canModifyStore(store.getId())) {
            throw new AccessDeniedException("No tienes permiso para modificar la información de esta tienda.");
        }

        // Validar que el slug no existe en la tienda
        if (categoryRepository.existsBySlugAndStoreId(createCategoryRequest.getSlug(), createCategoryRequest.getStoreId())) {
            throw new IllegalArgumentException("Ya existe una categoría con el slug '" + createCategoryRequest.getSlug() + "' en esta tienda");
        }

        // Crear la categoría
        Category category = categoryMapper.toEntity(createCategoryRequest);

        category.setStore(store);

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(savedCategory);
    }

    /**
     * Actualiza una categoría existente
     * @param id ID de la categoría a actualizar
     * @param updateCategoryRequest nuevos datos de la categoría
     * @return CategoryResponse de la categoría actualizada
     * @throws EntityNotFoundException si no se encuentra la categoría
     * @throws IllegalArgumentException si el nuevo slug ya existe en la tienda
     * @throws SecurityException si el usuario no tiene permisos para modificar esta categoría
     */
    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest updateCategoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una categoría con el ID: " + id));

        // Verificar permisos
        if (!authUtils.canModifyCategory(category)) {
            throw new SecurityException("No tienes permisos para modificar esta categoría");
        }

        // Validar que el nuevo slug no existe en la tienda (excepto para la categoría actual)
        if (!category.getSlug().equals(updateCategoryRequest.getSlug()) &&
            categoryRepository.existsBySlugAndStoreId(updateCategoryRequest.getSlug(), category.getStore().getId())) {
            throw new IllegalArgumentException("Ya existe una categoría con el slug '" + updateCategoryRequest.getSlug() + "' en esta tienda");
        }

        categoryMapper.updateEntityFromDto(updateCategoryRequest, category);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(updatedCategory);
    }

    /**
     * Elimina una categoría por ID
     * @param id ID de la categoría a eliminar
     * @throws EntityNotFoundException si no se encuentra la categoría
     * @throws IllegalStateException si la categoría tiene productos asociados
     * @throws SecurityException si el usuario no tiene permisos para eliminar esta categoría
     */
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una categoría con el ID: " + id));

        // Verificar permisos
        if (!authUtils.canModifyCategory(category)) {
            throw new SecurityException("No tienes permisos para eliminar esta categoría");
        }

        // Verificar que no tenga productos asociados
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene productos asociados");
        }

        categoryRepository.delete(category);
    }

    /**
     * Busca una categoría por slug dentro de una tienda específica
     * @param slug slug de la categoría
     * @param storeId ID de la tienda
     * @return CategoryResponse si se encuentra
     * @throws EntityNotFoundException si no se encuentra la categoría
     */
    public CategoryResponse getBySlugAndStoreId(String slug, Long storeId) {
        Category category = categoryRepository.findBySlugAndStoreId(slug, storeId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una categoría con el slug '" + slug + "' en la tienda con ID: " + storeId));
        return categoryMapper.toResponseDto(category);
    }

    /**
     * Verifica si existe una categoría con el slug dado en una tienda específica
     * @param slug slug a verificar
     * @param storeId ID de la tienda
     * @return true si existe, false en caso contrario
     */
    public boolean existsBySlugAndStoreId(String slug, Long storeId) {
        return categoryRepository.existsBySlugAndStoreId(slug, storeId);
    }
}
