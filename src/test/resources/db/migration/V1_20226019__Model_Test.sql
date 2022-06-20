CREATE TABLE countries (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE residents (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    version NUMERIC NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);