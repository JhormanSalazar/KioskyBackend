package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.dto.AppUserResponse;
import com.kiosky.kiosky.dto.RegisterAppUserRequest;
import com.kiosky.kiosky.dto.RegisterStoreWithUserRequest;
import com.kiosky.kiosky.service.AppUserService;
import com.kiosky.kiosky.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class AppUserController {

    private final AppUserService appUserService;
    private final RegistrationService registrationService;

    @GetMapping
    public ResponseEntity<List<AppUserResponse>> getAll() {
        return ResponseEntity.ok(appUserService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appUserService.getById(id));
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<AppUserResponse> getUserByStoreId(@PathVariable Long storeId) {
        return appUserService.getUserByStoreId(storeId)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<AppUserResponse> createUser(@Valid @RequestBody RegisterAppUserRequest request) {
        AppUserResponse createdUser = appUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/register-store-with-user")
    public ResponseEntity<AppUserResponse> registerStoreWithUser(@Valid @RequestBody RegisterStoreWithUserRequest request) {
        AppUserResponse createdUser = registrationService.registerStoreWithUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        appUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
