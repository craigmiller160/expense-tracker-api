CREATE INDEX transactions_category_id_idx ON transactions (category_id);
CREATE INDEX transactions_expense_date_idx ON transactions (expense_date);
CREATE INDEX transactions_amount_idx ON transactions (amount);
CREATE INDEX transactions_confirmed_idx ON transactions (confirmed);