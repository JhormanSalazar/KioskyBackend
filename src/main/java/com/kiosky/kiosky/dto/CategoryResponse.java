package com.kiosky.kiosky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private Long id;
    private String slug;
    private String name;
    private Long storeId;
    private String storeName;
    private Integer productCount;
}
