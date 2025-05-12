package com.cyna.products.dto;

import com.cyna.products.models.Media;
import com.cyna.products.models.PricingModel;
import com.cyna.products.models.ProductStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ProductGetDto {
    private long id;

    private String priceId;

    private String name;

    private String brand;

    private String description;

    private String caracteristics;

    private PricingModel pricingModel;

    private long amount;

    private long categoryId;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"))
    private Set<Media> images;

    private ProductStatus status = ProductStatus.AVAILABLE;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean active;

    private boolean promo;
}
