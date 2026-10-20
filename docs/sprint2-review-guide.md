# Relatório de Preparação e Validação — Sprint 2 Review (Issue #52)

**Objetivo**: Garantir a execução sem falhas dos 4 cenários principais da Sprint 2 Review.


## Estado dos Critérios de Aceitação

- [x] **Cenário A**: Estudante com histórico preparado e perfil explicado.
- [x] **Cenário B**: Estudante novo, faz o quiz e vê o perfil inicial; depois regista notas e o peso do quiz desce.
- [x] **Cenário C**: Demonstração da explicação do perfil (US16).
- [x] **Cenário D**: `./mvnw clean test` e `npm test` a correr num PC limpo.
- [x] **Ensaio feito com dados alterados na hora**: Simulação de inserção/alteração de dados em tempo real testada com sucesso.


## Evidências de Validação por Cenário

### **Cenário A — Visualização e Evolução da Mascote**
* **Estudante sem dados**: Seleção da estudante `12345 · bruna`. O sistema exibiu corretamente a mascote no estado inicial ("Pix ainda é bebé"), aviso de quiz pendente e competências marcadas como "Sem dados".
* **Estudante com histórico**: Seleção do estudante `20260001` com notas registadas. A mascote evoluiu para **"Pix pontual · nível 4"** e o perfil refletiu a ponderação combinada (83% quiz / 17% histórico).


### **Cenário B — Ciclo de Vida do Estudante Novo (Joao Carlos)**
1. **Criação**: Registado o novo estudante `2445673 · Joao Carlos` na aba *Estudantes*.
2. **Quiz Inicial**: O estudante realizou o quiz com sucesso. O perfil inicial foi gerado a **100% com dados do Quiz**, evoluindo a mascote para **"Pix team player · nível 5"**.
3. **Registo de Notas**: Após a inscrição e aprovação na UC de `Programação Orientada a Objetos` (nota 16), o peso do quiz reduziu automaticamente para **83%** e a origem das competências afetadas transitou para **"Misto"**.


### **Cenário C — Explicação Detalhada do Perfil (US16)**
* Ao expandir os cards de competências (ex.: *Programação* e *Trab. em grupo*), o sistema apresenta o detalhe do cálculo:
    * Ponderação entre Quiz (83%) e Notas (17%).
    * Tabela explicativa por UC com a **Melhor Nota**, conversão para **Nível (1-5)**, **Peso** e **Contribuição**.


### **Cenário D — Suíte de Testes Automáticos**

* **Frontend (Vitest)**:
    * **Status**: `5 passed (5)`
    * **Testes**: `24 passed (24)`
    * **Comando**: `npm test`
  
* **Backend (Spring Boot / JUnit)**:
    * **Status**: `BUILD SUCCESS`
    * **Testes**: `57 passed (57)`
    * **Comando**: `./mvnw clean test`


### **Ensaio com Dados Alterados na Hora (Simulação)**
* **Simulação Prática**: Criou-se um estudante do zero durante o ensaio, efetuou-se a submissão do quiz inicial e adicionou-se uma nova nota em tempo real.
* **Comportamento Verificado**: O sistema reavaliou instantaneamente o perfil, recalculou as ponderações de 100% (Quiz) para 83% (Misto) e atualizou os acessórios/nível da mascote Pix no ecrã sem necessidade de recarregar a página.