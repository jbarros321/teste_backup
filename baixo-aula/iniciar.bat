@echo off
title Aula de Contrabaixo
cd /d "%~dp0"

where node >nul 2>nul
if %errorlevel%==0 (
  start "" http://localhost:8765
  node servidor.js
  goto :eof
)

where python >nul 2>nul
if %errorlevel%==0 (
  echo.
  echo  Aviso: sem o Node, a busca de partituras ^(modo 7^) nao funciona.
  echo  O resto da aula funciona normalmente.
  echo.
  start "" http://localhost:8765
  python -m http.server 8765
  goto :eof
)

echo.
echo  Nao encontrei Node nem Python nesta maquina.
echo  Instale um dos dois e rode este arquivo de novo.
echo.
pause
