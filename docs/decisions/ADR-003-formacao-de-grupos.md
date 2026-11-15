# ADR-003 — Estratégia de formação automática de grupos

- **Estado:** Aceite (estratégia aprovada pelo grupo a 29/10/2026)
- **Data:** 2026-11-15
- **Issue:** #73 · relacionada com #21 (US10), #22 (US11), #23 (US12), #24 (US13), #70, #71, #72
- **Interface:** `backend/src/main/java/com/studymatch/grouping/GroupFormation.java`
- **Implementação:** #70 (tamanhos) e #71 (distribuição), no pacote `com.studymatch.grouping`
- **Testes:** `backend/src/test/java/com/studymatch/grouping/`

## Contexto

O Sprint 3 pede que o StudyMatch forme grupos **automaticamente** a partir dos perfis do Sprint 2 (ADR-002). O enunciado não impõe algoritmo, mas obriga a regras fixas (secção 3.1):

- todos os alunos elegíveis são considerados;
- cada aluno fica em **um só grupo** por geração, e nunca repetido no mesmo grupo;
- os grupos têm **4 ou 5 alunos** sempre que for matematicamente possível;
- configurações impossíveis são tratadas **explicitamente**;
- os grupos ficam guardados e podem ser consultados mais tarde.

A equipa tem de justificar que características usa, como influenciam o resultado, o que é um bom grupo e como se combinam os critérios. É isso que este ADR decide.

## O que é um bom grupo para o StudyMatch

1. **Equilibrado:** nenhum grupo fica muito mais forte ou mais fraco do que os outros (justiça entre grupos).
2. **Complementar:** dentro do grupo há alguém forte em cada uma das 4 áreas, para o trabalho não ficar parado numa delas.
3. **Explicável:** o docente e o aluno percebem porque é que aqueles alunos ficaram juntos.

A ordem importa: o equilíbrio é uma restrição, a complementaridade é o que se tenta melhorar dentro dela.

## Decisões

### D1. Tamanhos: grupos de 5 e de 4

- Nº de grupos: `k = ⌈n / 5⌉`.
- Há configuração válida se `4k ≤ n`. Ficam `n − 4k` grupos de 5 e os restantes de 4.

| Alunos (n) | Grupos | Tamanhos |
|---|---|---|
| 4 | 1 | 4 |
| 9 | 2 | 5 + 4 |
| 12 | 3 | 4 + 4 + 4 |
| 23 | 5 | 5 + 5 + 5 + 4 + 4 |

- **Impossíveis:** `n ∈ {1, 2, 3, 6, 7, 11}` (e 0). A partir de 12 alunos há sempre solução.
- Impossível → `ImpossibleGroupConfigurationException` com mensagem clara; a API responde **400**. Não se forma um grupo de 3 "à força", porque violava uma regra obrigatória.
- Alunos duplicados na entrada (mesmo `studentId`) também são rejeitados.

**Porquê `⌈n/5⌉`?** Usa o menor número de grupos possível, logo os grupos maiores possíveis. Com 23 alunos dá 5+5+5+4+4 (válido) e evita 5+5+5+5+3 (inválido), que é o exemplo do enunciado.

### D2. Características usadas: os 4 níveis do perfil

Cada aluno entra no algoritmo como um `GroupCandidate`: o seu nível (1 a 5) em `ORG`, `TEMPO`, `PROG` e `EQUIPA`, tal como o `ProfileCalculator` o calcula (ADR-002). Já inclui as notas, o peso do quiz e a transição quiz → histórico, por isso a formação de grupos não repete essas regras.

- **Média geral do aluno** = média dos níveis das competências **com dados**.
- Não se usam notas individuais, nome, curso nem ano: só o perfil.

### D3. Equilíbrio: distribuição em serpentina

1. Ordenar os alunos pela média geral, da maior para a menor.
2. Distribuir em serpentina: G1, G2, …, Gk, Gk, …, G2, G1, G1, G2, …, respeitando os tamanhos de D1.

Assim, cada grupo recebe alunos de todos os níveis e as médias dos grupos ficam próximas.

- **Desempate:** pelo número de estudante (ordem crescente). É único, por isso o resultado é **sempre o mesmo** para os mesmos dados (substitui a "ordem alfabética" da RN23, que não é única).
- **Limite de equilíbrio:** a diferença entre a maior e a menor média de grupo deve ficar ≤ **0,5** (RN24). É um parâmetro do algoritmo.

### D4. Complementaridade: trocas que cobrem mais áreas

- Um aluno é **forte** numa área se o seu nível nela for ≥ **3,5**.
- **Cobertura** de um grupo = nº de áreas (0 a 4) em que o grupo tem pelo menos um aluno forte.
- Depois da serpentina, o algoritmo percorre os pares de alunos de grupos diferentes, por ordem fixa, e faz a troca se:
  1. a cobertura total (soma das coberturas dos grupos) **aumenta**; e
  2. a diferença de médias entre grupos **continua ≤ 0,5** (ou não piora, se a serpentina já a tiver ultrapassado).
- Repete até não haver nenhuma troca que melhore. Trocas não mudam os tamanhos, por isso D1 continua garantida.

**Porquê assim?** É simples de explicar ("trocámos alunos para cada grupo ter alguém forte em cada área, sem desequilibrar"), é determinístico e termina sempre, porque cada troca aceite aumenta a cobertura, que tem um máximo.

### D5. Cold start e dados em falta

- Um aluno só com quiz entra normalmente: os seus níveis vêm do quiz (origem QUIZ no ADR-002).
- Uma competência **sem dados** não entra na média como 0 nem como 1 (coerente com a D8 do ADR-002), e não conta como "forte" nessa área.
- Um aluno **sem dados nenhuns** (não deve acontecer, porque o quiz é obrigatório) entra com média **neutra 3,0** só para a ordenação, e fica assinalado. Não se exclui ninguém: todos os elegíveis têm de ser considerados.

### D6. Explicação de cada grupo

O resultado (`GroupFormationResult`) traz, por grupo:

- **média** do grupo (média das médias dos membros);
- **diferença para os outros:** média do grupo − média dos outros grupos;
- **membro mais forte por área** (empate pelo número de estudante; uma área sem ninguém com dados fica de fora);

e, no total, a **maior diferença entre médias** (`maxAverageGap`).

Respeita a RN26: o ecrã mostra quem é o mais forte em cada área, **nunca** os níveis nem as notas individuais dos colegas.

### D7. Java puro, atrás de uma interface

`GroupFormation` é uma **interface**: recebe a lista de competências e os candidatos, devolve o resultado. A implementação (#70 e #71) não depende de Spring nem da base de dados, tal como o `ProfileCalculator`. Consequências:

- os testes JUnit correm em milissegundos e cobrem todas as regras de D1 a D6;
- o service da API (#69) depende só da interface, e pode simulá-la com Mockito;
- uma segunda estratégia (por exemplo, só equilíbrio) pode ser acrescentada sem mexer no resto.

### D8. Guardar o resultado de cada geração

Ao contrário do perfil (ADR-002, D9), os grupos **são guardados**: uma geração é uma decisão do docente e tem de poder ser consultada depois, mesmo que as notas mudem. A unicidade `(contexto, aluno)` na base de dados (#68) garante a regra "um aluno, um grupo" também ao nível dos dados.

## Alternativas consideradas

| Alternativa | Porque foi rejeitada |
|---|---|
| Grupos aleatórios | Não usa o perfil; pode juntar todos os alunos fortes no mesmo grupo. |
| Só serpentina, sem complementaridade | Equilibra as médias, mas pode deixar um grupo sem ninguém forte em Programação. |
| Agrupar alunos parecidos | Grupos fortes e grupos fracos: o contrário do objetivo. |
| Otimização exata (programação inteira) | Melhor solução teórica, mas difícil de explicar, de testar e pesada para a demonstração. |
| Tamanho escolhido pelo docente (RN21 antiga) | O enunciado do Sprint 3 obriga a grupos de 4 ou 5. |

## Consequências

- A formação de grupos reutiliza o perfil do Sprint 2 sem repetir regras.
- Todas as regras obrigatórias têm teste direto: tamanhos (incluindo 11 vs 12 alunos), aluno num só grupo, duplicados, cold start e dados em falta.
- **Limitação conhecida:** a complementaridade é uma melhoria local (trocas de um par de cada vez), não garante a melhor distribuição possível. Para turmas de dezenas de alunos é suficiente e explicável.
- **Limitação conhecida:** os perfis só com quiz são autoavaliados. O D7 do ADR-002 reduz o peso do quiz à medida que entram notas, por isso as gerações futuras ficam mais fiáveis.
