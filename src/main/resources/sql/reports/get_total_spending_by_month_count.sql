SELECT COUNT(DISTINCT DATE_TRUNC('month', expense_date))
FROM transactions_view
WHERE user_id = :userId
AND CASE
    WHEN :categoryIdType = 'INCLUDE' THEN category_id IN (:categoryIds)
    WHEN :categoryIdType = 'EXCLUDE' THEN (category_id IS NULL OR category_id NOT IN (:categoryIds))
    ELSE true = true
END;