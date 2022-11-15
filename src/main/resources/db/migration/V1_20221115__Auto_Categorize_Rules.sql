CREATE TABLE auto_categorize_rules (
    id UUID NOT NULL,
    category_id UUID NOT NULL,
    ordinal INT NOT NULL,
    regex VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    min_amount NUMERIC,
    max_amount NUMERIC,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE unconfirmed_transaction_last_applied_rule (
    rule_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    PRIMARY KEY (rule_id, transaction_id),
    FOREIGN KEY (rule_id) REFERENCES auto_categorize_rules (id),
    FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);