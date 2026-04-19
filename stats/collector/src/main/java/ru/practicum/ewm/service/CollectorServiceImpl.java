package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.mapper.UserActionAvroMapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {
    private final UserActionAvroMapper userActionAvroMapper;
    private final Producer<Long, SpecificRecordBase> kafkaProducer;
    @Value("${kafka.collector.user-action-topic}")
    private String userActionTopic;

    @Override
    public void sendUserAction(UserActionProto userAction) {
        log.info("Получен UserAction {}", userAction.toString());
        Long eventId = userAction.getEventId();
        Instant timestamp = Instant.ofEpochSecond(
                userAction.getTimestamp().getSeconds(),
                userAction.getTimestamp().getNanos()
        );
        UserActionAvro message = userActionAvroMapper.toUserActionAvro(userAction);
        log.info("UserActionAvro message = {}", message);
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(
                userActionTopic,
                null,
                timestamp.toEpochMilli(),
                eventId,
                message);
        send(record, userAction.getActionType().toString());
    }

    private void send(ProducerRecord<Long, SpecificRecordBase> record, String type) {
        Future<RecordMetadata> future = kafkaProducer.send(record);
        kafkaProducer.flush();
        try {
            RecordMetadata metadata = future.get();
            log.info("Действие {} было успешно сохранёно в топик {} в партицию {} со смещением {}",
                    type, metadata.topic(), metadata.partition(), metadata.offset());
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Не удалось записать действие {} в топик {}", type, userActionTopic, e);
        }
    }
}
