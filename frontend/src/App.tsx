import { Route, Routes } from "react-router-dom";
import { Layout } from "./components/Layout";
import { SendForm } from "./pages/SendForm";
import { Logs } from "./pages/Logs";

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/"     element={<SendForm />} />
        <Route path="/logs" element={<Logs />} />
      </Route>
    </Routes>
  );
}
