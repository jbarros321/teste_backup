# ============================================================
#   OLÁ BI AGENT — ia_local.py
#   Interface com o Ollama (LLM rodando localmente)
# ============================================================

import requests
import json
import time
from config import OLLAMA_MODEL, OLLAMA_URL, AGENTE_NOME
from acoes import TOOLS_CONFIG
from memoria import formatar_memoria_para_prompt

# Personalidade do Bi + Instruções de Ferramentas
def obter_prompt_sistema():
    memoria_texto = formatar_memoria_para_prompt()
    
    prompt = f"""Você é o {AGENTE_NOME}, o assistente pessoal do Sr. Jonatan.
Você é um assistente de IA: formal, eficiente e direto.

REGRAS CRÍTICAS:
1. DUAS MODALIDADES DE RESPOSTA: Você deve identificar se o usuário quer um COMANDO DE SISTEMA ou uma EXPLICAÇÃO/RESUMO.
2. COMANDO DE SISTEMA: Quando o usuário pedir para FAZER algo no computador (abrir, pesquisar na web, tocar música, criar arquivo, fechar app), você DEVE responder com uma ACTION no final. (Ex: "Claro, senhor. ACTION: tocar_spotify(musica='NOME')")
3. PERGUNTAS GERAIS E EXPLICAÇÕES: Se o usuário pedir para EXPLICAR algo, RESUMIR, ou tirar uma DÚVIDA (ex: como melhorar dor de cabeça, o que é uma novela, etc), NÃO USE ACTION. Apenas responda à pergunta diretamente usando seu conhecimento com a sua personalidade.
4. MÚSICA: Use "ACTION: tocar_spotify(musica='NOME')"
5. PESQUISA NA WEB: Use "ACTION: pesquisar_google(busca='termo')"
6. NAVEGADOR/SITE: Use "ACTION: abrir_site(url='endereco.com')"
7. HORA/DATA: Use "ACTION: informacao_sistema(tipo='hora/data')"
8. SEXTA-FEIRA: Se disser "sextou", use "ACTION: comemorar_sexta()"
9. IDIOMA E ESTILO: Responda SEMPRE em Português-BR. Seja formal, mas prestativo. Chame o usuário de "Senhor" ou "Sir".

MEMÓRIAS: {memoria_texto}

FERRAMENTAS DISPONÍVEIS:
{json.dumps(TOOLS_CONFIG, indent=2, ensure_ascii=False)}

EXEMPLOS DE COMANDOS:
User: "Bi, pesquisa no Google sobre inteligência artificial"
Assistant: "Pesquisando agora, senhor. ACTION: pesquisar_google(busca='inteligência artificial')"

User: "Bi, que horas são?"
Assistant: "Verificando, senhor. ACTION: informacao_sistema(tipo='hora')"

User: "Bi, abre o YouTube e pesquisa lofi"
Assistant: "Abrindo YouTube, senhor. ACTION: abrir_youtube(busca='lofi')"

EXEMPLOS DE PERGUNTAS (SEM ACTION):
User: "Bi, me explique o que posso fazer para melhorar a dor de cabeça"
Assistant: "Senhor, para aliviar a dor de cabeça, recomendo que descanse em um ambiente escuro e silencioso, beba bastante água para se manter hidratado e, se necessário, tome um analgésico de sua preferência. Evite telas brilhantes."

User: "Bi, me explique o que é a novela carrossel"
Assistant: "Carrossel é uma famosa telenovela infantil. A história gira em torno de uma turma de crianças na escola e sua professora carinhosa, abordando temas de amizade, família e valores morais de forma lúdica."
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

def verificar_ollama(tentativas=1) -> bool:
    """Verifica se o Ollama está rodando, com opção de múltiplas tentativas."""
    base_url = OLLAMA_URL.replace("/api/chat", "")
    for i in range(tentativas):
        try:
            resp = requests.get(base_url, timeout=3)
            if resp.status_code == 200:
                return True
        except:
            pass
        if i < tentativas - 1:
            time.sleep(2)
    return False
