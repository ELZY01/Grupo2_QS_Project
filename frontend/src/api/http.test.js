import { afterEach, describe, expect, it, vi } from 'vitest';
import { SESSION_EXPIRED_EVENT, saveSession } from '../auth/session.js';
import { apiFetch, request } from './http';

const response = (status, body) => ({ ok: status >= 200 && status < 300, status, json: async () => body });

afterEach(() => {
  saveSession(null);
  vi.restoreAllMocks();
});

describe('apiFetch', () => {
  it('sends the session token in the Authorization header', async () => {
    saveSession({ role: 'admin', token: 'abc123' });
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(response(200, []));

    await apiFetch('http://x/api/students', { headers: { 'Content-Type': 'application/json' } });

    expect(globalThis.fetch.mock.calls[0][1].headers).toEqual({
      'Content-Type': 'application/json',
      Authorization: 'Bearer abc123',
    });
  });

  it('does not add a header without a session', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(response(200, []));
    await apiFetch('http://x/api/health');
    expect(globalThis.fetch).toHaveBeenCalledWith('http://x/api/health');
  });

  it('warns the app when the session expired (401 with a token)', async () => {
    saveSession({ role: 'admin', token: 'old' });
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(response(401, { message: 'Tens de iniciar sessão.' }));
    const listener = vi.fn();
    window.addEventListener(SESSION_EXPIRED_EVENT, listener);

    await expect(request('/students')).rejects.toThrow('Tens de iniciar sessão.');
    expect(listener).toHaveBeenCalled();
    window.removeEventListener(SESSION_EXPIRED_EVENT, listener);
  });

  it('accepts 204 responses without a body', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(response(204, null));
    await expect(request('/auth/logout', { method: 'POST' })).resolves.toBeNull();
  });
});
