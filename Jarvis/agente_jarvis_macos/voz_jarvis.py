# ============================================================
#   OLÁ BI AGENT — voz_jarvis.py
#   Motor de voz com Microsoft Neural TTS
# ============================================================

import asyncio
import edge_tts
import pygame
import tempfile
import os
import time
from config import JARVIS_VOICE, JARVIS_RATE

import shutil

# Inicializa pygame para tocar áudio
try:
    pygame.mixer.init()
except:
    print("⚠️  Aviso: Erro ao iniciar mixer de áudio.")

def tocar_beep():
    """Toca um som curto de ativação."""
    # (Simulamos um beep usando frequências do pygame se não houver arquivo)
    try:
        # Se você tiver um arquivo 'activate.wav' na pasta, use-o.
        # Caso contrário, geramos um som simples.
        import numpy as np
        duration = 0.1  # segundos
        sample_rate = 44100
        t = np.linspace(0, duration, int(sample_rate * duration), False)
        tone = np.sin(440 * t * 2 * np.pi) * 0.3 # Lá (440Hz)
        sound = pygame.sndarray.make_sound((tone * 32767).astype(np.int16))
        sound.play()
    except:
        pass

def falar(texto: str):
    """Sintetiza o texto com a voz do Bi e toca o áudio."""
    import gui_jarvis
    gui_jarvis.mudar_estado('speaking')
    print(f"\n🤖 {texto}")
    
    # Se o texto for muito curto (uma ação), falamos rápido
    try:
        asyncio.run(_sintetizar_e_tocar(texto))
    except Exception as e:
        # Fallback offline rápido
        try:
            import pyttsx3
            engine = pyttsx3.init()
            engine.setProperty('rate', 170)
            engine.say(texto)
            engine.runAndWait()
        except:
            print(f"  [VOZ INDISPONÍVEL] {texto}")

async def _sintetizar_e_tocar(texto: str):
    """Gera áudio com edge-tts e toca via pygame."""
    try:
        communicate = edge_tts.Communicate(texto, JARVIS_VOICE, rate=JARVIS_RATE)
        
        # Pasta temporária segura
        tmp_dir = tempfile.gettempdir()
        tmp_path = os.path.join(tmp_dir, f"jarvis_voice_{int(time.time())}.mp3")

        await communicate.save(tmp_path)
        
        pygame.mixer.music.load(tmp_path)
        pygame.mixer.music.play()
        
        while pygame.mixer.music.get_busy():
            await asyncio.sleep(0.05)
        
        pygame.mixer.music.unload()
        if os.path.exists(tmp_path):
            os.unlink(tmp_path)
    except Exception as e:
        raise e

def listar_vozes_disponiveis():
    """Lista todas as vozes neurais disponíveis."""
    async def _listar():
        vozes = await edge_tts.list_voices()
        for v in vozes:
            if v["Gender"] == "Male":
                print(f"  🎙️  {v['ShortName']} — {v['Locale']}")
    asyncio.run(_listar())

if __name__ == "__main__":
    print("Testando voz Bi...")
    falar("Sistemas online, senhor. Bi operacional.")
    falar("Como posso ajudá-lo hoje?")
