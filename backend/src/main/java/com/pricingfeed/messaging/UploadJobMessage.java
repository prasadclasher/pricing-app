package com.pricingfeed.messaging;

import java.time.OffsetDateTime;

/**
 * Message payload published to Kafka when a CSV upload is accepted.
 * Contains only metadata — the actual CSV content is stored in the
 * {@code uploaded_files} table and referenced via {@code fileId}.
 */
public class UploadJobMessage {

    private String jobId;
    private Long fileId;
    private Long storeId;
    private Long uploadedBy;
    private OffsetDateTime uploadedAt;
    private boolean isHq;

    public UploadJobMessage() {}

    public UploadJobMessage(String jobId, Long fileId, Long storeId, Long uploadedBy, OffsetDateTime uploadedAt, boolean isHq) {
        this.jobId = jobId;
        this.fileId = fileId;
        this.storeId = storeId;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
        this.isHq = isHq;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }

    public OffsetDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(OffsetDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public boolean isHq() { return isHq; }
    public void setHq(boolean isHq) { this.isHq = isHq; }
}
