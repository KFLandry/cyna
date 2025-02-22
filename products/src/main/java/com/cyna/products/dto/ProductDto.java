package com.cyna.products.dto;

import com.cyna.products.models.PricingModel;
import com.cyna.products.models.ProductStatus;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class ProductDto {
    private long id;

    private String name;

    private String brand;

    private String description;

    private String caracteristics;

    private PricingModel pricingModel;

    private int price;

    private long categoryId;

    private Set<MultipartFile> images;

    private ProductStatus status = ProductStatus.AVAILABLE;
}
