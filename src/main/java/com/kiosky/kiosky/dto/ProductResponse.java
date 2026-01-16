package com.kiosky.kiosky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private BigDecimal price;
    private String description;
    private String attributes;
    private String[] images;
    private Boolean isVisible;
    private LocalDateTime createdAt;
    private Long categoryId;
    private String categoryName;
    private Long storeId;
    private String storeName;
}
