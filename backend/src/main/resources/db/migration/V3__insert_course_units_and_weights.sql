
-- =====================================================
-- StudyMatch - Sprint 2
-- V3: Unidades curriculares e pesos de exemplo
--
-- Dados ficticios para desenvolvimento e testes.
-- Nao representam um plano curricular oficial.
-- =====================================================

BEGIN;

-- 1. Criar unidades curriculares de exemplo.
INSERT INTO course_units
(code, name, ects, academic_year, semester)
VALUES
    ('PROG1', 'Programação I', 6, 1, 1),
    ('BD', 'Bases de Dados', 6, 2, 1),
    ('ES', 'Engenharia de Software', 6, 2, 2),
    ('POO', 'Programação Orientada a Objetos', 6, 1, 2)
    ON CONFLICT (code) DO NOTHING;

-- 2. Associar as UCs as competencias.
-- Os pesos representam a importancia relativa
-- de cada UC para uma determinada competencia.

INSERT INTO course_unit_competencies
(course_unit_id, competency_id, weight)
SELECT
    uc.id,
    c.id,
    dados.weight
FROM (
         VALUES
             ('PROG1', 'PROG', 1.0),
             ('PROG1', 'ORG', 0.3),
             ('BD', 'PROG', 0.8),
             ('BD', 'ORG', 0.5),
             ('ES', 'EQUIPA', 1.0),
             ('ES', 'TEMPO', 0.8),
             ('ES', 'ORG', 0.7),
             ('POO', 'PROG', 1.0),
             ('POO', 'EQUIPA', 0.4)
     ) AS dados(uc_code, competency_code, weight)
         JOIN course_units uc
              ON uc.code = dados.uc_code
         JOIN competencies c
              ON c.code = dados.competency_code
    ON CONFLICT (course_unit_id, competency_id) DO NOTHING;

COMMIT;
