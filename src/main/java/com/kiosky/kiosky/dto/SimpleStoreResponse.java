package com.kiosky.kiosky.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleStoreResponse {
    private Long id;
    private String name;
    private String domain;
}
