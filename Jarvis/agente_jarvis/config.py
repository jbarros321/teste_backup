# ============================================================
#   OLÁ BI AGENT — config.py
#   Edite este arquivo para personalizar seus comandos!
# ============================================================

import os

# ----- SEU USUÁRIO DO WINDOWS -----
USUARIO = os.getenv("USERNAME", "Usuario")

# ----- AUXILIAR DE CAMINHOS -----
def buscar_caminho(nome_exe, caminhos_provaveis):
    """Procura um executável em uma lista de caminhos ou no PATH."""
    import shutil
    # 1. Tenta no PATH do sistema
    no_path = shutil.which(nome_exe)
    if no_path: return no_path
    
    # 2. Tenta nos caminhos prováveis
    for caminho in caminhos_provaveis:
        if os.path.exists(caminho):
            return caminho
    return nome_exe # Fallback para o nome simples

# ----- CAMINHOS DOS APLICATIVOS -----
COMANDOS = {
    "spotify": {
        "tipo": "app",
        "processo": "Spotify.exe",
        "caminho": buscar_caminho("Spotify.exe", [
            rf"C:\Users\{USUARIO}\AppData\Roaming\Spotify\Spotify.exe",
            rf"C:\Users\{USUARIO}\AppData\Local\Microsoft\WindowsApps\Spotify.exe"
        ]),
        "aliases": ["spotify", "musica", "tocar musica"]
    },
    "vscode": {
        "tipo": "app",
        "processo": "Code.exe",
        "caminho": buscar_caminho("Code.exe", [
            rf"C:\Users\{USUARIO}\AppData\Local\Programs\Microsoft VS Code\Code.exe",
            r"C:\Program Files\Microsoft VS Code\Code.exe"
        ]),
        "aliases": ["vscode", "vs code", "editor de codigo"]
    },
    "chrome": {
        "tipo": "app",
        "processo": "chrome.exe",
        "caminho": buscar_caminho("chrome.exe", [
            r"C:\Program Files\Google\Chrome\Application\chrome.exe",
            r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe"
        ]),
        "aliases": ["chrome", "navegador", "internet"]
    },
    "discord": {
        "tipo": "app",
        "processo": "Discord.exe",
        "caminho": buscar_caminho("Discord.exe", [
            rf"C:\Users\{USUARIO}\AppData\Local\Discord\Update.exe --processStart Discord.exe",
            rf"C:\Users\{USUARIO}\AppData\Local\Discord\app.exe"
        ]),
        "aliases": ["discord", "chat"]
    },
    "obsidian": {
        "tipo": "app",
        "processo": "Obsidian.exe",
        "caminho": buscar_caminho("Obsidian.exe", [
            rf"C:\Users\{USUARIO}\AppData\Local\Obsidian\Obsidian.exe",
            rf"C:\Users\{USUARIO}\AppData\Local\Programs\Obsidian\Obsidian.exe",
            r"C:\Program Files\Obsidian\Obsidian.exe"
        ]),
        "aliases": ["obsidian", "anotações", "conhecimento"]
    },
    "antigravity": {
        "tipo": "app",
        "processo": "Antigravity.exe",
        "caminho": buscar_caminho("Antigravity.exe", [
            rf"C:\Program Files\Antigravity\Antigravity.exe",
            rf"C:\Users\{USUARIO}\AppData\Local\Programs\Antigravity\Antigravity.exe"
        ]),
        "aliases": ["antigravity", "ia", "agente"]
    },
    "whatsapp": {
        "tipo": "app",
        "processo": "WhatsApp.exe",
        "caminho": buscar_caminho("WhatsApp.exe", [
            rf"C:\Users\{USUARIO}\AppData\Local\WhatsApp\WhatsApp.exe",
            rf"C:\Users\{USUARIO}\AppData\Local\Microsoft\WindowsApps\WhatsApp.exe"
        ]),
        "aliases": ["whatsapp", "zap", "mensagens"]
    },
    "notepad": {"tipo": "app", "processo": "notepad.exe", "caminho": "notepad.exe", "aliases": ["notepad", "bloco de notas"]},
    "calculadora": {"tipo": "app", "processo": "CalculatorApp.exe", "caminho": "calc.exe", "aliases": ["calculadora", "calc"]},
    "explorer": {"tipo": "app", "processo": "explorer.exe", "caminho": "explorer.exe", "aliases": ["explorador", "arquivos"]},
    "taskmgr": {"tipo": "app", "processo": "taskmgr.exe", "caminho": "taskmgr.exe", "aliases": ["gerenciador de tarefas", "processos"]},
}

# ----- COMANDOS DE PALMAS -----
# Número de palmas → lista de apps para abrir
PALMAS = {
    2: ["spotify", "vscode"],
    3: ["antigravity", "spotify", "vscode"],
    4: ["discord", "spotify"],
    5: ["chrome", "vscode", "spotify", "discord"],
}

# ----- CONFIGURAÇÃO DA VOZ JARVIS -----
# Voz Microsoft Neural (soa como Jarvis)
# Opções recomendadas:
#   "en-GB-RyanNeural"     ← VOZ JARVIS (inglês britânico, grave)
#   "en-US-GuyNeural"      ← Alternativa americana
#   "pt-BR-AntonioNeural"  ← Português BR masculino profissional
JARVIS_VOICE = "pt-BR-AntonioNeural"

# Taxa de fala (palavras por minuto) — 140 a 180 recomendado
JARVIS_RATE = "+0%"

# Nome do agente
AGENTE_NOME = "Bi"

# Wake words aceitas
WAKE_WORDS = ["oi bi", "oibi", "oí bi"]

# Saudações iniciais (escolhida aleatoriamente)
SAUDACOES = [
    "Sistemas online. É bom tê-lo de volta, senhor.",
    "Bom dia, senhor. Bi operacional e pronto para servir.",
    "Bem-vindo de volta. Como posso ajudá-lo hoje?",
    "Online e aguardando suas ordens, senhor. O que deseja?",
    "Ao seu dispor, senhor. O que faremos hoje?",
]

# Idioma para reconhecimento de voz
# "pt-BR" para português, "en-US" para inglês
VOZ_IDIOMA = "pt-BR"

# Modelo Ollama para IA local
OLLAMA_MODEL = "llama3.2"

# URL do Ollama
OLLAMA_URL = "http://localhost:11434/api/chat"
