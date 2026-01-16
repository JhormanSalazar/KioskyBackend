package com.kiosky.kiosky.mappers;

import com.kiosky.kiosky.domain.entity.Category;
import com.kiosky.kiosky.dto.CategoryResponse;
import com.kiosky.kiosky.dto.CreateCategoryRequest;
import com.kiosky.kiosky.dto.UpdateCategoryRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.domain", target = "storeName")
    @Mapping(target = "productCount", expression = "java(category.getProducts() != null ? category.getProducts().size() : 0)")
    CategoryResponse toResponseDto(Category category);

    List<CategoryResponse> toResponseDtoList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CreateCategoryRequest createCategoryRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntityFromDto(UpdateCategoryRequest updateCategoryRequest, @MappingTarget Category category);
}
