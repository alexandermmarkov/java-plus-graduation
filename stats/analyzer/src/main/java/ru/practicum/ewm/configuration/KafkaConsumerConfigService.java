package ru.practicum.ewm.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
public class KafkaConsumerConfigService {
    private final KafkaConfig kafkaConfig;

    public Properties getConsumerProperties(String consumerName) {
        KafkaConfig.ConsumerConfigDetails config = kafkaConfig.getConsumers().get(consumerName);
        if (config == null) {
            throw new IllegalArgumentException("Не найдена конфигурация для консюмера: " + consumerName);
        }

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getValueDeserializer());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, config.getClientId());

        KafkaConfig.ConsumerConfigDetails.Properties propsConfig = config.getProperties();
        if (propsConfig.getFetchMinBytes() != null) {
            props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, propsConfig.getFetchMinBytes());
        }
        if (propsConfig.getMaxPollRecords() != null) {
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, propsConfig.getMaxPollRecords());
        }
        if (propsConfig.getEnableAutoCommit() != null) {
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, propsConfig.getEnableAutoCommit());
        }
        if (propsConfig.getFetchMaxWaitMs() != null) {
            props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, propsConfig.getFetchMaxWaitMs());
        }
        if (propsConfig.getMaxPartitionFetchBytes() != null) {
            props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, propsConfig.getMaxPartitionFetchBytes());
        }

        return props;
    }

    public String getTopic(String consumerName) {
        return kafkaConfig.getConsumers().get(consumerName).getTopic();
    }
}
