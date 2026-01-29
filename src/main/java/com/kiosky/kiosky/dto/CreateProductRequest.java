package com.kiosky.kiosky.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotBlank(message = "El slug del producto es obligatorio")
    private String slug;

    @NotNull(message = "El precio del producto es obligatorio")
    @PositiveOrZero(message = "El precio debe ser positivo o cero")
    private BigDecimal price;

    private String description;

    private String attributes;

    private String[] images;

    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoryId;

    private Boolean isVisible = true;

    @NotNull(message = "El ID de la tienda es obligatorio")
    private Long storeId;
}
