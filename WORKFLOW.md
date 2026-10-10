 # StudyMatch - Workflow & Quality Guidelines

## Workflow de Desenvolvimento

### 1. Fluxo de Trabalho (Git Workflow)
O nosso fluxo segue estritamente o ciclo: **Issue ➔ Branch ➔ Commit ➔ Pull Request (PR) ➔ Review ➔ Merge**.

1.1. **Issues:** Todo o trabalho deve ser previamente associado a uma Issue no GitHub Projects.  
1.2. **Branches:** É estritamente proibido fazer commits diretos no ramo `main`. O trabalho é isolado em ramos 
temporários criados a partir do `main` atualizado:
- `feature/#<id>-descricao` (ex: `feature/#14-registar-inscricoes`)
- `docs/#<id>-descricao` (ex: `docs/#2-#7-workflow`)  
  1.3. **Commits:** O título/resumo do commit deve ser escrito em **Inglês** (ex: `docs: add workflow guidelines`). 
A descrição detalhada no corpo do commit pode ser em **Inglês ou Português**.

### 2. Regras de Pull Request (PR) e Code Review
2.1. **Criação de PR:** O título do PR deve indicar o seu propósito e associar-se à Issue respetiva
(ex: `docs: #2 #7 Add workflow and DoD`).  
2.2. **Descrição do PR:** Deve conter a palavra-chave para fechar/vincular a Issue automaticamente
(ex: `Closes #2, Closes #7`).  
2.3. **Code Review:**
- Nenhum PR é fundido (*merged*) sem **pelo menos 1 aprovação** de outro membro da equipa.
- É **proibido aprovar o próprio PR**.
- O revisor deve inspecionar as alterações, testar/compilar e validar os critérios de aceitação da Issue.  
2.4. **Limpeza:** O ramo de origem (*head branch*) deve ser eliminado do GitHub após o *merge* bem-sucedido.


## Convenções de Nomenclatura e Código

Para garantir legibilidade e consistência no código:

1. **Classes e Interfaces:** `PascalCase` e substantivos no singular (ex: `StudentProfile`).
2. **Métodos e Variáveis:** `camelCase` (ex: `calculateGrade()`, `studentId`).
3. **Constantes:** `UPPER_SNAKE_CASE` (ex: `MAX_GROUP_SIZE`).
4. **Métodos Acessores:** Usar `getSomething()`, `setSomething()` e `isSomething()` para booleanos.
5. **Idioma do Código:** Código em **Inglês**, comentários e mensagens de commit em **Inglês ou Português**.


## Definition of Done (DoD) - Sprint 1

Uma Issue/tarefa só é considerada concluída (*Done*) quando cumpre os seguintes requisitos:

1. **Rastreabilidade:** Criada a partir de uma Issue e desenvolvida num ramo único e temporário.
2. **Qualidade de Código:** Cumpre as convenções de nomenclatura e compila sem erros localmente.
3. **Revisão:** Tem pelo menos uma aprovação independente por outro colega no Pull Request.
4. **Integração:** Efetuado o *merge* no `main`, e o ramo temporário eliminado no GitHub e localmente.