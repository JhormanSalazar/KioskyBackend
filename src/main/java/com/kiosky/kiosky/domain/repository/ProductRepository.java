package com.kiosky.kiosky.domain.repository;

import com.kiosky.kiosky.domain.entity.Category;
import com.kiosky.kiosky.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Encuentra todos los productos de una categoría específica
     * @param category la categoría
     * @return Lista de productos de esa categoría
     */
    List<Product> findByCategory(Category category);

    /**
     * Encuentra todos los productos de una categoría específica por ID
     * @param categoryId el ID de la categoría
     * @return Lista de productos de esa categoría
     */
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Encuentra todos los productos visibles de una categoría
     * @param category la categoría
     * @return Lista de productos visibles de esa categoría
     */
    List<Product> findByCategoryAndIsVisibleTrue(Category category);

    /**
     * Encuentra todos los productos de una tienda específica (relación directa)
     * @param storeId el ID de la tienda
     * @return Lista de productos de esa tienda
     */
    List<Product> findByStoreId(Long storeId);

    /**
     * Encuentra todos los productos visibles de una tienda específica (relación directa)
     * @param storeId el ID de la tienda
     * @return Lista de productos visibles de esa tienda
     */
    List<Product> findByStoreIdAndIsVisibleTrue(Long storeId);

    /**
     * Encuentra un producto por su slug
     * @param slug el slug del producto
     * @return Optional con el producto si existe
     */
    Optional<Product> findBySlug(String slug);

    /**
     * Encuentra un producto por slug dentro de una tienda específica (relación directa)
     * @param slug el slug del producto
     * @param storeId el ID de la tienda
     * @return Optional con el producto si existe
     */
    Optional<Product> findBySlugAndStoreId(String slug, Long storeId);

    /**
     * Verifica si existe un producto con el slug dado en una tienda específica (relación directa)
     * @param slug el slug a verificar
     * @param storeId el ID de la tienda
     * @return true si existe, false en caso contrario
     */
    boolean existsBySlugAndStoreId(String slug, Long storeId);

    /**
     * Encuentra productos por rango de precios en una tienda específica (relación directa)
     * @param storeId el ID de la tienda
     * @param minPrice precio mínimo
     * @param maxPrice precio máximo
     * @return Lista de productos en el rango de precios
     */
    List<Product> findByStoreIdAndPriceBetweenAndIsVisibleTrue(Long storeId, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Busca productos por nombre (búsqueda parcial) en una tienda específica (relación directa)
     * @param storeId el ID de la tienda
     * @param name parte del nombre a buscar
     * @return Lista de productos que coinciden con la búsqueda
     */
    List<Product> findByStoreIdAndNameContainingIgnoreCaseAndIsVisibleTrue(Long storeId, String name);
}
