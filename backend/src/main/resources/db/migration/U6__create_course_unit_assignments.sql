-- =====================================================
-- StudyMatch - reversão do V6 (atribuições às UCs)
-- Remove só as associações; UCs, alunos, contas e notas ficam.
-- =====================================================

BEGIN;

DROP TABLE IF EXISTS course_unit_students;
DROP TABLE IF EXISTS course_unit_teachers;

COMMIT;
