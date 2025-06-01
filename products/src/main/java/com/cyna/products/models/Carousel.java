package com.cyna.products.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Carousel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private String imageUrl;

    @JsonBackReference
    @JoinColumn(name = "category_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Category category;

    @JsonBackReference
    @JoinColumn(name = "product_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Product product;
}
