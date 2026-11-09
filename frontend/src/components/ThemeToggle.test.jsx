import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { applyStoredTheme } from '../auth/theme.js';
import ThemeToggle from './ThemeToggle.jsx';

const mockSystem = (dark) => {
  window.matchMedia = vi.fn().mockImplementation((query) => ({ matches: dark && query.includes('dark'), media: query }));
};

afterEach(() => {
  cleanup();
  window.localStorage.clear();
  delete document.documentElement.dataset.theme;
  delete window.matchMedia;
});

describe('ThemeToggle', () => {
  it('starts from the system theme when nothing was chosen', () => {
    mockSystem(true);
    render(<ThemeToggle />);

    expect(screen.getByRole('button', { name: 'Mudar para o modo de dia' })).toHaveAttribute('aria-pressed', 'true');
    expect(document.documentElement.dataset.theme).toBe('dark');
  });

  it('switches to night mode and remembers the choice', () => {
    mockSystem(false);
    render(<ThemeToggle />);

    fireEvent.click(screen.getByRole('button', { name: 'Mudar para o modo noturno' }));

    expect(document.documentElement.dataset.theme).toBe('dark');
    expect(window.localStorage.getItem('studymatch.theme')).toBe('dark');
    expect(screen.getByRole('button', { name: 'Mudar para o modo de dia' })).toHaveTextContent('Noite');
  });

  it('applies the stored choice before the app renders', () => {
    window.localStorage.setItem('studymatch.theme', 'light');
    applyStoredTheme();
    expect(document.documentElement.dataset.theme).toBe('light');
  });

  it('ignores invalid stored values', () => {
    window.localStorage.setItem('studymatch.theme', 'roxo');
    applyStoredTheme();
    expect(document.documentElement.dataset.theme).toBeUndefined();
  });
});
