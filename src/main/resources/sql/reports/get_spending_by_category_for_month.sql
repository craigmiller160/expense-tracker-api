-- The efficient way to use this SQL is to repeat it for each month that needs to be retrieved, and then concatenate a UNION between each one
SELECT :theDate AS month, category_name, SUM(amount) AS amount
FROM transactions_view
WHERE expense_date >= DATE_TRUNC('month', :theDate::date)
AND expense_date <= (DATE_TRUNC('month', :theDate::date) + interval '1 month - 1 day')
GROUP BY category_name
ORDER BY category_name ASC;