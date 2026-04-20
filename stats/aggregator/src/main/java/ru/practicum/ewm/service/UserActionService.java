package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {
    /**
     * Матрица весов взаимодействий
     * Map<eventId, Map<userId, максимальный вес из всех его действий с этим мероприятием>>
     */
    private final Map<Long, Map<Long, Double>> eventUserWeightMatrix = new ConcurrentHashMap<>();
    /**
     * общие суммы весов каждого из мероприятий. Для Знаменателя
     * Map<eventId, сумма весов действий пользователей>
     */
    private final Map<Long, Double> eventWeightSums = new ConcurrentHashMap<>();

    /**
     * Сумма минимальных весов для каждой пары мероприятий. Числитель.
     * Map<eventIdA, Map<eventIdB, сумма их минимальных весов>>
     */
    private final Map<Long, Map<Long, Double>> eventMinWeightSums = new ConcurrentHashMap<>();
    private final double epsilon = 1e-10;
    @Value("${user-action.view:0.4}")
    Double viewValue;

    @Value("${user-action.register:0.8}")
    Double registerValue;

    @Value("${user-action.like:1.0}")
    Double likeValue;

    public List<EventSimilarityAvro> updateSimilarity(UserActionAvro userActionAvro) {
        long userId = userActionAvro.getUserId();
        long eventId = userActionAvro.getEventId();
        ActionTypeAvro actionTypeAvro = userActionAvro.getActionType();

        List<EventSimilarityAvro> similarities = new ArrayList<>();

        Map<Long, Double> userMaxWeight = eventUserWeightMatrix.computeIfAbsent(eventId, v -> new ConcurrentHashMap<>());
        double oldWeight = userMaxWeight.getOrDefault(userId, 0.0);
        // Получаем вес нового действия
        double newWeight = switch (actionTypeAvro) {
            case VIEW -> viewValue;
            case REGISTER -> registerValue;
            case LIKE -> likeValue;
        };

        if (Math.abs(newWeight - oldWeight) < epsilon) {
            log.debug("Вес не изменился, пользователь {}, мероприятие {}", userId, eventId);
            return similarities;
        }
        if (newWeight < oldWeight) {
            log.debug("Новый вес {} меньше старого {}. Обновлять не надо.", newWeight, oldWeight);
            return similarities;
        }
        // Если вес был обновлён, нужно пересчитать частные суммы и косинусное сходство мероприятия A с остальными мероприятиями.
        double oldWeightSum = eventWeightSums.getOrDefault(eventId, 0.0);
        double newWeightSum = oldWeightSum - oldWeight + newWeight;
        userMaxWeight.put(userId, newWeight); // матрица весов обновилась
        eventWeightSums.put(eventId, newWeightSum);
        log.info("Мероприятию {} изменили общий вес, был {}, стал {}", eventId, oldWeightSum, newWeightSum);

        for (Long anotherEventId : eventUserWeightMatrix.keySet()) {
            if (eventId == anotherEventId) continue;
            Map<Long, Double> anotherUserWeights = eventUserWeightMatrix.get(anotherEventId);
            if (anotherUserWeights != null && anotherUserWeights.containsKey(userId)) {
                updateMinWeightSums(userId, eventId, anotherEventId, oldWeight, newWeight);
                double similarity = calculateSimilarity(eventId, anotherEventId);
                if (Math.abs(similarity) > epsilon) {
                    similarities.add(createSimilarityAvro(eventId, anotherEventId, similarity, userActionAvro.getTimestamp()));
                }
            }
        }
        log.info("Возвращаем similarities {}", similarities);
        return similarities;
    }

    private EventSimilarityAvro createSimilarityAvro(long eventA, long eventB, double similarity, Instant timestamp) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(similarity)
                .setTimestamp(timestamp)
                .build();
    }

    private Double calculateSimilarity(long eventId, long anotherEventId) {
        log.debug("Рассчитываем коэффициент сходства мероприятий {} и {}", eventId, anotherEventId);
        double sumEvent = eventWeightSums.getOrDefault(eventId, 0.0);
        double sumAnotherEvent = eventWeightSums.getOrDefault(anotherEventId, 0.0);
        double denominator = Math.sqrt(sumEvent) * Math.sqrt(sumAnotherEvent);
        if (Math.abs(denominator) < epsilon) {
            return 0.0;
        }
        return getMin(eventId, anotherEventId) / denominator;
    }

    private void updateMinWeightSums(long userId, long eventId, long anotherEventId, double oldWeight,
                                     double newWeight) {
        log.info("Изменение минимальных весов для мероприятий {} и {}", eventId, anotherEventId);
        double anotherEventWeight = eventUserWeightMatrix
                .getOrDefault(anotherEventId, new ConcurrentHashMap<>())
                .getOrDefault(userId, 0.0);
        log.debug("Вес другого мероприятия {} у пользователя {} = {}", anotherEventId, userId, anotherEventWeight);

        double oldMinimum = Math.min(oldWeight, anotherEventWeight);
        double newMinimum = Math.min(newWeight, anotherEventWeight);
        if (Math.abs(oldMinimum - newMinimum) < epsilon) {
            log.debug("Минимальный вес в паре мероприятий не изменился");
            return;
        }
        // сумма минимумов пар весов двух мероприятий
        double oldSumMin = getMin(eventId, anotherEventId);
        double newSumMin = oldSumMin - oldMinimum + newMinimum;
        putMin(eventId, anotherEventId, newSumMin);
        log.info("Новый минимальный вес для мероприятий {} и {} = {}", eventId, anotherEventId, newSumMin);
    }

    public void putMin(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        eventMinWeightSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .put(second, sum);
    }

    public double getMin(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return eventMinWeightSums
                .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
                .getOrDefault(second, 0.0);
    }
}
