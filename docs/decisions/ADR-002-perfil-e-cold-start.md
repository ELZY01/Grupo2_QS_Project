# ADR-002 — Modelo do perfil de competências e estratégia de cold start

- **Estado:** Aceite
- **Data:** 2026-10-18
- **Issue:** #51 · relacionada com #44, #39 (US16), #17 (US06), #20 (US09)
- **Implementação:** `backend/src/main/java/com/studymatch/profile/ProfileCalculator.java`
- **Testes:** `backend/src/test/java/com/studymatch/profile/ProfileCalculatorTest.java`

## Contexto

O Sprint 2 pede que o StudyMatch construa um **perfil de competências** a partir do percurso académico e que represente alunos **sem histórico** (*cold start*). O enunciado não impõe fórmulas nem lista de competências, mas exige que a abordagem seja clara, coerente, testada e preparada para mudar.

O perfil é a base da formação de grupos nos próximos sprints, por isso tem de ser:
1. **explicável:** o aluno e o docente percebem de onde vem cada valor;
2. **robusto a dados em falta:** um caloiro tem de ter perfil desde o primeiro dia;
3. **testável sem infraestrutura:** as regras correm em testes unitários, sem base de dados.

## Decisões

### D1. Quatro competências base, pré-carregadas

| Código | Competência |
|---|---|
| `ORG` | Organização |
| `TEMPO` | Gestão de tempo |
| `PROG` | Programação |
| `EQUIPA` | Trabalho em grupo |

São as quatro áreas do quiz inicial (RN12). Neste sprint entram na base de dados por *script* (`V2__insert_initial_competencies.sql`). A configuração pelo docente (US05, #16) fica no backlog: **é uma decisão de âmbito**, não uma limitação do modelo, porque o cálculo recebe a lista de competências como parâmetro.

### D2. Cada UC contribui com um peso para cada competência

A associação `CourseUnitCompetency` guarda um peso em ]0, 1] (RN11). Exemplo dos dados de desenvolvimento (`V3__...sql`): *Programação I* conta 1,0 para `PROG` e 0,3 para `ORG`.

**Porquê pesos e não "uma UC = uma competência"?** Porque uma UC desenvolve várias capacidades em graus diferentes. Os pesos permitem modelar isto sem mudar o código, só os dados.

### D3. Por UC conta a melhor nota aprovada

- Todas as tentativas ficam guardadas (RN07), mas para o perfil conta **a melhor nota ≥ 10** (RN08).
- **Reprovações não contam.** Uma reprovação seguida de aprovação não deve penalizar o aluno para sempre. O perfil descreve o que ele **sabe agora**, não o caminho até lá.
- Inscrições **em curso** (sem nota) não contam.

### D4. Conversão nota → nível: `nivel = 1 + 4 × (nota − 10) / 10`

| Nota | 10 | 12 | 15 | 18 | 20 |
|---|---|---|---|---|---|
| Nível | 1,0 | 1,8 | 3,0 | 4,2 | 5,0 |

**Esta decisão substitui a versão do Sprint 1** (`1 + 4 × nota / 20`, RN13). Como só entram notas aprovadas (D3), a fórmula antiga só produzia valores entre 3,0 e 5,0: os níveis 1 e 2 ficavam inalcançáveis e as diferenças entre alunos ficavam comprimidas. A nova fórmula usa **toda a escala 1–5** para o intervalo de notas que realmente conta (10–20).

### D5. Nível histórico = média dos níveis ponderada pelos pesos

```
nivelHistorico(c) = Σ (peso_UC × nivel_UC) / Σ peso_UC     (só UCs aprovadas associadas a c)
```

Como a conversão D4 é linear, é equivalente a converter a média ponderada das notas. Calcula-se por UC porque assim cada parcela fica disponível para a explicação do perfil (D8).

### D6. Cold start: quiz de autoavaliação

- 20 perguntas, 5 por competência base, resposta de 1 a 5 (UC08, RN17, RN18).
- Resultado por área = média das 5 respostas (1–5).
- **Porquê autoavaliação e não um teste com respostas certas?** "Organização", "gestão de tempo" e "trabalho em grupo" não têm respostas objetivamente certas. Uma escala de 1 a 5 é rápida para um caloiro e dá um valor diretamente comparável com o nível vindo das notas. A desvantagem (as pessoas tendem a sobrestimar-se) é mitigada pela D7: o quiz perde peso à medida que surgem dados reais.

### D7. Transição quiz → histórico

```
pesoQuiz = max(0, 1 − n / N)          n = nº de UCs diferentes aprovadas, N = 6
nivel    = pesoQuiz × nivelQuiz + (1 − pesoQuiz) × nivelHistorico
```

| UCs aprovadas (n) | 0 | 1 | 3 | 6 | 7+ |
|---|---|---|---|---|---|
| Peso do quiz | 100% | 83% | 50% | 0% | 0% |

Exemplo (UC09): 3 UCs aprovadas, quiz 4,0 e histórico 3,0 → `0,5 × 4,0 + 0,5 × 3,0 = 3,5`.

**Porquê N = 6?** Corresponde, aproximadamente, a um semestre completo. A partir daí há evidência real suficiente para deixar de depender da autoavaliação. `N` é um parâmetro do `ProfileCalculator` e pode ser ajustado sem alterar código.

### D8. Dados em falta e origem do valor

Cada competência indica **de onde vem** o seu nível:

| Situação | Origem | Nível |
|---|---|---|
| Sem quiz e sem notas nas UCs associadas | `NO_DATA` | vazio ("sem dados") |
| Só quiz (ou sem notas nesta competência) | `QUIZ` | resultado do quiz |
| Só notas (ou `pesoQuiz = 0`) | `HISTORY` | nível histórico |
| Ambos | `MIXED` | combinação D7 |

Uma competência sem notas próprias usa só o quiz, **mesmo que o aluno tenha outras UCs aprovadas**. Não faz sentido "diluir" o quiz com zero evidência.

"Sem dados" é representado como **ausência de valor**, nunca como 0 ou 1. Assim, a falta de informação não é confundida com uma competência fraca, e a formação de grupos pode tratá-la de forma diferente.

O resultado inclui ainda, para cada competência, a lista de **contribuições** (UC, nota usada, peso, fração do nível). É a base da funcionalidade da equipa, a explicação do perfil (US16, UC16).

### D9. O perfil é calculado, não guardado

O perfil é recalculado a partir dos dados atuais em cada pedido (RN14). Com o volume de dados de uma turma, o custo é desprezável, e elimina o risco de o perfil guardado ficar desatualizado em relação às notas. Se o volume crescer, pode passar a ser guardado em cache, sem mudar as regras.

### D10. Arredondamentos só na apresentação

O cálculo usa precisão total. O arredondamento (1 casa decimal) é feito apenas no frontend, para evitar erros acumulados quando os níveis forem usados na formação de grupos.

## Alternativas consideradas

| Alternativa | Porque foi rejeitada |
|---|---|
| Média simples das notas, sem pesos | Não distingue UCs centrais de UCs marginais para uma competência. |
| Média de todas as tentativas | Penaliza para sempre quem reprovou e depois aprovou. |
| Quiz com respostas certas (teste) | Competências comportamentais não têm resposta objetiva. Aumenta o tempo de onboarding. |
| Manter o quiz sempre a 50% | A autoavaliação continuaria a pesar mesmo com um curso inteiro de evidência real. |
| Usar a nota de candidatura ao ensino superior | Não está disponível no sistema e não se relaciona com as 4 competências. |
| Guardar o perfil na base de dados | Duplica informação derivada e cria risco de inconsistência (D9). |

## Consequências

- As regras estão concentradas numa classe sem dependências de Spring nem da base de dados, coberta por 33 testes unitários: cenário normal, melhor tentativa, reprovações, limites (notas 10 e 20; 0, 3, 6 e 7 UCs), exemplo do UC09, dados em falta e dados inválidos.
- Mudar a escala, o `N`, as competências ou os pesos não exige alterar a estrutura: são dados ou parâmetros.
- A avaliação entre pares (UC15, RN31) poderá entrar mais tarde como uma terceira fonte na D7, sem mudar as restantes regras.
- **Limitação conhecida:** o resultado do quiz é autoavaliado e pode ser otimista. Fica mitigado pela D7 e pela indicação da origem (D8), que deixa claro quando um valor vem só do quiz.
