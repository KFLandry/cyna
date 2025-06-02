package com.cyna.products.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Pagination {
    private long size;
    private List<ProductGetDto> products;
}
