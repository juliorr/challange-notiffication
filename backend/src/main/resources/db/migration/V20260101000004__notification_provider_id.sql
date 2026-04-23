-- Persist the provider-assigned ID returned by ChannelSender.send() so that a
-- successful delivery can be cross-checked against the upstream provider
-- (Twilio / SendGrid / FCM in production; mock UUIDs today).
ALTER TABLE notifications
  ADD COLUMN provider_id VARCHAR(128);

-- Protect the audit trail: a notification row must survive deletion of its
-- parent message. Forward-only: drop the CASCADE FK and recreate as RESTRICT.
ALTER TABLE notifications
  DROP CONSTRAINT notifications_message_id_fkey;

ALTER TABLE notifications
  ADD CONSTRAINT notifications_message_id_fkey
  FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE RESTRICT;
