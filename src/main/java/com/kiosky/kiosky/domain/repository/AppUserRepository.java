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
     * Encuentra el usuario dueño de una tienda específica a través de la relación inversa
     * @param store la tienda
     * @return Lista de usuarios de esa tienda (debería ser solo uno ya que es OneToOne)
     */
    List<AppUser> findByStore(Store store);

    /**
     * Encuentra el usuario dueño de una tienda por ID de tienda usando join
     * @param storeId el ID de la tienda
     * @return Optional del usuario dueño de esa tienda
     */
    @Query("SELECT u FROM AppUser u WHERE u.store.id = :storeId")
    Optional<AppUser> findByStoreId(@Param("storeId") Long storeId);

    /**
     * Encuentra un usuario por su email
     * @param email el email del usuario
     * @return Optional del usuario encontrado
     */
    Optional<AppUser> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email especificado
     * @param email el email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario para una tienda específica usando join
     * @param storeId el ID de la tienda
     * @return true si existe, false si no
     */
    @Query("SELECT COUNT(u) > 0 FROM AppUser u WHERE u.store.id = :storeId")
    boolean existsByStoreId(@Param("storeId") Long storeId);
}
