# 🎙️ Guia de Instalação e Configuração: J.A.R.V.I.S Agent

Este documento contém todos os passos necessários para configurar o assistente Jarvis no seu computador, desde o ambiente Python até a automação de inicialização.

---

## 1. Pré-requisitos de Software

### 🐍 Python 3.12 (Recomendado)
O Jarvis foi otimizado para a versão 3.12 do Python.
- **Link:** [Download Python 3.12](https://www.python.org/downloads/windows/)
- **IMPORTANTE:** Durante a instalação, marque a caixa **"Add Python to PATH"**.

### 🤖 Ollama (Núcleo de IA)
O Ollama é responsável por processar a inteligência do Jarvis localmente.
- **Link:** [Download Ollama](https://ollama.com/download/windows)
- **Aceleração por GPU:** Para rodar o Ollama usando o processamento da sua placa de vídeo (GPU) NVIDIA ou AMD, siga o guia completo em [CONFIGURACAO_GPU.md](Jarvis/CONFIGURACAO_GPU.md).
- Após instalar, abra o terminal (CMD ou PowerShell) e baixe o modelo padrão:
  ```bash
  ollama pull llama3.2
  ```

---

## 2. Configuração do Projeto

### 📂 Estrutura de Pastas
Recomenda-se manter o projeto em uma pasta de fácil acesso, como `C:\Jarvis` ou em uma unidade secundária como `E:\Jarvis`.

### 📦 Instalação de Dependências
Abra o terminal na pasta `Jarvis/agente_jarvis` e execute:
```bash
pip install -r requirements.txt
```

*Nota: Se encontrar erros com o `pyaudio`, pode ser necessário instalar os "Build Tools for Visual Studio" ou baixar o arquivo `.whl` correspondente.*

---

## 3. Configuração de Voz e Áudio

### 🎤 Microfone
- Certifique-se de que o microfone padrão do Windows está funcionando.
- No arquivo `config.py`, você pode ajustar o idioma em `VOZ_IDIOMA = "pt-BR"`.

### 🔊 Sintetizador de Voz (Edge-TTS)
O Jarvis utiliza a tecnologia neural da Microsoft para uma voz realista. Certifique-se de ter conexão com a internet para a primeira geração dos áudios de sistema.

---

## 4. Como Rodar o Jarvis

### ▶️ Inicialização Manual
Você pode iniciar o Jarvis executando o arquivo em lotes:
- **Arquivo:** `iniciar_jarvis.bat`
- Este script verifica se o Ollama está rodando, inicia o servidor se necessário, e abre o agente.

### 🚀 Inicialização Automática (Com o Windows)
Para que o Jarvis inicie sozinho ao ligar o PC:
1. Pressione `Win + R`, digite `shell:startup` e dê Enter.
2. Crie um **Atalho** para o arquivo `iniciar_jarvis.bat` dentro dessa pasta.
3. (Opcional) Faça o mesmo para o **Ollama**.

---

## 5. Comandos de Voz Principais

- **"Jarvis"**: Ativa a escuta ativa (aguarda seu comando).
- **"Jarvis, pesquisar no Google por [termo]"**: Abre o navegador com a pesquisa.
- **"Jarvis, que horas são?"**: Informa a hora atual.
- **"Jarvis, tocar [música]"**: Abre o Spotify e inicia a reprodução.
- **"Jarvis, sair/encerrar"**: Desliga o agente.

---

## 6. Solução de Problemas

### "Não consegui contato com meu núcleo de IA"
- Verifique se o ícone do Ollama (lhama) aparece na barra de tarefas.
- Se não aparecer, execute o Ollama manualmente antes de abrir o Jarvis.
- O novo `iniciar_jarvis.bat` tenta resolver isso automaticamente esperando 10 segundos.

### O Jarvis não entende o que eu falo
- Verifique o `energy_threshold` em `agente.py` (aumente se houver muito ruído ambiente).
- Certifique-se de estar usando um microfone de boa qualidade.

---
*Documentação gerada para o Sr. Jonatan.*
