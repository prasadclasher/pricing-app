import { useCallback, useEffect, useState } from "react";
import { apiFetch, getStoredToken, login, setStoredToken } from "./api";

type RecordItem = {
  id: number;
  storeId: number;
  sku: string;
  productName: string;
  price: number;
  priceDate: string;
  currencyCode: string;
  version: number;
};

type Me = {
  userId: number;
  username: string;
  role: string;
  storeId: number | null;
};

export function App() {
  const [token, setToken] = useState<string | null>(() => getStoredToken());
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loginError, setLoginError] = useState("");
  const [me, setMe] = useState<Me | null>(null);
  const [jobId, setJobId] = useState("");
  const [jobStatus, setJobStatus] = useState<unknown>(null);
  const [records, setRecords] = useState<RecordItem[]>([]);
  const [selected, setSelected] = useState<RecordItem | null>(null);
  const [newPrice, setNewPrice] = useState("");
  const [query, setQuery] = useState({ storeId: "", sku: "", productName: "" });

  const loadMe = useCallback(async () => {
    if (!getStoredToken()) return;
    const res = await apiFetch("/auth/me");
    if (res.ok) {
      setMe(await res.json());
    } else {
      setMe(null);
      setToken(null);
    }
  }, []);

  useEffect(() => {
    if (token) {
      loadMe();
    } else {
      setMe(null);
      setJobId("");
      setJobStatus(null);
      setRecords([]);
      setSelected(null);
      setNewPrice("");
      setQuery({ storeId: "", sku: "", productName: "" });
    }
  }, [token, loadMe]);

  useEffect(() => {
    const onLogout = () => {
      setToken(null);
      setMe(null);
    };
    window.addEventListener("auth:logout", onLogout);
    return () => window.removeEventListener("auth:logout", onLogout);
  }, []);

  const handleLogin = async () => {
    setLoginError("");
    try {
      await login(username, password);
      setToken(getStoredToken());
    } catch (e) {
      setLoginError(e instanceof Error ? e.message : "Login failed");
    }
  };

  const handleLogout = () => {
    setStoredToken(null);
    setToken(null);
    setMe(null);
  };

  const upload = async (file: File) => {
    const body = new FormData();
    body.append("file", file);
    const res = await apiFetch("/uploads", { method: "POST", body });
    if (!res.ok) {
      alert(`Upload failed: ${res.status}`);
      return;
    }
    const data = await res.json();
    setJobId(data.jobId);
  };

  const pollJob = async () => {
    if (!jobId) return;
    const res = await apiFetch(`/uploads/${jobId}`);
    if (res.ok) {
      setJobStatus(await res.json());
    }
  };

  const search = async () => {
    const params = new URLSearchParams();
    if (query.storeId) params.set("storeId", query.storeId);
    if (query.sku) params.set("sku", query.sku);
    if (query.productName) params.set("productName", query.productName);
    const res = await apiFetch(`/pricing-records?${params}`);
    if (res.ok) {
      const data = await res.json();
      setRecords(data.content ?? []);
    }
  };

  const loadRecord = async (id: number) => {
    const res = await apiFetch(`/pricing-records/${id}`);
    if (res.ok) {
      const data = await res.json();
      setSelected(data);
      setNewPrice(String(data.price));
    }
  };

  const saveRecord = async () => {
    if (!selected) return;
    const res = await apiFetch(`/pricing-records/${selected.id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ price: Number(newPrice), version: selected.version })
    });
    if (res.status === 409) {
      alert("Version conflict. Refresh and try again.");
      return;
    }
    if (res.ok) {
      setSelected(await res.json());
      alert("Saved");
      await search();
    }
  };

  if (!token) {
    return (
      <div className="page">
        <h1>Pricing Feed MVP</h1>
        <section>
          <h2>Login</h2>
          <p>Use credentials from your local <code>.env</code> (see README).</p>
          <div>
            <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} />
            <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} />
            <button onClick={handleLogin}>Login</button>
          </div>
          {loginError && <p style={{ color: "crimson" }}>{loginError}</p>}
        </section>
      </div>
    );
  }

  return (
    <div className="page">
      <h1>Pricing Feed MVP</h1>
      <p>
        Signed in as <strong>{me?.username ?? "…"}</strong> ({me?.role ?? "…"})
        {me?.storeId != null && <> — store {me.storeId}</>}
        <button onClick={handleLogout} style={{ marginLeft: "1rem" }}>Logout</button>
      </p>

      <section>
        <h2>Upload CSV</h2>
        <input type="file" accept=".csv" onChange={(e) => e.target.files && upload(e.target.files[0])} />
        <button onClick={pollJob}>Refresh Job</button>
        {jobStatus != null && <pre>{JSON.stringify(jobStatus, null, 2)}</pre>}
      </section>

      <section>
        <h2>Search</h2>
        <input placeholder="Store ID" value={query.storeId} onChange={(e) => setQuery({ ...query, storeId: e.target.value })} />
        <input placeholder="SKU" value={query.sku} onChange={(e) => setQuery({ ...query, sku: e.target.value })} />
        <input placeholder="Product name" value={query.productName} onChange={(e) => setQuery({ ...query, productName: e.target.value })} />
        <button onClick={search}>Search</button>
        <table>
          <thead>
          <tr><th>ID</th><th>Store</th><th>SKU</th><th>Name</th><th>Price</th><th /></tr>
          </thead>
          <tbody>
          {records.map((r) => (
            <tr key={r.id}>
              <td>{r.id}</td><td>{r.storeId}</td><td>{r.sku}</td><td>{r.productName}</td><td>{r.price}</td>
              <td><button onClick={() => loadRecord(r.id)}>Edit</button></td>
            </tr>
          ))}
          </tbody>
        </table>
      </section>

      <section>
        <h2>Edit & Save</h2>
        {!selected ? <p>Select a record</p> : (
          <div>
            <div>SKU: {selected.sku}</div>
            <div>Version: {selected.version}</div>
            <input value={newPrice} onChange={(e) => setNewPrice(e.target.value)} />
            <button onClick={saveRecord}>Save</button>
          </div>
        )}
      </section>
    </div>
  );
}
