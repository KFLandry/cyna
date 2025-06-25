package com.cyna.products.dto;

import com.cyna.products.models.PricingModel;
import com.cyna.products.models.ProductStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
@Builder
public class ProductDto {
    private Long id;

    private String priceId;

    private String name;

    private String brand;

    private String description;

    private String caracteristics;

    private PricingModel pricingModel;

    private Long amount;

    private Long categoryId;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"))
    private Set<MultipartFile> images;

    private ProductStatus status = ProductStatus.AVAILABLE;
    private Boolean active = true;
    private Boolean promo = false;
}
