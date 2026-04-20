package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final UserActionService userActionService;
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final Producer<String, EventSimilarityAvro> producer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Value("${kafka.aggregator.user-action-topic}")
    private String userActionTopic;

    @Value("${kafka.aggregator.events-similarity-topic}")
    private String eventsSimilarityTopic;

    public void start() {
        log.info("Старт агрегатора");
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(Collections.singletonList(userActionTopic));
            log.info("Подписка на топик {}", userActionTopic);
            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) {
                    continue;
                }
                log.debug("Получено {} записей", records.count());

                int processedCount = 0;
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    // сначала отправить сообщение, если это нужно
                    handleRecord(record, producer);
                    currentOffsets.put(
                            new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1)
                    );
                    processedCount++;
                }
                if (processedCount > 0) {
                    try {
                        consumer.commitSync(currentOffsets);
                        log.info("Зафиксировано {} записей", processedCount);
                    } catch (Exception e) {
                        log.error("Ошибка при коммите оффсетов: {}", e.getMessage(), e);
                    }
                }
            }
        } catch (WakeupException ignored) {
            log.info("Получено исключение WakeupException");
        } finally {
            try {
                producer.flush();
                if (!currentOffsets.isEmpty()) {
                    log.info("Финализируем оффсеты...");
                    consumer.commitSync(currentOffsets);
                }
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private void handleRecord(ConsumerRecord<String, UserActionAvro> record, Producer<String, EventSimilarityAvro> producer) {
        try {
            UserActionAvro userActionAvro = record.value();
            log.info("метод handleRecord, userActionAvro = {}", userActionAvro);
            List<EventSimilarityAvro> eventSimilarities = userActionService.updateSimilarity(userActionAvro);
            for (EventSimilarityAvro similarityAvro : eventSimilarities) {
                ProducerRecord<String, EventSimilarityAvro> producerRecord = new ProducerRecord<>(
                        eventsSimilarityTopic,
                        null,
                        similarityAvro.getTimestamp().toEpochMilli(),
                        similarityAvro.getEventA() + "_" + similarityAvro.getEventB(),
                        similarityAvro
                );
                producer.send(producerRecord, (metadata, exception) -> {
                    if (exception != null) {
                        log.error("Ошибка при отправке EventSimilarity в топик {}: {}", eventsSimilarityTopic, exception.getMessage(), exception);
                    } else {
                        log.info("Отправлено {} успешно в топик {} на партицию {} со смещением {}",
                                similarityAvro, metadata.topic(), metadata.partition(), metadata.offset());
                    }
                });
            }
        } catch (Exception e) {
            log.error("Ошибка обработки записи: {}", record.value(), e);
        }
    }

}
