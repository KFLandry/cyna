package com.cyna.products.services;

import com.cyna.products.dto.CarouselDto;
import com.cyna.products.dto.CarouselGetDto;
import com.cyna.products.dto.CarouselMapper;
import com.cyna.products.models.Carousel;
import com.cyna.products.models.Media;
import com.cyna.products.repositories.CarouselRepo;
import com.cyna.products.repositories.CategoryRepo;
import com.cyna.products.repositories.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarouselService {

    private final CarouselRepo carouselRepo;
    private final MediaService mediaService;
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final CarouselMapper carouselMapper;

    public Carousel getCarousel(long id) {
        return carouselRepo.findById(id).orElseThrow();
    }

    public List<CarouselGetDto> getAllCarousel(long limit) {
        return carouselRepo.findAll().stream()
                .limit(limit)
                .map(carouselMapper::toGetDto)
                .toList();
    }

    public Carousel update(CarouselDto carousel) {
        Carousel updateCarousel = carouselRepo.findById(carousel.getId()).orElseThrow();

        updateCarousel.setId(carousel.getId());
        updateCarousel.setTitle(Optional.ofNullable(carousel.getTitle()).orElse(updateCarousel.getTitle()));
        updateCarousel.setText(Optional.ofNullable(carousel.getText()).orElse(updateCarousel.getText()));
        if (!carousel.getImages().isEmpty()) {
            Media image = mediaService.uploadFiles(carousel.getImages()).stream().findFirst().get();
            updateCarousel.setImageUrl(image.getUrl());
        }
        // Assuming that the product and category are optional, you can set them conditionally
        updateCarousel.setProduct(carousel.getProductId()!=0 ? productRepo.findById(carousel.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"))  : updateCarousel.getProduct());

        updateCarousel.setCategory(carousel.getCategoryId() !=0 ?  categoryRepo.findById(carousel.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"))  : updateCarousel.getCategory());

        return carouselRepo.save(updateCarousel);
    }
    public String delete(long id) {
        carouselRepo.deleteById(id);
        return "CarouselDto deleted";
    }

    public Carousel create(CarouselDto carousel) {
        Media image = mediaService.uploadFiles(carousel.getImages()).stream().findFirst().get();

        Carousel newCarousel = Carousel.builder()
                .title(carousel.getTitle())
                .text(carousel.getText())
                .imageUrl(image.getUrl())
                .product(carousel.getProductId() !=0 ? productRepo.findById(carousel.getProductId()).orElseThrow(() -> new RuntimeException("Product not found")) : null)
                .category(carousel.getCategoryId() !=0 ? categoryRepo.findById(carousel.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found")) : null)
                .build();

        return carouselRepo.save(newCarousel);
    }
}
