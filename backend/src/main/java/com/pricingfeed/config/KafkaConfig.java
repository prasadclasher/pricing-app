package com.pricingfeed.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka configuration — auto-creates the upload topic if it doesn't exist.
 * Producer/consumer serialization is driven by application.yml.
 */
@Configuration
public class KafkaConfig {

    @Value("${app.kafka.upload-topic:csv-upload-jobs}")
    private String uploadTopic;

    @Bean
    public NewTopic uploadJobsTopic() {
        return TopicBuilder.name(uploadTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
