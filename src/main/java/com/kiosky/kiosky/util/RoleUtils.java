package com.kiosky.kiosky.util;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.entity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Utilidades para trabajar con roles en Spring Security
 */
public class RoleUtils {

    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Convierte un Role enum a una lista de GrantedAuthority para Spring Security
     */
    public static Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        return List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.name()));
    }

    /**
     * Verifica si un rol tiene permisos específicos basado en la jerarquía
     */
    public static boolean hasPermission(Role userRole, Role requiredRole) {
        // ADMIN tiene acceso a todo
        if (userRole == Role.ADMIN) {
            return true;
        }

        // OWNER tiene acceso a EMPLOYEE y CUSTOMER
        if (userRole == Role.OWNER &&
            (requiredRole == Role.EMPLOYEE || requiredRole == Role.CUSTOMER)) {
            return true;
        }

        // EMPLOYEE tiene acceso a CUSTOMER
        if (userRole == Role.EMPLOYEE && requiredRole == Role.CUSTOMER) {
            return true;
        }

        // Mismo nivel de acceso
        return userRole == requiredRole;
    }

    /**
     * Verifica si un usuario puede acceder/modificar una tienda específica
     * Reglas de negocio:
     * - ADMIN: Puede acceder a cualquier tienda
     * - OWNER: Solo puede acceder a SU propia tienda
     * - EMPLOYEE/CUSTOMER: No pueden modificar tiendas directamente
     *
     * @param user el usuario que intenta acceder
     * @param storeId el ID de la tienda a la que intenta acceder
     * @return true si tiene permisos, false si no
     */
    public static boolean canAccessStore(AppUser user, Long storeId) {
        if (user == null || storeId == null) {
            return false;
        }

        // ADMIN tiene acceso a todas las tiendas
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // OWNER solo puede acceder a su propia tienda
        if (user.getRole() == Role.OWNER) {
            return user.getStore() != null && user.getStore().getId().equals(storeId);
        }

        // EMPLOYEE y CUSTOMER no pueden modificar tiendas directamente
        return false;
    }

    /**
     * Verifica si un usuario puede acceder/modificar productos o categorías de una tienda
     * Reglas de negocio:
     * - ADMIN: Puede acceder a cualquier tienda
     * - OWNER: Solo puede acceder a productos/categorías de SU tienda
     * - EMPLOYEE: Solo puede acceder a productos/categorías de la tienda donde trabaja
     * - CUSTOMER: Solo lectura (no modificación)
     *
     * @param user el usuario que intenta acceder
     * @param storeId el ID de la tienda a la que pertenecen los productos/categorías
     * @return true si tiene permisos, false si no
     */
    public static boolean canAccessStoreContent(AppUser user, Long storeId) {
        if (user == null || storeId == null) {
            return false;
        }

        // ADMIN tiene acceso a todo
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // OWNER puede acceder a contenido de su tienda
        if (user.getRole() == Role.OWNER) {
            return user.getStore() != null && user.getStore().getId().equals(storeId);
        }

        // EMPLOYEE puede acceder a contenido de la tienda donde trabaja
        // Nota: Necesitarías agregar relación Employee->Store para esto
        if (user.getRole() == Role.EMPLOYEE) {
            // Por ahora, asumo que el empleado también tiene referencia a la tienda
            // Esto requeriría modificar el modelo de datos para ser más preciso
            return user.getStore() != null && user.getStore().getId().equals(storeId);
        }

        // CUSTOMER no puede modificar contenido
        return false;
    }

    /**
     * Verifica si un usuario puede modificar una categoría específica
     * Extrae el storeId de la categoría y valida permisos
     *
     * @param user el usuario que intenta modificar
     * @param category la categoría a modificar
     * @return true si tiene permisos, false si no
     */
    public static boolean canModifyCategory(AppUser user, com.kiosky.kiosky.domain.entity.Category category) {
        if (category == null || category.getStore() == null) {
            return false;
        }
        return canAccessStoreContent(user, category.getStore().getId());
    }

    /**
     * Verifica si un usuario puede modificar un producto específico
     * Extrae el storeId del producto a través de su categoría y valida permisos
     *
     * @param user el usuario que intenta modificar
     * @param product el producto a modificar
     * @return true si tiene permisos, false si no
     */
    public static boolean canModifyProduct(AppUser user, com.kiosky.kiosky.domain.entity.Product product) {
        if (product == null || product.getCategory() == null ||
            product.getCategory().getStore() == null) {
            return false;
        }
        return canAccessStoreContent(user, product.getCategory().getStore().getId());
    }

    /**
     * Obtiene el nombre del rol sin prefijo para mostrar en la UI
     */
    public static String getRoleDisplayName(Role role) {
        return role.getDescription();
    }
}
