package com.kiosky.kiosky.controller;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.entity.Brand;
import com.kiosky.kiosky.service.AppUserService;
import com.kiosky.kiosky.service.BrandService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class AppUserController {

    @Autowired
    private final AppUserService appUserService;

    @Autowired
    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<AppUser>> getAll() {
        return ResponseEntity.ok(appUserService.getAll());
    }

    @GetMapping("/get-brands")
    public ResponseEntity<List<Brand>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }
}
