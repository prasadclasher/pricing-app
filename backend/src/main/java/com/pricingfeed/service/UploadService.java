package com.pricingfeed.service;

import com.pricingfeed.api.response.UploadJobErrorRowResponse;
import com.pricingfeed.api.response.UploadJobResponse;
import com.pricingfeed.entity.UploadJob;
import com.pricingfeed.entity.UploadJobStatus;
import com.pricingfeed.entity.UploadedFile;
import com.pricingfeed.messaging.UploadJobMessage;
import com.pricingfeed.messaging.UploadJobProducer;
import com.pricingfeed.repo.UploadJobErrorRepository;
import com.pricingfeed.repo.UploadJobRepository;
import com.pricingfeed.repo.UploadedFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import static com.pricingfeed.service.ApiExceptions.BadRequestException;
import static com.pricingfeed.service.ApiExceptions.ForbiddenException;
import static com.pricingfeed.service.ApiExceptions.NotFoundException;

@Service
public class UploadService {

    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    private final UploadJobRepository uploadJobRepository;
    private final UploadJobErrorRepository uploadJobErrorRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final UploadJobProducer uploadJobProducer;

    public UploadService(UploadJobRepository uploadJobRepository,
                         UploadJobErrorRepository uploadJobErrorRepository,
                         UploadedFileRepository uploadedFileRepository,
                         UploadJobProducer uploadJobProducer) {
        this.uploadJobRepository = uploadJobRepository;
        this.uploadJobErrorRepository = uploadJobErrorRepository;
        this.uploadedFileRepository = uploadedFileRepository;
        this.uploadJobProducer = uploadJobProducer;
    }

    /**
     * Accepts an uploaded CSV file, persists the blob and job metadata,
     * then publishes a Kafka event after the transaction commits.
     *
     * @return the generated jobId for status polling
     */
    @Transactional
    public String acceptUpload(MultipartFile file, ActorContext.Actor actor) {
        // 1. Validate the incoming file
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("CSV file is required");
        }
        if (file.getContentType() != null && !file.getContentType().contains("csv")) {
            throw new BadRequestException("Only CSV files are supported");
        }

        // 2. Persist the lightweight job record (status = PENDING)
        UploadJob job = new UploadJob();
        job.setStoreId(actor.storeId() == null ? 1L : actor.storeId());
        job.setUploadedBy(actor.userId());
        job.setStatus(UploadJobStatus.PENDING);
        uploadJobRepository.save(job);

        // 3. Persist the CSV blob in a separate table
        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setJobId(job.getId());
        uploadedFile.setOriginalFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.csv");
        uploadedFile.setContentType(file.getContentType() != null ? file.getContentType() : "text/csv");
        uploadedFile.setSize(file.getSize());
        try {
            uploadedFile.setContent(file.getBytes()); // read within request lifecycle
        } catch (Exception ex) {
            throw new BadRequestException("Failed to read uploaded file");
        }
        uploadedFileRepository.save(uploadedFile);

        // 4. Store the generated fileId on the job for later lookup
        job.setFileId(uploadedFile.getId());
        uploadJobRepository.save(job);

        // 5. Publish a Kafka event *after* the DB transaction commits
        publishAfterCommit(job, uploadedFile);

        return job.getId();
    }

    /**
     * Returns the current status of an upload job including error details.
     */
    @Transactional(readOnly = true)
    public UploadJobResponse getJob(String id, ActorContext.Actor actor) {
        UploadJob job = uploadJobRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Upload job not found"));
        if (!actor.isHq() && !Objects.equals(job.getStoreId(), actor.storeId())) {
            throw new ForbiddenException("Upload job is outside user store scope");
        }
        List<UploadJobErrorRowResponse> rows = uploadJobErrorRepository.findByUploadJobIdOrderByRowIdAsc(id)
                .stream()
                .map(r -> new UploadJobErrorRowResponse(r.getRowId(), r.getErrorMessage(), r.getRowData()))
                .toList();
        return new UploadJobResponse(job.getId(), job.getStatus(), job.getTotalRows(),
                job.getSuccessfulRows(), job.getFailedRows(), job.getStartedAt(), job.getCompletedAt(), rows);
    }

    /**
     * Registers a transaction synchronisation callback that publishes the
     * Kafka message only after the surrounding transaction successfully commits.
     */
    private void publishAfterCommit(UploadJob job, UploadedFile file) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                UploadJobMessage message = new UploadJobMessage(
                        job.getId(),
                        file.getId(),
                        job.getStoreId(),
                        job.getUploadedBy(),
                        OffsetDateTime.now()
                );
                uploadJobProducer.publish(message);
                log.info("Published Kafka message for jobId={}", job.getId());
            }
        });
    }
}
