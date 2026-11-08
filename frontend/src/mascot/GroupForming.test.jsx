import { act, cleanup, render, renderHook, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import GroupForming from './GroupForming.jsx';
import { MIN_FORMING_MS, useMinimumDuration } from './useMinimumDuration.js';

function mockReducedMotion(matches) {
  window.matchMedia = vi.fn().mockImplementation((query) => ({
    matches: matches && query.includes('reduce'),
    media: query,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  }));
}

afterEach(() => {
  cleanup();
  delete window.matchMedia;
  vi.useRealTimers();
});

describe('GroupForming', () => {
  it('shows a squad of four Pix, one banner per area, and the loading text', () => {
    mockReducedMotion(false);
    render(<GroupForming />);

    expect(screen.getByRole('status')).toHaveTextContent('A formar grupos…');
    const lobby = screen.getByTestId('group-forming-stage');
    expect(lobby.querySelectorAll('.lobby-slot')).toHaveLength(4);
    expect(lobby.querySelectorAll('.lobby-banner')).toHaveLength(4);
    const labels = [...lobby.querySelectorAll('.lobby-banner-label')].map((el) => el.title);
    expect(labels).toEqual(['Organização', 'Gestão de tempo', 'Programação', 'Trabalho em grupo']);
    ['body-org', 'body-tempo', 'body-prog', 'body-equipa'].forEach((body) => {
      expect(lobby.innerHTML).toContain(body);
    });
    expect(lobby).toHaveTextContent('✦');
  });

  it('only shows the text when the user prefers reduced motion', () => {
    mockReducedMotion(true);
    render(<GroupForming text="A gerar…" />);

    expect(screen.getByRole('status')).toHaveTextContent('A gerar…');
    expect(screen.queryByTestId('group-forming-stage')).toBeNull();
  });

  it('still renders the lobby when matchMedia is not available', () => {
    render(<GroupForming />);
    expect(screen.getByTestId('group-forming-stage')).toBeInTheDocument();
  });
});

describe('useMinimumDuration', () => {
  it('keeps the animation visible for the minimum time when the request is fast', () => {
    vi.useFakeTimers();
    const { result, rerender } = renderHook(({ active }) => useMinimumDuration(active), {
      initialProps: { active: true },
    });

    act(() => vi.advanceTimersByTime(300));
    rerender({ active: false });
    expect(result.current).toBe(true);

    act(() => vi.advanceTimersByTime(MIN_FORMING_MS - 301));
    expect(result.current).toBe(true);

    act(() => vi.advanceTimersByTime(1));
    expect(result.current).toBe(false);
  });

  it('hides right away when the request took longer than the minimum', () => {
    vi.useFakeTimers();
    const { result, rerender } = renderHook(({ active }) => useMinimumDuration(active, 2000), {
      initialProps: { active: true },
    });

    act(() => vi.advanceTimersByTime(3000));
    rerender({ active: false });
    act(() => vi.advanceTimersByTime(0));
    expect(result.current).toBe(false);
  });

  it('stays hidden if it never started', () => {
    const { result } = renderHook(() => useMinimumDuration(false));
    expect(result.current).toBe(false);
  });
});
