package com.kiosky.kiosky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request para actualizar la visibilidad de un producto")
public class UpdateProductVisibilityRequest {
    
    @NotNull(message = "El campo isVisible es obligatorio")
    @Schema(description = "Visibilidad del producto", example = "true", required = true)
    private Boolean isVisible;
}
