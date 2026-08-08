# 🤖 JARVIS — AI Personal Assistant

Agente de IA local com voz do Jarvis (Homem de Ferro), reconhecimento de voz, detector de palmas e automação do PC.

---

## 📋 REQUISITOS

- Windows 10/11
- Python 3.11+ → [python.org](https://python.org) (marque "Add to PATH")
- Ollama → [ollama.com](https://ollama.com) (Veja como configurar a GPU em [CONFIGURACAO_GPU.md](../CONFIGURACAO_GPU.md))
- Microfone funcionando

---

## 🚀 INSTALAÇÃO (Passo a Passo)

### 1. Instale as dependências
Dê duplo clique em: **`instalar.bat`**

Ele instala tudo automaticamente.

### 2. Configure seus apps
Abra o **`config.py`** em qualquer editor de texto e edite os caminhos dos seus aplicativos:

```python
"antigravity": {
    "caminho": r"C:\Caminho\Correto\AntiGravity.exe",  # ← edite aqui
},
```

Para descobrir o caminho de um app:
- Clique com botão direito no atalho do app → Propriedades → Campo "Destino"

### 3. Inicie o Jarvis
Dê duplo clique em: **`iniciar_jarvis.bat`**

---

## 🎙️ A VOZ DO JARVIS

Usa **Microsoft Edge Neural TTS** com a voz **en-GB-RyanNeural** — a mesma tecnologia da Microsoft usada no Windows 11, com timbre grave e sotaque britânico, muito similar ao Jarvis do filme.

Para trocar a voz, edite no `config.py`:
```python
JARVIS_VOICE = "en-GB-RyanNeural"    # ← Jarvis (britânico)
# JARVIS_VOICE = "en-US-GuyNeural"   # ← Americano
# JARVIS_VOICE = "pt-BR-AntonioNeural" # ← Português BR
```

---

## 👏 COMANDOS DE PALMAS

| Palmas | Ação |
|--------|------|
| 2 palmas | Spotify + VSCode |
| 3 palmas | AntiGravity + Spotify + VSCode |
| 4 palmas | Discord + Spotify |
| 5 palmas | Chrome + VSCode + Spotify + Discord |

Personalize no `config.py` na seção `PALMAS`.

---

## 💬 EXEMPLOS DE COMANDOS

| Comando | O que acontece |
|---------|----------------|
| `abrir spotify` | Abre o Spotify |
| `tocar Bohemian Rhapsody` | Abre Spotify e busca a música |
| `abrir youtube lo-fi music` | Busca no YouTube |
| `criar pasta projetos` | Cria pasta no Desktop |
| `screenshot` | Salva print na tela |
| `pesquisar inteligência artificial` | Busca no Google |
| `qual a capital da Alemanha?` | IA local responde |
| `me explique machine learning` | IA local explica |

---

## 🔧 SOLUÇÃO DE PROBLEMAS

**PyAudio não instala:**
```bash
pip install pipwin
pipwin install pyaudio
```

**Voz não funciona:**
```bash
pip install edge-tts pygame --upgrade
```

**Ollama não responde:**
```bash
# Abra um terminal e execute:
ollama serve
# Em outro terminal:
ollama pull llama3.2
```

**Microfone não detectado:**
- Verifique nas configurações do Windows se o microfone está habilitado
- Tente ajustar o `limiar` em `palmas.py` (padrão: 0.35)

---

## 📁 ESTRUTURA DOS ARQUIVOS

```
agente_jarvis/
├── agente.py        ← Arquivo principal (execute este)
├── config.py        ← Suas configurações e atalhos
├── acoes.py         ← Motor de ações do sistema
├── voz_jarvis.py    ← Motor de voz Jarvis
├── palmas.py        ← Detector de palmas
├── ia_local.py      ← Interface com Ollama
├── instalar.bat     ← Instalação automática
└── iniciar_jarvis.bat ← Iniciar o agente
```
