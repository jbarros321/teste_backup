@echo off
REM =========================================================================
REM  Sincronizador Windows <-> Mac via GitHub
REM  Log em: GITHUB_sync\sync_log.txt
REM =========================================================================
setlocal EnableDelayedExpansion

set "PASTA_PROJETO=E:\personalizacoes-main\personalizacoes-main"
set "LOG=%PASTA_PROJETO%\GITHUB_sync\sync_log.txt"

cd /d "%PASTA_PROJETO%" 2>nul
if errorlevel 1 (
  echo !! Pasta nao encontrada: %PASTA_PROJETO%
  goto :fim
)

where git >nul 2>nul
if errorlevel 1 (
  echo !! Git nao instalado. Instale em https://git-scm.com
  goto :fim
)

echo ===== %date% %time% ===== >> "%LOG%"

REM --- garante identidade e opcoes (sem isso o commit falha) ---
for /f "delims=" %%i in ('git config user.name') do set "GU=%%i"
if "!GU!"=="" git config user.name "jbarros321"
for /f "delims=" %%i in ('git config user.email') do set "GE=%%i"
if "!GE!"=="" git config user.email "raphael.carvalho@neuonsolucoes.com"
git config core.fileMode false
git config core.autocrlf true
git config pull.rebase false

REM --- 1) COMMITA O LOCAL ANTES DE PUXAR (ordem correta) ---
echo ==^> Salvando alteracoes locais...
git add -A
git diff --cached --quiet
if errorlevel 1 (
  git commit -m "Sync Windows - %date% %time%" >> "%LOG%" 2>&1
  echo Commit local criado.
) else (
  echo Nada novo local.
)

REM --- 2) PUXA (para de verdade se falhar) ---
echo ==^> Puxando do GitHub...
git pull origin main --no-rebase --no-edit >> "%LOG%" 2>&1
if errorlevel 1 (
  echo.
  echo !! FALHOU O PULL. Motivo real:
  git pull origin main --no-rebase --no-edit
  echo.
  echo !! Arquivos em conflito:
  git diff --name-only --diff-filter=U
  echo.
  echo !! NAO vou enviar nada enquanto isso nao for resolvido.
  echo !! FALHA NO PULL >> "%LOG%"
  goto :fim
)
echo Pull OK.

REM --- 3) ENVIA ---
echo ==^> Enviando para o GitHub...
git push origin main >> "%LOG%" 2>&1
if errorlevel 1 (
  echo !! FALHA NO PUSH. Motivo real:
  git push origin main
  echo !! FALHA NO PUSH >> "%LOG%"
  goto :fim
)
echo Push OK. Sincronizado.
echo Push OK >> "%LOG%"

:fim
echo.
REM so pausa se rodado a mao (nao pausa no Agendador de Tarefas)
echo %cmdcmdline% | find /i "%~0" >nul
if not errorlevel 1 pause
