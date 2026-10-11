
const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

/**
 * Consulta os registos guardados na base de dados Supabase.
 * O frontend comunica com o Spring Boot, que acede ao PostgreSQL.
 */
export async function getPersistencia() {
  // Envia um pedido GET ao endpoint de persistência.
  const response = await fetch(`${API_URL}/api/teste-persistencia`);

  // Se o servidor devolver um erro, informa quem chamou a função.
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  // Devolve os registos recebidos em formato JSON.
  return response.json();
}
