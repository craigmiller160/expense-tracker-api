SELECT COUNT(DISTINCT DATE_TRUNC('month', expense_date))
FROM transactions_view
WHERE user_id = :userId