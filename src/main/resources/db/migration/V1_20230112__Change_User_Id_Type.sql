-- Transaction, AutoCategorizeRule, AutoCategorizeRuleView, Category, LastRuleApplied, TransactionView

ALTER TABLE transactions
RENAME COLUMN user_id TO legacy_user_id;

ALTER TABLE transactions
ADD COLUMN user_id UUID;