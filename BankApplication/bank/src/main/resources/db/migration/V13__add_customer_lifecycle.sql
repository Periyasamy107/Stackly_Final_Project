ALTER TABLE customers
    ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE customers
    ADD CONSTRAINT chk_customers_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE'
            )
        );

CREATE INDEX idx_customers_status
    ON customers(status);