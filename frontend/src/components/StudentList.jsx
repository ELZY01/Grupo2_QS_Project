/**
 * Tabela com os estudantes registados.
 * A linha do estudante em edição fica destacada.
 */
export default function StudentList({ students, onEdit, editingId = null }) {
  if (students.length === 0) {
    return (
      <div className="empty-state">
        <p className="empty-title">Ainda não há estudantes registados.</p>
        <p className="empty-text">
          Cria o primeiro no formulário ao lado. Os estudantes também se podem registar sozinhos ao
          entrar na app.
        </p>
      </div>
    );
  }

  return (
    <div className="table-wrap">
      <table className="student-table">
        <thead>
          <tr>
            <th>Número</th>
            <th>Nome</th>
            <th>Email</th>
            <th>Curso</th>
            <th className="num">Ano</th>
            <th aria-label="Ações" />
          </tr>
        </thead>
        <tbody>
          {students.map((student) => (
            <tr key={student.id} className={student.id === editingId ? 'is-editing' : undefined}>
              <td className="mono">{student.studentNumber}</td>
              <td className="strong">{student.name}</td>
              <td className="muted">{student.email}</td>
              <td className="muted">{student.course}</td>
              <td className="num mono">{student.academicYear}.º</td>
              <td>
                <button type="button" className="row-action" onClick={() => onEdit(student)}>
                  Editar
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
