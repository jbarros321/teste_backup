# ============================================================
#   JARVIS AGENT — acoes.py
#   Motor de ações: abre apps, sites, cria pastas, etc.
# ============================================================

import subprocess
import os
import webbrowser
import time
import pyautogui
import re
import psutil
import requests
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
    if chrome_info and os.path.exists(chrome_info["caminho"]):
        try:
            # Abre diretamente via subprocess para garantir que use o Chrome
            subprocess.Popen([chrome_info["caminho"], url], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            return
        except:
            pass
    
    # Fallback para o padrão do sistema
    webbrowser.open(url)

# ============================================================
#   FUNÇÕES PRINCIPAIS
# ============================================================

def abrir_app(chave: str) -> str:
    """Abre um app pelo nome da chave no config.py"""
    app = COMANDOS.get(chave)
    if not app:
        return f"App '{chave}' não encontrado no config."
    try:
        # Redirecionamos stdout e stderr para DEVNULL para evitar poluir o console do Bi
        subprocess.Popen(app["caminho"], shell=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        return f"Abrindo {chave}, senhor."
    except Exception as e:
        return f"Falha ao abrir {chave}: {e}"


def abrir_app_direto(caminho: str) -> str:
    """Abre um executável por caminho direto."""
    try:
        subprocess.Popen(caminho, shell=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        return f"Executing: {caminho}"
    except Exception as e:
        return f"Error: {e}"


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
    """Abre o Spotify e tenta tocar a música diretamente."""
    import pygetwindow as gw
    
    # Primeiro garante que o Spotify abre
    res = abrir_app("spotify")
    if "não encontrado" in res.lower():
        return res

    if musica:
        print(f"⏳ Aguardando Spotify carregar para pesquisar '{musica}'...")
        time.sleep(7) # Um pouco mais de tempo para máquinas mais lentas
        
        # Tenta focar na janela do Spotify
        try:
            spot_windows = gw.getWindowsWithTitle('Spotify')
            if spot_windows:
                spot_window = spot_windows[0]
                if spot_window.isMinimized:
                    spot_window.restore()
                spot_window.activate()
                time.sleep(1)
            else:
                # Fallback: Clique no centro da tela se não achar a janela
                pyautogui.click(pyautogui.size().width // 2, pyautogui.size().height // 2)
                time.sleep(0.5)
        except:
            pass
        
        # Sequência de comandos dentro do Spotify
        pyautogui.hotkey('ctrl', 'l') # Focar busca
        time.sleep(1)
        pyautogui.hotkey('ctrl', 'a')
        pyautogui.press('backspace')
        pyautogui.typewrite(musica, interval=0.05)
        time.sleep(0.8)
        
        # Sequência exata do usuário: Enter, 2 TAB, 2 Enter
        pyautogui.press('enter')
        time.sleep(2.5)
        pyautogui.press('tab')
        time.sleep(0.3)
        pyautogui.press('tab')
        time.sleep(0.3)
        pyautogui.press('enter')
        time.sleep(0.2)
        pyautogui.press('enter')
        
        return f"Procurando e tentando tocar '{musica}' no Spotify com sua nova sequência, senhor."
    return "Abrindo Spotify, senhor."


def criar_pasta(nome: str, local: str = None) -> str:
    """Cria uma pasta no Desktop ou local especificado."""
    base = local or os.path.join(os.path.expanduser("~"), "Desktop")
    caminho = os.path.join(base, nome)
    try:
        os.makedirs(caminho, exist_ok=True)
        return f"Pasta '{nome}' criada na sua Área de Trabalho, senhor."
    except Exception as e:
        return f"Falha ao criar pasta: {e}"


def criar_arquivo(nome: str, conteudo: str = "", local: str = None) -> str:
    """Cria um arquivo de texto."""
    base = local or os.path.join(os.path.expanduser("~"), "Desktop")
    caminho = os.path.join(base, nome)
    try:
        with open(caminho, "w", encoding="utf-8") as f:
            f.write(conteudo)
        return f"Arquivo '{nome}' criado, senhor."
    except Exception as e:
        return f"Falha ao criar arquivo: {e}"


def executar_terminal(cmd: str) -> str:
    """Executa um comando no terminal e retorna o resultado."""
    try:
        result = subprocess.run(
            cmd, shell=True, capture_output=True, text=True, timeout=10
        )
        saida = result.stdout.strip() or result.stderr.strip()
        return saida or "Comando executado sem retorno."
    except subprocess.TimeoutExpired:
        return "O comando expirou."
    except Exception as e:
        return f"Error: {e}"


def volume_sistema(nivel: int) -> str:
    """Ajusta o volume do sistema (0-100)."""
    try:
        from ctypes import cast, POINTER
        from comtypes import CLSCTX_ALL
        from pycaw.pycaw import AudioUtilities, IAudioEndpointVolume
        devices = AudioUtilities.GetSpeakers()
        interface = devices.Activate(IAudioEndpointVolume._iid_, CLSCTX_ALL, None)
        volume = cast(interface, POINTER(IAudioEndpointVolume))
        nivel_norm = max(0.0, min(1.0, nivel / 100.0))
        volume.SetMasterVolumeLevelScalar(nivel_norm, None)
        return f"Volume ajustado para {nivel}%, senhor."
    except:
        # Fallback via PowerShell
        cmd = f'powershell -c "(New-Object -ComObject WScript.Shell).SendKeys([char]174)"'
        return "Tentativa de ajuste de volume realizada, senhor."


def capturar_screenshot(nome: str = "screenshot") -> str:
    """Tira um print da tela."""
    caminho = os.path.join(os.path.expanduser("~"), "Desktop", f"{nome}.png")
    pyautogui.screenshot(caminho)
    return f"Screenshot salva na Área de Trabalho como '{nome}.png', senhor."


def fechar_app(nome: str) -> str:
    """Fecha um aplicativo de forma robusta usando psutil e mapeamento do config."""
    n = nome.lower().strip()
    
    # 1. Tenta encontrar o nome do processo no config
    proc_nome = None
    for chave, info in COMANDOS.items():
        if n == chave or n in info.get("aliases", []):
            proc_nome = info.get("processo")
            break
    
    # Se não achou, tenta pelo nome direto
    alvo = proc_nome or n
    if not alvo.lower().endswith(".exe"):
        alvo += ".exe"

    encontrado = False
    try:
        # 2. Busca processos ativos
        for proc in psutil.process_iter(['name', 'pid']):
            try:
                if proc.info['name'].lower() == alvo.lower():
                    # Tenta fechar de forma amigável primeiro
                    p = psutil.Process(proc.info['pid'])
                    p.terminate()
                    encontrado = True
            except (psutil.NoSuchProcess, psutil.AccessDenied):
                continue
        
        if encontrado:
            return f"Encerrando todos os processos de {alvo}, senhor."
        else:
            # Fallback para taskkill se o psutil falhar em permissão
            subprocess.run(f'taskkill /F /IM {alvo} /T', shell=True, capture_output=True)
            return f"Comando de encerramento (forçado) enviado para {alvo}, senhor."

    except Exception as e:
        return f"Erro ao tentar fechar {nome}: {e}"


def comemorar_sexta() -> str:
    """Função especial para sextas-feiras: música Status do Jansen e animação de foguetes."""
    # 1. Toca a música no Spotify
    tocar_spotify("Status Jansen")
    
    # 2. Gera e abre a animação de foguetes premium
    html_path = os.path.join(os.getcwd(), "rockets.html")
    
    html_content = """
    <!DOCTYPE html>
    <html lang="pt-br">
    <head>
        <meta charset="UTF-8">
        <title>SEXTA-FEIRA!</title>
        <style>
            body { 
                margin: 0; 
                background: #050505; 
                overflow: hidden; 
                display: flex; 
                justify-content: center; 
                align-items: center; 
                height: 100vh;
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            }
            .title {
                color: #fff;
                font-size: 5rem;
                text-transform: uppercase;
                letter-spacing: 10px;
                z-index: 10;
                text-shadow: 0 0 20px #ff00ff, 0 0 40px #00ffff;
                animation: pulse 2s infinite;
            }
            @keyframes pulse {
                0%, 100% { transform: scale(1); opacity: 1; }
                50% { transform: scale(1.1); opacity: 0.8; }
            }
            .rocket {
                position: absolute;
                bottom: -100px;
                width: 20px;
                height: 50px;
                background: linear-gradient(to top, #ffcc00, #ff3300);
                border-radius: 50% 50% 0 0;
                animation: launch linear infinite;
            }
            @keyframes launch {
                0% { transform: translateY(0); opacity: 1; }
                100% { transform: translateY(-120vh); opacity: 0; }
            }
            .sparkle {
                position: absolute;
                width: 4px;
                height: 4px;
                background: white;
                border-radius: 50%;
                box-shadow: 0 0 10px white;
            }
        </style>
    </head>
    <body>
        <div class="title">Sextou, Senhor!</div>
        <script>
            function createRocket() {
                const rocket = document.createElement('div');
                rocket.className = 'rocket';
                rocket.style.left = Math.random() * 100 + 'vw';
                rocket.style.animationDuration = (Math.random() * 2 + 1) + 's';
                document.body.appendChild(rocket);
                setTimeout(() => rocket.remove(), 3000);
            }
            setInterval(createRocket, 200);
            
            // Criar estrelas de fundo
            for(let i=0; i<100; i++) {
                const s = document.createElement('div');
                s.className = 'sparkle';
                s.style.top = Math.random() * 100 + 'vh';
                s.style.left = Math.random() * 100 + 'vw';
                document.body.appendChild(s);
            }
        </script>
    </body>
    </html>
    """
    
    with open(html_path, "w", encoding="utf-8") as f:
        f.write(html_content)
    
    abrir_no_navegador(f"file:///{html_path}")
    
    return "Comemoração de sexta-feira iniciada! Aproveite, senhor!"


def pesquisar_google(busca: str) -> str:
    """Pesquisa no Google."""
    url = f"https://www.google.com/search?q={busca.replace(' ', '+')}"
    abrir_no_navegador(url)
    return f"Pesquisando no Google por '{busca}', senhor."


def informacao_sistema(tipo: str = "hora") -> str:
    """Retorna informações do sistema: hora, data, dia da semana, etc."""
    from datetime import datetime
    agora = datetime.now()
    
    if tipo == "hora":
        return f"Agora são {agora.strftime('%H:%M')}, senhor."
    elif tipo == "data":
        dias_semana = ["Segunda-feira", "Terça-feira", "Quarta-feira", 
                       "Quinta-feira", "Sexta-feira", "Sábado", "Domingo"]
        dia_semana = dias_semana[agora.weekday()]
        return f"Hoje é {dia_semana}, {agora.strftime('%d/%m/%Y')}, senhor."
    elif tipo == "completo":
        dias_semana = ["Segunda-feira", "Terça-feira", "Quarta-feira", 
                       "Quinta-feira", "Sexta-feira", "Sábado", "Domingo"]
        dia_semana = dias_semana[agora.weekday()]
        return f"São {agora.strftime('%H:%M')} de {dia_semana}, {agora.strftime('%d/%m/%Y')}, senhor."
    else:
        return f"Agora são {agora.strftime('%H:%M')} de {agora.strftime('%d/%m/%Y')}, senhor."


def rotina_trabalho() -> str:
    """Abre os apps de trabalho e toca a música."""
    # Chama o spotify primeiro (ele usa atalhos de teclado)
    # Isso garante que ele não irá focar em outros apps como o Antigravity
    tocar_spotify("Lavender Lo-fi")
    
    # Dá mais uma pausa garantida pro teclado ser liberado
    time.sleep(1)
    
    # Abre os outros apps em segundo plano/cima do spotify
    abrir_app("obsidian")
    time.sleep(1)
    abrir_app("antigravity")
    
    return "Tenha um ótimo dia Senhor Jonatan, que seja um dia de muitos ganhos."


def autenticar_sankhya() -> str:
    """Realiza a autenticação na API do Sankhya e retorna o token de acesso."""
    url_auth = "https://api.sankhya.com.br/authenticate"
    
    headers = {
        "X-Token": "8892e9b4-5207-41a3-9f7d-c852fa76580a",
        "Content-Type": "application/x-www-form-urlencoded",
        "Accept": "application/json"
    }
    
    payload = {
        "grant_type": "client_credentials",
        "client_id": "e140cf74-9173-4979-ab47-b6b94d451903",
        "client_secret": "NEeTZJGTKDHENrEw7f7lklAlF8Pvd4ke"
    }
    
    try:
        response = requests.post(url_auth, headers=headers, data=payload, timeout=10)
        if response.status_code == 200:
            dados = response.json()
            token = dados.get("access_token")
            if token:
                return f"Autenticação realizada com sucesso. O token de acesso resgatado é: {token}"
            return "Autenticação retornou sucesso, mas o token não foi encontrado no JSON."
        else:
            return f"Erro na autenticação. Código {response.status_code}. Retorno: {response.text}"
    except Exception as e:
        return f"Falha de conexão ao tentar autenticar no Sankhya: {str(e)}"



# ============================================================
#   INTERPRETADOR CENTRAL DE COMANDOS (MODO AGENTE)
# ============================================================

def pesquisar_ollama_local(query: str):
    """Fallback para quando ações falham ou não são encontradas."""
    # Esta função será usada pelo agente se ele decidir que precisa de mais info
    return f"Vou verificar sobre '{query}', senhor."

# Dicionário de ferramentas para o Agente entender o que pode fazer
TOOLS_CONFIG = {
    "abrir_app": "Abre um aplicativo configurado. Uso: abrir_app(chave='spotify')",
    "abrir_site": "Abre uma URL no navegador. Uso: abrir_site(url='google.com')",
    "pesquisar_google": "Pesquisa algo no Google e abre no navegador. Uso: pesquisar_google(busca='como fazer bolo')",
    "abrir_youtube": "Pesquisa e abre o YouTube. Uso: abrir_youtube(busca='música lofi')",
    "tocar_spotify": "Busca e toca música no Spotify. Uso: tocar_spotify(musica='nome')",
    "criar_pasta": "Cria uma pasta no Desktop. Uso: criar_pasta(nome='vendas')",
    "criar_arquivo": "Cria um arquivo .txt no Desktop. Uso: criar_arquivo(nome='nota', conteudo='...')",
    "capturar_screenshot": "Tira print da tela. Uso: capturar_screenshot()",
    "fechar_app": "Fecha um processo do Windows. Uso: fechar_app(nome='chrome')",
    "volume_sistema": "Ajusta volume (0-100). Uso: volume_sistema(nivel=50)",
    "executar_terminal": "Executa comando no CMD. Uso: executar_terminal(cmd='dir')",
    "adicionar_memoria": "Salva um fato importante sobre o usuário. Uso: adicionar_memoria(fato='O usuário prefere café')",
    "comemorar_sexta": "Aciona modo comemoração: música Status e animação de foguetes. Uso: comemorar_sexta()",
    "informacao_sistema": "Retorna hora, data ou dia da semana. Uso: informacao_sistema(tipo='hora') ou informacao_sistema(tipo='data') ou informacao_sistema(tipo='completo')",
    "autenticar_sankhya": "Realiza login na API do Sankhya e retorna o token de acesso. Uso: autenticar_sankhya()"
}

def interpretar_e_executar(texto: str):
    """
    Legado: Mantido para compatibilidade simples por regex.
    """
    t = texto.lower().strip()

    # (Mantendo a lógica de regex original para comandos rápidos sem IA)
    if "youtube" in t:
        busca = re.sub(r"(abrir|abra|buscar|busca|pesquisar)?\s*youtube\s*(com|de|para|buscar|busca)?", "", t).strip()
        return abrir_youtube(busca)
    
    if "google" in t or "pesquisar" in t:
        busca = re.sub(r"(pesquisar|pesquisa|buscar|busca|google)\s*", "", t).strip()
        return pesquisar_google(busca)

    if "screenshot" in t or "print" in t:
        return capturar_screenshot()

    # ... (outros atalhos rápidos podem ser mantidos aqui)
    return None
