ALTER TABLE auto_categorize_rules
ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE last_rule_applied
ADD COLUMN user_id BIGINT NOT NULL;