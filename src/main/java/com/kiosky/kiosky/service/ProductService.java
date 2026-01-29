package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.Category;
import com.kiosky.kiosky.domain.entity.Product;
import com.kiosky.kiosky.domain.repository.CategoryRepository;
import com.kiosky.kiosky.domain.repository.ProductRepository;
import com.kiosky.kiosky.dto.CreateProductRequest;
import com.kiosky.kiosky.dto.ProductResponse;
import com.kiosky.kiosky.dto.UpdateProductRequest;
import com.kiosky.kiosky.mappers.ProductMapper;
import com.kiosky.kiosky.util.AuthorizationUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final AuthorizationUtils authUtils;

    /**
     * Obtiene todos los productos
     * @return Lista de ProductResponse
     */
    public List<ProductResponse> getAll() {
        return productMapper.toResponseDtoList(productRepository.findAll());
    }

    /**
     * Obtiene un producto por ID
     * @param id ID del producto
     * @return ProductResponse
     * @throws EntityNotFoundException si no se encuentra el producto
     */
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un producto con el ID: " + id));
        return productMapper.toResponseDto(product);
    }

    /**
     * Obtiene un producto por slug
     * @param slug slug del producto
     * @return ProductResponse
     * @throws EntityNotFoundException si no se encuentra el producto
     */
    public ProductResponse getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un producto con el slug: " + slug));
        return productMapper.toResponseDto(product);
    }

    /**
     * Obtiene todos los productos de una categoría específica
     * @param categoryId ID de la categoría
     * @return Lista de ProductResponse
     */
    public List<ProductResponse> getByCategoryId(Long categoryId) {
        return productMapper.toResponseDtoList(productRepository.findByCategoryId(categoryId));
    }

    /**
     * Obtiene todos los productos de una tienda específica
     * @param storeId ID de la tienda
     * @return Lista de ProductResponse
     */
    public List<ProductResponse> getByStoreId(Long storeId) {
        return productMapper.toResponseDtoList(productRepository.findByStoreId(storeId));
    }

    /**
     * Obtiene todos los productos visibles de una tienda específica
     * @param storeId ID de la tienda
     * @return Lista de ProductResponse
     */
    public List<ProductResponse> getVisibleByStoreId(Long storeId) {
        return productMapper.toResponseDtoList(productRepository.findByStoreIdAndIsVisibleTrue(storeId));
    }

    /**
     * Busca productos por nombre en una tienda específica
     * @param storeId ID de la tienda
     * @param name término de búsqueda
     * @return Lista de ProductResponse
     */
    public List<ProductResponse> searchByNameInStore(Long storeId, String name) {
        return productMapper.toResponseDtoList(productRepository.findByStoreIdAndNameContainingIgnoreCaseAndIsVisibleTrue(storeId, name));
    }

    /**
     * Busca productos por rango de precios en una tienda específica
     * @param storeId ID de la tienda
     * @param minPrice precio mínimo
     * @param maxPrice precio máximo
     * @return Lista de ProductResponse
     */
    public List<ProductResponse> getByPriceRangeInStore(Long storeId, BigDecimal minPrice, BigDecimal maxPrice) {
        return productMapper.toResponseDtoList(productRepository.findByStoreIdAndPriceBetweenAndIsVisibleTrue(storeId, minPrice, maxPrice));
    }

    /**
     * Crea un nuevo producto
     * @param createProductRequest datos del nuevo producto
     * @return ProductResponse del producto creado
     * @throws EntityNotFoundException si no se encuentra la categoría
     * @throws IllegalArgumentException si el slug ya existe en la tienda
     */
    @Transactional
    public ProductResponse create(CreateProductRequest createProductRequest) throws AccessDeniedException {
        // Validar que el ID de categoría no sea nulo
        Long categoryId = createProductRequest.getCategoryId();
        if (categoryId == null) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo");
        }

        // Validar que la categoría existe
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una categoría con el ID: " + categoryId));

        // Validar permisos
        if (!authUtils.canModifyStore(category.getStore().getId())) {
            throw new AccessDeniedException("No tienes permiso para modificar la información de esta tienda.");
        }

        // Validar que el slug no existe en la tienda
        Long storeId = category.getStore().getId();
        if (productRepository.existsBySlugAndStoreId(createProductRequest.getSlug(), storeId)) {
            throw new IllegalArgumentException("Ya existe un producto con el slug '" + createProductRequest.getSlug() + "' en esta tienda");
        }

        // Crear el producto
        Product product = productMapper.toEntity(createProductRequest);
        product.setCategory(category);
        product.setStore(category.getStore()); // Establecer relación directa con tienda

        // Validar consistencia entre categoría y tienda
        product.validateStoreConsistency();

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponseDto(savedProduct);
    }

    /**
     * Actualiza un producto existente
     * @param id ID del producto a actualizar
     * @param updateProductRequest nuevos datos del producto
     * @return ProductResponse del producto actualizado
     * @throws EntityNotFoundException si no se encuentra el producto o la nueva categoría
     * @throws IllegalArgumentException si el nuevo slug ya existe en la tienda
     */
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest updateProductRequest) throws AccessDeniedException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un producto con el ID: " + id));

        // Validar permisos
        if (!authUtils.canModifyProduct(product)) {
            throw new AccessDeniedException("No tienes permiso para modificar la información de esta tienda.");
        }

        // Validar que la nueva categoría existe
        Category newCategory = categoryRepository.findById(updateProductRequest.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una categoría con el ID: " + updateProductRequest.getCategoryId()));

        // Validar que el nuevo slug no existe en la tienda (excepto para el producto actual)
        Long storeId = newCategory.getStore().getId();
        if (!product.getSlug().equals(updateProductRequest.getSlug()) &&
            productRepository.existsBySlugAndStoreId(updateProductRequest.getSlug(), storeId)) {
            throw new IllegalArgumentException("Ya existe un producto con el slug '" + updateProductRequest.getSlug() + "' en esta tienda");
        }

        productMapper.updateEntityFromDto(updateProductRequest, product);
        product.setCategory(newCategory);
        product.setStore(newCategory.getStore()); // Actualizar relación directa con tienda

        // Validar consistencia entre categoría y tienda
        product.validateStoreConsistency();

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponseDto(updatedProduct);
    }

    /**
     * Elimina un producto por ID
     * @param id ID del producto a eliminar
     * @throws EntityNotFoundException si no se encuentra el producto
     * @throws AccessDeniedException si no tiene permisos para realizar la acción
     */
    @Transactional
    public void delete(Long id) throws AccessDeniedException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un producto con el ID: " + id));

        // Validar permisos
        if (!authUtils.canModifyProduct(product)) {
            throw new AccessDeniedException("No tienes permiso para modificar la información de esta tienda.");
        }

        productRepository.delete(product);
    }

    /**
     * Cambia la visibilidad de un producto
     * @param id ID del producto
     * @param isVisible nueva visibilidad
     * @return ProductResponse del producto actualizado
     * @throws EntityNotFoundException si no se encuentra el producto
     */
    @Transactional
    public ProductResponse toggleVisibility(Long id, Boolean isVisible) throws AccessDeniedException{
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un producto con el ID: " + id));

        // Validar permisos
        if (!authUtils.canModifyProduct(product)) {
            throw new AccessDeniedException("No tienes permiso para modificar la información de esta tienda.");
        }

        product.setIsVisible(isVisible);
        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponseDto(updatedProduct);
    }

    /**
     * Busca un producto por slug dentro de una tienda específica
     * @param slug slug del producto
     * @param storeId ID de la tienda
     * @return ProductResponse si se encuentra
     * @throws EntityNotFoundException si no se encuentra el producto
     */
    public ProductResponse getBySlugAndStoreId(String slug, Long storeId) {
        Product product = productRepository.findBySlugAndStoreId(slug, storeId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un producto con el slug '" + slug + "' en la tienda con ID: " + storeId));
        return productMapper.toResponseDto(product);
    }

    /**
     * Verifica si existe un producto con el slug dado en una tienda específica
     * @param slug slug a verificar
     * @param storeId ID de la tienda
     * @return true si existe, false en caso contrario
     */
    public boolean existsBySlugAndStoreId(String slug, Long storeId) {
        return productRepository.existsBySlugAndStoreId(slug, storeId);
    }
}
