SELECT DATE_TRUNC('month', expense_date) AS month, SUM(amount) as total
FROM transactions_view
WHERE user_id = :userId
AND CASE
    WHEN :categoryIdType = 'INCLUDE' THEN category_id IN (:categoryIds)
    WHEN :categoryIdType = 'EXCLUDE' THEN (category_id IS NULL OR category_id NOT IN (:categoryIds))
    ELSE true = true
END
GROUP BY month
ORDER BY month DESC
OFFSET :offset
LIMIT :limit;