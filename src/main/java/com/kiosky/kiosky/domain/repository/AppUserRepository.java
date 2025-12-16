package com.kiosky.kiosky.domain.repository;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Encuentra un usuario por su número de teléfono
     * @param tel el número de teléfono
     * @return Optional con el usuario si existe
     */
    Optional<AppUser> findByTel(String tel);

    /**
     * Encuentra usuarios por nombre
     * @param firstName el primer nombre
     * @return Lista de usuarios con ese nombre
     */
    List<AppUser> findByFirstName(String firstName);

    /**
     * Encuentra usuarios por nombre y apellido
     * @param firstName el primer nombre
     * @param lastName el apellido
     * @return Lista de usuarios con ese nombre y apellido
     */
    List<AppUser> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Encuentra todos los usuarios de una tienda específica
     * @param store la tienda
     * @return Lista de usuarios de esa tienda
     */
    List<AppUser> findByStore(Store store);

    /**
     * Encuentra todos los usuarios de una tienda por ID de tienda
     * @param storeId el ID de la tienda
     * @return Lista de usuarios de esa tienda
     */
    List<AppUser> findByStoreId(Long storeId);


    /**
     * Verifica si existe un usuario con ese teléfono
     * @param tel el teléfono a verificar
     * @return true si existe, false si no
     */
    boolean existsByTel(String tel);

    /**
     * Cuenta los usuarios de una tienda específica
     * @param storeId el ID de la tienda
     * @return número de usuarios en esa tienda
     */
    long countByStoreId(Long storeId);

    /**
     * Encuentra usuarios cuyo nombre contenga el texto especificado (búsqueda parcial)
     * @param name parte del nombre a buscar
     * @return Lista de usuarios que coinciden
     */
    @Query("SELECT u FROM AppUser u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name%")
    List<AppUser> findByNameContaining(@Param("name") String name);

    /**
     * Encuentra usuarios de una tienda específica con paginación
     * @param storeId el ID de la tienda
     * @return Lista de usuarios ordenados por firstName
     */
    @Query("SELECT u FROM AppUser u WHERE u.store.id = :storeId ORDER BY u.firstName ASC")
    List<AppUser> findByStoreIdOrderByFirstName(@Param("storeId") Long storeId);
}
