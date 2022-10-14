ALTER TABLE transactions
ADD COLUMN mark_not_duplicate_nano BIGINT;

DROP TRIGGER set_content_hash_on_change ON transactions;
DROP FUNCTION set_content_hash_on_change();
DROP FUNCTION txn_content_hash(user_id BIGINT, expense_date DATE, amount NUMERIC, description VARCHAR(255));

CREATE FUNCTION txn_content_hash (user_id BIGINT, expense_date DATE, amount NUMERIC,
    description VARCHAR(255), mark_not_duplicate_nano BIGINT)
RETURNS VARCHAR AS
$$
    BEGIN
        RETURN ENCODE(DIGEST(CONCAT(user_id, expense_date, amount, description, mark_not_duplicate_nano), 'sha256'), 'hex');
    END;
$$ LANGUAGE plpgsql;

UPDATE transactions
SET content_hash = txn_content_hash(user_id, expense_date, amount, description, mark_not_duplicate_nano);

CREATE FUNCTION set_content_hash_on_change()
RETURNS TRIGGER AS
$$
    BEGIN
        new.content_hash := txn_content_hash(new.user_id, new.expense_date, new.amount, new.description, new.mark_not_duplicate_nano);
        RETURN new;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_content_hash_on_change
BEFORE INSERT OR UPDATE ON transactions
FOR EACH ROW
EXECUTE PROCEDURE set_content_hash_on_change();