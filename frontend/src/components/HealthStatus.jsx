
import { useCallback, useEffect, useState } from 'react';
import { getHealth } from '../api/healthApi';
import { getPersistencia } from '../api/persistenciaApi';
import './HealthStatus.css';

export default function HealthStatus() {

  // Guarda o estado do backend.
  const [state, setState] = useState({
    loading: true,
    data: null,
    error: null
  });

  // Guarda os registos consultados na base de dados.
  const [registos, setRegistos] = useState([]);

  // Guarda possíveis erros da consulta ao Supabase.
  const [erroBD, setErroBD] = useState(null);

  // Consulta o backend e a base de dados.
  const checkHealth = useCallback(async () => {
    setState({ loading: true, data: null, error: null });
    setRegistos([]);
    setErroBD(null);

    try {
      // Verifica se o Spring Boot está a funcionar.
      const data = await getHealth();

      setState({
        loading: false,
        data,
        error: null
      });
    } catch (error) {
      setState({
        loading: false,
        data: null,
        error: error.message
      });
    }

    try {
      // O Spring Boot consulta os dados no Supabase.
      const dadosBD = await getPersistencia();
      setRegistos(dadosBD);
    } catch (error) {
      setErroBD(error.message);
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

      <hr />

      <h3>Base de dados Supabase</h3>

      {erroBD && (
        <p className="health-error" role="alert">
          Erro ao consultar a base de dados: {erroBD}
        </p>
      )}

      {!erroBD && registos.length > 0 && (
        <>
          <p>Ligação à base de dados estabelecida.</p>

          <ul>
            {registos.map((registo) => (
              <li key={registo.id}>
                {registo.descricao}
              </li>
            ))}
          </ul>
        </>
      )}

      {!erroBD && !loading && registos.length === 0 && (
        <p>Sem registos para apresentar.</p>
      )}
    </section>
  );
}
