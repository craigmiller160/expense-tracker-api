SELECT DATE_TRUNC('month', tv.expense_date) AS month, SUM(tv.amount) as total
FROM transactions_view tv
WHERE tv.user_id = :userId
AND CASE
    WHEN :categoryIdType = 'INCLUDE' THEN tv.category_id IN (:categoryIds)
    WHEN :categoryIdType = 'EXCLUDE' THEN (tv.category_id IS NULL OR tv.category_id NOT IN (:categoryIds))
    ELSE true = true
END
AND CASE
    WHEN :unknownCategoryType = 'INCLUDE' THEN tv.category_id IS NULL
    WHEN :unknownCategoryType = 'EXCLUDE' THEN tv.category_id IS NOT NULL
    ELSE true = true
END
GROUP BY tv.month
ORDER BY tv.month DESC
OFFSET :offset
LIMIT :limit;