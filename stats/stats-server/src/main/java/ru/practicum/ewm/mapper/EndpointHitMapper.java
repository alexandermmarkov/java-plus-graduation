package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.EndpointHitDto;
import ru.practicum.ewm.core.config.CommonMapperConfiguration;
import ru.practicum.ewm.model.EndpointHit;

@Mapper(config = CommonMapperConfiguration.class)
public interface EndpointHitMapper {

    EndpointHitDto toDto(EndpointHit entity);

    @Mapping(target = "created", source = "dto.timestamp")
    EndpointHit toEntity(EndpointHitDto dto);
}
