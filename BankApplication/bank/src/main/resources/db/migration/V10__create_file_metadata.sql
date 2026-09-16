CREATE TABLE file_metadata (
    id BIGINT NOT NULL AUTO_INCREMENT,

    customer_id BIGINT NOT NULL,
    uploaded_by_user_id BIGINT NOT NULL,

    document_type VARCHAR(50) NOT NULL,

    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,

    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    checksum VARCHAR(128) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT fk_file_metadata_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_file_metadata_uploaded_by
        FOREIGN KEY (uploaded_by_user_id)
        REFERENCES users(id),

    CONSTRAINT chk_file_metadata_file_size
        CHECK (file_size > 0),

    CONSTRAINT chk_file_metadata_status
        CHECK (
            status IN (
                'ACTIVE',
                'DELETED',
                'REJECTED'
            )
        )
);

CREATE INDEX idx_file_metadata_customer_id
    ON file_metadata(customer_id);

CREATE INDEX idx_file_metadata_uploaded_by_user_id
    ON file_metadata(uploaded_by_user_id);

CREATE INDEX idx_file_metadata_document_type
    ON file_metadata(document_type);

CREATE INDEX idx_file_metadata_status
    ON file_metadata(status);

CREATE INDEX idx_file_metadata_checksum
    ON file_metadata(checksum);