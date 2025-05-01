package com.cyna.products.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MediaDto {
    private Long id;
    private String name;
    private String url;
    private LocalDateTime uploadDate;
}
