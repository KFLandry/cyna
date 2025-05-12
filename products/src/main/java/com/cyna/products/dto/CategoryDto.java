package com.cyna.products.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Data
public class CategoryDto {
    private long id;

    private String name;

    private String description;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"))
    private Set<MultipartFile> images;

    private List<ProductDto> productList;

}
