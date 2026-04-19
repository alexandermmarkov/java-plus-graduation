package ru.practicum.ewm.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka.analyzer")
@Component
public class KafkaConfig {
    private String bootstrapServers;
    private Double view;
    private Double register;
    private Double like;
    private Map<String, ConsumerConfigDetails> consumers = new HashMap<>();

    @Getter
    @Setter
    public static class ConsumerConfigDetails {
        private String topic;
        private String keyDeserializer;
        private String valueDeserializer;
        private String groupId;
        private String clientId;
        private Properties properties = new Properties();

        @Getter
        @Setter
        public static class Properties {
            private Integer fetchMinBytes;
            private Integer maxPollRecords;
            private Boolean enableAutoCommit;
            private Integer fetchMaxWaitMs;
            private Integer maxPartitionFetchBytes;
        }
    }
}
