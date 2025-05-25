package com.cyna.products.dto;

import com.cyna.products.models.Media;
import com.cyna.products.models.Product;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class ProductGetMapper {

    public ProductGetDto toDto(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductGetDto productGetDto = new ProductGetDto();

        if ( product.getId() != null ) {
            productGetDto.setId( product.getId() );
        }
        productGetDto.setPriceId( product.getPriceId() );
        productGetDto.setName( product.getName() );
        productGetDto.setBrand( product.getBrand() );
        productGetDto.setDescription( product.getDescription() );
        productGetDto.setCaracteristics( product.getCaracteristics() );
        productGetDto.setPricingModel( product.getPricingModel() );
        productGetDto.setCategoryId(product.getCategory().getId() );
        productGetDto.setCreatedAt(product.getCreatedAt());
        productGetDto.setUpdatedAt(product.getUpdatedAt());
        productGetDto.setPromo(product.isPromo());
        productGetDto.setActive(product.isActive());
        if ( product.getAmount() != null ) {
            productGetDto.setAmount( product.getAmount() );
        }
        Set<Media> set = product.getImages();
        if ( set != null ) {
            productGetDto.setImages( new LinkedHashSet<>( set ) );
        }
        productGetDto.setStatus( product.getStatus() );

        return productGetDto;
    }
}

