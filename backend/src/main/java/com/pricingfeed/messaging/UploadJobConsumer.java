package com.pricingfeed.messaging;

import com.pricingfeed.service.CsvProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens for upload job messages and delegates
 * CSV processing to {@link CsvProcessingService}.
 */
@Component
public class UploadJobConsumer {

    private static final Logger log = LoggerFactory.getLogger(UploadJobConsumer.class);

    private final CsvProcessingService csvProcessingService;

    public UploadJobConsumer(CsvProcessingService csvProcessingService) {
        this.csvProcessingService = csvProcessingService;
    }

    @KafkaListener(
            topics = "${app.kafka.upload-topic:csv-upload-jobs}",
            groupId = "${spring.kafka.consumer.group-id:pricing-feed-consumers}"
    )
    public void onMessage(UploadJobMessage message) {
        log.info("Received upload job message: jobId={}, fileId={}", message.getJobId(), message.getFileId());
        try {
            csvProcessingService.processUploadJob(
                    message.getJobId(),
                    message.getFileId(),
                    message.getStoreId()
            );
        } catch (Exception ex) {
            log.error("Failed to process upload job {}: {}", message.getJobId(), ex.getMessage(), ex);
            // The message will not be re-consumed (at-most-once by default).
            // For retries, configure a retry template or DLQ in KafkaConfig.
        }
    }
}
