package ru.practicum.ewm.service;

import com.querydsl.core.BooleanBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.ewm.constant.EventState;
import ru.practicum.ewm.constant.EventStateAction;
import ru.practicum.ewm.dto.event.EventNewDto;
import ru.practicum.ewm.dto.event.EventUpdateDto;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.filter.EventsFilter;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class EventService {

    private final EventRepository repository;
    private final CategoryRepository categoryRepository;
    private final EventMapper mapper;

    private final LocationService locationService;

    @Transactional
    public Event create(@Valid EventNewDto dto, UserDto userDto) throws ConditionsException {
        Category category = getCategoryOrThrow(dto.getCategory());
        Location location = locationService.getOrCreateLocation(dto.getLocation());

        Event event = mapper.toEntityWithNewDto(dto, userDto.getId(), category, location);
        event = event.toBuilder().createdOn(LocalDateTime.now()).build();
        event = repository.save(event);
        log.info("Создано событие с id = {}", event.getId());
        return event;
    }

    @Transactional
    public Event update(Long userId, Long eventId, @Valid EventUpdateDto dto, UserDto userDto) throws ConditionsException, ConflictException {
        Event event = getEventOrThrow(eventId, userId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменять опубликованное событие");
        }

        Category category = (dto.getCategory() == null) ? null : getCategoryOrThrow(dto.getCategory());
        Location location = (dto.getLocation() == null) ? null : locationService.getOrCreateLocation(dto.getLocation());
        EventState state = dto.getStateAction() == null ?
                null :
                switch (dto.getStateAction()) {
                    case SEND_TO_REVIEW -> EventState.PENDING;
                    case CANCEL_REVIEW -> EventState.CANCELED;
                    case PUBLISH_EVENT, REJECT_EVENT -> null;
                };
        event = mapper.toEntityWithUpdateDto(event, dto, category, location, state);
        event = repository.save(event);
        log.info("Обновлено событие с id = {}", eventId);
        return event;
    }

    @Transactional
    public Event updateAdmin(Long eventId, @Valid EventUpdateDto dto) throws ConditionsException, ConflictException {
        Event event = getEventOrThrow(eventId);

        EventState currentState = event.getState();
        EventStateAction action = dto.getStateAction();
        LocalDateTime newDate = dto.getEventDate();

        if (action != null) {
            if (action == EventStateAction.PUBLISH_EVENT) {
                if (currentState != EventState.PENDING) {
                    throw new ConflictException("Можно публиковать только события в состоянии PENDING");
                }
            } else if (action == EventStateAction.REJECT_EVENT) {
                if (currentState == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить опубликованное событие");
                }
            }
        }
        if (dto.getEventDate() != null) {
            validateEventDate(newDate, action, currentState, event);
        }

        EventState state = action == null ?
                null :
                switch (action) {
                    case PUBLISH_EVENT -> EventState.PUBLISHED;
                    case REJECT_EVENT -> EventState.CANCELED;
                    case SEND_TO_REVIEW, CANCEL_REVIEW -> null;
                };

        LocalDateTime eventDate = dto.getEventDate() == null ? null : newDate;
        Category category = (dto.getCategory() == null) ? null : getCategoryOrThrow(dto.getCategory());
        Location location = (dto.getLocation() == null) ? null : locationService.getOrCreateLocation(dto.getLocation());
        dto = dto.toBuilder().eventDate(eventDate).build();

        event = mapper.toEntityWithUpdateDto(event, dto, category, location, state);
        event = repository.save(event);
        log.info("Администратор обновил событие с id = {}", eventId);
        return event;
    }

    @Transactional(readOnly = true)
    public List<Event> findByUserId(Long userId, Pageable pageable) throws ConditionsException {
        return repository.findAllByInitiatorId(userId, pageable)
                .stream()
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<Event> findEventsWithFilter(EventsFilter filter, Pageable pageable, boolean forAdmin) {
        BooleanBuilder predicate = EventPredicateBuilder.buildPredicate(filter, forAdmin);
        if (!forAdmin) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "eventDate")
            );
        }
        return repository.findAll(predicate, pageable);
    }

    @Transactional(readOnly = true)
    public List<Event> findEventsByEventIds(List<Long> eventIds) {
        return repository.findEventsByEventIds(eventIds);
    }


    private Category getCategoryOrThrow(Long catId) throws ConditionsException {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new ConditionsException("Категория с id=" + catId + " не найдена"));
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long eventId) {
        return getEventOrThrow(eventId);
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long eventId, Long userId) throws ConditionsException {
        return getEventOrThrow(eventId, userId);
    }

    @Transactional(readOnly = true)
    public Event getEvent(Long eventId, EventState state) {
        return getEventOrThrow(eventId, state);
    }

    private Event getEventOrThrow(Long eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
    }

    private Event getEventOrThrow(Long eventId, Long userId) throws ConditionsException {
        Event event = getEventOrThrow(eventId);
        if (!event.getInitiatorId().equals(userId)) {
            throw new ConditionsException("Пользователь " + userId + " не является инициатором события");
        }
        return event;
    }

    private Event getEventOrThrow(Long eventId, EventState state) {
        Event event = getEventOrThrow(eventId);
        if (event.getState() != state) {
            throw new NotFoundException("Событие не в состоянии " + state);
        }
        return event;
    }

    private void validateEventDate(LocalDateTime newDate, EventStateAction action, EventState currentState, Event event) throws ConditionsException {
        if (action == EventStateAction.PUBLISH_EVENT) {
            if (newDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConditionsException("Дата начала должна быть не ранее чем через 1 час при публикации");
            }
        } else if (currentState == EventState.PUBLISHED && event.getPublishedOn() != null) {
            if (newDate.isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConditionsException("Дата начала должна быть не ранее чем через 1 час после публикации");
            }
        }
    }
}