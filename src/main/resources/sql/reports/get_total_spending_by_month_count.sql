SELECT COUNT(DISTINCT DATE_TRUNC('month', tv.expense_date))
FROM transactions_view tv
WHERE tv.user_id = :userId
AND CASE
    WHEN :categoryIdType = 'INCLUDE_NO_UNKNOWN' THEN (
        tv.category_id IN (:categoryIds)
    )
    WHEN :categoryIdType = 'INCLUDE_WITH_UNKNOWN' THEN (
        tv.category_id IN (:categoryIds) OR tv.category_id IS NULL
    )
    WHEN :categoryIdType = 'EXCLUDE_NO_UNKNOWN' THEN (
        tv.category_id NOT IN (:categoryIds) AND tv.category_id IS NOT NULL
    )
    WHEN :categoryIdType = 'EXCLUDE_WITH_UNKNOWN' THEN (
        tv.category_id NOT IN (:categoryIds) OR tv.category_id IS NULL
    )
    WHEN :categoryIdType = 'ALL_NO_UNKNOWN' THEN (
        tv.category_id IS NOT NULL
    )
    WHEN :categoryIdType = 'ALL_WITH_UNKNOWN' THEN (
        true = true
    )
    ELSE true = true
END;