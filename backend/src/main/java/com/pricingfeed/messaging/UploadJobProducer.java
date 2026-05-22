package com.pricingfeed.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link UploadJobMessage} to the Kafka topic for asynchronous CSV processing.
 */
@Component
public class UploadJobProducer {

    private static final Logger log = LoggerFactory.getLogger(UploadJobProducer.class);

    private final KafkaTemplate<String, UploadJobMessage> kafkaTemplate;
    private final String topic;

    public UploadJobProducer(KafkaTemplate<String, UploadJobMessage> kafkaTemplate,
                             @Value("${app.kafka.upload-topic:csv-upload-jobs}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Publishes the given message to the configured Kafka topic.
     * Uses the jobId as the Kafka record key for partition affinity.
     */
    public void publish(UploadJobMessage message) {
        log.info("Publishing upload job message to topic={} jobId={}", topic, message.getJobId());
        kafkaTemplate.send(topic, message.getJobId(), message);
    }
}
