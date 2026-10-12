import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import HealthStatus from './HealthStatus';

describe('HealthStatus', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('shows UP when the backend answers', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => ({ status: 'UP', service: 'studymatch-backend' }),
    });

    render(<HealthStatus />);

    expect(await screen.findByText('UP')).toBeInTheDocument();
    expect(screen.getByText('studymatch-backend')).toBeInTheDocument();
    expect(globalThis.fetch).toHaveBeenCalledWith('http://localhost:8080/api/health');
  });

  it('shows an error when the backend is down', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('Failed to fetch'));

    render(<HealthStatus />);

    expect(
      await screen.findByText(/Não foi possível contactar o backend/)
    ).toBeInTheDocument();
  });
});
