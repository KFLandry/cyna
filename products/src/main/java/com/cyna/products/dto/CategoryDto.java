package com.cyna.products.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;

    private String name;

    private String description;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"))
    private Set<MultipartFile> images;

    private Set<Long> imagesToDelete = new HashSet<>();

    private List<ProductDto> productList;
}
