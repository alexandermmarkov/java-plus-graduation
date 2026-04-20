package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.configuration.KafkaConsumerConfigService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProcessor implements Runnable {
    private final KafkaConsumerConfigService configService;
    private final EventSimilarityService eventSimilarityService;
    private final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

    @Override
    public void run() {
        Properties props = configService.getConsumerProperties("event-similarity");
        String topic = configService.getTopic("event-similarity");
        KafkaConsumer<String, EventSimilarityAvro> consumer = new KafkaConsumer<>(props);
        // Добавить shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try {
            consumer.subscribe(Collections.singletonList(topic));
            log.info("Подписка на топик {}", topic);
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) {
                    continue;
                }
                log.debug("Получено {} записей", records.count());
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    try {
                        eventSimilarityService.updateSimilarity(record);
                        offsets.put(
                                new TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + 1)
                        );
                    } catch (Exception e) {
                        log.error("Ошибка при обработке, offset: {}", record.offset(), e);
                    }
                }
                // Коммитим накопленные offsets
                if (!offsets.isEmpty()) {
                    consumer.commitSync(offsets);
                    offsets.clear();
                }
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал завершения (исключение WakeupException)");
        } finally {
            try {
                if (!offsets.isEmpty()) {
                    consumer.commitSync(offsets);
                }
            } finally {
                log.info("Закрываем консьюмер сценариев");
                consumer.close();
            }
        }
    }

}
