"""
╔══════════════════════════════════════════════╗
║         J.A.R.V.I.S  Launcher  v7.0         ║
║  "Jarvis Ativar"  → Som de Cinema           ║
║  "Jarvis Feche"   → Protocolo de Encerramento║
╚══════════════════════════════════════════════╝
"""

import asyncio
import numpy as np
import subprocess
import sys
import time
import os
import threading
import sounddevice as sd
import wave
import struct
import speech_recognition as sr
import tempfile

# ─── CONFIGURAÇÕES ────────────────────────────────────────────────────────────
WAKE_ATIVAR     = "jarvis ativar"     # Frase para abrir tudo
WAKE_FECHAR     = ["jarvis feche", "jarvis encerrar"]  # Frases para fechar e desligar
COOLDOWN        = 10.0                # Pausa após acionar (segundos)
SAMPLE_RATE     = 16000              # Taxa de amostra para reconhecimento
DURACAO_ESCUTA  = 4                  # Segundos de gravação por ciclo

# ─── NOVOS COMANDOS ───────────────────────────────────────────────────────────
WAKE_RESUMO_INICIO = "jarvis gerar resumo de atividade"
WAKE_RESUMO_FIM    = "jarvis fim de atividade"
CLOCKIFY_DIR       = os.path.join(os.path.dirname(__file__), "clockify")

# ─── CAMINHOS DOS APPS ────────────────────────────────────────────────────────
CHROME_PATH      = r"C:\Program Files\Google\Chrome\Application\chrome.exe"
BRAVE_PATH       = r"C:\Program Files\BraveSoftware\Brave-Browser\Application\brave.exe"
ANTIGRAVITY_PATH = r"C:\Users\Jonatan Barros\AppData\Local\Programs\Antigravity\antigravity.exe"
ANTIGRAVITY_DIR  = r"E:\personalizacoes-main\personalizacoes-main"

CHROME_URL = "https://calendar.google.com"
BRAVE_URL  = "https://agent.mitralab.io/w/18618"

# ─── SPOTIFY ──────────────────────────────────────────────────────────────────
SPOTIFY_MUSICA = "Céu Azul"

# ─── VOZ JARVIS ───────────────────────────────────────────────────────────────
# Voz: pt-PT-DuarteNeural — Masculina, Europeia (Estilo Mordomo/Jarvis)
VOZ_JARVIS    = "pt-PT-DuarteNeural"
MENSAGEM_VOZ  = "Bom dia, senhor. Iniciando as atividades. Todos os sistemas estão operacionais. Tenha um excelente dia."

# ──────────────────────────────────────────────────────────────────────────────


async def _sintetizar_voz(mensagem: str, caminho_mp3: str):
    """Sintetiza fala com edge-tts (voz formal europeia)."""
    import edge_tts
    # Rate -10% para voz mais calma e profunda estilo Jarvis
    tts = edge_tts.Communicate(mensagem, VOZ_JARVIS, rate="-5%", volume="+0%")
    await tts.save(caminho_mp3)


def falar(mensagem: str):
    """Fala com a voz masculina brasileira do J.A.R.V.I.S via edge-tts."""
    try:
        with tempfile.NamedTemporaryFile(suffix=".mp3", delete=False) as f:
            tmp_path = f.name

        # Gera o áudio de forma assíncrona
        asyncio.run(_sintetizar_voz(mensagem, tmp_path))

        # Toca o MP3 com PowerShell (nativo do Windows, sem dependências extras)
        ps_script = (
            "Add-Type -AssemblyName presentationCore; "
            "$m = [System.Windows.Media.MediaPlayer]::new(); "
            f"$m.Open([uri]::new('file:///{tmp_path.replace(chr(92), '/')}'));"
            "$m.Play(); "
            "Start-Sleep -s 12; "
            "$m.Close()"
        )
        subprocess.call(
            ["powershell", "-WindowStyle", "Hidden", "-Command", ps_script],
            timeout=20
        )
        try:
            os.unlink(tmp_path)
        except Exception:
            pass

    except Exception as e:
        print(f"[VOZ] edge-tts erro: {e}")
        # Fallback: pyttsx3 com voz masculina
        try:
            import pyttsx3
            engine = pyttsx3.init()
            engine.setProperty("rate", 155)
            engine.setProperty("volume", 1.0)
            vozes = engine.getProperty("voices")
            for v in vozes:
                if "daniel" in v.id.lower() or "rafael" in v.id.lower():
                    engine.setProperty("voice", v.id)
                    break
            engine.say(mensagem)
            engine.runAndWait()
            engine.stop()
        except Exception as e2:
            print(f"[VOZ] pyttsx3 fallback erro: {e2}")


def focar_janela(titulo_parcial: str) -> bool:
    """Força o foco em uma janela pelo título usando PowerShell."""
    try:
        subprocess.call(
            ["powershell", "-WindowStyle", "Hidden", "-Command",
             f'(New-Object -ComObject WScript.Shell).AppActivate("{titulo_parcial}")'],
            timeout=3
        )
        return True
    except Exception:
        return False


def abrir_spotify(musica: str):
    """Abre o Spotify com a música pesquisada e dá play automaticamente."""
    try:
        uri = f"spotify:search:{musica}"
        os.startfile(uri)
        print(f"[OK] Spotify aberto -> '{musica}'")

        try:
            import pyautogui

            # Aguarda o Spotify abrir e carregar a pesquisa
            time.sleep(5)

            # Força o foco no Spotify via PowerShell (mais confiável)
            focar_janela("Spotify")
            time.sleep(1.5)

            # Espaço = Play/Pause universal do Spotify
            pyautogui.press("space")
            print("[OK] Play disparado no Spotify.")

        except Exception as e:
            print(f"[AVISO] Auto-play Spotify: {e}")

    except Exception as e:
        print(f"[ERRO] Spotify: {e}")


def abrir_apps():
    """Abre todos os aplicativos após o wake word ser reconhecido."""
    print("\n*** J.A.R.V.I.S ATIVADO! Executando ações... ***\n")

    # Voz em thread paralela
    t_voz = threading.Thread(target=falar, args=(MENSAGEM_VOZ,), daemon=True)
    t_voz.start()

    time.sleep(0.3)

    # ── Chrome → Google Agenda ──
    try:
        if os.path.exists(CHROME_PATH):
            subprocess.Popen([CHROME_PATH, CHROME_URL])
        else:
            subprocess.Popen(f'start chrome "{CHROME_URL}"', shell=True)
        print("[OK] Chrome -> Google Agenda")
    except Exception as e:
        print(f"[ERRO] Chrome: {e}")

    time.sleep(0.4)

    # ── Brave → Mitra ──
    try:
        if os.path.exists(BRAVE_PATH):
            subprocess.Popen([BRAVE_PATH, BRAVE_URL])
        else:
            subprocess.Popen(f'start brave "{BRAVE_URL}"', shell=True)
        print("[OK] Brave -> Mitra.io")
    except Exception as e:
        print(f"[ERRO] Brave: {e}")

    time.sleep(0.4)

    # ── Antigravity → abre passando a pasta como argumento ──
    try:
        if os.path.exists(ANTIGRAVITY_PATH):
            # O ponto "." instrui o programa a abrir o diretório de trabalho (ANTIGRAVITY_DIR)
            subprocess.Popen([ANTIGRAVITY_PATH, "."], cwd=ANTIGRAVITY_DIR)
            print(f"[OK] Antigravity aberto em: {ANTIGRAVITY_DIR}")
        else:
            print(f"[AVISO] Antigravity não encontrado em: {ANTIGRAVITY_PATH}")
    except Exception as e:
        print(f"[ERRO] Antigravity: {e}")

    time.sleep(0.4)

    # ── Spotify ──
    abrir_spotify(SPOTIFY_MUSICA)

    t_voz.join(timeout=15)
    print("\nTudo pronto! Tenha um ótimo dia, senhor.\n" + "─" * 50)


def fechar_tudo():
    """Fecha todos os apps conhecidos e desliga o computador."""
    print("\n*** J.A.R.V.I.S ENCERRANDO SISTEMAS... ***\n")

    threading.Thread(
        target=falar,
        args=("Encerrando todos os sistemas. Boa noite, senhor.",),
        daemon=True
    ).start()

    time.sleep(2)  # Aguarda a voz começar

    # ── Fecha os apps conhecidos ──
    apps_fechar = [
        "chrome.exe",
        "brave.exe",
        "Spotify.exe",
        "antigravity.exe",
        "Code.exe",       # VS Code, caso esteja aberto
    ]
    for app in apps_fechar:
        try:
            subprocess.call(
                ["taskkill", "/F", "/IM", app],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL
            )
            print(f"[OK] Fechado: {app}")
        except Exception:
            pass

    time.sleep(3)  # Aguarda a voz terminar

    # ── Desliga o computador em 30 segundos ──
    print("[OK] Desligando o computador em 30 segundos...")
    subprocess.call(["shutdown", "/s", "/t", "10"])
    print("\nAté logo, senhor. Desligando em 10s.\n" + "─" * 50)


# ─── RECONHECIMENTO DE VOZ ────────────────────────────────────────────────────

def gravar_audio(duracao=DURACAO_ESCUTA, rate=SAMPLE_RATE):
    """Grava áudio pelo microfone usando sounddevice."""
    print(".", end="", flush=True)
    gravacao = sd.rec(int(duracao * rate), samplerate=rate, channels=1, dtype="int16")
    sd.wait()
    return gravacao


def reconhecer_voz(audio_array, rate=SAMPLE_RATE) -> str:
    """Converte array de áudio em texto via Google Speech Recognition."""
    try:
        recognizer = sr.Recognizer()

        with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as tmp:
            tmp_path = tmp.name

        audio_flat = audio_array.flatten()
        with wave.open(tmp_path, "wb") as wf:
            wf.setnchannels(1)
            wf.setsampwidth(2)
            wf.setframerate(rate)
            wf.writeframes(struct.pack(f"<{len(audio_flat)}h", *audio_flat))

        with sr.AudioFile(tmp_path) as source:
            audio = recognizer.record(source)

        os.unlink(tmp_path)

        texto = recognizer.recognize_google(audio, language="pt-BR").strip().lower()
        return texto

    except sr.UnknownValueError:
        return ""
    except sr.RequestError as e:
        print(f"\n[ERRO] Google Speech: {e}")
        return ""
    except Exception as e:
        print(f"\n[ERRO] reconhecer_voz: {e}")
        return ""


def salvar_resumo_clockify(textos):
    """Cria a pasta clockify e salva a lista de atividades em um arquivo .txt."""
    try:
        if not os.path.exists(CLOCKIFY_DIR):
            os.makedirs(CLOCKIFY_DIR)
            print(f"[OK] Pasta criada: {CLOCKIFY_DIR}")

        timestamp = time.strftime("%Y%m%d_%H%M%S")
        caminho_arquivo = os.path.join(CLOCKIFY_DIR, f"resumo_clockify_{timestamp}.txt")

        with open(caminho_arquivo, "w", encoding="utf-8") as f:
            f.write("─── RESUMO DE ATIVIDADES (CLOCKIFY) ───\n")
            f.write(f"Data: {time.strftime('%d/%m/%Y %H:%M:%S')}\n")
            f.write("─" * 40 + "\n\n")
            for i, txt in enumerate(textos, 1):
                f.write(f"{i}. {txt.capitalize()}\n")

        print(f"[OK] Resumo salvo: {caminho_arquivo}")
        return True
    except Exception as e:
        print(f"[ERRO] salvar_resumo: {e}")
        return False


def escutar_wake_word():
    """Loop principal — fica ouvindo até detectar os comandos de voz."""
    print("=" * 50)
    print("   J.A.R.V.I.S  v6.0")
    print(f"   '{WAKE_ATIVAR.upper()}'  → Abre tudo")
    print(f"   'JARVIS FECHE'          → Fecha tudo e desliga")
    print("=" * 50)
    print("\nMicrofone ativo. Ouvindo", end="")

    ultima_acao = 0.0
    modo_resumo = False
    transcricao_resumo = []

    while True:
        try:
            audio = gravar_audio()
            texto = reconhecer_voz(audio)

            if texto:
                print(f"\n[MIC] Ouvi: '{texto}'")

            agora = time.time()
            em_cooldown = (agora - ultima_acao) <= COOLDOWN

            # ── Modo Resumo Ativo ──
            if modo_resumo:
                if WAKE_RESUMO_FIM in texto:
                    modo_resumo = False
                    print("\n[SISTEMA] Finalizando resumo...")
                    if transcricao_resumo:
                        salvar_resumo_clockify(transcricao_resumo)
                        falar("Resumo de atividade finalizado e salvo na pasta Clockify, senhor.")
                    else:
                        falar("Nenhuma atividade foi registrada, senhor.")
                elif texto:
                    # Se não for o comando de fim, adiciona o texto à lista
                    transcricao_resumo.append(texto)
                continue

            # ── Comando: GERAR RESUMO ──
            if WAKE_RESUMO_INICIO in texto:
                if not em_cooldown:
                    ultima_acao = agora
                    modo_resumo = True
                    transcricao_resumo = []
                    threading.Thread(target=falar, args=("Iniciando registro de atividade. Pode falar, senhor.",), daemon=True).start()
                    print("\n[SISTEMA] Modo Resumo Ativado.")
                else:
                    print("Cooldown ativo, aguarde...")

            # ── Comando: ABRIR TUDO ──
            elif WAKE_ATIVAR in texto:
                if not em_cooldown:
                    ultima_acao = agora
                    threading.Thread(target=abrir_apps, daemon=True).start()
                    print("\nCooldown ativo por 10s...")
                    print("\nOuvindo", end="")
                else:
                    print("Cooldown ativo, aguarde...")

            # ── Comando: FECHAR TUDO E DESLIGAR ──
            elif any(cmd in texto for cmd in WAKE_FECHAR):
                if not em_cooldown:
                    ultima_acao = agora
                    threading.Thread(target=fechar_tudo, daemon=True).start()
                    print("\nDesligando sistemas...")
                else:
                    print("Cooldown ativo, aguarde...")

        except KeyboardInterrupt:
            print("\n\nEncerrando J.A.R.V.I.S. Até logo!")
            sys.exit(0)
        except Exception as e:
            print(f"\n[ERRO] Loop principal: {e}")
            time.sleep(1)


if __name__ == "__main__":
    escutar_wake_word()