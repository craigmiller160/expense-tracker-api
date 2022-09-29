INSERT
    INTO
        public.transactions(
            id,
            user_id,
            expense_date,
            description,
            amount,
            category_id,
            confirmed,
            created,
            updated,
            version
        )
    VALUES(
        gen_random_uuid(),
        1,
        NOW(),
        'DIRECTPAY FULL BALANCESEE DETAILS OF YOUR NEXT DIRECTPAY BELOW',
        1928.54,
        NULL,
        FALSE,
        '2022-07-02 18:55:13.824209',
        '2022-07-02 18:55:13.824209',
        1
    );