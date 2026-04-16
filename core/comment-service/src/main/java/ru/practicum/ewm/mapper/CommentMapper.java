package ru.practicum.ewm.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.config.CommonMapperConfiguration;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.CommentUpdateDto;
import ru.practicum.ewm.model.Comment;

@Mapper(config = CommonMapperConfiguration.class)
public interface CommentMapper {

    @Mapping(target = "authorName", ignore = true)
    @Mapping(target = "event", source = "entity.eventId")
    CommentDto toDto(Comment entity);

    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updated", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "text", source = "dto.text")
    @Mapping(target = "deleted", source = "dto.deleted")
    Comment mapEntityFromDto(@MappingTarget Comment entity, CommentUpdateDto dto);

}
