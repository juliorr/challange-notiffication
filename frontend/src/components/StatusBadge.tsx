import type { NotificationStatus } from "../api/types";

const COLORS: Record<NotificationStatus, string> = {
  PENDING:     "#9ca3af",
  SENDING:     "#3b82f6",
  SENT:        "#16a34a",
  FAILED:      "#d97706",
  DEAD_LETTER: "#dc2626",
};

export function StatusBadge({ status }: { status: NotificationStatus }) {
  return (
    <span
      style={{
        background: COLORS[status],
        color: "white",
        padding: "2px 8px",
        borderRadius: 4,
        fontSize: "0.75rem",
        fontWeight: 600,
        letterSpacing: "0.03em",
      }}
    >
      {status}
    </span>
  );
}
