import { useCallback, useEffect, useRef, useState } from "react";

export interface Page<T> {
  items: T[];
  nextCursor: string | null;
}

export interface PaginationApi<T> {
  items: T[];
  page: number;
  loading: boolean;
  error: string | null;
  hasPrev: boolean;
  hasNext: boolean;
  goFirst: () => void;
  goPrev: () => void;
  goNext: () => void;
  replaceItem: (matcher: (item: T) => boolean, next: T) => boolean;
}

export function usePagination<T>(
  fetcher: (cursor: string | null) => Promise<Page<T>>,
  onPageOneLoaded?: () => void,
): PaginationApi<T> {
  const [items, setItems] = useState<T[]>([]);
  const [page, setPage] = useState<number>(1);
  const [cursors, setCursors] = useState<(string | null)[]>([null]);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const onPageOneLoadedRef = useRef(onPageOneLoaded);
  onPageOneLoadedRef.current = onPageOneLoaded;

  const loadPage = useCallback(
    async (targetPage: number, cursor: string | null) => {
      setLoading(true);
      setError(null);
      try {
        const res = await fetcher(cursor);
        setItems(res.items);
        setNextCursor(res.nextCursor);
        setPage(targetPage);
        setCursors((prev) => {
          const next = prev.slice(0, targetPage);
          next[targetPage] = res.nextCursor;
          return next;
        });
        if (targetPage === 1) onPageOneLoadedRef.current?.();
      } catch (e) {
        setError(e instanceof Error ? e.message : "Failed to load");
      } finally {
        setLoading(false);
      }
    },
    [fetcher],
  );

  const goFirst = useCallback(() => {
    setCursors([null]);
    loadPage(1, null);
  }, [loadPage]);

  const goPrev = useCallback(() => {
    if (page <= 1 || loading) return;
    loadPage(page - 1, cursors[page - 2] ?? null);
  }, [page, loading, cursors, loadPage]);

  const goNext = useCallback(() => {
    if (!nextCursor || loading) return;
    loadPage(page + 1, nextCursor);
  }, [nextCursor, loading, page, loadPage]);

  const replaceItem = useCallback((matcher: (item: T) => boolean, next: T): boolean => {
    let replaced = false;
    setItems((prev) => {
      const idx = prev.findIndex(matcher);
      if (idx < 0) return prev;
      replaced = true;
      const copy = prev.slice();
      copy[idx] = next;
      return copy;
    });
    return replaced;
  }, []);

  useEffect(() => {
    loadPage(1, null);
  }, [loadPage]);

  return {
    items,
    page,
    loading,
    error,
    hasPrev: page > 1,
    hasNext: nextCursor !== null,
    goFirst,
    goPrev,
    goNext,
    replaceItem,
  };
}
