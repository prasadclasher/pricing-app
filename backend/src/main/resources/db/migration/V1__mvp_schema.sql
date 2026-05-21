CREATE TABLE stores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    default_currency_code CHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(120) NOT NULL UNIQUE,
    store_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_users_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE upload_jobs (
    id CHAR(36) PRIMARY KEY,
    store_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    total_rows INT NOT NULL DEFAULT 0,
    successful_rows INT NOT NULL DEFAULT 0,
    failed_rows INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_upload_jobs_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT fk_upload_jobs_user FOREIGN KEY (uploaded_by) REFERENCES app_users (id)
);

CREATE TABLE upload_job_errors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    upload_job_id CHAR(36) NOT NULL,
    row_id INT NOT NULL,
    row_data JSON NULL,
    error_message VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_upload_job_errors_job FOREIGN KEY (upload_job_id) REFERENCES upload_jobs (id)
);

CREATE TABLE pricing_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_id BIGINT NOT NULL,
    sku VARCHAR(128) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    price_date DATE NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_pricing_records_store_sku_date UNIQUE (store_id, sku, price_date),
    CONSTRAINT fk_pricing_records_store FOREIGN KEY (store_id) REFERENCES stores (id)
);

CREATE TABLE pricing_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pricing_record_id BIGINT NOT NULL,
    changed_by BIGINT NOT NULL,
    old_value JSON NOT NULL,
    new_value JSON NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pricing_audit_record FOREIGN KEY (pricing_record_id) REFERENCES pricing_records (id),
    CONSTRAINT fk_pricing_audit_user FOREIGN KEY (changed_by) REFERENCES app_users (id)
);
