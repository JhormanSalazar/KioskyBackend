package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.entity.Store;
import com.kiosky.kiosky.domain.repository.StoreRepository;
import com.kiosky.kiosky.dto.RegisterStoreRequest;
import com.kiosky.kiosky.dto.StoreResponse;
import com.kiosky.kiosky.mappers.StoreMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    public List<StoreResponse> getAll() {
        return storeMapper.toResponseDtoList(storeRepository.findAll());
    }

    public StoreResponse getById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una tienda con el ID: " + id));
        return storeMapper.toResponseDto(store);
    }

    /**
     * Obtiene la entidad Store por ID (para uso interno en servicios)
     * @param id ID de la tienda
     * @return La entidad Store
     * @throws EntityNotFoundException si no se encuentra la tienda
     */
    public Store getStoreEntityById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró una tienda con el ID: " + id));
    }

    @Transactional
    public StoreResponse createStore(RegisterStoreRequest request, AppUser owner) {
        // Validar que el dominio sea único
        if (existsByDomain(request.getDomain())) {
            throw new IllegalArgumentException("Ya existe una tienda con este dominio: " + request.getDomain());
        }

        // Verificar que el usuario no tenga ya una tienda
        if (owner.getStore() != null) {
            throw new IllegalArgumentException("El usuario ya tiene una tienda asignada");
        }

        Store store = new Store();
        store.setDomain(request.getDomain());
        store.setThemeSettings(request.getThemeSettings());
        store.setAppUser(owner);

        Store savedStore = storeRepository.save(store);
        return storeMapper.toResponseDto(savedStore);
    }

    public boolean existsByDomain(String domain) {
        return storeRepository.existsByDomain(domain);
    }

    @Transactional
    public void deleteStore(Long id) {
        if (!storeRepository.existsById(id)) {
            throw new EntityNotFoundException("No se encontró una tienda con el ID: " + id);
        }
        storeRepository.deleteById(id);
    }
}
