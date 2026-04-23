import { useMessageForm } from "../hooks/useMessageForm";
import { field, formErrorText, input, submitBtn, successText } from "../styles/tokens";

export function SendForm() {
  const form = useMessageForm();

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    await form.submit();
  }

  return (
    <section style={{ maxWidth: 560 }}>
      <h1>Send a notification</h1>
      <form onSubmit={onSubmit}>
        <label style={field}>
          Category
          <select
            value={form.category}
            onChange={(e) => form.setCategory(e.target.value)}
            style={input}
          >
            {form.categories.map((c) => (
              <option key={c.code} value={c.code}>{c.name}</option>
            ))}
          </select>
        </label>
        <label style={field}>
          Body
          <textarea
            value={form.body}
            onChange={(e) => form.setBody(e.target.value)}
            rows={5}
            style={input}
          />
        </label>
        <button type="submit" disabled={form.submitting} style={submitBtn}>
          {form.submitting ? "Sending…" : "Send"}
        </button>
      </form>

      {form.error && <p style={formErrorText}>Error: {form.error}</p>}
      {form.result && (
        <p style={successText}>
          Message <code>#{form.result.id}</code> created — {form.result.fanoutCount} notifications enqueued.
        </p>
      )}
    </section>
  );
}
