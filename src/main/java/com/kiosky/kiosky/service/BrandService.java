package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.Brand;
import com.kiosky.kiosky.domain.repository.BrandRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class BrandService {
    @Autowired
    private final BrandRepository brandRepository;

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

}
