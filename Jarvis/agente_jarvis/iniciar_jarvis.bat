@echo off
:: Jarvis Agent Startup Script
chcp 65001 > nul
cls

echo ======================================================
echo           INICIANDO OLÁ BI AGENT
echo ======================================================
echo.

:: Detecta se o Ollama esta rodando
tasklist /FI "IMAGENAME eq ollama.exe" 2>NUL | find /I /N "ollama.exe">NUL
if "%ERRORLEVEL%"=="0" goto :PYTHON_START

echo [INFO] Iniciando Ollama...

if exist "%LOCALAPPDATA%\Programs\Ollama\ollama app.exe" (
    start "" "%LOCALAPPDATA%\Programs\Ollama\ollama app.exe"
    goto :WAIT_OLLAMA
)

if exist "%LOCALAPPDATA%\Programs\Ollama\ollama.exe" (
    start /min "" "%LOCALAPPDATA%\Programs\Ollama\ollama.exe" serve
    goto :WAIT_OLLAMA
)

start /min "" ollama serve

:WAIT_OLLAMA
echo [INFO] Aguardando nucleo de IA carregar (10s)...
timeout /t 10 /nobreak > nul

:PYTHON_START
:: Procura o Python 3.12 ou fallback
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

echo [OK] Iniciando sistemas com: %PY_CMD%
echo.

%PY_CMD% agente.py

echo.
echo [Bi encerrado]
pause
