-- =====================================================
-- StudyMatch - reversão do V8 (UC dos contextos de agrupamento)
-- Remove só a ligação à UC; contextos, grupos e membros ficam.
-- =====================================================

BEGIN;

DROP INDEX IF EXISTS idx_gc_course_unit;

ALTER TABLE group_contexts
    DROP CONSTRAINT IF EXISTS fk_gc_course_unit;

ALTER TABLE group_contexts
    DROP COLUMN IF EXISTS course_unit_id;

COMMIT;
