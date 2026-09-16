CREATE TABLE loans (
    id BIGINT NOT NULL AUTO_INCREMENT,

    customer_id BIGINT NOT NULL,

    loan_type VARCHAR(30) NOT NULL,

    principal_amount DECIMAL(19,4) NOT NULL,

    interest_rate DECIMAL(7,4),

    start_date DATE,

    end_date DATE,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    
    rejection_reason VARCHAR(500),

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    version BIGINT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT fk_loans_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT chk_loans_principal_amount
        CHECK (principal_amount > 0.0000),

    CONSTRAINT chk_loans_interest_rate
        CHECK (
            interest_rate IS NULL
            OR interest_rate >= 0.0000
        ),

    CONSTRAINT chk_loans_dates
        CHECK (
            end_date IS NULL
            OR start_date IS NULL
            OR end_date >= start_date
        ),

    CONSTRAINT chk_loans_type
        CHECK (
            loan_type IN (
                'PERSONAL',
                'HOME',
                'EDUCATION',
                'VEHICLE',
                'BUSINESS'
            )
        ),

    CONSTRAINT chk_loans_status
        CHECK (
            status IN (
                'PENDING',
                'APPROVED',
                'ACTIVE',
                'REJECTED',
                'COMPLETED',
                'DEFAULTED'
            )
        )
);

CREATE INDEX idx_loans_customer_id
    ON loans(customer_id);

CREATE INDEX idx_loans_status
    ON loans(status);

CREATE INDEX idx_loans_start_date
    ON loans(start_date);

CREATE INDEX idx_loans_end_date
    ON loans(end_date);