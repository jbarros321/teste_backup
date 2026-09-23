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
# CONFIG
# =====================================================================
# Schema confirmado pelo mapa de FK de 23/09/2026:
#   DET_DEMONSTRATIVO_ANO            (ID, ID_DET_DEMONSTRATIVO, ANO?)
#   DET_DEMONSTRATIVO_REFERENCIA     (ID, ID_DET_DEMONSTRATIVO_ANO, REFERENCIA)
#   DET_DEMONSTRATIVO_ANO_CTACTB     (ID, ID_DET_DEMONSTRATIVO_ANO, PADRAO_CTACTB, SINAL)
#
# CAMINHO decide para onde as queries vao apontar. Rode o bloco A do
# 01_descobre_estrutura_nova.sql ANTES de escolher: o caminho errado faz a
# tela abrir zerada sem dar erro nenhum.
#
#   "LEGADO" -> _ANO -> _REFERENCIA -> _CTACTB   (vinculos ainda em _CTACTB)
#   "NOVO"   -> _ANO -> _ANO_CTACTB              (vinculos migrados)
CAMINHO = "LEGADO"
# LEGADO e o que o queri1.sql (BP Externo, ja em producao) adotou, e mantem as 35
# regras de exclusao funcionando. Para trocar para NOVO, rode antes o
# 05_migra_exclusoes.sql - senao as 35 regras somem em silencio.

TABELA_ANO = "DET_DEMONSTRATIVO_ANO"
COL_ANO = "ANO"                    # confirmado: DET_DEMONSTRATIVO_ANO(ID, ID_DET_DEMONSTRATIVO, ANO int)
FK_ANO_PARA_DET = "ID_DET_DEMONSTRATIVO"
FK_REF_PARA_ANO = "ID_DET_DEMONSTRATIVO_ANO"     # confirmado pela API do tenant_60677
ALIAS_ANO = "DETANO"
# =====================================================================

RAIZ = pathlib.Path(__file__).resolve().parent.parent      # .../Analytics/TECWAY
REF = "DET_DEMONSTRATIVO_REFERENCIA"

# ---------------------------------------------------------------------
# Os quatro jeitos que o encadeamento aparece nas queries da pasta.
# ---------------------------------------------------------------------

JOIN_KW = r"(?:INNER\s+|LEFT\s+|RIGHT\s+|CROSS\s+)?JOIN"

# A) JOIN <REF> <alias> ON <pai>.ID = <alias>.ID_DET_DEMONSTRATIVO
PADRAO_A = re.compile(
    r"(?P<join>" + JOIN_KW + r")\s+" + REF + r"\s+(?P<alias>\w+)\s+"
    r"ON\s+(?P<pai>\w+)\.ID\s*=\s*(?P=alias)\.ID_DET_DEMONSTRATIVO\b",
    re.IGNORECASE)

# A') o mesmo com o ON invertido: ON <alias>.ID_DET_DEMONSTRATIVO = <pai>.ID
PADRAO_A_INV = re.compile(
    r"(?P<join>" + JOIN_KW + r")\s+" + REF + r"\s+(?P<alias>\w+)\s+"
    r"ON\s+(?P=alias)\.ID_DET_DEMONSTRATIVO\s*=\s*(?P<pai>\w+)\.ID\b",
    re.IGNORECASE)

# B) subconsulta correlata: FROM <REF> <alias> ... WHERE <alias>.ID_DET_DEMONSTRATIVO = <pai>.ID
PADRAO_B = re.compile(
    r"FROM\s+" + REF + r"\s+(?P<alias>\w+)(?P<meio>\s+)"
    r"WHERE\s+(?P=alias)\.ID_DET_DEMONSTRATIVO\s*=\s*(?P<pai>\w+)\.ID\b",
    re.IGNORECASE)

# N) so no CAMINHO NOVO: o join do vinculo, que penduraava em _REFERENCIA
PADRAO_VINC = re.compile(
    r"(?P<join>" + JOIN_KW + r")\s+(?P<tabela>DET_DEMONSTRATIVO_CTACTB(?:_EXC)?)\s+"
    r"(?P<alias>\w+)\s+ON\s+(?:\w+\.ID\s*=\s*(?P=alias)\.ID_DET_DEMONSTRATIVO_REFERENCIA"
    r"|(?P=alias)\.ID_DET_DEMONSTRATIVO_REFERENCIA\s*=\s*\w+\.ID)\b",
    re.IGNORECASE)

# C) sentido inverso - sobe da referencia para a linha:
#    JOIN DET_DEMONSTRATIVO <d> ON <d>.ID = <r>.ID_DET_DEMONSTRATIVO
PADRAO_C = re.compile(
    r"(?P<join>" + JOIN_KW + r")\s+DET_DEMONSTRATIVO\s+(?P<dalias>\w+)\s+"
    r"ON\s+(?P=dalias)\.ID\s*=\s*(?P<ralias>\w+)\.ID_DET_DEMONSTRATIVO\b",
    re.IGNORECASE)


def patch(texto):
    """Devolve (texto_novo, lista_de_mudancas, lista_de_avisos)."""
    mudancas, avisos = [], []

    def desce(m):
        """A / A' - o join descia DET_DEMONSTRATIVO -> REFERENCIA."""
        alias, pai, join = m.group("alias"), m.group("pai"), m.group("join")
        mudancas.append(f"[A] {pai} -> {alias}: {TABELA_ANO} inserido como {ALIAS_ANO}")
        return (f"{join} {TABELA_ANO} {ALIAS_ANO} "
                f"ON {pai}.ID = {ALIAS_ANO}.{FK_ANO_PARA_DET}\n"
                f"    {join} {REF} {alias} "
                f"ON {ALIAS_ANO}.ID = {alias}.{FK_REF_PARA_ANO}")

    texto = PADRAO_A.sub(desce, texto)
    texto = PADRAO_A_INV.sub(desce, texto)

    if CAMINHO == "NOVO":
        # os vinculos passam a pendurar direto na tabela de ano;
        # _REFERENCIA sai do encadeamento.
        def liga_vinculo(m):
            tabela = m.group("tabela").upper()
            nova = ("DET_DEMONSTRATIVO_ANO_CTACTB_EXC"
                    if tabela.endswith("_EXC") else "DET_DEMONSTRATIVO_ANO_CTACTB")
            mudancas.append(f"[N] {tabela} -> {nova}, pendurado em {ALIAS_ANO}")
            return (f"{m.group('join')} {nova} {m.group('alias')} "
                    f"ON {ALIAS_ANO}.ID = {m.group('alias')}.{FK_ANO_PARA_DET}_ANO"
                    .replace("ID_DET_DEMONSTRATIVO_ANO_ANO", "ID_DET_DEMONSTRATIVO_ANO"))

        texto = PADRAO_VINC.sub(liga_vinculo, texto)
        avisos.append("CAMINHO=NOVO: confira a mao se sobrou algum uso de "
                      "DET_DEMONSTRATIVO_REFERENCIA - ela sai do encadeamento")

    def sobe(m):
        """C - o join subia REFERENCIA -> DET_DEMONSTRATIVO."""
        join, dalias, ralias = m.group("join"), m.group("dalias"), m.group("ralias")
        alias_ano = ALIAS_ANO if ALIAS_ANO.upper() != dalias.upper() else ALIAS_ANO + "2"
        mudancas.append(f"[C] {ralias} -> {dalias}: {TABELA_ANO} inserido como {alias_ano}")
        return (f"{join} {TABELA_ANO} {alias_ano} "
                f"ON {alias_ano}.ID = {ralias}.{FK_REF_PARA_ANO}\n"
                f"    {join} DET_DEMONSTRATIVO {dalias} "
                f"ON {dalias}.ID = {alias_ano}.{FK_ANO_PARA_DET}")

    texto = PADRAO_C.sub(sobe, texto)

    def subconsulta(m):
        """B - a subconsulta da referencia vigente."""
        alias, pai = m.group("alias"), m.group("pai")
        mudancas.append(f"[B] subconsulta {alias}: {alias}.ID_DET_DEMONSTRATIVO = {pai}.ID"
                        f"  ->  {alias}.{FK_REF_PARA_ANO} = {ALIAS_ANO}.ID")
        return (f"FROM {REF} {alias}{m.group('meio')}"
                f"WHERE {alias}.{FK_REF_PARA_ANO} = {ALIAS_ANO}.ID")

    texto = PADRAO_B.sub(subconsulta, texto)

    # sobrou alguma referencia a coluna antiga? avisa e NAO mexe.
    for m in re.finditer(r"(\w+)\.ID_DET_DEMONSTRATIVO\b(?!_)", texto):
        if m.group(1).upper() not in (ALIAS_ANO.upper(), (ALIAS_ANO + "2").upper()):
            linha = texto[:m.start()].count("\n") + 1
            avisos.append(f"linha {linha}: {m.group(0)} ficou sem tratamento - revise a mao")

    # o filtro de ano vigente nao da para inserir com seguranca por regex
    if mudancas and COL_ANO.upper() not in texto.upper():
        avisos.append(f"falta o filtro de ano vigente ({ALIAS_ANO}.{COL_ANO} = "
                      f"(SELECT MAX(...))) - copie do 02_template_join_corrigido.sql. "
                      f"Sem ele cada conta entra uma vez por ano e os valores multiplicam")

    return texto, mudancas, avisos


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--aplicar", action="store_true",
                    help="grava as alteracoes (default: so mostra)")
    args = ap.parse_args()

    if CAMINHO not in ("LEGADO", "NOVO"):
        sys.exit('CAMINHO deve ser "LEGADO" ou "NOVO".')
    destino = ("DET_DEMONSTRATIVO_ANO_CTACTB" if CAMINHO == "NOVO"
               else "DET_DEMONSTRATIVO_REFERENCIA -> DET_DEMONSTRATIVO_CTACTB")
    print(f"CAMINHO={CAMINHO}: DET_DEMONSTRATIVO -> {TABELA_ANO}({COL_ANO}) -> {destino}")
    print("Rode o bloco A do 01_descobre_estrutura_nova.sql antes de --aplicar: "
          "o caminho errado abre a tela zerada, sem erro.\n")

    total_arquivos = total_mudancas = 0
    aqui = pathlib.Path(__file__).resolve().parent
    for caminho in sorted(RAIZ.rglob("*.sql")):
        if aqui in caminho.parents:
            continue          # nao mexe nos proprios arquivos desta pasta
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
