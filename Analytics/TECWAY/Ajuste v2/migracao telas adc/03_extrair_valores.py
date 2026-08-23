#!/usr/bin/env python3
"""
Extrai DRE_TECWAY e BP_TECWAY do projeto de origem (tenant_28263) via REST,
agrega por (MES, ID_EMPRESA, ID_CONTA_CONTABIL) e gera os INSERTs para o
projeto de destino (tenant_47255).

  python3 03_extrair_valores.py                 # todos os 52 meses (01/2022..04/2026)
  python3 03_extrair_valores.py 01/2024 12/2025 # só o intervalo informado

Saída: 04_dados_valores_DRE_TECWAY.sql e 04_dados_valores_BP_TECWAY.sql
       (+ .csv com o mesmo conteúdo, caso prefira importar por CSV)

Volume esperado no período completo:
  DRE_TECWAY ~  23 mil linhas agregadas (de 220.202 analíticas)
  BP_TECWAY  ~ 142 mil linhas agregadas (de 1.012.398 analíticas)
A extração completa faz ~1.700 chamadas REST; leve ~30 min. Ela é
retomável: já baixados ficam em cache/ e não são baixados de novo.
"""
import csv
import json
import os
import sys
import time
import urllib.error
import urllib.request
from collections import defaultdict

BASE = "https://analytics2.mitrasheet.com:4435/rest/v0"
# tenant_28263 - projeto "telas adc" (integra.md)
TOKEN_ORIGEM = (
    "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0dyIsIlgtVGVuYW50SUQiOiJ0ZW5hbnRfMjgyNjMifQ."
    "_FBCzUNY0fRszFTQAOfKAfhoz8p-iL88AxtnmyR4TPiAUqCYb_9wYvXnDm-3Cz3FRm5L9dzTcOuWXmekkEWdFg"
)

AQUI = os.path.dirname(os.path.abspath(__file__))
CACHE = os.path.join(AQUI, "cache")

MESES = [f"{m:02d}/{a}" for a in range(2022, 2027) for m in range(1, 13)]
MESES = MESES[: MESES.index("04/2026") + 1]

TABELAS = {
    # tabela: (coluna de valor, prefixos de conta esperados)
    "DRE_TECWAY": "DRE_TECWAY",
    "BP_TECWAY": "BP_TECWAY",
}


def get(path, tentativas=4):
    for i in range(tentativas):
        req = urllib.request.Request(
            f"{BASE}/{path}",
            headers={
                "Authorization": "Bearer " + TOKEN_ORIGEM,
                "Content-Type": "application/json",
            },
        )
        try:
            with urllib.request.urlopen(req, timeout=90) as r:
                return json.loads(r.read().decode("utf-8"))
        except Exception as exc:  # timeout / 5xx esporádico
            if i == tentativas - 1:
                raise
            time.sleep(2 * (i + 1))
            del exc


def baixar_mes(tabela, mes):
    os.makedirs(CACHE, exist_ok=True)
    destino = os.path.join(CACHE, f"{tabela}_{mes.replace('/', '-')}.json")
    if os.path.exists(destino):
        return json.load(open(destino))
    linhas, pagina = [], 0
    while True:
        d = get(f"{tabela}?MES={mes.replace('/', '%2F')}&page={pagina}&size=1000")
        c = d.get("content", [])
        linhas += c
        if len(c) < 1000:
            break
        pagina += 1
    json.dump(linhas, open(destino, "w"))
    return linhas


def sql_valor(v):
    if v is None:
        return "NULL"
    if isinstance(v, str):
        return "'" + v.replace("'", "''") + "'"
    return repr(v)


def gerar(tabela, meses):
    coluna = TABELAS[tabela]
    agregado = defaultdict(float)
    for mes in meses:
        linhas = baixar_mes(tabela, mes)
        for x in linhas:
            chave = (x["MES"], x["ID_EMPRESA"], x["ID_CONTA_CONTABIL"])
            agregado[chave] += x[coluna] or 0
        print(f"  {tabela} {mes}: {len(linhas):>6} linhas -> {len(agregado):>7} chaves")

    itens = sorted(agregado.items())
    cols = ["MES", "ID_EMPRESA", "ID_CONTA_CONTABIL", coluna]

    caminho_sql = os.path.join(AQUI, f"04_dados_valores_{tabela}.sql")
    with open(caminho_sql, "w", encoding="utf-8") as f:
        f.write(f"/* {tabela} agregada por (MES, ID_EMPRESA, ID_CONTA_CONTABIL)\n")
        f.write(f"   Origem: tenant_28263 | {len(itens)} linhas | meses: "
                f"{meses[0]} a {meses[-1]} */\n\n")
        f.write(f"DELETE FROM {tabela} WHERE MES IN ("
                + ", ".join(f"'{m}'" for m in meses) + ");\n\n")
        for i in range(0, len(itens), 500):
            bloco = itens[i:i + 500]
            f.write(f"INSERT INTO {tabela} (" + ", ".join(cols) + ") VALUES\n")
            f.write(",\n".join(
                "  (" + ", ".join(sql_valor(v) for v in (k[0], k[1], k[2], round(val, 6))) + ")"
                for k, val in bloco
            ) + ";\n\n")

    caminho_csv = os.path.join(AQUI, f"04_dados_valores_{tabela}.csv")
    with open(caminho_csv, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(cols)
        for k, val in itens:
            w.writerow([k[0], k[1], k[2], round(val, 6)])

    print(f"=> {caminho_sql} ({len(itens)} linhas)")
    print(f"=> {caminho_csv}")


if __name__ == "__main__":
    meses = MESES
    if len(sys.argv) == 3:
        meses = MESES[MESES.index(sys.argv[1]): MESES.index(sys.argv[2]) + 1]
    print(f"Extraindo {len(meses)} mes(es): {meses[0]} a {meses[-1]}")
    for tabela in TABELAS:
        gerar(tabela, meses)
