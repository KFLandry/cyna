package com.cyna.products.dto;


import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@Data
public class CarouselDto {

    private Long id;

    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Text is required")
    private String text;

    @ArraySchema(schema = @Schema(type = "string", format = "binary"))
    private Set<MultipartFile> images = new HashSet<>(); // Initialisation avec un HashSet vide

    private long productId;

    private long categoryId;

    private boolean replaceImage = false;

    public boolean isReplaceImage() {
        return replaceImage;
    }

    public void setReplaceImage(boolean replaceImage) {
        this.replaceImage = replaceImage;
    }
}
