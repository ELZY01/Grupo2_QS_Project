/**
 * Tabela com os estudantes registados.
 */
export default function StudentList({ students, onEdit }) {
  if (students.length === 0) {
    return <p>Ainda não há estudantes registados.</p>;
  }

  return (
    <table className="student-table">
      <thead>
        <tr>
          <th>Número</th>
          <th>Nome</th>
          <th>Email</th>
          <th>Curso</th>
          <th>Ano</th>
          <th aria-label="Ações" />
        </tr>
      </thead>
      <tbody>
        {students.map((student) => (
          <tr key={student.id}>
            <td>{student.studentNumber}</td>
            <td>{student.name}</td>
            <td>{student.email}</td>
            <td>{student.course}</td>
            <td>{student.academicYear}</td>
            <td>
              <button type="button" onClick={() => onEdit(student)}>
                Editar
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
