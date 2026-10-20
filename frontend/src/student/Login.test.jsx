import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Login from './Login.jsx';

const STUDENTS = [{ id: 3, studentNumber: '2024099', email: 'tf@upt.pt', name: 'Teste Francisco' }];

const setup = () => {
  const props = { onBack: vi.fn(), onRegister: vi.fn(), onStudent: vi.fn(), onAdmin: vi.fn() };
  render(<Login {...props} />);
  return props;
};

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe('Login', () => {
  it('asks for a password only for the admin user and rejects a wrong one', () => {
    const props = setup();
    expect(screen.queryByLabelText('Palavra-passe')).not.toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Número de estudante ou email'), { target: { value: 'admin' } });
    fireEvent.change(screen.getByLabelText('Palavra-passe'), { target: { value: 'errada' } });
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(screen.getByRole('alert')).toHaveTextContent('Palavra-passe de administrador errada.');
    expect(props.onAdmin).not.toHaveBeenCalled();
  });

  it('lets the admin in with the right password', () => {
    const props = setup();
    fireEvent.change(screen.getByLabelText('Número de estudante ou email'), { target: { value: 'admin' } });
    fireEvent.change(screen.getByLabelText('Palavra-passe'), { target: { value: 'studymatch' } });
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }));
    expect(props.onAdmin).toHaveBeenCalled();
  });

  it('finds a student by number or email', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(STUDENTS) });
    const props = setup();
    fireEvent.change(screen.getByLabelText('Número de estudante ou email'), { target: { value: 'TF@upt.pt' } });
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }));
    await vi.waitFor(() => expect(props.onStudent).toHaveBeenCalledWith(STUDENTS[0]));
  });
});
