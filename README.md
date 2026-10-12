# Grupo2_QS_Project

StudyMatch
> Projeto de grupo de **Qualidade de Software** — Grupo 2 · Universidade Portucalense
O StudyMatch é uma aplicação web que forma grupos de trabalho equilibrados a partir das competências dos estudantes. As competências são calculadas com base no percurso académico de cada aluno: UCs, inscrições e notas.
Contexto do produto
Nos trabalhos de grupo, os grupos costumam formar-se por afinidade ou ao acaso. Isso cria grupos desequilibrados: uns concentram os alunos mais fortes e outros ficam sem ninguém que domine as áreas críticas. O StudyMatch resolve este problema em quatro passos:
Representa o percurso académico de cada estudante: as UCs, as tentativas e as notas.
Converte notas em competências. Cada UC contribui, com um peso, para uma ou mais competências, numa escala de 1 a 5.
Constrói um perfil de competências do estudante, com os pontos fortes e fracos.
Gera grupos equilibrados num contexto definido pelo docente (UC, atividade e tamanho do grupo).
Problemas que resolvemos
Problema	Solução
Arranque a frio: um aluno novo ainda não tem notas	Quiz inicial de 20 perguntas em 4 áreas (organização, gestão de tempo, programação, trabalho em grupo) que dá um perfil de 1 a 5 desde o primeiro dia. O peso do quiz diminui à medida que o aluno conclui UCs.
Equilíbrio dos grupos	Os alunos são ordenados pela média do perfil e distribuídos em serpentina (G1, G2, G2, G1, …), o que dá grupos com médias praticamente iguais. O docente pode ajustar os grupos manualmente.
Funcionalidades propostas pela equipa
Explicação da formação do grupo: o aluno vê porque ficou num grupo, sem ver as notas dos colegas.
Importação por CSV: o docente importa alunos e notas de um ficheiro.
Avaliação entre pares: no fim do trabalho, os colegas avaliam-se de forma anónima, e essa avaliação atualiza a competência "trabalho em grupo".
Atores
Estudante: regista o percurso, responde ao quiz, consulta o perfil e participa em grupos.
Docente: define competências, cria contextos de agrupamento, gera e ajusta grupos.
Stack tecnológica
Camada	Tecnologia
Frontend	React + Vite (npm)
Backend / API	Java 21 + Spring Boot (API REST), Maven
Persistência	PostgreSQL (Supabase) via Spring Data JPA
Testes	JUnit 5 + Spring Boot Test · Vitest + React Testing Library
Gestão	Git + GitHub (Issues, Projects, Pull Requests)
A justificação das escolhas está no ADR-001.
Estrutura do repositório
```
Grupo2_QS_Project/
├── backend/     # API Spring Boot (Maven)
├── frontend/    # Aplicação React (Vite)
├── docs/        # Casos de uso, modelo de domínio, arquitetura, decisões (ADR)
├── CONTRIBUTING.md
└── README.md
```
Como correr o projeto
Requisitos
Java 21 (o Maven Wrapper vem incluído no projeto)
Node.js 20+ (inclui o npm)
Acesso à base de dados PostgreSQL. As credenciais são definidas por variáveis de ambiente e nunca entram no repositório.
1. Backend
```bash
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```
A API arranca em http://localhost:8080. Para verificar: http://localhost:8080/api/health deve devolver `{"status":"UP"}`.
2. Frontend
```bash
cd frontend
npm install
npm run dev
```
A aplicação abre em http://localhost:5173 e mostra o estado do backend (UP).
Testes
```bash
cd backend && ./mvnw test     # backend
cd frontend && npm test       # frontend
```
Documentação
Documento	Descrição
Casos de uso	15 casos de uso: as 6 áreas do enunciado e 3 propostas da equipa
Modelo de domínio	Diagrama de classes UML do domínio
ADR-001	Decisão e justificação da stack
CONTRIBUTING	Workflow, convenções de código e Definition of Done
Workflow da equipa
Todo o trabalho segue o fluxo Issue → Branch → Pull Request → Review → Merge:
cada tarefa é uma Issue no Project board;
branches com o formato `feature/<id>-descricao` ou `docs/<id>-descricao`;
cada PR precisa da aprovação de outro membro, e ninguém aprova o seu próprio PR;
depois do merge, a branch é eliminada.
Os detalhes estão no CONTRIBUTING.md.

Equipa — Grupo 2
Membro	GitHub
Elisio Leite @ELZY01
Aires David @A1res16
Bruna Costa @Bruninha9
Francisco Jacinto	@FranciscoJacinto7077
Estado atual — Sprint 1
O Sprint 1 é a fundação do projeto: organização no GitHub, casos de uso, modelo de domínio, arquitetura e um esqueleto full-stack a funcionar (frontend → API → base de dados). As funcionalidades do produto (histórias de utilizador US01–US15) estão no Backlog para os próximos sprints. 