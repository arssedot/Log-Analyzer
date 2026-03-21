package com.arssedot.loganalyzer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_LOGS_INGEST = "logs.ingest";

    @Bean
    public NewTopic logIngestTopic() {
        return TopicBuilder.name(TOPIC_LOGS_INGEST)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
