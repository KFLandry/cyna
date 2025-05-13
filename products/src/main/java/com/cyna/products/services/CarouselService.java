package com.cyna.products.services;

import com.cyna.products.models.Carousel;
import com.cyna.products.repositories.CarouselRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarouselService {

    private final CarouselRepo carouselRepo;

    public Carousel getCarousel(long id) {
        return carouselRepo.findById(id).orElseThrow();
    }

    public List<Carousel> getAllCarousel() {
        return carouselRepo.findAll();
    }

    public Carousel update(Carousel carousel) {
        Carousel updateCarousel = carouselRepo.findById(carousel.getId()).orElseThrow();

        updateCarousel.setTitle(Optional.ofNullable(carousel.getTitle()).orElse(updateCarousel.getTitle()));
        updateCarousel.setText(Optional.ofNullable(carousel.getText()).orElse(updateCarousel.getText()));

        return carouselRepo.save(carousel);
    }
    public String delete(long id) {
        carouselRepo.deleteById(id);
        return "Carousel deleted";
    }

    public Carousel create(Carousel carousel) {
        return carouselRepo.save(carousel);
    }
}
