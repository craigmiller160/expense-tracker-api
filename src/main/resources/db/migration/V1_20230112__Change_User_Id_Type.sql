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

DROP VIEW auto_categorize_rules_view;
CREATE VIEW auto_categorize_rules_view(uid, user_id, category_id, ordinal, regex, start_date, end_date, min_amount, max_amount, category_name) AS
	SELECT acr.uid,
    acr.user_id,
    acr.category_id,
    acr.ordinal,
    acr.regex,
    acr.start_date,
    acr.end_date,
    acr.min_amount,
    acr.max_amount,
    c.name AS category_name
   FROM auto_categorize_rules acr
     JOIN categories c ON acr.category_id = c.uid;

DROP VIEW transactions_view;
CREATE VIEW transactions_view(uid, user_id, expense_date, description, amount, category_id, confirmed, category_name, content_hash, created, updated, duplicate) AS
	SELECT t1.uid,
    t1.user_id,
    t1.expense_date,
    t1.description,
    t1.amount,
    t1.category_id,
    t1.confirmed,
    c.name AS category_name,
    t1.content_hash,
    t1.created,
    t1.updated,
    (EXISTS ( SELECT t2.uid AS id,
            t2.user_id,
            t2.expense_date,
            t2.description,
            t2.amount,
            t2.category_id,
            t2.confirmed,
            t2.created,
            t2.updated,
            t2.version,
            t2.content_hash,
            t2.mark_not_duplicate_nano
           FROM transactions t2
          WHERE t1.uid <> t2.uid AND t1.content_hash::text = t2.content_hash::text)) AS duplicate
   FROM transactions t1
     LEFT JOIN categories c ON t1.category_id = c.uid;

