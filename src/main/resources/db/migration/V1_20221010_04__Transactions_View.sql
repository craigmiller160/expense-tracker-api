CREATE VIEW transactions_view AS
SELECT t1.*, EXISTS(
    SELECT t2.*
    FROM transactions t2
    WHERE t1.id != t2.id
    AND t1.content_hash = t2.content_hash
) AS duplicate
FROM transactions t1;