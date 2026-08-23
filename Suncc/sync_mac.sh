#!/bin/bash
# =============================================================================
#  Sincronizador Windows <-> Mac via GitHub
#  Roda manualmente ou automatico (via launchd). Log em Suncc/sync_log.txt
# =============================================================================
PASTA="$HOME/Downloads/personalizacoes-main"
LOG="$PASTA/Suncc/sync_log.txt"

# mantem o log com no maximo ~2000 linhas
[ -f "$LOG" ] && tail -2000 "$LOG" > "$LOG.tmp" 2>/dev/null && mv "$LOG.tmp" "$LOG"
exec >> "$LOG" 2>&1

echo ""
echo "===== $(date '+%Y-%m-%d %H:%M:%S') ====="

export PATH="/usr/local/bin:/opt/homebrew/bin:/usr/bin:/bin:/usr/sbin:/sbin"
cd "$PASTA" || { echo "ERRO: pasta nao encontrada"; exit 1; }

# so usa SSH sem prompt: se a chave nao autenticar, aborta em vez de travar
export GIT_SSH_COMMAND="ssh -o BatchMode=yes -o StrictHostKeyChecking=yes"
export GIT_TERMINAL_PROMPT=0

# garante identidade e opcoes
[ -z "$(git config user.name)"  ] && git config user.name  "jbarros321"
[ -z "$(git config user.email)" ] && git config user.email "raphael.carvalho@neuonsolucoes.com"
git config core.fileMode false
git config core.precomposeUnicode true
git config pull.rebase false

# 1) commita o que mudou localmente ANTES de puxar
git add -A
if git diff --cached --quiet; then
  echo "Nada novo local."
else
  git commit -q -m "Sync Mac - $(date '+%Y-%m-%d %H:%M:%S')" && echo "Commit local criado."
fi

# 2) puxa do GitHub
if git pull origin main --no-rebase --no-edit -q; then
  echo "Pull OK."
else
  echo "!! CONFLITO no pull. Arquivos:"
  git diff --name-only --diff-filter=U
  echo "!! Resolva manualmente. Sincronizacao interrompida."
  exit 1
fi

# 3) envia
if git push -q origin main; then
  echo "Push OK. Sincronizado."
else
  echo "!! Falha no push (ver acima)."
  exit 1
fi
