package com.kiosky.kiosky.dto;

import com.kiosky.kiosky.domain.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private Long storeId;
}
