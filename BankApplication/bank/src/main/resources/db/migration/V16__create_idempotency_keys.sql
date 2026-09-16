CREATE TABLE api_idempotency_keys (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    idempotency_key VARCHAR(100) NOT NULL,

    request_hash CHAR(64) NOT NULL,

    status VARCHAR(30) NOT NULL,

    http_status INT,

    response_body LONGTEXT,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    completed_at DATETIME NULL,

    expires_at DATETIME NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_api_idempotency_user_key
        UNIQUE (user_id, idempotency_key),

    CONSTRAINT fk_api_idempotency_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_api_idempotency_status
        CHECK (
            status IN (
                'IN_PROGRESS',
                'COMPLETED'
            )
        )
);

CREATE INDEX idx_api_idempotency_expires_at
    ON api_idempotency_keys(expires_at);

CREATE INDEX idx_api_idempotency_status
    ON api_idempotency_keys(status);