import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import AdminApp from './AdminApp.jsx';

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe('AdminApp', () => {
  it('shows the ultra rare admin Pix and lets the admin log out', () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve([]) });
    const onLogout = vi.fn();
    render(<AdminApp onLogout={onLogout} />);

    expect(screen.getByRole('img', { name: /Pix Admin, versão ultra rara/ })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Sair' }));
    expect(onLogout).toHaveBeenCalled();
  });
});
