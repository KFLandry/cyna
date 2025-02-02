package com.cyna.products.services;

import com.cyna.products.dto.ProductDto;
import com.cyna.products.models.Category;
import com.cyna.products.models.Media;
import com.cyna.products.models.Product;
import com.cyna.products.repositories.ProductRepo;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional
public class ProductService {

    private ProductRepo productRepo;
    private MediaService mediaService;
    private CategoryService categoryService;

    public String AddImages(long productId, Set<MultipartFile> images) {

        Product product = productRepo.findById(productId).orElseThrow();
        Set<Media> medias = mediaService.uploadFiles(images);
        medias.addAll(product.getImages());
        product.setImages(medias);
        productRepo.save(product);

        return "Operation successful";
    }

    public String deleteImages(long productId, Set<Long> imagesId) {

        Product product = productRepo.findById(productId).orElseThrow();
        if (imagesId.size() >= product.getImages().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You most delete less images than what exist");
        }
        Set<Media> imagesToDelete = mediaService.getMediaByIds(imagesId);
        if (imagesToDelete.isEmpty() || imagesToDelete.size() != imagesId.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Images must exist");
        }
        Set<Media> imagesToKeep = product.getImages();
        imagesToDelete.forEach(imagesToKeep::remove);
        product.setImages(imagesToKeep);
        productRepo.save(product);

        mediaService.deleteImages(imagesToDelete);

        return "Operation successful";
    }

    public String create(ProductDto productDto) {

        Set<Media> images = mediaService.uploadFiles(productDto.getImages());
        Category category = categoryService.getCategoryById(productDto.getCategoryId());
        if (category == null)
            throw new BadRequestException(" La categorie indexée n'existe pas!");

        Product product = Product.builder()
                .category(category)
                .name(productDto.getName())
                .description(productDto.getDescription())
                .caracteristics(productDto.getCaracteristics())
                .brand(productDto.getBrand())
                .pricingModel(productDto.getPricingModel())
                .price(productDto.getPrice())
                .status(productDto.getStatus())
                .images(images)
                .build();

        productRepo.save(product);

        return "Operation successful";

    }

    public Set<Product> getProducts() {
        return (Set<Product>) productRepo.findAll();
    }

    public Set<Product> findByText(String text) {
        return productRepo.findByText(text);
    }

    public Product getProduct(long productId) {
        return productRepo.findById(productId).orElseThrow();
    }

    public String udpate(ProductDto productdto) {
        Product product = productRepo.findById(productdto.getId()).orElseThrow();

        // On update uniquement les champs ayant change

        Product updatedProduct = Product.builder()
                .id(product.getId())
                .name(Optional.ofNullable(productdto.getName()).orElse(product.getName()))
                .description(Optional.ofNullable(productdto.getDescription()).orElse(product.getDescription()))
                .caracteristics(Optional.ofNullable(productdto.getCaracteristics()).orElse(product.getCaracteristics()))
                .brand(Optional.ofNullable(productdto.getBrand()).orElse(product.getBrand()))
                .pricingModel(Optional.ofNullable(productdto.getPricingModel()).orElse(product.getPricingModel()))
                .status(Optional.ofNullable(productdto.getStatus()).orElse(product.getStatus()))
                .category(productdto.getCategoryId() != product.getCategory().getId() ?
                        categoryService.getCategoryById(productdto.getCategoryId()) :
                        product.getCategory())
                .price(Optional.of(productdto.getPrice()).orElse(product.getPrice()))
                .build();

        productRepo.save(updatedProduct);

        return "Operation successful";
    }

    public String deleteProduct(long id) {
        productRepo.deleteById(id);

        return "Operation successful";
    }

}
