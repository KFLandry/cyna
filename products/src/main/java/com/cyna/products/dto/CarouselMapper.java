package com.cyna.products.dto;

import com.cyna.products.models.Carousel;
import org.springframework.stereotype.Component;

@Component
public class CarouselMapper {

    public CarouselGetDto toGetDto(Carousel carousel) {
        if ( carousel == null ) {
            return null;
        }

        CarouselGetDto carouselGetDto = new CarouselGetDto();

        carouselGetDto.setId( carousel.getId() );
        carouselGetDto.setTitle( carousel.getTitle() );
        carouselGetDto.setText( carousel.getText() );
        carouselGetDto.setImageUrl( carousel.getImageUrl() );
        carouselGetDto.setProductId(carousel.getProduct() != null ? carousel.getProduct().getId() : 0L);
        carouselGetDto.setCategoryId(carousel.getCategory() != null ? carousel.getCategory().getId() : 0L);

        return carouselGetDto;
    }
}
