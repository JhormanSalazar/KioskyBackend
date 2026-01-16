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
     * Encuentra todos los productos de una tienda específica a través de la categoría
     * @param storeId el ID de la tienda
     * @return Lista de productos de esa tienda
     */
    @Query("SELECT p FROM Product p WHERE p.category.store.id = :storeId")
    List<Product> findByStoreId(@Param("storeId") Long storeId);

    /**
     * Encuentra todos los productos visibles de una tienda específica
     * @param storeId el ID de la tienda
     * @return Lista de productos visibles de esa tienda
     */
    @Query("SELECT p FROM Product p WHERE p.category.store.id = :storeId AND p.isVisible = true")
    List<Product> findByStoreIdAndIsVisibleTrue(@Param("storeId") Long storeId);

    /**
     * Encuentra un producto por su slug
     * @param slug el slug del producto
     * @return Optional con el producto si existe
     */
    Optional<Product> findBySlug(String slug);

    /**
     * Encuentra un producto por slug dentro de una tienda específica
     * @param slug el slug del producto
     * @param storeId el ID de la tienda
     * @return Optional con el producto si existe
     */
    @Query("SELECT p FROM Product p WHERE p.slug = :slug AND p.category.store.id = :storeId")
    Optional<Product> findBySlugAndStoreId(@Param("slug") String slug, @Param("storeId") Long storeId);

    /**
     * Verifica si existe un producto con el slug dado en una tienda específica
     * @param slug el slug a verificar
     * @param storeId el ID de la tienda
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.slug = :slug AND p.category.store.id = :storeId")
    boolean existsBySlugAndStoreId(@Param("slug") String slug, @Param("storeId") Long storeId);

    /**
     * Encuentra productos por rango de precios en una tienda específica
     * @param storeId el ID de la tienda
     * @param minPrice precio mínimo
     * @param maxPrice precio máximo
     * @return Lista de productos en el rango de precios
     */
    @Query("SELECT p FROM Product p WHERE p.category.store.id = :storeId AND p.price BETWEEN :minPrice AND :maxPrice AND p.isVisible = true")
    List<Product> findByStoreIdAndPriceBetween(@Param("storeId") Long storeId,
                                               @Param("minPrice") BigDecimal minPrice,
                                               @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Busca productos por nombre (búsqueda parcial) en una tienda específica
     * @param storeId el ID de la tienda
     * @param name parte del nombre a buscar
     * @return Lista de productos que coinciden con la búsqueda
     */
    @Query("SELECT p FROM Product p WHERE p.category.store.id = :storeId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isVisible = true")
    List<Product> findByStoreIdAndNameContainingIgnoreCase(@Param("storeId") Long storeId, @Param("name") String name);
}
