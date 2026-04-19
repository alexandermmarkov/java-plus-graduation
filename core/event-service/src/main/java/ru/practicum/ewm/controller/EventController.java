package ru.practicum.ewm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.filter.EventsFilter;
import ru.practicum.ewm.service.EventFacadeService;

import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventController {
    private final EventFacadeService eventFacadeService;

    @GetMapping
    public List<EventShortDto> find(
            @Valid @ParameterObject EventsFilter filter,
            @PageableDefault(page = 0, size = 10) Pageable pageable, HttpServletRequest request) {
        log.info("Получить список публичных мероприятий по фильтру {}", filter);
        return eventFacadeService.findPublicEventsWithFilter(filter, pageable, request);
    }

    @GetMapping("/{id}")
    public EventFullDto findById(@PathVariable @Positive Long id, @RequestHeader("X-EWM-USER-ID") Long userId) {
        log.info("Получить публичное мероприятие по id {}", id);
        return eventFacadeService.findPublicEventById(id, userId);
    }

    @GetMapping("/event/{id}")
    public EventFullDto getById(@PathVariable @Positive Long id) {
        log.info("Получить мероприятие по id {}", id);
        return eventFacadeService.getEventById(id);
    }

    @GetMapping("/recommendations")
    public List<EventShortDto> getRecommendationsForUser(@RequestHeader("X-EWM-USER-ID") Long userId, Integer maxResults)
            throws ConditionsException {
        log.info("Получить рекомендации для пользователя {}, не более {}", userId, maxResults);
        return eventFacadeService.getRecommendationsForUser(userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void addLikeToEvent(@PathVariable @Positive Long eventId,
                               @RequestHeader(value = "X-EWM-USER-ID") Long userId) throws ConditionsException {
        log.info("Пользователь {} ставит лайк мероприятию {}", userId, eventId);
        eventFacadeService.addLikeToEvent(userId, eventId);
    }

}
