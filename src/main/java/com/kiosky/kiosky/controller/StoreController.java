package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.dto.RegisterStoreRequest;
import com.kiosky.kiosky.dto.StoreResponse;
import com.kiosky.kiosky.service.AppUserService;
import com.kiosky.kiosky.service.StoreService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;
    private final AppUserService appUserService;

    @GetMapping
    public ResponseEntity<List<StoreResponse>> getAll() {
        return ResponseEntity.ok(storeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.getById(id));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<StoreResponse> createStoreForUser(
            @PathVariable Long userId,
            @Valid @RequestBody RegisterStoreRequest request) {
        // Obtener el usuario que será dueño de la tienda
        AppUser owner = appUserService.getUserEntityById(userId);

        StoreResponse createdStore = storeService.createStore(request, owner);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }
}
