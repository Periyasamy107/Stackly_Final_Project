ALTER TABLE transactions
    ADD CONSTRAINT chk_transactions_type CHECK (
        transaction_type IN (
            'DEPOSIT',
            'WITHDRAWAL',
            'TRANSFER_DEBIT',
            'TRANSFER_CREDIT',
            'LOAN_DISBURSEMENT'
        )
    );

ALTER TABLE loans
    ADD COLUMN disbursement_account_id BIGINT NULL,
    ADD COLUMN disbursement_transaction_reference VARCHAR(50) NULL,
    ADD COLUMN disbursed_at DATETIME NULL;

ALTER TABLE loans
    ADD CONSTRAINT fk_loans_disbursement_account
        FOREIGN KEY (disbursement_account_id)
        REFERENCES accounts(id);

ALTER TABLE loans
    ADD CONSTRAINT uk_loans_disbursement_transaction_reference
        UNIQUE (disbursement_transaction_reference);

CREATE INDEX idx_loans_disbursement_account_id
    ON loans(disbursement_account_id);

CREATE INDEX idx_loans_disbursed_at
    ON loans(disbursed_at);