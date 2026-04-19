import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";

const API = axios.create({ baseURL: "" });
API.interceptors.request.use(config => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = "Bearer " + token;
  return config;
});

interface Application {
  id: number;
  jobTitle: string;
  company: string;
  location: string;
  salary: string;
  jobUrl: string;
  notes: string;
  status: string;
  appliedAt: string;
}

const COLUMNS = [
  { key: "APPLIED", label: "Applied", icon: "📋", color: "#6c63ff" },
  { key: "PHONE_SCREEN", label: "Phone Screen", icon: "📞", color: "#f59e0b" },
  { key: "INTERVIEW", label: "Interview", icon: "💼", color: "#3b82f6" },
  { key: "OFFER", label: "Offer", icon: "🎉", color: "#10b981" },
  { key: "REJECTED", label: "Rejected", icon: "❌", color: "#ef4444" },
];

const emptyForm = { jobTitle: "", company: "", location: "", salary: "", jobUrl: "", notes: "", status: "APPLIED" };

export default function Kanban() {
  const [applications, setApplications] = useState<Application[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editApp, setEditApp] = useState<Application | null>(null);
  const [dragId, setDragId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);

  const fetchApps = useCallback(async () => {
    try {
      const res = await API.get("/api/applications");
      setApplications(res.data);
    } catch (e) { console.error(e); }
  }, []);

  useEffect(() => { fetchApps(); }, [fetchApps]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editApp) {
        await API.put("/api/applications/" + editApp.id, form);
      } else {
        await API.post("/api/applications", form);
      }
      setShowForm(false);
      setEditApp(null);
      setForm(emptyForm);
      fetchApps();
    } catch (e) { alert("Failed to save"); }
  };

  const handleStatusChange = async (id: number, status: string) => {
    try {
      await API.put("/api/applications/" + id + "/status", { status });
      fetchApps();
    } catch (e) { console.error(e); }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("Delete this application?")) return;
    await API.delete("/api/applications/" + id);
    fetchApps();
  };

  const handleEdit = (app: Application) => {
    setEditApp(app);
    setForm({ jobTitle: app.jobTitle, company: app.company, location: app.location || "", salary: app.salary || "", jobUrl: app.jobUrl || "", notes: app.notes || "", status: app.status });
    setShowForm(true);
  };

  const handleDrop = (status: string) => {
    if (dragId !== null) { handleStatusChange(dragId, status); setDragId(null); }
  };

  return (
    <div style={{ minHeight: "100vh", background: "#f0f2f5", padding: 24 }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
        <div>
          <h1 style={{ fontSize: 28, fontWeight: 800, color: "#1a1a2e", margin: 0 }}>Job Tracker</h1>
          <p style={{ fontSize: 14, color: "#888", margin: "4px 0 0" }}>Track your job applications</p>
        </div>
        <button style={{ background: "linear-gradient(135deg,#6c63ff,#764ba2)", color: "white", border: "none", padding: "12px 24px", borderRadius: 10, fontSize: 14, fontWeight: 600, cursor: "pointer" }}
          onClick={() => { setEditApp(null); setForm(emptyForm); setShowForm(true); }}>
          + Add Application
        </button>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(5,1fr)", gap: 12, marginBottom: 24 }}>
        {COLUMNS.map(col => (
          <div key={col.key} style={{ background: "white", borderRadius: 12, padding: 16, textAlign: "center", boxShadow: "0 2px 8px rgba(0,0,0,0.06)", borderTop: "3px solid " + col.color }}>
            <div style={{ fontSize: 32, fontWeight: 800, color: col.color, lineHeight: 1 }}>
              {applications.filter(a => a.status === col.key).length}
            </div>
            <div style={{ fontSize: 12, color: "#888", marginTop: 4 }}>{col.icon} {col.label}</div>
          </div>
        ))}
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(5,1fr)", gap: 16, alignItems: "start" }}>
        {COLUMNS.map(col => (
          <div key={col.key} style={{ background: "white", borderRadius: 14, overflow: "hidden", boxShadow: "0 2px 12px rgba(0,0,0,0.06)", minHeight: 200 }}
            onDragOver={e => { e.preventDefault(); e.stopPropagation(); }}
            onDrop={(e) => { e.preventDefault(); handleDrop(col.key); }}>
            <div style={{ padding: "14px 16px", display: "flex", alignItems: "center", gap: 8, background: "#fafafa", borderBottom: "2px solid " + col.color }}>
              <span style={{ fontSize: 18 }}>{col.icon}</span>
              <span style={{ flex: 1, fontSize: 13, fontWeight: 700, color: "#1a1a2e" }}>{col.label}</span>
              <span style={{ color: "white", width: 22, height: 22, borderRadius: "50%", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 11, fontWeight: 700, background: col.color }}>
                {applications.filter(a => a.status === col.key).length}
              </span>
            </div>
            <div style={{ padding: 12, display: "flex", flexDirection: "column", gap: 10 }}>
              {applications.filter(a => a.status === col.key).map(app => (
                <div key={app.id} style={{ background: "#f8f9ff", border: "1px solid #e8eaff", borderRadius: 10, padding: 14, cursor: "grab" }}
                  draggable onDragStart={() => setDragId(app.id)}>
                  <div style={{ fontSize: 13, fontWeight: 700, color: "#1a1a2e", marginBottom: 6 }}>{app.jobTitle}</div>
                  <div style={{ fontSize: 12, color: "#6c63ff", marginBottom: 4, fontWeight: 600 }}>🏢 {app.company}</div>
                  {app.location && <div style={{ fontSize: 11, color: "#888", marginBottom: 2 }}>📍 {app.location}</div>}
                  {app.salary && <div style={{ fontSize: 11, color: "#888", marginBottom: 2 }}>💰 {app.salary}</div>}
                  {app.notes && <div style={{ fontSize: 11, color: "#666", background: "#f0f0f8", borderRadius: 6, padding: "4px 8px", marginTop: 6 }}>{app.notes}</div>}
                  <div style={{ fontSize: 10, color: "#aaa", marginBottom: 8, marginTop: 4 }}>
                    {new Date(app.appliedAt).toLocaleDateString()}
                  </div>
                  <div style={{ display: "flex", gap: 4, alignItems: "center" }}>
                    <select style={{ flex: 1, fontSize: 10, padding: "3px 4px", borderRadius: 5, border: "1px solid #ddd" }}
                      value={app.status} onChange={e => handleStatusChange(app.id, e.target.value)}>
                      {COLUMNS.map(c => <option key={c.key} value={c.key}>{c.label}</option>)}
                    </select>
                    <button style={{ padding: "3px 7px", background: "white", border: "1px solid #ddd", borderRadius: 5, cursor: "pointer", fontSize: 12 }} onClick={() => handleEdit(app)}>✏️</button>
                    {app.jobUrl && <a href={app.jobUrl} target="_blank" rel="noreferrer" style={{ padding: "3px 7px", background: "white", border: "1px solid #ddd", borderRadius: 5, fontSize: 12, textDecoration: "none" }}>🔗</a>}
                    <button style={{ padding: "3px 7px", background: "#fff5f5", border: "1px solid #fed7d7", borderRadius: 5, cursor: "pointer", fontSize: 12 }} onClick={() => handleDelete(app.id)}>🗑</button>
                  </div>
                </div>
              ))}
              {applications.filter(a => a.status === col.key).length === 0 && (
                <div style={{ textAlign: "center", color: "#bbb", padding: "20px 0", fontSize: 12, borderRadius: 8, border: "2px dashed #eee" }}>Drop here</div>
              )}
            </div>
          </div>
        ))}
      </div>

      {showForm && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.5)", zIndex: 1000, display: "flex", alignItems: "center", justifyContent: "center", padding: 16 }}>
          <div style={{ background: "white", borderRadius: 16, width: "100%", maxWidth: 560, maxHeight: "90vh", overflow: "auto" }}>
            <div style={{ padding: "20px 24px 16px", borderBottom: "1px solid #eee", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <h2 style={{ fontSize: 18, fontWeight: 700, margin: 0, color: "#1a1a2e" }}>{editApp ? "Edit" : "Add"} Application</h2>
              <button style={{ background: "none", border: "none", fontSize: 20, cursor: "pointer" }} onClick={() => setShowForm(false)}>✕</button>
            </div>
            <form onSubmit={handleSubmit} style={{ padding: 24 }}>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16, marginBottom: 20 }}>
                {[["jobTitle","Job Title *",true],["company","Company *",true],["location","Location",false],["salary","Salary",false]].map(([key,label,req]) => (
                  <div key={key as string} style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                    <label style={{ fontSize: 13, fontWeight: 600, color: "#444" }}>{label as string}</label>
                    <input style={{ padding: "10px 12px", borderRadius: 8, border: "1.5px solid #eee", fontSize: 14, outline: "none", width: "100%", boxSizing: "border-box" }}
                      value={form[key as keyof typeof form]}
                      onChange={e => setForm({ ...form, [key as string]: e.target.value })}
                      required={req as boolean} />
                  </div>
                ))}
                <div style={{ display: "flex", flexDirection: "column", gap: 6, gridColumn: "1 / -1" }}>
                  <label style={{ fontSize: 13, fontWeight: 600, color: "#444" }}>Job URL</label>
                  <input style={{ padding: "10px 12px", borderRadius: 8, border: "1.5px solid #eee", fontSize: 14, outline: "none", width: "100%", boxSizing: "border-box" }}
                    value={form.jobUrl} onChange={e => setForm({ ...form, jobUrl: e.target.value })} />
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: 6, gridColumn: "1 / -1" }}>
                  <label style={{ fontSize: 13, fontWeight: 600, color: "#444" }}>Status</label>
                  <select style={{ padding: "10px 12px", borderRadius: 8, border: "1.5px solid #eee", fontSize: 14, outline: "none", width: "100%", boxSizing: "border-box" }}
                    value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                    {COLUMNS.map(c => <option key={c.key} value={c.key}>{c.icon} {c.label}</option>)}
                  </select>
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: 6, gridColumn: "1 / -1" }}>
                  <label style={{ fontSize: 13, fontWeight: 600, color: "#444" }}>Notes</label>
                  <textarea style={{ padding: "10px 12px", borderRadius: 8, border: "1.5px solid #eee", fontSize: 14, outline: "none", width: "100%", boxSizing: "border-box", height: 80, resize: "vertical" }}
                    value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
                </div>
              </div>
              <div style={{ display: "flex", gap: 12, justifyContent: "flex-end" }}>
                <button type="button" style={{ padding: "10px 20px", background: "white", border: "1.5px solid #eee", borderRadius: 8, fontSize: 14, cursor: "pointer" }} onClick={() => setShowForm(false)}>Cancel</button>
                <button type="submit" style={{ padding: "10px 24px", background: "linear-gradient(135deg,#6c63ff,#764ba2)", color: "white", border: "none", borderRadius: 8, fontSize: 14, fontWeight: 600, cursor: "pointer" }}>{editApp ? "Update" : "Add Application"}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
