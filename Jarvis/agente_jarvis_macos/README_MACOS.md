# Bi Agent — macOS Edition 

Esta é a versão do Bi otimizada para macOS.

## Diferenças Principais
- **Abertura de Apps**: Usa o comando nativo `open -a`.
- **Controle de Volume**: Usa AppleScript (`osascript`) para precisão.
- **Encerramento de Apps**: Usa AppleScript para encerramento amigável e `pkill` como fallback.
- **Spotify**: Controle avançado via AppleScript (permite tocar faixas específicas sem simular teclado).
- **Prints de Tela**: Usa o utilitário nativo `screencapture`.

## Como Instalar no Mac

1. **Requisitos**:
   - Python 3.10+
   - [Ollama](https://ollama.com/) instalado e rodando.
   - Microfone habilitado.

2. **Instalar Dependências**:
   Abra o Terminal na pasta `agente_jarvis_macos` e rode:
   ```bash
   pip install -r requirements.txt
   ```

3. **Permissões**:
   Ao rodar pela primeira vez, o macOS solicitará permissões para:
   - Microfone (para ouvir você).
   - Acessibilidade (se usar funções de teclado/mouse do PyAutoGUI).
   - Automação (para controlar o Spotify e o Volume).

4. **Iniciar**:
   ```bash
   python agente.py
   ```

## Configuração
Edite o arquivo `config.py` se quiser adicionar novos aplicativos. No Mac, basta usar o nome que aparece na pasta `/Applications`.
