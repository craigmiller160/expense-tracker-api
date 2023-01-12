ALTER TABLE transactions
DROP COLUMN legacy_user_id;

ALTER TABLE transactions
ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE auto_categorize_rules
DROP COLUMN legacy_user_id;

ALTER TABLE auto_categorize_rules
ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE categories
DROP COLUMN legacy_user_id;

ALTER TABLE categories
ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE last_rule_applied
DROP COLUMN legacy_user_id;

ALTER TABLE last_rule_applied
ALTER COLUMN user_id SET NOT NULL;