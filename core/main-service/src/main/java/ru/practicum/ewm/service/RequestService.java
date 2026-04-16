package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.constant.EventState;
import ru.practicum.ewm.constant.RequestStatus;
import ru.practicum.ewm.core.exception.ConditionsException;
import ru.practicum.ewm.core.exception.ConflictException;
import ru.practicum.ewm.core.exception.NotFoundException;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    private final RequestRepository repository;
    private final RequestMapper mapper;


    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) throws ConditionsException, ConflictException {
        if (repository.findByEventIdAndRequesterId(eventId, userId).isPresent()) {
            throw new ConflictException("Запрос уже существует");
        }

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие с id=" + eventId + " не найдено"));

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Нельзя подать заявку на своё мероприятие");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Подать заявку можно только на опубликованные мероприятия");
        }

        Long limit = event.getParticipantLimit();
        if (limit != null && limit > 0) {
            long confirmedCount = repository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (confirmedCount >= limit) {
                throw new ConflictException("Достигнут лимит участников");
            }
        }

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .build();

        Boolean moderation = event.getRequestModeration();
        if (!moderation || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
//        if (moderation != null && !moderation) {
//            request.setStatus(RequestStatus.CONFIRMED);
//        }
        request = repository.save(request);
        log.info("Создан запрос, id = {}", request.getId());
        return mapper.toDto(request);
    }


    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return repository.findByRequesterId(userId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) throws ConditionsException {
        Request request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка не найдена"));
        if (!Objects.equals(request.getRequester().getId(), userId)) {
            throw new ConditionsException("Только владелец может отменить заявку");
        }
        request.setStatus(RequestStatus.CANCELED);
        request = repository.save(request);
        log.info("Отменен запрос id = {}", requestId);
        return mapper.toDto(repository.save(request));
    }


    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsForEventOwner(Long ownerId, Long eventId) throws ConditionsException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие с id=" + eventId + " не найдено"));
        if (!Objects.equals(event.getInitiator().getId(), ownerId)) {
            throw new ConditionsException("Только владелец мероприятия может просматривать запросы на это мероприятие");
        }
        return repository.findByEventId(eventId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(
            Long ownerId,
            Long eventId,
            EventRequestStatusUpdateRequest updateDto) throws ConditionsException, ConflictException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Мероприятие с id=" + eventId + " не найдено"));

        if (!Objects.equals(event.getInitiator().getId(), ownerId)) {
            throw new ConditionsException("Только владелец мероприятия может изменять статус запроса");
        }

        if (updateDto.getRequestIds() == null || updateDto.getRequestIds().isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }
        if (updateDto.getStatus() == null) {
            throw new ConditionsException("Не указан статус");
        }

        if (updateDto.getStatus() == RequestStatus.CONFIRMED &&
                (!event.getRequestModeration() || event.getParticipantLimit() == 0)) {
            throw new ConditionsException("Подтверждение заявок не требуется");
        }

        Long freeLimit = event.getParticipantLimit();
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

        if (freeLimit != null && freeLimit == 0 && event.getParticipantLimit() > 0) {
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
}
