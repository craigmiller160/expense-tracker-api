CREATE TABLE countries (
    uid UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL,
    PRIMARY KEY (uid)
);

CREATE TABLE residents (
    uid UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    version NUMERIC NOT NULL DEFAULT 1,
    PRIMARY KEY (uid)
);