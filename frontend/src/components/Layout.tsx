import { Link, Outlet } from "react-router-dom";

export function Layout() {
  return (
    <>
      <header style={{ borderBottom: "1px solid rgba(128,128,128,0.3)", padding: "1rem 2rem" }}>
        <strong>Messages Challenge</strong>
        <nav style={{ float: "right" }}>
          <Link to="/" style={{ marginRight: "1rem" }}>Send</Link>
          <Link to="/logs">Logs</Link>
        </nav>
      </header>
      <main style={{ padding: "2rem" }}>
        <Outlet />
      </main>
    </>
  );
}
