package ru.practicum.ewm.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.constant.EventState;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.comment.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {

    private Long id;

    private String annotation;

    private CategoryDto category;

    private Long confirmedRequests;

    private LocalDateTime createdOn;

    private String description;

    private LocalDateTime eventDate;

    private UserDto initiator;

    private EventLocationDto location;

    private Boolean paid;

    private Long participantLimit;

    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    private EventState state;

    private String title;

    private Long views;

    private List<CommentDto> comments;
}
