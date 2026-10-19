import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import StudentManager from './StudentManager';

const ANA = {
  id: 1,
  studentNumber: '2024001',
  name: 'Ana Silva',
  email: 'ana@upt.pt',
  course: 'Engenharia Informática',
  academicYear: 2,
};

const jsonResponse = (status, body) => ({
  ok: status >= 200 && status < 300,
  status,
  json: async () => body,
});

const fillForm = (values) => {
  for (const [label, value] of Object.entries(values)) {
    fireEvent.change(screen.getByLabelText(label), { target: { value } });
  }
};

describe('StudentManager', () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('lists the students returned by the API', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, [ANA]));

    render(<StudentManager />);

    expect(await screen.findByText('Ana Silva')).toBeInTheDocument();
    expect(globalThis.fetch).toHaveBeenCalledWith('http://localhost:8080/api/students');
  });

  it('creates a student and adds it to the list', async () => {
    const created = { ...ANA, id: 2, name: 'Bruno Costa', email: 'bruno@upt.pt', studentNumber: '2024002' };
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(jsonResponse(200, []))
      .mockResolvedValueOnce(jsonResponse(201, created));

    render(<StudentManager />);
    await screen.findByText('Ainda não há estudantes registados.');

    fillForm({
      'Número de estudante': '2024002',
      Nome: 'Bruno Costa',
      Email: 'bruno@upt.pt',
      Curso: 'Engenharia Informática',
      'Ano curricular': '2',
    });
    fireEvent.click(screen.getByRole('button', { name: 'Criar estudante' }));

    expect(await screen.findByText('Bruno Costa')).toBeInTheDocument();
    const [url, options] = globalThis.fetch.mock.calls[1];
    expect(url).toBe('http://localhost:8080/api/students');
    expect(options.method).toBe('POST');
    expect(JSON.parse(options.body).academicYear).toBe(2);
  });

  it('shows the validation error returned by the API', async () => {
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(jsonResponse(200, []))
      .mockResolvedValueOnce(jsonResponse(400, { message: 'O formato do email é inválido.' }));

    render(<StudentManager />);
    await screen.findByText('Ainda não há estudantes registados.');

    fillForm({ Email: 'email-errado' });
    fireEvent.click(screen.getByRole('button', { name: 'Criar estudante' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('O formato do email é inválido.');
  });

  it('shows a clear message for duplicates when the API sends no text', async () => {
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(jsonResponse(200, []))
      .mockResolvedValueOnce(jsonResponse(409, { status: 409, error: 'Conflict' }));

    render(<StudentManager />);
    await screen.findByText('Ainda não há estudantes registados.');

    fireEvent.click(screen.getByRole('button', { name: 'Criar estudante' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'O email ou o número de estudante já está registado.'
    );
  });

  it('edits a student with PUT', async () => {
    const updated = { ...ANA, name: 'Ana Sousa' };
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(jsonResponse(200, [ANA]))
      .mockResolvedValueOnce(jsonResponse(200, updated));

    render(<StudentManager />);
    fireEvent.click(await screen.findByRole('button', { name: 'Editar' }));

    expect(screen.getByLabelText('Nome')).toHaveValue('Ana Silva');
    fillForm({ Nome: 'Ana Sousa' });
    fireEvent.click(screen.getByRole('button', { name: 'Guardar alterações' }));

    expect(await screen.findByText('Ana Sousa')).toBeInTheDocument();
    const [url, options] = globalThis.fetch.mock.calls[1];
    expect(url).toBe('http://localhost:8080/api/students/1');
    expect(options.method).toBe('PUT');
  });

  it('shows an error when the list cannot be loaded', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('Failed to fetch'));

    render(<StudentManager />);

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Não foi possível carregar os estudantes'
    );
  });
});
