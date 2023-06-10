SELECT :theDate AS month, tv.category_name, c.color, SUM(tv.amount) AS amount
FROM transactions_view tv
LEFT JOIN categories c ON tv.category_id = c.uid AND c.user_id = :userId
WHERE tv.expense_date >= DATE_TRUNC('month', :theDate::date)
AND tv.expense_date <= (DATE_TRUNC('month', :theDate::date) + interval '1 month - 1 day')
AND tv.user_id = :userId
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
END
GROUP BY tv.category_name, c.color
ORDER BY tv.category_name ASC;