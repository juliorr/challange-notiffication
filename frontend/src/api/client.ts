import type {
  CatalogItem,
  CreateMessageResponse,
  NotificationView,
  PageResponse,
  ProblemDetail,
} from "./types";

const BASE = "";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    ...init,
  });
  if (!res.ok) {
    let problem: ProblemDetail | null = null;
    try {
      problem = (await res.json()) as ProblemDetail;
    } catch {

    }
    throw new Error(problem?.detail ?? `${res.status} ${res.statusText}`);
  }
  return (await res.json()) as T;
}

export const api = {
  categories: () => request<CatalogItem[]>("/api/categories"),
  channels:   () => request<CatalogItem[]>("/api/channels"),

  createMessage: (category: string, body: string) =>
    request<CreateMessageResponse>("/api/messages", {
      method: "POST",
      body: JSON.stringify({ category, body }),
    }),

  listNotifications: (params?: { limit?: number; status?: string; cursor?: string }) => {
    const q = new URLSearchParams();
    if (params?.limit)  q.set("limit",  String(params.limit));
    if (params?.status) q.set("status", params.status);
    if (params?.cursor) q.set("cursor", params.cursor);
    const qs = q.toString();
    return request<PageResponse<NotificationView>>(`/api/notifications${qs ? `?${qs}` : ""}`);
  },
};
