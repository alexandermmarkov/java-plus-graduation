package ru.practicum.ewm.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.CollectorClient;
import ru.practicum.ewm.client.CommentClient;
import ru.practicum.ewm.client.RecommendationsClient;
import ru.practicum.ewm.client.RequestClient;
import ru.practicum.ewm.client.UserClient;
import ru.practicum.ewm.constant.EventState;
import ru.practicum.ewm.constant.RequestStatus;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventNewDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.EventUpdateDto;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConditionsException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.filter.EventsFilter;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.mapper.EventMapperDep;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventFacadeService {
    private final EventMapper mapper;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final CommentClient commentClient;

    private final EventService eventService;

    private final CollectorClient collectorClient;
    private final RecommendationsClient recommendationsClient;


    public EventFullDto create(EventNewDto dto, Long userId) throws ConditionsException {
        UserDto userDto = getUserOrThrow(userId);
        log.info("получен пользователь {}", userDto.getName());
        Event event = eventService.create(dto, userDto);
        EventFullDto dtoResult = mapper.toDto(event);
        return dtoResult.toBuilder()
                .initiator(userDto)
                .build();
    }

    public EventFullDto update(Long userId, Long eventId, EventUpdateDto dto) throws ConditionsException, ConflictException {
        UserDto userDto = getUserOrThrow(userId);
        Event event = eventService.update(userId, eventId, dto, userDto);
        Long calcConfirmedRequests = getConfirmedRequests(eventId);
        // получаем рейтинги
        Map<Long, Double> ratings = getRatings(List.of(eventId));
        EventFullDto dtoResult = mapper.toDto(event);
        return dtoResult.toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .rating(ratings.get(event.getId()))
                .comments(getComments(eventId))
                .initiator(userDto)
                .build();
    }

    public EventFullDto updateAdmin(Long eventId, EventUpdateDto dto) throws ConditionsException, ConflictException {
        Event event = eventService.updateAdmin(eventId, dto);

        Long userId = event.getInitiatorId();
        UserDto userDto = getUserOrThrow(userId);
        Long calcConfirmedRequests = getConfirmedRequests(eventId);
        // получаем рейтинги
        Map<Long, Double> ratings = getRatings(List.of(eventId));
        EventFullDto dtoResult = mapper.toDto(event);
        return dtoResult.toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .rating(ratings.get(event.getId()))
                .comments(getComments(eventId))
                .initiator(userDto)
                .build();
    }

    public EventFullDto findByUserIdAndEventId(Long userId, Long eventId) throws ConditionsException {
        UserDto userDto = getUserOrThrow(userId);
        log.info("Получено событие {} пользователя {}", eventId, userId);
        Long calcConfirmedRequests = getConfirmedRequests(eventId);
        Event event = eventService.getEvent(eventId, userId);
        // получаем рейтинги
        Map<Long, Double> ratings = getRatings(List.of(eventId));

        EventFullDto dtoResult = mapper.toDto(event);
        return dtoResult.toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .rating(ratings.get(event.getId()))
                .comments(getComments(eventId))
                .initiator(userDto)
                .build();
    }

    public EventFullDto findPublicEventById(Long eventId, Long userId) {
        Event event = eventService.getEvent(eventId, EventState.PUBLISHED);
        Long calcConfirmedRequests = getConfirmedRequests(eventId); // обращение к requestClient
        // получаем рейтинги
        Map<Long, Double> ratings = getRatings(List.of(eventId));
        UserDto userDto;
        try {
            userDto = getUserOrThrow(event.getInitiatorId());
        } catch (ConditionsException e) {
            throw new RuntimeException(e);
        }
        collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
        log.info("Получено публичное событие {}", eventId);
        EventFullDto dtoResult = mapper.toDto(event);
        return dtoResult.toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .rating(ratings.get(event.getId()))
                .comments(getComments(eventId))
                .initiator(userDto)
                .build();
    }

    public EventFullDto getEventById(Long eventId) {
        Event event = eventService.getEvent(eventId);
        List<Long> eventIds = List.of(eventId);
        Long calcConfirmedRequests = getConfirmedRequests(eventId);
        // получаем рейтинги
        Map<Long, Double> ratings = getRatings(List.of(eventId));

        UserDto userDto;
        try {
            userDto = getUserOrThrow(event.getInitiatorId());
        } catch (ConditionsException e) {
            throw new RuntimeException(e);
        }
        log.info("Получено событие {}", eventId);
        EventFullDto dtoResult = mapper.toDto(event);
        return dtoResult.toBuilder()
                .confirmedRequests(calcConfirmedRequests)
                .rating(ratings.get(event.getId()))
                .comments(getComments(eventId))
                .initiator(userDto)
                .build();
    }

    public List<EventShortDto> findByUserId(Long userId, Pageable pageable) throws ConditionsException {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        UserDto userDto = getUserOrThrow(userId);
        List<Event> events = eventService.findByUserId(userId, pageable);
        List<Long> eventIds = events.stream()
                .map(Event::getId).distinct().toList();
        // получаем рейтинги
        Map<Long, Double> ratings = getRatings(eventIds);

        return events.stream()
                .map(event -> EventMapperDep.eventToShortDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        ratings.get(event.getId()),
                        userDto
                ))
                .toList();
    }

    public void addLikeToEvent(Long userId, Long eventId) throws ConditionsException {
        if (!userIsExist(userId)) {
            throw new NotFoundException("Пользователь %s не найден".formatted(userId));
        }
        eventService.getEvent(eventId);
        if (requestClient.checkUserParticipation(userId, eventId)) {
            collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
        } else {
            throw new ConditionsException("Пользователь %s не участвовал в мероприятии %s".formatted(userId, eventId));
        }
    }

    public List<EventShortDto> getRecommendationsForUser(Long userId, Integer maxResults) throws ConditionsException {
        UserDto userDto = getUserOrThrow(userId);
        List<RecommendedEventProto> protoList = recommendationsClient
                .getRecommendationsForUser(userId, maxResults)
                .toList();
        if (protoList.isEmpty()) {
            return List.of();
        }
        //получить список eventIds и достать все эти события
        List<Long> eventIds = protoList.stream().map(RecommendedEventProto::getEventId).toList();
        List<Event> eventList = eventService.findEventsByEventIds(eventIds);
        Map<Long, Double> ratings = getRatings(eventIds);

        return eventList.stream()
                .map(event -> EventMapperDep.eventToShortDto(
                                event,
                                getConfirmedRequests(event.getId()),
                                ratings.get(event.getId()),
                                userDto != null ? userDto : fallbackUser()
                        )
                )
                .toList();

    }

    public List<EventShortDto> findPublicEventsWithFilter(
            EventsFilter filter,
            Pageable pageable,
            HttpServletRequest request) {

        // Получаем события из БД — через транзакционный сервис
        Page<Event> eventsPage = eventService.findEventsWithFilter(filter, pageable, false);
        if (eventsPage.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> eventIds = eventsPage.stream()
                .map(Event::getId).distinct().toList();
        // получаем рейтинги
        Map<Long, Double> ratings = getRatings(eventIds);

        // Собираем все ID инициаторов
        List<Long> initiatorIds = eventsPage.stream()
                .map(Event::getInitiatorId).distinct().toList();

        // Один вызов к user-service за всеми пользователями
        List<UserDto> users = userClient.getUsers(initiatorIds);
        Map<Long, UserDto> userMap = users.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        // Преобразуем в DTO
        return eventsPage.stream()
                .map(event -> {
                    Long confirmedRequests = getConfirmedRequests(event.getId());
                    UserDto userDto = userMap.get(event.getInitiatorId());
                    Double rating = ratings.get(event.getId());
                    return EventMapperDep.eventToShortDto(
                            event,
                            confirmedRequests,
                            rating,
                            userDto != null ? userDto : fallbackUser()
                    );
                })
                .toList();
    }

    public List<EventFullDto> findAdminEventsWithFilter(EventsFilter filter, Pageable pageable) {
        // Получаем события из БД — через транзакционный сервис
        Page<Event> eventsPage = eventService.findEventsWithFilter(filter, pageable, true);
        if (eventsPage.isEmpty()) {
            return Collections.emptyList();
        }

        // Собираем все ID для пакетных вызовов
        List<Long> eventIds = eventsPage.stream()
                .map(Event::getId).toList();
        List<Long> initiatorIds = eventsPage.stream()
                .map(Event::getInitiatorId).distinct().toList();

        // Пакетные вызовы к другим сервисам
        // Один вызов к user-service за всеми пользователями
        List<UserDto> users = userClient.getUsers(initiatorIds);
        Map<Long, UserDto> userMap = users.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        // получаем рейтинги
        Map<Long, Double> ratings = getRatings(eventIds);

        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsForEvents(eventIds); // обращение к requestClient
        Map<Long, List<CommentDto>> commentsMap = getCommentsForEvents(eventIds); // обращение к commentClient

        // Преобразуем в DTO
        return eventsPage.stream()
                .map(event -> {
                    UserDto userDto = userMap.getOrDefault(event.getInitiatorId(), fallbackUser());
                    Long confirmedRequests = confirmedRequestsMap.getOrDefault(event.getId(), 0L);
                    List<CommentDto> comments = commentsMap.getOrDefault(event.getId(), Collections.emptyList());
                    Double rating = ratings.get(event.getId());
                    return EventMapperDep.eventToFullDto(
                            event,
                            confirmedRequests,
                            rating,
                            comments,
                            userDto
                    );
                })
                .toList();
    }

    private UserDto fallbackUser() {
        return new UserDto(-1L, "Пользователь недоступен", null);
    }

    private Map<Long, Long> getConfirmedRequestsForEvents(List<Long> eventIds) {
        try {
            return requestClient.getConfirmedRequestsForEvents(eventIds);
        } catch (Exception e) {
            log.warn("Не удалось получить подтверждённые заявки для событий {}: {}", eventIds, e.getMessage());
            return eventIds.stream().collect(Collectors.toMap(id -> id, id -> 0L));
        }
    }

    private UserDto getUserOrThrow(Long userId) throws ConditionsException {
        try {
            return userClient.getUserById(userId);
        } catch (FeignException.NotFound ex) {
            throw new ConditionsException("Пользователь с id=" + userId + " не найден");
        }
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestClient.getCountRequestsByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private List<CommentDto> getComments(Long eventId) {
        return commentClient.findAllCommentsForEvent(eventId);
    }

    private Map<Long, List<CommentDto>> getCommentsForEvents(List<Long> eventIds) {
        try {
            return commentClient.getCommentsForEvents(eventIds);
        } catch (Exception e) {
            log.warn("Не удалось получить комментарии для событий {}: {}", eventIds, e.getMessage());
            return eventIds.stream().collect(Collectors.toMap(id -> id, id -> Collections.emptyList()));
        }
    }

    public boolean userIsExist(Long userId) {
        try {
            userClient.getUserById(userId);
            return true;
        } catch (FeignException.NotFound ex) {
            return false;
        }
    }

    private Map<Long, Double> getRatings(List<Long> eventIds) {
        return recommendationsClient.getInteractionsCount(eventIds)
                .collect(Collectors.toMap(
                        RecommendedEventProto::getEventId,
                        RecommendedEventProto::getScore,
                        (a, b) -> a
                ));
    }
}
