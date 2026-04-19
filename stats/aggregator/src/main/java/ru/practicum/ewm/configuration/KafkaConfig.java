package ru.practicum.ewm.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka.aggregator")
@Component
public class KafkaConfig {
    private String bootstrapServers;
    private String eventsSimilarityTopic;
    private String userActionTopic;

    private Producer producer = new Producer();
    private Consumer consumer = new Consumer();

    @Getter
    @Setter
    public static class Producer {
        private String keySerializer;
        private String valueSerializer;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String keyDeserializer;
        private String valueDeserializer;
        private String groupId;
        private String clientId;
        private Properties properties = new Properties();

        @Getter
        @Setter
        //вложенный класс
        public static class Properties {
            private Integer fetchMinBytes;
            private Integer maxPollRecords;
            private Boolean enableAutoCommit;
            private Integer fetchMaxWaitMs;
            private Integer maxPartitionFetchBytes;
        }
    }
}
