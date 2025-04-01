package com.cyna.orders.dto;

import lombok.Data;

@Data
public class CustomerDto {
    private long userId;
    private String name;
    private String email;
}
