package com.kiosky.kiosky.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateThemeSettingsRequest {

    @NotBlank(message = "La configuración de tema no puede estar vacía")
    private String themeSettings; // JSON con la configuración visual de la tienda
}
