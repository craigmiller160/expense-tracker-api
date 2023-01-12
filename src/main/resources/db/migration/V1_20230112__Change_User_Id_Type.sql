-- TODO do I need to migrate these? AutoCategorizeRuleView, TransactionView

ALTER TABLE transactions
RENAME COLUMN user_id TO legacy_user_id;

ALTER TABLE transactions
ADD COLUMN user_id UUID;

ALTER TABLE auto_categorize_rules
RENAME COLUMN user_id TO legacy_user_id;

ALTER TABLE auto_categorize_rules
ADD COLUMN user_id UUID;

ALTER TABLE categories
RENAME COLUMN user_id TO legacy_user_id;

ALTER TABLE categories
ADD COLUMN user_id UUID;

ALTER TABLE last_rule_applied
RENAME COLUMN user_id TO legacy_user_id;

ALTER TABLE last_rule_applied
ADD COLUMN user_id UUID;