package com.cyna.products.services;

import com.cyna.products.dto.*;
import com.cyna.products.models.Category;
import com.cyna.products.models.Media;
import com.cyna.products.models.Product;
import com.cyna.products.repositories.ProductRepo;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private static final String SUBCRIPTIONS_ID = "subscriptions" ;
    private final ProductRepo productRepo;
    private final MediaService mediaService;
    private final CategoryService categoryService;
    private final DiscoveryClient discoveryClient;
    private final RestClient.Builder restClientBuilder;
    private final ProductGetMapper productMapper;

    public String addImages(long productId, Set<MultipartFile> images) {

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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "You most delete less images than what exist");
        }
        Set<Media> imagesToDelete = mediaService.getMediaByIds(imagesId);
        if (imagesToDelete.isEmpty() || imagesToDelete.size() != imagesId.size()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Images must exist");
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
                .amount(productDto.getAmount())
                .status(productDto.getStatus())
                .images(images)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(productDto.isActive())
                .promo(productDto.isPromo())
                .build();

        // On save pour avoir l'id du produit
        product = productRepo.save(product);

        // On crée le produit dans Stripe
        PriceDto priceDto = this.createStripePrice(product);

        // On ajoute le priceId de stripe au produit
        product.setPriceId(priceDto.getPriceId());
        productRepo.save(product);

        return "Operation successful";

    }

    public List<ProductGetDto> getProducts() {
        List<Product> products = (List<Product>) productRepo.findAll();
        return products.stream().map(productMapper::toDto).toList();
    }

    public List<Product> findByText(String text) {
        return productRepo.findByText(text);
    }

    public Product getProduct(long productId) {
        return productRepo.findById(productId).orElseThrow();
    }

    public String update(ProductDto productdto) {
        System.out.println(">>> Tentative de mise à jour : productdto = " + productdto);
        System.out.println(">>> ID reçu = " + productdto.getId());
        Product product = productRepo.findById(productdto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable avec l'ID : " + productdto.getId()));


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
                .amount(Optional.of(productdto.getAmount()).orElse(product.getAmount()))
                .active(productdto.isActive())
                .promo(productdto.isPromo())
                .updatedAt(LocalDateTime.now())
                .build();

        productRepo.save(updatedProduct);

        return "Operation successful";
    }

    public String deleteProduct(long id) {
        productRepo.deleteById(id);

        this.deleteStripePrice(id);

        return "Operation successful";
    }

    public PriceDto createStripePrice(Product product){
        try {
            PriceDto priceDto = PriceDto.builder()
                    .currency("eur")
                    .productId(product.getId())
                    .productName(product.getName())
                    .amount(product.getAmount())
                    .description(product.getDescription())
                    .build();

            PriceDto result = restClientBuilder.build()
                    .post()
                    .uri(this.getServiceURI(SUBCRIPTIONS_ID) + "/api/v1/subscriptions/create-price")
                    .body(priceDto)
                    .retrieve()
                    .body(PriceDto.class);

            log.debug("[ProductsService][CreateStripePrice] Create a stripe products. Result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("[StripeService][updateCustomerId] Error while creating Stripe price", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while creating Stripe price");
        }
    }

    public void deleteStripePrice(long productId){
        try {
            restClientBuilder.build()
                    .delete()
                    .uri(this.getServiceURI(SUBCRIPTIONS_ID) + "/api/v1/subscriptions/create-price/"+productId);

        } catch (Exception e) {
            log.error("[StripeService][updateCustomerId] Error while deleting Sripe price", e);

        }
    }

    public URI getServiceURI(String serviceId){
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances.isEmpty()) {
            log.error("No instances found for service: {}", serviceId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No instances found for service: " + serviceId);
        }

        ServiceInstance serviceInstance = instances.getFirst();
        log.debug("Calling auth service at: {}", serviceInstance.getUri());

        return serviceInstance.getUri();
    }

    public List<ProductGetDto> getTopProducts(int top, boolean active, boolean promo) {
        List<TopProduct> topProducts = Collections.singletonList(restClientBuilder.build()
                .get()
                .uri(this.getServiceURI(SUBCRIPTIONS_ID) + "/api/v1/subscriptions/top-products?top=" + top)
                .retrieve()
                .body(TopProduct.class));

        List<Product> products = topProducts.stream().map(p -> productRepo.findById(p.getProduct_id()).orElse(null)).toList();
        return products.stream().filter(p -> (p.isActive()==active && p.isPromo()==promo)).toList().stream().map(productMapper::toDto).toList();
    }
}
