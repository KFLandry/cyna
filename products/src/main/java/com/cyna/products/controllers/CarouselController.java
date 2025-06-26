package com.cyna.products.controllers;

import com.cyna.products.dto.CarouselDto;
import com.cyna.products.dto.CarouselGetDto;
import com.cyna.products.models.Carousel;
import com.cyna.products.services.CarouselService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/carousel")
@RequiredArgsConstructor
@Slf4j
public class CarouselController {

    private final CarouselService carouselService;

    @GetMapping
    public ResponseEntity<List<CarouselGetDto>> getCarousels(
            @RequestParam(value ="limits", defaultValue = "1000") long limit // <-- augmente la valeur par défaut
    ) {
        return ResponseEntity.ok(carouselService.getAllCarousel(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carousel> getCarousel(@PathVariable long id) {
        return ResponseEntity.ok(carouselService.getCarousel(id));
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<Carousel> create(
            @RequestParam("title") String title,
            @RequestParam("text") String text,
            @RequestParam(value = "productId", required = false, defaultValue = "0") long productId,
            @RequestParam(value = "categoryId", required = false, defaultValue = "0") long categoryId,
            @RequestParam("images") MultipartFile[] images) {

        // Correction : accepter aussi un seul fichier (images.length == 1)
        Set<MultipartFile> imageSet = new HashSet<>();
        if (images != null) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    imageSet.add(image);
                }
            }
        }
        log.info("CarouselController: images received count = {}", imageSet.size());
        if (imageSet.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Au moins une image valide est requise");
        }

        CarouselDto carouselDto = new CarouselDto();
        carouselDto.setTitle(title);
        carouselDto.setText(text);
        carouselDto.setProductId(productId);
        carouselDto.setCategoryId(categoryId);
        carouselDto.setImages(imageSet);

        log.info("Creating carousel '{}' with {} images", title, imageSet.size());
        return ResponseEntity.ok(carouselService.create(carouselDto));
    }

    @PatchMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<Carousel> update(
            @RequestParam("id") Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "productId", required = false, defaultValue = "0") long productId,
            @RequestParam(value = "categoryId", required = false, defaultValue = "0") long categoryId,
            @RequestParam(value = "replaceImage", required = false, defaultValue = "false") boolean replaceImage,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        CarouselDto carouselDto = new CarouselDto();
        carouselDto.setId(id);
        if (title != null) carouselDto.setTitle(title);
        if (text != null) carouselDto.setText(text);
        carouselDto.setProductId(productId);
        carouselDto.setCategoryId(categoryId);
        carouselDto.setReplaceImage(replaceImage);

        // Correction : images optionnelles, et on ne set replaceImage à true que si demandé
        if (images != null && images.length > 0) {
            Set<MultipartFile> imageSet = new HashSet<>();
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    imageSet.add(image);
                }
            }
            carouselDto.setImages(imageSet);
        }

        log.info("Updating carousel {} with title '{}', replaceImage={}",
                id, title, carouselDto.isReplaceImage());
        return ResponseEntity.ok(carouselService.update(carouselDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable long id) {
        return ResponseEntity.ok(carouselService.delete(id));
    }

}