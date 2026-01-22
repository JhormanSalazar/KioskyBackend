package com.kiosky.kiosky.security;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserService appUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 🔑 username = email (tu identificador único)
        // Spring Security llama a este método cuando alguien intenta hacer login
        AppUser user = appUserService.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // 🏗️ Construimos el objeto UserDetails que Spring Security entiende
        return User.builder()
                .username(user.getEmail())  // ✅ Usamos email como username
                .password(user.getPassword())  // Contraseña hasheada de la BD
                .authorities(getAuthorities(user.getRole().name()))  // ✅ Pasamos el STRING del rol
                .build();
    }

    /**
     * 🎭 Convierte el rol en "gafetes" que Spring Security puede leer
     *
     * Ejemplo:
     * - Entrada: "ADMIN"
     * - Salida: [GrantedAuthority("ROLE_ADMIN")]
     *
     * Spring Security SIEMPRE necesita el prefijo "ROLE_" para roles
     */
    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        // 📝 SimpleGrantedAuthority es como escribir el texto en el gafete
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
