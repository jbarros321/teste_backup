// Teclado em SVG. Geometria compartilhada com o canvas do Modo Acordes Caindo.
const KB = (() => {
  const BLACK = new Set([1, 3, 6, 8, 10]);
  const isBlack = m => BLACK.has(m % 12);
  function geom(lo, hi) {
    const keys = new Map(); let wi = 0;
    for (let m = lo; m <= hi; m++) {
      if (!isBlack(m)) { keys.set(m, { x: wi, w: 1, black: false }); wi++; }
      else {
        const off = { 1: -0.35, 3: -0.25, 6: -0.37, 8: -0.3, 10: -0.23 }[m % 12];
        keys.set(m, { x: wi + off, w: 0.6, black: true });
      }
    }
    return { keys, width: wi, lo, hi };
  }
  // marks: {midi: 'me'|'md'|'hit'|'scale'|'root'|...} ou {midi: {cls, label}}
  function svg({ lo = 48, hi = 72, marks = {}, labels = 'marked', height = 110, names = null }) {
    const g = geom(lo, hi), W = 24, H = height, BH = H * 0.62;
    const out = [`<svg class="kb" viewBox="0 0 ${g.width * W} ${H}" preserveAspectRatio="xMidYMid meet">`];
    const draw = black => {
      for (const [m, k] of g.keys) {
        if (k.black !== black) continue;
        let mk = marks[m]; if (typeof mk === 'string') mk = { cls: mk };
        const x = k.x * W, w = k.w * W, h = black ? BH : H;
        out.push(`<rect data-m="${m}" class="${black ? 'kb-b' : 'kb-w'} ${mk ? 'mk-' + mk.cls : ''}" x="${x + 0.5}" y="0" width="${w - 1}" height="${h}" rx="3"/>`);
        const show = labels === 'all' ? !black : (labels === 'marked' && mk);
        if (show || (mk && mk.label)) {
          const txt = (mk && mk.label) || (names ? names(m) : M.noteName(m % 12));
          out.push(`<text class="kb-t ${black ? 'on-b' : ''} ${mk ? 'on-mk' : ''}" x="${x + w / 2}" y="${h - (black ? 6 : 8)}">${txt}</text>`);
        }
        if (m % 12 === 0 && !black && labels !== 'none') out.push(`<text class="kb-c" x="${x + w / 2}" y="${H + 12}">C${Math.floor(m / 12) - 1}</text>`);
      }
    };
    draw(false); draw(true);
    out.push('</svg>');
    return out.join('');
  }
  // range que cobre as notas com folga, alinhado a C
  function rangeFor(midis, minSpan = 24) {
    if (!midis.length) return [48, 72];
    let lo = Math.min(...midis), hi = Math.max(...midis);
    lo = Math.floor(lo / 12) * 12; hi = Math.ceil((hi + 1) / 12) * 12;
    while (hi - lo < minSpan) hi += 12;
    return [lo, hi];
  }
  return { geom, svg, isBlack, rangeFor };
})();
