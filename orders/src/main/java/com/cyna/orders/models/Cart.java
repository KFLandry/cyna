package com.cyna.orders.models;

import com.cyna.orders.dto.UserDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
// @AllArgsConstructor
// @NoArgsConstructor
// @Builder
// @Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     @JoinColumn(name = "user_id", nullable = false)
     private Long user;

    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY , mappedBy = "cart")
    private List<CartItem> cartItems;

    @Column(nullable = false)
    private LocalDateTime creationDate;

}
