package com.kiosky.kiosky.util;

import com.kiosky.kiosky.domain.entity.AppUser;
import com.kiosky.kiosky.domain.entity.Category;
import com.kiosky.kiosky.domain.entity.Product;
import com.kiosky.kiosky.domain.entity.Role;
import com.kiosky.kiosky.domain.entity.Store;
import com.kiosky.kiosky.domain.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilidades para verificar autorización del usuario actualmente autenticado
 * sobre recursos de la aplicación (tiendas, categorías, productos).
 *
 * Este componente es inyectable y reutilizable en todos los servicios.
 */
@Component
public class AuthorizationUtils {

    private final AppUserRepository appUserRepository;

    public AuthorizationUtils(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    /**
     * Obtiene el usuario actualmente autenticado desde el SecurityContext
     *
     * @return AppUser del usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado o no se encuentra en la BD
     */
    public AppUser getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("No hay un usuario autenticado");
        }

        String email = authentication.getName();
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en la base de datos: " + email));
    }

    /**
     * Obtiene el usuario autenticado desde un objeto Authentication específico
     *
     * @param authentication objeto Authentication de Spring Security
     * @return AppUser del usuario autenticado
     * @throws IllegalStateException si no se puede obtener el usuario
     */
    public AppUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("No hay un usuario autenticado");
        }

        String email = authentication.getName();
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en la base de datos: " + email));
    }

    /**
     * Verifica si el usuario autenticado puede modificar una tienda específica.
     *
     * Reglas:
     * - ADMIN: puede modificar cualquier tienda
     * - OWNER: solo puede modificar su propia tienda
     * - EMPLOYEE/CUSTOMER: no pueden modificar tiendas
     *
     * @param storeId ID de la tienda
     * @return true si el usuario puede modificar la tienda, false en caso contrario
     */
    public boolean canModifyStore(Long storeId) {
        try {
            AppUser currentUser = getCurrentAuthenticatedUser();
            return canModifyStore(currentUser, storeId);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Verifica si un usuario específico puede modificar una tienda
     *
     * @param user el usuario a verificar
     * @param storeId ID de la tienda
     * @return true si el usuario puede modificar la tienda, false en caso contrario
     */
    public boolean canModifyStore(AppUser user, Long storeId) {
        if (user == null || storeId == null) {
            return false;
        }

        // ADMIN puede modificar cualquier tienda
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // OWNER solo puede modificar su propia tienda
        if (user.getRole() == Role.OWNER) {
            return user.getStore() != null && user.getStore().getId().equals(storeId);
        }

        // EMPLOYEE y CUSTOMER no pueden modificar tiendas
        return false;
    }

    /**
     * Verifica si el usuario autenticado puede modificar una tienda específica (entidad Store)
     *
     * @param store la entidad Store
     * @return true si el usuario puede modificar la tienda, false en caso contrario
     */
    public boolean canModifyStore(Store store) {
        if (store == null || store.getId() == null) {
            return false;
        }
        return canModifyStore(store.getId());
    }

    /**
     * Verifica si el usuario autenticado puede modificar una categoría específica.
     *
     * Reglas:
     * - ADMIN: puede modificar cualquier categoría
     * - OWNER: solo puede modificar categorías de su tienda
     * - EMPLOYEE: solo puede modificar categorías de la tienda donde trabaja
     * - CUSTOMER: no puede modificar categorías
     *
     * @param categoryId ID de la categoría
     * @param categoryStoreId ID de la tienda a la que pertenece la categoría
     * @return true si el usuario puede modificar la categoría, false en caso contrario
     */
    public boolean canModifyCategory(Long categoryId, Long categoryStoreId) {
        try {
            AppUser currentUser = getCurrentAuthenticatedUser();
            return canModifyCategory(currentUser, categoryStoreId);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Verifica si un usuario específico puede modificar categorías de una tienda
     *
     * @param user el usuario a verificar
     * @param storeId ID de la tienda a la que pertenece la categoría
     * @return true si el usuario puede modificar la categoría, false en caso contrario
     */
    public boolean canModifyCategory(AppUser user, Long storeId) {
        if (user == null || storeId == null) {
            return false;
        }

        // ADMIN puede modificar cualquier categoría
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // OWNER puede modificar categorías de su tienda
        if (user.getRole() == Role.OWNER) {
            return user.getStore() != null && user.getStore().getId().equals(storeId);
        }

        // EMPLOYEE puede modificar categorías de su tienda
        if (user.getRole() == Role.EMPLOYEE) {
            return user.getStore() != null && user.getStore().getId().equals(storeId);
        }

        // CUSTOMER no puede modificar categorías
        return false;
    }

    /**
     * Verifica si el usuario autenticado puede modificar una categoría (entidad Category)
     *
     * @param category la entidad Category
     * @return true si el usuario puede modificar la categoría, false en caso contrario
     */
    public boolean canModifyCategory(Category category) {
        if (category == null || category.getStore() == null) {
            return false;
        }
        return canModifyCategory(category.getId(), category.getStore().getId());
    }

    /**
     * Verifica si el usuario autenticado puede modificar un producto específico.
     *
     * Reglas:
     * - ADMIN: puede modificar cualquier producto
     * - OWNER: solo puede modificar productos de su tienda
     * - EMPLOYEE: solo puede modificar productos de la tienda donde trabaja
     * - CUSTOMER: no puede modificar productos
     *
     * @param productId ID del producto
     * @param productStoreId ID de la tienda a la que pertenece el producto (a través de su categoría)
     * @return true si el usuario puede modificar el producto, false en caso contrario
     */
    public boolean canModifyProduct(Long productId, Long productStoreId) {
        try {
            AppUser currentUser = getCurrentAuthenticatedUser();
            return canModifyProduct(currentUser, productStoreId);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Verifica si un usuario específico puede modificar productos de una tienda
     *
     * @param user el usuario a verificar
     * @param storeId ID de la tienda a la que pertenece el producto
     * @return true si el usuario puede modificar el producto, false en caso contrario
     */
    public boolean canModifyProduct(AppUser user, Long storeId) {
        if (user == null || storeId == null) {
            return false;
        }

        // ADMIN puede modificar cualquier producto
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // OWNER puede modificar productos de su tienda
        if (user.getRole() == Role.OWNER) {
            return user.getStore() != null && user.getStore().getId().equals(storeId);
        }

        // EMPLOYEE puede modificar productos de su tienda
        if (user.getRole() == Role.EMPLOYEE) {
            return user.getStore() != null && user.getStore().getId().equals(storeId);
        }

        // CUSTOMER no puede modificar productos
        return false;
    }

    /**
     * Verifica si el usuario autenticado puede modificar un producto (entidad Product)
     *
     * @param product la entidad Product
     * @return true si el usuario puede modificar el producto, false en caso contrario
     */
    public boolean canModifyProduct(Product product) {
        if (product == null || product.getCategory() == null ||
            product.getCategory().getStore() == null) {
            return false;
        }
        Long storeId = product.getCategory().getStore().getId();
        return canModifyProduct(product.getId(), storeId);
    }

    /**
     * Verifica si el usuario autenticado tiene acceso de lectura a una tienda.
     * Por defecto, todas las tiendas son públicas para lectura, pero este método
     * puede ser útil para futuras restricciones.
     *
     * @param storeId ID de la tienda
     * @return true si el usuario puede ver la tienda
     */
    public boolean canReadStore(Long storeId) {
        // Por defecto, todas las tiendas son públicas para lectura
        // Puedes agregar lógica adicional aquí si es necesario
        return storeId != null;
    }

    /**
     * Verifica si el usuario autenticado es ADMIN
     *
     * @return true si el usuario es ADMIN, false en caso contrario
     */
    public boolean isAdmin() {
        try {
            AppUser currentUser = getCurrentAuthenticatedUser();
            return currentUser.getRole() == Role.ADMIN;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Verifica si el usuario autenticado es OWNER de alguna tienda
     *
     * @return true si el usuario es OWNER, false en caso contrario
     */
    public boolean isOwner() {
        try {
            AppUser currentUser = getCurrentAuthenticatedUser();
            return currentUser.getRole() == Role.OWNER;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Verifica si el usuario autenticado es OWNER de una tienda específica
     *
     * @param storeId ID de la tienda
     * @return true si el usuario es OWNER de esa tienda, false en caso contrario
     */
    public boolean isOwnerOfStore(Long storeId) {
        try {
            AppUser currentUser = getCurrentAuthenticatedUser();
            return currentUser.getRole() == Role.OWNER &&
                   currentUser.getStore() != null &&
                   currentUser.getStore().getId().equals(storeId);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Verifica si el usuario autenticado es EMPLOYEE
     *
     * @return true si el usuario es EMPLOYEE, false en caso contrario
     */
    public boolean isEmployee() {
        try {
            AppUser currentUser = getCurrentAuthenticatedUser();
            return currentUser.getRole() == Role.EMPLOYEE;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Obtiene el ID de la tienda del usuario autenticado (si tiene una)
     *
     * @return ID de la tienda o null si el usuario no tiene tienda
     */
    public Long getCurrentUserStoreId() {
        try {
            AppUser currentUser = getCurrentAuthenticatedUser();
            return currentUser.getStore() != null ? currentUser.getStore().getId() : null;
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
