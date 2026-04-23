-- New catalog entry kept intentionally without subscribers. Used by repository
-- tests to assert that findSubscribersWithChannelsByCategoryCode returns an
-- empty list for a real-but-unsubscribed category.
INSERT INTO categories (code, name) VALUES
  ('TRAVEL', 'Travel');
