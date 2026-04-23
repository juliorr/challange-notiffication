-- Dev-only seed: "DLQ Tester" user wired to every category and every channel.
-- Because the email contains the '+fail@' sub-addressing marker, EmailMockSender
-- returns a permanent failure for this recipient. After max-attempts retries the
-- EMAIL notification for this user lands in DEAD_LETTER, while its SMS and PUSH
-- counterparts succeed — exercising the full fan-out + retry + DLQ path end-to-end.
--
-- This migration only runs when `spring.flyway.locations` includes
-- `classpath:db/migration-dev`, which is wired in `application-dev.yml`. Other
-- profiles (prod, test) keep the default `classpath:db/migration` and never
-- insert this user.

INSERT INTO users (name, email, phone_number) VALUES
  ('DLQ Tester', 'dlq+fail@test.local', '+525599999999');

INSERT INTO user_categories (user_id, category_id)
SELECT u.id, c.id
FROM users u
CROSS JOIN categories c
WHERE u.email = 'dlq+fail@test.local';

INSERT INTO user_channels (user_id, channel_id)
SELECT u.id, ch.id
FROM users u
CROSS JOIN channels ch
WHERE u.email = 'dlq+fail@test.local';
