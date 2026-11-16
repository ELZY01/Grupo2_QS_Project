# Cobertura de testes (JaCoCo)

- **Issue:** #76 · relacionada com #72, #113, #114, #115 e com o relatório de qualidade (#85)
- **Ferramenta:** JaCoCo 0.8.13, configurado em `backend/pom.xml`
- **Como gerar:** `cd backend` e `mvnw clean test` (Windows) ou `./mvnw clean test`; o relatório fica em `backend/target/site/jacoco/index.html`
- **Dados brutos:** `jacoco-antes-2026-11-16.csv` (exportado pelo próprio JaCoCo, uma linha por classe)

## Medição inicial ("antes"), 16/11/2026

Medido sobre o `main` depois dos PRs #116 (interface dos grupos) e #117 (tabelas dos grupos), antes dos testes dos controllers e do algoritmo final dos grupos.

| Métrica | Cobertos | Cobertura |
|---|---|---|
| **Linhas** | 895 de 1 148 | **78,0%** |
| **Ramos** (*branches*) | 238 de 347 | **68,6%** |
| Instruções | 3 938 de 4 884 | 80,6% |
| Métodos | 275 de 382 | 72,0% |

### Por pacote

| Pacote | Linhas | Ramos | Leitura |
|---|---|---|---|
| `profile` | 97,2% | 88,0% | Regras do perfil e do cold start (Sprint 2), bem testadas. |
| `security` | 96,8% | 71,9% | Autenticação bem coberta; faltam alguns ramos de acesso (`StudentAccess`, 56%). |
| `config` | 92,1% | 50,0% | Arranque e segurança; poucos ramos no total. |
| `service` | 79,2% | **59,6%** | Faltam sobretudo caminhos de erro. 59 ramos por cobrir. |
| `controller` | 70,6% | **25,0%** | Sem testes próprios: só cobertos indiretamente. |
| `grouping` | 61,9% | 83,3% | Só o `GroupCandidate` tem testes; o resultado dos grupos ainda não. |
| `model` | 59,4% | 38,9% | Entidades JPA: getters, setters e entidades novas dos grupos ainda sem uso. |
| `dto` | 100% | sem ramos | Records sem lógica. |
| `com.studymatch` | 33,3% | sem ramos | Só o `main()` do `BackendApplication`. |

### Classes importantes com cobertura fraca

| Classe | Linhas | Ramos | Porque importa | Plano |
|---|---|---|---|---|
| `QuizService` | 40,7% | 30,0% | Submissão do quiz = arranque a frio. Os erros (aluno ou pergunta inexistente, respostas a mais) não são testados. | Testes dos caminhos de erro; `@WebMvcTest` do `QuizController` (#115) |
| `StudentService` | 51,2% | 50,0% | Validação dos dados do aluno: metade dos ramos nunca corre. | Testes das validações; melhoria #84 (Bean Validation) |
| `QuizController` | 27,3% | 0,0% | Pedido de entrada do quiz. | `@WebMvcTest` (#115) |
| `AdminAssignmentController` | 60,0% | 25,0% | Só o administrador pode atribuir docentes e alunos. | `@WebMvcTest` (#114) |
| `EnrollmentService` | 83,6% | 68,4% | Registo de notas, que alimenta o perfil. 12 ramos por cobrir. | Casos inválidos (nota fora de 0–20, estado incoerente) |
| `FormedGroup`, `GroupFormationResult` | 0% | 0% | Resultado da formação de grupos. | Testes da implementação dos grupos (#71, #72) |

### O que não vale a pena forçar

- **`BackendApplication.main()`:** só arranca o Spring; já é exercitado pelo teste `contextLoads`.
- **Getters e setters das entidades (`model`):** sem lógica; ficam cobertos quando os services e controllers que as usam forem testados. Não se escrevem testes só para getters.

## Meta

- **≥ 90% de linhas e de ramos** em `profile`, `service` e `grouping`, que é a lógica de negócio.
- **≥ 85% de linhas no total**, sem excluir pacotes do relatório.

Cobertura alta não prova que o código está certo: só mostra que foi executado. Por isso cada teste novo verifica o resultado (valores esperados, códigos 400/404) e os limites (por exemplo, 11 vs 12 alunos), e não só que o código "corre".

## Medição final ("depois")

*A preencher no fim do Sprint 3, com o mesmo comando, e com um novo CSV `jacoco-depois-AAAA-MM-DD.csv` para comparação.*
