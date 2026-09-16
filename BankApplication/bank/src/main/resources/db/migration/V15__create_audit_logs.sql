-- ============================================================
-- JPA actor auditing for mutable business entities
-- ============================================================

ALTER TABLE users
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE customers
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE employees
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE accounts
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE loans
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE investments
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE file_metadata
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);


-- ============================================================
-- Audit log
-- ============================================================

CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,

    event_id VARCHAR(100) NOT NULL,

    user_id BIGINT NULL,

    username VARCHAR(100),

    role VARCHAR(30),

    action VARCHAR(40) NOT NULL,

    entity_type VARCHAR(100),

    entity_id BIGINT,

    service_name VARCHAR(150) NOT NULL,

    method_name VARCHAR(150) NOT NULL,

    http_method VARCHAR(20),

    request_uri VARCHAR(1000),

    success BOOLEAN NOT NULL,

    error_type VARCHAR(200),

    error_message VARCHAR(1000),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_by VARCHAR(100),

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    updated_by VARCHAR(100),

    PRIMARY KEY (id),

    CONSTRAINT uk_audit_logs_event_id
        UNIQUE (event_id),

    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_user_id
    ON audit_logs(user_id);

CREATE INDEX idx_audit_logs_action
    ON audit_logs(action);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs(created_at);

CREATE INDEX idx_audit_logs_success
    ON audit_logs(success);