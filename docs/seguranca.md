# Avaliação de segurança

> Sprint 3 · Issue #82 · Grupo 2

Avaliação básica de segurança do StudyMatch: scan automático com o OWASP ZAP e testes manuais.
Todos os testes foram feitos **só em ambiente local** (backend em `localhost:8080` com H2 em memória ou PostgreSQL local, frontend em `localhost:5173`), com dados de teste.

## 1. Scan automático (OWASP ZAP)

*Baseline scan* (passivo: o ZAP navega pela app e analisa as respostas, sem enviar ataques) feito a 15/11/2026 com o ZAP 2.17.0 (`zaproxy/zap-stable`).

**Ambiente:** tudo local. O backend deste branch corria em `localhost:8080`, ligado a um PostgreSQL descartável em Docker com os scripts V1–V6, e não ao Supabase da equipa. O frontend foi compilado (`npm run build`) e servido como ficheiros estáticos em `localhost:4173`, como ficaria em produção. O servidor de desenvolvimento do Vite recusa pedidos vindos do container (403), por isso não serve para o scan.

```bash
docker run --rm -v "$PWD/docs/zap:/zap/wrk" -t zaproxy/zap-stable zap-baseline.py \
  -t http://host.docker.internal:4173 -r zap-frontend.html -J zap-frontend.json
docker run --rm -v "$PWD/docs/zap:/zap/wrk" -t zaproxy/zap-stable zap-baseline.py \
  -t http://host.docker.internal:8080/api/health -r zap-api.html -J zap-api.json
```

**Resultado:** 0 FAIL. Frontend: 9 avisos e 59 regras sem problemas. API: 2 avisos e 66 regras sem problemas. Relatórios completos em [`docs/zap/`](zap/).

| Alerta | Risco ZAP | Componente | É real? | Precisa de correção? |
|---|---|---|---|---|
| Content Security Policy (CSP) Header Not Set [10038] | Médio | Frontend (servidor dos ficheiros estáticos) | **Sim.** Sem CSP, o browser não limita de onde vêm scripts. Se houvesse um XSS, o código injetado corria sem restrições e podia ler o token do `localStorage` | Sim, no servidor onde o frontend for publicado: `Content-Security-Policy: default-src 'self'; connect-src 'self' <URL da API>; frame-ancestors 'none'`. Hoje o risco é baixo porque não há vetores de XSS conhecidos (T3) |
| Missing Anti-clickjacking Header [10020] | Médio | Frontend | **Sim.** A página pode ser aberta dentro de um `<iframe>` de outro site (*clickjacking*) | Sim, com `frame-ancestors 'none'` na CSP ou `X-Frame-Options: DENY`. A API já envia `DENY` (T6) |
| Sub Resource Integrity Attribute Missing [90003] | Médio | Frontend (`index.html`) | **Falso positivo.** O SRI serve para scripts carregados de outros domínios (CDN). Aqui só há scripts do próprio site (`/assets/index-*.js`), gerados pelo Vite | Não |
| X-Content-Type-Options Header Missing [10021] | Baixo | Frontend | Sim, mas de impacto baixo (o browser pode "adivinhar" o tipo de um ficheiro) | Sim, `nosniff` no servidor do frontend. A API já o envia |
| Server Leaks Version Information [10036] | Baixo | Servidor estático usado no scan | Não se aplica: vem do servidor de teste (Python `http.server`), não do código do projeto | Não. Confirmar no servidor final |
| Cross-Origin-Resource-Policy / Embedder / Opener-Policy [90004] | Baixo | Frontend e API (`/api/health`) | Sim, são cabeçalhos de isolamento em falta, de impacto baixo para esta app | Opcional |
| Permissions Policy Header Not Set [10063] | Baixo | Frontend | Sim, de impacto baixo (a app não usa câmara, microfone nem localização) | Opcional |
| Storable / Non-Storable Content, Modern Web Application [10049, 10109] | Informativo | Frontend e API | Só informação. A API responde com `Cache-Control: no-store`, o que é correto para dados pessoais | Não |

**Conclusão:** a API não tem alertas relevantes, porque o Spring Security já envia `nosniff`, `X-Frame-Options: DENY` e `no-store`. Os alertas reais são todos cabeçalhos em falta no servidor que publica o frontend. Corrigem-se na configuração desse servidor, não no código React.

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
| Frontend sem CSP nem anti-clickjacking (ZAP 10038, 10020) | Dependem do servidor onde o frontend for publicado | Configurar os cabeçalhos da secção 1 nesse servidor |
| Token guardado em `localStorage` | Se houver XSS, o token pode ser lido | Não há vetores de XSS conhecidos (T3); passar a cookie `HttpOnly` numa versão futura |
| Tokens e bloqueios em memória | Reiniciar o backend termina as sessões; com várias instâncias não seriam partilhados | Aceitável para o projeto; guardar na base de dados se for preciso escalar |
| Contas de demonstração com palavras-passe conhecidas | Facilitam os testes | Definir `ADMIN_PASSWORD` e `DOCENTE_PASSWORD` em produção |
