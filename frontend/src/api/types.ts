export type CatalogItem = { code: string; name: string };

export type CreateMessageResponse = { id: number; fanoutCount: number };

export type NotificationStatus =
  | "PENDING"
  | "SENDING"
  | "SENT"
  | "FAILED"
  | "DEAD_LETTER";

export type NotificationView = {
  id: number;
  messageId: number;
  messageBody: string;
  user: { id: number; name: string; email: string };
  channel: "SMS" | "EMAIL" | "PUSH" | string;
  category: string;
  status: NotificationStatus;
  attempts: number;
  lastError: string | null;
  createdAt: string;
  firstSentAt: string | null;
};

export type PageResponse<T> = {
  items: T[];
  nextCursor: string | null;
};

export type ProblemDetail = {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  errors?: { field: string; message: string }[];
};
