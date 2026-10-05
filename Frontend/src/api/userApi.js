/**
 * API layer for User Service
 * All requests go through Vite proxy → API Gateway (8085) → User Service (8081)
 */

const BASE = '/api/users';

/**
 * Register a new user
 * POST /api/users/register
 */
export async function registerUser({ firstName, lastName, email, password, mobile, role }) {
  const res = await fetch(`${BASE}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ firstName, lastName, email, password, mobile, role }),
  });

  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw new Error(data.message || `Registration failed (${res.status})`);
  }

  return res.json(); // UserResponse { id, firstName, lastName, email, mobile }
}

/**
 * Fetch all users (used for login matching — simple, no auth server)
 * GET /api/users
 */
export async function fetchAllUsers() {
  const res = await fetch(BASE);
  if (!res.ok) throw new Error(`Failed to fetch users (${res.status})`);
  return res.json();
}
