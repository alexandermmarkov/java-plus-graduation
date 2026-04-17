package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.model.Event;

import java.util.List;

@UtilityClass
public class EventMapperDep {
    private final CategoryMapper categoryMapper = new CategoryMapperImpl();
    private final LocationMapper locationMapper = new LocationMapperImpl();

    public EventFullDto eventToFullDto(Event event, Long confirmedRequests, Long views, List<CommentDto> comments, UserDto userDto) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(userDto)
                .location(locationMapper.toDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(views)
                .comments(comments)
                .build();
    }

    public EventShortDto eventToShortDto(Event event, Long confirmedRequests, Long views, UserDto userDto) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .initiator(userDto)
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

}
