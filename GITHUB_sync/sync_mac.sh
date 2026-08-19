#!/bin/bash
# =============================================================================
#  Sincronizador automático - Windows <-> Mac via GitHub
#  Use este script no Mac para baixar/clonar e sincronizar o projeto.
# =============================================================================

# ----------------------------- CONFIGURACAO --------------------------------
REPO_URL="https://github.com/jbarros321/teste_backup.git"
# Tenta localizar a pasta em Downloads
if [ -d "$HOME/Downloads/personalizacoes-main" ]; then
  PASTA_DESTINO="$HOME/Downloads/personalizacoes-main"
else
  PASTA_DESTINO="$HOME/Downloads/Personalizacoes-main"
fi
# ---------------------------------------------------------------------------

set -e

echo "==> Verificando se o Git esta instalado..."
if ! command -v git >/dev/null 2>&1; then
  echo "Git nao encontrado. Instalando via Xcode Command Line Tools..."
  xcode-select --install
  echo "Apos instalar, rode este script novamente."
  exit 1
fi
echo "Git OK ($(git --version))"

# 1) Se a pasta ainda nao existe, clona o projeto pela primeira vez
if [ ! -d "$PASTA_DESTINO/.git" ]; then
  echo "==> Clonando o projeto pela primeira vez em: $PASTA_DESTINO"
  mkdir -p "$(dirname "$PASTA_DESTINO")"
  git clone "$REPO_URL" "$PASTA_DESTINO"
else
  echo "==> Projeto ja existe. Atualizando..."
fi

cd "$PASTA_DESTINO"

# 2) Puxa as mudancas mais recentes do GitHub
echo "==> puxando alteracoes do GitHub (pull)..."
git pull origin main --allow-unrelated-histories --no-rebase --no-edit || echo "!! Aviso: nao foi possivel fazer pull (talvez haja conflito)."

# 3) Envia as mudancas locais para o GitHub
echo "==> Enviando alteracoes locais (add/commit/push)..."
git add -A
if git diff --cached --quiet; then
  echo "Nada novo para enviar."
else
  git commit -m "Sincronizacao automatica - $(date '+%Y-%m-%d %H:%M:%S')"
  git push
  echo "Alteracoes enviadas com sucesso!"
fi

echo "==> Sincronizacao concluida. Pasta: $PASTA_DESTINO"
echo ""
read -p "Pressione [ENTER] para fechar..."
