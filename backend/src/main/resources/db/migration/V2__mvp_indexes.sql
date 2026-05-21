CREATE INDEX idx_pricing_store_sku_date ON pricing_records (store_id, sku, price_date);
CREATE INDEX idx_pricing_store_date ON pricing_records (store_id, price_date);
CREATE INDEX idx_pricing_sku_date ON pricing_records (sku, price_date);
CREATE INDEX idx_pricing_date ON pricing_records (price_date);
CREATE INDEX idx_pricing_product_name ON pricing_records (product_name);

CREATE INDEX idx_upload_jobs_store_status_created ON upload_jobs (store_id, status, created_at);
CREATE INDEX idx_pricing_audit_record_changed ON pricing_audit_log (pricing_record_id, changed_at);
