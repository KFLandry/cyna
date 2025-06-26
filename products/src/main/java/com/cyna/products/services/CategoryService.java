package com.cyna.products.services;

import com.cyna.products.dto.CategoryDto;
import com.cyna.products.dto.CategoryGetDto;
import com.cyna.products.dto.CategoryMapper;
import com.cyna.products.models.Category;
import com.cyna.products.models.Media;
import com.cyna.products.repositories.CategoryRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private CategoryMapper categoryMapper;

    public String createCategory(CategoryDto categoryDto) {

        Set<Media> medias = mediaService.uploadFiles(categoryDto.getImages());
        Category category =  Category.builder()
                .name(categoryDto.getName())
                .images(medias)
                .description(categoryDto.getDescription())
                .build();
        categoryRepo.save(category);
        return "Operation successful";

    }

    public List<CategoryGetDto> getCategories() {
        List<Category> categories = (List<Category>) categoryRepo.findAll();
        return categories.stream().map(categoryMapper::toDto).toList();
    }


    public Category getCategoryById(Long id) {
        return categoryRepo.findById(id).orElseThrow();
    }

    public List<Category> getCategoryByName(String name) {
        return categoryRepo.findByName(name);
    }

    public String update(long categoryId, String name) {
        Category category = categoryRepo.findById(categoryId).orElseThrow();
        category.setName(name);
        categoryRepo.save(category);

        return "Operation successful";
    }


    public String AddImages(long categoryId, Set<MultipartFile> images) {

        Category category = categoryRepo.findById(categoryId).orElseThrow();
        Set<Media> medias = mediaService.uploadFiles(images);
        medias.addAll(category.getImages());
        category.setImages(medias);
        categoryRepo.save(category);

        return "Operation successful";
    }

    public String deleteImages(long categoryId, Set<Long> imagesId) {

        Category category = categoryRepo.findById(categoryId).orElseThrow();
        if (imagesId.size() >= category.getImages().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You most delete less images than what exist");
        }
        Set<Media> imagesToDelete = mediaService.getMediaByIds(imagesId);
        if (imagesToDelete.isEmpty() || imagesToDelete.size() != imagesId.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Images must exist");
        }
        Set<Media> imagesToKeep = category.getImages();
        imagesToDelete.forEach(imagesToKeep::remove);
        category.setImages(imagesToKeep);
        categoryRepo.save(category);

        mediaService.deleteImages(imagesToDelete);

        return "Operation successful";
    }

    public String delete(long categoryId) {
        categoryRepo.deleteById(categoryId);

        return "Operation successful";
    }

    public String update(long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepo.findById(categoryId).orElseThrow();

        // Mise à jour des champs textuels
        if (categoryDto.getName() != null) {
            category.setName(categoryDto.getName());
        }

        if (categoryDto.getDescription() != null) {
            category.setDescription(categoryDto.getDescription());
        }

        // Mise à jour des images si fournies
        if (categoryDto.getImages() != null && !categoryDto.getImages().isEmpty()) {
            Set<Media> newImages = mediaService.uploadFiles(categoryDto.getImages());
            Set<Media> allImages = new HashSet<>(category.getImages());
            allImages.addAll(newImages);
            category.setImages(allImages);
        }

        // Suppression des images si demandée
        if (categoryDto.getImagesToDelete() != null && !categoryDto.getImagesToDelete().isEmpty()) {
            if (categoryDto.getImagesToDelete().size() >= category.getImages().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must keep at least one image");
            }

            Set<Media> imagesToDelete = mediaService.getMediaByIds(categoryDto.getImagesToDelete());
            Set<Media> imagesToKeep = category.getImages();
            imagesToDelete.forEach(imagesToKeep::remove);
            category.setImages(imagesToKeep);

            mediaService.deleteImages(imagesToDelete);
        }

        categoryRepo.save(category);
        return "Operation successful";
    }
}
