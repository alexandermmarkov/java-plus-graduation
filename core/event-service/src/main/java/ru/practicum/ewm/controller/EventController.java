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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
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
    public EventFullDto findById(@PathVariable @Positive Long id, HttpServletRequest request) {
        log.info("Получить публичное мероприятие по id {}", id);
        return eventFacadeService.findPublicEventById(id, request);
    }

    @GetMapping("/event/{id}")
    public EventFullDto getById(@PathVariable @Positive Long id) {
        log.info("Получить мероприятие по id {}", id);
        return eventFacadeService.getEventById(id);
    }

}
