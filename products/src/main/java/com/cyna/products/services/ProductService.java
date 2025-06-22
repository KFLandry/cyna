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
import java.util.*;
import java.util.stream.Collectors;

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
    private final ProductMapper productMapper;

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

    public Pagination findByText(String text, long page, long size, Boolean active) {
        List<ProductGetDto> products = productRepo.findByText(text).stream()
                .skip(page==1 ? 0 : page * size)
                .limit(size)
                .map(productMapper::toDto)
                .filter(p -> (active == null|| p.isActive() == active))
                .toList();
        return Pagination.builder()
                .size(productRepo.countByText(text))
                .products(products)
                .build();
    }

    public ProductGetDto getProduct(long productId) {
        return productMapper.toDto(productRepo.findById(productId).orElseThrow());
    }

    public String udpate(ProductDto productdto) {
        Product product = productRepo.findById(productdto.getId()).orElseThrow();

        Set<Media> images;
        if (productdto.getImages() == null || productdto.getImages().isEmpty()) {
            // On garde les images existantes si aucune nouvelle image n'est envoyée
            images = product.getImages();
        } else {
            // On ajoute les nouvelles images aux existantes
            images = new HashSet<>(product.getImages());
            images.addAll(mediaService.uploadFiles(productdto.getImages()));
        }

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
                .images(images) // <-- Correction ici
                .updatedAt(LocalDateTime.now())
                .createdAt(product.getCreatedAt())
                .build();

        productRepo.save(updatedProduct);

        return "Operation successful";
    }

    public String deleteProduct(long id) {
        this.udpate(ProductDto.builder()
                .id(id)
                .active(false) // On désactive le produit
                .build());
        // On supprime le produit de Stripe
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
                    .pricingModel(product.getPricingModel())
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
            log.error("[StripeService][updateCustomerId] Error while deleting Stripe price", e);

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

    public List<ProductGetDto> getTopProducts(int top, Boolean promo, Boolean active) {
        List<TopProduct> topProducts = Arrays.asList(restClientBuilder.build()
            .get()
            .uri(this.getServiceURI(SUBCRIPTIONS_ID) + "/api/v1/subscriptions/top-products?top=" + top)
            .retrieve()
            .body(TopProduct[].class));

        // Map productId to sales number for quick lookup
        Map<Long, Long> salesMap = topProducts.stream()
            .collect(Collectors.toMap(TopProduct::getProduct_id, TopProduct::getSales_number));

        List<Product> products = topProducts.stream()
            .map(p -> productRepo.findById(p.getProduct_id()).orElse(null))
            .filter(Objects::nonNull)
            .filter(p -> (active == null || p.isActive() == active) && (promo == null || p.isPromo() == promo))
            .toList();

        return products.stream()
            .map(productMapper::toDto)
            .peek(dto -> dto.setSalesNumber(salesMap.getOrDefault(dto.getId(), 0L)))
            .toList();
    }

    public Pagination getProductsByCategories(Set<Long> categoryIds, boolean promoOnly, int page, int size, String sort) {
        if (page < 0 || size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page and size must be greater than 0");
        }
        int offset = page * size;

        List<Product> products = productRepo.findProductByCategoryAndPromo(categoryIds, promoOnly, size, offset)
                .stream()
                .sorted((p1, p2) -> {
                    switch (sort) {
                        case "asc" -> {
                            return p1.getName().compareToIgnoreCase(p2.getName());
                        }
                        case "desc" -> {
                            return p2.getName().compareToIgnoreCase(p1.getName());
                        }
                        case "priceAsc" -> {
                            return Double.compare(p1.getAmount(), p2.getAmount());
                        }
                        case "priceDesc" -> {
                            return Double.compare(p2.getAmount(), p1.getAmount());
                        }
                        case "createdAtAsc" -> {
                            return p1.getCreatedAt().compareTo(p2.getCreatedAt());
                        }
                        case "createdAtDesc" -> {
                            return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                        }
                        default -> {
                            return 0; // No sorting applied
                        }
                    }
                })
                .toList();

        return Pagination.builder()
                .size(productRepo.countProductByCategoryAndPromo(categoryIds, promoOnly ))
                .products(products.stream().map(productMapper::toDto).toList())
                .build();
    }
}
