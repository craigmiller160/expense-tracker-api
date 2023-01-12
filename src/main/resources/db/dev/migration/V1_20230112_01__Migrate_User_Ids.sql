UPDATE transactions
SET user_id = '2e0eccce-1f64-4d13-a269-fda027f5ddee'
WHERE legacy_user_id = 1;

ALTER TABLE transactions
DROP COLUMN legacy_user_id;

ALTER TABLE transactions
ALTER COLUMN user_id SET NOT NULL;