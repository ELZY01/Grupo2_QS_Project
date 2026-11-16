-- OBS: Script corrido no SQL

-- SCRIPT DE DADOS DE DEMONSTRAÇÃO (ISSUE #78) - 23+ ALUNOS (A partir do ID 16)

-- 1. INSERÇÃO DOS 23 ALUNOS (IDs 16 a 38)
INSERT INTO students (id, student_number, name, email, course, academic_year) VALUES
  -- GRUPO 1: Alunos em Cold Start (Apenas questionário inicial - 5 alunos)
  (16, '2026016', 'Ana ColdStart', 'ana.cold@alunos.upt.pt', 'Engenharia Informática', 1),
  (17, '2026017', 'Bruno ColdStart', 'bruno.cold@alunos.upt.pt', 'Engenharia Informática', 1),
  (18, '2026018', 'Carla ColdStart', 'carla.cold@alunos.upt.pt', 'Engenharia Informática', 1),
  (19, '2026019', 'Daniel ColdStart', 'daniel.cold@alunos.upt.pt', 'Ciências da Computação', 1),
  (20, '2026020', 'Eva ColdStart', 'eva.cold@alunos.upt.pt', 'Ciências da Computação', 1),

  -- GRUPO 2: Alunos do 1º Ano com UCs e Notas Iniciais (5 alunos)
  (21, '2026021', 'Filipe PrimeiroAno', 'filipe.1ano@alunos.upt.pt', 'Engenharia Informática', 1),
  (22, '2026022', 'Gina PrimeiroAno', 'gina.1ano@alunos.upt.pt', 'Engenharia Informática', 1),
  (23, '2026023', 'Hugo PrimeiroAno', 'hugo.1ano@alunos.upt.pt', 'Engenharia Informática', 1),
  (24, '2026024', 'Inês PrimeiroAno', 'ines.1ano@alunos.upt.pt', 'Ciências da Computação', 1),
  (25, '2026025', 'João PrimeiroAno', 'joao.1ano@alunos.upt.pt', 'Ciências da Computação', 1),

  -- GRUPO 3: Alunos do 2º Ano com Histórico Consolidado (5 alunos)
  (26, '2026026', 'Katia SegundoAno', 'katia.2ano@alunos.upt.pt', 'Engenharia Informática', 2),
  (27, '2026027', 'Luís SegundoAno', 'luis.2ano@alunos.upt.pt', 'Engenharia Informática', 2),
  (28, '2026028', 'Marta SegundoAno', 'marta.2ano@alunos.upt.pt', 'Engenharia Informática', 2),
  (29, '2026029', 'Nuno SegundoAno', 'nuno.2ano@alunos.upt.pt', 'Ciências da Computação', 2),
  (30, '2026030', 'Olga SegundoAno', 'olga.2ano@alunos.upt.pt', 'Ciências da Computação', 2),

  -- GRUPO 4: Alunos do 3º Ano com Perfil Avançado (4 alunos)
  (31, '2026031', 'Pedro Avancado', 'pedro.adv@alunos.upt.pt', 'Engenharia Informática', 3),
  (32, '2026032', 'Rita Avancada', 'rita.adv@alunos.upt.pt', 'Engenharia Informática', 3),
  (33, '2026033', 'Sílvia Avancada', 'silvia.adv@alunos.upt.pt', 'Ciências da Computação', 3),
  (34, '2026034', 'Tiago Avancado', 'tiago.adv@alunos.upt.pt', 'Ciências da Computação', 3),

  -- GRUPO 5: Alunos com Perfil Misto / Retentores (4 alunos)
  (35, '2026035', 'Vasco Misto', 'vasco.misto@alunos.upt.pt', 'Engenharia Informática', 2),
  (36, '2026036', 'Xenia Mista', 'xenia.mista@alunos.upt.pt', 'Engenharia Informática', 2),
  (37, '2026037', 'Telmo Misto', 'telmo.misto@alunos.upt.pt', 'Ciências da Computação', 3),
  (38, '2026038', 'Zulmira Mista', 'zulmira.mista@alunos.upt.pt', 'Ciências da Computação', 3)
ON CONFLICT (id) DO NOTHING;



-- 2. SUBMISSÕES DE QUIZ E RESPOSTAS (Para o Grupo 1 - Cold Start: IDs 16 a 20)

INSERT INTO quiz_submissions (id, student_id, quiz_id, submitted_at)
SELECT id + 100, id, 1, NOW() - INTERVAL '1 day'
FROM students WHERE id BETWEEN 16 AND 20
ON CONFLICT (id) DO NOTHING;

-- Inserir respostas associadas a essas submissões
INSERT INTO answers (id, submission_id, question_id, value)
SELECT
  (qs.id * 10) + q.question_order,
  qs.id,
  q.id,
  ((qs.id + q.id) % 5) + 1
FROM quiz_submissions qs
CROSS JOIN questions q
WHERE qs.student_id BETWEEN 16 AND 20
ON CONFLICT (id) DO NOTHING;



-- 3. INSCRIÇÕES E NOTAS (Enrollments para os Grupos 2, 3, 4 e 5: IDs 21 a 38)
-- Utiliza as UCs reais: 1 (PROG1), 2 (BD), 3 (ES) e 4 (POO)

INSERT INTO enrollments (id, student_id, course_unit_id, school_year, semester, attempt_number, grade, status) VALUES
  -- Grupo 1º Ano (Inscritos em PROG1 e BD)
  (101, 21, 1, 2026, 1, 1, 14, 'APROVADO'),
  (102, 21, 2, 2026, 1, 1, 15, 'APROVADO'),
  (103, 22, 1, 2026, 1, 1, 13, 'APROVADO'),
  (104, 22, 2, 2026, 1, 1, 16, 'APROVADO'),
  (105, 23, 1, 2026, 1, 1, 12, 'APROVADO'),
  (106, 24, 2, 2026, 1, 1, 17, 'APROVADO'),
  (107, 25, 1, 2026, 1, 1, 15, 'APROVADO'),

  -- Grupo 2º Ano (Inscritos em POO e ES)
  (108, 26, 3, 2026, 1, 1, 14, 'APROVADO'),
  (109, 26, 4, 2026, 1, 1, 15, 'APROVADO'),
  (110, 27, 3, 2026, 1, 1, 12, 'APROVADO'),
  (111, 27, 4, 2026, 1, 1, 13, 'APROVADO'),
  (112, 28, 3, 2026, 1, 1, 18, 'APROVADO'),
  (113, 29, 4, 2026, 1, 1, 11, 'APROVADO'),
  (114, 30, 3, 2026, 1, 1, 16, 'APROVADO'),

  -- Grupo 3º Ano (Perfil Avançado - Múltiplas UCs com excelência)
  (115, 31, 1, 2026, 1, 1, 19, 'APROVADO'),
  (116, 31, 4, 2026, 1, 1, 18, 'APROVADO'),
  (117, 32, 2, 2026, 1, 1, 17, 'APROVADO'),
  (118, 32, 3, 2026, 1, 1, 20, 'APROVADO'),
  (119, 33, 1, 2026, 1, 1, 16, 'APROVADO'),
  (120, 34, 4, 2026, 1, 1, 15, 'APROVADO'),

  -- Grupo Misto / Retentores (Com tentativas falhadas e melhorias)
  (121, 35, 1, 2025, 1, 1, 7, 'REPROVADO'),
  (122, 35, 1, 2026, 1, 2, 10, 'APROVADO'),
  (123, 36, 2, 2025, 1, 1, 8, 'REPROVADO'),
  (124, 36, 2, 2026, 1, 2, 11, 'APROVADO'),
  (125, 37, 3, 2026, 1, 1, 9, 'REPROVADO'),
  (126, 38, 4, 2026, 1, 1, 10, 'APROVADO')
ON CONFLICT (id) DO NOTHING;