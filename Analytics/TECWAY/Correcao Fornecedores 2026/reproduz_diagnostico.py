#!/usr/bin/env python3
"""
Reproduz o diagnostico de 00_DIAGNOSTICO.md lendo o tenant pela API REST.

    python3 reproduz_diagnostico.py

Nao escreve nada - so le IMP_BASE_BALANCETE e as tabelas de estrutura.
Credenciais: Analytics/TECWAY/APIS.md (API 1 / tenant_47255).
"""
import collections
import json
import urllib.request

BASE = "https://analytics2.mitrasheet.com:4435/rest/v0/"
TOKEN = ("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBUEkgMSIsIlgtVGVuYW50SUQiOiJ0ZW5hbnRfNDcyNTUifQ"
         ".F0JpE1JJ90RZuruOS-zaW8Ipdx1VbzXeMZIlIz9Q6h9VuI3HfMigMHgIe1hQtkXiNIYhRtoYkWQjqRKw_Y6nkA")

CODEMP = 999                                   # consolidado
PREFIXOS = ("2.1.01.01.", "2.1.01.02.")        # Fornecedores no plano de contas
ALVO_TELA = 1_434_848.92                       # valor reclamado no BP Interno
ALVO_SANKHYA = 1_707_138.17                    # balancete do Sankhya em 21/09/2026


def fetch(table):
    rows, page = [], 0
    while True:
        req = urllib.request.Request(f"{BASE}{table}?page={page}&size=5000",
                                     headers={"Authorization": "Bearer " + TOKEN})
        chunk = json.load(urllib.request.urlopen(req, timeout=300)).get("content", [])
        rows += chunk
        if len(chunk) < 5000:
            return rows
        page += 1


def main():
    bal = fetch("IMP_BASE_BALANCETE")

    print("=" * 62)
    print("1) SALDO DE FORNECEDORES MES A MES  (CODEMP %d)" % CODEMP)
    print("=" * 62)
    mov = collections.defaultdict(float)
    for r in bal:
        if r["CODEMP"] == CODEMP and r["CTACTB"].startswith(PREFIXOS):
            mov[r["REFERENCIA"][:7]] += r["VLRLANC"]
    saldo = 0.0
    for competencia in sorted(mov):
        saldo += mov[competencia]
        if competencia >= "2025-09":
            print(f"  {competencia}   movimento {-mov[competencia]:>15,.2f}"
                  f"   saldo {-saldo:>15,.2f}")
    saldo_final = -saldo
    print()
    print(f"  saldo na ultima competencia carregada : {saldo_final:>15,.2f}")
    print(f"  valor reclamado na tela               : {ALVO_TELA:>15,.2f}"
          f"   {'<= BATE' if abs(saldo_final - ALVO_TELA) < 0.01 else '<= NAO bate'}")
    print(f"  balancete do Sankhya em 21/09/2026    : {ALVO_SANKHYA:>15,.2f}")
    print(f"  diferenca a explicar                  : {ALVO_SANKHYA - saldo_final:>15,.2f}")

    print()
    print("=" * 62)
    print("2) ATE QUANDO A CARGA DO BALANCETE FOI FEITA")
    print("=" * 62)
    por_mes = collections.Counter(r["REFERENCIA"][:7] for r in bal)
    for competencia in sorted(por_mes):
        if competencia >= "2025-09":
            print(f"  {competencia}   {por_mes[competencia]:>6} linhas")
    ultima = max(por_mes)
    print(f"\n  MAX(REFERENCIA) na tabela inteira: {ultima}")

    print()
    print("=" * 62)
    print("3) CONTAS DE FORNECEDOR SEM VINCULO NO BP INTERNO (ESTR 2)")
    print("=" * 62)
    vinc = fetch("DET_DEMONSTRATIVO_CTACTB")
    # referencia 82 = DET_DEMONSTRATIVO 42 (Fornecedores) em 2025-12-01
    mapeadas = {v["PADRAO_CTACTB"] for v in vinc
                if v["ID_DET_DEMONSTRATIVO_REFERENCIA"] == 82}
    saldo_conta = collections.defaultdict(float)
    contas = set()
    for r in bal:
        if r["CTACTB"].startswith(PREFIXOS):
            contas.add(r["CTACTB"])
            if r["CODEMP"] == CODEMP:
                saldo_conta[r["CTACTB"]] += r["VLRLANC"]
    faltando = sorted(contas - mapeadas)
    impacto = -sum(saldo_conta[c] for c in faltando)
    print(f"  contas de fornecedor no razao : {len(contas)}")
    print(f"  mapeadas na estrutura         : {len(mapeadas)}")
    print(f"  sem vinculo                   : {len(faltando)}")
    print(f"  impacto no CODEMP {CODEMP}         : {impacto:,.2f}")
    for c in faltando:
        print(f"     {c}  {-saldo_conta[c]:>12,.2f}")


if __name__ == "__main__":
    main()
