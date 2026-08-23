#!/bin/bash
LOG="$HOME/Downloads/personalizacoes-main/Suncc/push_log.txt"
exec > >(tee "$LOG") 2>&1

echo "===== INICIO $(date) ====="
cd "$HOME/Downloads/personalizacoes-main" || exit 1

echo "--- incluindo correcao do LFS no commit ---"
git add .gitattributes .gitignore
git commit --amend --no-edit
echo "exit do amend: $?"

echo ""
echo "--- verificacao: sobrou algum mp4/ponteiro LFS? ---"
echo "mp4 rastreados: $(git ls-tree -r HEAD --name-only | grep -ci '\.mp4$')  (tem que ser 0)"

echo ""
echo "--- ENVIANDO ---"
git push origin main 2>&1 | grep -v "^Writing objects\|^Counting objects\|^Compressing objects\|Resolving deltas"
echo "exit do push: ${PIPESTATUS[0]}"

echo ""
echo "--- estado final ---"
git status -sb | head -1
echo "===== FIM $(date) ====="
