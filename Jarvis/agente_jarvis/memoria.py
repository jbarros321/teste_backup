import json
import os

MEMORIA_FILE = "memoria_jarvis.json"

def carregar_memoria():
    """Carrega os dados de memória do arquivo JSON."""
    if os.path.exists(MEMORIA_FILE):
        try:
            with open(MEMORIA_FILE, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception as e:
            print(f"Erro ao carregar memória: {e}")
    
    # Memória padrão se o arquivo não existir ou estiver corrompido
    return {
        "fatos": [],
        "contexto_usuario": {},
        "historico_resumo": "",
        "preferencias": {}
    }

def salvar_memoria(dados):
    """Salva os dados de memória no arquivo JSON."""
    try:
        with open(MEMORIA_FILE, "w", encoding="utf-8") as f:
            json.dump(dados, f, ensure_ascii=False, indent=4)
    except Exception as e:
        print(f"Erro ao salvar memória: {e}")

def adicionar_fato(fato):
    """Adiciona um novo fato à memória de longo prazo."""
    memoria = carregar_memoria()
    if fato not in memoria["fatos"]:
        memoria["fatos"].append(fato)
        salvar_memoria(memoria)
        return True
    return False

def obter_fatos():
    """Retorna a lista de fatos memorizados."""
    memoria = carregar_memoria()
    return memoria.get("fatos", [])

def formatar_memoria_para_prompt():
    """Gera uma string formatada dos fatos para injetar no System Prompt."""
    fatos = obter_fatos()
    if not fatos:
        return "Nenhum fato memorizado ainda."
    
    return "\n".join([f"- {fato}" for fato in fatos])
