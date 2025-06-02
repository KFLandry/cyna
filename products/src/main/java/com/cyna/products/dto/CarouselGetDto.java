package com.cyna.products.dto;


import lombok.Data;

@Data
public class CarouselGetDto {

    private Long id;

    private String title;

    private String text;

    private String imageUrl;

    private long productId;

    private long categoryId;
}
