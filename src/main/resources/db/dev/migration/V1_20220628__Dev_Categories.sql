INSERT INTO categories (id, user_id, name, created, updated, version)
VALUES (gen_random_uuid(), 1, 'Groceries', now(), now(), 1),
       (gen_random_uuid(), 1, 'Entertainment', now(), now(), 1),
       (gen_random_uuid(), 1, 'Restaurants', now(), now(), 1),
       (gen_random_uuid(), 1, 'Travel', now(), now(), 1);