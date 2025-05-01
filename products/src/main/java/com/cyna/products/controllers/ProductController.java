package com.cyna.products.controllers;

import com.cyna.products.dto.ProductDto;
import com.cyna.products.dto.ProductResponseDto;
import com.cyna.products.services.ProductService;
import com.cyna.products.mappers.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable long productId) {
        var product = productService.getProduct(productId);
        return ResponseEntity.ok(productMapper.toDto(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getProducts() {
        var products = productService.getProducts();
        return ResponseEntity.ok(products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDto>> searchProducts(@RequestParam(required = false) String text) {
        var results = productService.findByText(text);
        return ResponseEntity.ok(results.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<String> create(@ModelAttribute ProductDto productdto) {
        return ResponseEntity.ok(productService.create(productdto));
    }

    @PatchMapping
    public ResponseEntity<String> update(@RequestBody ProductDto productdto) {
        return ResponseEntity.ok(productService.update(productdto));
    }

    @PatchMapping("/{productId}/images")
    public ResponseEntity<String> addImages(@PathVariable long productId, @RequestParam("images") Set<MultipartFile> images) {
        return ResponseEntity.ok(productService.addImages(productId, images));
    }

    @DeleteMapping("/{productId}/images")
    public ResponseEntity<String> deleteImages(@PathVariable long productId, @RequestBody Set<Long> imagesId) {
        return  ResponseEntity.ok(productService.deleteImages(productId, imagesId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable long productId) {
        return ResponseEntity.ok(productService.deleteProduct(productId));
    }
}
