ALTER TABLE auto_categorize_rules
ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE last_rule_applied
ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE auto_categorize_rules
ADD UNIQUE (user_id, ordinal);

ALTER TABLE last_rule_applied
ADD UNIQUE (user_id, rule_id, transaction_id);