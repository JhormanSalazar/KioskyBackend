package com.kiosky.kiosky.mappers;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.dto.RegisterAppUserRequest;
import com.kiosky.kiosky.dto.AppUserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppUserMapper {

    AppUserResponse toResponseDto(AppUser appUser);
    List<AppUserResponse> toResponseDtoList(List<AppUser> appUsers);


    AppUser toEntity(RegisterAppUserRequest registerAppUserRequest);

}
