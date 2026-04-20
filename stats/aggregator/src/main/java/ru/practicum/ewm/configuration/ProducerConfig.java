package ru.practicum.ewm.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Properties;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Component
@RequiredArgsConstructor
public class ProducerConfig {
    private final KafkaConfig kafkaConfig;

    @Bean
    public Producer<String, EventSimilarityAvro> eventSimilarityProducer() {
        return new KafkaProducer<>(getProducerProperties());
    }

    private Properties getProducerProperties() {
        Properties config = new Properties();
        config.put(BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        config.put(KEY_SERIALIZER_CLASS_CONFIG, kafkaConfig.getProducer().getKeySerializer());
        config.put(VALUE_SERIALIZER_CLASS_CONFIG, kafkaConfig.getProducer().getValueSerializer());
        return config;
    }
}
