package com.kiosky.kiosky.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "JSONB")
    private String attributes;

    @Column(name = "images", columnDefinition = "TEXT[]")
    private String[] images;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Foreign key relationship - Product pertenece a una categoría,
    // y la tienda se obtiene a través de category.store
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Foreign key relationship directo - Product pertenece a una tienda
    // (desnormalización para mejorar performance en consultas)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isVisible == null) {
            isVisible = true;
        }
    }

    /**
     * Método de conveniencia para obtener la tienda a la que pertenece este producto
     * @return La tienda del producto (relación directa)
     */
    public Store getStore() {
        return store;
    }

    /**
     * Valida que la tienda del producto sea consistente con la tienda de su categoría
     * @throws IllegalStateException si hay inconsistencia entre product.store y category.store
     */
    public void validateStoreConsistency() {
        if (category != null && store != null && category.getStore() != null) {
            if (!store.getId().equals(category.getStore().getId())) {
                throw new IllegalStateException(
                    String.format("Inconsistencia detectada: Product.store.id=%d != Category.store.id=%d", 
                                store.getId(), category.getStore().getId()));
            }
        }
    }
}
