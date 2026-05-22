-- ============================================================
-- V5: Make uploaded_files.content nullable
--     This allows us to clean up the blob by setting it to NULL
--     after processing is completed, saving database space.
-- ============================================================

ALTER TABLE uploaded_files MODIFY content LONGBLOB NULL;
