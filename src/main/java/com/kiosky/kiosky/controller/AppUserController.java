package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.dto.AppUserResponse;
import com.kiosky.kiosky.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (requiere autenticación)")
@SecurityRequirement(name = "Bearer Authentication")
public class AppUserController {

    private final AppUserService appUserService;

    @Operation(summary = "Listar todos los usuarios", description = "Obtiene la lista completa de usuarios del sistema")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppUserResponse.class))))
    @GetMapping
    public ResponseEntity<List<AppUserResponse>> getAll() {
        return ResponseEntity.ok(appUserService.getAll());
    }

    @Operation(summary = "Obtener usuario por ID", description = "Busca y retorna un usuario específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = AppUserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppUserResponse> getById(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        return ResponseEntity.ok(appUserService.getById(id));
    }

    @Operation(summary = "Obtener usuario por tienda", description = "Busca el usuario propietario de una tienda específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = AppUserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/store/{storeId}")
    public ResponseEntity<AppUserResponse> getUserByStoreId(
            @Parameter(description = "ID de la tienda") @PathVariable Long storeId) {
        return appUserService.getUserByStoreId(storeId)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        appUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
