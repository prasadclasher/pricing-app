package com.pricingfeed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "upload_jobs")
@Getter
@Setter
public class UploadJob {
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UploadJobStatus status;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Column(name = "successful_rows", nullable = false)
    private int successfulRows;

    @Column(name = "failed_rows", nullable = false)
    private int failedRows;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
