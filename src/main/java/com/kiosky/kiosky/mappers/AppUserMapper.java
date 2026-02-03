package com.kiosky.kiosky.mappers;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.dto.RegisterAppUserRequest;
import com.kiosky.kiosky.dto.AppUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppUserMapper {

    @Mapping(target = "storeId", source = "appUser.store.id")
    @Mapping(target = "role", source = "appUser.role")
    AppUserResponse toResponseDto(AppUser appUser);
    List<AppUserResponse> toResponseDtoList(List<AppUser> appUsers);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "role", ignore = true)
    AppUser toEntity(RegisterAppUserRequest registerAppUserRequest);

}
