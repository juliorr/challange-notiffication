import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import type { CatalogItem, CreateMessageResponse } from "../api/types";

const categoriesMock = vi.fn<() => Promise<CatalogItem[]>>();
const createMessageMock =
  vi.fn<(category: string, body: string) => Promise<CreateMessageResponse>>();

vi.mock("../api/client", () => ({
  api: {
    categories: (...args: []) => categoriesMock(...args),
    createMessage: (...args: [string, string]) => createMessageMock(...args),
  },
}));

import { SendForm } from "./SendForm";

const DEFAULT_CATEGORIES: CatalogItem[] = [
  { code: "SPORTS",  name: "Sports"  },
  { code: "FINANCE", name: "Finance" },
  { code: "MOVIES",  name: "Movies"  },
];

async function renderForm(categories: CatalogItem[] = DEFAULT_CATEGORIES) {
  categoriesMock.mockResolvedValueOnce(categories);
  render(<SendForm />);
  if (categories.length > 0) {
    await screen.findByRole("option", { name: categories[0].name });
  } else {
    await waitFor(() => expect(categoriesMock).toHaveBeenCalled());
  }
}

function typeBody(value: string) {
  fireEvent.change(screen.getByLabelText(/body/i), { target: { value } });
}

function selectCategory(code: string) {
  fireEvent.change(screen.getByLabelText(/category/i), { target: { value: code } });
}

function submit() {
  fireEvent.click(screen.getByRole("button", { name: /send/i }));
}

beforeEach(() => {
  categoriesMock.mockReset();
  createMessageMock.mockReset();
});
afterEach(() => cleanup());

describe("SendForm — input validation", () => {
  it("shows error and does not call the API when body is empty", async () => {
    await renderForm();

    submit();

    expect(await screen.findByText(/message body is required/i)).toBeInTheDocument();
    expect(createMessageMock).not.toHaveBeenCalled();
  });

  it("rejects a body made of only whitespace (trimmed by schema)", async () => {
    await renderForm();

    typeBody("     ");
    submit();

    expect(await screen.findByText(/message body is required/i)).toBeInTheDocument();
    expect(createMessageMock).not.toHaveBeenCalled();
  });

  it("rejects bodies longer than 4000 characters", async () => {
    await renderForm();

    typeBody("x".repeat(4001));
    submit();

    expect(await screen.findByText(/≤ 4000 characters/i)).toBeInTheDocument();
    expect(createMessageMock).not.toHaveBeenCalled();
  });

  it("rejects an empty category (when the catalog came back empty)", async () => {
    await renderForm([]);

    typeBody("hello");
    submit();

    expect(await screen.findByText(/select a category/i)).toBeInTheDocument();
    expect(createMessageMock).not.toHaveBeenCalled();
  });
});

describe("SendForm — happy path", () => {
  it("calls createMessage with the selected category and body, shows success, and clears the body", async () => {
    createMessageMock.mockResolvedValueOnce({ id: 42, fanoutCount: 7 });
    await renderForm();

    selectCategory("FINANCE");
    typeBody("quarterly report is out");
    submit();

    await waitFor(() => expect(createMessageMock).toHaveBeenCalledTimes(1));
    expect(createMessageMock).toHaveBeenCalledWith("FINANCE", "quarterly report is out");

    expect(await screen.findByText(/#42/)).toBeInTheDocument();
    expect(screen.getByText(/7 notifications enqueued/i)).toBeInTheDocument();
    expect((screen.getByLabelText(/body/i) as HTMLTextAreaElement).value).toBe("");
  });

  it("disables the button and shows 'Sending…' while the request is in flight", async () => {
    let resolve!: (v: CreateMessageResponse) => void;
    createMessageMock.mockReturnValueOnce(new Promise<CreateMessageResponse>((r) => { resolve = r; }));
    await renderForm();

    typeBody("hola");
    submit();

    const button = await screen.findByRole("button", { name: /sending/i });
    expect(button).toBeDisabled();

    resolve({ id: 1, fanoutCount: 1 });

    await waitFor(() =>
      expect(screen.getByRole("button", { name: /^send$/i })).not.toBeDisabled(),
    );
  });
});

describe("SendForm — network/backend errors", () => {
  it("renders the Error message, re-enables the button and keeps the body when the API throws", async () => {
    createMessageMock.mockRejectedValueOnce(new Error("backend is down"));
    await renderForm();

    typeBody("please keep me");
    submit();

    expect(await screen.findByText(/backend is down/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /^send$/i })).not.toBeDisabled();
    expect((screen.getByLabelText(/body/i) as HTMLTextAreaElement).value).toBe("please keep me");
  });

  it("stringifies non-Error rejections before displaying them", async () => {
    createMessageMock.mockRejectedValueOnce("boom");
    await renderForm();

    typeBody("hi");
    submit();

    expect(await screen.findByText(/boom/)).toBeInTheDocument();
  });
});

describe("SendForm — catalog load", () => {
  it("populates the select and preselects the first category", async () => {
    await renderForm();

    const select = screen.getByLabelText(/category/i) as HTMLSelectElement;
    expect(select.value).toBe("SPORTS");
    expect(screen.getAllByRole("option")).toHaveLength(3);
  });

  it("renders an empty select when the catalog is empty and surfaces the validation error on submit", async () => {
    await renderForm([]);

    expect(screen.queryAllByRole("option")).toHaveLength(0);

    typeBody("hello");
    submit();

    expect(await screen.findByText(/select a category/i)).toBeInTheDocument();
    expect(createMessageMock).not.toHaveBeenCalled();
  });
});

describe("SendForm — UX regressions", () => {
  it("allows a second successful submission after the first one", async () => {
    createMessageMock
      .mockResolvedValueOnce({ id: 1, fanoutCount: 3 })
      .mockResolvedValueOnce({ id: 2, fanoutCount: 5 });
    await renderForm();

    typeBody("first");
    submit();
    expect(await screen.findByText(/#1/)).toBeInTheDocument();

    typeBody("second");
    submit();

    expect(await screen.findByText(/#2/)).toBeInTheDocument();
    expect(screen.getByText(/5 notifications enqueued/i)).toBeInTheDocument();
    expect(createMessageMock).toHaveBeenCalledTimes(2);
  });

  it("clears a previous error after a subsequent successful submit", async () => {
    createMessageMock
      .mockRejectedValueOnce(new Error("transient"))
      .mockResolvedValueOnce({ id: 99, fanoutCount: 1 });
    await renderForm();

    typeBody("retry me");
    submit();
    expect(await screen.findByText(/transient/i)).toBeInTheDocument();

    submit();

    expect(await screen.findByText(/#99/)).toBeInTheDocument();
    expect(screen.queryByText(/transient/i)).not.toBeInTheDocument();
  });
});
