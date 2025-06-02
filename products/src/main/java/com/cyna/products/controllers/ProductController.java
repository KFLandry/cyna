package com.cyna.products.controllers;

import com.cyna.products.dto.Pagination;
import com.cyna.products.dto.ProductDto;
import com.cyna.products.dto.ProductGetDto;
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
    public ResponseEntity<ProductGetDto> getProduct(@PathVariable long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping
    public ResponseEntity<List<ProductGetDto>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @GetMapping("/pagination")
    public ResponseEntity<Pagination> getProducts(@RequestParam(value = "page", defaultValue = "0") int page,
                                                  @RequestParam(value = "size", defaultValue = "6") int size,
                                                  @RequestParam(value = "categoriesIds", required = false) Set<Long> categoriesIds,
                                                  @RequestParam(value = "promoOnly", defaultValue = "0") boolean promoOnly,
                                                  @RequestParam(value = "sort", defaultValue = "desc") String sort
    ) {
        return ResponseEntity.ok((productService.getProductsByCategories(categoriesIds,promoOnly, page, size, sort)));
    }

    @GetMapping(value = "top-products")
    public ResponseEntity<List<ProductGetDto>> getTopProducts(@RequestParam(value = "top", defaultValue = "10") int top, @RequestParam(value = "promo", required = false)  Boolean promo, @RequestParam(value = "active", defaultValue = "true") Boolean active) {
        return ResponseEntity.ok(productService.getTopProducts(top, promo, active));
    }

    @GetMapping("/search")
    public ResponseEntity<Pagination> searchProducts(@RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value="page" , defaultValue = "0") long page,
                                                        @RequestParam(value="size" , defaultValue = "6") long size) {
        return ResponseEntity.ok(productService.findByText(keyword, page, size));
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
