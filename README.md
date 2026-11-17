# StudyMatch

> Projeto de grupo de **Qualidade de Software** — Grupo 2 · Universidade Portucalense

O StudyMatch é uma aplicação web que forma grupos de trabalho equilibrados a partir das competências dos estudantes. As competências são calculadas com base no percurso académico de cada aluno: UCs, inscrições e notas.

## Contexto do produto

Nos trabalhos de grupo, os grupos costumam formar-se por afinidade ou ao acaso. Isso cria grupos desequilibrados: uns concentram os alunos mais fortes e outros ficam sem ninguém que domine as áreas críticas. O StudyMatch resolve este problema em quatro passos:

1. **Representa o percurso académico** de cada estudante: as UCs, as tentativas e as notas.
2. **Converte notas em competências.** Cada UC contribui, com um peso, para uma ou mais competências, numa escala de 1 a 5.
3. **Constrói um perfil de competências** do estudante, com os pontos fortes e fracos.
4. **Gera grupos equilibrados** num contexto definido pelo docente (UC, atividade e tamanho do grupo).

### Problemas que resolvemos

| Problema | Solução |
|---|---|
| **Arranque a frio:** um aluno novo ainda não tem notas | Quiz inicial de 20 perguntas em 4 áreas (organização, gestão de tempo, programação, trabalho em grupo) que dá um perfil de 1 a 5 desde o primeiro dia. O peso do quiz diminui à medida que o aluno conclui UCs. |
| **Equilíbrio dos grupos** | Os alunos são ordenados pela média do perfil e distribuídos em serpentina (G1, G2, G2, G1, …), o que dá grupos com médias praticamente iguais. O docente pode ajustar os grupos manualmente. |

### Funcionalidades propostas pela equipa

- **Explicação da formação do grupo:** o aluno vê porque ficou num grupo, sem ver as notas dos colegas.
- **Importação por CSV:** o docente importa alunos e notas de um ficheiro.
- **Avaliação entre pares:** no fim do trabalho, os colegas avaliam-se de forma anónima, e essa avaliação atualiza a competência "trabalho em grupo".

### Atores

- **Estudante:** regista o percurso, responde ao quiz, consulta o perfil e participa em grupos.
- **Docente:** define competências, cria contextos de agrupamento, gera e ajusta grupos.

## Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Frontend | React + Vite (npm) |
| Backend / API | Java 21 + Spring Boot (API REST), Maven |
| Persistência | PostgreSQL (Supabase) via Spring Data JPA |
| Testes | JUnit 5 + Spring Boot Test · Vitest + React Testing Library |
| Gestão | Git + GitHub (Issues, Projects, Pull Requests) |

A justificação das escolhas está no [ADR-001](docs/decisions/ADR-001-stack-tecnologica.md).

## Estrutura do repositório

```
Grupo2_QS_Project/
├── backend/            # API Spring Boot (Maven)
├── frontend/           # Aplicação React (Vite)
├── docs/
│   ├── casos-de-uso.md
│   ├── modelo-dominio.md
│   ├── arquitetura.md
│   └── decisions/      # Decisões de arquitetura (ADR)
├── CONTRIBUTING.md     # Workflow, convenções e Definition of Done
└── README.md
```

## Como correr o projeto

### Requisitos

- **Java 21** (o Maven Wrapper vem incluído no projeto, não é preciso instalar o Maven)
- **Node.js 20+** (inclui o npm)
- **Credenciais da base de dados Supabase** (pedir à equipa). Nunca entram no repositório.

### 1. Backend

O endereço da base de dados já está em `backend/src/main/resources/application.properties`. O utilizador e a palavra-passe são lidos de variáveis de ambiente, que têm de ser definidas **no mesmo terminal** antes de arrancar:

| Variável | Valor |
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

### Contas e autenticação

O backend usa Spring Security: todos os pedidos (exceto `GET /api/health`, login e registo) precisam de sessão, e cada perfil só acede ao que lhe pertence (403 caso contrário).

| Perfil | Como entra | O que pode fazer |
|---|---|---|
| Admin | utilizador `admin` | estudantes, notas, docentes e inscrições nas UCs |
| Docente | utilizador dado pelo admin | as suas UCs, os alunos inscritos e as estatísticas de cada aluno |
| Aluno | número de estudante ou email | só os seus dados (perfil, quiz, histórico) |

No arranque, o backend cria (se não existirem) as contas de demonstração e a UC `QS-TESTE` associada ao docente. As palavras-passe por omissão são só para desenvolvimento; em produção definir as variáveis de ambiente:

| Variável | Por omissão |
|---|---|
| `ADMIN_USER` / `ADMIN_PASSWORD` | `admin` / `studymatch` |
| `DOCENTE_USER` / `DOCENTE_PASSWORD` | `docente` / `docente123` |

**Antes de arrancar esta versão contra o Supabase**, correr no SQL editor, por esta ordem, `backend/src/main/resources/db/migration/V5__create_app_users.sql` e `V6__create_course_unit_assignments.sql` (com `ddl-auto=validate`, o backend não arranca sem as tabelas). Para reverter: `U6__…` e depois `U5__…`. Os alunos que já existiam não têm conta: o admin define-lhes a palavra-passe em *Estudantes → Editar*.

### 2. Frontend

Num segundo terminal:

```bash
cd frontend
cp .env.example .env          # Windows (cmd): copy .env.example .env
npm install
npm run dev
```

O ficheiro `.env` define `VITE_API_URL=http://localhost:8080`, o endereço do backend. A aplicação abre em http://localhost:5173 e mostra o estado do backend (UP).

#### Avanço automático do quiz do aluno (Issue #126)

No quiz, ao escolher uma resposta, o ecrã passa sozinho para a pergunta seguinte ao fim de 220 ms. Esse avanço não era cancelado, o que dava dois problemas: duas respostas seguidas muito rápidas saltavam uma pergunta, e o avanço corria mesmo depois de o quiz sair do ecrã. Era isto que fazia o `npm test` mostrar o erro `window is not defined` no fim dos testes. Agora o avanço pendente é cancelado quando há uma nova escolha, quando o aluno usa "Anterior" ou "Seguinte" e quando o quiz fecha.

Os três testes novos estão em `frontend/src/student/StudentQuiz.test.jsx` e usam timers simulados (`vi.useFakeTimers`). Sem a correção, os três falham.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| does not skip a question when the student changes the answer quickly | Duas respostas à 1.ª pergunta antes dos 220 ms | O quiz avança só para a 2.ª pergunta |
| stays on the previous question when the student goes back right after answering | Responder à 2.ª pergunta e carregar logo em "Anterior" | O quiz fica na 1.ª pergunta |
| cancels the pending move when the quiz is closed | Responder e fechar o quiz antes dos 220 ms | Não fica nenhum timer pendente |

### Testes

```bash
cd backend && ./mvnw clean test   # backend (Windows: mvnw.cmd clean test)
cd frontend && npm test           # frontend (Vitest)
```

Os testes do backend **não precisam das credenciais da base de dados**: usam uma base H2 em memória (perfil `test`). O `clean test` compila o projeto e corre todos os testes unitários, incluindo as regras do perfil de competências (`ProfileCalculatorTest`).

#### O que cada teste verifica

O `main` tem 167 testes no backend e 92 no frontend. No backend há 135 métodos `@Test`. A diferença vem dos testes parametrizados, que correm uma vez por cada valor da lista.

Usamos quatro tipos de teste:

- **Unitário** (JUnit 5 + Mockito): testa uma classe sozinha, com os repositórios simulados. Não arranca o Spring nem usa base de dados.
- **Web** (`@WebMvcTest`): arranca só o controller e as regras do Spring Security. O serviço é simulado, por isso o que se testa é o HTTP: estado da resposta, JSON e permissões.
- **Backend completo** (`@SpringBootTest` + H2): arranca a aplicação toda com a base H2 em memória e faz pedidos reais à API, com login e token.
- **Frontend** (Vitest + React Testing Library): mostra o componente como o utilizador o vê e simula a API.

Cada classe está num bloco fechado. Clica no nome para abrir.

##### Backend: perfil e formação de grupos

<details>
<summary><code>ProfileCalculatorTest</code> · unitário · 15 métodos, 33 execuções</summary>

Regras do perfil de competências (Issue #44, ADR-002). Os pesos usados são os mesmos da migração `V3`.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `convertePelaFormula` | Conversão de nota em nível para 10, 15, 20 e 12 | Níveis 1,0; 3,0; 5,0 e 1,8 |
| `rejeitaNotasNaoAprovadasOuForaDoIntervalo` | Notas 9, 0 e 21 | Erro: só notas de 10 a 20 têm nível |
| `mediaPonderadaPelosPesos` | PROG com PROG1 = 20 (peso 1,0) e BD = 10 (peso 0,8) | Nível (1,0 × 5 + 0,8 × 1) / 1,8 ≈ 3,22, origem "histórico", contributos que somam 100% |
| `contaAMelhorTentativa` | Três tentativas a PROG1 (8, 12 e 16) | Conta só o 16 e a UC conta uma vez como concluída |
| `reprovacoesEEmCursoNaoContam` | Uma reprovação (9) e uma inscrição em curso | 0 UCs concluídas e PROG fica sem dados |
| `novaNotaAtualizaOPerfil` | Acrescentar POO = 20 a quem só tinha PROG1 = 12 | O nível de PROG passa de 1,8 para 3,4 |
| `semNotasEhSoQuiz` | Aluno sem notas, só com o quiz | Peso do quiz 100% e cada nível igual ao do quiz |
| `exemploDoUc09` | Exemplo do caso de uso UC09: 3 UCs com 15, quiz 4 | Peso do quiz 0,5 e nível 3,5, origem "misto" |
| `limitesDoPesoDoQuiz` | Peso do quiz com 0, 1, 3, 5, 6, 7 e 20 UCs concluídas | 1,0 → 0,83 → 0,5 → 0,17 → 0 a partir de 6, nunca negativo |
| `comNUcsEhSoHistorico` | Aluno com 6 UCs concluídas | O quiz deixa de contar e o nível vem só das notas |
| `competenciaSemHistoricoUsaQuiz` | EQUIPA sem nenhuma UC que a alimente | Usa só o quiz (nível 2,0, peso 1,0) |
| `semDadosNenhuns` | Aluno sem quiz e sem notas | As 4 competências ficam "sem dados", sem nível |
| `notaForaDoIntervalo` | Notas -1, 21 e 25 | Erro ao criar o resultado académico |
| `pesoForaDoIntervalo` | Pesos 0,0; -0,5 e 1,5 | Erro: o peso tem de estar entre 0 e 1 |
| `quizForaDoIntervalo` | Resultados do quiz 0,0; 0,99; 5,01 e 6,0 | Erro: o quiz vai de 1 a 5 |

</details>

<details>
<summary><code>GroupCandidateTest</code> · unitário · 8 testes</summary>

O aluno como o algoritmo de grupos o vê (Issue #73, ADR-003).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `averageOfAllLevels` | Média de um aluno com as 4 competências | 3,5, e o aluno tem dados |
| `missingLevelIsIgnored` | Aluno sem nível em TEMPO | A competência em falta não entra na média como 0 |
| `noDataUsesNeutralAverageForSorting` | Aluno sem dados nenhuns | Média vazia; para ordenar usa o valor neutro 3,0 |
| `boundaryLevelsAreAccepted` | Níveis 1 e 5 | Aceites |
| `levelOutOfRangeIsRejected` | Níveis 0,9 e 5,1 | Erro |
| `requiredFields` | ID, número, nome ou níveis em falta | Erro em cada um dos quatro casos |
| `levelsAreCopied` | Mudar o mapa original depois de criar o aluno | Os níveis do aluno não mudam e não se podem alterar |
| `fromProfile` | Converter um perfil do `ProfileCalculator` | ORG = 4,0 passa e PROG continua "sem dados" |

</details>

<details>
<summary><code>GroupFormationTest</code> · unitário · 12 métodos, 26 execuções</summary>

Distribuição dos alunos em grupos de 4 a 5 (Issues #70 e #72).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `shouldReturnEmptyListWhenThereAreNoStudents` | Lista de alunos vazia | Nenhum grupo |
| `shouldRejectNullStudentList` | Lista nula | Erro |
| `shouldRejectNullStudents` | Lista com um aluno nulo | Erro |
| `shouldRejectStudentsWithoutProfile` | Aluno sem perfil | Erro |
| `shouldRejectStudentsWithoutStudentNumber` | Aluno sem número de estudante | Erro |
| `shouldCreateTwoGroupsOfFourStudents` | 8 alunos | 2 grupos de 4 |
| `shouldCreateTwoGroupsOfFiveStudents` | 10 alunos | 2 grupos de 5 |
| `shouldRejectImpossibleNumberOfStudents` | 6 alunos (não dá grupos de 4 a 5) | Erro |
| `shouldCreateGroupsWithValidSizes` | 4, 5, 8, 9, 10 e de 12 a 21 alunos | Todos os grupos com 4 ou 5 e ninguém fica de fora |
| `shouldDistributeEveryStudentExactlyOnce` | 13 alunos | Cada aluno aparece uma única vez |
| `shouldDistributeStudentsDeterministically` | Os mesmos alunos duas vezes | Os mesmos grupos das duas vezes |
| `shouldAcceptStudentsWithoutCompetencyData` | Alunos sem dados de competências | Formam-se os grupos na mesma (8 alunos, 2 grupos) |

</details>

##### Backend: serviços

<details>
<summary><code>StudentServiceTest</code> · unitário · 4 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `deveCriarEstudanteValido` | Criar um estudante com dados válidos | É guardado uma vez, com o nome certo |
| `deveRejeitarEmailDuplicado` | Email que já existe | 409 e nada é guardado |
| `deveRejeitarNomeVazio` | Nome em branco | 400 |
| `deveRejeitarEstudanteInexistente` | Procurar um estudante que não existe | 404 |

</details>

<details>
<summary><code>EnrollmentServiceTest</code> · unitário · 9 testes</summary>

Inscrições e notas no histórico académico.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `shouldAcceptGradeZero` | Nota 0 | Aceite, 1.ª tentativa, estado "reprovado" |
| `shouldAcceptGradeTwenty` | Nota 20 | Aceite, estado "aprovado" |
| `shouldRejectNegativeGrade` | Nota negativa | 400 e nada é guardado |
| `shouldRejectGradeAboveTwenty` | Nota acima de 20 | 400 |
| `shouldAcceptEnrollmentInProgress` | Inscrição sem nota | Aceite, estado "em curso" |
| `shouldRejectGradeWhenInProgress` | Inscrição "em curso" com nota | 400 |
| `shouldIncrementAttemptNumber` | Nova inscrição numa UC com 2 tentativas | Fica como 3.ª tentativa |
| `shouldRejectUnknownStudent` | Inscrever um aluno que não existe | 404 |
| `shouldReturnStudentHistory` | Pedir o histórico de um aluno | Devolve a inscrição com nota 16, "aprovado" |

</details>

<details>
<summary><code>ProfileServiceTest</code> · unitário · 4 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `estudanteInexistenteDevolve404` | Perfil de um aluno que não existe | 404 |
| `perfilComHistoricoUsaAMelhorNotaDeCadaUc` | Aluno com notas e sem quiz | Conta a melhor nota de cada UC: 2 UCs concluídas e Programação = (5 + 1) / 2, com 2 contributos |
| `alunoSoComQuizTemPerfilPelasMediasDeCadaArea` | Aluno só com o quiz | Peso do quiz 100%; PROG = 4,0 e EQUIPA = 2,0 |
| `alunoSemQuizNemNotasFicaSemDados` | Aluno só com uma UC em curso | 0 UCs concluídas e todas as competências "sem dados" |

</details>

<details>
<summary><code>QuizServiceTest</code> · unitário · 4 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `testSubmitQuiz_NormalSubmission` | Submeter as 20 respostas com valor 4 | Guarda 1 submissão e 20 respostas |
| `testSubmitQuiz_InvalidAnswerCount_ThrowsException` | Submeter só 19 respostas | 400 "O quiz de arranque a frio deve conter exatamente 20 respostas." |
| `testSubmitQuiz_BoundaryValues_AllOnes` | Todas as respostas com 1, o mínimo | Aceite, 20 respostas guardadas |
| `testSubmitQuiz_BoundaryValues_AllFives` | Todas as respostas com 5, o máximo | Aceite, 20 respostas guardadas |

</details>

<details>
<summary><code>CourseUnitServiceTest</code> · unitário · 2 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `returnsCourseUnitsOrderedByYearSemesterAndCode` | Listar as UCs | Ordenadas por ano, semestre e código, com nome e ECTS |
| `returnsEmptyListWhenThereAreNoCourseUnits` | Não há UCs | Lista vazia |

</details>

<details>
<summary><code>TeacherServiceTest</code> · unitário · 4 testes</summary>

Área do docente (R3, R7).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `listsOnlyTheUnitsAssignedToTheTeacherWithTheStudentCount` | UCs da docente | Só a UC dela (QS), com 24 alunos inscritos |
| `listsTheStudentsOfAnAssignedUnit` | Alunos de uma UC da docente | Devolve a Ana |
| `forbidsTheStudentsOfAUnitFromAnotherTeacher` | Alunos de uma UC de outro docente | 403 |
| `returns404ForAnUnknownUnit` | UC que não existe | 404 |

</details>

<details>
<summary><code>AuthServiceTest</code> · unitário · 9 testes</summary>

Login, registo e palavras-passe.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `logsInWithTheRightPasswordAndReturnsAWorkingToken` | Login do admin com espaços à volta do nome | Entra como ADMIN e o token devolvido é válido |
| `rejectsAWrongPasswordAndAnUnknownUserWithTheSameMessage` | Palavra-passe errada e utilizador que não existe | 401 com a mesma mensagem nos dois casos, para não revelar que contas existem |
| `blocksTheUserAfter5FailedAttemptsEvenWithTheRightPassword` | 5 falhas seguidas e depois a palavra-passe certa | 429: a conta fica bloqueada durante um tempo |
| `aSuccessfulLoginResetsTheFailures` | Falhas, um login certo e mais falhas | O contador volta a zero e o login seguinte entra |
| `aStudentCanLogInWithTheEmail` | Aluno a entrar com o email | Entra como ALUNO com o ID de estudante certo |
| `registersTheStudentWithAHashedPasswordAndStartsTheSession` | Registo de um aluno | Conta ALUNO criada, palavra-passe guardada só como hash e sessão iniciada |
| `rejectsAShortPasswordBeforeCreatingTheStudent` | Registo com palavra-passe curta | 400 antes de criar o aluno |
| `adminCreatesTheAccountOfAnOldStudentWithoutOne` | Admin define a palavra-passe de um aluno sem conta | A conta é criada com o número de estudante |
| `adminResetsTheExistingPassword` | Admin muda a palavra-passe de um aluno com conta | A nova palavra-passe passa a funcionar |

</details>

<details>
<summary><code>CourseUnitAssignmentServiceTest</code> · unitário · 9 testes</summary>

Atribuições feitas pelo admin (R1, R2).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `enrollsAStudentInTheCourseUnit` | Inscrever a Ana em QS | A inscrição é guardada com a UC e o aluno certos |
| `rejectsAStudentAlreadyEnrolledInTheSameCourseUnit` | Inscrever a Ana outra vez | 409 "Este aluno já está inscrito nesta UC." |
| `returns404ForUnknownCourseUnitOrStudent` | UC ou aluno que não existem | 404 |
| `removesAnEnrolledStudentAndFailsIfNotEnrolled` | Remover um aluno inscrito e um que não está | O primeiro sai; o segundo dá 404 |
| `assignsATeacherToTheCourseUnit` | Associar uma docente a QS | A associação é guardada |
| `onlyTeacherAccountsCanBeAssigned` | Associar uma conta de aluno como docente | 400 |
| `rejectsAssigningTheSameTeacherTwice` | Associar a mesma docente duas vezes | 409 |
| `createsATeacherAccountWithAHashedPassword` | Criar uma conta de docente com espaços nos campos | Nome e utilizador sem espaços, role DOCENTE e palavra-passe em hash |
| `rejectsADuplicateUsernameOrAShortPassword` | Utilizador repetido ou palavra-passe curta | 409 e 400 |

</details>

##### Backend: segurança

<details>
<summary><code>TokenServiceTest</code> · unitário · 3 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `issuesARandomTokenThatResolvesToTheUser` | Emitir dois tokens | São diferentes, têm pelo menos 40 caracteres e levam ao utilizador certo |
| `rejectsUnknownBlankAndRevokedTokens` | Token revogado, inventado, vazio ou nulo | Nenhum é aceite |
| `expiresAfterTheTtl` | Um token depois do tempo de validade | Válido antes, inválido depois |

</details>

<details>
<summary><code>LoginThrottleTest</code> · unitário · 3 testes</summary>

Limite de tentativas de login (Issue #82).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `blocksAfterTheMaximumFailuresIgnoringCase` | Falhas com "admin" e " ADMIN " | Contam para a mesma conta e bloqueiam-na; outra conta não é afetada |
| `unblocksWhenTheFailuresLeaveTheWindow` | 3 falhas e 15 minutos e 1 segundo depois | A conta já não está bloqueada |
| `successClearsTheFailures` | 2 falhas, um sucesso e mais 1 falha | Não bloqueia: o sucesso apagou as falhas anteriores |

</details>

<details>
<summary><code>SecurityAccessTest</code> · backend completo · 15 testes</summary>

Quem pode chamar o quê, com pedidos reais à API (R1, R7, RN05, RN16).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `healthIsPublic` | `GET /api/health` sem sessão | 200 |
| `protectedEndpointsNeedASession` | Alunos, UCs e quiz sem sessão | 401 "Tens de iniciar sessão." |
| `anInvalidTokenIsTreatedAsNoSession` | Token inventado | 401 |
| `wrongPasswordIsRejected` | Login com a palavra-passe errada | 401 "Utilizador ou palavra-passe errados." |
| `adminManagesStudents` | Admin a listar alunos e a ver a própria sessão | 200 e role ADMIN |
| `logoutInvalidatesTheToken` | Usar o token depois do logout | 401 |
| `aStudentOnlySeesTheirOwnData` | Ana a ver os seus dados e os do Rui | Os dela dão 200; os do Rui e a lista de alunos dão 403 |
| `aStudentCannotChangeEnrollmentsOrOtherStudents` | Ana a criar inscrições, alterar o Rui, submeter o quiz dele ou mudar palavras-passe | 403 em todos |
| `aStudentCanLogInWithTheEmail` | Login com o email | 200, role ALUNO e o ID certo |
| `registrationNeedsAPasswordWithAtLeast8Characters` | Registo com "curta" | 400 "A palavra-passe tem de ter pelo menos 8 caracteres." |
| `adminResetsAStudentPassword` | Admin muda a palavra-passe de um aluno | 204, e a palavra-passe antiga deixa de entrar |
| `aTeacherCannotUseTheAdminAreaNorSeeStudentsOutsideTheirUnits` | Docente na área do admin e num aluno fora das suas UCs | 403 |
| `studentsCannotUseTheTeacherArea` | Aluno na área do docente | 403 |
| `onlyTheAdminEnrollsStudentsInCourseUnits` | Docente, aluno e admin a inscrever um aluno numa UC | 403, 403 e 200; repetir dá 409 |
| `onlyTheAdminAssignsTeachers` | Docente a listar docentes; admin a ver as atribuições | 403 para o docente; o admin vê a docente associada |

</details>

<details>
<summary><code>TeacherAccessTest</code> · backend completo · 5 testes</summary>

Área do docente com pedidos reais à API (R3, R6, R7).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `theTeacherSeesTheirUnitTheEnrolledStudentsAndTheirStats` | Docente na sua UC | Vê a UC, os alunos inscritos e o perfil, o histórico e os dados de cada um |
| `theTeacherCannotSeeStudentsOutsideTheirUnits` | Perfil e histórico de um aluno fora das suas UCs | 403 |
| `theTeacherCannotOpenAnotherTeachersUnit` | UC de outro docente e UC que não existe | 403 "Esta UC não te está atribuída." e 404 |
| `aTeacherWithoutUnitsSeesNothing` | Docente acabado de criar | Lista vazia e 403 em UCs e alunos |
| `studentsAndAdminsCannotUseTheTeacherArea` | Admin e pedido sem sessão na área do docente | 403 e 401 |

</details>

##### Backend: controllers (camada web)

<details>
<summary><code>HealthControllerTest</code> · web · 3 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `health_returnsOkAndServiceStatus_evenWithoutAuth` | `GET /api/health` sem sessão | 200 com `status: UP` e `service: studymatch-backend` |
| `unknownEndpoint_withoutAuth_returns401` | Rota que não existe, sem sessão | 401 |
| `postOnHealth_withoutAuth_returns401` | `POST /api/health` sem sessão | 401: só o `GET` é público |

</details>

<details>
<summary><code>CourseUnitControllerTest</code> · web · 4 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `getAllCourseUnits_withoutAuth_returns401` | Lista de UCs sem sessão | 401 e o serviço nem é chamado |
| `getAllCourseUnits_whenAuthenticatedAsStudent_returns200AndList` | Lista de UCs com sessão de aluno | 200 com as 2 UCs e todos os campos no JSON |
| `getAllCourseUnits_whenEmpty_returns200AndEmptyArray` | Não há UCs | 200 com `[]` |
| `unknownSubRoute_whenAuthenticated_returns404` | `GET /api/course-units/999` | 404 |

</details>

<details>
<summary><code>QuizControllerTest</code> · web · 13 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `getQuiz_withoutAuth_returns401` | Perguntas do quiz sem sessão | 401 |
| `getQuiz_whenQuestionsExist_returns200AndQuestions` | Perguntas do quiz com sessão | 200 com a pergunta, o ID e o texto |
| `getQuiz_whenNoQuestionsFound_returns404` | Quiz sem perguntas | 404 |
| `submitQuiz_withoutAuth_returns401` | Submeter sem sessão | 401 |
| `submitQuiz_whenNotSelf_returns403` | Submeter o quiz de outro aluno | 403 |
| `submitQuiz_whenValidRequestAndDefaultQuizId_returns200` | Submissão válida do próprio aluno | 200 com o ID da submissão; usa o quiz 1 por omissão |
| `submitQuiz_whenCustomQuizIdParam_passesQuizIdToService` | Submeter com `?quizId=2` | O serviço recebe o quiz 2 |
| `submitQuiz_whenInvalidPayloadOrServiceBadRequest_returns400` | O serviço rejeita as respostas | 400 |
| `submitQuiz_whenMissingRequestBody_returns400` | Pedido sem corpo | 400 |
| `getLatestQuizResults_withoutAuth_returns401` | Resultados sem sessão | 401 |
| `getLatestQuizResults_whenCannotView_returns403` | Resultados de um aluno que não se pode ver | 403 |
| `getLatestQuizResults_whenAuthorized_returns200AndCompetencyMap` | Resultados com permissão | 200 com a média por área (Liderança 4,2; Comunicação 3,8) |
| `getLatestQuizResults_whenStudentOrSubmissionNotFound_returns404` | Aluno ou submissão que não existem | 404 |

</details>

<details>
<summary><code>TeacherControllerTest</code> · web · 8 testes</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `myCourseUnits_withoutAuth_returns401` | UCs do docente sem sessão | 401 |
| `myCourseUnits_whenAuthenticatedAsStudent_returns403` | UCs do docente com sessão de aluno | 403 |
| `myCourseUnits_whenAuthenticatedAsTeacher_returns200AndList` | UCs do docente com sessão de docente | 200 com a UC e 15 alunos |
| `students_withoutAuth_returns401` | Alunos de uma UC sem sessão | 401 |
| `students_whenAuthenticatedAsStudent_returns403` | Alunos de uma UC com sessão de aluno | 403 |
| `students_whenTeacherNotAssignedToCourseUnit_returns403` | UC que não é do docente | 403 |
| `students_whenCourseUnitNotFound_returns404` | UC que não existe | 404 |
| `students_whenValidAssignment_returns200AndStudentSummaryList` | UC do docente | 200 com a lista de alunos |

</details>

<details>
<summary><code>BackendApplicationTests</code> · backend completo · 1 teste</summary>

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `contextLoads` | Arrancar a aplicação toda com o perfil `test` | Arranca sem erros, o que confirma que a configuração e os beans batem certo |

</details>

##### Frontend

Os testes do frontend correm com `npm test`. A API é sempre simulada, por isso não precisam do backend ligado.

<details>
<summary><code>api/http.test.js</code> · 4 testes</summary>

| Teste | O que verifica |
|---|---|
| sends the session token in the Authorization header | Com sessão, cada pedido leva o token no cabeçalho `Authorization` |
| does not add a header without a session | Sem sessão, o pedido vai sem cabeçalho |
| warns the app when the session expired (401 with a token) | Um 401 com token avisa a aplicação de que a sessão expirou |
| accepts 204 responses without a body | Uma resposta 204 sem corpo não dá erro |

</details>

<details>
<summary><code>api/quizApi.test.js</code> · 4 testes</summary>

| Teste | O que verifica |
|---|---|
| uses the same API address as the other files | O quiz usa o mesmo endereço da API que o resto da aplicação |
| sends the answers of the student | As respostas vão no pedido para o aluno certo |
| shows the message sent by the API | Quando a API falha, aparece a mensagem que ela enviou |
| uses a clear message when the API does not send one | Sem mensagem da API, aparece um texto claro em vez de um erro técnico |

</details>

<details>
<summary><code>components/AcademicHistory.test.jsx</code> · 5 testes</summary>

| Teste | O que verifica |
|---|---|
| shows every attempt with UC name, grade and status, ordered by school year | Todas as tentativas aparecem, com UC, nota e estado, por ano letivo |
| registers an enrollment with POST and notifies the parent | Registar uma inscrição faz `POST` e avisa o ecrã que contém o histórico |
| shows the validation error returned by the API | O erro de validação da API aparece no formulário |
| sends no grade for an enrollment in progress | Uma inscrição "em curso" vai sem nota |
| shows an error when the history cannot be loaded | Se o histórico não carrega, aparece um erro |

</details>

<details>
<summary><code>components/CompetencyProfile.test.jsx</code> · 9 testes</summary>

| Teste | O que verifica |
|---|---|
| shows each competency with its level, origin and the quiz weight | Cada competência mostra nível, origem e peso do quiz |
| explains a level with the UCs, grades, weights and shares | A explicação de um nível lista as UCs, notas, pesos e percentagens |
| shows "Sem dados" with a suggested action | Competência sem dados mostra "Sem dados" e o que fazer a seguir |
| asks for the initial quiz when it is missing | Sem quiz, o perfil pede para o fazer |
| reloads the profile when refreshKey changes | O perfil volta a carregar quando há dados novos |
| shows the API error when the student does not exist | Aluno que não existe mostra o erro da API |
| talks about "this student" when a teacher is viewing the profile | Para o docente o texto fala "deste aluno" em vez de "tu" |
| says there is no quiz when the student never did it | Perfil só com notas diz que o aluno não fez o quiz (#89) |
| says the quiz no longer counts after 6+ UCs when the quiz was done | Com 6 ou mais UCs, diz que o quiz já não conta (#89) |

</details>

<details>
<summary><code>components/CourseUnitAssignments.test.jsx</code> · 6 testes</summary>

| Teste | O que verifica |
|---|---|
| shows the teachers and the enrolled students of the chosen unit | Mostra os docentes e os alunos da UC escolhida |
| assigns a teacher to the unit (R2) | O admin associa um docente à UC |
| only offers students that are not enrolled yet and enrolls them (R1) | Só aparecem para inscrever alunos que ainda não estão na UC |
| removes an enrolled student | Remover um aluno inscrito |
| shows the backend error, e.g. a duplicate enrollment | Erros do backend, como uma inscrição repetida, aparecem no ecrã |
| creates a teacher account | Criar uma conta de docente |

</details>

<details>
<summary><code>components/HealthStatus.test.jsx</code> · 2 testes</summary>

| Teste | O que verifica |
|---|---|
| shows UP when the backend answers | Com o backend ligado, aparece UP |
| shows an error when the backend is down | Com o backend em baixo, aparece um erro |

</details>

<details>
<summary><code>components/StudentManager.test.jsx</code> · 8 testes</summary>

| Teste | O que verifica |
|---|---|
| lists the students returned by the API | Lista os alunos que a API devolve |
| creates a student and adds it to the list | Criar um aluno junta-o à lista |
| shows the validation error returned by the API | O erro de validação da API aparece |
| shows a clear message for duplicates when the API sends no text | Num duplicado sem mensagem da API, aparece um texto claro |
| asks for an initial password of 8+ characters before creating | Só cria com uma palavra-passe de 8 ou mais caracteres |
| sets a new password when editing only if one is typed | Ao editar, a palavra-passe só muda se for escrita uma nova |
| edits a student with PUT | Editar um aluno faz `PUT` |
| shows an error when the list cannot be loaded | Se a lista não carrega, aparece um erro |

</details>

<details>
<summary><code>components/ThemeToggle.test.jsx</code> · 4 testes</summary>

| Teste | O que verifica |
|---|---|
| starts from the system theme when nothing was chosen | Sem escolha guardada, segue o tema do sistema |
| switches to night mode and remembers the choice | Muda para o modo noturno e guarda a escolha |
| applies the stored choice before the app renders | A escolha guardada é aplicada antes de a página aparecer |
| ignores invalid stored values | Um valor guardado inválido é ignorado |

</details>

<details>
<summary><code>groups/GroupContexts.test.jsx</code> · 12 testes</summary>

Ecrã dos contextos e grupos (#74) e troca manual de alunos (#75).

| Teste | O que verifica |
|---|---|
| creates a context with the chosen students and shows the group split | Cria o contexto com os alunos escolhidos e mostra como ficam os grupos |
| blocks an impossible configuration with a clear message | Um número de alunos que não dá grupos de 4 a 5 é bloqueado com uma mensagem clara |
| shows the API error when the backend rejects the generation | Se o backend recusa gerar os grupos, aparece o erro |
| shows the Pix animation for at least 2 s while generating | A animação aparece pelo menos 2 segundos enquanto gera |
| opens a previous generation from the history | Abre uma geração antiga a partir do histórico |
| shows members, average, strongest per area and the gap to the other groups | Cada grupo mostra membros, média, o mais forte por área e a diferença para os outros |
| never shows the grades or levels of other students (RN26) | Nunca aparecem notas nem níveis de outros alunos (RN26) |
| warns when the gap between groups is above the limit | Avisa quando a diferença entre grupos passa o limite |
| swaps two students and shows the recalculated averages | Trocar dois alunos atualiza as médias |
| does not call the API when both students are in the same group | Dois alunos do mesmo grupo não chegam à API |
| warns when the swap leaves the groups more than 0,5 apart | Avisa quando a troca deixa os grupos a mais de 0,5 |
| shows the API error when the swap is rejected | Se a API recusa a troca, aparece o erro |

</details>

<details>
<summary><code>groups/groupPlan.test.js</code> · 11 testes</summary>

| Teste | O que verifica |
|---|---|
| rejects the impossible configurations | Números de alunos que não dão grupos de 4 a 5 são recusados |
| only makes groups of 5 and 4 | Os grupos têm sempre 5 ou 4 alunos |
| handles the 11/12 boundary | O caso de 11 e 12 alunos dá a divisão certa |
| explains the split or why it is impossible | O texto explica a divisão ou porque é impossível |
| uses a comma and one decimal place | Os números aparecem com vírgula e uma casa decimal |
| shows the sign of the gap | A diferença entre grupos mostra o sinal (+ ou −) |
| finds the group of a student | Encontra o grupo de um aluno |
| accepts two students from different groups | Aceita trocar alunos de grupos diferentes |
| asks for both students | Pede os dois alunos antes de trocar |
| rejects two students from the same group | Recusa trocar dois alunos do mesmo grupo |
| rejects a student that is not in any group | Recusa um aluno que não está em nenhum grupo |

</details>

<details>
<summary><code>mascot/GroupForming.test.jsx</code> · 6 testes</summary>

| Teste | O que verifica |
|---|---|
| shows a squad of four Pix, one banner per area, and the loading text | A animação mostra quatro Pix, uma bandeira por área e o texto de espera |
| only shows the text when the user prefers reduced motion | Com "reduzir movimento" ligado no sistema, só aparece o texto |
| still renders the lobby when matchMedia is not available | Funciona em browsers sem `matchMedia` |
| keeps the animation visible for the minimum time when the request is fast | Se o pedido é rápido, a animação fica o tempo mínimo |
| hides right away when the request took longer than the minimum | Se o pedido demora mais do que o mínimo, a animação some logo |
| stays hidden if it never started | Se não começou, não aparece |

</details>

<details>
<summary><code>mascot/mascot.test.js</code> · 8 testes</summary>

| Teste | O que verifica |
|---|---|
| uses only the base dino when there is no data | Sem dados, aparece o dinossauro base |
| colours the body with the strongest area and stacks every accessory | A cor vem da área mais forte e os acessórios juntam-se |
| keeps the base colour while no area reaches level 3 | Abaixo do nível 3 em todas as áreas, fica a cor base |
| marks a tie and takes the colour from the first area in order | Num empate, fica marcado e usa a cor da primeira área |
| keeps the plain base dino when every level rounds to 1 | Com tudo no nível 1, fica o dinossauro simples |
| starts with a whole egg | O ovo começa inteiro |
| cracks more as the answers go up | O ovo racha mais à medida que o aluno responde |
| has one sprite per stage | Há uma imagem para cada fase do ovo |

</details>

<details>
<summary><code>student/Login.test.jsx</code> · 4 testes</summary>

| Teste | O que verifica |
|---|---|
| always asks for the password and checks it in the backend | Pede sempre a palavra-passe e quem a valida é o backend |
| shows the error for wrong credentials | Credenciais erradas mostram o erro |
| does not call the backend without user or password | Sem utilizador ou palavra-passe, não faz o pedido |
| no longer has the admin password in the frontend | A palavra-passe do admin já não está no código do frontend |

</details>

<details>
<summary><code>student/StudentQuiz.test.jsx</code> · 2 testes</summary>

| Teste | O que verifica |
|---|---|
| shows one question at a time and cracks the egg as the student answers | Uma pergunta de cada vez, e o ovo racha a cada resposta |
| only lets the egg hatch when every question is answered | O ovo só choca quando todas as perguntas estão respondidas |

</details>

<details>
<summary><code>teacher/TeacherApp.test.jsx</code> · 6 testes</summary>

| Teste | O que verifica |
|---|---|
| lists only the teacher units and opens the first one with its students | Mostra só as UCs do docente e abre a primeira com os alunos |
| shows the stats of the chosen student (profile and read-only history) | Mostra o perfil e o histórico do aluno, sem poder editar |
| shows a clear empty state without units | Docente sem UCs vê uma mensagem clara |
| shows a clear empty state for a unit without students | UC sem alunos mostra uma mensagem clara |
| shows the 403 message from the backend | Um 403 do backend mostra a mensagem dele |
| has a groups section with a clear empty state and a logout button | Tem a secção dos grupos, com estado vazio, e o botão de sair |

</details>

<details>
<summary><code>AdminApp.test.jsx</code> · 1 teste</summary>

| Teste | O que verifica |
|---|---|
| shows the ultra rare admin Pix and lets the admin log out | Mostra o Pix do admin e deixa sair da sessão |

</details>

#### Cobertura dos testes (JaCoCo)

O mesmo `clean test` gera o relatório de cobertura do backend (JaCoCo), sem nenhum comando extra:

```bash
cd backend && ./mvnw clean test      # Windows: mvnw.cmd clean test
# abrir backend/target/site/jacoco/index.html no browser (Windows: start target\site\jacoco\index.html)
```

O relatório mostra a cobertura de linhas e de ramos por pacote e por classe. A medição inicial, as classes mais fracas e a meta estão em [`docs/qualidade/cobertura.md`](docs/qualidade/cobertura.md). O `jacoco.xml` gerado é o que o SonarQube lê para mostrar a cobertura.

#### Testes do `QuizService` (Issue #120)

Testes unitários com Mockito (sem base de dados) em `backend/src/test/java/com/studymatch/service/QuizServiceTest.java`. Cobrem os caminhos de erro do quiz de arranque a frio, que antes não eram testados.

| Cobertura do `QuizService` | Linhas | Ramos |
|---|---|---|
| Antes | 40,7% | 30,0% |
| Depois | **100%** | **100%** |

Com estes testes, a cobertura do projeto passou de 78,7% para 82,4% nas linhas e de 69,5% para 73,8% nos ramos.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `submitQuiz_PedidoNulo_Devolve400` | Submeter o quiz sem pedido | 400 e nada é guardado |
| `submitQuiz_ListaDeRespostasNula_Devolve400` | Pedido sem a lista de respostas | 400 e nada é guardado |
| `submitQuiz_RespostasAMais_Devolve400` | Pedido com 21 respostas (o quiz tem 20) | 400 e nada é guardado |
| `submitQuiz_AlunoInexistente_Devolve404` | Submissão de um aluno que não existe | 404 "Estudante não encontrado" e a submissão não é guardada |
| `submitQuiz_QuizInexistente_Devolve404` | Submissão para um quiz que não existe | 404 "Quiz não encontrado" e a submissão não é guardada |
| `submitQuiz_PerguntaInexistente_Devolve400ComOId` | Resposta a uma pergunta que não existe | 400 com o ID da pergunta na mensagem e nenhuma resposta guardada |
| `submitQuiz_ValorForaDaEscala_Devolve400` | Resposta com valor 6 (a escala é de 1 a 5) | 400 e nenhuma resposta guardada |
| `getAllQuizzes_DevolveOsQuizzesDoRepositorio` | Listar os quizzes | Devolve os quizzes que estão na base de dados |
| `getQuizQuestions_QuizInexistente_Devolve404` | Pedir as perguntas de um quiz que não existe | 404 e as perguntas nem chegam a ser procuradas |
| `getQuizQuestions_DevolvePerguntasPorOrdem` | Pedir as perguntas de um quiz, com e sem o quiz associado | Devolve as perguntas pela ordem do repositório |
| `calculateResults_CalculaAMediaPorArea` | Resultado de uma submissão com respostas de duas áreas | Média por área: Programação (5, 4, 3) = 4,0 e Organização (1, 2) = 1,5 |
| `calculateResults_SemRespostas_DevolveMapaVazio` | Resultado de uma submissão sem respostas | Mapa vazio, sem erro |
| `getLatestSubmission_AlunoInexistente_Devolve404` | Última submissão de um aluno que não existe | 404 e as submissões nem chegam a ser procuradas |
| `getLatestSubmission_AlunoSemSubmissoes_Devolve404` | Última submissão de um aluno que nunca fez o quiz | 404 "Nenhuma submissão encontrada para o estudante com ID 1" |
| `getLatestResults_UsaASubmissaoMaisRecente` | Resultados do aluno quando há várias submissões | Usa só a submissão mais recente (média 3,0 a partir de 2 e 4) |

Para gerar o artefacto do backend (`backend/target/backend-*.jar`):

```bash
cd backend && ./mvnw clean package   # Windows: mvnw.cmd clean package
```

#### Testes da autenticação e das atribuições (Issue #122)

Testes que cobrem os ramos que faltavam no acesso aos dados dos estudantes, no login e nas atribuições feitas pelo admin. São testes unitários com Mockito, exceto o `SecurityAccessTest`, que arranca o backend completo com a base H2.

| Classe | Antes (linhas / ramos) | Depois (linhas / ramos) |
|---|---|---|
| `StudentAccess` | 85,7% / 60,0% | **100% / 100%** |
| `AuthService` | 94,6% / 66,7% | **100% / 87,5%** |
| `CourseUnitAssignmentService` | 86,8% / 65,4% | **100% / 96,2%** |
| `SecurityConfig` | 87,7% / 50,0% | **100%** / 50,0% |

No `SecurityConfig` fica por cobrir um único ramo: o `parseId` receber `null`. Isso não acontece na prática, porque a regra só se aplica a URLs com o `{studentId}`. Com estes testes, a cobertura do projeto passou de 78,7% para 80,4% nas linhas e de 69,5% para 76,1% nos ramos.

**`StudentAccessTest`** (`security/`): quem pode ver e alterar os dados de um estudante (RN05, RN16).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `currentUserIsNullWithoutASession` | Pedido sem sessão | Não há utilizador atual (`null`) |
| `currentUserIsNullWhenThePrincipalIsNotAnAuthenticatedUser` | Sessão cujo utilizador não é uma conta do StudyMatch | Não há utilizador atual (`null`) |
| `currentUserReturnsTheLoggedInUser` | Sessão de um aluno | Devolve o aluno da sessão |
| `nobodySeesAStudentWithoutASessionOrWithoutAnId` | Ver um aluno sem sessão, ou sem indicar o ID | Acesso recusado, sem consultar as UCs do docente |
| `theAdminSeesEveryStudent` | O admin a ver alunos diferentes | Acesso permitido a todos |
| `aStudentOnlySeesTheirOwnData` | Um aluno a ver os seus dados e os de outro | Só vê os seus |
| `aTeacherOnlySeesStudentsOfTheirUnits` | Um docente a ver um aluno da sua UC e um de fora | Só vê o aluno da sua UC |
| `onlyTheAdminAndTheStudentThemselvesCanEdit` | Alterar dados como admin, como o próprio aluno, como outro aluno e sem sessão | Só o admin e o próprio aluno podem alterar |
| `aTeacherCannotEditEvenAStudentOfTheirUnit` | Um docente a alterar dados de um aluno | Recusado, mesmo que o aluno esteja na sua UC |
| `isSelfOnlyForTheStudentWithTheSameId` | "É o próprio aluno?" com o ID certo, sem ID, sem sessão e para o admin | Só é verdade para o aluno com o mesmo ID |

**`AuthServiceTest`** (`service/`): testes novos do login, do registo e do logout.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `aLoginWithoutDataIsRejectedWithoutLookingForTheAccount` | Login sem utilizador nem palavra-passe | 401, sem procurar a conta na base de dados |
| `aStudentWithoutAnAccountCannotLogInWithTheEmail` | Aluno que existe mas ainda não tem conta a entrar com o email | 401 |
| `anEmptyRegistrationIsRejected` | Registo sem dados | 400 e o aluno não é criado |
| `aRegistrationIsRejectedWhenTheStudentNumberAlreadyHasAnAccount` | Registo com um número de estudante que já tem conta | 409 e a conta não é guardada |
| `logoutInvalidatesTheToken` | Terminar a sessão | O token deixa de ser válido |

**`CourseUnitAssignmentServiceTest`** (`service/`): testes novos das atribuições do admin (R1, R2).

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `listsTheTeacherAccounts` | Listar as contas de docente | Devolve os docentes com o utilizador certo |
| `aTeacherNeedsAUsernameAndAName` | Criar um docente sem dados, com o utilizador em branco ou sem nome | 400 e a conta não é guardada |
| `nullIdsAreTreatedAsNotFound` | Pedidos com o ID da UC, do docente ou do aluno em falta | 404 |
| `removesAnAssignedTeacherAndFailsIfNotAssigned` | Remover um docente associado à UC e um que não está | O primeiro é removido; o segundo dá 404 "Este docente não está associado a esta UC." |
| `theAssignmentsListTheTeachersAndTheEnrolledStudents` | Consultar as atribuições de uma UC | Devolve o docente e o aluno inscritos |

**`SecurityAccessTest`** (`security/`, backend completo) e **`SecurityConfigTest`** (`config/`): regras do Spring Security.

| Teste | O que verifica | Resultado esperado |
|---|---|---|
| `aStudentIdThatIsNotANumberIsDenied` | Um aluno a pedir `/api/students/abc` e `/api/students/abc/profile` | 403 |
| `loadsTheAccountWithItsHashAndRole` | Carregar uma conta da tabela `app_users` | Utilizador, hash da palavra-passe e role (`ROLE_DOCENTE`) certos |
| `anUnknownUsernameIsNotFound` | Carregar um utilizador que não existe | `UsernameNotFoundException` |

## Documentação

| Documento | Descrição |
|---|---|
| [Casos de uso](docs/casos-de-uso.md) | 16 casos de uso: as 6 áreas do enunciado e 4 propostas da equipa |
| [Modelo de domínio](docs/modelo-dominio.md) | Diagrama de classes UML do domínio |
| [Arquitetura](docs/arquitetura.md) | Diagrama de arquitetura e responsabilidades dos componentes |
| [ADR-001](docs/decisions/ADR-001-stack-tecnologica.md) | Decisão e justificação da stack |
| [ADR-002](docs/decisions/ADR-002-perfil-e-cold-start.md) | Modelo do perfil de competências e estratégia de cold start |
| [CONTRIBUTING](CONTRIBUTING.md) | Workflow, convenções de código e Definition of Done |

## Workflow da equipa

Todo o trabalho segue o fluxo **Issue → Branch → Pull Request → Review → Merge**:

- cada tarefa é uma Issue no Project board;
- branches com o formato `feat/<id>-descricao` ou `docs/<id>-descricao`;
- cada PR precisa da aprovação de outro membro, e ninguém aprova o seu próprio PR;
- depois do merge, a branch é eliminada.

Os detalhes estão no [CONTRIBUTING.md](CONTRIBUTING.md).

## Equipa — Grupo 2

| Membro | GitHub |
|---|---|
| Elisio Leite | [@ELZY01](https://github.com/ELZY01) |
| Aires David | [@A1res16](https://github.com/A1res16) |
| Bruna Costa | [@Bruninha9](https://github.com/Bruninha9) |
| Francisco Jacinto | [@FranciscoJacinto7077](https://github.com/FranciscoJacinto7077) |

## Estado atual — Sprint 2

O Sprint 2 constrói o subsistema de **perfil do estudante**: gestão de estudantes, histórico académico, perfil de competências calculado a partir das notas, quiz de arranque a frio para alunos sem histórico e a **explicação do perfil** (funcionalidade da equipa). As regras e a sua justificação estão no [ADR-002](docs/decisions/ADR-002-perfil-e-cold-start.md). A formação automática de grupos fica para os próximos sprints.

O Sprint 1 foi a fundação do projeto: organização no GitHub, casos de uso, modelo de domínio, arquitetura e um esqueleto full-stack a funcionar (frontend → API → base de dados).
