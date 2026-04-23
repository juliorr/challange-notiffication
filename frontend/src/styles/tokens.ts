import type { CSSProperties } from "react";

export const th: CSSProperties = {
  textAlign: "left",
  borderBottom: "1px solid rgba(128,128,128,0.3)",
  padding: "0.5rem",
  fontWeight: 600,
};

export const banner: CSSProperties = {
  padding: "0.5rem 0.75rem",
  margin: "0.5rem 0",
  background: "rgba(59, 130, 246, 0.12)",
  border: "1px solid rgba(59, 130, 246, 0.4)",
  borderRadius: 4,
};

export const pager: CSSProperties = {
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  gap: "1rem",
  padding: "1rem",
};

export const btn: CSSProperties = {
  padding: "0.4rem 0.9rem",
  border: "1px solid rgba(128,128,128,0.4)",
  borderRadius: 4,
  cursor: "pointer",
};

export const field: CSSProperties = {
  display: "block",
  marginBottom: "0.5rem",
};

export const input: CSSProperties = {
  display: "block",
  marginTop: 4,
  padding: "0.4rem",
  width: "100%",
};

export const submitBtn: CSSProperties = {
  padding: "0.5rem 1rem",
};

export const mutedText: CSSProperties = {
  color: "#6b7280",
};

export const errorText: CSSProperties = {
  color: "#b91c1c",
};

export const formErrorText: CSSProperties = {
  color: "#dc2626",
  marginTop: "1rem",
};

export const successText: CSSProperties = {
  color: "#16a34a",
  marginTop: "1rem",
};

export const cellEllipsis: CSSProperties = {
  maxWidth: 360,
  whiteSpace: "nowrap",
  overflow: "hidden",
  textOverflow: "ellipsis",
};

export const emptyCell: CSSProperties = {
  padding: "1rem",
  color: "#6b7280",
};

export const table: CSSProperties = {
  borderCollapse: "collapse",
  width: "100%",
};
