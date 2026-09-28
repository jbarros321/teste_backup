// Leitor de cifra em texto (acordes em cima da letra) -> formato das músicas do app.
// Mesmo formato do extrator da apostila (data/parse2.py): {s: seção} | {c: [[coluna, acorde]...], l: letra}
const Cifra = (() => {
  const CH = String.raw`[A-G](?:#|b)?(?:maj|dim|aug|sus|add|m|M|[0-9]|\+|-|º|°|ø|\(|\)|#|b|/(?=[0-9#b+-]))*(?:/[A-G](?:#|b)?)?`;
  const CHRE = new RegExp('^' + CH + '$');
  const IGN = /^[()|\-–.:/]*$|^\(?(x\d+|\d+x|\d+ª\s*vez|\d+ªvez)\)?$/i;
  const SEC = /^(Intro|Refr[aã]o|Ponte|Final|Solo|Pr[eé]-?refr[aã]o|Verso|Parte|Primeira|Segunda|Terceira|Interl[uú]dio|Coro|Estrofe|Tag|Base|Riff)/i;
  function clean(t) {
    t = t.replace(/^[,;]+|[,;]+$/g, '');
    const cnt = (s, ch) => s.split(ch).length - 1;
    while (t.startsWith('(') && cnt(t, '(') > cnt(t, ')')) t = t.slice(1);
    while (t.endsWith(')') && cnt(t, ')') > cnt(t, '(')) t = t.slice(0, -1);
    if (t.startsWith('(') && t.endsWith(')') && cnt(t, '(') === 1) t = t.slice(1, -1);
    return t;
  }
  const isChord = t => CHRE.test(t);
  // retorna null se não for linha de acordes; senão {lab, cs}
  function chordLine(line) {
    let s = line, lab = null, off = 0;
    const m = /^(\s*\[([^\]]+)\]\s*)(.*)$/.exec(s);
    if (m) { lab = m[2].trim(); off = m[1].length; s = m[3]; }
    const cs = [];
    for (const mm of s.matchAll(/\S+/g)) {
      const t = mm[0];
      if (IGN.test(t)) continue;
      const c = clean(t);
      if (c && isChord(c)) cs.push([mm.index + off, c]); else return null;
    }
    return { lab, cs };
  }
  function parse(text) {
    const src = text.replace(/\r/g, '').replace(/\t/g, '    ').split('\n');
    const out = [];
    for (let i = 0; i < src.length; i++) {
      const x = src[i].replace(/\s+$/, '');
      if (!x.trim()) continue;
      const r = chordLine(x);
      if (r && r.cs.length) {
        if (r.lab) out.push({ s: r.lab });
        let lyr = '';
        const nx = src[i + 1];
        if (nx && nx.trim() && !chordLine(nx) && !/^\s*\[.*\]\s*$/.test(nx)) { lyr = nx.replace(/\s+$/, ''); i++; }
        out.push({ c: r.cs, l: lyr });
        continue;
      }
      if (r && r.lab) { out.push({ s: r.lab }); continue; }
      const st = x.trim();
      if (/^\[.*\]$/.test(st) || (st.length < 25 && SEC.test(st))) { out.push({ s: st.replace(/^\[|\]$/g, '').replace(/:$/, '') }); continue; }
      out.push({ c: [], l: x });
    }
    return out;
  }
  const chordCount = L => L.reduce((a, l) => a + (l.c ? l.c.length : 0), 0);
  return { parse, chordCount, isChord };
})();
