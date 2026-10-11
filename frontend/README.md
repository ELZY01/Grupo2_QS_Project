# StudyMatch — Frontend

Aplicação React (Vite) do StudyMatch. No Sprint 1 tem um ecrã de estado que chama o backend (`GET /api/health`) para provar que a stack funciona ponta a ponta.

## Requisitos

- Node.js 20 ou superior (inclui o npm)
- Backend a correr em `http://localhost:8080` (ver `backend/`)

## Arrancar

```bash
cd frontend
npm install
npm run dev
```

Abrir http://localhost:5173. Com o backend ligado, o ecrã mostra o estado **UP**. Sem backend, mostra uma mensagem de erro.

## Configuração

O URL do backend vem da variável `VITE_API_URL` (por omissão `http://localhost:8080`). Para mudar, copia `.env.example` para `.env` e altera o valor. O `.env` não é versionado.

## Scripts

| Comando | O que faz |
|---|---|
| `npm run dev` | Servidor de desenvolvimento (porta 5173) |
| `npm run build` | Build de produção em `dist/` |
| `npm test` | Testes (Vitest + React Testing Library) |
| `npm run lint` | ESLint |

## Estrutura

```
frontend/
├── src/
│   ├── api/            # chamadas HTTP ao backend
│   ├── components/     # componentes React (+ testes *.test.jsx)
│   ├── test/           # configuração dos testes
│   ├── App.jsx
│   └── main.jsx
├── .env.example
└── vite.config.js
```

Regra do ADR-001: o frontend nunca acede diretamente à base de dados. Todo o acesso a dados passa pela API do backend.
