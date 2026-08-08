# ⚡ Guia de Configuração: Rodando a IA do Jarvis na Placa de Vídeo (GPU)

Este guia detalha o passo a passo completo para configurar o núcleo de Inteligência Artificial do Jarvis (Ollama) para rodar diretamente na sua **placa de vídeo (GPU)**. Rodar na GPU garante respostas quase instantâneas, liberando processamento do seu processador (CPU) para outras tarefas.

---

## 📋 Pré-requisitos e Versões Recomendadas

O **Ollama** detecta e acelera automaticamente o processamento em GPUs compatíveis. Certifique-se de atender aos requisitos abaixo:

### 🟢 Para Placas NVIDIA (Recomendado)
*   **Arquitetura:** Maxwell ou mais recente (Série GTX 900, GTX 1000/1600, RTX 2000, 3000, 4000 ou superior).
*   **Driver de Vídeo:** Versão **525.xx ou superior** (Game Ready ou Studio Driver).
*   **CUDA Toolkit:** O Ollama já possui suporte embutido ao CUDA, mas é altamente recomendável ter o **CUDA Toolkit v11.x ou v12.x** instalado no sistema.
    *   *Link para Download:* [NVIDIA CUDA Toolkit](https://developer.nvidia.com/cuda-downloads)

### 🔴 Para Placas AMD
*   **Arquitetura:** GCN 4ª geração (RX 500), Vega, RDNA 1/2/3 (RX 5000, 6000, 7000 ou superior).
*   **Driver de Vídeo:** AMD Software Adrenalin (versão mais atualizada).
*   **ROCm:** O Ollama possui suporte nativo a ROCm no Windows para placas RDNA.

### 💾 Memória de Vídeo (VRAM) Mínima
*   **Para Llama 3.2 3B (Modelo padrão do Jarvis):** Mínimo de **4 GB de VRAM** (Ex: GTX 1050 Ti, 1650, etc. Funcionará perfeitamente e totalmente na GPU).
*   **Para Llama 3 8B / Mistral 7B (Modelos maiores):** Mínimo de **6 GB a 8 GB de VRAM** (Ex: RTX 2060, 3060, 4060, etc.).

---

## 🛠️ Passo a Passo para Configuração

### Passo 1: Atualizar os Drivers da GPU
Antes de começar, certifique-se de que sua GPU está com os drivers mais recentes fornecidos pela fabricante.
1. Baixe o driver mais recente para seu sistema no site da NVIDIA ou AMD.
2. Realize uma instalação limpa e reinicie o computador.

### Passo 2: Instalar o Ollama
Se ainda não instalou o Ollama:
1. Acesse [ollama.com](https://ollama.com) e baixe a versão para Windows.
2. Execute o instalador `OllamaSetup.exe` e siga as instruções na tela.
3. Certifique-se de que o ícone do Ollama (um ícone cinza de lhama) está ativo na bandeja do Windows (próximo ao relógio).

### Passo 3: Baixar o Modelo Adequado
No prompt de comando (CMD) ou PowerShell, execute o comando para baixar o modelo `llama3.2` de 3 bilhões de parâmetros (configurado por padrão no Jarvis):
```bash
ollama pull llama3.2
```

---

## 🖥️ Como Verificar se a IA Está Usando a GPU

Para ter certeza absoluta de que o processamento está ocorrendo na placa de vídeo e não no processador, utilize os métodos abaixo:

### 1. Via Prompt de Comando (NVIDIA)
Abra o CMD e digite:
```bash
nvidia-smi
```
Isso exibirá uma tabela com as informações da sua placa. Preste atenção no seguinte durante uma pergunta ao Jarvis:
*   **GPU-Util:** Deve subir enquanto a IA está respondendo.
*   **Memory-Usage:** Deve mostrar um consumo estável de aproximadamente **2.0 GB a 2.5 GB** a mais do que o normal (referente ao modelo Llama 3.2 carregado em VRAM).

Se quiser monitorar em tempo real a cada 1 segundo, execute:
```bash
nvidia-smi -l 1
```

### 2. Através do Gerenciador de Tarefas do Windows
1. Pressione `Ctrl + Shift + Esc` para abrir o Gerenciador de Tarefas.
2. Vá para a aba **Desempenho** (Performance).
3. Selecione a sua placa de vídeo (**GPU 0** ou **GPU 1**).
4. Verifique os seguintes gráficos enquanto a IA processa uma resposta:
    *   **Memória de GPU dedicada (Dedicated GPU Memory):** Deve estar ocupada com o tamanho do modelo (~2.2 GB).
    *   **CUDA** ou **Compute_0** (Você pode mudar a categoria de um dos gráficos menores clicando no nome dele e escolhendo "CUDA"): Esse gráfico deve ter picos de uso de até 100% durante o processamento de texto.

### 3. Lendo os Logs do Ollama
O Ollama gera um arquivo de log detalhado sobre a detecção de hardware. 
1. Pressione `Win + R`, digite `%LOCALAPPDATA%\Ollama` e dê Enter.
2. Abra o arquivo `server.log` com o Bloco de Notas.
3. Procure por linhas semelhantes a esta para confirmar que a GPU foi localizada com sucesso:
   ```text
   [...] discovering GPU devices
   [...] found device CUDA
   [...] GPU vram: xxxx megabytes
   [...] total vram: xxxx megabytes
   ```

---

## ⚙️ Otimizações no Código do Jarvis para GPU

No código do Jarvis, já deixamos ativadas algumas configurações essenciais para extrair o máximo de desempenho da GPU. Elas estão localizadas no arquivo [ia_local.py](file:///e:/personalizacoes-main/personalizacoes-main/Jarvis/agente_jarvis/ia_local.py):

```python
resp = requests.post(
    OLLAMA_URL,
    json={
        "model": OLLAMA_MODEL,
        "messages": messages,
        "stream": True,        # Permite que o Jarvis fale enquanto gera a resposta (economia de tempo)
        "keep_alive": -1,      # Mantém o modelo na memória da GPU indefinidamente (evita carregar do zero a cada pergunta)
        "options": {
            "temperature": 0.4, # Deixa a IA mais direta e focada
            "num_ctx": 2048,    # Contexto reduzido de 2K tokens (limita consumo de VRAM e acelera respostas)
        }
    },
    ...
)
```

> [!TIP]
> O parâmetro `"keep_alive": -1` é de extrema importância. Ele faz com que o modelo fique carregado na memória VRAM da placa de vídeo. Assim, quando você chama "Bi", ele responde imediatamente sem o atraso de 5 a 10 segundos de leitura de disco para a memória.

---

## 🔌 Variáveis de Ambiente Úteis (Avançado)

Se o seu sistema possui configurações especiais de hardware, você pode adicionar **Variáveis de Ambiente** no Windows para controlar o Ollama:

1. Pressione `Win + S`, pesquise por "Editar as variáveis de ambiente do sistema" e clique.
2. Clique no botão **Variáveis de Ambiente...**
3. Em *Variáveis do sistema*, você pode criar as seguintes variáveis:

*   **`CUDA_VISIBLE_DEVICES`**: Se você tem mais de uma GPU (uma integrada da CPU e uma dedicada da NVIDIA, por exemplo), defina o valor como `0` para usar a primeira placa dedicada, ou `1` para a segunda. Isso evita que o Ollama tente rodar na GPU integrada fraca.
*   **`OLLAMA_NUM_PARALLEL`**: Controla quantas requisições podem ser feitas simultaneamente. O padrão é `1`. Se você for o único usuário no computador, mantenha em `1` para poupar VRAM.

---

## ❓ Solução de Problemas (Troubleshooting)

### 🔴 O modelo está respondendo muito lento (parecendo CPU)
1. Certifique-se de que o arquivo `server.log` do Ollama não contém mensagens como `failed to initialize CUDA` ou `falling back to CPU`.
2. Certifique-se de não estar sem VRAM livre. Se sua GPU tem 4GB e você tem jogos ou navegadores pesados abertos consumindo VRAM, o Ollama pode ser obrigado a dividir as camadas (layers) entre GPU e CPU, reduzindo drasticamente a velocidade.
3. Feche aplicativos pesados em segundo plano e reinicie o Ollama.

### 🔴 O Ollama não abre ou fecha sozinho
*   Verifique se o driver de vídeo está atualizado. Versões muito antigas de drivers não suportam as chamadas de CUDA modernas usadas pelo Ollama.
*   Caso tenha uma placa AMD ou Intel, verifique se a versão instalada do Ollama é a mais recente disponível no site deles, pois as correções para suporte de placas concorrentes são frequentes.

---
*Documentação criada para suporte na otimização do Jarvis do Sr. Jonatan.*
