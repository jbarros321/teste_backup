#!/bin/bash
# Abre o Tecladista PRO no navegador (servidor local para o microfone funcionar)
cd "$(dirname "$0")/app"
PORT=8765
if ! lsof -i :$PORT >/dev/null 2>&1; then
  nohup python3 -m http.server $PORT >/dev/null 2>&1 &
  sleep 1
fi
if [ -d "/Applications/Google Chrome.app" ]; then
  open -a "Google Chrome" "http://localhost:$PORT/"
else
  open "http://localhost:$PORT/"
fi
echo "Tecladista PRO aberto em http://localhost:$PORT  (pode fechar esta janela)"
