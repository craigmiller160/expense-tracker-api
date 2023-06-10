SELECT COUNT(DISTINCT DATE_TRUNC('month', tv.expense_date))
FROM transactions_view tv
WHERE tv.user_id = :userId
AND CASE
    WHEN :categoryIdType = 'INCLUDE' AND :unknownCategoryType = 'INCLUDE' THEN (
        tv.category_id IN (:categoryIds) OR tv.category_id IS NULL
    )
    WHEN :categoryIdType = 'INCLUDE' AND :unknownCategoryType <> 'INCLUDE' THEN (
        tv.category_id IN (:categoryIds)
    )
    WHEN :categoryIdType = 'EXCLUDE' AND :unknownCategoryType = 'EXCLUDE' THEN (
        tv.category_id NOT IN (:categoryIds) AND tv.category_id IS NOT NULL
    )
    WHEN :categoryIdType = 'EXCLUDE' AND :unknownCategoryType <> 'EXCLUDE' THEN (
        tv.category_id NOT IN (:categoryIds) OR tv.category_id IS NULL
    )
    ELSE true = true
END;