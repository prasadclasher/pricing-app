package com.pricingfeed.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReaderHeaderAware;
import com.pricingfeed.entity.*;
import com.pricingfeed.repo.PricingRecordRepository;
import com.pricingfeed.repo.UploadJobErrorRepository;
import com.pricingfeed.repo.UploadJobRepository;
import com.pricingfeed.repo.UploadedFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.pricingfeed.service.ApiExceptions.BadRequestException;

/**
 * Processes CSV content row-by-row from the {@link UploadedFile} blob.
 * Invoked by the Kafka consumer after a message is received.
 */
@Service
public class CsvProcessingService {

    private static final Logger log = LoggerFactory.getLogger(CsvProcessingService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> REQUIRED = List.of("store_id", "sku", "product_name", "price", "price_date");

    private final UploadJobRepository uploadJobRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final PricingRecordRepository pricingRecordRepository;
    private final UploadJobErrorRepository uploadJobErrorRepository;

    public CsvProcessingService(UploadJobRepository uploadJobRepository,
                                UploadedFileRepository uploadedFileRepository,
                                PricingRecordRepository pricingRecordRepository,
                                UploadJobErrorRepository uploadJobErrorRepository) {
        this.uploadJobRepository = uploadJobRepository;
        this.uploadedFileRepository = uploadedFileRepository;
        this.pricingRecordRepository = pricingRecordRepository;
        this.uploadJobErrorRepository = uploadJobErrorRepository;
    }

    /**
     * Main entry point called by the Kafka consumer.
     * Fetches the blob, streams CSV rows, upserts pricing records,
     * logs per-row errors, and updates job status/counters.
     */
    @Transactional
    public void processUploadJob(String jobId, Long fileId, Long storeId, boolean isHq) {
        UploadJob job = uploadJobRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Upload job not found: {}", jobId);
                    return new IllegalStateException("Upload job not found: " + jobId);
                });

        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> {
                    log.error("Uploaded file not found: {}", fileId);
                    return new IllegalStateException("Uploaded file not found: " + fileId);
                });

        // Mark job as processing
        job.setStatus(UploadJobStatus.PROCESSING);
        job.setStartedAt(OffsetDateTime.now());
        uploadJobRepository.save(job);

        byte[] content = uploadedFile.getContent();
        if (content == null || content.length == 0) {
            job.setStatus(UploadJobStatus.FAILED);
            job.setErrorMessage("Uploaded file content is empty");
            job.setCompletedAt(OffsetDateTime.now());
            uploadJobRepository.save(job);
            return;
        }

        int rowNumber = 1;
        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(
                new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8))) {

            Map<String, String> data;
            while ((data = reader.readMap()) != null) {
                rowNumber++;
                job.setTotalRows(job.getTotalRows() + 1);

                try {
                    processRow(data, storeId, isHq);
                    job.setSuccessfulRows(job.getSuccessfulRows() + 1);
                } catch (Exception ex) {
                    logRowError(job.getId(), rowNumber, data, ex.getMessage());
                    job.setFailedRows(job.getFailedRows() + 1);
                }
            }

            job.setStatus(job.getFailedRows() > 0 ? UploadJobStatus.PARTIAL : UploadJobStatus.COMPLETED);
        } catch (Exception ex) {
            log.error("Fatal error processing CSV for job {}: {}", jobId, ex.getMessage(), ex);
            job.setStatus(UploadJobStatus.FAILED);
            job.setErrorMessage("CSV processing failed: " + ex.getMessage());
        }

        job.setCompletedAt(OffsetDateTime.now());
        uploadJobRepository.save(job);

        // Cleanup: nullify the blob content to free DB space
        uploadedFileRepository.deleteContentById(fileId);
        log.info("Completed processing job={} total={} success={} failed={}",
                jobId, job.getTotalRows(), job.getSuccessfulRows(), job.getFailedRows());
    }

    private void processRow(Map<String, String> data, Long storeId, boolean isHq) {
        validateHeaders(data);

        Long rowStoreId = Long.parseLong(data.get("store_id"));
        if (!isHq && !Objects.equals(storeId, rowStoreId)) {
            throw new BadRequestException("Row store_id does not match upload store");
        }

        String sku = required(data, "sku");
        String productName = required(data, "product_name");
        BigDecimal price = new BigDecimal(required(data, "price"));
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("price must be non-negative");
        }
        LocalDate priceDate = LocalDate.parse(required(data, "price_date"));
        String currency = data.getOrDefault("currency_code", "USD");

        PricingRecord record = pricingRecordRepository
                .findByStoreIdAndSkuAndPriceDate(rowStoreId, sku, priceDate)
                .orElseGet(PricingRecord::new);
        record.setStoreId(rowStoreId);
        record.setSku(sku);
        record.setProductName(productName);
        record.setPrice(price);
        record.setPriceDate(priceDate);
        record.setCurrencyCode(currency);
        pricingRecordRepository.save(record);
    }

    private void logRowError(String jobId, int rowNumber, Map<String, String> data, String message) {
        UploadJobError error = new UploadJobError();
        error.setUploadJobId(jobId);
        error.setRowId(rowNumber);
        try {
            error.setRowData(OBJECT_MAPPER.writeValueAsString(data));
        } catch (Exception e) {
            error.setRowData("{}");
        }
        error.setErrorMessage(message);
        uploadJobErrorRepository.save(error);
    }

    private void validateHeaders(Map<String, String> rowData) {
        for (String key : REQUIRED) {
            if (!rowData.containsKey(key)) {
                throw new BadRequestException("Missing required CSV column: " + key);
            }
        }
    }

    private String required(Map<String, String> rowData, String key) {
        String value = rowData.get(key);
        if (value == null || value.isBlank()) throw new BadRequestException("Missing value: " + key);
        return value;
    }
}
