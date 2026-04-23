import { useEffect, useState } from "react";
import { api } from "../api/client";
import { CreateMessageSchema } from "../api/schemas";
import type { CatalogItem, CreateMessageResponse } from "../api/types";

export interface MessageFormApi {
  categories: CatalogItem[];
  category: string;
  setCategory: (code: string) => void;
  body: string;
  setBody: (text: string) => void;
  submitting: boolean;
  error: string | null;
  result: CreateMessageResponse | null;
  submit: () => Promise<void>;
}

export function useMessageForm(): MessageFormApi {
  const [categories, setCategories] = useState<CatalogItem[]>([]);
  const [category, setCategory] = useState("");
  const [body, setBody] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<CreateMessageResponse | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    api.categories().then((cs) => {
      setCategories(cs);
      if (cs.length > 0) setCategory(cs[0].code);
    });
  }, []);

  async function submit() {
    setError(null);
    setResult(null);

    const parsed = CreateMessageSchema.safeParse({ category, body });
    if (!parsed.success) {
      setError(parsed.error.issues.map((i) => i.message).join("; "));
      return;
    }

    setSubmitting(true);
    try {
      const res = await api.createMessage(parsed.data.category, parsed.data.body);
      setResult(res);
      setBody("");
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setSubmitting(false);
    }
  }

  return {
    categories,
    category,
    setCategory,
    body,
    setBody,
    submitting,
    error,
    result,
    submit,
  };
}
