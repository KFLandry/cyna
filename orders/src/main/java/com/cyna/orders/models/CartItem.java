package com.cyna.orders.models;

import com.cyna.orders.dto.ProductDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
// @Data
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn( name = "card_id", nullable = false)
    private Cart cart;

     @JoinColumn(name = "product_id", nullable = false)
     private Long product;

    @Column(nullable = false)
    private int quantity;
}
