# 🌐 Acesso Local (Localhost)

Guia de acesso local dos projetos deste repositório que possuem interface executável em máquina local.

---

## 1. fluxo-pro (React + Vite)

Dashboard frontend em **React 19 + Vite**.

| Item | Valor |
|---|---|
| Pasta | `fluxo-pro/` |
| URL local | **http://localhost:5173** |
| Comando | `cd fluxo-pro && npm install && npm run dev` |
| Build de produção | `npm run build` → `npm run preview` (http://localhost:4173) |

---

## 2. UIFlux (React + TypeScript + Vite + Supabase)

Aplicação com autenticação e multi-empresa via **Supabase**.

| Item | Valor |
|---|---|
| Pasta | `UIFlux/` |
| URL local | **http://localhost:5173** |
| Comando | `cd UIFlux && npm install && npm run dev` |
| Pré-requisitos | `.env` com `VITE_SUPABASE_URL` e `VITE_SUPABASE_ANON_KEY` (copiar de `.env.example`) |
| Banco | Criar projeto no Supabase e rodar `supabase/schema.sql` (ver `SETUP.md`) |

> ⚠️ Se `fluxo-pro` e `UIFlux` rodarem ao mesmo tempo, o Vite assume automaticamente a próxima porta livre (ex.: 5174).

---

## 3. profit_rtd_server (Integrador Profit Pro DDE)

Minisservidor Python que expõe cotações (WIN/WDO) via **WebSocket**.

| Item | Valor |
|---|---|
| Pasta | `profit_rtd_server/` |
| URL local | **ws://127.0.0.1:8080/ws** |
| Comando | `pip install -r requirements.txt && python server.py` |
| Pré-requisitos | Windows, Profit Pro aberto/logado, Python 3.9+ |

> O frontend React consome `ws://127.0.0.1:8080/ws`. Se estiver em outra máquina, usar o IP de rede da máquina Windows.

---

## 4. Analytics (HTML estático)

Dashboards HTML puros (sem build).

| Item | Valor |
|---|---|
| Pasta | `Analytics/` |
| Arquivos | `index.html` (Cockpit de Vendas), `Lannotas.html`, `index - cópia.html` |
| Acesso | Abrir direto no navegador OU servir via servidor estático |

Exemplo com servidor estático:

```bash
cd Analytics && python3 -m http.server 8000
```

Acesso: **http://localhost:8000/index.html**

---

## 5. trading_platform.jsx (simulador de trading)

Arquivo único de componente React (simulação de mercado, sem build próprio). Rode em qualquer setup React/Vite apontando para o arquivo `trading_platform.jsx`, ou abra em um ambiente de preview como o Vite do `fluxo-pro`.

---

## Resumo rápido

| Projeto | URL |
|---|---|
| fluxo-pro | http://localhost:5173 |
| UIFlux | http://localhost:5173 (ou 5174) |
| profit_rtd_server | ws://127.0.0.1:8080/ws |
| Analytics | http://localhost:8000/index.html |

> O restante do repositório é composto por personalizações **Sankhya** (Java/EJB/XML) que rodam dentro do próprio servidor Sankhya, sem acesso via localhost.
