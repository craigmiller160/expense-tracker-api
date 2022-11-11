SELECT :theDate AS month, tv.category_name, c.color, SUM(tv.amount) AS amount
FROM transactions_view tv
JOIN categories c ON tv.category_id = c.id AND c.user_id = :userId
WHERE tv.expense_date >= DATE_TRUNC('month', :theDate::date)
AND tv.expense_date <= (DATE_TRUNC('month', :theDate::date) + interval '1 month - 1 day')
AND tv.user_id = :userId
GROUP BY tv.category_name, c.color
ORDER BY tv.category_name ASC;