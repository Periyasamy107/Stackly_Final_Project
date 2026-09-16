CREATE TABLE investment_performance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    investment_id BIGINT NOT NULL,
    performance_date DATE NOT NULL,
    invested_amount DECIMAL(19,4) NOT NULL,
    current_value DECIMAL(19,4) NOT NULL,
    profit_loss_amount DECIMAL(19,4) NOT NULL,
    return_percentage DECIMAL(19,8) NOT NULL,
    notes VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_investment_performance_investment
        FOREIGN KEY (investment_id)
        REFERENCES investments(id),

    CONSTRAINT uk_investment_performance_date
        UNIQUE (investment_id, performance_date),

    CONSTRAINT chk_investment_performance_invested_amount
        CHECK (invested_amount > 0.0000),

    CONSTRAINT chk_investment_performance_current_value
        CHECK (current_value > 0.0000),

    CONSTRAINT chk_investment_performance_return_percentage
        CHECK (return_percentage >= -100.00000000)
);

CREATE INDEX idx_investment_performance_investment_id
    ON investment_performance(investment_id);

CREATE INDEX idx_investment_performance_date
    ON investment_performance(performance_date);