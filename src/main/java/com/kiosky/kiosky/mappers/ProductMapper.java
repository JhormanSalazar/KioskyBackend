package com.kiosky.kiosky.mappers;

import com.kiosky.kiosky.domain.entity.Product;
import com.kiosky.kiosky.dto.CreateProductRequest;
import com.kiosky.kiosky.dto.ProductResponse;
import com.kiosky.kiosky.dto.UpdateProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "store.domain", target = "storeName")
    ProductResponse toResponseDto(Product product);

    List<ProductResponse> toResponseDtoList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(CreateProductRequest createProductRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(UpdateProductRequest updateProductRequest, @MappingTarget Product product);
}
