const API = "http://localhost:8080/api";

const TOKEN_KEY = "pricing_feed_token";

export function getStoredToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string | null) {
  if (token) {
    sessionStorage.setItem(TOKEN_KEY, token);
  } else {
    sessionStorage.removeItem(TOKEN_KEY);
  }
}

export async function apiFetch(path: string, options: RequestInit = {}): Promise<Response> {
  const token = getStoredToken();
  const headers = new Headers(options.headers);
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  const res = await fetch(`${API}${path}`, { ...options, headers });
  if (res.status === 401) {
    setStoredToken(null);
    window.dispatchEvent(new Event("auth:logout"));
  }
  return res;
}

export async function login(username: string, password: string): Promise<void> {
  const res = await fetch(`${API}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password })
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error((err as { message?: string }).message ?? "Login failed");
  }
  const data = (await res.json()) as { accessToken: string };
  setStoredToken(data.accessToken);
}

export { API };
