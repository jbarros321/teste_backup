# Cifra Ministério

PWA de cifras para ministérios de louvor: letra com acordes alinhados, transposição,
metrônomo, rolagem automática, modo palco, repertórios e uso offline.

## Rodando

```bash
npm install
npm run dev      # http://localhost:5173
```

## Scripts

| Comando | O que faz |
|---|---|
| `npm run dev` | Servidor de desenvolvimento |
| `npm run build` | Typecheck + build de produção |
| `npm test` | Testes unitários (Vitest) |
| `npm run test:watch` | Testes em modo watch |
| `npm run test:coverage` | Cobertura do núcleo em `src/lib` |
| `npm run lint` | oxlint |

## Arquitetura

```
src/
  app/         router, providers, layouts
  components/  UI compartilhada
  features/    módulos de produto (songs, metronome, autoscroll, stage, …)
  lib/
    music/     motor de acordes, tonalidades e transposição — PURO, sem React
    chordpro/  parser e serializador ChordPro
    import/    detecção de formato e importação de texto
    audio/     engine do metrônomo (Tone.js)
    db/        cache offline (IndexedDB)
  services/    única porta de entrada para o Supabase
  pages/       telas roteadas
```

Regras que o código segue:

- `lib/music` e `lib/chordpro` são puros — sem React, sem rede, sem DOM — e cobertos por testes.
- Componentes nunca chamam o Supabase direto; só através de `services/`.
- O áudio nunca usa `setInterval` como relógio musical: o agendamento é no clock do Web Audio.
- A música é guardada como estrutura (ChordPro + AST), nunca como HTML renderizado.

## Supabase

Sem `.env.local`, o app roda em **modo demonstração** com o acervo de exemplo.
Para ligar o backend, siga o `SETUP.md`.

## Política de conteúdo

O sistema **não** raspa nem copia letra ou cifra de terceiros. Conteúdo entra por
três caminhos, todos legítimos:

1. **Cadastro e importação por texto colado** — o usuário traz o que ele já tem.
2. **Metadados de bases abertas** — MusicBrainz (CC0): título, artista, álbum,
   ano e duração. Metadado é fato, não obra protegida.
3. **Acervo de domínio público** — hinos cujo texto caiu em domínio público,
   com as cifras escritas por nós.

BPM e tom não vêm de serviço externo: o tom sai da nossa análise dos acordes
(`lib/music/key.ts`) e o BPM, do tap tempo.

## Status

Plano completo em `~/.claude/plans/scalable-munching-shore.md`.

- [x] **M0** Scaffold: Vite + React + TS, Tailwind v4, rotas, tema claro/escuro/palco, Vitest
- [x] **M1** Motor musical: acordes, tonalidades, transposição e ChordPro (95 testes)
- [x] **M2** SongViewer: acordes sobre a letra, transposição e tamanho de fonte
- [x] **M3** Supabase: schema, RLS multi-tenant, auth e camada de serviços
- [x] **M4** Dashboard, busca, CRUD de músicas, editor ChordPro, categorias e artistas
- [x] **M4.5** Aquisição de músicas: importação de texto colado, metadados MusicBrainz, acervo de domínio público
- [x] **M5** Metrônomo: Tone.js, compasso, subdivisão, acento, tap tempo, atalhos
- [x] **M6** Auto scroll, modo palco e atalhos de teclado
- [ ] M7 Repertórios, favoritos e histórico
- [ ] M8 Importação (TXT, ChordPro, texto colado)
- [ ] M9 PWA e offline
- [ ] M10 E2E e deploy
