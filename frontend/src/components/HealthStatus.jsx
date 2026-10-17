import { useCallback, useEffect, useState } from 'react';
import { getHealth } from '../api/healthApi';
import './HealthStatus.css';

export default function HealthStatus() {

  // Guarda o estado da ligação ao backend Spring Boot.
  const [state, setState] = useState({
    loading: true,
    data: null,
    error: null
  });

  // Consulta o endpoint de saúde do backend.
  const checkHealth = useCallback(async () => {
    setState({ loading: true, data: null, error: null });

    try {
      // Verifica se o Spring Boot está disponível.
      const data = await getHealth();

      setState({
        loading: false,
        data,
        error: null
      });
    } catch (error) {
      // Guarda a mensagem de erro se o backend não responder.
      setState({
        loading: false,
        data: null,
        error: error.message
      });
    }
  }, []);

  // Executa a verificação quando o componente é apresentado.
  useEffect(() => {
    checkHealth();
  }, [checkHealth]);

  const { loading, data, error } = state;
  const isUp = data?.status === 'UP';

  return (
    <section className="health-card" aria-live="polite">
      <h2>Estado do sistema</h2>

      {loading && <p>A verificar o backend...</p>}

      {!loading && data && (
        <dl className="health-details">
          <dt>Estado</dt>
          <dd>
            <span className={`health-badge ${isUp ? 'up' : 'down'}`}>
              {data.status}
            </span>
          </dd>

          <dt>Serviço</dt>
          <dd>{data.service}</dd>
        </dl>
      )}

      {!loading && error && (
        <p className="health-error" role="alert">
          Não foi possível contactar o backend ({error}).
        </p>
      )}

      <button type="button" onClick={checkHealth} disabled={loading}>
        Verificar novamente
      </button>
    </section>
  );
}