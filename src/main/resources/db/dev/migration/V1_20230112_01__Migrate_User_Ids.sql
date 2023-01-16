UPDATE transactions
SET user_id = '2e0eccce-1f64-4d13-a269-fda027f5ddee'
WHERE legacy_user_id = 1;

ALTER TABLE transactions
DROP COLUMN legacy_user_id;

ALTER TABLE transactions
ALTER COLUMN user_id SET NOT NULL;

UPDATE auto_categorize_rules
SET user_id = '2e0eccce-1f64-4d13-a269-fda027f5ddee'
WHERE legacy_user_id = 1;

ALTER TABLE auto_categorize_rules
DROP COLUMN legacy_user_id;

ALTER TABLE auto_categorize_rules
ALTER COLUMN user_id SET NOT NULL;

UPDATE categories
SET user_id = '2e0eccce-1f64-4d13-a269-fda027f5ddee'
WHERE legacy_user_id = 1;

ALTER TABLE categories
DROP COLUMN legacy_user_id;

ALTER TABLE categories
ALTER COLUMN user_id SET NOT NULL;

UPDATE last_rule_applied
SET user_id = '2e0eccce-1f64-4d13-a269-fda027f5ddee'
WHERE legacy_user_id = 1;

ALTER TABLE last_rule_applied
DROP COLUMN legacy_user_id;

ALTER TABLE last_rule_applied
ALTER COLUMN user_id SET NOT NULL;