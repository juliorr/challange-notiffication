import type { NotificationStatus, NotificationView } from "../api/types";
import { formatNotificationDate } from "../utils/formatDate";
import { cellEllipsis } from "../styles/tokens";
import { StatusBadge } from "./StatusBadge";

export interface NotificationRowProps {
  status: NotificationStatus;
  channel: string;
  category: string;
  messageBody: string;
  userName: string;
  userEmail: string;
  attempts: number;
  lastError: string | null;
  createdAt: string;
}

export function NotificationRow(props: NotificationRowProps) {
  const {
    status,
    channel,
    category,
    messageBody,
    userName,
    userEmail,
    attempts,
    lastError,
    createdAt,
  } = props;
  return (
    <tr>
      <td><StatusBadge status={status} /></td>
      <td>{channel}</td>
      <td>{category}</td>
      <td title={messageBody} style={cellEllipsis}>{messageBody}</td>
      <td>{userName} ({userEmail})</td>
      <td>{attempts}</td>
      <td title={lastError ?? ""}>{lastError ? "⚠" : ""}</td>
      <td title={createdAt}>{formatNotificationDate(createdAt)}</td>
    </tr>
  );
}

export function fromNotificationView(n: NotificationView): NotificationRowProps {
  return {
    status: n.status,
    channel: n.channel,
    category: n.category,
    messageBody: n.messageBody,
    userName: n.user.name,
    userEmail: n.user.email,
    attempts: n.attempts,
    lastError: n.lastError,
    createdAt: n.createdAt,
  };
}
