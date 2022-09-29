SELECT
    'UNCONFIRMED' AS TYPE,
    MIN( t.expense_date ) AS oldest
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.confirmed = FALSE
UNION SELECT
    'DUPLICATE' AS TYPE,
    MIN( t.expense_date ) AS oldest
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.duplicate = TRUE
UNION SELECT
    'UNCATEGORIZED' AS TYPE,
    MIN( t.expense_date ) AS oldest
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.category_id IS NULL
UNION SELECT
    'POSSIBLE_REFUND' AS TYPE,
    MIN( t.expense_date ) AS COUNT
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.amount > 0;