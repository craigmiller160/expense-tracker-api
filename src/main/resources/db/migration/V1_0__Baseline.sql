CREATE TABLE categories (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE transactions (
    id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount numeric NOT NULL,
    category_id UUID,
    confirmed BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES categories (id)
)