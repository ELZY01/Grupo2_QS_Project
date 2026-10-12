
import { afterEach, describe, expect, it, vi } from 'vitest';
import { getPersistencia } from './persistenciaApi';

describe('getPersistencia', () => {

  // Limpa as simulações depois de cada teste.
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('devolve os registos da base de dados', async () => {
    // Simula uma resposta válida do Spring Boot.
    const registos = [
      { id: 1, descricao: 'Ligação StudyMatch funcionando' }
    ];

    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => registos
    });

    vi.stubGlobal('fetch', fetchMock);

    const resultado = await getPersistencia();

    // Confirma que a API foi chamada e devolveu os dados.
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/api/teste-persistencia')
    );
    expect(resultado).toEqual(registos);
  });

  it('lança um erro quando o backend falha', async () => {
    // Simula um erro HTTP 500.
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 500
    }));

    await expect(getPersistencia()).rejects.toThrow('HTTP 500');
  });
});
