package com.kiosky.kiosky.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAppUserRequest {
    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;
    private String password;    
}
