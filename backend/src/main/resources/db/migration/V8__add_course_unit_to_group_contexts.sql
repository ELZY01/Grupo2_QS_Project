-- =====================================================
-- StudyMatch - Grupos de uma UC (Issue #106)
-- V8: liga cada contexto de agrupamento a uma unidade curricular
--
-- O docente só gere grupos das suas UCs (R4). Para isso o contexto
-- tem de saber a que UC pertence. A coluna aceita NULL porque os
-- contextos criados pelo admin em /api/group-contexts (#69) não têm UC.
--
-- Correr no Supabase depois do V7 e antes de arrancar o backend
-- (ddl-auto=validate). Reverter com U8__add_course_unit_to_group_contexts.sql.
-- =====================================================

BEGIN;

ALTER TABLE group_contexts
    ADD COLUMN IF NOT EXISTS course_unit_id BIGINT;

ALTER TABLE group_contexts
    ADD CONSTRAINT fk_gc_course_unit
        FOREIGN KEY (course_unit_id) REFERENCES course_units(id) ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_gc_course_unit
    ON group_contexts(course_unit_id);

COMMIT;
