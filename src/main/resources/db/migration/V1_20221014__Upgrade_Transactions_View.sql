DROP VIEW transactions_view;

CREATE VIEW transactions_view AS
SELECT t1.id, t1.user_id, t1.expense_date,
t1.description, t1.amount, t1.category_id,
t1.confirmed, c.name AS category_name,
t1.content_hash,
EXISTS(
    SELECT t2.*
    FROM transactions t2
    WHERE t1.id != t2.id
    AND t1.content_hash = t2.content_hash
) AS duplicate
FROM transactions t1
LEFT JOIN categories c ON t1.category_id = c.id;