package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.core.exception.ConditionsException;
import ru.practicum.ewm.core.exception.ConflictException;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventNewDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.EventUpdateDto;
import ru.practicum.ewm.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class UserEventsController {
    private final EventService service;

    @GetMapping
    public List<EventShortDto> find(
            @Positive @PathVariable Long userId,
            @PageableDefault(page = 0, size = 10, sort = "createdOn", direction = Sort.Direction.DESC) Pageable pageable
    ) throws ConditionsException {
        return service.findByUserId(userId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(
            @Positive @PathVariable Long userId,
            @RequestBody EventNewDto dto) throws ConditionsException {
        return service.create(dto, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findByUserIdAndEventId(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long eventId) throws ConditionsException {
        return service.findByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long eventId,
            @RequestBody EventUpdateDto dto) throws ConditionsException, ConflictException {
        return service.update(userId, eventId, dto);
    }


}
