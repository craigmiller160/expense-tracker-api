CREATE TABLE categories (
    id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    version NUMERIC NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE (name)
);
CREATE INDEX categories_user_id_idx ON categories (user_id);

CREATE TABLE transactions (
    id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount numeric NOT NULL,
    category_id UUID,
    confirmed BOOLEAN NOT NULL DEFAULT false,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    version NUMERIC NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES categories (id)
);
CREATE INDEX transactions_user_id_idx ON transactions (user_id);