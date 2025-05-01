package com.cyna.products.mappers;

import com.cyna.products.dto.MediaDto;
import com.cyna.products.dto.ProductResponseDto;
import com.cyna.products.models.Media;
import com.cyna.products.models.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductResponseDto toDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .caracteristics(product.getCaracteristics())
                .pricingModel(product.getPricingModel().name())
                .amount(product.getAmount())
                .status(product.getStatus().name())
                .images(toMediaDtoSet(product.getImages()))
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null) // 🟢 pas le Category complet !
                .build();
    }


    public Set<ProductResponseDto> toDtoSet(Set<Product> products) {
        return products.stream().map(this::toDto).collect(Collectors.toSet());
    }

    public List<ProductResponseDto> toDtoList(Set<Product> products) {
        return new ArrayList<>(products) // ✅ on crée une copie stable ici
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    private Set<MediaDto> toMediaDtoSet(Set<Media> medias) {
        return medias.stream()
                .map(media -> MediaDto.builder()
                        .id(media.getId())
                        .name(media.getName())
                        .url(media.getUrl())
                        .uploadDate(media.getUploadDate())
                        .build())
                .collect(Collectors.toSet());
    }
}
