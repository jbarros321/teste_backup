# ============================================================
#   JARVIS AGENT — ia_local.py
#   Interface com o Ollama (LLM rodando localmente)
# ============================================================

import requests
import json
from config import OLLAMA_MODEL, OLLAMA_URL, AGENTE_NOME
from acoes import TOOLS_CONFIG
from memoria import formatar_memoria_para_prompt

# Personalidade do Jarvis + Instruções de Ferramentas
def obter_prompt_sistema():
    memoria_texto = formatar_memoria_para_prompt()
    
    prompt = f"""Você é o {AGENTE_NOME}, o assistente pessoal do Sr. Jonatan.
Você é inspirado no JARVIS: formal, britânico, eficiente e direto.

REGRAS CRÍTICAS DE COMANDO:
1. MÚSICA: Se o usuário pedir para "tocar", "ouvir" ou mencionar uma música/artista, você DEVE usar obrigatoriamente: "ACTION: tocar_spotify(musica='NOME DA MUSICA')".
   - Nunca use 'abrir_app' para música. Use sempre 'tocar_spotify'.
   - Mesmo que o nome da música pareça uma frase (ex: "Parece Caro"), ignore o sentido da frase e APENAS TOQUE A MÚSICA. Não dê conselhos!
2. IDIOMA: Responda SEMPRE em Português-BR.
3. ESTILO: Chame o usuário de "Senhor" ou "Sir".
4. SEXTA-FEIRA: Se o usuário disser "sexta feira" (ou variações como "sextou"), você DEVE usar a ferramenta "ACTION: comemorar_sexta()".

MEMÓRIAS: {memoria_texto}

FERRAMENTAS:
{json.dumps(TOOLS_CONFIG, indent=2, ensure_ascii=False)}

EXEMPLO 1:
User: "Jarvis, tocar parece caro do MD Chef"
Assistant: "Imediatamente, senhor. Preparando a reprodução de MD Chef. ACTION: tocar_spotify(musica='Parece Caro MD Chef')"

EXEMPLO 2:
User: "Jarvis, sexta feira!"
Assistant: "Com certeza, senhor! Preparando as comemorações. Sextou! ACTION: comemorar_sexta()"
"""
    return prompt

def perguntar_ia(pergunta: str, historico: list = None):
    """
    Envia pergunta ao Ollama usando a Chat API com suporte a Streaming.
    Retorna um gerador de strings.
    """
    try:
        messages = [{"role": "system", "content": obter_prompt_sistema()}]
        
        if historico:
            for msg in historico[-8:]: # Reduzido para 8 para maior velocidade
                messages.append(msg)
        
        if not historico or (historico and historico[-1]["content"] != pergunta):
             messages.append({"role": "user", "content": pergunta})

        resp = requests.post(
            OLLAMA_URL,
            json={
                "model": OLLAMA_MODEL,
                "messages": messages,
                "stream": True, # Ativa streaming
                "keep_alive": -1, # Mantém o modelo na memória da GPU indefinidamente (0 loading delay)
                "options": {
                    "temperature": 0.4, # Mais focado e rápido
                    "num_ctx": 2048,    # Suficiente para 3080 processar rápido
                }
            },
            timeout=90,
            stream=True
        )

        if resp.status_code == 200:
            for line in resp.iter_lines():
                if line:
                    chunk = json.loads(line)
                    content = chunk.get("message", {}).get("content", "")
                    if content:
                        yield content
        else:
            yield f"Erro {resp.status_code} da IA, senhor."

    except requests.ConnectionError:
        yield "Não consegui contato com meu núcleo de IA, senhor. Verifique se o Ollama está rodando."
    except Exception as e:
        yield f"Erro inesperado no módulo de IA: {e}"

def verificar_ollama() -> bool:
    """Verifica se o Ollama está rodando."""
    try:
        # Ponto de checagem da API base
        base_url = OLLAMA_URL.replace("/api/chat", "")
        resp = requests.get(base_url, timeout=3)
        return resp.status_code == 200
    except:
        return False
