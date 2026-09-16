CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    event_id VARCHAR(100) NOT NULL,

    notification_type VARCHAR(50) NOT NULL,

    title VARCHAR(150) NOT NULL,

    message VARCHAR(1000) NOT NULL,

    reference_type VARCHAR(50),

    reference_id BIGINT,

    read_at DATETIME NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_notifications_event_id
        UNIQUE (event_id),

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT chk_notifications_type
        CHECK (
            notification_type IN (
                'CUSTOMER_CREATED',
                'LOAN_APPROVED',
                'TRANSACTION_COMPLETED',
                'INVESTMENT_MATURED'
            )
        )
);

CREATE INDEX idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX idx_notifications_user_read
    ON notifications(user_id, read_at);

CREATE INDEX idx_notifications_created_at
    ON notifications(created_at);