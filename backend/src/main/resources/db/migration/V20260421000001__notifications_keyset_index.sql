-- Composite index supporting keyset pagination ordered by (created_at, id) DESC.
-- Replaces the single-column index on created_at since the composite covers both
-- the ordering and the tie-breaker predicate used by the keyset query.

CREATE INDEX IF NOT EXISTS idx_notifications_created_id_desc
    ON notifications (created_at DESC, id DESC);

DROP INDEX IF EXISTS idx_notifications_created_desc;
