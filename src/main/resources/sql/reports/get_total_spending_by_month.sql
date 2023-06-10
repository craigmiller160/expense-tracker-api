SELECT DATE_TRUNC('month', tv.expense_date) AS month, SUM(tv.amount) as total
FROM transactions_view tv
WHERE tv.user_id = :userId
AND is_report_category_allowed(tv.category_id, :categoryIdType::report_category_filter_type, ARRAY[:categoryIds]::UUID[])
GROUP BY month
ORDER BY month DESC
OFFSET :offset
LIMIT :limit;