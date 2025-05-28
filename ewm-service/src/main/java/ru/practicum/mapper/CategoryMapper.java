package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.CategoryDto;
import ru.practicum.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

}
