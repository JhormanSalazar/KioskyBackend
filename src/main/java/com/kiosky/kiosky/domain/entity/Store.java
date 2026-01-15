package com.kiosky.kiosky.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "store")
public class Store {

    @Id
    @Column(name = "store_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "domain", nullable = false, unique = true)
    private String domain;

    @Column(name = "theme_settings", columnDefinition = "JSONB")
    private String themeSettings;

    // La tienda DEBE tener un dueño (usuario)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_user_id", nullable = false)
    private AppUser appUser;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> categories;

    // Los productos pertenecen a la tienda a través de las categorías
    // Si necesitas acceder a todos los productos de una tienda,
    // puedes hacerlo a través de sus categorías

    /**
     * Método de conveniencia para obtener todos los productos de la tienda
     * a través de sus categorías
     * @return Lista de todos los productos de la tienda
     */
    public List<Product> getAllProducts() {
        if (categories == null) return List.of();
        return categories.stream()
                .flatMap(category -> category.getProducts() != null ?
                    category.getProducts().stream() : Stream.empty())
                .toList();
    }
}
