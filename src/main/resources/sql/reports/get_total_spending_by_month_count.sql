SELECT COUNT(DISTINCT DATE_TRUNC('month', expense_date))
FROM transactions_view
WHERE user_id = :userId
{{#excludeCategoryIds}}
AND (category_id IS NULL OR category_id NOT IN (:categoryIds))
{{/excludeCategoryIds}}
{{#includeCategoryIds}}
AND (category_id IS NULL OR category_id IN (:categoryIds))
{{/includeCategoryIds}}