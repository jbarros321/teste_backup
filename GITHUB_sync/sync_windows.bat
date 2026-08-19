@echo off
REM =========================================================================
REM  Sincronizador automatico - Windows <-> Mac via GitHub
REM  Use este script no Windows para sincronizar o projeto.
REM =========================================================================
setlocal EnableDelayedExpansion

REM ----------------------------- CONFIGURACAO ------------------------------
set "PASTA_PROJETO=E:\personalizacoes-main\personalizacoes-main"
REM --------------------------------------------------------------------------

cd /d "%PASTA_PROJETO%" 2>nul
if errorlevel 1 (
  echo !! Pasta nao encontrada: %PASTA_PROJETO%
  pause
  exit /b 1
)

echo ==^> Verificando se Git esta instalado...
where git >nul 2>nul
if errorlevel 1 (
  echo !! Git nao instalado. Instale em https://git-scm.com
  pause
  exit /b 1
)

REM 1) Puxa as mudancas mais recentes do GitHub
echo ==^> Puxando alteracoes do GitHub (pull)...
git pull --no-edit
if errorlevel 1 echo !! Aviso: nao foi possivel fazer pull (possivel conflito).

REM 2) Envia as mudancas locais para o GitHub
echo ==^> Enviando alteracoes locais (add/commit/push)...
git add -A
git diff --cached --quiet
if errorlevel 1 (
  git commit -m "Sincronizacao automatica - %date% %time%"
  git push
  echo Alteracoes enviadas com sucesso!
) else (
  echo Nada novo para enviar.
)

echo ==^> Sincronizacao concluida.
echo.
pause
