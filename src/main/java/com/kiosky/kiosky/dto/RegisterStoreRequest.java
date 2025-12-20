package com.kiosky.kiosky.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterStoreRequest {

    @NotBlank(message = "El dominio es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]$",
             message = "El dominio debe contener solo letras, números y guiones, sin espacios")
    private String domain;

    private String themeSettings; // JSON opcional para configuraciones de tema
}
