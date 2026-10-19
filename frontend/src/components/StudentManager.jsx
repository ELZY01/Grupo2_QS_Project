import { useCallback, useEffect, useState } from 'react';
import { createStudent, getStudents, updateStudent } from '../api/studentApi';
import StudentForm from './StudentForm';
import StudentList from './StudentList';
import './StudentManager.css';

/**
 * Página de gestão de estudantes: lista, criação e edição.
 * As chamadas à API estão em src/api/studentApi.js.
 */
export default function StudentManager() {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [editing, setEditing] = useState(null);
  const [formError, setFormError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(null);

  // Carrega a lista de estudantes.
  const loadStudents = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      setStudents(await getStudents());
    } catch (error) {
      setLoadError(error.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStudents();
  }, [loadStudents]);

  // Cria ou atualiza, conforme haja um estudante em edição.
  const handleSubmit = async (data) => {
    setSaving(true);
    setFormError(null);
    setSuccess(null);
    try {
      if (editing) {
        const updated = await updateStudent(editing.id, data);
        setStudents((list) => list.map((s) => (s.id === updated.id ? updated : s)));
        setSuccess('Estudante atualizado.');
      } else {
        const created = await createStudent(data);
        setStudents((list) => [...list, created]);
        setSuccess('Estudante criado.');
      }
      setEditing(null);
    } catch (error) {
      // Mostra o erro de validação devolvido pela API.
      setFormError(error.message);
    } finally {
      setSaving(false);
    }
  };

  const handleEdit = (student) => {
    setEditing(student);
    setFormError(null);
    setSuccess(null);
  };

  const handleCancel = () => {
    setEditing(null);
    setFormError(null);
  };

  return (
    <section className="student-manager">
      <h2>Estudantes</h2>

      <StudentForm
        student={editing}
        onSubmit={handleSubmit}
        onCancel={handleCancel}
        error={formError}
        saving={saving}
      />

      {success && (
        <p className="student-success" role="status">
          {success}
        </p>
      )}

      {loading && <p>A carregar estudantes...</p>}

      {!loading && loadError && (
        <p className="student-error" role="alert">
          Não foi possível carregar os estudantes ({loadError}).
        </p>
      )}

      {!loading && !loadError && <StudentList students={students} onEdit={handleEdit} />}
    </section>
  );
}
