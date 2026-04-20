package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.EventSimilarity;
import ru.practicum.ewm.model.UserAction;
import ru.practicum.ewm.repository.EventSimilarityRepository;
import ru.practicum.ewm.repository.UserActionRepository;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationsService {
    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;

    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        Long userId = request.getUserId();
        int limit = request.getMaxResults();
        log.info("Получение рекомендаций для пользователя {}, limit: {}", userId, limit);

        // Выгрузить мероприятия, с которыми пользователь уже взаимодействовал.
        // При этом отсортировать их по дате взаимодействия от новых к старым и ограничить N взаимодействиями.
        // Уникальность гарантируется условием UNIQUE (user_id, event_id)
        List<Long> userEventIds = userActionRepository.findRecentEventIdsByUserId(userId, PageRequest.of(0, limit));
        if (userEventIds.isEmpty()) {
            log.debug("Нет событий у пользователя {}.", userId);
            return Stream.empty();
        }
        log.debug("Получили список событий пользователя {}: {}", userId, userEventIds);
        // но поиск в Set быстрей
        Set<Long> userEventSet = new HashSet<>(userEventIds);
        // все веса для пар с событиями из списка userEventIds, по убыванию веса
        List<EventSimilarity> similarities = eventSimilarityRepository.findSimilarEventsForListEventIds(userEventIds);
        log.debug("Получили similarities: {}", similarities);
        Map<Long, Double> recommendations = new HashMap<>();

        for (EventSimilarity sim : similarities) {
            Long otherEventId = userEventSet.contains(sim.getEvent1()) ? sim.getEvent2() : sim.getEvent1();
            // если  пользователь не взаимодействовал со вторым мероприятием из пары, берем в рекомендации
            if (!userEventSet.contains(otherEventId)) {
                recommendations.merge(otherEventId, sim.getSimilarity(), Math::max);
            }
        }

        log.debug("Собрали recommendations: {}", recommendations);

        return recommendations.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build());
    }

    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Long eventId = request.getEventId();
        Long userId = request.getUserId();
        int limit = request.getMaxResults();
        log.info("Поиск мероприятий, похожих на {}, для пользователя {}, limit: {}", eventId, userId, limit);
        // все события пользователя userId
        Set<Long> userEvents = userActionRepository.findEventIdsByUserId(userId);
        // все коэффициенты подобия для пары мероприятий, в которой одно (любое) соответствует указанному eventId
        List<EventSimilarity> similarities = eventSimilarityRepository.findAllByEventId(eventId);

        Map<Long, Double> recommendations = new HashMap<>();
        for (EventSimilarity sim : similarities) {
            // Убрать из выдачи те коэффициенты подобия, в которых пользователь взаимодействовал с обоими мероприятиями.
            // т.е. в recommendations берем те, где пользователь не взаимодействовал ни с одним
            if (!(userEvents.contains(sim.getEvent1()) && userEvents.contains(sim.getEvent2()))) {
                Long otherEventId = sim.getEvent1().equals(eventId) ? sim.getEvent2() : sim.getEvent1();
                recommendations.merge(otherEventId, sim.getSimilarity(), Math::max);
            }
        }

        return recommendations.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build());
    }

    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        log.info("Расчет сумм максимальных весов для списка мероприятий");
        List<Long> eventIds = request.getEventIdList();
        if (eventIds.isEmpty()) {
            log.debug("Задан пустой список мероприятий");
            return Stream.empty();
        }
        log.debug("Запросили список мероприятий eventIds {}", eventIds);
        List<UserAction> userActions = userActionRepository.findActionsForListEventIds(eventIds);
        if (userActions.isEmpty()) {
            log.debug("Не было взаимодействий со списком мероприятий {}", eventIds);
            return Stream.empty();
        }
        log.debug("Получили список userActions: {}", userActions);

        Map<Long, Double> sums = userActions.stream()
                .collect(Collectors.groupingBy(
                        UserAction::getEventId,
                        Collectors.summingDouble(UserAction::getRating)
                ));
        log.debug("Получили sums (из UserAction,  getEventId: getRating): {}", sums);

        return sums.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build());

    }

}
