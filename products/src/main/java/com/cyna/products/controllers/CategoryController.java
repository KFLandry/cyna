package com.cyna.products.controllers;

import com.cyna.products.dto.CategoryDto;
import com.cyna.products.dto.CategoryResponseDto;
import com.cyna.products.services.CategoryService;
import com.cyna.products.mappers.CategoryMapper;
import com.cyna.products.models.Category;
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

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping()
    public ResponseEntity<List<CategoryResponseDto>> getCategories() {
        List<Category> categories = categoryService.getCategories();
        return ResponseEntity.ok(categoryMapper.toDtoList(categories));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(categoryMapper.toDto(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponseDto>> getCategoriesByName(@RequestParam String name) {
        List<Category> categories = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(categoryMapper.toDtoList(categories));
    }

    @PostMapping
    public ResponseEntity<String> createCategory(@ModelAttribute CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDto));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<String> update(@PathVariable long categoryId, @RequestParam String name) {
        return ResponseEntity.ok(categoryService.update(categoryId, name));
    }

    @PatchMapping("/{categoryId}/images")
    public ResponseEntity<String> addImages(@PathVariable long categoryId, @RequestParam("images") Set<MultipartFile> images) {
        return ResponseEntity.ok(categoryService.AddImages(categoryId, images));
    }

    @DeleteMapping("/{categoryId}/images")
    public ResponseEntity<String> deleteImages(@PathVariable long categoryId, @RequestBody Set<Long> imagesId) {
        return ResponseEntity.ok(categoryService.deleteImages(categoryId, imagesId));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable long categoryId) {
        return ResponseEntity.ok(categoryService.delete(categoryId));
    }
}
