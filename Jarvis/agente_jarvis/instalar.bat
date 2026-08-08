@echo off
:: Arquivo salvo sem BOM para evitar erros no CMD do Windows
chcp 65001 > nul
cls

echo ======================================================
echo           JARVIS AGENT - INSTALACAO (FORCANDO 3.12)
echo ======================================================
echo.

:: Tenta encontrar o 3.12 pela ordem de prioridade
set PY_CMD=none

python3.12 --version > nul 2>&1
if not errorlevel 1 (set PY_CMD=python3.12)

if "%PY_CMD%"=="none" (
    py -3.12 --version > nul 2>&1
    if not errorlevel 1 (set PY_CMD=py -3.12)
)

if "%PY_CMD%"=="none" (
    python3 --version > nul 2>&1
    if not errorlevel 1 (set PY_CMD=python3)
)

if "%PY_CMD%"=="none" (
    set PY_CMD=python
)

echo [OK] Usando o comando: %PY_CMD%
echo.

echo [1/3] Garantindo dependencias base (setuptools)...
%PY_CMD% -m pip install --upgrade pip --quiet
%PY_CMD% -m pip install setuptools wheel --quiet

echo [2/3] Instalando todas as bibliotecas do requirements.txt...
%PY_CMD% -m pip install -r requirements.txt --upgrade

echo [3/3] Verificando IA local (Ollama)...
ollama pull llama3.2

echo.
echo ======================================================
echo           INSTALACAO CONCLUIDA NO 3.12!
echo ======================================================
pause
