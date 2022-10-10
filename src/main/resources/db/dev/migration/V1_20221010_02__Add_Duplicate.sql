INSERT INTO transactions (id, user_id, expense_date, description, amount, category_id, confirmed, created, updated, version)
SELECT gen_random_uuid(), user_id, expense_date, description, amount, category_id, confirmed, created, updated, 1
FROM transactions
LIMIT 1;