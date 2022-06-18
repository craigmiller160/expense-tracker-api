CREATE TYPE company AS ENUM ('DISCOVER')
CREATE TYPE file_format AS ENUM ('CSV');

CREATE TABLE transaction_import_types (
    id UUID NOT NULL,
    company company NOT NULL,
    file_format file_format NOT NULL,
    created TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);