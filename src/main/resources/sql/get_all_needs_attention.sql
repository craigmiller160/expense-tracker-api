SELECT
    'UNCONFIRMED',
    COUNT( t )
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.confirmed = FALSE
UNION SELECT
    'DUPLICATE',
    COUNT( t )
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.duplicate = TRUE
UNION SELECT
    'UNCATEGORIZED',
    COUNT( t )
FROM
    transactions t
WHERE
    t.user_id =:userId
    AND t.category_id IS NULL