package com.cyna.products.controllers;

import com.cyna.products.dto.CategoryDto;
import com.cyna.products.dto.CategoryGetDto;
import com.cyna.products.models.Category;
import com.cyna.products.services.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/categories")
@AllArgsConstructor
public class CategoryController {

    private CategoryService categoryService;

    @GetMapping()
    public ResponseEntity<List<CategoryGetDto>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getCategory(@PathVariable long categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Category>> getCategoriesByName(@RequestParam String name) {
        return ResponseEntity.ok(categoryService.getCategoryByName(name));
    }
    @PostMapping
    public ResponseEntity<String> createCategory(@ModelAttribute CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDto));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<String> update(@PathVariable long categoryId, @RequestParam String name){
        return ResponseEntity.ok(categoryService.update(categoryId, name));
    }


    @PatchMapping("/{categoryId}/images")
    public ResponseEntity<String> addImages(@PathVariable long categoryId, @RequestParam("images") Set<MultipartFile> images) {
        return ResponseEntity.ok(categoryService.AddImages(categoryId, images));
    }

    @DeleteMapping("/{categoryId}/images")
    public ResponseEntity<String> deleteImages(@PathVariable long categoryId, @RequestBody Set<Long> imagesId) {
        return  ResponseEntity.ok(categoryService.deleteImages(categoryId, imagesId));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable long categoryId) {
        return ResponseEntity.ok(categoryService.delete(categoryId));
    }
}
