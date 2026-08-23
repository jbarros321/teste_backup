# -*- coding: utf-8 -*-
"""
Simulador da logica de calculo de STP_PARCELARFINA_SATIS.
Porta fiel de PARCELA.SQL (ORIGINAL) e STP_PARCELARFINA_SATIS.sql (CORRIGIDA).
Aritmetica com Decimal + ROUND_HALF_UP para reproduzir ROUND/TRUNC do Oracle.
"""
from decimal import Decimal, ROUND_HALF_UP, ROUND_DOWN
from datetime import date, timedelta
import re

D = Decimal


def ora_round(v, casas=2):
    if v is None:
        return None
    return D(v).quantize(D(1).scaleb(-casas), rounding=ROUND_HALF_UP)


def ora_trunc(v, casas=2):
    return D(v).quantize(D(1).scaleb(-casas), rounding=ROUND_DOWN)


def brl(v):
    if v is None:
        return "NULL"
    s = f"{D(v):,.2f}"
    return s.replace(",", "@").replace(".", ",").replace("@", ".")


class AbortouComErro(Exception):
    pass


# ---------------------------------------------------------------- ORIGINAL
def original_automatico(vlr_titulo, n, juros, forma, taxa, base_venc, rng):
    """PARCELA.SQL linhas 59-183. juros = valor lido do parametro (sempre None
    na pratica, porque a linha 48 le 'PARAM_P_JUROS' em vez de 'P_JUROS')."""
    total = D(vlr_titulo)
    if (juros or "N") == "S":
        if forma == "P":
            total = total + total * (D(taxa) / 100)
        elif forma == "V":
            total = total + D(taxa)
        else:
            total = None                       # CASE sem ELSE -> NULL
    if total is None:
        raise AbortouComErro("VLRDESDOB = NULL (CASE sem ELSE em P_FORMAJUROS)")

    # linha 108: RAISE_APPLICATION_ERROR de depuracao
    raise AbortouComErro(
        "ORA-20009 'Somente Titulos de Receitas nao baixados podem ser parcelados!' "
        "(linha 108, RAISE de depuracao) - aborta antes de gravar")


def original_automatico_sem_raise(vlr_titulo, n, juros, forma, taxa, base_venc, rng):
    """Mesmo calculo, ignorando o RAISE da linha 108, para medir o rateio."""
    total = D(vlr_titulo)
    if (juros or "N") == "S":
        if forma == "P":
            total = total + total * (D(taxa) / 100)
        elif forma == "V":
            total = total + D(taxa)
    valor = ora_round(total / n)                # linha 110: mesmo valor p/ TODAS
    return [{"parc": i,
             "valor": valor,
             "venc": base_venc + timedelta(days=(i - 1) * rng)}
            for i in range(1, n + 1)]


def original_manual(vlr_titulo, n, lista, juros, forma, taxa, base_venc, rng,
                    dtneg_param, dtneg_original):
    """PARCELA.SQL linhas 186-373."""
    itens = [x for x in lista.split(";") if x]
    if len(itens) != n:
        raise AbortouComErro(f"ORA-20001 qtd informada ({len(itens)}) <> esperada ({n})")

    parcelas, soma = [], D(0)
    for it in itens:
        num = int(re.match(r"^[^=]+", it).group(0))
        raw = re.search(r"[^=]+$", it).group(0)
        raw = raw.replace(".", "").replace(",", ".")   # linha 208
        parcelas.append((num, D(raw)))
        soma += D(raw)

    if soma != D(vlr_titulo):
        raise AbortouComErro(f"ORA-20007 soma {brl(soma)} <> titulo {brl(vlr_titulo)}")
    for i, (num, _) in enumerate(parcelas, start=1):
        if num != i:
            raise AbortouComErro(f"ORA-20009 parcelas devem ser sequenciais - erro na parcela {num}")

    titulo_original = None
    novos = []
    for num, vlr in parcelas:
        v = vlr
        if (juros or "N") == "S":                       # linhas 295-308
            if forma == "P":
                v = v + v * (D(taxa) / 100)
            elif forma == "V":
                v = v + D(taxa)                         # somado a CADA parcela
            else:
                v = None
        dtneg = (dtneg_original if dtneg_param == "NO"
                 else date.today() if dtneg_param == "H" else None)
        venc = base_venc + timedelta(days=num * rng)    # linha 312: num, nao num-1

        # linha 315: UPDATE FORA do IF -> roda em TODA iteracao
        titulo_original = {"parc_rotulo": 1, "desdobramento": num,
                           "valor": v, "venc": venc, "dtneg": dtneg}
        if num > 1:                                     # linha 326
            novos.append({"parc": num, "valor": v, "venc": venc, "dtneg": dtneg})
    return titulo_original, novos


# ---------------------------------------------------------------- CORRIGIDA
def fn_num_br(txt):
    t = txt.strip().replace(" ", "").replace("R$", "")
    if "," in t:
        t = t.replace(".", "").replace(",", ".")
    if not re.match(r"^[0-9]+(\.[0-9]{1,6})?$", t):
        raise AbortouComErro(f"ORA-20115 VALOR DE PARCELA INVALIDO: \"{txt}\"")
    return D(t)


def fn_juros(base, aplica, forma, taxa):
    if aplica != "S":
        return D(0)
    if forma == "P":
        return ora_round(D(base) * (D(taxa) / 100))
    if forma == "V":
        return ora_round(D(taxa))
    return D(0)


def fn_vencimento(parc, base, rng, tipo, dia_util):
    if tipo == "M":
        m = base.month - 1 + (parc - 1)
        dt = date(base.year + m // 12, m % 12 + 1, min(base.day, 28))
    else:
        dt = base + timedelta(days=(parc - 1) * rng)
    if dia_util == "S":
        while dt.weekday() in (5, 6):
            dt += timedelta(days=1)
    return dt


def corrigida_automatico(vlr, n, aplica, forma, taxa, base, rng, tipo="D", du="N"):
    total = ora_round(D(vlr) + fn_juros(vlr, aplica, forma, taxa))
    parc = ora_trunc(total / n)
    if parc <= 0:
        raise AbortouComErro(f"ORA-20107 valor insuficiente para {n} parcelas")
    out, acum = [], D(0)
    for i in range(1, n + 1):
        v = parc if i < n else ora_round(total - acum)
        acum += parc
        out.append({"parc": i, "valor": v,
                    "venc": fn_vencimento(i, base, rng, tipo, du)})
    return out


def corrigida_manual(vlr, n, lista, aplica, forma, taxa, base, rng, tipo="D", du="N"):
    entrada, soma = {}, D(0)
    for it in [x.strip() for x in lista.split(";") if x.strip()]:
        if "=" not in it:
            raise AbortouComErro(f"ORA-20111 item {it} fora do formato")
        numtxt, vlrtxt = it.split("=", 1)
        numtxt = numtxt.strip()
        if not re.match(r"^[0-9]+$", numtxt):
            raise AbortouComErro(f"ORA-20114 NUMERO DE PARCELA INVALIDO: {numtxt}")
        num = int(numtxt)
        if num < 1:
            raise AbortouComErro("ORA-20114 parcela nao pode ser zero ou negativa")
        if num > n:
            raise AbortouComErro(f"ORA-20114 parcela {num} maior que {n}")
        if num in entrada:
            raise AbortouComErro(f"ORA-20114 parcela {num} informada mais de uma vez")
        entrada[num] = fn_num_br(vlrtxt)
        if entrada[num] <= 0:
            raise AbortouComErro(f"ORA-20115 valor da parcela {num} deve ser > 0")
        soma += entrada[num]

    if len(entrada) != n:
        raise AbortouComErro(f"ORA-20112 qtd informada ({len(entrada)}) <> esperada ({n})")
    for i in range(1, n + 1):
        if i not in entrada:
            raise AbortouComErro(f"ORA-20114 faltou a parcela {i}")
    if abs(ora_round(soma) - ora_round(D(vlr))) > D("0.005"):
        raise AbortouComErro(f"ORA-20113 soma {brl(soma)} <> titulo {brl(vlr)}")

    total = ora_round(soma + fn_juros(soma, aplica, forma, taxa))
    out, acum = [], D(0)
    for i in range(1, n + 1):
        v = ora_round(entrada[i] * total / soma) if i < n else ora_round(total - acum)
        acum += v
        out.append({"parc": i, "valor": v,
                    "venc": fn_vencimento(i, base, rng, tipo, du)})
    return out


# ---------------------------------------------------------------- RELATORIO
LARG = 78


def titulo(txt):
    print("\n" + "=" * LARG)
    print(txt)
    print("=" * LARG)


def mostra(rotulo, plano, esperado=None):
    if isinstance(plano, str):
        print(f"  {rotulo:<12} {plano}")
        return
    linhas = " | ".join(f"{p['parc']}:{brl(p['valor'])}" for p in plano)
    soma = sum(p["valor"] for p in plano)
    marca = ""
    if esperado is not None:
        marca = "  <-- OK" if soma == D(esperado) else f"  <-- ERRO (esperado {brl(D(esperado))})"
    print(f"  {rotulo:<12} {linhas}")
    print(f"  {'':<12} soma = {brl(soma)}{marca}")


def roda(fn, *a, **kw):
    try:
        return fn(*a, **kw)
    except AbortouComErro as e:
        return str(e)


BASE = date(2026, 9, 10)

titulo("CENARIO 1 - Rateio com dizima: R$ 100,00 em 3x, sem juros")
mostra("ORIGINAL", roda(original_automatico, "100.00", 3, None, None, 0, BASE, 30))
mostra("ORIGINAL*", roda(original_automatico_sem_raise, "100.00", 3, None, None, 0, BASE, 30), "100.00")
mostra("CORRIGIDA", roda(corrigida_automatico, "100.00", 3, "N", None, 0, BASE, 30), "100.00")
print("  * ignorando o RAISE da linha 108, so para medir o rateio")

titulo("CENARIO 2 - Juros percentual: R$ 1.000,00 em 2x, P 10%")
mostra("ORIGINAL", roda(original_automatico_sem_raise, "1000.00", 2, None, "P", 10, BASE, 30),
       "1000.00")
print("    ^ parametro lido como 'PARAM_P_JUROS' (linha 48) -> chega NULL -> sem juros")
mostra("CORRIGIDA", roda(corrigida_automatico, "1000.00", 2, "S", "P", 10, BASE, 30), "1100.00")

titulo("CENARIO 3 - Juros valor fixo: R$ 1.000,00 em 4x, V R$ 100,00 (modo manual)")
print("  (aqui o parametro de juros foi passado corrigido nas DUAS versoes, para")
print("   isolar o defeito de calculo do defeito de leitura do parametro)")
lista3 = "1=250,00;2=250,00;3=250,00;4=250,00"
orig3 = roda(original_manual, "1000.00", 4, lista3, "S", "V", 100, BASE, 30, "NO", date(2026, 8, 1))
if isinstance(orig3, str):
    mostra("ORIGINAL", orig3)
else:
    tit, novos = orig3
    todas = [{"parc": 1, "valor": tit["valor"]}] + [{"parc": p["parc"], "valor": p["valor"]} for p in novos]
    mostra("ORIGINAL", todas, "1100.00")
mostra("CORRIGIDA", roda(corrigida_manual, "1000.00", 4, lista3, "S", "V", 100, BASE, 30), "1100.00")

titulo("CENARIO 4 - Manual fora de ordem: R$ 1.000,00 -> 3=300;1=500;2=200")
lista4 = "3=300,00;1=500,00;2=200,00"
mostra("ORIGINAL", roda(original_manual, "1000.00", 3, lista4, None, None, 0, BASE, 30, "NO", date(2026, 8, 1)))
mostra("CORRIGIDA", roda(corrigida_manual, "1000.00", 3, lista4, "N", None, 0, BASE, 30), "1000.00")

titulo("CENARIO 5 - Estado final do titulo original (modo manual, em ordem)")
lista5 = "1=500,00;2=300,00;3=200,00"
tit, novos = original_manual("1000.00", 3, lista5, None, None, 0, BASE, 30, "NO", date(2026, 8, 1))
print("  ORIGINAL - o UPDATE roda em TODA iteracao (linha 315 fora do IF):")
print(f"    titulo NUFIN original -> HISTORICO='PARCELA {tit['parc_rotulo']}/3'"
      f"  DESDOBRAMENTO={tit['desdobramento']}"
      f"  VLRDESDOB={brl(tit['valor'])}  DTVENC={tit['venc']:%d/%m/%Y}")
for p in novos:
    print(f"    novo NUFIN            -> parcela {p['parc']}"
          f"  VLRDESDOB={brl(p['valor'])}  DTVENC={p['venc']:%d/%m/%Y}")
soma_orig = tit["valor"] + sum(p["valor"] for p in novos)
print(f"    soma gravada = {brl(soma_orig)}   (titulo era R$ 1.000,00)")
print("\n  CORRIGIDA:")
for p in corrigida_manual("1000.00", 3, lista5, "N", None, 0, BASE, 30):
    print(f"    parcela {p['parc']}  VLRDESDOB={brl(p['valor'])}  DTVENC={p['venc']:%d/%m/%Y}")

titulo("CENARIO 6 - Vencimentos: base 10/09/2026, intervalo 30 dias, 3 parcelas")
o = original_automatico_sem_raise("900.00", 3, None, None, 0, BASE, 30)
tit6, nov6 = original_manual("900.00", 3, "1=300,00;2=300,00;3=300,00", None, None, 0, BASE, 30, "NO", BASE)
print("  ORIGINAL automatico: " + "  ".join(f"{p['parc']}={p['venc']:%d/%m/%Y}" for p in o))
print("  ORIGINAL manual    : " + "  ".join(
    f"{p}={ (BASE + timedelta(days=p*30)):%d/%m/%Y}" for p in (1, 2, 3)) + "   <-- 1a parcela em base+30")
print("  CORRIGIDA          : " + "  ".join(
    f"{p['parc']}={p['venc']:%d/%m/%Y}" for p in corrigida_automatico("900.00", 3, "N", None, 0, BASE, 30)))

titulo("CENARIO 7 - Parsing de valor")
for txt in ["1500,00", "1.500,00", "1500.00", "R$ 1.234,56", "abc"]:
    o = txt.replace(".", "").replace(",", ".")
    try:
        vo = brl(D(o))
    except Exception:
        vo = "ORA-06502 (nao numerico)"
    try:
        vc = brl(fn_num_br(txt))
    except AbortouComErro as e:
        vc = str(e)
    print(f"  entrada {txt:<14} ORIGINAL -> {vo:<28} CORRIGIDA -> {vc}")

titulo("CENARIO 8 - Validacoes que a versao original nao faz")
casos = [
    ("Parcela faltando (1 e 3, N=3)", "1=500,00;3=500,00", "1000.00", 3),
    ("Parcela duplicada",             "1=500,00;1=500,00", "1000.00", 2),
    ("Soma divergente",               "1=400,00;2=400,00", "1000.00", 2),
    ("Valor negativo",                "1=1500,00;2=-500,00", "1000.00", 2),
]
for desc, lst, vlr, n in casos:
    r = roda(corrigida_manual, vlr, n, lst, "N", None, 0, BASE, 30)
    o = roda(original_manual, vlr, n, lst, None, None, 0, BASE, 30, "NO", BASE)
    om = o if isinstance(o, str) else "ACEITOU e gravou"
    print(f"  {desc:<32}\n      ORIGINAL  -> {om}\n      CORRIGIDA -> {r if isinstance(r, str) else 'ACEITOU'}")

titulo("CENARIO 9 - Divisao por zero (N nao informado)")
try:
    original_automatico_sem_raise("1000.00", 0, None, None, 0, BASE, 30)
except ZeroDivisionError:
    print("  ORIGINAL  -> ORA-01476 divisor is equal to zero")
print("  CORRIGIDA -> ORA-20101 O NUMERO DE PARCELAS DEVE SER MAIOR OU IGUAL A 2. (antes de gravar)")
print()
