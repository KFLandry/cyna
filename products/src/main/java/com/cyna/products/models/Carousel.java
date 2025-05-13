package com.cyna.products.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Carousel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String text;
}
