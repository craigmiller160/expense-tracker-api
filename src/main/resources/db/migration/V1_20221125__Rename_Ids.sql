ALTER TABLE auto_categorize_rules
RENAME COLUMN id TO uid;

ALTER TABLE categories
RENAME COLUMN id TO uid;

ALTER TABLE last_rule_applied
RENAME COLUMN id TO uid;

ALTER TABLE transactions
RENAME COLUMN id TO uid;

ALTER TABLE auto_categorize_rules_view
RENAME COLUMN id TO uid;

ALTER TABLE transactions_view
RENAME COLUMN id TO uid;