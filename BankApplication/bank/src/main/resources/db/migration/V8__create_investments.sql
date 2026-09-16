CREATE TABLE investments (
    id BIGINT NOT NULL AUTO_INCREMENT,

    customer_id BIGINT NOT NULL,

    account_id BIGINT NOT NULL,

    investment_type VARCHAR(30) NOT NULL,

    amount DECIMAL(19,4) NOT NULL,

    start_date DATE NOT NULL,

    maturity_date DATE NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    description VARCHAR(500),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT fk_investments_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_investments_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_investments_amount
        CHECK (amount > 0.0000),

    CONSTRAINT chk_investments_dates
        CHECK (maturity_date >= start_date),

    CONSTRAINT chk_investments_status
        CHECK (
            status IN (
                'ACTIVE',
                'MATURED',
                'CLOSED',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_investments_customer_id
    ON investments(customer_id);

CREATE INDEX idx_investments_account_id
    ON investments(account_id);

CREATE INDEX idx_investments_status
    ON investments(status);

CREATE INDEX idx_investments_maturity_date
    ON investments(maturity_date);