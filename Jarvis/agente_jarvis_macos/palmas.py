# ============================================================
#   OLÁ BI AGENT — palmas.py
#   Detecta palmas em tempo real via microfone
# ============================================================

import sounddevice as sd
import numpy as np
import time
import threading
from config import PALMAS


class DetectorPalmas:
    """
    Detecta palmas em tempo real e dispara callbacks.
    Roda em thread separada sem travar o agente principal.
    """

    def __init__(self, callback, 
                 limiar=0.35,
                 intervalo_min=0.15,
                 janela_coleta=1.8,
                 taxa=44100):
        """
        callback     : função chamada com (n_palmas: int)
        limiar       : sensibilidade (0.0 a 1.0) — ajuste se detectar mal
        intervalo_min: tempo mínimo entre duas palmas (evita eco)
        janela_coleta: tempo de espera após a primeira palma
        taxa         : sample rate do microfone
        """
        self.callback = callback
        self.limiar = limiar
        self.intervalo_min = intervalo_min
        self.janela_coleta = janela_coleta
        self.taxa = taxa
        self.ativo = False
        self.pausado = False
        self._thread = None

    def iniciar(self):
        """Inicia a detecção em background."""
        self.ativo = True
        self._thread = threading.Thread(target=self._loop, daemon=True)
        self._thread.start()
        print("👏 Detector de palmas ativo.")

    def parar(self):
        """Para a detecção."""
        self.ativo = False

    def pausar(self):
        """Pausa a detecção (thread continua, mas ignora palmas)."""
        self.pausado = True
        print("👏 Detector de palmas pausado (modo escuta ativo).")

    def retomar(self):
        """Retoma a detecção de palmas."""
        self.pausado = False
        print("👏 Detector de palmas retomado.")

    def _loop(self):
        """Loop principal de detecção."""
        print("👂 Monitorando palmas em tempo real...")
        chunk = int(self.taxa * 0.05)  # Blocos de 50ms

        with sd.InputStream(samplerate=self.taxa, channels=1, 
                            dtype='float32', blocksize=chunk) as stream:
            ultimo_palma = 0
            coletando = False
            inicio_coleta = 0
            palmas_contadas = 0

            while self.ativo:
                data, _ = stream.read(chunk)
                amplitude = float(np.max(np.abs(data)))
                agora = time.time()

                # Se pausado, ignora detecções (evita voz alta = palma)
                if self.pausado:
                    continue

                # Detectou palma?
                if amplitude > self.limiar and (agora - ultimo_palma) > self.intervalo_min:
                    ultimo_palma = agora

                    if not coletando:
                        # Primeira palma → inicia janela de coleta
                        coletando = True
                        inicio_coleta = agora
                        palmas_contadas = 1
                        print(f"👏 Palma 1 detectada")
                    else:
                        palmas_contadas += 1
                        print(f"👏 Palma {palmas_contadas} detectada")

                # Janela de coleta expirou?
                if coletando and (agora - inicio_coleta) > self.janela_coleta:
                    if palmas_contadas >= 2 and palmas_contadas in PALMAS:
                        print(f"✅ {palmas_contadas} palmas! Executando ação...")
                        threading.Thread(
                            target=self.callback, 
                            args=(palmas_contadas,), 
                            daemon=True
                        ).start()
                    # Reset
                    coletando = False
                    palmas_contadas = 0


def testar_microfone(duracao=3):
    """Testa o microfone e mostra amplitude máxima."""
    print(f"🎤 Testando microfone por {duracao} segundos... Fale algo!")
    taxa = 44100
    audio = sd.rec(int(duracao * taxa), samplerate=taxa, channels=1, dtype='float32')
    sd.wait()
    amp_max = float(np.max(np.abs(audio)))
    print(f"   Amplitude máxima capturada: {amp_max:.3f}")
    if amp_max < 0.01:
        print("   ⚠️  Microfone muito baixo ou não encontrado!")
    elif amp_max > 0.5:
        print("   ✅ Microfone funcionando bem!")
    else:
        print("   ✅ Microfone detectado.")
    return amp_max


if __name__ == "__main__":
    print("=== Teste de Detecção de Palmas ===")
    testar_microfone()
    print("\nBata palmas para testar (Ctrl+C para sair):\n")

    def meu_callback(n):
        print(f"🎯 CALLBACK: {n} palmas detectadas!")

    detector = DetectorPalmas(meu_callback)
    detector.iniciar()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        detector.parar()
        print("\nTeste encerrado.")
