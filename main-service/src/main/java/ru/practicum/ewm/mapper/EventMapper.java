package ru.practicum.ewm.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.constant.EventState;
import ru.practicum.ewm.core.config.CommonMapperConfiguration;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventNewDto;
import ru.practicum.ewm.dto.event.EventUpdateDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;

@Mapper(config = CommonMapperConfiguration.class)
public interface EventMapper {
    //    @Mapping(target = "events", ignore = true)
    EventFullDto toDto(Event source);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "annotation", source = "dto.annotation")
    @Mapping(target = "category", source = "category")
//    @Mapping(target = "createdOn", source = "dto.createdOn")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "eventDate", source = "dto.eventDate")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "paid", source = "dto.paid")
    @Mapping(target = "participantLimit", source = "dto.participantLimit")
    @Mapping(target = "publishedOn", source = "dto.publishedOn")
    @Mapping(target = "requestModeration", source = "dto.requestModeration")
    @Mapping(target = "state", source = "dto.state")
    @Mapping(target = "title", source = "dto.title")
    Event toEntityWithNewDto(EventNewDto dto, User initiator, Category category, Location location);

    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "annotation", source = "dto.annotation")
    @Mapping(target = "category", source = "category")
//    @Mapping(target = "createdOn", source = "dto.createdOn")
    @Mapping(target = "description", source = "dto.description")
//    @Mapping(target = "initiator", source = "initiator")
    @Mapping(target = "eventDate", source = "dto.eventDate")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "paid", source = "dto.paid")
    @Mapping(target = "participantLimit", source = "dto.participantLimit")
//    @Mapping(target = "publishedOn", source = "dto.publishedOn")
    @Mapping(target = "requestModeration", source = "dto.requestModeration")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "title", source = "dto.title")
    Event toEntityWithUpdateDto(@MappingTarget Event event, EventUpdateDto dto, Category category, Location location, EventState state);
}

