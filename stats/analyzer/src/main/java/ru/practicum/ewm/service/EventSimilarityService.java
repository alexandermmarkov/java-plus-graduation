package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.model.EventSimilarity;
import ru.practicum.ewm.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityService {
    private final EventSimilarityRepository eventSimilarityRepository;

    @Transactional
    public void updateSimilarity(ConsumerRecord<String, EventSimilarityAvro> record) {
        EventSimilarityAvro eventSimilarityAvro = record.value();
        log.info("метод updateSimilarity, eventSimilarityAvro = {}", eventSimilarityAvro);
        long eventA = eventSimilarityAvro.getEventA();
        long eventB = eventSimilarityAvro.getEventB();

        Long event1 = Math.min(eventA, eventB);
        Long event2 = Math.max(eventA, eventB);

        log.debug("Получили eventA {}, eventB {}, будут event1 {}, event2 {}", eventA, eventB, event1, event2);
        EventSimilarity eventSimilarity = eventSimilarityRepository
                .findByEvent1AndEvent2(event1, event2)
                .orElse(EventSimilarity.builder()
                        .event1(event1)
                        .event2(event2)
                        .similarity(eventSimilarityAvro.getScore())
                        .timestamp(eventSimilarityAvro.getTimestamp())
                        .build());
        log.debug("Сохраняем eventSimilarity {}", eventSimilarity);
        eventSimilarityRepository.save(eventSimilarity);
    }
}
