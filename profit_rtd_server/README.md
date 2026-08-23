# Integrador Profit Pro DDE Websocket

Este minisservidor conecta-se ao **Profit Pro** (Nelogica) através da tecnologia **DDE** e expõe os dados de índice (WIN) e dólar (WDO) via WebSockets na sua rede local para que o frontend React os consuma em tempo real.

## Requisitos
* Windows (O Profit Pro e o servidor Python precisam rodar na mesma máquina Windows).
* Profit Pro Aberto e logado.
* Python 3.9+ instalado no seu Windows.

## Instalação e Execução

1. Abra o `Prompt de Comando` ou `PowerShell` dentro desta pasta no seu Windows.
2. Crie e ative um ambiente virtual (opcional):
   ```bash
   python -m venv venv
   .\venv\Scripts\activate
   ```
3. Instale as bibliotecas necessárias:
   ```bash
   pip install -r requirements.txt
   ```
4. Execute o servidor:
   ```bash
   python server.py
   ```
5. Você deverá ver a mensagem `"Conectado ao DDE do Profit Pro!"` no console.

## Como o React acha os dados?
Ao rodar este script, ele criará um servidor WebSocket local (`ws://127.0.0.1:8080/ws`).
O frontend feito em React estará configurado para buscar os dados direto desse IP e porta. Se o React e o Profit estiverem em máquinas diferentes (Ex: React no Mac e Profit na VM Windows), você deve trocar no React o endereço `127.0.0.1` pelo IP de rede da sua Máquina Virtual Windows.
