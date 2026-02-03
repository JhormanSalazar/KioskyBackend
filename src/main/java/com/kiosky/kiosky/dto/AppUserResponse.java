package com.kiosky.kiosky.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.kiosky.kiosky.domain.entity.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Long storeId;
    private Role role;
}
