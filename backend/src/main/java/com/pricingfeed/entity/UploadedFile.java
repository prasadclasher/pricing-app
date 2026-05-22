package com.pricingfeed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Holds the raw CSV payload for a single upload job. The blob is kept only
 * until processing completes, after which it is deleted to free DB space.
 */
@Entity
@Table(name = "uploaded_files")
@Getter
@Setter
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "job_id", nullable = false, columnDefinition = "CHAR(36)")
    private String jobId;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long size;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    /**
     * Stores the raw CSV payload. Using LONGBLOB ensures compatibility with MySQL for large files.
     */
    @Lob
    @Column(name = "content", columnDefinition = "LONGBLOB")
    private byte[] content;

    /**
     * Sets the uploaded timestamp automatically before persisting.
     */
    @PrePersist
    void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = OffsetDateTime.now();
        }
    }
}
