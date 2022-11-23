CREATE VIEW auto_categorize_rules_view AS
SELECT acr.id, acr.user_id, acr.category_id, acr.ordinal, acr.regex,
    acr.start_date, acr.end_date, acr.min_amount, acr.max_amount,
    c.name AS category_name
FROM auto_categorize_rules acr
JOIN categories c ON acr.category_id = c.id;