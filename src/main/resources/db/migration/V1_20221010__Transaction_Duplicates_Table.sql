CREATE TABLE transaction_duplicates(
    id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    new_transaction_id UUID NOT NULL,
    possible_duplicate_transaction_id UUID NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(new_transaction_id) REFERENCES transactions(id),
    FOREIGN KEY(possible_duplicate_transaction_id) REFERENCES transactions(id)
);