package com.cyna.products.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CarouselDto {

    private Long id;

    private String title;

    private String text;

    private MultipartFile image;

    private long productId;

    private long categoryId;
}
