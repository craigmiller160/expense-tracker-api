SELECT DATE_TRUNC('month', expense_date) AS month, SUM(amount) as total
FROM transactions_view
GROUP BY month
ORDER BY month DESC
OFFSET :offset
LIMIT :limit;