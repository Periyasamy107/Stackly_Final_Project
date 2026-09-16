ALTER TABLE transactions
    ADD CONSTRAINT chk_transactions_type
    CHECK (
        transaction_type IN (
            'DEPOSIT',
            'WITHDRAWAL',
            'TRANSFER_DEBIT',
            'TRANSFER_CREDIT',
            'LOAN_DISBURSEMENT',
            'LOAN_REPAYMENT',
            'INVESTMENT_SETTLEMENT',
            'REVERSED'
        )
    );