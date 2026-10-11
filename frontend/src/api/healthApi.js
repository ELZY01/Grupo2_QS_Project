const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

/**
 * Calls the backend health endpoint (GET /api/health).
 * @returns {Promise<{status: string, service: string}>}
 */
export async function getHealth() {
  const response = await fetch(`${API_URL}/api/health`);
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  return response.json();
}
