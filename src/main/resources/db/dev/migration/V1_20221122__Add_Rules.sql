INSERT INTO auto_categorize_rules (category_id, id, user_id, ordinal, regex, start_date, end_date, min_amount, max_amount, created, updated, version)
SELECT id, gen_random_uuid(), 1, 1, 'Demo 1', '2022-01-01'::date, '2022-02-02'::date, 10, 20, now(), now(), 1
FROM categories
LIMIT 1;

INSERT INTO auto_categorize_rules (category_id, id, user_id, ordinal, regex, start_date, end_date, min_amount, max_amount, created, updated, version)
SELECT id, gen_random_uuid(), 1, 1, 'Demo 2', null, null, null, null, now(), now(), 1
FROM categories
LIMIT 1;

INSERT INTO auto_categorize_rules (category_id, id, user_id, ordinal, regex, start_date, end_date, min_amount, max_amount, created, updated, version)
SELECT id, gen_random_uuid(), 1, 1, 'Demo 3', '2022-01-01'::date, '2022-02-02'::date, null, null, now(), now(), 1
FROM categories
LIMIT 1;

INSERT INTO auto_categorize_rules (category_id, id, user_id, ordinal, regex, start_date, end_date, min_amount, max_amount, created, updated, version)
SELECT id, gen_random_uuid(), 1, 1, 'Demo 4', null, null, 10, 20, now(), now(), 1
FROM categories
LIMIT 1;