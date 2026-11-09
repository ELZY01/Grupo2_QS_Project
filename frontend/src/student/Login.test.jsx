import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Login from './Login.jsx';

const SESSION = { token: 'tok', role: 'ALUNO', userId: 5, studentId: 3, displayName: 'Teste Francisco' };

const jsonResponse = (status, body) => ({ ok: status >= 200 && status < 300, status, json: async () => body });

const setup = () => {
  const props = { onBack: vi.fn(), onRegister: vi.fn(), onLogin: vi.fn() };
  render(<Login {...props} />);
  return props;
};

const fill = (user, password) => {
  fireEvent.change(screen.getByLabelText('Número de estudante, email ou utilizador'), { target: { value: user } });
  fireEvent.change(screen.getByLabelText('Palavra-passe'), { target: { value: password } });
  fireEvent.click(screen.getByRole('button', { name: 'Entrar' }));
};

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe('Login', () => {
  it('always asks for the password and checks it in the backend', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, SESSION));
    const props = setup();

    fill(' TF@upt.pt ', 'segredo123');

    await vi.waitFor(() => expect(props.onLogin).toHaveBeenCalledWith(SESSION));
    const [url, options] = globalThis.fetch.mock.calls[0];
    expect(url).toBe('http://localhost:8080/api/auth/login');
    expect(JSON.parse(options.body)).toEqual({ username: 'TF@upt.pt', password: 'segredo123' });
  });

  it('shows the error for wrong credentials', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      jsonResponse(401, { message: 'Utilizador ou palavra-passe errados.' }),
    );
    const props = setup();

    fill('admin', 'errada');

    expect(await screen.findByRole('alert')).toHaveTextContent('Utilizador ou palavra-passe errados.');
    expect(props.onLogin).not.toHaveBeenCalled();
  });

  it('does not call the backend without user or password', () => {
    vi.spyOn(globalThis, 'fetch');
    setup();

    fill('admin', '');

    expect(screen.getByRole('alert')).toHaveTextContent('Escreve a palavra-passe.');
    expect(globalThis.fetch).not.toHaveBeenCalled();
  });

  it('no longer has the admin password in the frontend', async () => {
    const session = await import('../auth/session.js');
    expect(session.ADMIN_PASSWORD).toBeUndefined();
    expect(session.isAdminLogin).toBeUndefined();
  });
});
