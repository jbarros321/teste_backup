#!/bin/bash
# Aula de Contrabaixo — macOS
# Dê dois cliques neste arquivo no Finder. Para parar, aperte Ctrl+C nesta janela.

cd "$(dirname "$0")" || exit 1
PORTA=8765

echo ""
echo "  🎸 Aula de Contrabaixo — preparando..."
echo ""

# Se a porta já estiver ocupada, provavelmente a aula já está no ar.
if lsof -nP -iTCP:$PORTA -sTCP:LISTEN >/dev/null 2>&1; then
  echo "  A porta $PORTA já está em uso — abrindo o navegador na aula que já está rodando."
  open "http://localhost:$PORTA"
  exit 0
fi

# Procura o Node, inclusive nos lugares onde o Homebrew instala (o Finder não
# carrega o seu .zshrc, então o PATH aqui é curtinho).
export PATH="/opt/homebrew/bin:/usr/local/bin:$HOME/.nvm/versions/node/current/bin:$PATH"
if [ -s "$HOME/.nvm/nvm.sh" ]; then . "$HOME/.nvm/nvm.sh" >/dev/null 2>&1; fi

abrir_depois() { ( sleep 1; open "http://localhost:$PORTA" ) & }

if command -v node >/dev/null 2>&1; then
  abrir_depois
  node servidor.js
  exit 0
fi

# Plano B: o Python 3 já vem no macOS (via Ferramentas de Linha de Comando).
# O servidor.py faz tudo o que o servidor.js faz, busca de partituras inclusive.
if command -v python3 >/dev/null 2>&1; then
  echo "  (usando Python, já que o Node não está instalado — funciona igual)"
  abrir_depois
  python3 servidor.py
  exit 0
fi

echo ""
echo "  Não encontrei Node nem Python3 neste Mac."
echo ""
echo "  O jeito mais fácil de instalar o Node:"
echo "    1) instale o Homebrew:  /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
echo "    2) depois:              brew install node"
echo ""
echo "  Aperte Enter para fechar."
read -r _
