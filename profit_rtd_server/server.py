import asyncio
import json
import logging
from aiohttp import web
from aiohttp.web import WebSocketResponse

# Para DDE no Windows
try:
    import win32ui
    import dde
except ImportError:
    print("Módulos pywin32 não encontrados. Esse script deve rodar no Windows (onde está o Profit).")
    print("Instale com: pip install pypiwin32 aiohttp")

logging.basicConfig(level=logging.INFO)

async def dde_market_data(ws: WebSocketResponse):
    # Setup do Cliente DDE
    server = dde.CreateServer()
    server.Create("PythonReactFluxoClient")
    conversation = dde.CreateConversation(server)
    
    # "profit" é a aplicação, "cot" é o tópico para cotações
    try:
        conversation.ConnectTo("profit", "cot")
        logging.info("Conectado ao DDE do Profit Pro!")
    except Exception as e:
        logging.error(f"Não foi possível conectar ao Profit: {e}")
        await ws.send_json({"error": "Profit Pro não encontrado ou fechado."})
        return

    # Símbolos que vamos monitorar (padrão atual futuro)
    symbols = ["WIN@", "WDO@"]
    
    while not ws.closed:
        data_payload = {}
        
        for sym in symbols:
            try:
                # Requisita dados do Profit via DDE
                # Formato no Profit: =profit|cot!WIN@.Ult
                ult = float(conversation.Request(f"{sym}.Ult").replace(",", "."))
                compra = float(conversation.Request(f"{sym}.Cpa").replace(",", "."))
                venda = float(conversation.Request(f"{sym}.Vda").replace(",", "."))
                var = float(conversation.Request(f"{sym}.Var").replace(",", "."))
                vol = float(conversation.Request(f"{sym}.Vol").replace(",", "."))
                
                # O Profit DDE puro geralmente não exporta "agressão" de tape sem criar colunas DDEs customizadas.
                # Vamos mandar o que temos real no topo do livro. O frontend fará lógica do tape baseado nestas viradas.
                
                data_payload[sym] = {
                    "price": ult,
                    "bid": compra,
                    "ask": venda,
                    "change": var,
                    "vol": vol
                }
            except Exception as e:
                pass # Pode falhar se o ativo não for encontrado no dde
        
        if data_payload:
            await ws.send_json({"type": "market_data", "data": data_payload})
        
        await asyncio.sleep(0.5) # Atualiza a cada 500ms

async def websocket_handler(request):
    ws = web.WebSocketResponse()
    await ws.prepare(request)
    
    logging.info("Frontend do Fluxo Pro Conectado!")
    
    await dde_market_data(ws)
        
    logging.info("Frontend desconectado.")
    return ws

app = web.Application()
app.add_routes([web.get('/ws', websocket_handler)])

if __name__ == '__main__':
    logging.info("Iniciando servidor de integração Profit <-> React na porta 8080...")
    web.run_app(app, port=8080)
