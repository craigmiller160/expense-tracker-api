CREATE FUNCTION txn_content_hash (user_id BIGINT, expense_date DATE, amount NUMERIC, description VARCHAR(255))
RETURNS VARCHAR AS
$$
    BEGIN
        RETURN ENCODE(DIGEST(CONCAT(user_id, expense_date, amount, description), 'sha256'), 'hex');
    END;
$$ LANGUAGE plpgsql;

ALTER TABLE transactions
ADD COLUMN content_hash VARCHAR(255);

ALTER TABLE transactions
DROP COLUMN duplicate;

UPDATE transactions
SET content_hash = txn_content_hash(user_id, expense_date, amount, description);

ALTER TABLE transactions
ALTER COLUMN content_hash SET NOT NULL;

CREATE INDEX transaction_content_hash_idx ON transactions(content_hash);

CREATE FUNCTION set_content_hash_on_change()
RETURNS TRIGGER AS
$$
    BEGIN
        new.content_hash := txn_content_hash(new.user_id, new.expense_date, new.amount, new.description);
        RETURN new;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_content_hash_on_change
BEFORE INSERT OR UPDATE ON transactions
FOR EACH ROW
EXECUTE PROCEDURE set_content_hash_on_change();