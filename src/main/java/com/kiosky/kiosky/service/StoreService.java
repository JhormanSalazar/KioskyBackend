package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.entity.Role;
import com.kiosky.kiosky.domain.entity.Store;
import com.kiosky.kiosky.domain.repository.AppUserRepository;
import com.kiosky.kiosky.domain.repository.StoreRepository;
import com.kiosky.kiosky.dto.RegisterStoreRequest;
import com.kiosky.kiosky.dto.SimpleStoreResponse;
import com.kiosky.kiosky.dto.StoreResponse;
import com.kiosky.kiosky.dto.UpdateStoreRequest;
import com.kiosky.kiosky.mappers.StoreMapper;
import com.kiosky.kiosky.util.AuthorizationUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final AuthorizationUtils authUtils;

    public List<StoreResponse> getAll() {
        return storeMapper.toResponseDtoList(storeRepository.findAll());
    }

    public StoreResponse getById(Long id) {
        if(id == null){
            throw new IllegalArgumentException("El ID de la tienda no puede ser nulo.");
        }  

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una tienda con el ID: " + id));
        return storeMapper.toResponseDto(store);
    }

    public SimpleStoreResponse getSimpleStoreById(Long id) {
       if(id == null){
            throw new IllegalArgumentException("El ID de la tienda no puede ser nulo.");
        }  

        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una tienda con el ID: " + id));
        return storeMapper.toSimpleResponseDto(store);
    }

    /**
     * Obtiene la entidad Store por ID (para uso interno en servicios)
     * @param id ID de la tienda
     * @return La entidad Store
     * @throws EntityNotFoundException si no se encuentra la tienda
     */
    public Store getStoreEntityById(Long id) {
         if(id == null){
            throw new IllegalArgumentException("El ID de la tienda no puede ser nulo.");
        }  


        return storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una tienda con el ID: " + id));
    }

    @Transactional
    public StoreResponse createStore(RegisterStoreRequest request, AppUser owner) {
        log.info("🏪 Iniciando creación de tienda para usuario ID: {} con dominio: '{}'", 
                owner.getId(), request.getDomain());

        // Verificar que el usuario no tenga ya una tienda
        if (owner.getStore() != null) {
            log.warn("⚠️ Usuario ID: {} ya tiene una tienda asignada con ID: {}", 
                    owner.getId(), owner.getStore().getId());
            throw new IllegalArgumentException("El usuario ya tiene una tienda asignada");
        }

        // Normalizar el dominio (trim y lowercase)
        String normalizedDomain = normalizeDomain(request.getDomain());
        log.info("🔄 Dominio normalizado: '{}' -> '{}'", request.getDomain(), normalizedDomain);

        // Verificar que el dominio no esté ya en uso con manejo de concurrencia
        if (existsByDomain(normalizedDomain)) {
            log.error("❌ El dominio '{}' ya está en uso", normalizedDomain);
            throw new IllegalArgumentException("El dominio '" + normalizedDomain + "' ya está en uso");
        }
        
        log.info("✅ Dominio '{}' disponible, procediendo con la creación", normalizedDomain);

        try {
            Store store = new Store();
            store.setName(request.getName());
            store.setDomain(normalizedDomain);
            store.setAppUser(owner);

            // Actualizar el rol a OWNER si el usuario no lo tiene
            if (owner.getRole() != Role.ADMIN && owner.getRole() != Role.OWNER) {
                owner.setRole(Role.OWNER);
                log.info("🔄 Actualizando rol del usuario ID: {} a OWNER", owner.getId());
            }

            // IMPORTANTE: Guardar la tienda PRIMERO (esto también guarda el usuario por cascada desde Store)
            log.info("💾 Guardando tienda (incluirá actualización del usuario por relación bidireccional)...");
            Store savedStore = storeRepository.save(store);
            
            // Sincronizar la relación bidireccional DESPUÉS del guardado
            owner.setStore(savedStore);
            
            log.info("🎉 Tienda creada exitosamente con ID: {} y dominio: '{}' para usuario ID: {}", 
                    savedStore.getId(), savedStore.getDomain(), owner.getId());
            
            return storeMapper.toResponseDto(savedStore);
            
        } catch (DataIntegrityViolationException e) {
            // Manejo específico para violaciones de integridad (dominios duplicados)
            log.error("💥 Error de integridad al crear tienda con dominio '{}': {}", normalizedDomain, e.getMessage());
            if (e.getMessage().contains("duplicate key") && e.getMessage().contains("domain")) {
                throw new IllegalArgumentException("El dominio '" + normalizedDomain + "' ya está en uso");
            }
            throw new RuntimeException("Error al crear la tienda: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("💥 Error inesperado al crear tienda: {}", e.getMessage(), e);
            throw e;
        }
    }

    public boolean existsByDomain(String domain) {
        log.info("🔍 Verificando existencia de dominio: '{}'", domain);
        boolean exists = storeRepository.existsByDomain(domain);
        log.info("📊 Resultado verificación dominio '{}': {}", domain, exists ? "EXISTE" : "DISPONIBLE");
        return exists;
    }

    /**
     * Normaliza un dominio removiendo espacios y convirtiendo a minúsculas
     * @param domain el dominio a normalizar
     * @return el dominio normalizado
     */
    public String normalizeDomain(String domain) {
        if (!StringUtils.hasText(domain)) {
            throw new IllegalArgumentException("El dominio no puede estar vacío");
        }
        return domain.trim().toLowerCase();
    }

    @Transactional
    public StoreResponse updateStore(Long storeId, UpdateStoreRequest request) {
        log.info("🔄 Iniciando actualización de tienda ID: {}", storeId);
        
        if (storeId == null) {
            throw new IllegalArgumentException("El ID de la tienda no puede ser nulo.");
        }

        // Verificar permisos
        if (!authUtils.canModifyStore(storeId)) {
            throw new IllegalArgumentException("No tienes acceso a esta tienda.");
        }

        // Obtener la tienda existente
        Store store = getStoreEntityById(storeId);

        // Actualizar nombre si se proporciona
        if (StringUtils.hasText(request.getName())) {
            log.info("📝 Actualizando nombre: '{}' -> '{}'", store.getName(), request.getName());
            store.setName(request.getName());
        }

        // Actualizar dominio si se proporciona
        if (StringUtils.hasText(request.getDomain())) {
            String normalizedDomain = normalizeDomain(request.getDomain());
            
            // Verificar que el nuevo dominio no esté en uso por otra tienda
            if (!store.getDomain().equals(normalizedDomain) && existsByDomain(normalizedDomain)) {
                log.error("❌ El dominio '{}' ya está en uso por otra tienda", normalizedDomain);
                throw new IllegalArgumentException("El dominio '" + normalizedDomain + "' ya está en uso");
            }
            
            log.info("🔄 Actualizando dominio: '{}' -> '{}'", store.getDomain(), normalizedDomain);
            store.setDomain(normalizedDomain);
        }

        // Actualizar themeSettings si se proporciona
        if (request.getThemeSettings() != null) {
            log.info("🎨 Actualizando configuración de tema");
            store.setThemeSettings(request.getThemeSettings());
        }

        try {
            Store updatedStore = storeRepository.save(store);
            log.info("✅ Tienda ID: {} actualizada exitosamente", storeId);
            return storeMapper.toResponseDto(updatedStore);
        } catch (DataIntegrityViolationException e) {
            log.error("💥 Error de integridad al actualizar tienda: {}", e.getMessage());
            if (e.getMessage().contains("duplicate key") && e.getMessage().contains("domain")) {
                throw new IllegalArgumentException("El dominio ya está en uso");
            }
            throw new RuntimeException("Error al actualizar la tienda: " + e.getMessage(), e);
        }
    }

    @Transactional
    public StoreResponse updateThemeSettings(Long storeId, String themeSettings) {
        log.info("🎨 Iniciando actualización de themeSettings para tienda ID: {}", storeId);
        
        if (storeId == null) {
            throw new IllegalArgumentException("El ID de la tienda no puede ser nulo.");
        }

        // Verificar permisos
        if (!authUtils.canModifyStore(storeId)) {
            throw new IllegalArgumentException("No tienes acceso a esta tienda.");
        }

        // Obtener la tienda existente
        Store store = getStoreEntityById(storeId);

        // Actualizar solo themeSettings
        log.info("🔄 Actualizando themeSettings de tienda ID: {}", storeId);
        store.setThemeSettings(themeSettings);

        try {
            Store updatedStore = storeRepository.save(store);
            log.info("✅ ThemeSettings de tienda ID: {} actualizado exitosamente", storeId);
            return storeMapper.toResponseDto(updatedStore);
        } catch (Exception e) {
            log.error("💥 Error al actualizar themeSettings: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar la configuración de tema: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteStore(Long storeId) {

         if(storeId == null){
            throw new IllegalArgumentException("El ID de la tienda no puede ser nulo.");
        }  


        if(!authUtils.canModifyStore(storeId)){
            throw new IllegalArgumentException("No tienes acceso a esta tienda.");
        }

        if (!storeRepository.existsById(storeId)) {
            throw new EntityNotFoundException("No se encontró una tienda con el ID: " + storeId);
        }
        storeRepository.deleteById(storeId);
    }
}
