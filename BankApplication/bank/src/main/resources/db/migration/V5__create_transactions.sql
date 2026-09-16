CREATE TABLE transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,

    account_id BIGINT NOT NULL,

    transaction_reference VARCHAR(50) NOT NULL,

    transaction_type VARCHAR(30) NOT NULL,

    amount DECIMAL(19,4) NOT NULL,

    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    description VARCHAR(500),

    counterparty_account VARCHAR(30),

    transaction_status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_transactions_reference
        UNIQUE (transaction_reference),

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_transactions_amount
        CHECK (amount > 0.0000),

    CONSTRAINT chk_transactions_type
        CHECK (
            transaction_type IN (
                'DEPOSIT',
                'WITHDRAWAL',
                'TRANSFER_DEBIT',
                'TRANSFER_CREDIT'
            )
        ),

    CONSTRAINT chk_transactions_status
        CHECK (
            transaction_status IN (
                'PENDING',
                'COMPLETED',
                'FAILED',
                'REVERSED'
            )
        )
);

CREATE INDEX idx_transactions_account_id
    ON transactions(account_id);

CREATE INDEX idx_transactions_transaction_date
    ON transactions(transaction_date);

CREATE INDEX idx_transactions_status
    ON transactions(transaction_status);