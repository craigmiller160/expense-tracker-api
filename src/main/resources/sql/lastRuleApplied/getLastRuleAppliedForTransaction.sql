SELECT last_rule.id, last_rule.transaction_id, last_rule.rule_id, last_rule.user_id,
    rule.ordinal, rule.category_id, rule.regex, rule.start_date, rule.end_date,
    rule.min_amount, rule.max_amount
FROM last_rule_applied last_rule
JOIN auto_categorize_rules rule ON last_rule.rule_id = rule.id
WHERE last_rule.transaction_id = :transactionId
AND last_rule.user_id = :userId