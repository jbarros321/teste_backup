#!/usr/bin/env python3
"""
Reescreve o encadeamento DET_DEMONSTRATIVO -> DET_DEMONSTRATIVO_REFERENCIA
para passar pela nova tabela de ano, em todos os .sql da pasta TECWAY.

    # 1. preencha o CONFIG abaixo com os nomes reais
    # 2. veja o que mudaria, sem gravar nada:
    python3 03_patch_queries.py
    # 3. grave (faz .bak de cada arquivo alterado):
    python3 03_patch_queries.py --aplicar

Cobre os dois padroes que quebram (ver 00_DIAGNOSTICO.md, item 5):
  A) JOIN direto  ->  erro "Unknown column '<alias>.ID_DET_DEMONSTRATIVO' in 'on clause'"
  B) subconsulta  ->  erro "Unknown column 'R.ID_DET_DEMONSTRATIVO' in 'where clause'"

Nao e magica: revise o diff. Onde o script nao tiver certeza ele avisa e
NAO mexe, para voce corrigir a mao.
"""
import argparse
import pathlib
import re
import sys

# =====================================================================
# CONFIG - preencher com os nomes reais antes de rodar
# =====================================================================
TABELA_ANO = "NOVA_TABELA_ANO"        # nome da tabela de intermediacao
COL_ANO = "ANO"                       # coluna do ano nela
FK_ANO_PARA_DET = "ID_DET_DEMONSTRATIVO"   # FK dela -> DET_DEMONSTRATIVO
FK_REF_PARA_ANO = "ID_NOVA_TABELA_ANO"     # FK de DET_DEMONSTRATIVO_REFERENCIA -> tabela de ano
ALIAS_ANO = "DETANO"                  # alias a usar nas queries reescritas
VAR_DATA = ":VAR_DATA_REF_DRE"        # variavel de data de corte usada nas queries
# =====================================================================

RAIZ = pathlib.Path(__file__).resolve().parent.parent      # .../Analytics/TECWAY
REF = "DET_DEMONSTRATIVO_REFERENCIA"

# A) JOIN <REF> <alias> ON <algo>.ID = <alias>.ID_DET_DEMONSTRATIVO
PADRAO_JOIN = re.compile(
    r"(?P<join>(?:INNER\s+|LEFT\s+|RIGHT\s+)?JOIN)\s+" + REF + r"\s+(?P<alias>\w+)\s+"
    r"ON\s+(?P<pai>\w+)\.ID\s*=\s*(?P=alias)\.ID_DET_DEMONSTRATIVO",
    re.IGNORECASE)

# B) FROM <REF> <alias> ... WHERE <alias>.ID_DET_DEMONSTRATIVO = <pai>.ID
PADRAO_SUB = re.compile(
    r"FROM\s+" + REF + r"\s+(?P<alias>\w+)\s+"
    r"WHERE\s+(?P=alias)\.ID_DET_DEMONSTRATIVO\s*=\s*(?P<pai>\w+)\.ID",
    re.IGNORECASE | re.DOTALL)


def patch(texto):
    """Devolve (texto_novo, lista_de_mudancas, lista_de_avisos)."""
    mudancas, avisos = [], []

    def troca_join(m):
        alias, pai = m.group("alias"), m.group("pai")
        novo = (f"{m.group('join')} {TABELA_ANO} {ALIAS_ANO} "
                f"ON {pai}.ID = {ALIAS_ANO}.{FK_ANO_PARA_DET}\n"
                f"  {m.group('join')} {REF} {alias} "
                f"ON {ALIAS_ANO}.ID = {alias}.{FK_REF_PARA_ANO}")
        mudancas.append(f"[A] join {pai} -> {alias}: inserido {TABELA_ANO} como {ALIAS_ANO}")
        return novo

    texto = PADRAO_JOIN.sub(troca_join, texto)

    def troca_sub(m):
        alias, pai = m.group("alias"), m.group("pai")
        # dentro da subconsulta o pai passa a ser o alias da tabela de ano
        novo = (f"FROM {REF} {alias} "
                f"WHERE {alias}.{FK_REF_PARA_ANO} = {ALIAS_ANO}.ID")
        mudancas.append(f"[B] subconsulta {alias}: {alias}.ID_DET_DEMONSTRATIVO = {pai}.ID"
                        f"  ->  {alias}.{FK_REF_PARA_ANO} = {ALIAS_ANO}.ID")
        return novo

    texto = PADRAO_SUB.sub(troca_sub, texto)

    # sobrou alguma referencia a coluna antiga? aviso, nao mexe.
    for m in re.finditer(r"(\w+)\.ID_DET_DEMONSTRATIVO\b", texto):
        if m.group(1).upper() != ALIAS_ANO.upper():
            linha = texto[:m.start()].count("\n") + 1
            avisos.append(f"linha {linha}: {m.group(0)} ficou sem tratamento - revise a mao")

    # o filtro de ano vigente nao da para inserir com seguranca por regex
    if mudancas and COL_ANO.upper() not in texto.upper():
        avisos.append(f"falta o filtro de ano vigente ({ALIAS_ANO}.{COL_ANO} = "
                      f"(SELECT MAX(...))) - copie do 02_template_join_corrigido.sql, "
                      f"senao cada conta entra uma vez por ano e os valores multiplicam")

    return texto, mudancas, avisos


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--aplicar", action="store_true",
                    help="grava as alteracoes (default: so mostra)")
    args = ap.parse_args()

    if TABELA_ANO == "NOVA_TABELA_ANO":
        sys.exit("Preencha o CONFIG no topo do script com os nomes reais antes de rodar.")

    total_arquivos = total_mudancas = 0
    for caminho in sorted(RAIZ.rglob("*.sql")):
        original = caminho.read_text(encoding="utf-8", errors="replace")
        if "ID_DET_DEMONSTRATIVO" not in original:
            continue
        novo, mudancas, avisos = patch(original)
        if not mudancas and not avisos:
            continue

        print(f"\n=== {caminho.relative_to(RAIZ)}")
        for c in mudancas:
            print("   ", c)
        for a in avisos:
            print("    AVISO:", a)

        if mudancas and novo != original:
            total_arquivos += 1
            total_mudancas += len(mudancas)
            if args.aplicar:
                caminho.with_suffix(caminho.suffix + ".bak").write_text(
                    original, encoding="utf-8")
                caminho.write_text(novo, encoding="utf-8")

    print(f"\n{total_mudancas} junção(ões) em {total_arquivos} arquivo(s).")
    print("Gravado (.bak criado)." if args.aplicar
          else "Nada gravado. Rode com --aplicar depois de revisar.")


if __name__ == "__main__":
    main()
