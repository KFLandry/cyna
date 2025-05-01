package com.cyna.products.mappers;

import com.cyna.products.dto.CategoryResponseDto;
import com.cyna.products.dto.MediaDto;
import com.cyna.products.dto.ProductResponseDto;
import com.cyna.products.models.Category;
import com.cyna.products.models.Media;
import com.cyna.products.models.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    private final ProductMapper productMapper;

    public CategoryResponseDto toDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .images(toMediaDtos(category.getImages()))
                .products(productMapper.toDtoList(new HashSet<>(category.getProducts())))
                .build();
    }

    public List<CategoryResponseDto> toDtoList(List<Category> categories) {
        return categories.stream().map(this::toDto).collect(Collectors.toList());
    }

    private Set<MediaDto> toMediaDtos(Set<Media> medias) {
        return medias.stream().map(media -> MediaDto.builder()
                        .id(media.getId())
                        .name(media.getName())
                        .url(media.getUrl())
                        .uploadDate(media.getUploadDate())
                        .build())
                .collect(Collectors.toSet());
    }
}
