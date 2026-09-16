CREATE TABLE accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,

    customer_id BIGINT NOT NULL,

    account_number VARCHAR(30) NOT NULL,

    account_type VARCHAR(30) NOT NULL,

    balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_accounts_account_number
        UNIQUE (account_number),

    CONSTRAINT fk_accounts_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT chk_accounts_balance
        CHECK (balance >= 0.0000),

    CONSTRAINT chk_accounts_type
        CHECK (
            account_type IN (
                'SAVINGS',
                'CURRENT',
                'SALARY'
            )
        ),

    CONSTRAINT chk_accounts_status
        CHECK (
            status IN (
                'ACTIVE',
                'INACTIVE',
                'BLOCKED',
                'CLOSED'
            )
        )
);