-- ============================================================
-- V4: Add uploaded_files table for temporary CSV blob storage
--     and extend upload_jobs with file_id + error_message
-- ============================================================

CREATE TABLE uploaded_files (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id      CHAR(36) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes  BIGINT NOT NULL,
    content     LONGBLOB NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_uploaded_files_job FOREIGN KEY (job_id) REFERENCES upload_jobs (id)
);

ALTER TABLE upload_jobs
    ADD COLUMN file_id       BIGINT       NULL AFTER uploaded_by,
    ADD COLUMN error_message VARCHAR(2000) NULL AFTER file_id;
