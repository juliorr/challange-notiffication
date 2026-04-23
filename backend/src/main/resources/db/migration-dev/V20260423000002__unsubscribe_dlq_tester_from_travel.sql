-- Forward-only fix for the previous dev seed: V20260423000001 subscribed
-- "DLQ Tester" to every existing category via CROSS JOIN, which accidentally
-- included TRAVEL. TRAVEL is seeded by V20260101000003 as an intentionally
-- empty category (no subscribers) to exercise the "empty fanout" path, so we
-- remove that specific subscription here to restore the invariant.
DELETE FROM user_categories uc
USING users u, categories c
WHERE uc.user_id = u.id
  AND uc.category_id = c.id
  AND u.email = 'dlq+fail@test.local'
  AND c.code = 'TRAVEL';
