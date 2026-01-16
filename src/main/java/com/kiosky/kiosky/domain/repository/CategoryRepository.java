package com.kiosky.kiosky.domain.repository;

import com.kiosky.kiosky.domain.entity.Category;
import com.kiosky.kiosky.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Encuentra todas las categorías de una tienda específica
     * @param store la tienda
     * @return Lista de categorías de esa tienda
     */
    List<Category> findByStore(Store store);

    /**
     * Encuentra todas las categorías de una tienda específica por ID
     * @param storeId el ID de la tienda
     * @return Lista de categorías de esa tienda
     */
    @Query("SELECT c FROM Category c WHERE c.store.id = :storeId")
    List<Category> findByStoreId(@Param("storeId") Long storeId);

    /**
     * Encuentra una categoría por su slug
     * @param slug el slug de la categoría
     * @return Optional con la categoría si existe
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Encuentra una categoría por slug dentro de una tienda específica
     * @param slug el slug de la categoría
     * @param storeId el ID de la tienda
     * @return Optional con la categoría si existe
     */
    @Query("SELECT c FROM Category c WHERE c.slug = :slug AND c.store.id = :storeId")
    Optional<Category> findBySlugAndStoreId(@Param("slug") String slug, @Param("storeId") Long storeId);

    /**
     * Verifica si existe una categoría con el slug dado en una tienda específica
     * @param slug el slug a verificar
     * @param storeId el ID de la tienda
     * @return true si existe, false en caso contrario
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.slug = :slug AND c.store.id = :storeId")
    boolean existsBySlugAndStoreId(@Param("slug") String slug, @Param("storeId") Long storeId);

    /**
     * Encuentra una categoría por nombre dentro de una tienda específica
     * @param name el nombre de la categoría
     * @param storeId el ID de la tienda
     * @return Optional con la categoría si existe
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.store.id = :storeId")
    Optional<Category> findByNameAndStoreId(@Param("name") String name, @Param("storeId") Long storeId);
}
