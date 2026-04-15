package ru.practicum.ewm.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.core.config.CommonMapperConfiguration;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.Request;

@Mapper(config = CommonMapperConfiguration.class)
public interface RequestMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "event", source = "entity.event.id")
    @Mapping(target = "requester", source = "entity.requester.id")
    @Mapping(target = "status", source = "entity.status")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "id", source = "entity.id")
    ParticipationRequestDto toDto(Request entity);
}
