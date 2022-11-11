SELECT :theDate AS month, category_name, SUM(amount) AS amount
FROM transactions_view
WHERE expense_date >= DATE_TRUNC('month', :theDate::date)
AND expense_date <= (DATE_TRUNC('month', :theDate::date) + interval '1 month - 1 day')
AND user_id = :userId
GROUP BY category_name
ORDER BY category_name ASC;