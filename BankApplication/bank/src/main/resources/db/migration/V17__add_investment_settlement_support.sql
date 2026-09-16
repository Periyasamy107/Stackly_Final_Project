ALTER TABLE investments
    ADD COLUMN settlement_reference VARCHAR(100) NULL,
    ADD COLUMN settled_at DATETIME NULL,
    ADD COLUMN settlement_account_id BIGINT NULL;

ALTER TABLE investments
    ADD CONSTRAINT uq_investments_settlement_reference
        UNIQUE (settlement_reference);

ALTER TABLE investments
    ADD CONSTRAINT fk_investments_settlement_account
        FOREIGN KEY (settlement_account_id)
        REFERENCES accounts (id);