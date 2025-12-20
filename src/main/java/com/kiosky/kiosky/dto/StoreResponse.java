package com.kiosky.kiosky.dto;

import com.kiosky.kiosky.domain.entity.Category;
import com.kiosky.kiosky.domain.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponse {
    private Long id;
    private String domain;
    private String themeSettings;
    private List<Category> categories;
    private List<Product> products;
}
