package com.cyna.products.controllers;

import com.cyna.products.dto.ProductDto;
import com.cyna.products.models.Product;
import com.cyna.products.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String text){
        return ResponseEntity.ok(productService.findByText(text));
    }

    @PostMapping
    public ResponseEntity<String> create(@ModelAttribute ProductDto productdto) {
        return ResponseEntity.ok(productService.create(productdto));
    }

    @PatchMapping
    public ResponseEntity<String> update(@ModelAttribute ProductDto productdto) {
        return ResponseEntity.ok(productService.udpate(productdto));
    }


    @PatchMapping("/{productId}/images")
    public ResponseEntity<String> addImages(@PathVariable long productId, @RequestParam("images") Set<MultipartFile> images) {
        return ResponseEntity.ok(productService.AddImages(productId, images));
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
