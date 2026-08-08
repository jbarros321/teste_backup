# ============================================================
#   OLÁ BI AGENT — agente.py
#   Arquivo principal — MODO MÃOS LIVRES + STREAMING
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
    PALMAS, COMANDOS, WAKE_WORDS
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
detector = None  # Referência global para controlar palmas
clap_event = threading.Event()  # Sinaliza quando palmas foram detectadas

# ============================================================
#   RECONHECIMENTO DE VOZ
# ============================================================

def ouvir_microfone(timeout=5, phrase_time_limit=None) -> str:
    """Captura fala do microfone e converte para texto."""
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
    """Loop de baixo consumo que espera ouvir 'Olá Bi'."""
    r = sr.Recognizer()
    mic = sr.Microphone()
    
    print(f"👂 Aguardando comando... (Diga 'Oi Bi')")
    
    with mic as source:
        r.adjust_for_ambient_noise(source, duration=0.5)
    
    while ATIVO:
        # Se as palmas dispararam o evento, pula a wake word
        if clap_event.is_set():
            clap_event.clear()
            tocar_beep()
            gui_jarvis.mudar_estado('listening')
            gui_jarvis.enviar_log("🎙️ Ativação por palmas!", "action")
            return True

        with mic as source:
            try:
                # Timeout curto permite verificar o clap_event frequentemente
                audio = r.listen(source, timeout=1, phrase_time_limit=4)
                texto = r.recognize_google(audio, language=VOZ_IDIOMA).lower()
                
                # Verifica todas as variações de wake word
                if any(w in texto for w in WAKE_WORDS):
                    tocar_beep()
                    gui_jarvis.mudar_estado('listening')
                    gui_jarvis.enviar_log("🎙️ Wake word detectada!", "action")
                    return True
            except sr.WaitTimeoutError:
                continue
            except:
                continue

# ============================================================
#   EXECUÇÃO DE FERRAMENTAS
# ============================================================

def executar_ferramenta_agente(comando_acao: str) -> str:
    """Executa ações solicitadas pela IA."""
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

        if nome_funcao == "adicionar_memoria":
            fato = args.get("fato", "")
            return f"Memorizado: {fato}" if memoria.adicionar_fato(fato) else "Já sabia disso."

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
        falar(f"Palmas detectadas. Abrindo sistemas, senhor.")
        gui_jarvis.enviar_log(f"👏 {n_palmas} palmas → Abrindo: {', '.join(apps)}", "action")
        for app in apps:
            acoes.abrir_app(app)
        
        # Ativa o modo escuta automaticamente após as palmas
        clap_event.set()

# ============================================================
#   PROCESSADOR DE COMANDOS (STREAMING)
# ============================================================

def limpar_action_do_texto(texto: str) -> str:
    """Remove qualquer 'ACTION: funcao(...)' do texto para não falar o comando."""
    return re.sub(r"ACTION:\s*\w+\(.*?\)", "", texto).strip()


def processar_comando(texto: str) -> bool:
    global historico_conversa
    texto = texto.strip()
    if not texto: return True

    # Log do comando recebido
    gui_jarvis.enviar_log(f"📥 Comando: \"{texto}\"", "info")

    if any(p in texto.lower() for p in ["sair", "desligar", "encerrar"]):
        gui_jarvis.enviar_log("🔌 Desligando sistemas...", "system")
        falar("Desligando sistemas. Até logo, senhor.")
        return False

    if "vamos trabalhar" in texto.lower():
        print("⚙️ Executando rotina de trabalho...")
        gui_jarvis.enviar_log("🏢 Executando rotina de trabalho...", "action")
        msg = acoes.rotina_trabalho()
        falar(msg)
        gui_jarvis.enviar_log("✅ Rotina de trabalho concluída", "result")
        historico_conversa.append({"role": "user", "content": texto})
        historico_conversa.append({"role": "assistant", "content": msg})
        return True

    # ── ATALHOS DIRETOS (sem depender da IA) ──
    t = texto.lower()
    
    # Hora / Data
    if any(p in t for p in ["que hora", "que horas", "hora atual", "horas agora"]):
        gui_jarvis.enviar_log("🕐 Consultando relógio do sistema...", "search")
        resultado = acoes.informacao_sistema("hora")
        falar(resultado)
        gui_jarvis.enviar_log(f"✅ {resultado}", "result")
        historico_conversa.append({"role": "user", "content": texto})
        historico_conversa.append({"role": "assistant", "content": resultado})
        return True
    
    if any(p in t for p in ["que dia", "dia hoje", "data de hoje", "data atual"]):
        gui_jarvis.enviar_log("📅 Consultando calendário do sistema...", "search")
        resultado = acoes.informacao_sistema("data")
        falar(resultado)
        gui_jarvis.enviar_log(f"✅ {resultado}", "result")
        historico_conversa.append({"role": "user", "content": texto})
        historico_conversa.append({"role": "assistant", "content": resultado})
        return True

    # Pesquisa no Google
    for gatilho in ["pesquisa ", "pesquisar ", "pesquise ", "busca ", "buscar ", "procura ", "procurar "]:
        if gatilho in t:
            busca = t.split(gatilho, 1)[1].strip()
            # Remove palavras extras como "no google", "na internet"
            busca = re.sub(r"\b(no google|na internet|pra mim|para mim)\b", "", busca).strip()
            if busca:
                gui_jarvis.enviar_log(f"🔍 Pesquisando: \"{busca}\"", "search")
                resultado = acoes.pesquisar_google(busca)
                falar(f"Pesquisando por {busca}, senhor.")
                gui_jarvis.enviar_log(f"✅ Pesquisa aberta no navegador", "result")
                historico_conversa.append({"role": "user", "content": texto})
                historico_conversa.append({"role": "assistant", "content": resultado})
                return True

    # ── FLUXO NORMAL VIA IA ──
    historico_conversa.append({"role": "user", "content": texto})

    gui_jarvis.mudar_estado('thinking')
    print("🧠 Pensando...", end="", flush=True)
    
    # Logs decorativos de processamento (em thread separada)
    threading.Thread(target=gui_jarvis.enviar_logs_processamento, daemon=True).start()
    
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
            gui_jarvis.enviar_log(f"⚙️ Executando: {comando_acao}", "action")
            resultado = executar_ferramenta_agente(comando_acao)
            print(f"⚙️  Resultado: {resultado}")
            gui_jarvis.enviar_log(f"✅ {resultado}", "result")
            
            historico_conversa.append({"role": "assistant", "content": resumo_ia})
            historico_conversa.append({"role": "user", "content": f"OBSERVATION: {resultado}"})
            
            final_gen = perguntar_ia(
                f"O resultado da ação foi: '{resultado}'. Repita essa informação de forma natural e curtíssima para o usuário. NÃO invente dados, use exatamente o que o resultado disse. NÃO inclua 'ACTION:' na sua resposta.",
                historico_conversa
            )
            final_texto = limpar_action_do_texto("".join(list(final_gen)))
            falar(final_texto)
            historico_conversa.append({"role": "assistant", "content": final_texto})
        else:
            falar(limpar_action_do_texto(resumo_ia))
            historico_conversa.append({"role": "assistant", "content": resumo_ia})
    else:
        resposta_limpa = limpar_action_do_texto(resumo_ia)
        gui_jarvis.enviar_log(f"💬 Resposta gerada", "info")
        falar(resposta_limpa)
        historico_conversa.append({"role": "assistant", "content": resumo_ia})

    if len(historico_conversa) > 10:
        historico_conversa = historico_conversa[-10:]

    return True

# ============================================================
#   MAIN
# ============================================================

def main():
    global ATIVO, detector
    os.system("cls" if os.name == "nt" else "clear")
    print("█" * 55)
    print(f"  O.L.A  B.I  —  ALWAYS LISTENING (RTX 3080)")
    print("█" * 55)

    print("📡 Verificando núcleo de IA...", end="", flush=True)
    if not verificar_ollama(tentativas=5):
        print("\n⚠️  Erro: Ollama não detectado. O Bi funcionará apenas com comandos diretos.")
    else:
        print(" [OK]")
    
    detector = DetectorPalmas(callback=ao_detectar_palmas, limiar=0.35)
    try: detector.iniciar()
    except: pass

    falar(random.choice(SAUDACOES))
    gui_jarvis.rodar_interface_assincrona()

    while ATIVO:
        if aguardar_wake_word():
            # Pausa palmas para evitar falso positivo com voz alta
            if detector:
                detector.pausar()
            
            print("🎤 Ouvindo comando...")
            comando = ouvir_microfone(timeout=5, phrase_time_limit=10)
            if comando:
                print(f"👤 Senhor: {comando}")
                ATIVO = processar_comando(comando)
                gui_jarvis.mudar_estado('idle')
                gui_jarvis.enviar_log("💤 Modo espera", "info")
            else:
                print("💤 Voltando para modo de espera.")
                gui_jarvis.mudar_estado('idle')
            
            # Retoma palmas após processamento
            if detector:
                detector.retomar()

    if detector:
        detector.parar()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        sys.exit(0)
