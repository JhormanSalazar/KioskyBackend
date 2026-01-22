package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.dto.AppUserResponse;
import com.kiosky.kiosky.dto.RegisterAppUserRequest;
import com.kiosky.kiosky.dto.RegisterStoreRequest;
import com.kiosky.kiosky.dto.RegisterStoreWithUserRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final StoreService storeService;
    private final AppUserService appUserService;

    /**
     * Registra un usuario primero y luego su tienda en una sola transacción
     * El flujo es: Usuario -> Tienda (la tienda depende del usuario)
     *
     * @param request Datos para crear el usuario y la tienda
     * @return Respuesta con los datos del usuario creado
     * @throws IllegalArgumentException si hay errores de validación
     */
    @Transactional
    public AppUserResponse registerStoreWithUser(RegisterStoreWithUserRequest request) {

        // 1. Validar que el email no esté en uso
        if (appUserService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email: " + request.getEmail());
        }

        // 2. Validar que el dominio no esté en uso
        if (storeService.existsByDomain(request.getDomain())) {
            throw new IllegalArgumentException("Ya existe una tienda con este dominio: " + request.getDomain());
        }

        try {
            // 3. Crear el usuario PRIMERO (sin tienda)
            RegisterAppUserRequest userRequest = new RegisterAppUserRequest();
            userRequest.setEmail(request.getEmail());
            userRequest.setPassword(request.getPassword());

            AppUserResponse createdUserResponse = appUserService.createUser(userRequest);

            // 4. Obtener la entidad del usuario creado
            AppUser createdUser = appUserService.getUserEntityById(createdUserResponse.getId());

            // 5. Crear la tienda asociada al usuario
            RegisterStoreRequest storeRequest = new RegisterStoreRequest();
            storeRequest.setDomain(request.getDomain());
            storeRequest.setThemeSettings(request.getThemeSettings());

            // La tienda requiere el usuario como dueño
            storeService.createStore(storeRequest, createdUser);

            // 6. Retornar la respuesta del usuario (que ahora tiene su tienda)
            return appUserService.getById(createdUser.getId());

        } catch (Exception e) {
            // Si algo falla, la anotación @Transactional se encarga del rollback
            throw new RuntimeException("Error al registrar el usuario y la tienda: " + e.getMessage(), e);
        }
    }
}
