package ru.practicum.ewm.configuration;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Properties;

@Configuration
public class KafkaConfig {
    @Value("${kafka.collector.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.collector.key-serializer}")
    private String keySerializer;

    @Value("${kafka.collector.value-serializer}")
    private String valueSerializer;

    @Bean
    @Primary
    public Producer<Long, SpecificRecordBase> kafkaProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

        return new KafkaProducer<>(config);
    }
}
