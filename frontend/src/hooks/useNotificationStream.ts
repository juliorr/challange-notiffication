import { useEffect, useRef, useState } from "react";
import type { NotificationView } from "../api/types";
import { SSE_ENDPOINT, SSE_EVENT_NAME } from "../config";

export type StreamState = "idle" | "connecting" | "open" | "error";

export function useNotificationStream(onEvent: (view: NotificationView) => void) {
  const [state, setState] = useState<StreamState>("idle");
  const handlerRef = useRef(onEvent);
  handlerRef.current = onEvent;

  useEffect(() => {
    setState("connecting");
    const es = new EventSource(SSE_ENDPOINT);

    es.onopen  = () => setState("open");
    es.onerror = () => setState("error");

    es.addEventListener(SSE_EVENT_NAME, (ev) => {
      try {
        const view = JSON.parse((ev as MessageEvent).data) as NotificationView;
        handlerRef.current(view);
      } catch {

      }
    });

    return () => es.close();
  }, []);

  return { state };
}
