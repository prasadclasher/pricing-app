import { useState } from "react";

const API = "http://localhost:8080/api";
const HEADERS = {
  "x-role": "HQ_ADMIN",
  "x-user-id": "1"
};

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

export function App() {
  const [jobId, setJobId] = useState("");
  const [jobStatus, setJobStatus] = useState<any>(null);
  const [records, setRecords] = useState<RecordItem[]>([]);
  const [selected, setSelected] = useState<RecordItem | null>(null);
  const [newPrice, setNewPrice] = useState("");
  const [query, setQuery] = useState({ storeId: "", sku: "", productName: "" });

  const upload = async (file: File) => {
    const body = new FormData();
    body.append("file", file);
    const res = await fetch(`${API}/uploads`, { method: "POST", headers: HEADERS, body });
    const data = await res.json();
    setJobId(data.jobId);
  };

  const pollJob = async () => {
    if (!jobId) return;
    const res = await fetch(`${API}/uploads/${jobId}`);
    setJobStatus(await res.json());
  };

  const search = async () => {
    const params = new URLSearchParams();
    if (query.storeId) params.set("storeId", query.storeId);
    if (query.sku) params.set("sku", query.sku);
    if (query.productName) params.set("productName", query.productName);
    const res = await fetch(`${API}/pricing-records?${params}`, { headers: HEADERS });
    const data = await res.json();
    setRecords(data.content ?? []);
  };

  const loadRecord = async (id: number) => {
    const res = await fetch(`${API}/pricing-records/${id}`, { headers: HEADERS });
    const data = await res.json();
    setSelected(data);
    setNewPrice(String(data.price));
  };

  const saveRecord = async () => {
    if (!selected) return;
    const res = await fetch(`${API}/pricing-records/${selected.id}`, {
      method: "PUT",
      headers: { ...HEADERS, "Content-Type": "application/json" },
      body: JSON.stringify({ price: Number(newPrice), version: selected.version })
    });
    if (res.status === 409) {
      alert("Version conflict. Refresh and try again.");
      return;
    }
    setSelected(await res.json());
    alert("Saved");
  };

  return (
    <div className="page">
      <h1>Pricing Feed MVP</h1>

      <section>
        <h2>Upload CSV</h2>
        <input type="file" accept=".csv" onChange={(e) => e.target.files && upload(e.target.files[0])} />
        <button onClick={pollJob}>Refresh Job</button>
        {jobStatus && <pre>{JSON.stringify(jobStatus, null, 2)}</pre>}
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
