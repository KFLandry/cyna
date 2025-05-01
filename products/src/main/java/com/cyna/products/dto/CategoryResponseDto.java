package com.cyna.products.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class CategoryResponseDto {
    private Long id;
    private String name;
    private String description;
    private Set<MediaDto> images;
    private List<ProductResponseDto> products;
}
