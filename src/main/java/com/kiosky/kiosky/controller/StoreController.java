package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.dto.RegisterStoreRequest;
import com.kiosky.kiosky.dto.StoreResponse;
import com.kiosky.kiosky.service.AppUserService;
import com.kiosky.kiosky.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/stores")
@Tag(name = "Tiendas", description = "Gestión de tiendas del sistema")
@SecurityRequirement(name = "Bearer Authentication")
public class StoreController {

    private final StoreService storeService;
    private final AppUserService appUserService;

    @Operation(summary = "Listar todas las tiendas", description = "Obtiene la lista completa de tiendas del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de tiendas obtenida exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = StoreResponse.class))))
    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAll() {
        return ResponseEntity.ok(storeService.getAll());
    }

    @Operation(summary = "Obtener tienda por ID", description = "Busca y retorna una tienda específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tienda encontrada",
                    content = @Content(schema = @Schema(implementation = StoreResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tienda no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(
            @Parameter(description = "ID de la tienda") @PathVariable Long id) {
        return ResponseEntity.ok(storeService.getById(id));
    }

    @Operation(summary = "Crear tienda para usuario", description = "Crea una nueva tienda asociada a un usuario existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tienda creada exitosamente",
                    content = @Content(schema = @Schema(implementation = StoreResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PostMapping("/user/{userId}")
    public ResponseEntity<StoreResponse> createStoreForUser(
            @Parameter(description = "ID del usuario propietario") @PathVariable Long userId,
            @Valid @RequestBody RegisterStoreRequest request) {
        // Obtener el usuario que será dueño de la tienda
        AppUser owner = appUserService.getUserEntityById(userId);

        StoreResponse createdStore = storeService.createStore(request, owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);
    }

    @Operation(summary = "Verificar disponibilidad de dominio", description = "Verifica si un dominio de tienda ya existe en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación exitosa"),
            @ApiResponse(responseCode = "400", description = "Dominio inválido")
    })
    @GetMapping("/domain/exists")
    public ResponseEntity<Boolean> checkDomainExists(
            @Parameter(description = "Dominio de la tienda a verificar") @RequestParam String domain) {
        // Normalizar el dominio antes de verificar
        String normalizedDomain = storeService.normalizeDomain(domain);
        boolean exists = storeService.existsByDomain(normalizedDomain);
        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "Eliminar tienda", description = "Elimina una tienda del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tienda eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tienda no encontrada"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(
            @Parameter(description = "ID de la tienda") @PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}
