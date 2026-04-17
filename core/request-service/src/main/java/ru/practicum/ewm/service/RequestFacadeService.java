package ru.practicum.ewm.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.EventClient;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.constant.EventState;
import ru.practicum.ewm.constant.RequestStatus;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestFacadeService {

    private final UserClient userClient;
    private final EventClient eventClient;
    private final RequestService requestService;

    public ParticipationRequestDto create(Long userId, Long eventId) throws ConditionsException, ConflictException {
        log.info("Создать запрос, userId = {}, eventId = {} ", userId, eventId);
        UserDto requester = getUserOrThrowCondEx(userId);
        EventFullDto eventFullDto = getEventOrThrow(eventId);
        if (Objects.equals(eventFullDto.getInitiator().getId(), userId)) {
            throw new ConflictException("Нельзя подать заявку на своё мероприятие");
        }
        if (eventFullDto.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Подать заявку можно только на опубликованные мероприятия");
        }
        return requestService.create(userId, eventId, requester, eventFullDto);
    }

    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        log.info("Получить запросы getRequestsByUser, userId = {}", userId);
        getUserOrThrowNotFound(userId);
        return requestService.getRequestsByUser(userId);
    }


    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) throws ConditionsException {
        log.info("Отменить запрос, userId = {}, requestId = {} ", userId, requestId);
        getUserOrThrowNotFound(userId);
        return requestService.cancelRequest(userId, requestId);
    }

    public List<ParticipationRequestDto> getRequestsForEventOwner(Long ownerId, Long eventId) throws ConditionsException {
        log.info("Получить запросы владельца, ownerId = {}, eventId = {}", ownerId, eventId);
        EventFullDto eventFullDto = getEventOrThrow(eventId);

        if (!Objects.equals(eventFullDto.getInitiator().getId(), ownerId)) {
            throw new ConditionsException("Только владелец мероприятия может просматривать запросы на это мероприятие");
        }
        return requestService.getRequestsForEventOwner(eventId);
    }

    public EventRequestStatusUpdateResult updateRequestStatus(
            Long ownerId,
            Long eventId,
            EventRequestStatusUpdateRequest updateDto) throws ConditionsException, ConflictException {
        log.info("Изменить мероприятие владельца, ownerId = {}, eventId = {}, updateDto = {}", ownerId, eventId, updateDto);
        EventFullDto eventFullDto = getEventOrThrow(eventId);
        if (!Objects.equals(eventFullDto.getInitiator().getId(), ownerId)) {
            throw new ConditionsException("Только владелец мероприятия может изменять статус запроса");
        }

        if (updateDto.getRequestIds() == null || updateDto.getRequestIds().isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }
        if (updateDto.getStatus() == null) {
            throw new ConditionsException("Не указан статус");
        }

        if (updateDto.getStatus() == RequestStatus.CONFIRMED &&
                (!eventFullDto.getRequestModeration() || eventFullDto.getParticipantLimit() == 0)) {
            throw new ConditionsException("Подтверждение заявок не требуется");
        }
        return requestService.updateRequestStatus(ownerId, eventId, updateDto, eventFullDto);
    }


    private UserDto getUserOrThrowCondEx(Long userId) throws ConditionsException {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound ex) {
            throw new ConditionsException("Пользователь с id=" + userId + " не найден");
        }
    }

    private UserDto getUserOrThrowNotFound(Long userId) throws NotFoundException {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private EventFullDto getEventOrThrow(Long eventId) {
        try {
            return eventClient.getEventById(eventId);
        } catch (FeignException.NotFound ex) {
            throw new NotFoundException("Мероприятие с id=" + eventId + " не найдено");
        }
    }
}
