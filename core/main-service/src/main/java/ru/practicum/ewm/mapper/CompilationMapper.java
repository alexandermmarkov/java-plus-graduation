package ru.practicum.ewm.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.core.config.CommonMapperConfiguration;
import ru.practicum.ewm.dto.compilation.CompilationFullDto;
import ru.practicum.ewm.dto.compilation.CompilationUpdateDto;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;

import java.util.Set;

@Mapper(config = CommonMapperConfiguration.class, uses = {EventMapperDep.class}) //проверить
public interface CompilationMapper {

    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "pinned", source = "dto.pinned")
    @Mapping(target = "events", source = "events")
    Compilation toEntity(CompilationUpdateDto dto, Set<Event> events);

    Compilation toEntity(CompilationFullDto dto);

    CompilationFullDto toFullDto(Compilation entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    @Mapping(target = "title", source = "dto.title")
    @Mapping(target = "pinned", source = "dto.pinned")
    Compilation toEntityGeneral(@MappingTarget Compilation entity, CompilationUpdateDto dto, Set<Event> events);
}

