package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.Category;
import com.kiosky.kiosky.domain.entity.Store;
import com.kiosky.kiosky.domain.repository.CategoryRepository;
import com.kiosky.kiosky.domain.repository.StoreRepository;
import com.kiosky.kiosky.dto.CategoryResponse;
import com.kiosky.kiosky.dto.CreateCategoryRequest;
import com.kiosky.kiosky.dto.UpdateCategoryRequest;
import com.kiosky.kiosky.mappers.CategoryMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final CategoryMapper categoryMapper;

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
     */
    @Transactional
    public CategoryResponse create(CreateCategoryRequest createCategoryRequest) {
        // Validar que la tienda existe
        Store store = storeRepository.findById(createCategoryRequest.getStoreId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una tienda con el ID: " + createCategoryRequest.getStoreId()));

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
     */
    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest updateCategoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una categoría con el ID: " + id));

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
     */
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una categoría con el ID: " + id));

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
