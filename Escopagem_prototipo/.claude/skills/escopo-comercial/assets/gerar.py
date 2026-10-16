"""
Gera os dois entregaveis de um escopo comercial: Word e PDF, com a identidade
visual Neuon (paleta e tipografia de https://neuonagentes.netlify.app/).

    python gerar.py <caminho_do_md>

Produz, ao lado do .md:
    <nome>.docx   via pandoc + reference-doc da marca
    <nome>.pdf    via pandoc -> HTML com neuon.css -> Chrome/Edge headless

Nao precisa de LaTeX nem wkhtmltopdf. Nao usa --toc.
"""
import os
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile

AQUI = os.path.dirname(os.path.abspath(__file__))
CSS = os.path.join(AQUI, "neuon.css")
REF_DOCX = os.path.join(AQUI, "neuon-reference.docx")

PRIMARY = "2563EB"
TEXT = "0F172A"
TEXT_2 = "475569"
BORDER = "E2E8F0"
FONTE = "Inter"


def acha(nomes, extras=()):
    for n in nomes:
        p = shutil.which(n)
        if p:
            return p
    for p in extras:
        if os.path.isfile(p):
            return p
    return None


def pandoc():
    p = acha(["pandoc"], [
        os.path.expandvars(r"%LOCALAPPDATA%\Pandoc\pandoc.exe"),
        r"C:\Program Files\Pandoc\pandoc.exe",
    ])
    if not p:
        sys.exit("pandoc nao encontrado. Instale: winget install --id "
                 "JohnMacFarlane.Pandoc -e --accept-source-agreements "
                 "--accept-package-agreements")
    return p


def navegador():
    p = acha(["chrome", "msedge"], [
        r"C:\Program Files\Google\Chrome\Application\chrome.exe",
        r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
        r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
        r"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
    ])
    if not p:
        sys.exit("Chrome ou Edge nao encontrado - necessario para gerar o PDF.")
    return p


# --------------------------------------------------------------------------
# reference.docx da marca: parte do default do pandoc e troca fonte e cores
# --------------------------------------------------------------------------
def monta_reference(pd):
    if os.path.isfile(REF_DOCX):
        return REF_DOCX
    bruto = os.path.join(tempfile.mkdtemp(), "ref.docx")
    with open(bruto, "wb") as f:
        f.write(subprocess.run([pd, "--print-default-data-file", "reference.docx"],
                               capture_output=True, check=True).stdout)

    tmp = tempfile.mkdtemp()
    with zipfile.ZipFile(bruto) as z:
        z.extractall(tmp)

    sp = os.path.join(tmp, "word", "styles.xml")
    with open(sp, encoding="utf-8") as f:
        xml = f.read()

    # tipografia global
    xml = re.sub(r'w:ascii="[^"]*"', f'w:ascii="{FONTE}"', xml)
    xml = re.sub(r'w:hAnsi="[^"]*"', f'w:hAnsi="{FONTE}"', xml)
    xml = re.sub(r'w:cs="[^"]*"', f'w:cs="{FONTE}"', xml)

    # titulos na cor da marca
    def pinta(m):
        bloco = m.group(0)
        if re.search(r'w:styleId="(Heading[1-6]|Title|Subtitle)"', bloco):
            if "<w:color " in bloco:
                bloco = re.sub(r'<w:color w:val="[^"]*"', f'<w:color w:val="{PRIMARY}"', bloco)
            else:
                bloco = bloco.replace("<w:rPr>", f'<w:rPr><w:color w:val="{PRIMARY}"/>', 1)
        return bloco

    xml = re.sub(r'<w:style [^>]*>.*?</w:style>', pinta, xml, flags=re.S)
    with open(sp, "w", encoding="utf-8") as f:
        f.write(xml)

    with zipfile.ZipFile(REF_DOCX, "w", zipfile.ZIP_DEFLATED) as z:
        for raiz, _, arqs in os.walk(tmp):
            for a in arqs:
                cheio = os.path.join(raiz, a)
                z.write(cheio, os.path.relpath(cheio, tmp).replace("\\", "/"))
    return REF_DOCX


def gera(md):
    md = os.path.abspath(md)
    if not os.path.isfile(md):
        sys.exit(f"nao encontrei {md}")
    raiz = os.path.splitext(md)[0]
    docx, pdf = raiz + ".docx", raiz + ".pdf"
    pd = pandoc()

    for alvo in (docx, pdf):
        lock = os.path.join(os.path.dirname(alvo), "~$" + os.path.basename(alvo)[2:])
        if os.path.exists(lock):
            sys.exit(f"{os.path.basename(alvo)} esta aberto no Word. Feche e rode de novo.")

    # ---- Word
    subprocess.run([pd, md, "-o", docx, f"--reference-doc={monta_reference(pd)}"],
                   check=True)

    # ---- PDF
    tmp = tempfile.mkdtemp()
    html = os.path.join(tmp, "doc.html")
    shutil.copy(CSS, os.path.join(tmp, "neuon.css"))
    subprocess.run([pd, md, "-s", "--metadata", "title=Escopo Neuon",
                    "-c", "neuon.css", "-o", html], check=True)

    # Inter do Google Fonts, com fallback local
    with open(html, encoding="utf-8") as f:
        h = f.read()
    h = h.replace("</head>",
                  '<link rel="preconnect" href="https://fonts.googleapis.com">'
                  '<link href="https://fonts.googleapis.com/css2?family=Inter:'
                  'wght@400;500;600;700&display=swap" rel="stylesheet"></head>')
    with open(html, "w", encoding="utf-8") as f:
        f.write(h)

    subprocess.run([navegador(), "--headless", "--disable-gpu", "--no-sandbox",
                    "--no-pdf-header-footer", "--virtual-time-budget=10000",
                    f"--print-to-pdf={pdf}", "file:///" + html.replace("\\", "/")],
                   check=True, capture_output=True)

    for a in (docx, pdf):
        if not os.path.isfile(a):
            sys.exit(f"falhou ao gerar {a}")
        print(f"OK {os.path.basename(a)}  {os.path.getsize(a):,} bytes")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    gera(sys.argv[1])
