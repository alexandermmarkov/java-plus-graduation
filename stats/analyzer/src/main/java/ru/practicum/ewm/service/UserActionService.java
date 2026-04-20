package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.model.UserAction;
import ru.practicum.ewm.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {
    private final UserActionRepository userActionRepository;
    private final double epsilon = 1e-10;
    @Value("${user-action.view:0.4}")
    Double viewValue;

    @Value("${user-action.register:0.8}")
    Double registerValue;

    @Value("${user-action.like:1.0}")
    Double likeValue;

    @Transactional
    public void updateAction(ConsumerRecord<String, UserActionAvro> record) {
        UserActionAvro userActionAvro = record.value();
        log.info("метод updateAction, userActionAvro = {}", userActionAvro);
        long userId = userActionAvro.getUserId();
        long eventId = userActionAvro.getEventId();
        ActionTypeAvro actionTypeAvro = userActionAvro.getActionType();

        // Получаем вес нового действия
        double newRating = switch (actionTypeAvro) {
            case VIEW -> viewValue;
            case REGISTER -> registerValue;
            case LIKE -> likeValue;
        };
        log.debug("newRating = {}", newRating);
        UserAction userAction = userActionRepository
                .findByUserIdAndEventId(userId, eventId)
                .orElse(UserAction.builder()
                        .userId(userId)
                        .eventId(eventId)
                        .rating(0.0)
                        .timestamp(Instant.now())
                        .build());

        Double oldRating = userAction.getRating();
        log.debug("oldRating = {}", oldRating);
        if (newRating - oldRating >= epsilon) {
            userAction.setRating(newRating);
            userAction.setTimestamp(userActionAvro.getTimestamp());
            log.debug("Сохраняем действие с событием {}, userAction {} в userActionRepository", userAction.getEventId(), userAction);
            userActionRepository.save(userAction);
        }
    }

}
