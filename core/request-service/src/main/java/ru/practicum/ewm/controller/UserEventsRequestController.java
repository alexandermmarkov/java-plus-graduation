package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.service.RequestFacadeService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
@Slf4j
public class UserEventsRequestController {

    private final RequestFacadeService requestFacadeService;

    @GetMapping
    public List<ParticipationRequestDto> findForEvent(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long eventId) throws ConditionsException {
        log.info("Получить заявки на мероприятие {}, владелец {}", eventId, userId);
        return requestFacadeService.getRequestsForEventOwner(userId, eventId);
    }

    @PatchMapping
    public EventRequestStatusUpdateResult update(
            @Positive @PathVariable Long userId,
            @Positive @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest dto) throws ConditionsException, ConflictException {
        log.info("Изменить статус запроса на мероприятие, userId = {}, eventId = {}, updateDto = {}", userId, eventId, dto);
        return requestFacadeService.updateRequestStatus(userId, eventId, dto);
    }
}
