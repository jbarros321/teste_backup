# ============================================================
#   OLÁ BI AGENT — config.py (macOS VERSION)
# ============================================================

import os

# ----- SEU USUÁRIO DO MACOS -----
USUARIO = os.getenv("USER", "usuario")

# ----- CAMINHOS DOS APLICATIVOS (macOS) -----
COMANDOS = {
    "spotify": {
        "tipo": "app",
        "processo": "Spotify",
        "caminho": "Spotify", # No mac usamos o nome para 'open -a'
        "aliases": ["spotify", "musica", "tocar musica"]
    },
    "vscode": {
        "tipo": "app",
        "processo": "Visual Studio Code",
        "caminho": "Visual Studio Code",
        "aliases": ["vscode", "vs code", "editor de codigo"]
    },
    "chrome": {
        "tipo": "app",
        "processo": "Google Chrome",
        "caminho": "Google Chrome",
        "aliases": ["chrome", "navegador", "internet"]
    },
    "discord": {
        "tipo": "app",
        "processo": "Discord",
        "caminho": "Discord",
        "aliases": ["discord", "chat"]
    },
    "obsidian": {
        "tipo": "app",
        "processo": "Obsidian",
        "caminho": "Obsidian",
        "aliases": ["obsidian", "anotações", "conhecimento"]
    },
    "whatsapp": {
        "tipo": "app",
        "processo": "WhatsApp",
        "caminho": "WhatsApp",
        "aliases": ["whatsapp", "zap", "mensagens"]
    },
    "terminal": {"tipo": "app", "processo": "Terminal", "caminho": "Terminal", "aliases": ["terminal", "console"]},
    "calculadora": {"tipo": "app", "processo": "Calculator", "caminho": "Calculator", "aliases": ["calculadora", "calc"]},
    "finder": {"tipo": "app", "processo": "Finder", "caminho": "Finder", "aliases": ["finder", "arquivos"]},
}

# ----- COMANDOS DE PALMAS -----
PALMAS = {
    2: ["spotify", "vscode"],
    3: ["antigravity", "spotify", "vscode"],
    4: ["discord", "spotify"],
    5: ["chrome", "vscode", "spotify", "discord"],
}

# ----- CONFIGURAÇÃO DA VOZ -----
JARVIS_VOICE = "pt-BR-AntonioNeural"
JARVIS_RATE = "+0%"
AGENTE_NOME = "Bi"
WAKE_WORDS = ["oi bi", "oibi", "oí bi"]

SAUDACOES = [
    "Sistemas online. É bom tê-lo de volta, senhor.",
    "Bom dia, senhor. Bi operacional e pronto para servir.",
    "Bem-vindo de volta. Como posso ajudá-lo hoje?",
    "Online e aguardando suas ordens, senhor. O que deseja?",
    "Ao seu dispor, senhor. O que faremos hoje?",
]

VOZ_IDIOMA = "pt-BR"
OLLAMA_MODEL = "llama3.2"
OLLAMA_URL = "http://localhost:11434/api/chat"
