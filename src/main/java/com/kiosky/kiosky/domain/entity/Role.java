package com.kiosky.kiosky.domain.entity;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("Administrador del sistema"),
    OWNER("Dueño de tienda"),
    EMPLOYEE("Empleado de tienda"),
    CUSTOMER("Cliente/Usuario normal");

    private final String description;

    Role(String description) {
        this.description = description;
    }
}
