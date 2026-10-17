# StudyMatch - Workflow & Quality Guidelines

## Workflow de Desenvolvimento

### 1. Fluxo de Trabalho (Git Workflow)
O nosso fluxo segue estritamente o ciclo: **Issue ➔ Branch ➔ Commit ➔ Pull Request (PR) ➔ Review ➔ Merge**.

1.1. **Issues:** Todo o trabalho deve ser previamente associado a uma Issue no GitHub Projects.  
1.2. **Branches:** É estritamente proibido fazer commits diretos no ramo `main`. O trabalho é isolado em ramos
temporários criados a partir do `main` atualizado:
- `feat/<id>-descricao` — novas funcionalidades (ex: `feat/14-registar-inscricoes`).
- `fix/<id>-descricao` — correções e dívida técnica (ex: `fix/s2-0-divida-tecnica`).
- `docs/<id>-descricao` — alterações de documentação (ex: `docs/7-workflow`).
  1.3. **Commits:** O título/resumo do commit deve ser escrito em **Inglês** (ex: `docs: add workflow guidelines`).
- A descrição detalhada no corpo do commit pode ser em **Inglês ou Português**.

### 2. Atribuição de Issues
- Todas as tarefas e histórias de utilizador devem estar devidamente registadas e geridas no GitHub Projects.
- Cada membro da equipa deve associar-se explicitamente como **Assignee** na respetiva Issue antes de iniciar
o desenvolvimento no seu ramo temporário, garantindo a responsabilização individual e a transparência do progresso.

### 3. Regras de Pull Request (PR) e Code Review
3.1. **Criação de PR:** O título do PR deve indicar o seu propósito e associar-se à Issue respetiva
(ex: `docs: #2 #7 Add workflow and DoD`).  
3.2. **Descrição do PR:** Deve conter a palavra-chave para fechar/vincular a Issue automaticamente
(ex: `Closes #2, Closes #7`).  
3.3. **Code Review e Responsabilidades:**
- Nenhum PR é fundido (*merged*) sem **pelo menos 1 aprovação independente** por outro membro da equipa.
- É estritamente **proibido aprovar o próprio PR**.
- O revisor tem a responsabilidade de inspecionar as alterações, verificar se o código compila corretamente, testar
as funcionalidades e validar rigorosamente todos os critérios de aceitação definidos na Issue.  
3.4. **Limpeza:** O ramo de origem (*head branch*) deve ser obrigatoriamente eliminado do GitHub e localmente após
o *merge* bem-sucedido.


## Estrutura do Projeto e Organização
Para garantir uma arquitetura limpa, coesa e de fácil manutenção, o repositório organiza-se de forma modular:
- **`src/main/java/`**: Contém todo o código fonte da aplicação, estruturado por pacotes lógicos (ex: domínio, serviços,
controladores/interface).
- **`src/test/java/`**: Contém os testes unitários e de integração correspondentes aos módulos desenvolvidos.
- **`docs/`**: Destinado à documentação formal do projeto, incluindo diagramas de arquitetura, relatórios e
especificações técnicas (como este ficheiro `CONTRIBUTING.md`).


## Convenções de Nomenclatura e Código
Para garantir legibilidade e consistência no código:

1. **Classes e Interfaces:** `PascalCase` e substantivos no singular (ex: `StudentProfile`).
2. **Métodos e Variáveis:** `camelCase` (ex: `calculateGrade()`, `studentId`).
3. **Constantes:** `UPPER_SNAKE_CASE` (ex: `MAX_GROUP_SIZE`).
4. **Métodos Acessores:** Usar `getSomething()`, `setSomething()` e `isSomething()` para booleanos.
5. **Idioma do Código:** Código em **Inglês**, comentários e mensagens de commit em **Inglês ou Português**.


## Definition of Done (DoD) - Sprint 1
Uma Issue/tarefa só é considerada concluída (*Done*) quando cumpre os seguintes requisitos:

1. **Rastreabilidade:** Criada a partir de uma Issue, atribuída corretamente e desenvolvida num ramo único e temporário.
2. **Qualidade de Código:** Cumpre as convenções de nomenclatura, a estrutura de pacotes definida e compila sem erros
localmente.
3. **Revisão:** Tem pelo menos uma aprovação independente por outro colega no Pull Request, sem recurso a auto-aprovação.
4. **Integração:** Efetuado o *merge* no `main`, e o ramo temporário eliminado no GitHub e localmente.