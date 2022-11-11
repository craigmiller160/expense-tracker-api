-- TODO This would need to be repeated for each month, it's not efficient
SELECT category_name, SUM(amount)
FROM transactions_view
WHERE expense_date >= DATE_TRUNC('month', :theDate::date)
AND expense_date <= (DATE_TRUNC('month', :theDate::date) + interval '1 month - 1 day')
GROUP BY category_name;