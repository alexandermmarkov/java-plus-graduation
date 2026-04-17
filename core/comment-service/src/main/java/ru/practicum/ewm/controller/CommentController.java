package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.core.interfaceValidation.CreateValidation;
import ru.practicum.ewm.core.interfaceValidation.UpdateValidation;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.CommentUpdateDto;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.service.CommentFacadeService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = "/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentFacadeService commentFacadeService;

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto findById(@PathVariable Long id) {
        return commentFacadeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(CreateValidation.class)
    public CommentDto create(
            @Valid @RequestBody CommentUpdateDto dto,
            @RequestHeader("X-User-Id") Long userId) {
        return commentFacadeService.create(dto, userId);
    }

    @PatchMapping(path = "/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    @Validated(UpdateValidation.class)
    public CommentDto update(
            @Valid @RequestBody CommentUpdateDto dto,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long commentId) throws ConditionsException {
        return commentFacadeService.update(dto, commentId, userId);
    }

    @DeleteMapping(path = "/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long commentId,
            @RequestHeader("X-User-Id") Long userId) throws ConditionsException {
        commentFacadeService.delete(commentId, userId);
    }

    @GetMapping(path = "/event/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> findAllCommentsForEvent(@PathVariable("eventId") Long eventId) {
        log.info("Найти все комментарии к событию {} (из event-service)", eventId);
        return commentFacadeService.findAllCommentsForEvent(eventId);
    }

    @GetMapping("/events/batch")
    @ResponseStatus(HttpStatus.OK)
    public Map<Long, List<CommentDto>> getCommentsForEvents(@RequestParam List<Long> eventIds) {
        log.info("Найти все комментарии к списку событий {} (из event-service)", eventIds);
        return commentFacadeService.getCommentsForEvents(eventIds);
    }

}
