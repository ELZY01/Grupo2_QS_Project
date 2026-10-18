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

### 2. Frontend

Num segundo terminal:

```bash
cd frontend
cp .env.example .env          # Windows (cmd): copy .env.example .env
npm install
npm run dev
```

O ficheiro `.env` define `VITE_API_URL=http://localhost:8080`, o endereço do backend. A aplicação abre em http://localhost:5173 e mostra o estado do backend (UP).

### Testes

```bash
cd backend && ./mvnw clean test   # backend (Windows: mvnw.cmd clean test)
cd frontend && npm test           # frontend (Vitest)
```

Os testes do backend **não precisam das credenciais da base de dados**: usam uma base H2 em memória (perfil `test`). O `clean test` compila o projeto e corre todos os testes unitários, incluindo as regras do perfil de competências (`ProfileCalculatorTest`).

Para gerar o artefacto do backend (`backend/target/backend-*.jar`):

```bash
cd backend && ./mvnw clean package   # Windows: mvnw.cmd clean package
```

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
