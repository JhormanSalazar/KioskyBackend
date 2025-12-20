package com.kiosky.kiosky.mappers;

import com.kiosky.kiosky.domain.entity.Store;
import com.kiosky.kiosky.dto.StoreResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMapper {
    StoreResponse toResponseDto(Store store);

    List<StoreResponse> toResponseDtoList(List<Store> stores);
}
