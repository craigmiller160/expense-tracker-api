CREATE
    OR REPLACE PROCEDURE set_dev_transaction_dates() AS $$ DECLARE the_date DATE;

the_row transactions % rowtype;

BEGIN the_date = now();

FOR the_row IN SELECT
    *
FROM
    transactions LOOP UPDATE
        transactions
    SET
        expense_date = the_date
    WHERE
        id = the_row.id;

the_date = the_date - 1;
END LOOP;
END;

$$ LANGUAGE plpgsql;

CALL set_dev_transaction_dates();

DROP
    PROCEDURE set_dev_transaction_dates();