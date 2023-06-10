SELECT COUNT(DISTINCT DATE_TRUNC('month', tv.expense_date))
FROM transactions_view tv
WHERE tv.user_id = :userId
AND is_report_category_allowed(tv.category_id, :categoryIdType::report_category_filter_type, :categoryIds);