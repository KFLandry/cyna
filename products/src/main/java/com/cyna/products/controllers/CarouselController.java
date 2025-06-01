package com.cyna.products.controllers;

import com.cyna.products.dto.CarouselDto;
import com.cyna.products.dto.CarouselGetDto;
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
    public ResponseEntity<List<CarouselGetDto>> getCarousels(@RequestParam(value ="limits", defaultValue = "10") long limit) {
        return ResponseEntity.ok(carouselService.getAllCarousel(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carousel> getCarousel(@PathVariable long id) {
        return ResponseEntity.ok(carouselService.getCarousel(id));
    }

    @PostMapping
    public ResponseEntity<Carousel> create(@RequestBody CarouselDto carousel) {
        return ResponseEntity.ok(carouselService.create(carousel));
    }

    @PatchMapping
    public ResponseEntity<Carousel> update(@RequestBody CarouselDto carousel) {
        return ResponseEntity.ok(carouselService.update(carousel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable long id) {
        return ResponseEntity.ok(carouselService.delete(id));
    }
}
