package ru.practicum.ewm.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class ConsumerConfig {
    private final KafkaConfig kafkaConfig;

    @Bean
    public KafkaConsumer<String, UserActionAvro> userActionConsumer() {
        return new KafkaConsumer<>(getConsumerProperties());
    }
    private Properties getConsumerProperties() {
        Properties config = new Properties();
        config.put(BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        config.put(KEY_DESERIALIZER_CLASS_CONFIG, kafkaConfig.getConsumer().getKeyDeserializer());
        config.put(VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConfig.getConsumer().getValueDeserializer());
        config.put(GROUP_ID_CONFIG, kafkaConfig.getConsumer().getGroupId());
        config.put(CLIENT_ID_CONFIG, kafkaConfig.getConsumer().getClientId());
        KafkaConfig.Consumer.Properties props = kafkaConfig.getConsumer().getProperties();
        if (props.getFetchMinBytes() != null) {
            config.put(FETCH_MIN_BYTES_CONFIG, props.getFetchMinBytes());
        }
        if (props.getMaxPollRecords() != null) {
            config.put(MAX_POLL_RECORDS_CONFIG, props.getMaxPollRecords());
        }
        if (props.getEnableAutoCommit() != null) {
            config.put(ENABLE_AUTO_COMMIT_CONFIG, props.getEnableAutoCommit());
        }
        if (props.getFetchMaxWaitMs() != null) {
            config.put(FETCH_MAX_WAIT_MS_CONFIG, props.getFetchMaxWaitMs());
        }
        if (props.getMaxPartitionFetchBytes() != null) {
            config.put(MAX_PARTITION_FETCH_BYTES_CONFIG, props.getMaxPartitionFetchBytes());
        }
        return config;
    }
}
