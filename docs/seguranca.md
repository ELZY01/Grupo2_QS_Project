# Avaliação de segurança

> Sprint 3 · Issue #82 · Grupo 2

Avaliação básica de segurança do StudyMatch: scan automático com o OWASP ZAP e testes manuais.
Todos os testes foram feitos **só em ambiente local** (backend em `localhost:8080` com H2 em memória, frontend em `localhost:5173`), com dados de teste.

## 1. Scan automático (OWASP ZAP)

> ⏳ **Por fazer.** O ZAP não estava instalado na máquina usada. Para correr o *baseline scan* com Docker, com a app a correr em local:

```bash
docker run --rm -t zaproxy/zap-stable zap-baseline.py \
  -t http://host.docker.internal:5173 -r zap-report.html
```

Depois, preencher a tabela abaixo com pelo menos 1 alerta analisado.

| Alerta | Risco ZAP | Componente | É real? | Precisa de correção? |
|---|---|---|---|---|
| _(a preencher)_ | | | | |

## 2. Testes manuais

Formato: **Test → Expected → Observed → Conclusion**.

### T1 — Palavra-passe do admin no frontend

- **Test:** procurar a palavra-passe do admin no código e no *bundle* gerado (`npm run build`, `dist/assets/*.js`); chamar `GET /api/students` sem sessão.
- **Expected:** nenhuma credencial no frontend; a API recusa pedidos sem sessão.
- **Observed (antes):** `session.js` tinha `ADMIN_PASSWORD = 'studymatch'` e o login do admin era validado só no browser; o backend aceitava qualquer pedido sem autenticação.
- **Observed (depois):** 0 ficheiros do *bundle* com a palavra-passe; `GET /api/students` sem token → **401** `{"message":"Tens de iniciar sessão."}`.
- **Conclusion:** vulnerabilidade **real e corrigida** (Spring Security, login no backend com BCrypt e token).

### T2 — Alterar IDs no URL (IDOR)

- **Test:** com a sessão do aluno A, pedir `/api/students/{id de B}/profile` e `/enrollments`; com a sessão de um docente, pedir o perfil de um aluno que não está numa UC sua.
- **Expected:** 403.
- **Observed:** próprio perfil → 200; perfil e histórico de B → **403**; docente com aluno de fora → **403**; aluno em `/api/admin/**` ou `/api/teacher/**` → **403**.
- **Conclusion:** controlo de acesso correto (RN05, RN16, R1–R7). Coberto por `SecurityAccessTest` e `TeacherAccessTest`.

### T3 — Texto malicioso (XSS guardado)

- **Test:** registar um aluno com o nome `<img src=x onerror=alert(1)>`.
- **Expected:** o texto é mostrado como texto, sem executar código.
- **Observed:** o backend guarda o nome tal como foi escrito; o frontend não usa `dangerouslySetInnerHTML` nem `innerHTML` (0 ocorrências) e o React escapa o texto.
- **Conclusion:** **não explorável** na versão atual. Recomendação: validar o formato do nome no backend (só letras, espaços e hífens).

### T4 — Injeção de SQL no login

- **Test:** `POST /api/auth/login` com `username = admin' OR '1'='1` e `password = x' OR '1'='1`.
- **Expected:** 401.
- **Observed:** **401**. As consultas usam Spring Data JPA com parâmetros, sem SQL concatenado.
- **Conclusion:** não vulnerável.

### T5 — Força bruta no login

- **Test:** 30 tentativas seguidas de login do `admin` com palavras-passe erradas.
- **Expected:** bloqueio depois de algumas falhas.
- **Observed (antes):** 30/30 tentativas avaliadas, sem qualquer bloqueio.
- **Observed (depois):** 5 × **401**, a seguir **429** "Demasiadas tentativas falhadas…", mesmo com a palavra-passe certa; outras contas continuam a entrar.
- **Conclusion:** vulnerabilidade **real e corrigida** (`LoginThrottle`: 5 falhas em 15 minutos, configurável).

### T6 — Cabeçalhos HTTP

- **Test:** `curl -D - http://localhost:8080/api/health`.
- **Observed:** `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Cache-Control: no-store` (vêm do Spring Security).
- **Conclusion:** OK para a API. Falta `Content-Security-Policy` no frontend, a configurar no servidor onde o frontend for publicado.

## 3. Riscos que ficam

| Risco | Porque fica | Mitigação |
|---|---|---|
| Token guardado em `localStorage` | Se houver XSS, o token pode ser lido | Não há vetores de XSS conhecidos (T3); passar a cookie `HttpOnly` numa versão futura |
| Tokens e bloqueios em memória | Reiniciar o backend termina as sessões; com várias instâncias não seriam partilhados | Aceitável para o projeto; guardar na base de dados se for preciso escalar |
| Contas de demonstração com palavras-passe conhecidas | Facilitam os testes | Definir `ADMIN_PASSWORD` e `DOCENTE_PASSWORD` em produção |
