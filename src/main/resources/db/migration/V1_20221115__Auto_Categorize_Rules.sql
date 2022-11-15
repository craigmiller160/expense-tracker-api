CREATE TABLE auto_categorize_rules (
    id UUID NOT NULL,
    category_id UUID NOT NULL,
    ordinal INT NOT NULL,
    regex VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    min_amount NUMERIC,
    max_amount NUMERIC,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    version NUMERIC NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE last_rule_applied (
    id UUID NOT NULL,
    rule_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    created TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (rule_id) REFERENCES auto_categorize_rules (id),
    FOREIGN KEY (transaction_id) REFERENCES transactions (id)
);