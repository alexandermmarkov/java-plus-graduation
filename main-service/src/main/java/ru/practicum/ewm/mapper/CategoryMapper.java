package ru.practicum.ewm.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.core.config.CommonMapperConfiguration;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.model.Category;

@Mapper(config = CommonMapperConfiguration.class)
public interface CategoryMapper {

    Category toEntity(CategoryDto dto);

    CategoryDto toDto(Category entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    CategoryDto updateDto(@MappingTarget CategoryDto targetDto, CategoryDto sourceDto);

}
