package com.cyna.products.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "products", indexes = {
        @Index(name = "idx_fulltext", columnList = "name, description, brand")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String priceId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, columnDefinition = ("TEXT"))
    private String caracteristics;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PricingModel pricingModel;

    @Column(nullable = false)
    private Long amount;

    @JsonBackReference
    @JoinColumn(nullable = false, name = "category_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;


    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "media_product",
            joinColumns = @JoinColumn(name = "product_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "media_id", nullable = false)
    )
    private Set<Media> images;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.AVAILABLE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean promo;
}
