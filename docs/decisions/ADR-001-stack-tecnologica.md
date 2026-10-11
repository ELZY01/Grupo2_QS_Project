# ADR-001 — Stack tecnológica do StudyMatch

* **Estado:** Aceite
* **Data:** 2026-10-10
* **Issue:** #3

## Contexto

O StudyMatch é uma aplicação full-stack (frontend, API, lógica de negócio e persistência) desenvolvida por uma equipa de 4 elementos, em sprints, com requisitos que vão mudar ao longo do projeto. A stack tem de ser adequada ao problema, fácil de manter e testar, e conhecida pela equipa.

## Decisão

|Camada|Tecnologia|Ferramenta de build/dependências|
|-|-|-|
|Frontend|React|npm + Vite|
|Backend / API|Java 17+ com Spring Boot (API REST)|Maven|
|Persistência|PostgreSQL alojado no Supabase, acedido via Spring Data JPA|(gerido pelo Maven)|
|Modelação|Visual Paradigm (UML: casos de uso, domínio, arquitetura)|—|
|Gestão e versões|Git + GitHub (Issues, Projects, Pull Requests)|—|

## Justificação

**Adequação.** O problema é uma aplicação web clássica: utilizadores, dados académicos relacionais (alunos, UCs, inscrições, notas, competências) e regras de negócio (cold start, formação de grupos). Um frontend web, uma API REST e uma base de dados relacional servem diretamente este modelo.

**Manutenção.** O Spring Boot impõe uma estrutura em camadas (controller → service → repository) que separa a API da lógica de negócio e da persistência. Isto permite alterar regras, como o algoritmo de agrupamento, sem mexer nas outras camadas. O React organiza a interface em componentes reutilizáveis.

**Testabilidade.** No backend usamos JUnit 5, Mockito e Spring Boot Test, integrados no Maven (`mvn test`). No frontend, Vitest e React Testing Library. A separação em camadas permite testar a lógica de negócio sem base de dados.

**Integração.** O React comunica com o Spring Boot por HTTP/JSON. O Spring Boot liga-se ao PostgreSQL do Supabase por JDBC/JPA, que é o caminho padrão e bem documentado.

**Conhecimento da equipa.**A equipa já trabalhou com Java noutras unidades curriculares, mas não com React. Escolhemos React mesmo assim por três razões: é a biblioteca de frontend com mais documentação, tutoriais e comunidade, o que reduz o tempo de aprendizagem; é a mais procurada no mercado, o que dá valor à aprendizagem; e é mais simples de começar do que o Angular. Para controlar o risco, no Sprint 1 o frontend fica limitado a um ecrã mínimo, criado a partir do template oficial do Vite, e a lógica de negócio fica toda no backend, onde a equipa tem mais experiência.

**Ferramentas.** O Supabase dá-nos um PostgreSQL gerido e gratuito, partilhado por toda a equipa, sem instalação local. O Maven e o npm são o standard dos respetivos ecossistemas. O Visual Paradigm suporta todos os diagramas UML pedidos.

## Alternativas consideradas

* **Base de dados NoSQL (MongoDB):** rejeitada, porque os dados são fortemente relacionais (aluno ↔ inscrição ↔ UC ↔ competência).
* **SQLite:** rejeitada, porque não permite uma base de dados partilhada pela equipa e não representa um ambiente realista.
* **Gradle:** é equivalente ao Maven. Escolhemos Maven por familiaridade da equipa.

## Consequências e regras

* **O frontend nunca acede diretamente ao Supabase.** Todo o acesso a dados passa pela API Spring Boot, para que as regras de negócio fiquem no backend.
* **As credenciais da base de dados ficam em variáveis de ambiente** e nunca são versionadas. Existe um ficheiro `.env.example` sem valores reais.
* Os testes automáticos não dependem da base de dados partilhada. Usam uma base local ou em memória.
* Esta decisão pode ser revista em sprints futuros. Uma alteração gera um novo ADR.

