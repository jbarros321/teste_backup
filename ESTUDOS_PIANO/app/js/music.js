// Motor de teoria musical: notas, acordes, voicings, escalas e campo harmônico.
const M = (() => {
  const SHARP = ['C','C#','D','D#','E','F','F#','G','G#','A','A#','B'];
  const FLAT  = ['C','Db','D','Eb','E','F','Gb','G','Ab','A','Bb','B'];
  const PT    = ['Dó','Dó#','Ré','Ré#','Mi','Fá','Fá#','Sol','Sol#','Lá','Lá#','Si'];
  const PT_FLAT = ['Dó','Réb','Ré','Mib','Mi','Fá','Solb','Sol','Láb','Lá','Sib','Si'];
  const LETTER_PC = {C:0,D:2,E:4,F:5,G:7,A:9,B:11};
  const FLAT_KEYS = new Set([5,10,3,8,1]); // F Bb Eb Ab Db

  function pc(name) {
    const m = /^([A-G])([#b]*)/.exec(name);
    if (!m) return null;
    let v = LETTER_PC[m[1]];
    for (const ch of m[2]) v += ch === '#' ? 1 : -1;
    return (v + 120) % 12;
  }
  function noteName(p, flats) { return (flats ? FLAT : SHARP)[((p % 12) + 12) % 12]; }
  function ptName(p, flats) { return (flats ? PT_FLAT : PT)[((p % 12) + 12) % 12]; }
  function midiOf(s) { // "C#4" -> 61
    const m = /^([A-G][#b]*)(-?\d)$/.exec(s.trim());
    if (!m) return null;
    return pc(m[1]) + (parseInt(m[2]) + 1) * 12;
  }
  function midiName(n, flats) { return noteName(n % 12, flats) + (Math.floor(n / 12) - 1); }
  function freq(n) { return 440 * Math.pow(2, (n - 69) / 12); }

  // ---------- Acordes ----------
  const QUALITY_NAMES = {
    '': 'Maior', 'm': 'Menor', '7': 'Sétima (dominante)', '7M': 'Sétima maior', 'm7': 'Menor com sétima',
    'm7(b5)': 'Meio-diminuto', '°': 'Diminuto', '°7': 'Diminuto com sétima', '+': 'Aumentado',
    'sus4': 'Suspenso (4)', 'sus2': 'Suspenso (2)', '7sus4': 'Sétima com 4ª suspensa', 'add9': 'Com nona adicionada',
    'm(add9)': 'Menor com nona', '6': 'Sexta', 'm6': 'Menor com sexta', '9': 'Nona (dominante)', 'm9': 'Menor com nona e sétima',
    '7M(9)': 'Sétima maior com nona', '6(9)': 'Sexta e nona', 'm7(11)': 'Menor com sétima e 11ª', '7(13)': 'Sétima com 13ª',
    '7(b9)': 'Sétima com nona menor', '5': 'Power chord (quinta)'
  };
  // Tipos usados no dicionário
  const DICT_TYPES = ['', 'm', '7', '7M', 'm7', 'm7(b5)', '°', '°7', '+', 'sus4', 'sus2', '7sus4', 'add9', 'm(add9)', '6', 'm6', '7(9)', 'm9', '7M(9)', '6(9)', 'm7(11)', '7(13)', '7(b9)', '5'];

  function splitChord(sym) {
    sym = sym.trim();
    const m = /^([A-G][#b]?)(.*)$/.exec(sym);
    if (!m) return null;
    let rest = m[2], bass = null;
    const bm = /\/([A-G][#b]?)$/.exec(rest);
    if (bm) { bass = bm[1]; rest = rest.slice(0, bm.index); }
    return { root: m[1], suffix: rest, bass };
  }

  // Retorna { root, rootPc, bassPc, ints:Set de intervalos, third, fifth, has7, name }
  function parseChord(sym) {
    const sp = splitChord(sym);
    if (!sp) return null;
    const rootPc = pc(sp.root);
    let s = sp.suffix.replace(/[()]/g, ' ').replace(/\//g, ' ').replace(/\s+/g, ' ').trim();
    let third = 4, fifth = 7; const add = new Set();
    let minor = false, power = false;
    if (/^m(?!aj)/.test(s)) { minor = true; third = 3; s = s.slice(1); }
    else if (/^-(?=\d|$|\s)/.test(s)) { minor = true; third = 3; s = s.slice(1); }
    s = s.trim();
    if (/^(dim|°|º)/.test(s)) {
      third = 3; fifth = 6; s = s.replace(/^(dim|°|º)/, '');
      if (/^7/.test(s)) { add.add(9); s = s.slice(1); }
    }
    if (/^ø/.test(s)) { third = 3; fifth = 6; add.add(10); s = s.slice(1); }
    if (/^(aug|\+)/.test(s)) { fifth = 8; s = s.replace(/^(aug|\+)/, ''); }
    if (/^M(?!aj)/.test(s) && !/^M7/.test(s) && s.length === 1) { s = ''; }
    // tokens
    const toks = s.replace(/maj7|7M|M7/g, ' MAJ7 ').replace(/add/g, ' add').replace(/sus/g, ' sus').split(' ').filter(Boolean);
    let susDone = false;
    for (let t of toks) {
      let flat = false, sharp = false;
      if (t === 'MAJ7') { add.add(11); continue; }
      if (t === 'M') { continue; }
      if (t === 'sus' || t === 'sus4') { third = 5; susDone = true; continue; }
      if (t === 'sus2') { third = 2; susDone = true; continue; }
      if (t === 'sus9') { third = 2; susDone = true; continue; }
      if (/^add/.test(t)) t = t.slice(3);
      // 7M dentro de m7M
      if (/^[b#-]/.test(t)) { flat = t[0] !== '#'; sharp = t[0] === '#'; t = t.slice(1); }
      if (/[+-]$/.test(t) && /\d/.test(t)) { flat = t.endsWith('-'); sharp = t.endsWith('+'); t = t.slice(0, -1); }
      if (/b$/.test(t)) { flat = true; t = t.slice(0, -1); }
      // números concatenados tipo "79" ou "74"
      const nums = t.match(/1[13]|[2-9]/g) || [];
      for (const nStr of nums) {
        const n = parseInt(nStr);
        const acc = flat ? -1 : sharp ? 1 : 0;
        switch (n) {
          case 5:
            if (acc) fifth = 7 + acc;
            else if (toks.length === 1 && nums.length === 1 && !minor) power = true;
            break;
          case 7: add.add(10); break;
          case 6: add.add(9 + acc); break;
          case 9: add.add(14 + acc); break;
          case 2: add.add(2); break;
          case 4: if (!minor && !susDone && third === 4) { third = 5; } else add.add(5); break;
          case 11: add.add(17 + acc); break;
          case 13: add.add(21 + acc); break;
        }
      }
    }
    const ints = new Set([0, fifth]);
    if (!power) ints.add(third);
    for (const a of add) ints.add(a);
    const bassPc = sp.bass ? pc(sp.bass) : rootPc;
    return { sym, root: sp.root, rootPc, bassPc, bass: sp.bass, ints, third: power ? null : third, fifth, minor, power, suffix: sp.suffix };
  }

  function chordPcs(ch) { const c = typeof ch === 'string' ? parseChord(ch) : ch; return [...c.ints].map(i => (c.rootPc + i) % 12); }

  // Notas essenciais que o detector exige (fundamental + 3ª/sus, + 5ª alterada)
  function requiredPcs(ch) {
    const c = typeof ch === 'string' ? parseChord(ch) : ch;
    const r = [c.rootPc];
    if (c.third != null) r.push((c.rootPc + c.third) % 12);
    if (c.fifth !== 7 || c.power) r.push((c.rootPc + c.fifth) % 12);
    return [...new Set(r)];
  }

  // Voicing no estilo da apostila: ME = baixo na oitava 2, MD = notas dentro de C4..B4
  function voicing(sym, songV) {
    if (songV && songV[sym] && songV[sym][1] && songV[sym][1].length) {
      const me = midiOf(songV[sym][0]);
      return { me: me != null ? [me] : [], md: songV[sym][1].map(midiOf).filter(x => x != null) };
    }
    const c = parseChord(sym);
    if (!c) return { me: [], md: [] };
    let ints = [...c.ints].sort((a, b) => a - b);
    if (ints.length > 4) ints = ints.filter(i => i !== c.fifth || ints.length <= 4); // tira a 5ª
    if (ints.length > 4) ints = ints.filter(i => i !== 0);
    const md = [...new Set(ints.map(i => 60 + (c.rootPc + i) % 12))].sort((a, b) => a - b);
    return { me: [36 + c.bassPc], md };
  }

  function transposeChord(sym, n, flats) {
    if (!n) return sym;
    const sp = splitChord(sym);
    if (!sp) return sym;
    const f = flats;
    const r = noteName((pc(sp.root) + n + 120) % 12, f);
    const b = sp.bass ? '/' + noteName((pc(sp.bass) + n + 120) % 12, f) : '';
    return r + sp.suffix + b;
  }

  function simplify(sym, level) {
    if (!level) return sym;
    const c = parseChord(sym);
    if (!c) return sym;
    let q = '';
    if (c.fifth === 6 && c.third === 3) q = '°';
    else if (c.third === 3) q = 'm';
    else if (c.fifth === 8) q = '+';
    const bass = level === 1 && c.bass ? '/' + c.bass : '';
    return c.root + q + bass;
  }

  // ---------- Escalas e campo harmônico ----------
  const SCALES = {
    'Maior (jônio)': [0,2,4,5,7,9,11],
    'Menor natural (eólio)': [0,2,3,5,7,8,10],
    'Menor harmônica': [0,2,3,5,7,8,11],
    'Menor melódica': [0,2,3,5,7,9,11],
    'Pentatônica maior': [0,2,4,7,9],
    'Pentatônica menor': [0,3,5,7,10],
    'Blues': [0,3,5,6,7,10],
    'Mixolídio': [0,2,4,5,7,9,10],
    'Dórico': [0,2,3,5,7,9,10],
    'Cromática': [0,1,2,3,4,5,6,7,8,9,10,11]
  };
  const FIELD_MAJOR = { tri: ['', 'm', 'm', '', '', 'm', '°'], tet: ['7M', 'm7', 'm7', '7M', '7', 'm7', 'm7(b5)'], roman: ['I','ii','iii','IV','V','vi','vii°'], fn: ['Tônica','Subdominante','Tônica (relativa)','Subdominante','Dominante','Tônica (relativa)','Dominante'] };
  const FIELD_MINOR = { tri: ['m', '°', '', 'm', 'm', '', ''], tet: ['m7', 'm7(b5)', '7M', 'm7', 'm7', '7M', '7'], roman: ['i','ii°','III','iv','v','VI','VII'], fn: ['Tônica','Subdominante','Tônica (relativa)','Subdominante','Dominante','Subdominante','Dominante'] };
  function useFlats(keyPc, minor) { const k = minor ? (keyPc + 3) % 12 : keyPc; return FLAT_KEYS.has(k); }
  function field(keyPc, minor, tetrads) {
    const sc = minor ? SCALES['Menor natural (eólio)'] : SCALES['Maior (jônio)'];
    const F = minor ? FIELD_MINOR : FIELD_MAJOR;
    const fl = useFlats(keyPc, minor);
    return sc.map((iv, i) => ({ degree: F.roman[i], fn: F.fn[i], chord: noteName((keyPc + iv) % 12, fl) + (tetrads ? F.tet[i] : F.tri[i]) }));
  }
  function progression(keyPc, degrees, minor) { // degrees: [1,5,6,4]
    const f = field(keyPc, minor, false);
    return degrees.map(d => f[d - 1].chord);
  }
  const INTERVALS = [
    ['Uníssono', 'mesma nota'], ['2ª menor', '1 semitom — "Tubarão"'], ['2ª maior', '1 tom — "Parabéns pra você"'],
    ['3ª menor', 'som triste — base do acorde menor'], ['3ª maior', 'som alegre — base do acorde maior'],
    ['4ª justa', '"Aqui vai / Aleluia" (início de hinos)'], ['Trítono', 'tenso — 4ª aumentada/5ª diminuta'],
    ['5ª justa', 'estável — "power chord"'], ['6ª menor', 'melancólica'], ['6ª maior', '"My Bonnie" / "Noite Feliz" (início)'],
    ['7ª menor', 'som de blues / dominante'], ['7ª maior', 'sonhador — acorde 7M'], ['Oitava', 'mesma nota, mais aguda']
  ];

  function detectKey(chords) { // estimativa do tom pelos acordes
    let best = null;
    for (let k = 0; k < 12; k++) for (const minor of [false, true]) {
      const f = field(k, minor, false).map(x => x.chord);
      const fp = f.map(c => { const p = parseChord(c); return p.rootPc + ':' + (p.third === 3 ? 'm' : 'M'); });
      let score = 0;
      chords.forEach((c, i) => {
        const p = parseChord(c); if (!p) return;
        const key = p.rootPc + ':' + (p.third === 3 ? 'm' : 'M');
        if (fp.includes(key)) score += 1;
        if (i === 0 && p.rootPc === k) score += 2;
        if (i === chords.length - 1 && p.rootPc === k) score += 1;
      });
      if (!best || score > best.score) best = { k, minor, score };
    }
    return best;
  }

  return { SHARP, FLAT, PT, pc, noteName, ptName, midiOf, midiName, freq, parseChord, chordPcs, requiredPcs, voicing, transposeChord, simplify,
    SCALES, field, progression, useFlats, INTERVALS, QUALITY_NAMES, DICT_TYPES, detectKey, splitChord };
})();
