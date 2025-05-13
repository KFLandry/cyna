package com.cyna.products.controllers;

import com.cyna.products.models.Carousel;
import com.cyna.products.services.CarouselService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carousel")
@RequiredArgsConstructor
public class CarouselController {

    private final CarouselService carouselService;

    @GetMapping
    public ResponseEntity<List<Carousel>> getCarousels() {
        return ResponseEntity.ok(carouselService.getAllCarousel());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carousel> getCarousel(@PathVariable long id) {
        return ResponseEntity.ok(carouselService.getCarousel(id));
    }

    @PostMapping
    public ResponseEntity<Carousel> create(@RequestBody Carousel carousel) {
        return ResponseEntity.ok(carouselService.create(carousel));
    }

    @PatchMapping
    public ResponseEntity<Carousel> update(@RequestBody Carousel carousel) {
        return ResponseEntity.ok(carouselService.update(carousel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable long id) {
        return ResponseEntity.ok(carouselService.delete(id));
    }
}
