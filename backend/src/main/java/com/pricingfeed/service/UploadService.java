package com.pricingfeed.service;

import com.opencsv.CSVReaderHeaderAware;
import com.pricingfeed.api.response.UploadJobErrorRowResponse;
import com.pricingfeed.api.response.UploadJobResponse;
import com.pricingfeed.entity.PricingRecord;
import com.pricingfeed.entity.UploadJob;
import com.pricingfeed.entity.UploadJobStatus;
import com.pricingfeed.entity.UploadJobError;
import com.pricingfeed.repo.PricingRecordRepository;
import com.pricingfeed.repo.UploadJobErrorRepository;
import com.pricingfeed.repo.UploadJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

import static com.pricingfeed.service.ApiExceptions.BadRequestException;
import static com.pricingfeed.service.ApiExceptions.ForbiddenException;
import static com.pricingfeed.service.ApiExceptions.NotFoundException;

@Service
public class UploadService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> REQUIRED = List.of("store_id", "sku", "product_name", "price", "price_date");

    private final UploadJobRepository uploadJobRepository;
    private final UploadJobErrorRepository uploadJobErrorRepository;
    private final PricingRecordRepository pricingRecordRepository;

    public UploadService(UploadJobRepository uploadJobRepository, UploadJobErrorRepository uploadJobErrorRepository, PricingRecordRepository pricingRecordRepository) {
        this.uploadJobRepository = uploadJobRepository;
        this.uploadJobErrorRepository = uploadJobErrorRepository;
        this.pricingRecordRepository = pricingRecordRepository;
    }

    @Transactional
    public String createAndProcess(MultipartFile file, ActorContext.Actor actor) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required");
        }

        UploadJob job = new UploadJob();
        job.setStoreId(actor.storeId() == null ? 1L : actor.storeId());
        job.setUploadedBy(actor.userId());
        job.setStatus(UploadJobStatus.PENDING);
        uploadJobRepository.save(job);

        processCsv(job, file, actor);
        return job.getId();
    }

    @Transactional(readOnly = true)
    public UploadJobResponse getJob(String id, ActorContext.Actor actor) {
        UploadJob job = uploadJobRepository.findById(id).orElseThrow(() -> new NotFoundException("Upload job not found"));
        if (!actor.isHq() && !Objects.equals(job.getStoreId(), actor.storeId())) {
            throw new ForbiddenException("Upload job is outside user store scope");
        }
        List<UploadJobErrorRowResponse> rows = uploadJobErrorRepository.findByUploadJobIdOrderByRowIdAsc(id)
                .stream()
                .map(r -> new UploadJobErrorRowResponse(r.getRowId(), r.getErrorMessage(), r.getRowData()))
                .toList();
        return new UploadJobResponse(job.getId(), job.getStatus(), job.getTotalRows(), job.getSuccessfulRows(), job.getFailedRows(), job.getStartedAt(), job.getCompletedAt(), rows);
    }

    private void processCsv(UploadJob job, MultipartFile file, ActorContext.Actor actor) {
        job.setStatus(UploadJobStatus.PROCESSING);
        job.setStartedAt(OffsetDateTime.now());
        uploadJobRepository.save(job);
        int row = 1;
        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new InputStreamReader(file.getInputStream()))) {
            Map<String, String> data;
            while ((data = reader.readMap()) != null) {
                row++;
                job.setTotalRows(job.getTotalRows() + 1);
                try {
                    validateHeaders(data);
                    Long storeId = Long.parseLong(data.get("store_id"));
                    if (!actor.isHq() && !Objects.equals(actor.storeId(), storeId)) {
                        throw new ForbiddenException("Store scoped user cannot upload for another store");
                    }
                    String sku = required(data, "sku");
                    String productName = required(data, "product_name");
                    BigDecimal price = new BigDecimal(required(data, "price"));
                    if (price.compareTo(BigDecimal.ZERO) < 0) throw new BadRequestException("price must be non-negative");
                    LocalDate priceDate = LocalDate.parse(required(data, "price_date"));
                    String currency = data.getOrDefault("currency_code", "USD");

                    PricingRecord record = pricingRecordRepository.findByStoreIdAndSkuAndPriceDate(storeId, sku, priceDate).orElseGet(PricingRecord::new);
                    record.setStoreId(storeId);
                    record.setSku(sku);
                    record.setProductName(productName);
                    record.setPrice(price);
                    record.setPriceDate(priceDate);
                    record.setCurrencyCode(currency);
                    pricingRecordRepository.save(record);
                    job.setSuccessfulRows(job.getSuccessfulRows() + 1);
                } catch (Exception ex) {
                    UploadJobError error = new UploadJobError();
                    error.setUploadJobId(job.getId());
                    error.setRowId(row);
                    try {
                        error.setRowData(OBJECT_MAPPER.writeValueAsString(data));
                    } catch (Exception e) {
                        error.setRowData("{}");
                    }
                    error.setErrorMessage(ex.getMessage());
                    uploadJobErrorRepository.save(error);
                    job.setFailedRows(job.getFailedRows() + 1);
                }
            }
            job.setStatus(job.getFailedRows() > 0 ? UploadJobStatus.PARTIAL : UploadJobStatus.COMPLETED);
        } catch (Exception ex) {
            job.setStatus(UploadJobStatus.FAILED);
        }
        job.setCompletedAt(OffsetDateTime.now());
        uploadJobRepository.save(job);
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
