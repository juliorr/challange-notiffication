import { useCallback, useState } from "react";
import { api } from "../api/client";
import type { NotificationView } from "../api/types";
import { fromNotificationView, NotificationRow } from "../components/NotificationRow";
import { PAGE_SIZE } from "../config";
import { useNotificationStream } from "../hooks/useNotificationStream";
import { usePagination } from "../hooks/usePagination";
import {
  banner,
  btn,
  emptyCell,
  errorText,
  mutedText,
  pager,
  table,
  th,
} from "../styles/tokens";

export function Logs() {
  const [pendingNew, setPendingNew] = useState<number>(0);

  const fetchPage = useCallback(async (cursor: string | null) => {
    const res = await api.listNotifications({
      limit: PAGE_SIZE,
      cursor: cursor ?? undefined,
    });
    return { items: res.items, nextCursor: res.nextCursor };
  }, []);

  const resetPending = useCallback(() => setPendingNew(0), []);

  const pg = usePagination<NotificationView>(fetchPage, resetPending);

  const handleStreamEvent = useCallback(
    (incoming: NotificationView) => {
      const replaced = pg.replaceItem((n) => n.id === incoming.id, incoming);
      if (!replaced) setPendingNew((n) => n + 1);
    },
    [pg],
  );

  const { state } = useNotificationStream(handleStreamEvent);

  return (
    <section>
      <h1>Notification logs</h1>
      <p style={mutedText}>
        Live stream: <code>{state}</code>
      </p>

      {pendingNew > 0 && (
        <div style={banner}>
          {pendingNew} new notification{pendingNew === 1 ? "" : "s"} arrived.{" "}
          <button type="button" style={btn} onClick={pg.goFirst}>
            Refresh
          </button>
        </div>
      )}

      {pg.error && <p style={errorText}>{pg.error}</p>}

      <table style={table}>
        <thead>
          <tr>
            <th style={th}>Status</th>
            <th style={th}>Channel</th>
            <th style={th}>Category</th>
            <th style={th}>Message</th>
            <th style={th}>User</th>
            <th style={th}>Attempts</th>
            <th style={th}></th>
            <th style={th}>Created</th>
          </tr>
        </thead>
        <tbody>
          {pg.items.length === 0 ? (
            <tr>
              <td colSpan={8} style={emptyCell}>
                {pg.loading ? "Loading…" : "No notifications yet. Send one from the home page."}
              </td>
            </tr>
          ) : (
            pg.items.map((n) => <NotificationRow key={n.id} {...fromNotificationView(n)} />)
          )}
        </tbody>
      </table>

      <nav style={pager} aria-label="Pagination">
        <button type="button" style={btn} disabled={!pg.hasPrev || pg.loading} onClick={pg.goPrev}>
          ← Prev
        </button>
        <span style={mutedText}>
          Page <strong>{pg.page}</strong>
          {pg.loading ? " · loading…" : ""}
        </span>
        <button type="button" style={btn} disabled={!pg.hasNext || pg.loading} onClick={pg.goNext}>
          Next →
        </button>
      </nav>
    </section>
  );
}
