# ============================================================
#   JARVIS AGENT — acoes.py (macOS VERSION)
# ============================================================

import subprocess
import os
import webbrowser
import time
import pyautogui
import re
import psutil
from config import COMANDOS

# Silencia o pyautogui
pyautogui.FAILSAFE = True
pyautogui.PAUSE = 0.3

# ============================================================
#   AUXILIARES
# ============================================================

def abrir_no_navegador(url: str):
    """Tenta abrir a URL no Chrome, caso contrário usa o padrão do sistema."""
    chrome_info = COMANDOS.get("chrome")
    # No Mac, verificamos se o app existe na pasta de aplicativos
    if os.path.exists("/Applications/Google Chrome.app"):
        try:
            subprocess.Popen(["open", "-a", "Google Chrome", url], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            return
        except:
            pass
    
    # Fallback para o padrão do sistema (Safari)
    webbrowser.open(url)

# ============================================================
#   FUNÇÕES PRINCIPAIS
# ============================================================

def abrir_app(chave: str) -> str:
    """Abre um app usando o comando 'open -a' do macOS."""
    app = COMANDOS.get(chave)
    if not app:
        return f"App '{chave}' não encontrado no config."
    try:
        # No Mac usamos 'open -a "Nome do App"'
        subprocess.Popen(["open", "-a", app["caminho"]], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        return f"Abrindo {chave}, senhor."
    except Exception as e:
        return f"Falha ao abrir {chave}: {e}"


def abrir_youtube(busca: str = "") -> str:
    """Abre o YouTube com uma busca."""
    if busca:
        url = f"https://www.youtube.com/results?search_query={busca.replace(' ', '+')}"
        abrir_no_navegador(url)
        return f"Pesquisando '{busca}' no YouTube, senhor."
    else:
        abrir_no_navegador("https://www.youtube.com")
        return "Abrindo YouTube, senhor."


def abrir_site(url: str) -> str:
    """Abre qualquer URL no navegador."""
    if not url.startswith("http"):
        url = "https://" + url
    abrir_no_navegador(url)
    return f"Abrindo {url}, senhor."


def tocar_spotify(musica: str = "") -> str:
    """Abre o Spotify e tenta tocar a música diretamente via AppleScript."""
    # Garante que o Spotify abre
    abrir_app("spotify")
    
    if musica:
        # No Mac, podemos usar AppleScript para controlar o Spotify de forma muito mais limpa
        script = f'''
        tell application "Spotify"
            play track "spotify:search:{musica}"
        end tell
        '''
        try:
            subprocess.run(["osascript", "-e", script])
            return f"Tocando '{musica}' no Spotify via AppleScript, senhor."
        except:
            # Fallback para automação de interface se o AppleScript falhar
            time.sleep(5)
            pyautogui.hotkey('command', 'l')
            pyautogui.typewrite(musica)
            pyautogui.press('enter')
            return f"Procurando '{musica}' no Spotify, senhor."
            
    return "Abrindo Spotify, senhor."


def criar_pasta(nome: str, local: str = None) -> str:
    """Cria uma pasta no Desktop ou local especificado."""
    base = local or os.path.expanduser("~/Desktop")
    caminho = os.path.join(base, nome)
    try:
        os.makedirs(caminho, exist_ok=True)
        return f"Pasta '{nome}' criada na sua Área de Trabalho, senhor."
    except Exception as e:
        return f"Falha ao criar pasta: {e}"


def criar_arquivo(nome: str, conteudo: str = "", local: str = None) -> str:
    """Cria um arquivo de texto."""
    base = local or os.path.expanduser("~/Desktop")
    caminho = os.path.join(base, nome)
    try:
        with open(caminho, "w", encoding="utf-8") as f:
            f.write(conteudo)
        return f"Arquivo '{nome}' criado, senhor."
    except Exception as e:
        return f"Falha ao criar arquivo: {e}"


def executar_terminal(cmd: str) -> str:
    """Executa um comando no terminal."""
    try:
        result = subprocess.run(
            cmd, shell=True, capture_output=True, text=True, timeout=10
        )
        return result.stdout.strip() or result.stderr.strip() or "Executado."
    except Exception as e:
        return f"Erro: {e}"


def volume_sistema(nivel: int) -> str:
    """Ajusta o volume via osascript (0-100)."""
    try:
        # O Mac usa escala de 0 a 7 (ou 0 a 100 dependendo do comando)
        # 'set volume output volume X' usa 0-100
        subprocess.run(["osascript", "-e", f"set volume output volume {nivel}"])
        return f"Volume ajustado para {nivel}%, senhor."
    except:
        return "Não consegui ajustar o volume, senhor."


def capturar_screenshot(nome: str = "screenshot") -> str:
    """Tira um print da tela usando 'screencapture' do macOS."""
    caminho = os.path.expanduser(f"~/Desktop/{nome}.png")
    try:
        subprocess.run(["screencapture", caminho])
        return f"Screenshot salva no Desktop como '{nome}.png', senhor."
    except:
        pyautogui.screenshot(caminho)
        return f"Screenshot capturada, senhor."


def fechar_app(nome: str) -> str:
    """Fecha um aplicativo usando 'pkill' ou AppleScript."""
    n = nome.lower().strip()
    
    # Tenta via AppleScript (mais limpo para apps com interface)
    script = f'tell application "{nome}" to quit'
    try:
        subprocess.run(["osascript", "-e", script], timeout=5)
        return f"Encerrando {nome}, senhor."
    except:
        # Fallback para pkill
        subprocess.run(["pkill", "-i", nome])
        return f"Comando de encerramento enviado para {nome}, senhor."


def informacao_sistema(tipo: str = "hora") -> str:
    """Retorna informações do sistema."""
    from datetime import datetime
    agora = datetime.now()
    
    if tipo == "hora":
        return f"Agora são {agora.strftime('%H:%M')}, senhor."
    elif tipo == "data":
        dias_semana = ["Segunda-feira", "Terça-feira", "Quarta-feira", 
                       "Quinta-feira", "Sexta-feira", "Sábado", "Domingo"]
        dia_semana = dias_semana[agora.weekday()]
        return f"Hoje é {dia_semana}, {agora.strftime('%d/%m/%Y')}, senhor."
    return f"Agora são {agora.strftime('%H:%M')} de {agora.strftime('%d/%m/%Y')}, senhor."


def rotina_trabalho() -> str:
    """Abre os apps de trabalho no macOS."""
    tocar_spotify("Lavender Lo-fi")
    time.sleep(2)
    abrir_app("obsidian")
    time.sleep(1)
    abrir_app("vscode")
    return "Rotina de trabalho iniciada no seu Mac, senhor."


TOOLS_CONFIG = {
    "abrir_app": "Abre um app no Mac. Ex: abrir_app(chave='spotify')",
    "abrir_site": "Abre uma URL. Ex: abrir_site(url='google.com')",
    "pesquisar_google": "Pesquisa no Google. Ex: pesquisar_google(busca='termo')",
    "abrir_youtube": "Pesquisa no YouTube. Ex: abrir_youtube(busca='lofi')",
    "tocar_spotify": "Toca música no Spotify via AppleScript.",
    "criar_pasta": "Cria pasta no Desktop Mac.",
    "criar_arquivo": "Cria arquivo no Desktop Mac.",
    "capturar_screenshot": "Tira print da tela do Mac.",
    "fechar_app": "Encerra um app no Mac.",
    "volume_sistema": "Ajusta volume do Mac (0-100).",
    "executar_terminal": "Executa comando no Terminal Mac.",
    "informacao_sistema": "Retorna hora ou data."
}
