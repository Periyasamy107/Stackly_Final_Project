CREATE TABLE loan_repayments (
    id BIGINT NOT NULL AUTO_INCREMENT,

    loan_id BIGINT NOT NULL,

    account_id BIGINT NOT NULL,

    transaction_reference VARCHAR(50) NOT NULL,

    amount DECIMAL(19,4) NOT NULL,

    repayment_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',

    description VARCHAR(500),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_loan_repayments_transaction_reference
        UNIQUE (transaction_reference),

    CONSTRAINT fk_loan_repayments_loan
        FOREIGN KEY (loan_id)
        REFERENCES loans(id),

    CONSTRAINT fk_loan_repayments_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_loan_repayments_amount
        CHECK (amount > 0.0000),

    CONSTRAINT chk_loan_repayments_status
        CHECK (
            status IN (
                'COMPLETED',
                'REVERSED'
            )
        )
);

CREATE INDEX idx_loan_repayments_loan_id
    ON loan_repayments(loan_id);

CREATE INDEX idx_loan_repayments_account_id
    ON loan_repayments(account_id);

CREATE INDEX idx_loan_repayments_repayment_date
    ON loan_repayments(repayment_date);

CREATE INDEX idx_loan_repayments_status
    ON loan_repayments(status);