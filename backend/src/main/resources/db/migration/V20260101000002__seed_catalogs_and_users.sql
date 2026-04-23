-- Categories
INSERT INTO categories (code, name) VALUES
  ('SPORTS',  'Sports'),
  ('FINANCE', 'Finance'),
  ('MOVIES',  'Movies');

-- Channels
INSERT INTO channels (code, name) VALUES
  ('SMS',   'SMS'),
  ('EMAIL', 'Email'),
  ('PUSH',  'Push');

-- Users
INSERT INTO users (name, email, phone_number) VALUES
  ('Ana',   'ana@example.com',   '+525511111111'),
  ('Beto',  'beto@example.com',  '+525522222222'),
  ('Carla', 'carla@example.com', '+525533333333'),
  ('Diego', 'diego@example.com', '+525544444444'),
  ('Elena', 'elena@example.com', '+525555555555');

-- User ↔ Category subscriptions
INSERT INTO user_categories (user_id, category_id)
SELECT u.id, c.id
FROM (VALUES
  ('ana@example.com',   'SPORTS'),
  ('ana@example.com',   'FINANCE'),
  ('beto@example.com',  'MOVIES'),
  ('carla@example.com', 'SPORTS'),
  ('carla@example.com', 'MOVIES'),
  ('diego@example.com', 'FINANCE')
  -- Elena: no subscriptions (edge case)
) AS seed(email, category_code)
JOIN users u       ON u.email = seed.email
JOIN categories c  ON c.code  = seed.category_code;

-- User ↔ Channel preferences
INSERT INTO user_channels (user_id, channel_id)
SELECT u.id, ch.id
FROM (VALUES
  ('ana@example.com',   'EMAIL'),
  ('ana@example.com',   'PUSH'),
  ('beto@example.com',  'SMS'),
  ('carla@example.com', 'EMAIL'),
  ('carla@example.com', 'SMS'),
  ('carla@example.com', 'PUSH'),
  ('diego@example.com', 'EMAIL'),
  ('elena@example.com', 'EMAIL')
) AS seed(email, channel_code)
JOIN users u       ON u.email = seed.email
JOIN channels ch   ON ch.code = seed.channel_code;
