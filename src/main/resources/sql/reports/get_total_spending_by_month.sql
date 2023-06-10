SELECT DATE_TRUNC('month', expense_date) AS month, SUM(amount) as total
FROM transactions_view
WHERE user_id = :userId
{{#excludeCategoryIds}}
AND (category_id IS NULL OR category_id NOT IN (:categoryIds))
{{/excludeCategoryIds}}
{{#includeCategoryIds}}
AND category_id IN (:categoryIds)
{{/includeCategoryIds}}
GROUP BY month
ORDER BY month DESC
OFFSET :offset
LIMIT :limit;