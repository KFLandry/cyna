package com.cyna.products.dto;

import com.cyna.products.models.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryGetDto toDto(Category category);
}
