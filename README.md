# StudyMatch

> Projeto de grupo de **Qualidade de Software** â€” Grupo 2 Â· Universidade Portucalense

O StudyMatch Ã© uma aplicaÃ§Ã£o web que forma grupos de trabalho equilibrados a partir das competÃªncias dos estudantes. As competÃªncias sÃ£o calculadas com base no percurso acadÃ©mico de cada aluno: UCs, inscriÃ§Ãµes e notas.

## Contexto do produto

Nos trabalhos de grupo, os grupos costumam formar-se por afinidade ou ao acaso. Isso cria grupos desequilibrados: uns concentram os alunos mais fortes e outros ficam sem ninguÃ©m que domine as Ã¡reas crÃ­ticas. O StudyMatch resolve este problema em quatro passos:

1. **Representa o percurso acadÃ©mico** de cada estudante: as UCs, as tentativas e as notas.
2. **Converte notas em competÃªncias.** Cada UC contribui, com um peso, para uma ou mais competÃªncias, numa escala de 1 a 5.
3. **ConstrÃ³i um perfil de competÃªncias** do estudante, com os pontos fortes e fracos.
4. **Gera grupos equilibrados** num contexto definido pelo docente (UC, atividade e tamanho do grupo).

### Problemas que resolvemos

| Problema | SoluÃ§Ã£o |
|---|---|
| **Arranque a frio:** um aluno novo ainda nÃ£o tem notas | Quiz inicial de 20 perguntas em 4 Ã¡reas (organizaÃ§Ã£o, gestÃ£o de tempo, programaÃ§Ã£o, trabalho em grupo) que dÃ¡ um perfil de 1 a 5 desde o primeiro dia. O peso do quiz diminui Ã  medida que o aluno conclui UCs. |
| **EquilÃ­brio dos grupos** | Os alunos sÃ£o ordenados pela mÃ©dia do perfil e distribuÃ­dos em serpentina (G1, G2, G2, G1, â€¦), o que dÃ¡ grupos com mÃ©dias praticamente iguais. O docente pode ajustar os grupos manualmente. |

### Funcionalidades propostas pela equipa

- **ExplicaÃ§Ã£o da formaÃ§Ã£o do grupo:** o aluno vÃª porque ficou num grupo, sem ver as notas dos colegas.
- **ImportaÃ§Ã£o por CSV:** o docente importa alunos e notas de um ficheiro.
- **AvaliaÃ§Ã£o entre pares:** no fim do trabalho, os colegas avaliam-se de forma anÃ³nima, e essa avaliaÃ§Ã£o atualiza a competÃªncia "trabalho em grupo".

### Atores

- **Estudante:** regista o percurso, responde ao quiz, consulta o perfil e participa em grupos.
- **Docente:** define competÃªncias, cria contextos de agrupamento, gera e ajusta grupos.

## Stack tecnolÃ³gica

| Camada | Tecnologia |
|---|---|
| Frontend | React + Vite (npm) |
| Backend / API | Java 21 + Spring Boot (API REST), Maven |
| PersistÃªncia | PostgreSQL (Supabase) via Spring Data JPA |
| Testes | JUnit 5 + Spring Boot Test Â· Vitest + React Testing Library |
| GestÃ£o | Git + GitHub (Issues, Projects, Pull Requests) |

A justificaÃ§Ã£o das escolhas estÃ¡ no [ADR-001](docs/decisions/ADR-001-stack-tecnologica.md).

## Estrutura do repositÃ³rio

```
Grupo2_QS_Project/
â”œâ”€â”€ backend/            # API Spring Boot (Maven)
â”œâ”€â”€ frontend/           # AplicaÃ§Ã£o React (Vite)
â”œâ”€â”€ docs/
â”‚   â”œâ”€â”€ casos-de-uso.md
â”‚   â”œâ”€â”€ modelo-dominio.md
â”‚   â”œâ”€â”€ arquitetura.md
â”‚   â””â”€â”€ decisions/      # DecisÃµes de arquitetura (ADR)
â”œâ”€â”€ CONTRIBUTING.md     # Workflow, convenÃ§Ãµes e Definition of Done
â””â”€â”€ README.md
```

## Como correr o projeto

### Requisitos

- **Java 21** (o Maven Wrapper vem incluÃ­do no projeto, nÃ£o Ã© preciso instalar o Maven)
- **Node.js 20+** (inclui o npm)
- **Credenciais da base de dados Supabase** (pedir Ã  equipa). Nunca entram no repositÃ³rio.

### 1. Backend

O endereÃ§o da base de dados jÃ¡ estÃ¡ em `backend/src/main/resources/application.properties`. O utilizador e a palavra-passe sÃ£o lidos de variÃ¡veis de ambiente, que tÃªm de ser definidas **no mesmo terminal** antes de arrancar:

| VariÃ¡vel | Valor |
|---|---|
| `DB_USERNAME` | utilizador da base de dados Supabase |
| `DB_PASSWORD` | palavra-passe da base de dados Supabase |

```bash
# Linux / macOS / Git Bash
export DB_USERNAME=...
export DB_PASSWORD=...
cd backend
./mvnw spring-boot:run
```

```bat
:: Windows (cmd)
set DB_USERNAME=...
set DB_PASSWORD=...
cd backend
mvnw.cmd spring-boot:run
```

A API arranca em http://localhost:8080. Para verificar, abrir http://localhost:8080/api/health, que deve devolver:

```json
{"status":"UP","service":"studymatch-backend"}
```

### Contas e autenticaÃ§Ã£o

O backend usa Spring Security: todos os pedidos (exceto `GET /api/health`, login e registo) precisam de sessÃ£o, e cada perfil sÃ³ acede ao que lhe pertence (403 caso contrÃ¡rio).

| Perfil | Como entra | O que pode fazer |
|---|---|---|
| Admin | utilizador `admin` | estudantes, notas, docentes e inscriÃ§Ãµes nas UCs |
| Docente | utilizador dado pelo admin | as suas UCs, os alunos inscritos e as estatÃ­sticas de cada aluno |
| Aluno | nÃºmero de estudante ou email | sÃ³ os seus dados (perfil, quiz, histÃ³rico) |

No arranque, o backend cria (se nÃ£o existirem) as contas de demonstraÃ§Ã£o e a UC `QS-TESTE` associada ao docente. As palavras-passe por omissÃ£o sÃ£o sÃ³ para desenvolvimento; em produÃ§Ã£o definir as variÃ¡veis de ambiente:

| VariÃ¡vel | Por omissÃ£o |
|---|---|
| `ADMIN_USER` / `ADMIN_PASSWORD` | `admin` / `studymatch` |
| `DOCENTE_USER` / `DOCENTE_PASSWORD` | `docente` / `docente123` |

**Antes de arrancar esta versÃ£o contra o Supabase**, correr no SQL editor, por esta ordem, `backend/src/main/resources/db/migration/V5__create_app_users.sql` e `V6__create_course_unit_assignments.sql` (com `ddl-auto=validate`, o backend nÃ£o arranca sem as tabelas). Para reverter: `U6__â€¦` e depois `U5__â€¦`. Os alunos que jÃ¡ existiam nÃ£o tÃªm conta: o admin define-lhes a palavra-passe em *Estudantes â†’ Editar*.

### 2. Frontend

Num segundo terminal:

```bash
cd frontend
cp .env.example .env          # Windows (cmd): copy .env.example .env
npm install
npm run dev
```

O ficheiro `.env` define `VITE_API_URL=http://localhost:8080`, o endereÃ§o do backend. A aplicaÃ§Ã£o abre em http://localhost:5173 e mostra o estado do backend (UP).

#### AvanÃ§o automÃ¡tico do quiz do aluno (Issue #126)

No quiz, ao escolher uma resposta, o ecrÃ£ passa sozinho para a pergunta seguinte ao fim de 220 ms. Esse avanÃ§o nÃ£o era cancelado, o que dava dois problemas: duas respostas seguidas muito rÃ¡pidas saltavam uma pergunta, e o avanÃ§o corria mesmo depois de o quiz sair do ecrÃ£. Era isto que fazia o `npm test` mostrar o erro `window is not defined` no fim dos testes. Agora o avanÃ§o pendente Ã© cancelado quando hÃ¡ uma nova escolha, quando o aluno usa "Anterior" ou "Seguinte" e quando o quiz fecha.

Os trÃªs testes novos estÃ£o em `frontend/src/student/StudentQuiz.test.jsx` e usam timers simulados (`vi.useFakeTimers`). Sem a correÃ§Ã£o, os trÃªs falham.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| does not skip a question when the student changes the answer quickly | Duas respostas Ã  1.Âª pergunta antes dos 220 ms | O quiz avanÃ§a sÃ³ para a 2.Âª pergunta |
| stays on the previous question when the student goes back right after answering | Responder Ã  2.Âª pergunta e carregar logo em "Anterior" | O quiz fica na 1.Âª pergunta |
| cancels the pending move when the quiz is closed | Responder e fechar o quiz antes dos 220 ms | NÃ£o fica nenhum timer pendente |

### Testes

```bash
cd backend && ./mvnw clean test   # backend (Windows: mvnw.cmd clean test)
cd frontend && npm test           # frontend (Vitest)
```

Os testes do backend **nÃ£o precisam das credenciais da base de dados**: usam uma base H2 em memÃ³ria (perfil `test`). O `clean test` compila o projeto e corre todos os testes unitÃ¡rios, incluindo as regras do perfil de competÃªncias (`ProfileCalculatorTest`).

#### O que cada teste verifica

O `main` tem 167 testes no backend e 92 no frontend. No backend hÃ¡ 135 mÃ©todos `@Test`. A diferenÃ§a vem dos testes parametrizados, que correm uma vez por cada valor da lista.

Usamos quatro tipos de teste:

- **UnitÃ¡rio** (JUnit 5 + Mockito): testa uma classe sozinha, com os repositÃ³rios simulados. NÃ£o arranca o Spring nem usa base de dados.
- **Web** (`@WebMvcTest`): arranca sÃ³ o controller e as regras do Spring Security. O serviÃ§o Ã© simulado, por isso o que se testa Ã© o HTTP: estado da resposta, JSON e permissÃµes.
- **Backend completo** (`@SpringBootTest` + H2): arranca a aplicaÃ§Ã£o toda com a base H2 em memÃ³ria e faz pedidos reais Ã  API, com login e token.
- **Frontend** (Vitest + React Testing Library): mostra o componente como o utilizador o vÃª e simula a API.

Cada classe estÃ¡ num bloco fechado. Clica no nome para abrir.

##### Backend: perfil e formaÃ§Ã£o de grupos

<details>
<summary><code>ProfileCalculatorTest</code> Â· unitÃ¡rio Â· 15 mÃ©todos, 33 execuÃ§Ãµes</summary>

Regras do perfil de competÃªncias (Issue #44, ADR-002). Os pesos usados sÃ£o os mesmos da migraÃ§Ã£o `V3`.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `convertePelaFormula` | ConversÃ£o de nota em nÃ­vel para 10, 15, 20 e 12 | NÃ­veis 1,0; 3,0; 5,0 e 1,8 |
| `rejeitaNotasNaoAprovadasOuForaDoIntervalo` | Notas 9, 0 e 21 | Erro: sÃ³ notas de 10 a 20 tÃªm nÃ­vel |
| `mediaPonderadaPelosPesos` | PROG com PROG1 = 20 (peso 1,0) e BD = 10 (peso 0,8) | NÃ­vel (1,0 Ã— 5 + 0,8 Ã— 1) / 1,8 â‰ˆ 3,22, origem "histÃ³rico", contributos que somam 100% |
| `contaAMelhorTentativa` | TrÃªs tentativas a PROG1 (8, 12 e 16) | Conta sÃ³ o 16 e a UC conta uma vez como concluÃ­da |
| `reprovacoesEEmCursoNaoContam` | Uma reprovaÃ§Ã£o (9) e uma inscriÃ§Ã£o em curso | 0 UCs concluÃ­das e PROG fica sem dados |
| `novaNotaAtualizaOPerfil` | Acrescentar POO = 20 a quem sÃ³ tinha PROG1 = 12 | O nÃ­vel de PROG passa de 1,8 para 3,4 |
| `semNotasEhSoQuiz` | Aluno sem notas, sÃ³ com o quiz | Peso do quiz 100% e cada nÃ­vel igual ao do quiz |
| `exemploDoUc09` | Exemplo do caso de uso UC09: 3 UCs com 15, quiz 4 | Peso do quiz 0,5 e nÃ­vel 3,5, origem "misto" |
| `limitesDoPesoDoQuiz` | Peso do quiz com 0, 1, 3, 5, 6, 7 e 20 UCs concluÃ­das | 1,0 â†’ 0,83 â†’ 0,5 â†’ 0,17 â†’ 0 a partir de 6, nunca negativo |
| `comNUcsEhSoHistorico` | Aluno com 6 UCs concluÃ­das | O quiz deixa de contar e o nÃ­vel vem sÃ³ das notas |
| `competenciaSemHistoricoUsaQuiz` | EQUIPA sem nenhuma UC que a alimente | Usa sÃ³ o quiz (nÃ­vel 2,0, peso 1,0) |
| `semDadosNenhuns` | Aluno sem quiz e sem notas | As 4 competÃªncias ficam "sem dados", sem nÃ­vel |
| `notaForaDoIntervalo` | Notas -1, 21 e 25 | Erro ao criar o resultado acadÃ©mico |
| `pesoForaDoIntervalo` | Pesos 0,0; -0,5 e 1,5 | Erro: o peso tem de estar entre 0 e 1 |
| `quizForaDoIntervalo` | Resultados do quiz 0,0; 0,99; 5,01 e 6,0 | Erro: o quiz vai de 1 a 5 |

</details>

<details>
<summary><code>GroupCandidateTest</code> Â· unitÃ¡rio Â· 8 testes</summary>

O aluno como o algoritmo de grupos o vÃª (Issue #73, ADR-003).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `averageOfAllLevels` | MÃ©dia de um aluno com as 4 competÃªncias | 3,5, e o aluno tem dados |
| `missingLevelIsIgnored` | Aluno sem nÃ­vel em TEMPO | A competÃªncia em falta nÃ£o entra na mÃ©dia como 0 |
| `noDataUsesNeutralAverageForSorting` | Aluno sem dados nenhuns | MÃ©dia vazia; para ordenar usa o valor neutro 3,0 |
| `boundaryLevelsAreAccepted` | NÃ­veis 1 e 5 | Aceites |
| `levelOutOfRangeIsRejected` | NÃ­veis 0,9 e 5,1 | Erro |
| `requiredFields` | ID, nÃºmero, nome ou nÃ­veis em falta | Erro em cada um dos quatro casos |
| `levelsAreCopied` | Mudar o mapa original depois de criar o aluno | Os nÃ­veis do aluno nÃ£o mudam e nÃ£o se podem alterar |
| `fromProfile` | Converter um perfil do `ProfileCalculator` | ORG = 4,0 passa e PROG continua "sem dados" |

</details>

<details>
<summary><code>GroupFormationTest</code> Â· unitÃ¡rio Â· 12 mÃ©todos, 26 execuÃ§Ãµes</summary>

DistribuiÃ§Ã£o dos alunos em grupos de 4 a 5 (Issues #70 e #72).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `shouldReturnEmptyListWhenThereAreNoStudents` | Lista de alunos vazia | Nenhum grupo |
| `shouldRejectNullStudentList` | Lista nula | Erro |
| `shouldRejectNullStudents` | Lista com um aluno nulo | Erro |
| `shouldRejectStudentsWithoutProfile` | Aluno sem perfil | Erro |
| `shouldRejectStudentsWithoutStudentNumber` | Aluno sem nÃºmero de estudante | Erro |
| `shouldCreateTwoGroupsOfFourStudents` | 8 alunos | 2 grupos de 4 |
| `shouldCreateTwoGroupsOfFiveStudents` | 10 alunos | 2 grupos de 5 |
| `shouldRejectImpossibleNumberOfStudents` | 6 alunos (nÃ£o dÃ¡ grupos de 4 a 5) | Erro |
| `shouldCreateGroupsWithValidSizes` | 4, 5, 8, 9, 10 e de 12 a 21 alunos | Todos os grupos com 4 ou 5 e ninguÃ©m fica de fora |
| `shouldDistributeEveryStudentExactlyOnce` | 13 alunos | Cada aluno aparece uma Ãºnica vez |
| `shouldDistributeStudentsDeterministically` | Os mesmos alunos duas vezes | Os mesmos grupos das duas vezes |
| `shouldAcceptStudentsWithoutCompetencyData` | Alunos sem dados de competÃªncias | Formam-se os grupos na mesma (8 alunos, 2 grupos) |

</details>

##### Backend: serviÃ§os

<details>
<summary><code>StudentServiceTest</code> Â· unitÃ¡rio Â· 4 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `deveCriarEstudanteValido` | Criar um estudante com dados vÃ¡lidos | Ã‰ guardado uma vez, com o nome certo |
| `deveRejeitarEmailDuplicado` | Email que jÃ¡ existe | 409 e nada Ã© guardado |
| `deveRejeitarNomeVazio` | Nome em branco | 400 |
| `deveRejeitarEstudanteInexistente` | Procurar um estudante que nÃ£o existe | 404 |

</details>

<details>
<summary><code>EnrollmentServiceTest</code> Â· unitÃ¡rio Â· 9 testes</summary>

InscriÃ§Ãµes e notas no histÃ³rico acadÃ©mico.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `shouldAcceptGradeZero` | Nota 0 | Aceite, 1.Âª tentativa, estado "reprovado" |
| `shouldAcceptGradeTwenty` | Nota 20 | Aceite, estado "aprovado" |
| `shouldRejectNegativeGrade` | Nota negativa | 400 e nada Ã© guardado |
| `shouldRejectGradeAboveTwenty` | Nota acima de 20 | 400 |
| `shouldAcceptEnrollmentInProgress` | InscriÃ§Ã£o sem nota | Aceite, estado "em curso" |
| `shouldRejectGradeWhenInProgress` | InscriÃ§Ã£o "em curso" com nota | 400 |
| `shouldIncrementAttemptNumber` | Nova inscriÃ§Ã£o numa UC com 2 tentativas | Fica como 3.Âª tentativa |
| `shouldRejectUnknownStudent` | Inscrever um aluno que nÃ£o existe | 404 |
| `shouldReturnStudentHistory` | Pedir o histÃ³rico de um aluno | Devolve a inscriÃ§Ã£o com nota 16, "aprovado" |

</details>

<details>
<summary><code>ProfileServiceTest</code> Â· unitÃ¡rio Â· 4 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `estudanteInexistenteDevolve404` | Perfil de um aluno que nÃ£o existe | 404 |
| `perfilComHistoricoUsaAMelhorNotaDeCadaUc` | Aluno com notas e sem quiz | Conta a melhor nota de cada UC: 2 UCs concluÃ­das e ProgramaÃ§Ã£o = (5 + 1) / 2, com 2 contributos |
| `alunoSoComQuizTemPerfilPelasMediasDeCadaArea` | Aluno sÃ³ com o quiz | Peso do quiz 100%; PROG = 4,0 e EQUIPA = 2,0 |
| `alunoSemQuizNemNotasFicaSemDados` | Aluno sÃ³ com uma UC em curso | 0 UCs concluÃ­das e todas as competÃªncias "sem dados" |

</details>

<details>
<summary><code>QuizServiceTest</code> Â· unitÃ¡rio Â· 4 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `testSubmitQuiz_NormalSubmission` | Submeter as 20 respostas com valor 4 | Guarda 1 submissÃ£o e 20 respostas |
| `testSubmitQuiz_InvalidAnswerCount_ThrowsException` | Submeter sÃ³ 19 respostas | 400 "O quiz de arranque a frio deve conter exatamente 20 respostas." |
| `testSubmitQuiz_BoundaryValues_AllOnes` | Todas as respostas com 1, o mÃ­nimo | Aceite, 20 respostas guardadas |
| `testSubmitQuiz_BoundaryValues_AllFives` | Todas as respostas com 5, o mÃ¡ximo | Aceite, 20 respostas guardadas |

</details>

<details>
<summary><code>CourseUnitServiceTest</code> Â· unitÃ¡rio Â· 2 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `returnsCourseUnitsOrderedByYearSemesterAndCode` | Listar as UCs | Ordenadas por ano, semestre e cÃ³digo, com nome e ECTS |
| `returnsEmptyListWhenThereAreNoCourseUnits` | NÃ£o hÃ¡ UCs | Lista vazia |

</details>

<details>
<summary><code>TeacherServiceTest</code> Â· unitÃ¡rio Â· 4 testes</summary>

Ãrea do docente (R3, R7).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `listsOnlyTheUnitsAssignedToTheTeacherWithTheStudentCount` | UCs da docente | SÃ³ a UC dela (QS), com 24 alunos inscritos |
| `listsTheStudentsOfAnAssignedUnit` | Alunos de uma UC da docente | Devolve a Ana |
| `forbidsTheStudentsOfAUnitFromAnotherTeacher` | Alunos de uma UC de outro docente | 403 |
| `returns404ForAnUnknownUnit` | UC que nÃ£o existe | 404 |

</details>

<details>
<summary><code>AuthServiceTest</code> Â· unitÃ¡rio Â· 9 testes</summary>

Login, registo e palavras-passe.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `logsInWithTheRightPasswordAndReturnsAWorkingToken` | Login do admin com espaÃ§os Ã  volta do nome | Entra como ADMIN e o token devolvido Ã© vÃ¡lido |
| `rejectsAWrongPasswordAndAnUnknownUserWithTheSameMessage` | Palavra-passe errada e utilizador que nÃ£o existe | 401 com a mesma mensagem nos dois casos, para nÃ£o revelar que contas existem |
| `blocksTheUserAfter5FailedAttemptsEvenWithTheRightPassword` | 5 falhas seguidas e depois a palavra-passe certa | 429: a conta fica bloqueada durante um tempo |
| `aSuccessfulLoginResetsTheFailures` | Falhas, um login certo e mais falhas | O contador volta a zero e o login seguinte entra |
| `aStudentCanLogInWithTheEmail` | Aluno a entrar com o email | Entra como ALUNO com o ID de estudante certo |
| `registersTheStudentWithAHashedPasswordAndStartsTheSession` | Registo de um aluno | Conta ALUNO criada, palavra-passe guardada sÃ³ como hash e sessÃ£o iniciada |
| `rejectsAShortPasswordBeforeCreatingTheStudent` | Registo com palavra-passe curta | 400 antes de criar o aluno |
| `adminCreatesTheAccountOfAnOldStudentWithoutOne` | Admin define a palavra-passe de um aluno sem conta | A conta Ã© criada com o nÃºmero de estudante |
| `adminResetsTheExistingPassword` | Admin muda a palavra-passe de um aluno com conta | A nova palavra-passe passa a funcionar |

</details>

<details>
<summary><code>CourseUnitAssignmentServiceTest</code> Â· unitÃ¡rio Â· 9 testes</summary>

AtribuiÃ§Ãµes feitas pelo admin (R1, R2).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `enrollsAStudentInTheCourseUnit` | Inscrever a Ana em QS | A inscriÃ§Ã£o Ã© guardada com a UC e o aluno certos |
| `rejectsAStudentAlreadyEnrolledInTheSameCourseUnit` | Inscrever a Ana outra vez | 409 "Este aluno jÃ¡ estÃ¡ inscrito nesta UC." |
| `returns404ForUnknownCourseUnitOrStudent` | UC ou aluno que nÃ£o existem | 404 |
| `removesAnEnrolledStudentAndFailsIfNotEnrolled` | Remover um aluno inscrito e um que nÃ£o estÃ¡ | O primeiro sai; o segundo dÃ¡ 404 |
| `assignsATeacherToTheCourseUnit` | Associar uma docente a QS | A associaÃ§Ã£o Ã© guardada |
| `onlyTeacherAccountsCanBeAssigned` | Associar uma conta de aluno como docente | 400 |
| `rejectsAssigningTheSameTeacherTwice` | Associar a mesma docente duas vezes | 409 |
| `createsATeacherAccountWithAHashedPassword` | Criar uma conta de docente com espaÃ§os nos campos | Nome e utilizador sem espaÃ§os, role DOCENTE e palavra-passe em hash |
| `rejectsADuplicateUsernameOrAShortPassword` | Utilizador repetido ou palavra-passe curta | 409 e 400 |

</details>

##### Backend: seguranÃ§a

<details>
<summary><code>TokenServiceTest</code> Â· unitÃ¡rio Â· 3 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `issuesARandomTokenThatResolvesToTheUser` | Emitir dois tokens | SÃ£o diferentes, tÃªm pelo menos 40 caracteres e levam ao utilizador certo |
| `rejectsUnknownBlankAndRevokedTokens` | Token revogado, inventado, vazio ou nulo | Nenhum Ã© aceite |
| `expiresAfterTheTtl` | Um token depois do tempo de validade | VÃ¡lido antes, invÃ¡lido depois |

</details>

<details>
<summary><code>LoginThrottleTest</code> Â· unitÃ¡rio Â· 3 testes</summary>

Limite de tentativas de login (Issue #82).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `blocksAfterTheMaximumFailuresIgnoringCase` | Falhas com "admin" e " ADMIN " | Contam para a mesma conta e bloqueiam-na; outra conta nÃ£o Ã© afetada |
| `unblocksWhenTheFailuresLeaveTheWindow` | 3 falhas e 15 minutos e 1 segundo depois | A conta jÃ¡ nÃ£o estÃ¡ bloqueada |
| `successClearsTheFailures` | 2 falhas, um sucesso e mais 1 falha | NÃ£o bloqueia: o sucesso apagou as falhas anteriores |

</details>

<details>
<summary><code>SecurityAccessTest</code> Â· backend completo Â· 15 testes</summary>

Quem pode chamar o quÃª, com pedidos reais Ã  API (R1, R7, RN05, RN16).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `healthIsPublic` | `GET /api/health` sem sessÃ£o | 200 |
| `protectedEndpointsNeedASession` | Alunos, UCs e quiz sem sessÃ£o | 401 "Tens de iniciar sessÃ£o." |
| `anInvalidTokenIsTreatedAsNoSession` | Token inventado | 401 |
| `wrongPasswordIsRejected` | Login com a palavra-passe errada | 401 "Utilizador ou palavra-passe errados." |
| `adminManagesStudents` | Admin a listar alunos e a ver a prÃ³pria sessÃ£o | 200 e role ADMIN |
| `logoutInvalidatesTheToken` | Usar o token depois do logout | 401 |
| `aStudentOnlySeesTheirOwnData` | Ana a ver os seus dados e os do Rui | Os dela dÃ£o 200; os do Rui e a lista de alunos dÃ£o 403 |
| `aStudentCannotChangeEnrollmentsOrOtherStudents` | Ana a criar inscriÃ§Ãµes, alterar o Rui, submeter o quiz dele ou mudar palavras-passe | 403 em todos |
| `aStudentCanLogInWithTheEmail` | Login com o email | 200, role ALUNO e o ID certo |
| `registrationNeedsAPasswordWithAtLeast8Characters` | Registo com "curta" | 400 "A palavra-passe tem de ter pelo menos 8 caracteres." |
| `adminResetsAStudentPassword` | Admin muda a palavra-passe de um aluno | 204, e a palavra-passe antiga deixa de entrar |
| `aTeacherCannotUseTheAdminAreaNorSeeStudentsOutsideTheirUnits` | Docente na Ã¡rea do admin e num aluno fora das suas UCs | 403 |
| `studentsCannotUseTheTeacherArea` | Aluno na Ã¡rea do docente | 403 |
| `onlyTheAdminEnrollsStudentsInCourseUnits` | Docente, aluno e admin a inscrever um aluno numa UC | 403, 403 e 200; repetir dÃ¡ 409 |
| `onlyTheAdminAssignsTeachers` | Docente a listar docentes; admin a ver as atribuiÃ§Ãµes | 403 para o docente; o admin vÃª a docente associada |

</details>

<details>
<summary><code>TeacherAccessTest</code> Â· backend completo Â· 5 testes</summary>

Ãrea do docente com pedidos reais Ã  API (R3, R6, R7).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `theTeacherSeesTheirUnitTheEnrolledStudentsAndTheirStats` | Docente na sua UC | VÃª a UC, os alunos inscritos e o perfil, o histÃ³rico e os dados de cada um |
| `theTeacherCannotSeeStudentsOutsideTheirUnits` | Perfil e histÃ³rico de um aluno fora das suas UCs | 403 |
| `theTeacherCannotOpenAnotherTeachersUnit` | UC de outro docente e UC que nÃ£o existe | 403 "Esta UC nÃ£o te estÃ¡ atribuÃ­da." e 404 |
| `aTeacherWithoutUnitsSeesNothing` | Docente acabado de criar | Lista vazia e 403 em UCs e alunos |
| `studentsAndAdminsCannotUseTheTeacherArea` | Admin e pedido sem sessÃ£o na Ã¡rea do docente | 403 e 401 |

</details>

##### Backend: controllers (camada web)

<details>
<summary><code>HealthControllerTest</code> Â· web Â· 3 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `health_returnsOkAndServiceStatus_evenWithoutAuth` | `GET /api/health` sem sessÃ£o | 200 com `status: UP` e `service: studymatch-backend` |
| `unknownEndpoint_withoutAuth_returns401` | Rota que nÃ£o existe, sem sessÃ£o | 401 |
| `postOnHealth_withoutAuth_returns401` | `POST /api/health` sem sessÃ£o | 401: sÃ³ o `GET` Ã© pÃºblico |

</details>

<details>
<summary><code>CourseUnitControllerTest</code> Â· web Â· 4 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `getAllCourseUnits_withoutAuth_returns401` | Lista de UCs sem sessÃ£o | 401 e o serviÃ§o nem Ã© chamado |
| `getAllCourseUnits_whenAuthenticatedAsStudent_returns200AndList` | Lista de UCs com sessÃ£o de aluno | 200 com as 2 UCs e todos os campos no JSON |
| `getAllCourseUnits_whenEmpty_returns200AndEmptyArray` | NÃ£o hÃ¡ UCs | 200 com `[]` |
| `unknownSubRoute_whenAuthenticated_returns404` | `GET /api/course-units/999` | 404 |

</details>

<details>
<summary><code>QuizControllerTest</code> Â· web Â· 13 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `getQuiz_withoutAuth_returns401` | Perguntas do quiz sem sessÃ£o | 401 |
| `getQuiz_whenQuestionsExist_returns200AndQuestions` | Perguntas do quiz com sessÃ£o | 200 com a pergunta, o ID e o texto |
| `getQuiz_whenNoQuestionsFound_returns404` | Quiz sem perguntas | 404 |
| `submitQuiz_withoutAuth_returns401` | Submeter sem sessÃ£o | 401 |
| `submitQuiz_whenNotSelf_returns403` | Submeter o quiz de outro aluno | 403 |
| `submitQuiz_whenValidRequestAndDefaultQuizId_returns200` | SubmissÃ£o vÃ¡lida do prÃ³prio aluno | 200 com o ID da submissÃ£o; usa o quiz 1 por omissÃ£o |
| `submitQuiz_whenCustomQuizIdParam_passesQuizIdToService` | Submeter com `?quizId=2` | O serviÃ§o recebe o quiz 2 |
| `submitQuiz_whenInvalidPayloadOrServiceBadRequest_returns400` | O serviÃ§o rejeita as respostas | 400 |
| `submitQuiz_whenMissingRequestBody_returns400` | Pedido sem corpo | 400 |
| `getLatestQuizResults_withoutAuth_returns401` | Resultados sem sessÃ£o | 401 |
| `getLatestQuizResults_whenCannotView_returns403` | Resultados de um aluno que nÃ£o se pode ver | 403 |
| `getLatestQuizResults_whenAuthorized_returns200AndCompetencyMap` | Resultados com permissÃ£o | 200 com a mÃ©dia por Ã¡rea (LideranÃ§a 4,2; ComunicaÃ§Ã£o 3,8) |
| `getLatestQuizResults_whenStudentOrSubmissionNotFound_returns404` | Aluno ou submissÃ£o que nÃ£o existem | 404 |

</details>

<details>
<summary><code>TeacherControllerTest</code> Â· web Â· 8 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `myCourseUnits_withoutAuth_returns401` | UCs do docente sem sessÃ£o | 401 |
| `myCourseUnits_whenAuthenticatedAsStudent_returns403` | UCs do docente com sessÃ£o de aluno | 403 |
| `myCourseUnits_whenAuthenticatedAsTeacher_returns200AndList` | UCs do docente com sessÃ£o de docente | 200 com a UC e 15 alunos |
| `students_withoutAuth_returns401` | Alunos de uma UC sem sessÃ£o | 401 |
| `students_whenAuthenticatedAsStudent_returns403` | Alunos de uma UC com sessÃ£o de aluno | 403 |
| `students_whenTeacherNotAssignedToCourseUnit_returns403` | UC que nÃ£o Ã© do docente | 403 |
| `students_whenCourseUnitNotFound_returns404` | UC que nÃ£o existe | 404 |
| `students_whenValidAssignment_returns200AndStudentSummaryList` | UC do docente | 200 com a lista de alunos |

</details>

<details>
<summary><code>BackendApplicationTests</code> Â· backend completo Â· 1 teste</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `contextLoads` | Arrancar a aplicaÃ§Ã£o toda com o perfil `test` | Arranca sem erros, o que confirma que a configuraÃ§Ã£o e os beans batem certo |

</details>

##### Frontend

Os testes do frontend correm com `npm test`. A API Ã© sempre simulada, por isso nÃ£o precisam do backend ligado.

<details>
<summary><code>api/http.test.js</code> Â· 4 testes</summary>

| Teste | O que verifica |
|---|---|
| sends the session token in the Authorization header | Com sessÃ£o, cada pedido leva o token no cabeÃ§alho `Authorization` |
| does not add a header without a session | Sem sessÃ£o, o pedido vai sem cabeÃ§alho |
| warns the app when the session expired (401 with a token) | Um 401 com token avisa a aplicaÃ§Ã£o de que a sessÃ£o expirou |
| accepts 204 responses without a body | Uma resposta 204 sem corpo nÃ£o dÃ¡ erro |

</details>

<details>
<summary><code>api/quizApi.test.js</code> Â· 4 testes</summary>

| Teste | O que verifica |
|---|---|
| uses the same API address as the other files | O quiz usa o mesmo endereÃ§o da API que o resto da aplicaÃ§Ã£o |
| sends the answers of the student | As respostas vÃ£o no pedido para o aluno certo |
| shows the message sent by the API | Quando a API falha, aparece a mensagem que ela enviou |
| uses a clear message when the API does not send one | Sem mensagem da API, aparece um texto claro em vez de um erro tÃ©cnico |

</details>

<details>
<summary><code>components/AcademicHistory.test.jsx</code> Â· 5 testes</summary>

| Teste | O que verifica |
|---|---|
| shows every attempt with UC name, grade and status, ordered by school year | Todas as tentativas aparecem, com UC, nota e estado, por ano letivo |
| registers an enrollment with POST and notifies the parent | Registar uma inscriÃ§Ã£o faz `POST` e avisa o ecrÃ£ que contÃ©m o histÃ³rico |
| shows the validation error returned by the API | O erro de validaÃ§Ã£o da API aparece no formulÃ¡rio |
| sends no grade for an enrollment in progress | Uma inscriÃ§Ã£o "em curso" vai sem nota |
| shows an error when the history cannot be loaded | Se o histÃ³rico nÃ£o carrega, aparece um erro |

</details>

<details>
<summary><code>components/CompetencyProfile.test.jsx</code> Â· 9 testes</summary>

| Teste | O que verifica |
|---|---|
| shows each competency with its level, origin and the quiz weight | Cada competÃªncia mostra nÃ­vel, origem e peso do quiz |
| explains a level with the UCs, grades, weights and shares | A explicaÃ§Ã£o de um nÃ­vel lista as UCs, notas, pesos e percentagens |
| shows "Sem dados" with a suggested action | CompetÃªncia sem dados mostra "Sem dados" e o que fazer a seguir |
| asks for the initial quiz when it is missing | Sem quiz, o perfil pede para o fazer |
| reloads the profile when refreshKey changes | O perfil volta a carregar quando hÃ¡ dados novos |
| shows the API error when the student does not exist | Aluno que nÃ£o existe mostra o erro da API |
| talks about "this student" when a teacher is viewing the profile | Para o docente o texto fala "deste aluno" em vez de "tu" |
| says there is no quiz when the student never did it | Perfil sÃ³ com notas diz que o aluno nÃ£o fez o quiz (#89) |
| says the quiz no longer counts after 6+ UCs when the quiz was done | Com 6 ou mais UCs, diz que o quiz jÃ¡ nÃ£o conta (#89) |

</details>

<details>
<summary><code>components/CourseUnitAssignments.test.jsx</code> Â· 6 testes</summary>

| Teste | O que verifica |
|---|---|
| shows the teachers and the enrolled students of the chosen unit | Mostra os docentes e os alunos da UC escolhida |
| assigns a teacher to the unit (R2) | O admin associa um docente Ã  UC |
| only offers students that are not enrolled yet and enrolls them (R1) | SÃ³ aparecem para inscrever alunos que ainda nÃ£o estÃ£o na UC |
| removes an enrolled student | Remover um aluno inscrito |
| shows the backend error, e.g. a duplicate enrollment | Erros do backend, como uma inscriÃ§Ã£o repetida, aparecem no ecrÃ£ |
| creates a teacher account | Criar uma conta de docente |

</details>

<details>
<summary><code>components/HealthStatus.test.jsx</code> Â· 2 testes</summary>

| Teste | O que verifica |
|---|---|
| shows UP when the backend answers | Com o backend ligado, aparece UP |
| shows an error when the backend is down | Com o backend em baixo, aparece um erro |

</details>

<details>
<summary><code>components/StudentManager.test.jsx</code> Â· 8 testes</summary>

| Teste | O que verifica |
|---|---|
| lists the students returned by the API | Lista os alunos que a API devolve |
| creates a student and adds it to the list | Criar um aluno junta-o Ã  lista |
| shows the validation error returned by the API | O erro de validaÃ§Ã£o da API aparece |
| shows a clear message for duplicates when the API sends no text | Num duplicado sem mensagem da API, aparece um texto claro |
| asks for an initial password of 8+ characters before creating | SÃ³ cria com uma palavra-passe de 8 ou mais caracteres |
| sets a new password when editing only if one is typed | Ao editar, a palavra-passe sÃ³ muda se for escrita uma nova |
| edits a student with PUT | Editar um aluno faz `PUT` |
| shows an error when the list cannot be loaded | Se a lista nÃ£o carrega, aparece um erro |

</details>

<details>
<summary><code>components/ThemeToggle.test.jsx</code> Â· 4 testes</summary>

| Teste | O que verifica |
|---|---|
| starts from the system theme when nothing was chosen | Sem escolha guardada, segue o tema do sistema |
| switches to night mode and remembers the choice | Muda para o modo noturno e guarda a escolha |
| applies the stored choice before the app renders | A escolha guardada Ã© aplicada antes de a pÃ¡gina aparecer |
| ignores invalid stored values | Um valor guardado invÃ¡lido Ã© ignorado |

</details>

<details>
<summary><code>groups/GroupContexts.test.jsx</code> Â· 12 testes</summary>

EcrÃ£ dos contextos e grupos (#74) e troca manual de alunos (#75).

| Teste | O que verifica |
|---|---|
| creates a context with the chosen students and shows the group split | Cria o contexto com os alunos escolhidos e mostra como ficam os grupos |
| blocks an impossible configuration with a clear message | Um nÃºmero de alunos que nÃ£o dÃ¡ grupos de 4 a 5 Ã© bloqueado com uma mensagem clara |
| shows the API error when the backend rejects the generation | Se o backend recusa gerar os grupos, aparece o erro |
| shows the Pix animation for at least 2 s while generating | A animaÃ§Ã£o aparece pelo menos 2 segundos enquanto gera |
| opens a previous generation from the history | Abre uma geraÃ§Ã£o antiga a partir do histÃ³rico |
| shows members, average, strongest per area and the gap to the other groups | Cada grupo mostra membros, mÃ©dia, o mais forte por Ã¡rea e a diferenÃ§a para os outros |
| never shows the grades or levels of other students (RN26) | Nunca aparecem notas nem nÃ­veis de outros alunos (RN26) |
| warns when the gap between groups is above the limit | Avisa quando a diferenÃ§a entre grupos passa o limite |
| swaps two students and shows the recalculated averages | Trocar dois alunos atualiza as mÃ©dias |
| does not call the API when both students are in the same group | Dois alunos do mesmo grupo nÃ£o chegam Ã  API |
| warns when the swap leaves the groups more than 0,5 apart | Avisa quando a troca deixa os grupos a mais de 0,5 |
| shows the API error when the swap is rejected | Se a API recusa a troca, aparece o erro |

</details>

<details>
<summary><code>groups/groupPlan.test.js</code> Â· 11 testes</summary>

| Teste | O que verifica |
|---|---|
| rejects the impossible configurations | NÃºmeros de alunos que nÃ£o dÃ£o grupos de 4 a 5 sÃ£o recusados |
| only makes groups of 5 and 4 | Os grupos tÃªm sempre 5 ou 4 alunos |
| handles the 11/12 boundary | O caso de 11 e 12 alunos dÃ¡ a divisÃ£o certa |
| explains the split or why it is impossible | O texto explica a divisÃ£o ou porque Ã© impossÃ­vel |
| uses a comma and one decimal place | Os nÃºmeros aparecem com vÃ­rgula e uma casa decimal |
| shows the sign of the gap | A diferenÃ§a entre grupos mostra o sinal (+ ou âˆ’) |
| finds the group of a student | Encontra o grupo de um aluno |
| accepts two students from different groups | Aceita trocar alunos de grupos diferentes |
| asks for both students | Pede os dois alunos antes de trocar |
| rejects two students from the same group | Recusa trocar dois alunos do mesmo grupo |
| rejects a student that is not in any group | Recusa um aluno que nÃ£o estÃ¡ em nenhum grupo |

</details>

<details>
<summary><code>mascot/GroupForming.test.jsx</code> Â· 6 testes</summary>

| Teste | O que verifica |
|---|---|
| shows a squad of four Pix, one banner per area, and the loading text | A animaÃ§Ã£o mostra quatro Pix, uma bandeira por Ã¡rea e o texto de espera |
| only shows the text when the user prefers reduced motion | Com "reduzir movimento" ligado no sistema, sÃ³ aparece o texto |
| still renders the lobby when matchMedia is not available | Funciona em browsers sem `matchMedia` |
| keeps the animation visible for the minimum time when the request is fast | Se o pedido Ã© rÃ¡pido, a animaÃ§Ã£o fica o tempo mÃ­nimo |
| hides right away when the request took longer than the minimum | Se o pedido demora mais do que o mÃ­nimo, a animaÃ§Ã£o some logo |
| stays hidden if it never started | Se nÃ£o comeÃ§ou, nÃ£o aparece |

</details>

<details>
<summary><code>mascot/mascot.test.js</code> Â· 8 testes</summary>

| Teste | O que verifica |
|---|---|
| uses only the base dino when there is no data | Sem dados, aparece o dinossauro base |
| colours the body with the strongest area and stacks every accessory | A cor vem da Ã¡rea mais forte e os acessÃ³rios juntam-se |
| keeps the base colour while no area reaches level 3 | Abaixo do nÃ­vel 3 em todas as Ã¡reas, fica a cor base |
| marks a tie and takes the colour from the first area in order | Num empate, fica marcado e usa a cor da primeira Ã¡rea |
| keeps the plain base dino when every level rounds to 1 | Com tudo no nÃ­vel 1, fica o dinossauro simples |
| starts with a whole egg | O ovo comeÃ§a inteiro |
| cracks more as the answers go up | O ovo racha mais Ã  medida que o aluno responde |
| has one sprite per stage | HÃ¡ uma imagem para cada fase do ovo |

</details>

<details>
<summary><code>student/Login.test.jsx</code> Â· 4 testes</summary>

| Teste | O que verifica |
|---|---|
| always asks for the password and checks it in the backend | Pede sempre a palavra-passe e quem a valida Ã© o backend |
| shows the error for wrong credentials | Credenciais erradas mostram o erro |
| does not call the backend without user or password | Sem utilizador ou palavra-passe, nÃ£o faz o pedido |
| no longer has the admin password in the frontend | A palavra-passe do admin jÃ¡ nÃ£o estÃ¡ no cÃ³digo do frontend |

</details>

<details>
<summary><code>student/StudentQuiz.test.jsx</code> Â· 2 testes</summary>

| Teste | O que verifica |
|---|---|
| shows one question at a time and cracks the egg as the student answers | Uma pergunta de cada vez, e o ovo racha a cada resposta |
| only lets the egg hatch when every question is answered | O ovo sÃ³ choca quando todas as perguntas estÃ£o respondidas |

</details>

<details>
<summary><code>teacher/TeacherApp.test.jsx</code> Â· 6 testes</summary>

| Teste | O que verifica |
|---|---|
| lists only the teacher units and opens the first one with its students | Mostra sÃ³ as UCs do docente e abre a primeira com os alunos |
| shows the stats of the chosen student (profile and read-only history) | Mostra o perfil e o histÃ³rico do aluno, sem poder editar |
| shows a clear empty state without units | Docente sem UCs vÃª uma mensagem clara |
| shows a clear empty state for a unit without students | UC sem alunos mostra uma mensagem clara |
| shows the 403 message from the backend | Um 403 do backend mostra a mensagem dele |
| has a groups section with a clear empty state and a logout button | Tem a secÃ§Ã£o dos grupos, com estado vazio, e o botÃ£o de sair |

</details>

<details>
<summary><code>AdminApp.test.jsx</code> Â· 1 teste</summary>

| Teste | O que verifica |
|---|---|
| shows the ultra rare admin Pix and lets the admin log out | Mostra o Pix do admin e deixa sair da sessÃ£o |

</details>

#### Cobertura dos testes (JaCoCo)

O mesmo `clean test` gera o relatÃ³rio de cobertura do backend (JaCoCo), sem nenhum comando extra:

```bash
cd backend && ./mvnw clean test      # Windows: mvnw.cmd clean test
# abrir backend/target/site/jacoco/index.html no browser (Windows: start target\site\jacoco\index.html)
```

O relatÃ³rio mostra a cobertura de linhas e de ramos por pacote e por classe. A mediÃ§Ã£o inicial, as classes mais fracas e a meta estÃ£o em [`docs/qualidade/cobertura.md`](docs/qualidade/cobertura.md). O `jacoco.xml` gerado Ã© o que o SonarQube lÃª para mostrar a cobertura.

#### Testes do `QuizService` (Issue #120)

Testes unitÃ¡rios com Mockito (sem base de dados) em `backend/src/test/java/com/studymatch/service/QuizServiceTest.java`. Cobrem os caminhos de erro do quiz de arranque a frio, que antes nÃ£o eram testados.

| Cobertura do `QuizService` | Linhas | Ramos |
|---|---|---|
| Antes | 40,7% | 30,0% |
| Depois | **100%** | **100%** |

Com estes testes, a cobertura do projeto passou de 78,7% para 82,4% nas linhas e de 69,5% para 73,8% nos ramos.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `submitQuiz_PedidoNulo_Devolve400` | Submeter o quiz sem pedido | 400 e nada Ã© guardado |
| `submitQuiz_ListaDeRespostasNula_Devolve400` | Pedido sem a lista de respostas | 400 e nada Ã© guardado |
| `submitQuiz_RespostasAMais_Devolve400` | Pedido com 21 respostas (o quiz tem 20) | 400 e nada Ã© guardado |
| `submitQuiz_AlunoInexistente_Devolve404` | SubmissÃ£o de um aluno que nÃ£o existe | 404 "Estudante nÃ£o encontrado" e a submissÃ£o nÃ£o Ã© guardada |
| `submitQuiz_QuizInexistente_Devolve404` | SubmissÃ£o para um quiz que nÃ£o existe | 404 "Quiz nÃ£o encontrado" e a submissÃ£o nÃ£o Ã© guardada |
| `submitQuiz_PerguntaInexistente_Devolve400ComOId` | Resposta a uma pergunta que nÃ£o existe | 400 com o ID da pergunta na mensagem e nenhuma resposta guardada |
| `submitQuiz_ValorForaDaEscala_Devolve400` | Resposta com valor 6 (a escala Ã© de 1 a 5) | 400 e nenhuma resposta guardada |
| `getAllQuizzes_DevolveOsQuizzesDoRepositorio` | Listar os quizzes | Devolve os quizzes que estÃ£o na base de dados |
| `getQuizQuestions_QuizInexistente_Devolve404` | Pedir as perguntas de um quiz que nÃ£o existe | 404 e as perguntas nem chegam a ser procuradas |
| `getQuizQuestions_DevolvePerguntasPorOrdem` | Pedir as perguntas de um quiz, com e sem o quiz associado | Devolve as perguntas pela ordem do repositÃ³rio |
| `calculateResults_CalculaAMediaPorArea` | Resultado de uma submissÃ£o com respostas de duas Ã¡reas | MÃ©dia por Ã¡rea: ProgramaÃ§Ã£o (5, 4, 3) = 4,0 e OrganizaÃ§Ã£o (1, 2) = 1,5 |
| `calculateResults_SemRespostas_DevolveMapaVazio` | Resultado de uma submissÃ£o sem respostas | Mapa vazio, sem erro |
| `getLatestSubmission_AlunoInexistente_Devolve404` | Ãšltima submissÃ£o de um aluno que nÃ£o existe | 404 e as submissÃµes nem chegam a ser procuradas |
| `getLatestSubmission_AlunoSemSubmissoes_Devolve404` | Ãšltima submissÃ£o de um aluno que nunca fez o quiz | 404 "Nenhuma submissÃ£o encontrada para o estudante com ID 1" |
| `getLatestResults_UsaASubmissaoMaisRecente` | Resultados do aluno quando hÃ¡ vÃ¡rias submissÃµes | Usa sÃ³ a submissÃ£o mais recente (mÃ©dia 3,0 a partir de 2 e 4) |

Para gerar o artefacto do backend (`backend/target/backend-*.jar`):

```bash
cd backend && ./mvnw clean package   # Windows: mvnw.cmd clean package
```

#### Testes da autenticaÃ§Ã£o e das atribuiÃ§Ãµes (Issue #122)

Testes que cobrem os ramos que faltavam no acesso aos dados dos estudantes, no login e nas atribuiÃ§Ãµes feitas pelo admin. SÃ£o testes unitÃ¡rios com Mockito, exceto o `SecurityAccessTest`, que arranca o backend completo com a base H2.

| Classe | Antes (linhas / ramos) | Depois (linhas / ramos) |
|---|---|---|
| `StudentAccess` | 85,7% / 60,0% | **100% / 100%** |
| `AuthService` | 94,6% / 66,7% | **100% / 87,5%** |
| `CourseUnitAssignmentService` | 86,8% / 65,4% | **100% / 96,2%** |
| `SecurityConfig` | 87,7% / 50,0% | **100%** / 50,0% |

No `SecurityConfig` fica por cobrir um Ãºnico ramo: o `parseId` receber `null`. Isso nÃ£o acontece na prÃ¡tica, porque a regra sÃ³ se aplica a URLs com o `{studentId}`. Com estes testes, a cobertura do projeto passou de 78,7% para 80,4% nas linhas e de 69,5% para 76,1% nos ramos.

**`StudentAccessTest`** (`security/`): quem pode ver e alterar os dados de um estudante (RN05, RN16).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `currentUserIsNullWithoutASession` | Pedido sem sessÃ£o | NÃ£o hÃ¡ utilizador atual (`null`) |
| `currentUserIsNullWhenThePrincipalIsNotAnAuthenticatedUser` | SessÃ£o cujo utilizador nÃ£o Ã© uma conta do StudyMatch | NÃ£o hÃ¡ utilizador atual (`null`) |
| `currentUserReturnsTheLoggedInUser` | SessÃ£o de um aluno | Devolve o aluno da sessÃ£o |
| `nobodySeesAStudentWithoutASessionOrWithoutAnId` | Ver um aluno sem sessÃ£o, ou sem indicar o ID | Acesso recusado, sem consultar as UCs do docente |
| `theAdminSeesEveryStudent` | O admin a ver alunos diferentes | Acesso permitido a todos |
| `aStudentOnlySeesTheirOwnData` | Um aluno a ver os seus dados e os de outro | SÃ³ vÃª os seus |
| `aTeacherOnlySeesStudentsOfTheirUnits` | Um docente a ver um aluno da sua UC e um de fora | SÃ³ vÃª o aluno da sua UC |
| `onlyTheAdminAndTheStudentThemselvesCanEdit` | Alterar dados como admin, como o prÃ³prio aluno, como outro aluno e sem sessÃ£o | SÃ³ o admin e o prÃ³prio aluno podem alterar |
| `aTeacherCannotEditEvenAStudentOfTheirUnit` | Um docente a alterar dados de um aluno | Recusado, mesmo que o aluno esteja na sua UC |
| `isSelfOnlyForTheStudentWithTheSameId` | "Ã‰ o prÃ³prio aluno?" com o ID certo, sem ID, sem sessÃ£o e para o admin | SÃ³ Ã© verdade para o aluno com o mesmo ID |

**`AuthServiceTest`** (`service/`): testes novos do login, do registo e do logout.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `aLoginWithoutDataIsRejectedWithoutLookingForTheAccount` | Login sem utilizador nem palavra-passe | 401, sem procurar a conta na base de dados |
| `aStudentWithoutAnAccountCannotLogInWithTheEmail` | Aluno que existe mas ainda nÃ£o tem conta a entrar com o email | 401 |
| `anEmptyRegistrationIsRejected` | Registo sem dados | 400 e o aluno nÃ£o Ã© criado |
| `aRegistrationIsRejectedWhenTheStudentNumberAlreadyHasAnAccount` | Registo com um nÃºmero de estudante que jÃ¡ tem conta | 409 e a conta nÃ£o Ã© guardada |
| `logoutInvalidatesTheToken` | Terminar a sessÃ£o | O token deixa de ser vÃ¡lido |

**`CourseUnitAssignmentServiceTest`** (`service/`): testes novos das atribuiÃ§Ãµes do admin (R1, R2).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `listsTheTeacherAccounts` | Listar as contas de docente | Devolve os docentes com o utilizador certo |
| `aTeacherNeedsAUsernameAndAName` | Criar um docente sem dados, com o utilizador em branco ou sem nome | 400 e a conta nÃ£o Ã© guardada |
| `nullIdsAreTreatedAsNotFound` | Pedidos com o ID da UC, do docente ou do aluno em falta | 404 |
| `removesAnAssignedTeacherAndFailsIfNotAssigned` | Remover um docente associado Ã  UC e um que nÃ£o estÃ¡ | O primeiro Ã© removido; o segundo dÃ¡ 404 "Este docente nÃ£o estÃ¡ associado a esta UC." |
| `theAssignmentsListTheTeachersAndTheEnrolledStudents` | Consultar as atribuiÃ§Ãµes de uma UC | Devolve o docente e o aluno inscritos |

**`SecurityAccessTest`** (`security/`, backend completo) e **`SecurityConfigTest`** (`config/`): regras do Spring Security.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `aStudentIdThatIsNotANumberIsDenied` | Um aluno a pedir `/api/students/abc` e `/api/students/abc/profile` | 403 |
| `loadsTheAccountWithItsHashAndRole` | Carregar uma conta da tabela `app_users` | Utilizador, hash da palavra-passe e role (`ROLE_DOCENTE`) certos |
| `anUnknownUsernameIsNotFound` | Carregar um utilizador que nÃ£o existe | `UsernameNotFoundException` |

#### Regras e estatÃ­sticas dos grupos de uma UC (Issue #106)

Primeira parte da #106: as regras de criar e editar grupos Ã  mÃ£o (R4, R5) e as estatÃ­sticas por grupo (R6). EstÃ£o no pacote `grouping`, em Java puro, sem Spring nem base de dados. Assim o service dos grupos (#69) sÃ³ tem de ir buscar os dados aos repositÃ³rios e chamar estas classes, e a UI nÃ£o fica presa ao algoritmo.

- `GroupMembershipRules.requireTeacherOfUnit`: a UC tem de ser do docente. Se nÃ£o for, dÃ¡ `GroupAccessDeniedException` (o service responde **403**).
- `GroupMembershipRules.validateMembers`: valida a lista final de membros de um grupo. Os alunos nÃ£o podem estar repetidos, tÃªm de estar inscritos na UC e nÃ£o podem estar noutro grupo do mesmo contexto, e o tamanho tem de respeitar os limites. Se alguma regra falhar, dÃ¡ `GroupMembershipConflictException` (o service responde **409**).
- `GroupSizeLimits`: o tamanho dos grupos Ã© de 4 a 5 alunos por omissÃ£o e pode ser configurado por contexto.
- `GroupStatistics.ofAll`: calcula, para cada grupo, o nÂº de membros, a mÃ©dia, a diferenÃ§a para os outros grupos, se estÃ¡ equilibrado (|diferenÃ§a| â‰¤ 0,5) e as lacunas (Ã¡reas sem ninguÃ©m com nÃ­vel â‰¥ 3,5). Por competÃªncia (`AreaStatistics`) dÃ¡ a mÃ©dia, o mÃ­nimo, o mÃ¡ximo, o nÂº de alunos fortes e a distribuiÃ§Ã£o pelos nÃ­veis 1 a 5. A mÃ©dia do grupo usa as mesmas regras do algoritmo: um aluno sem dados conta como 3,0.

SÃ£o classes novas, por isso nÃ£o hÃ¡ cobertura "antes".

| Classe | Linhas | Ramos |
|---|---|---|
| `GroupMembershipRules` | **100%** (36/36) | **100%** (22/22) |
| `GroupStatistics` | **100%** (50/50) | **100%** (32/32) |
| `AreaStatistics` | **100%** (7/7) | **100%** (4/4) |
| `GroupSizeLimits` | **100%** (8/8) | **100%** (8/8) |

Com estes testes, a cobertura do projeto passou de 88,9% para 89,8% nas linhas e de 82,4% para 84,9% nos ramos.

**`GroupMembershipRulesTest`** (`grouping/`, unitÃ¡rio): regras R4 e R5.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `aTeacherManagesGroupsOfTheirUnit` | Um docente a gerir grupos de uma UC sua | Aceite |
| `aTeacherCannotManageGroupsOfAnotherUnit` | Um docente a gerir grupos de uma UC de outro docente, ou sem UC | 403 "SÃ³ pode gerir grupos das suas unidades curriculares." |
| `aValidNewGroupIsAccepted` | Grupo novo com 4 e com 5 alunos inscritos e sem grupo | Aceite |
| `aStudentNotEnrolledIsRejected` | Grupo com um aluno que nÃ£o estÃ¡ inscrito na UC | 409 "O aluno 99 nÃ£o estÃ¡ inscrito nesta UC." |
| `aStudentAlreadyInAnotherGroupIsRejected` | PÃ´r num grupo novo (ou no grupo 2) um aluno que jÃ¡ estÃ¡ no grupo 1 do mesmo contexto | 409 "O aluno 4 jÃ¡ estÃ¡ no grupo 1 deste contexto." |
| `editingAGroupKeepsItsOwnMembers` | Editar o grupo 1 mantendo os seus membros e juntando um aluno sem grupo | Aceite (os membros do prÃ³prio grupo nÃ£o contam como duplicados) |
| `repeatedOrNullStudentsAreRejected` | Lista com o mesmo aluno duas vezes, ou com um valor vazio | 409 "O aluno 3 aparece mais de uma vez no grupo." |
| `sizesOutsideTheDefaultLimitsAreRejected` | Grupos de 3 e de 6 alunos com os limites de omissÃ£o | 409 "Um grupo tem de ter entre 4 e 5 alunos (este teria 3)." |
| `sizeIsConfigurablePerContext` | Contexto com grupos de 2 a 3 alunos | Grupo de 2 aceite, grupo de 4 recusado |
| `invalidLimitsAreRejected` | Limites de omissÃ£o, mÃ­nimo 0 e mÃ¡ximo menor do que o mÃ­nimo | OmissÃ£o 4â€“5; os limites invÃ¡lidos dÃ£o erro |
| `missingInputsAreProgrammingErrors` | Chamar as regras sem a lista de UCs, de alunos ou de limites | `NullPointerException` (erro do cÃ³digo, nÃ£o do utilizador) |

**`GroupStatisticsTest`** (`grouping/`, unitÃ¡rio): estatÃ­sticas por grupo (R6).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `countsMembersAndAverage` | Um grupo de 5 alunos com mÃ©dias 4, 2, 3, 3 e 3 | 5 membros, mÃ©dia 3,0, diferenÃ§a 0, equilibrado |
| `areaStatistics` | ORG com nÃ­veis 5,0; 3,5; 1,0 e 2,4, e PROG com um aluno sem dados | ORG: mÃ©dia 2,975, mÃ­n. 1,0, mÃ¡x. 5,0, 2 fortes, distribuiÃ§Ã£o 1/1/0/1/1. PROG sÃ³ conta os 3 alunos com dados |
| `gapsAreAreasWithoutAStrongMember` | Grupo forte sÃ³ em ORG (4,0) e EQUIPA (3,5); TEMPO no mÃ¡ximo 3,4 | Lacunas: TEMPO e PROG |
| `anAreaWithoutDataIsEmpty` | Grupo em que ninguÃ©m tem dados em EQUIPA | MÃ©dia, mÃ­n. e mÃ¡x. vazios; EQUIPA aparece como "sem dados" e como lacuna |
| `gapToOthersAndBalance` | TrÃªs grupos com mÃ©dias 4,0; 3,0 e 3,5 | DiferenÃ§as +0,75, âˆ’0,75 e 0; sÃ³ o terceiro estÃ¡ equilibrado |
| `aGapOfExactlyHalfIsBalanced` | Dois grupos com mÃ©dias 3,5 e 3,0 | DiferenÃ§a 0,5, ainda equilibrado (limite incluÃ­do) |
| `aStudentWithoutDataCountsAsNeutral` | Grupo com trÃªs alunos de nÃ­vel 5 e um sem dados | MÃ©dia do grupo (5 + 5 + 5 + 3) / 4 = 4,5; nas Ã¡reas sÃ³ contam 3 alunos com dados |
| `invalidInputIsRejected` | Sem competÃªncias, com um grupo vazio ou com um grupo em falta | Erro |
| `noGroupsGivesAnEmptyList` | Contexto sem grupos | Lista vazia |

## DocumentaÃ§Ã£o

| Documento | DescriÃ§Ã£o |
|---|---|
| [Casos de uso](docs/casos-de-uso.md) | 16 casos de uso: as 6 Ã¡reas do enunciado e 4 propostas da equipa |
| [Modelo de domÃ­nio](docs/modelo-dominio.md) | Diagrama de classes UML do domÃ­nio |
| [Arquitetura](docs/arquitetura.md) | Diagrama de arquitetura e responsabilidades dos componentes |
| [ADR-001](docs/decisions/ADR-001-stack-tecnologica.md) | DecisÃ£o e justificaÃ§Ã£o da stack |
| [ADR-002](docs/decisions/ADR-002-perfil-e-cold-start.md) | Modelo do perfil de competÃªncias e estratÃ©gia de cold start |
| [CONTRIBUTING](CONTRIBUTING.md) | Workflow, convenÃ§Ãµes de cÃ³digo e Definition of Done |

## Workflow da equipa

Todo o trabalho segue o fluxo **Issue â†’ Branch â†’ Pull Request â†’ Review â†’ Merge**:

- cada tarefa Ã© uma Issue no Project board;
- branches com o formato `feat/<id>-descricao` ou `docs/<id>-descricao`;
- cada PR precisa da aprovaÃ§Ã£o de outro membro, e ninguÃ©m aprova o seu prÃ³prio PR;
- depois do merge, a branch Ã© eliminada.

Os detalhes estÃ£o no [CONTRIBUTING.md](CONTRIBUTING.md).

## Equipa â€” Grupo 2

| Membro | GitHub |
|---|---|
| Elisio Leite | [@ELZY01](https://github.com/ELZY01) |
| Aires David | [@A1res16](https://github.com/A1res16) |
| Bruna Costa | [@Bruninha9](https://github.com/Bruninha9) |
| Francisco Jacinto | [@FranciscoJacinto7077](https://github.com/FranciscoJacinto7077) |


## Estado atual do projeto

O projeto StudyMatch evoluiu desde a fundaÃ§Ã£o do Sprint 1 e do subsistema de perfil do estudante desenvolvido no Sprint 2. Atualmente, inclui gestÃ£o de estudantes e histÃ³rico acadÃ©mico, cÃ¡lculo de perfis de competÃªncias, quiz de arranque a frio, explicaÃ§Ã£o dos perfis, autenticaÃ§Ã£o, validaÃ§Ã£o de dados e formaÃ§Ã£o automÃ¡tica de grupos.

A formaÃ§Ã£o de grupos permite gerar grupos equilibrados, configurar contextos de agrupamento e consultar estatÃ­sticas. O algoritmo e as regras de equilÃ­brio estÃ£o documentados no [ADR-003](docs/decisions/ADR-003-formacao-de-grupos.md).

As regras de cÃ¡lculo dos perfis e o funcionamento do quiz de arranque a frio estÃ£o documentados no [ADR-002](docs/decisions/ADR-002-perfil-e-cold-start.md).

O projeto inclui testes automatizados para o backend e frontend, bem como documentação técnica sobre arquitetura, casos de uso, segurança e decisões de desenvolvimento.
