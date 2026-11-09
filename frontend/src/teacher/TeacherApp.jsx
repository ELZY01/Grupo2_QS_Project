/**
 * Área do docente. Por agora só confirma a sessão; as UCs, os alunos e as
 * estatísticas chegam com as atribuições feitas pelo admin.
 */
export default function TeacherApp({ name, onLogout }) {
  return (
    <main className="sx-center">
      <section className="sx-card">
        <h1>Olá, {name}</h1>
        <p className="sx-muted">Ainda não tens unidades curriculares atribuídas.</p>
        <div className="sx-actions">
          <button type="button" onClick={onLogout}>
            Sair
          </button>
        </div>
      </section>
    </main>
  );
}
