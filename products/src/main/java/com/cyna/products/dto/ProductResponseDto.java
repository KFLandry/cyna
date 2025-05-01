package com.cyna.products.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class ProductResponseDto {
    private Long id;
    private String name;
    private String brand;
    private String description;
    private String caracteristics;
    private String pricingModel;
    private Long amount;
    private String status;
    private Long categoryId;
    private Set<MediaDto> images;
}
