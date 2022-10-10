SELECT 'UNCONFIRMED' AS TYPE, COUNT(t) AS COUNT
FROM transactions t
WHERE t.user_id = :userId
AND t.confirmed = FALSE
UNION
SELECT 'DUPLICATE' AS TYPE, COUNT(t) AS COUNT
FROM transactions_view t
WHERE t.user_id = :userId
AND t.duplicate = TRUE
UNION
SELECT 'UNCATEGORIZED' AS TYPE, COUNT(t) AS COUNT
FROM transactions t
WHERE t.user_id =: userId
AND t.category_id IS NULL
UNION
SELECT 'POSSIBLE_REFUND' AS TYPE, COUNT(t) AS COUNT
FROM transactions t
WHERE t.user_id = :userId
AND t.amount > 0;