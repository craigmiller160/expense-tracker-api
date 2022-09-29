SELECT
    'UNCONFIRMED',
    MIN( t.expense_date )
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.confirmed = FALSE
UNION SELECT
    'DUPLICATE',
    MIN( t.expense_date )
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.duplicate = TRUE
UNION SELECT
    'UNCATEGORIZED',
    MIN( t.expense_date )
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.category_id IS NULL;