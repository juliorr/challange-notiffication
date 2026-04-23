-- ===== Catalogs =====

CREATE TABLE categories (
  id         SERIAL PRIMARY KEY,
  code       VARCHAR(32) NOT NULL UNIQUE,
  name       VARCHAR(64) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE channels (
  id         SERIAL PRIMARY KEY,
  code       VARCHAR(32) NOT NULL UNIQUE,
  name       VARCHAR(64) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ===== Users =====

CREATE TABLE users (
  id           SERIAL PRIMARY KEY,
  name         VARCHAR(128) NOT NULL,
  email        VARCHAR(256) NOT NULL UNIQUE,
  phone_number VARCHAR(32)  NOT NULL,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE user_categories (
  user_id     INT NOT NULL REFERENCES users(id)      ON DELETE CASCADE,
  category_id INT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, category_id)
);

CREATE INDEX idx_user_categories_category ON user_categories(category_id);

CREATE TABLE user_channels (
  user_id    INT NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
  channel_id INT NOT NULL REFERENCES channels(id) ON DELETE RESTRICT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, channel_id)
);

-- ===== Messages =====

CREATE TABLE messages (
  id          BIGSERIAL PRIMARY KEY,
  category_id INT       NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
  body        TEXT      NOT NULL CHECK (char_length(body) > 0),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_messages_category_created ON messages(category_id, created_at DESC);

-- ===== Notifications (log) =====

CREATE TYPE notification_status AS ENUM
  ('PENDING', 'SENDING', 'SENT', 'FAILED', 'DEAD_LETTER');

CREATE TABLE notifications (
  id              BIGSERIAL PRIMARY KEY,
  message_id      BIGINT NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
  user_id         INT    NOT NULL REFERENCES users(id)    ON DELETE RESTRICT,
  channel_id      INT    NOT NULL REFERENCES channels(id) ON DELETE RESTRICT,
  status          notification_status NOT NULL DEFAULT 'PENDING',
  attempts        INT    NOT NULL DEFAULT 0,
  last_error      TEXT,
  payload         JSONB  NOT NULL,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  first_sent_at   TIMESTAMPTZ,
  last_attempt_at TIMESTAMPTZ
);

CREATE INDEX idx_notifications_created_desc ON notifications(created_at DESC);
CREATE INDEX idx_notifications_status       ON notifications(status) WHERE status IN ('PENDING','FAILED');
CREATE INDEX idx_notifications_message      ON notifications(message_id);
CREATE INDEX idx_notifications_user         ON notifications(user_id);
