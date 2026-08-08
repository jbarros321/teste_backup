# ============================================================
#   OLÁ BI AGENT — agente.py (macOS VERSION)
# ============================================================

import speech_recognition as sr
import threading
import random
import sys
import time
import os
import re

from config import (
    SAUDACOES, AGENTE_NOME, VOZ_IDIOMA,
    PALMAS, WAKE_WORDS
)
from voz_jarvis import falar, tocar_beep
import acoes
from palmas import DetectorPalmas
from ia_local import perguntar_ia, verificar_ollama
import memoria
import gui_jarvis

# ============================================================
#   ESTADO GLOBAL
# ============================================================

historico_conversa = []
ATIVO = True
detector = None
clap_event = threading.Event()

# ============================================================
#   RECONHECIMENTO DE VOZ
# ============================================================

def ouvir_microfone(timeout=5, phrase_time_limit=None) -> str:
    r = sr.Recognizer()
    r.energy_threshold = 300
    r.dynamic_energy_threshold = True

    with sr.Microphone() as source:
        try:
            audio = r.listen(source, timeout=timeout, phrase_time_limit=phrase_time_limit)
            texto = r.recognize_google(audio, language=VOZ_IDIOMA)
            return texto
        except:
            return ""

def aguardar_wake_word():
    r = sr.Recognizer()
    mic = sr.Microphone()
    
    print(f"👂 Aguardando comando... (Diga 'Oi Bi')")
    
    with mic as source:
        r.adjust_for_ambient_noise(source, duration=0.5)
    
    while ATIVO:
        if clap_event.is_set():
            clap_event.clear()
            tocar_beep()
            gui_jarvis.mudar_estado('listening')
            return True

        with mic as source:
            try:
                audio = r.listen(source, timeout=1, phrase_time_limit=4)
                texto = r.recognize_google(audio, language=VOZ_IDIOMA).lower()
                
                if any(w in texto for w in WAKE_WORDS):
                    tocar_beep()
                    gui_jarvis.mudar_estado('listening')
                    return True
            except sr.WaitTimeoutError:
                continue
            except:
                continue

# ============================================================
#   EXECUÇÃO DE FERRAMENTAS
# ============================================================

def executar_ferramenta_agente(comando_acao: str) -> str:
    try:
        match = re.match(r"(\w+)\((.*)\)", comando_acao)
        if not match: return "Erro: Formato inválido."

        nome_funcao = match.group(1)
        args_str = match.group(2)
        args = {}
        if args_str:
            pares = re.findall(r"(\w+)=['\"]?([^'\",]*)['\"]?", args_str)
            for k, v in pares:
                args[k] = int(v) if v.isdigit() else v

        if hasattr(acoes, nome_funcao):
            funcao = getattr(acoes, nome_funcao)
            return funcao(**args)
        
        return f"Erro: Ferramenta '{nome_funcao}' não encontrada."
    except Exception as e:
        return f"Erro na execução: {e}"

# ============================================================
#   CALLBACK DE PALMAS
# ============================================================

def ao_detectar_palmas(n_palmas: int):
    apps = PALMAS.get(n_palmas, [])
    if apps:
        falar(f"Palmas detectadas. Abrindo sistemas no Mac.")
        for app in apps:
            acoes.abrir_app(app)
        clap_event.set()

# ============================================================
#   PROCESSADOR DE COMANDOS
# ============================================================

def processar_comando(texto: str) -> bool:
    global historico_conversa
    texto = texto.strip()
    if not texto: return True

    gui_jarvis.enviar_log(f"📥 Comando: \"{texto}\"", "info")

    if any(p in texto.lower() for p in ["sair", "desligar", "encerrar"]):
        falar("Desligando sistemas. Até logo, senhor.")
        return False

    # ── FLUXO NORMAL VIA IA ──
    historico_conversa.append({"role": "user", "content": texto})
    gui_jarvis.mudar_estado('thinking')
    
    resumo_ia = ""
    for chunk in perguntar_ia(texto, historico_conversa):
        resumo_ia += chunk
        print(chunk, end="", flush=True)
        if "ACTION:" in resumo_ia and ")" in chunk: break

    print("\n")

    if "ACTION:" in resumo_ia:
        match = re.search(r"ACTION:\s*(\w+\(.*\))", resumo_ia)
        if match:
            comando_acao = match.group(1).strip()
            resultado = executar_ferramenta_agente(comando_acao)
            
            historico_conversa.append({"role": "assistant", "content": resumo_ia})
            final_gen = perguntar_ia(
                f"O resultado da ação foi: '{resultado}'. Informe ao usuário de forma natural.",
                historico_conversa
            )
            final_texto = "".join(list(final_gen))
            falar(final_texto)
            historico_conversa.append({"role": "assistant", "content": final_texto})
        else:
            falar(resumo_ia)
            historico_conversa.append({"role": "assistant", "content": resumo_ia})
    else:
        falar(resumo_ia)
        historico_conversa.append({"role": "assistant", "content": resumo_ia})

    return True

# ============================================================
#   MAIN
# ============================================================

def main():
    global ATIVO, detector
    os.system("clear")
    print("█" * 55)
    print(f"  O.L.A  B.I  —  macOS EDITION")
    print("█" * 55)

    if not verificar_ollama(tentativas=3):
        print("⚠️  Ollama não detectado.")
    
    detector = DetectorPalmas(callback=ao_detectar_palmas, limiar=0.35)
    try: detector.iniciar()
    except: pass

    falar("Bi operacional no seu Mac, senhor.")
    gui_jarvis.rodar_interface_assincrona()

    while ATIVO:
        if aguardar_wake_word():
            if detector: detector.pausar()
            comando = ouvir_microfone(timeout=5, phrase_time_limit=10)
            if comando:
                ATIVO = processar_comando(comando)
            gui_jarvis.mudar_estado('idle')
            if detector: detector.retomar()

    if detector: detector.parar()

if __name__ == "__main__":
    try: main()
    except KeyboardInterrupt: sys.exit(0)
