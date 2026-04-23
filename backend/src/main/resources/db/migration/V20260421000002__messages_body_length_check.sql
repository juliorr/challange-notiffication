ALTER TABLE messages
  DROP CONSTRAINT messages_body_check;

ALTER TABLE messages
  ADD CONSTRAINT messages_body_length_check
    CHECK (char_length(body) BETWEEN 1 AND 4000);
