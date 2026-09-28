import sys,os
from pypdf import PdfReader
d="/Users/jonatanbarrosdejesus/Downloads/personalizacoes-main/ESTUDOS_PIANO"
os.makedirs("txt",exist_ok=True)
for f in sorted(os.listdir(d)):
    if not f.endswith(".pdf"): continue
    r=PdfReader(os.path.join(d,f))
    t="\n\n=====PAGE=====\n".join((p.extract_text() or "") for p in r.pages)
    open("txt/"+f[:-4]+".txt","w").write(t)
    print(len(r.pages), len(t), f)
