CREATE SEQUENCE app_refresh_tokens_id_seq START 1;

CREATE TABLE app_refresh_tokens (
    id BIGINT NOT NULL DEFAULT nextval('app_refresh_tokens_id_seq'::regclass),
    token_id VARCHAR(255) NOT NULL UNIQUE,
    refresh_token TEXT NOT NULL,
    CONSTRAINT app_refresh_tokens_id_pk PRIMARY KEY (id)
);