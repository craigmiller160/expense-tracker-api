CREATE OR REPLACE PROCEDURE set_dev_transaction_dates ()
AS
$$
    DECLARE
        the_date DATE;
    BEGIN

    END;
$$
LANGUAGE plpgsql;

CALL set_dev_transaction_dates();

DROP PROCEDURE set_dev_transaction_dates();