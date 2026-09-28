import re, json, collections
src=open("txt/Cifra Melodica Gospel +1309.txt").read().replace("\n\n=====PAGE=====\n","\n")
L=src.split("\n")
CH=r"[A-G](?:#|b)?(?:maj|dim|aug|sus|add|m|M|[0-9]|\+|-|º|°|ø|\(|\)|#|b|/(?=[0-9#b+-]))*(?:/[A-G](?:#|b)?)?"
CHRE=re.compile("^"+CH+"$")
def clean(t):
    t=t.strip(",;")
    while t.startswith("(") and t.count("(")>t.count(")"): t=t[1:]
    while t.endswith(")") and t.count(")")>t.count("("): t=t[:-1]
    if t.startswith("(") and t.endswith(")") and t.count("(")==1: t=t[1:-1]
    return t
def is_chord(t): return bool(CHRE.match(t))
IGN=re.compile(r"^[\(\)\|\-–\.:/]*$|^\(?(x\d+|\d+x|\d+ª\s*vez|\d+ªvez)\)?$",re.I)
def chord_line(line):
    s=line; lab=None; off=0
    m=re.match(r"^(\s*\[([^\]]+)\]\s*)(.*)$",s)
    if m: lab=m.group(2).strip(); off=len(m.group(1)); s=m.group(3)
    out=[]
    for mm in re.finditer(r"\S+",s):
        t=mm.group()
        if IGN.match(t): continue
        c=clean(t)
        if c and is_chord(c): out.append([mm.start()+off,c])
        else: return None
    return lab,out
tom_idx=[i for i,l in enumerate(L) if l.startswith("Tom/primeiro acorde:")]
songs=[]
for k,ti in enumerate(tom_idx):
    # header back
    j=ti-2; tparts=[]
    while j>ti-6 and not re.match(r"^\d{4}\b",L[j]): tparts.insert(0,L[j].strip()); j-=1
    hm=re.match(r"^(\d{4})\s*(.*)$",L[j]); num=int(hm.group(1)); title=" ".join([hm.group(2).strip()]+tparts).strip()
    tom=L[ti].split(":")[1].split()[0]
    end=(tom_idx[k+1]-2) if k+1<len(tom_idx) else len(L)
    # walk back end to the header line
    if k+1<len(tom_idx):
        e=tom_idx[k+1]-2
        while not re.match(r"^\d{4}\b",L[e]): e-=1
        end=e
    blk=L[ti+1:end]
    artist=""; b=0
    while b<len(blk) and b<3:
        if blk[b].startswith("Cifra adaptada"): b+=1; continue
        if " - " in blk[b]: artist=blk[b].split(" - ")[0].strip(); b+=1
        break
    try: h=blk.index("Harmonia no Teclado")
    except ValueError: h=len(blk)
    body=blk[b:h]; vblk=blk[h+1:]
    voic={}; cur=None
    for x in vblk:
        x=x.strip()
        if x.startswith("ME:") and cur: voic[cur]["me"]=x[3:].strip()
        elif x.startswith("MD:") and cur: voic[cur]["md"]=x[3:].split()
        elif x.startswith("Notas:") or not x: pass
        elif all(is_chord(t) for t in x.split()):
            nm=x.split(); cur=nm[0]; voic[cur]={}
            for a in nm[1:]: voic[a]=voic[cur]
    vset=set(voic)
    # glue fix
    fixed=[]
    for x in body:
        m=re.match(r"^("+CH+r")([A-ZÁÉÍÓÚÂÊÔ][a-záéíóúâêôãõç].*)$",x)
        if m and m.group(1) in vset: fixed+=[m.group(1),m.group(2)]; continue
        m=re.match(r"^(.*[a-záéíóúâêôãõç,!?.])("+CH+r")((?:\s+"+CH+r")*\s*)$",x)
        if m and m.group(2) in vset and chord_line(x) is None:
            pos=len(m.group(1)); fixed+=[" "*pos+m.group(2)+m.group(3), m.group(1)]; 
            # swap: chord line should precede? chord belongs at end of this lyric line -> emit lyric then chord-line for next
            fixed[-2],fixed[-1]=fixed[-1],fixed[-2]
            fixed[-2:]=[fixed[-2]]; fixed.append(" "*0+m.group(2)+m.group(3)) if False else None
            fixed[-1:]=[m.group(1)]
            fixed.append("§"+m.group(2)+m.group(3))
            continue
        toks=list(re.finditer(r"\S+",x)); done=False
        for ti2,mm in enumerate(toks):
            t=mm.group()
            if is_chord(clean(t)) or IGN.match(t): continue
            if ti2>0:
                m2=re.match(r"^("+CH+r")([A-ZÁÉÍÓÚÂÊÔ][a-záéíóúâêôãõç].*)$",t)
                if m2 and m2.group(1) in vset:
                    fixed.append(x[:mm.start()]+m2.group(1)); fixed.append(x[mm.start()+len(m2.group(1)):]); done=True
            break
        if not done: fixed.append(x)
    lines=[]; i=0
    while i<len(fixed):
        x=fixed[i]
        if x.startswith("§"):  # trailing chord glued -> attach to previous lyric line end
            cs=x[1:].split()
            if lines and "c" in lines[-1]:
                ln=len(lines[-1]["l"]); 
                for c in cs: lines[-1]["c"].append([ln,c]); ln+=len(c)+1
            i+=1; continue
        if not x.strip(): i+=1; continue
        r=chord_line(x)
        if r is not None and r[1]:
            lab,cs=r
            if lab: lines.append({"s":lab})
            lyr=""
            if i+1<len(fixed) and fixed[i+1].strip() and not fixed[i+1].startswith("§") and chord_line(fixed[i+1]) is None and not re.match(r"^\s*\[.*\]\s*$",fixed[i+1]):
                lyr=fixed[i+1].rstrip(); i+=1
            lines.append({"c":cs,"l":lyr}); i+=1; continue
        if r is not None and not r[1] and r[0]: lines.append({"s":r[0]}); i+=1; continue
        st=x.strip()
        if re.match(r"^\[.*\]$",st) or (len(st)<25 and re.match(r"^(Intro|Refr[aã]o|Ponte|Final|Solo|Pr[eé]-?refr[aã]o|Verso|Parte|Interl[uú]dio|Coro|Estrofe|Primeira|Segunda|Terceira|Tag|Base|Riff)",st,re.I)):
            lines.append({"s":st.strip("[]")}); i+=1; continue
        lines.append({"c":[],"l":x.rstrip()}); i+=1
    nch=sum(len(x.get("c",[])) for x in lines)
    if nch<2: continue
    vv={c:[v.get("me",""),v.get("md",[])] for c,v in voic.items()}
    songs.append({"n":num,"t":title.title(),"a":artist,"k":tom,"L":lines,"v":vv})
print(len(songs))
json.dump(songs,open("songs.json","w"),ensure_ascii=False,separators=(",",":"))
import os; print(os.path.getsize("songs.json"))
s=[x for x in songs if x["n"]==652][0]
for x in s["L"][:14]: print(x)
