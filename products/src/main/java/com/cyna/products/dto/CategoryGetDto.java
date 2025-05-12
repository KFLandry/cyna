package com.cyna.products.dto;

import com.cyna.products.models.Media;
import com.cyna.products.models.Product;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Data
public class CategoryGetDto {
    private long id;

    private String name;

    private String description;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"))
    private Set<Media> images;

    private List<Product> products;
}
