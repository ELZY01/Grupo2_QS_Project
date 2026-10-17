
-- =====================================================
-- StudyMatch - Sprint 2
-- V2: Insercao das quatro competencias-base
--
-- Estas competencias sao utilizadas pelo quiz inicial
-- e pelo calculo do perfil dos estudantes.
--
-- ON CONFLICT impede a duplicacao de registos.
-- =====================================================

INSERT INTO competencies
(code, name, description, is_base)
VALUES
    (
        'ORG',
        'Organização',
        'Capacidade de organizar tarefas e atividades académicas.',
        TRUE
    ),

    (
        'TEMPO',
        'Gestão de tempo',
        'Capacidade de gerir o tempo e cumprir prazos.',
        TRUE
    ),

    (
        'PROG',
        'Programação',
        'Capacidade de compreender e resolver problemas de programação.',
        TRUE
    ),

    (
        'EQUIPA',
        'Trabalho em grupo',
        'Capacidade de colaborar e comunicar com outros estudantes.',
        TRUE
    )

    ON CONFLICT (code) DO NOTHING;
