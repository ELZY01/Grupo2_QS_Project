import { useCallback, useEffect, useState } from 'react';
import { getHealth } from '../api/healthApi';
import './HealthStatus.css';

export default function HealthStatus() {
  const [state, setState] = useState({ loading: true, data: null, error: null });

  const checkHealth = useCallback(async () => {
    setState({ loading: true, data: null, error: null });
    try {
      const data = await getHealth();
      setState({ loading: false, data, error: null });
    } catch (error) {
      setState({ loading: false, data: null, error: error.message });
    }
  }, []);

  useEffect(() => {
    checkHealth();
  }, [checkHealth]);

  const { loading, data, error } = state;
  const isUp = data?.status === 'UP';

  return (
    <section className="health-card" aria-live="polite">
      <h2>Estado do sistema</h2>

      {loading && <p className="health-loading">A verificar o backend…</p>}

      {!loading && data && (
        <dl className="health-details">
          <dt>Estado</dt>
          <dd>
            <span className={`health-badge ${isUp ? 'up' : 'down'}`}>{data.status}</span>
          </dd>
          <dt>Serviço</dt>
          <dd>{data.service}</dd>
        </dl>
      )}

      {!loading && error && (
        <p className="health-error" role="alert">
          Não foi possível contactar o backend ({error}). Confirma que está a correr em
          http://localhost:8080.
        </p>
      )}

      <button type="button" onClick={checkHealth} disabled={loading}>
        Verificar novamente
      </button>
    </section>
  );
}
