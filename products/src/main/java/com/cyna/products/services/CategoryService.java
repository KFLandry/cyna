package com.cyna.products.services;

import com.cyna.products.models.Category;
import com.cyna.products.models.Media;
import com.cyna.products.repositories.CategoryRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private MediaService mediaService;

    @Value("${server.url}")
    private String urlServer;

    public String createCategory(Set<MultipartFile> images, String name) {

        Set<Media> medias = mediaService.uploadFiles(images);
        Category category =  Category.builder()
                .name(name)
                .images(medias)
                .build();
        categoryRepo.save(category);
        return "Operation successful";
    }

    public List<Category> getCategories() {
        List<Category> categories = (List<Category>) categoryRepo.findAll();
        categories.forEach(category -> this.pathToUrl(category.getImages()));
        return categories;
    }

    public Category getCategoryById(Long id) {
        Category category = categoryRepo.findById(id).orElseThrow();
        this.pathToUrl(category.getImages());
        return category;
    }

    public Set<Category> getCategoryByName(String name) {
        Set<Category> categories = categoryRepo.findByName(name);
        categories.forEach(category -> this.pathToUrl(category.getImages()));
        return categories;
    }

    private void pathToUrl(Set<Media> images){
        images.forEach(image -> image.setPath(urlServer+"/"+ image.getName()));
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
}
