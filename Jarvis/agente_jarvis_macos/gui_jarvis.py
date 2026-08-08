import eel
import threading
import os
import time
import random

# Configuração da pasta web (aponta para a pasta onde criamos a interface)
# Como estamos dentro de Jarvis/agente_jarvis, voltamos um nível e entramos em sites/jarvis-gui
WEB_PATH = os.path.join(os.path.dirname(__file__), '..', '..', 'sites', 'jarvis-gui')

def iniciar_gui():
    """Inicializa a interface visual em uma janela do Chrome/Edge."""
    try:
        eel.init(WEB_PATH)
        eel.start('index.html', mode='chrome', size=(1024, 768))
    except Exception as e:
        print(f"Erro ao iniciar GUI: {e}")

def rodar_interface_assincrona():
    """Inicia a GUI em uma thread separada para não travar o agente."""
    t = threading.Thread(target=iniciar_gui, daemon=True)
    t.start()

def mudar_estado(estado):
    """
    Muda o estado visual da 'mente' do Bi.
    Estados: 'idle', 'listening', 'thinking', 'speaking'
    """
    try:
        eel.setState(estado)()
    except:
        pass

def enviar_log(mensagem: str, tipo: str = "info"):
    """
    Envia uma mensagem de log para a interface.
    Tipos: 'info', 'action', 'result', 'search', 'system'
    """
    try:
        eel.addLog(mensagem, tipo)()
    except:
        pass

def enviar_logs_processamento():
    """
    Envia logs decorativos de processamento para dar sensação de atividade.
    Roda em thread separada para não bloquear.
    """
    logs_ficticios = [
        ("🔗 Conectando ao núcleo neural...", "system"),
        ("📡 Verificando base de dados local...", "system"),
        (f"📊 Processando {random.randint(1200, 4800)} tokens...", "search"),
        ("🧬 Analisando contexto semântico...", "system"),
        (f"⚡ Latência: {random.randint(12, 85)}ms", "info"),
    ]
    for msg, tipo in random.sample(logs_ficticios, k=random.randint(2, 3)):
        enviar_log(msg, tipo)
        time.sleep(random.uniform(0.3, 0.7))

if __name__ == "__main__":
    print("Iniciando interface de teste...")
    iniciar_gui()
