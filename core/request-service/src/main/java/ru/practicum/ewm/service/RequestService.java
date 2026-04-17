package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.constant.RequestStatus;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository repository;
    private final RequestMapper mapper;

    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId, UserDto requester, EventFullDto eventFullDto)
            throws ConflictException {
        if (repository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Запрос уже существует");
        }

        Long limit = eventFullDto.getParticipantLimit();
        if (limit != null && limit > 0) {
            long confirmedCount = repository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedCount >= limit) {
                throw new ConflictException("Достигнут лимит участников");
            }
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .eventId(eventFullDto.getId())
                .requesterId(requester.getId())
                .status(RequestStatus.PENDING)
                .build();

        Boolean moderation = eventFullDto.getRequestModeration();
        if (!moderation || eventFullDto.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        request = repository.save(request);
        log.info("Создан запрос, id = {}", request.getId());
        return mapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        List<Request> requests = repository.findByRequesterId(userId);
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        return requests.stream()
                .map(mapper::toDto)
                .toList();
    }


    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) throws ConditionsException {
        Request request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));
        if (!Objects.equals(request.getRequesterId(), userId)) {
            throw new ConditionsException("Только владелец может отменить заявку");
        }
        request.setStatus(RequestStatus.CANCELED);
        request = repository.save(request);
        log.info("Отменен запрос id = {}", requestId);
        return mapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForEventOwner(Long eventId) throws ConditionsException {
        List<Request> requests = repository.findByEventId(eventId);
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        return requests.stream()
                .map(mapper::toDto)
                .toList();
    }


    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(
            Long ownerId,
            Long eventId,
            EventRequestStatusUpdateRequest updateDto,
            EventFullDto eventFullDto) throws ConditionsException, ConflictException {

        Long freeLimit = eventFullDto.getParticipantLimit();
        if (freeLimit != null && freeLimit > 0) {
            freeLimit = freeLimit - repository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (freeLimit <= 0) {
                throw new ConflictException("Лимит по заявкам на данное событие уже достигнут");
            }
        }

        List<Request> requests =
                repository.findAllByIdInAndStatus(updateDto.getRequestIds(), RequestStatus.PENDING);
        Map<Long, Request> map = requests.stream()
                .collect(Collectors.toMap(Request::getId, r -> r));

        List<Long> notFound = updateDto.getRequestIds().stream()
                .filter(id -> !map.containsKey(id))
                .toList();

        if (!notFound.isEmpty()) {
            throw new ConflictException("Не найдены заявки: " + notFound);
        }

        List<Request> toUpdate = new ArrayList<>();
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Long id : updateDto.getRequestIds()) {
            Request r = map.get(id);

            if (updateDto.getStatus() == RequestStatus.CONFIRMED) {
                if (freeLimit != null && freeLimit <= 0) {
                    r.setStatus(RequestStatus.REJECTED);
                    rejected.add(mapper.toDto(r));
                    log.info("Заявка {} будет отклонена", r.getId());
                } else {
                    if (freeLimit != null) {
                        freeLimit--;
                    }
                    r.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(mapper.toDto(r));
                    log.info("Заявка {} будет подтверждена", r.getId());
                }
            } else if (updateDto.getStatus() == RequestStatus.REJECTED) {
                r.setStatus(RequestStatus.REJECTED);
                rejected.add(mapper.toDto(r));
            } else {
                throw new ConflictException("Доступны только статусы CONFIRMED или REJECTED");
            }
            toUpdate.add(r);
        }

        if (!toUpdate.isEmpty()) {
            repository.saveAll(toUpdate);
            log.info("Список заявок обновлен");
        }

        if (freeLimit != null && freeLimit == 0 && eventFullDto.getParticipantLimit() > 0) {
            List<Request> pendingRequests =
                    repository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
            if (!pendingRequests.isEmpty()) {
                pendingRequests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
                List<Request> rejectedRequests = repository.saveAll(pendingRequests);
                rejected.addAll(rejectedRequests.stream().map(mapper::toDto).toList());
                log.info("Был достигнут лимит заявок, все оставшиеся PENDING, переведены в REJECTED");
            }
        }

        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    @Transactional(readOnly = true)
    public Long getCountRequestsByEventIdAndStatus(Long eventId, RequestStatus status) {
        return repository.countByEventIdAndStatus(eventId, status);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Object[]> results = repository.countConfirmedRequestsByEventIds(eventIds);

        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0], // eventId
                        row -> (Long) row[1] // count
                ));
    }

}
