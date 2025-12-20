package com.kiosky.kiosky.domain.repository;

import com.kiosky.kiosky.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * Busca una tienda por su dominio
     * @param domain el dominio de la tienda
     * @return Optional de la tienda encontrada
     */
    Optional<Store> findByDomain(String domain);

    /**
     * Verifica si existe una tienda con el dominio especificado
     * @param domain el dominio a verificar
     * @return true si existe, false si no
     */
    boolean existsByDomain(String domain);
}
