package com.kiosky.kiosky.service;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.repository.AppUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AppUserService {

    @Autowired
    private final AppUserRepository appUserRepository;

    public List<AppUser> getAll() {
        return appUserRepository.findAll();
    }
}
