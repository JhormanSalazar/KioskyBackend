package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.repository.AppUserRepository;
import com.kiosky.kiosky.dto.AppUserResponse;
import com.kiosky.kiosky.dto.RegisterAppUserRequest;
import com.kiosky.kiosky.dto.UpdateAppUserRequest;
import com.kiosky.kiosky.mappers.AppUserMapper;
import com.kiosky.kiosky.util.PasswordUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final PasswordUtils passwordUtils;

    public List<AppUserResponse> getAll() {
        return appUserMapper.toResponseDtoList(appUserRepository.findAll());
    }

    public AppUserResponse getById(Long id) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un usuario con el ID: " + id));
        return appUserMapper.toResponseDto(appUser);
    }

    /**
     * Obtiene la entidad AppUser por ID (para uso interno en servicios)
     * @param id ID del usuario
     * @return La entidad AppUser
     * @throws EntityNotFoundException si no se encuentra el usuario
     */
    public AppUser getUserEntityById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo.");
        }

        return appUserRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró un usuario con el ID: " + id));
    }

    @Transactional
    public AppUserResponse createUser(RegisterAppUserRequest request) {
        // Validar si el email ya existe
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email: " + request.getEmail());
        }

        // Validar y hashear la contraseña
        String hashedPassword = passwordUtils.hashPassword(request.getPassword());

        // Crear el usuario SIN tienda (la tienda se creará después)
        AppUser appUser = new AppUser();
        appUser.setFullName(request.getFullName());
        appUser.setEmail(request.getEmail());
        appUser.setPassword(hashedPassword);
        // NO asignar store aquí

        // Guardar el usuario
        AppUser savedUser = appUserRepository.save(appUser);

        return appUserMapper.toResponseDto(savedUser);
    }

    @Transactional
    public AppUserResponse updateUser(Long id, UpdateAppUserRequest request) {
         // Validar si el email ya existe
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email: " + request.getEmail());
        }

        AppUser appUser = getUserEntityById(id);
        
        appUser.setFullName(request.getFullName());
        appUser.setEmail(request.getEmail());

        // Si se proporciona una nueva contraseña, validarla y hashearla
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            String hashedPassword = passwordUtils.hashPassword(request.getPassword());
            appUser.setPassword(hashedPassword);
        }

        AppUser updatedUser = appUserRepository.save(appUser);
        return appUserMapper.toResponseDto(updatedUser);
    }

    public boolean existsByEmail(String email) {
        return appUserRepository.existsByEmail(email);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    /**
     * Obtiene el usuario dueño de una tienda específica
     * @param storeId ID de la tienda
     * @return Optional del usuario dueño
     */
    public Optional<AppUserResponse> getUserByStoreId(Long storeId) {
        Optional<AppUser> appUser = appUserRepository.findByStoreId(storeId);
        return appUser.map(appUserMapper::toResponseDto);
    }

    /**
     * Verifica si una tienda ya tiene un usuario asignado
     * @param storeId ID de la tienda
     * @return true si la tienda ya tiene usuario, false en caso contrario
     */
    public boolean storeHasUser(Long storeId) {
        return appUserRepository.existsByStoreId(storeId);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo.");
        }
        appUserRepository.deleteById(id);
    }

    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordUtils.matches(rawPassword, hashedPassword);
    }
}
