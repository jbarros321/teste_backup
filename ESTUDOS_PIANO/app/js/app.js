// Tecladista PRO — app principal (rotas, telas, Modo Acordes Caindo, treinos)
const $ = (s, el = document) => el.querySelector(s);
const $$ = (s, el = document) => [...el.querySelectorAll(s)];
const esc = s => String(s ?? '').replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
const view = () => $('#view');
let cleanup = [];
function onLeave(fn) { cleanup.push(fn); }
let lastInteract = Date.now();
['pointerdown', 'keydown'].forEach(ev => addEventListener(ev, () => lastInteract = Date.now()));
Detect.st.muteUntil = 0;

// ---------- Toasts ----------
Store.onToast((msg, big) => {
  const t = document.createElement('div'); t.className = 'toast' + (big ? ' big' : ''); t.textContent = msg;
  $('#toasts').appendChild(t); setTimeout(() => t.classList.add('out'), big ? 3200 : 1800); setTimeout(() => t.remove(), big ? 3800 : 2400);
  renderTop();
});

// ---------- Dados derivados das músicas ----------
// músicas extras (outras apostilas) + as que você adicionou colando a cifra
(window.EXTRA_SONGS || []).forEach(x => SONGS.push({ n: x.n, t: x.t, a: x.a, k: x.k, src: x.src, L: Cifra.parse(x.text), v: {} }));
Store.d.custom.forEach(x => SONGS.push(x));
const SONG_BY_N = new Map(SONGS.map(s => [s.n, s]));
function songInfo(s) {
  if (s._i) return s._i;
  const all = []; s.L.forEach(l => (l.c || []).forEach(c => all.push(c[1])));
  const uniq = [...new Set(all)];
  const complex = uniq.filter(c => !/^[A-G][#b]?m?$/.test(c)).length;
  const black = uniq.filter(c => /^[A-G][#b]/.test(c)).length;
  const score = uniq.length + complex * 1.5 + black * 0.5;
  const key = M.detectKey(all, s.k);
  s._i = { all, uniq, score, diff: score <= 5 ? 1 : score <= 11 ? 2 : 3, key };
  return s._i;
}
const DIFF = ['', 'Fácil', 'Médio', 'Difícil'];
const keyName = k => M.noteName(k.k, M.useFlats(k.k, k.minor)) + (k.minor ? 'm' : '');

// ---------- Microfone / MIDI ----------
async function connectMic() {
  try { await Detect.startMic(); Detect.st.sens = Store.d.settings.sens; if (Store.d.settings.gate) Detect.st.gate = Store.d.settings.gate; renderTop(); return true; }
  catch (e) { alert('Não consegui acessar o microfone: ' + e.message + '\n\nDica: abra o app pelo arquivo ABRIR_TECLADO.command (endereço http://localhost) e permita o microfone.'); return false; }
}
async function connectMidi() {
  try { await Detect.startMidi(); renderTop(); return true; } catch (e) { alert(e.message); return false; }
}
function micPill() {
  const m = Detect.st.mode;
  return m === 'mic' ? '<span class="pill ok">🎤 Ouvindo o teclado</span>' : m === 'midi' ? '<span class="pill ok">🎹 MIDI</span>' : '<span class="pill off">🎤 Microfone desligado — clique</span>';
}

// ---------- Layout ----------
const NAV = [['#/', '🏠', 'Início'], ['#/trilha', '🧭', 'Trilha'], ['#/exercicios', '💪', '100 Exercícios'], ['#/musicas', '🎵', 'Músicas'], ['#/treinos', '🎯', 'Treinos'], ['#/acordes', '📘', 'Acordes'], ['#/ferramentas', '🧰', 'Ferramentas'], ['#/progresso', '🏆', 'Progresso']];
function renderTop() {
  const lv = Store.level();
  $('#top').innerHTML = `
    <a class="brand" href="#/">🎹 <b>Tecladista</b><span>PRO</span></a>
    <div class="top-r">
      <button class="mic" id="micbtn">${micPill()}</button>
      <div class="lvl" title="${lv.name}"><span>Nv ${lv.n}</span><div class="bar"><i style="width:${Math.min(100, lv.cur / lv.need * 100)}%"></i></div><small>${Store.d.xp} XP</small></div>
      <span class="streak" title="Dias seguidos estudando (5+ min)">🔥 ${Store.streak()}</span>
    </div>`;
  $('#micbtn').onclick = async () => { if (Detect.st.mode === 'off') await connectMic(); else location.hash = '#/treinos/mic'; };
  const h = location.hash || '#/';
  $('#nav').innerHTML = NAV.map(([href, ic, t]) => `<a href="${href}" class="${(h === href || (href !== '#/' && h.startsWith(href))) ? 'on' : ''}"><span>${ic}</span><em>${t}</em></a>`).join('');
}

// ---------- Rotas ----------
const routes = [
  [/^#?\/?$/, home], [/^#\/trilha$/, trilha], [/^#\/aula\/(\w+)$/, aula], [/^#\/exercicios$/, exercicios],
  [/^#\/musicas$/, musicas], [/^#\/musicas\/nova$/, novaMusica], [/^#\/musica\/(\d+)$/, musica], [/^#\/play\/song\/(\d+)$/, playSong], [/^#\/play\/seq$/, playSeq],
  [/^#\/treinos$/, treinos], [/^#\/treinos\/([\w-]+)$/, treino], [/^#\/acordes$/, acordes], [/^#\/ferramentas$/, ferramentas], [/^#\/progresso$/, progresso]
];
function query() { const q = (location.hash.split('?')[1] || ''); return Object.fromEntries(new URLSearchParams(q)); }
function route() {
  cleanup.forEach(f => { try { f(); } catch (e) {} }); cleanup = [];
  Snd.metroStop();
  const h = location.hash.split('?')[0] || '#/';
  for (const [re, fn] of routes) { const m = re.exec(h); if (m) { view().innerHTML = ''; view().scrollTop = 0; window.scrollTo(0, 0); fn(...m.slice(1)); break; } }
  renderTop();
}
addEventListener('hashchange', route);

// tempo de estudo: conta a cada 15 s se você está ativo
setInterval(() => {
  if (document.hidden) return;
  const active = Date.now() - lastInteract < 120000 || (Detect.st.mode === 'mic' && Detect.st.level > Detect.st.gate);
  if (active) { Store.addTime(15); if (Math.random() < 0.1) renderTop(); }
}, 15000);

// ---------- Widgets reutilizáveis ----------
function chordKb(sym, extra = {}) {
  const v = M.voicing(sym); const marks = {};
  v.me.forEach(m => marks[m] = 'me'); v.md.forEach(m => marks[m] = 'md');
  return KB.svg({ lo: 36, hi: 83, marks, height: 90, ...extra });
}
function playChord(sym) { const v = M.voicing(sym); Snd.chord([...v.me, ...v.md], 0, 1.8, 0.03); Detect.st.muteUntil = performance.now() + 1800; }
function chordInfoHtml(sym) {
  const v = M.voicing(sym); const c = M.parseChord(sym);
  const fl = /b/.test(sym.slice(1, 2)) || (c && M.useFlats(c.rootPc, c.minor) && !/#/.test(sym));
  return `<div class="cinfo"><div class="cname">${esc(sym)} <button class="btn sm" data-play="${esc(sym)}">🔊 Ouvir</button></div>
    <div class="cnotes"><span class="me">ME: ${v.me.map(m => M.midiName(m, fl)).join(' ')}</span> <span class="md">MD: ${v.md.map(m => M.midiName(m, fl)).join(' ')}</span>
    <span class="pt">(${[...new Set([...v.me, ...v.md].map(m => M.ptName(m % 12, fl)))].join(' · ')})</span></div>${chordKb(sym)}</div>`;
}
document.addEventListener('click', e => {
  const p = e.target.closest('[data-play]'); if (p) { e.preventDefault(); playChord(p.dataset.play); }
  const n = e.target.closest('[data-note]'); if (n) { Snd.note(+n.dataset.note); }
});
function hydrate(root) {
  $$('.w-kb', root).forEach(el => {
    const ms = el.dataset.notes.split(/\s+/).map(M.midiOf); const [lo, hi] = KB.rangeFor(ms);
    const marks = {}; ms.forEach(m => marks[m] = 'md');
    el.innerHTML = `<div class="wk-h"><b>${esc(el.dataset.label || '')}</b> <button class="btn sm">🔊</button></div>${KB.svg({ lo, hi: Math.min(hi, lo + 36), marks, height: 80 })}`;
    $('button', el).onclick = () => { Snd.chord(ms, 0, 1.6, 0.12); Detect.st.muteUntil = performance.now() + 2000; };
  });
  $$('.w-chords', root).forEach(el => {
    const cs = el.dataset.chords.split(/\s+/);
    el.innerHTML = `<div class="chips">${cs.map(c => `<button class="chip" data-c="${esc(c)}">${esc(c)}</button>`).join('')}</div><div class="wc-kb"></div>`;
    const show = c => { $('.wc-kb', el).innerHTML = chordInfoHtml(c); $$('.chip', el).forEach(b => b.classList.toggle('on', b.dataset.c === c)); };
    $$('.chip', el).forEach(b => b.onclick = () => { show(b.dataset.c); playChord(b.dataset.c); });
    show(cs[0]);
  });
  $$('.w-field', root).forEach(el => {
    const k = M.pc(el.dataset.key), minor = el.dataset.minor === '1', tet = el.dataset.tet === '1';
    el.innerHTML = fieldTable(k, minor, tet);
  });
  $$('.w-scale', root).forEach(el => {
    const r = M.pc(el.dataset.root), sc = M.SCALES[el.dataset.scale]; const fl = M.useFlats(r, /menor/i.test(el.dataset.scale));
    const ms = sc.map(i => 60 + r + i).concat([72 + r]); const marks = {};
    ms.forEach((m, i) => marks[m] = { cls: i === 0 || i === ms.length - 1 ? 'root' : 'md', label: M.noteName(m % 12, fl) });
    el.innerHTML = `<div class="wk-h"><b>${esc(el.dataset.root)} — ${esc(el.dataset.scale)}</b>: ${ms.map(m => M.ptName(m % 12, fl)).join(' ')} <button class="btn sm">🔊</button></div>${KB.svg({ lo: 60, hi: 95, marks, height: 80 })}`;
    $('button', el).onclick = () => { ms.forEach((m, i) => Snd.note(m, i * 0.3, 0.6)); Detect.st.muteUntil = performance.now() + ms.length * 300 + 800; };
  });
}
function fieldTable(k, minor, tet) {
  const f = M.field(k, minor, tet);
  return `<div class="field"><div class="fh">${M.noteName(k, M.useFlats(k, minor))}${minor ? 'm' : ''} — campo harmônico ${minor ? 'menor' : 'maior'}${tet ? ' (tétrades)' : ''}</div>
  <div class="frow">${f.map(x => `<div class="fc"><small>${x.degree}</small><button class="chip" data-play="${esc(x.chord)}">${esc(x.chord)}</button><em>${x.fn}</em></div>`).join('')}</div></div>`;
}
function progressBar(p) { return `<div class="pbar"><i style="width:${Math.round(p * 100)}%"></i></div>`; }

// ================= INÍCIO =================
function home() {
  const d = Store.d, lv = Store.level();
  const allL = LESSONS.flatMap(m => m.lessons.map(l => ({ ...l, mod: m })));
  const nextL = allL.find(l => !d.lessons[l.id]);
  const exTodo = EX.filter(e => !d.ex[e.id]?.done).slice(0, 3);
  const easy = SONGS.filter(s => songInfo(s).diff === 1);
  const pool = d.fav.length ? d.fav.map(n => SONG_BY_N.get(n)).filter(Boolean) : easy;
  const dayN = Math.floor(Date.now() / 864e5);
  const sotd = pool[dayN % pool.length];
  const todaySec = d.log[Store.today()] || 0;
  view().innerHTML = `
  <section class="hero-card">
    <div><h1>Bora tocar${d.settings.name ? ', ' + esc(d.settings.name) : ''}! 🙌</h1>
    <p>Nível <b>${lv.n} — ${lv.name}</b> · ${d.xp} XP ${lv.max ? '' : `· faltam ${lv.need - lv.cur} XP para o próximo nível`}</p>
    ${progressBar(lv.cur / lv.need)}
    <div class="today"><div><b>${Math.floor(todaySec / 60)}</b><small>min hoje</small></div><div><b>🔥 ${Store.streak()}</b><small>dias seguidos</small></div>
    <div><b>${Object.keys(d.lessons).length}/${allL.length}</b><small>aulas</small></div><div><b>${Object.values(d.ex).filter(e => e.done).length}/100</b><small>exercícios</small></div>
    <div><b>${Object.values(d.songs).filter(s => s.done).length}</b><small>músicas</small></div></div></div>
    ${todaySec < 1800 ? `<div class="goal">Meta do dia: 30 min ${progressBar(todaySec / 1800)}</div>` : '<div class="goal ok">✅ Meta de 30 min batida hoje!</div>'}
  </section>
  ${Detect.st.mode === 'off' ? `<section class="card warnc"><b>🎤 Ligue o microfone</b> para o app ouvir o seu CTK-1200 e liberar os acordes quando você tocar certo. Coloque o computador perto do alto-falante do teclado. <button class="btn" id="hmic">Ligar microfone</button> <a class="btn ghost" href="#/treinos/mic">Calibrar</a></section>` : ''}
  <h2>Plano de hoje</h2>
  <div class="grid3">
    <a class="card plan" href="${nextL ? '#/aula/' + nextL.id : '#/trilha'}"><small>1 · Aula (10 min)</small><h3>${nextL ? nextL.mod.icon + ' ' + esc(nextL.title) : 'Trilha concluída! 🎓'}</h3><p>${nextL ? esc(nextL.mod.title) : 'Revise o que quiser'}</p></a>
    <a class="card plan" href="#/exercicios"><small>2 · Exercícios (10 min)</small><h3>💪 ${exTodo.map(e => '#' + e.id).join(', ') || 'Todos feitos!'}</h3><p>${exTodo.map(e => esc(e.t)).join(' · ')}</p></a>
    <a class="card plan" href="${sotd ? '#/musica/' + sotd.n : '#/musicas'}"><small>3 · Música do dia (10 min)</small><h3>🎵 ${sotd ? esc(sotd.t) : ''}</h3><p>${sotd ? esc(sotd.a) + ' · ' + DIFF[songInfo(sotd).diff] : ''}</p></a>
  </div>
  <h2>Atalhos</h2>
  <div class="grid4">
    <a class="card quick" href="#/treinos/chords">⚡<b>Acordes relâmpago</b></a>
    <a class="card quick" href="#/treinos/ear-quality">👂<b>Treino de ouvido</b></a>
    <a class="card quick" href="#/play/seq?c=C%20G%20Am%20F%20C%20G%20Am%20F&bpm=70&beats=4&t=I-V-vi-IV%20em%20C">🔥<b>Acordes caindo</b></a>
    <a class="card quick" href="#/ferramentas">⏱️<b>Metrônomo</b></a>
  </div>`;
  const hm = $('#hmic'); if (hm) hm.onclick = async () => { if (await connectMic()) home(); };
}

// ================= TRILHA =================
function trilha() {
  const d = Store.d;
  view().innerHTML = `<h1>🧭 Trilha de aulas</h1><p class="sub">Teoria + prática, do zero ao avançado. Siga a ordem — cada módulo prepara o próximo.</p>
  ${LESSONS.map((m, mi) => {
    const done = m.lessons.filter(l => d.lessons[l.id]).length;
    return `<section class="module" style="--mc:${m.color}"><div class="mh"><span class="mi">${m.icon}</span><div><small>Módulo ${mi + 1}</small><h2>${esc(m.title)}</h2></div><span class="mc">${done}/${m.lessons.length}</span></div>
    ${progressBar(done / m.lessons.length)}
    <div class="lessons">${m.lessons.map((l, i) => `<a href="#/aula/${l.id}" class="lesson ${d.lessons[l.id] ? 'done' : ''}"><span>${d.lessons[l.id] ? '✅' : (i + 1)}</span>${esc(l.title)}<small>+${l.xp} XP</small></a>`).join('')}</div></section>`;
  }).join('')}`;
}
function aula(id) {
  const all = LESSONS.flatMap(m => m.lessons.map(l => ({ ...l, mod: m })));
  const i = all.findIndex(l => l.id === id); const L = all[i]; if (!L) return trilha();
  const prev = all[i - 1], next = all[i + 1], done = Store.d.lessons[id];
  view().innerHTML = `<a class="back" href="#/trilha">← Trilha</a>
  <article class="lesson-page" style="--mc:${L.mod.color}"><small>${L.mod.icon} ${esc(L.mod.title)}</small><h1>${esc(L.title)}</h1>
  <div class="lesson-body">${L.html}</div>
  ${L.practice ? `<h2>🎯 Prática</h2><div class="chips">${L.practice.map(p => `<a class="btn" href="${practiceHref(p)}">${esc(p.label)}</a>`).join('')}</div>` : ''}
  ${L.quiz ? `<h2>❓ Quiz</h2><div class="quiz">${L.quiz.map((q, qi) => `<div class="q" data-q="${qi}"><p><b>${qi + 1}.</b> ${esc(q.q)}</p><div class="opts">${q.opts.map((o, oi) => `<button class="opt" data-o="${oi}">${esc(o)}</button>`).join('')}</div><div class="why"></div></div>`).join('')}</div>` : ''}
  <div class="lesson-foot">${prev ? `<a class="btn ghost" href="#/aula/${prev.id}">← ${esc(prev.title)}</a>` : '<span></span>'}
  <button class="btn big" id="done">${done ? '✅ Aula concluída' : `Concluir aula (+${L.xp} XP)`}</button>
  ${next ? `<a class="btn ghost" href="#/aula/${next.id}">${esc(next.title)} →</a>` : '<span></span>'}</div></article>`;
  hydrate(view());
  let right = 0;
  $$('.q').forEach(qel => {
    const q = L.quiz[+qel.dataset.q];
    $$('.opt', qel).forEach(b => b.onclick = () => {
      if (qel.classList.contains('answered')) return; qel.classList.add('answered');
      const ok = +b.dataset.o === q.a; if (ok) right++;
      b.classList.add(ok ? 'right' : 'wrong'); $$('.opt', qel)[q.a].classList.add('right');
      $('.why', qel).textContent = (ok ? '✔ Isso! ' : '✘ Resposta: ' + q.opts[q.a] + '. ') + (q.why || '');
    });
  });
  $('#done').onclick = () => {
    if (!Store.d.lessons[id]) { Store.d.lessons[id] = { date: Store.today(), quiz: right }; Store.addXp(L.xp + right * 5, 'aula concluída'); }
    if (next) location.hash = '#/aula/' + next.id; else location.hash = '#/trilha';
  };
}
function practiceHref(p) {
  if (p.drill) return `#/treinos/${p.drill}?${new URLSearchParams({ set: p.set || '', root: p.root || '', scale: p.scale || '' })}`;
  if (p.song) return `#/musica/${p.song}`;
  return `#/play/seq?${new URLSearchParams({ c: p.seq, bpm: p.bpm || 60, beats: p.beats || 4, t: p.label || p.t || 'Prática' })}`;
}

// ================= EXERCÍCIOS =================
function exercicios() {
  const q = query(); const lvl = +(q.l || 0), cat = q.cat || '';
  const cats = [...new Set(EX.map(e => e.cat))];
  const list = EX.filter(e => (!lvl || e.lvl === lvl) && (!cat || e.cat === cat));
  const d = Store.d; const doneN = EX.filter(e => d.ex[e.id]?.done).length;
  view().innerHTML = `<h1>💪 100 Exercícios</h1><p class="sub">${doneN}/100 concluídos. Faça em ordem dentro de cada nível. Marque como feito quando conseguir executar <b>3 vezes seguidas sem erro</b> no BPM indicado.</p>
  ${progressBar(doneN / 100)}
  <div class="filters">${[0, 1, 2, 3, 4, 5].map(l => `<a class="chip ${lvl === l ? 'on' : ''}" href="#/exercicios?${new URLSearchParams({ l, cat })}">${l ? l + ' · ' + LEVELS[l] : 'Todos'} ${l ? `<small>${EX.filter(e => e.lvl === l && d.ex[e.id]?.done).length}/${EX.filter(e => e.lvl === l).length}</small>` : ''}</a>`).join('')}</div>
  <div class="filters">${['', ...cats].map(c => `<a class="chip sm ${cat === c ? 'on' : ''}" href="#/exercicios?${new URLSearchParams({ l: lvl, cat: c })}">${c || 'Todas as categorias'}</a>`).join('')}</div>
  <div class="exlist">${list.map(e => {
    const st = d.ex[e.id] || {};
    return `<div class="ex ${st.done ? 'done' : ''}" data-id="${e.id}"><div class="exn">#${e.id}</div><div class="exb"><div class="ext"><b>${esc(e.t)}</b> <span class="tag">${e.cat}</span><span class="tag l${e.lvl}">${LEVELS[e.lvl]}</span>${e.bpm ? `<span class="tag">♩ ${e.bpm} BPM</span>` : ''}</div>
    <p>${esc(e.d)}</p><div class="exa">${e.act ? `<a class="btn sm" href="${practiceHref(e.act)}">▶ Praticar</a>` : ''}${e.bpm ? `<button class="btn sm ghost" data-metro="${e.bpm}">⏱️ Metrônomo ${e.bpm}</button>` : ''}
    <button class="btn sm ${st.done ? 'ok' : 'ghost'}" data-done="${e.id}">${st.done ? '✅ Feito' : 'Marcar como feito'}</button></div></div></div>`;
  }).join('')}</div>`;
  $$('[data-done]').forEach(b => b.onclick = () => {
    const id = +b.dataset.done, e = EX[id - 1], st = Store.d.ex[id] || {};
    if (st.done) { st.done = false; Store.d.ex[id] = st; Store.save(); }
    else { const first = !st.everDone; Object.assign(st, { done: true, everDone: true, date: Store.today() }); Store.d.ex[id] = st; Store.save(); if (first) Store.addXp(10 + e.lvl * 5, 'exercício #' + id); }
    exercicios();
  });
  $$('[data-metro]').forEach(b => b.onclick = () => {
    if (Snd.metro.on) { Snd.metroStop(); b.classList.remove('ok'); } else { Snd.metroStart(+b.dataset.metro, 4); b.classList.add('ok'); }
  });
}

// ================= MÚSICAS =================
function musicas() {
  const q = query(); const s0 = Store.d.settings;
  view().innerHTML = `<div class="songhead"><div><h1>🎵 Músicas</h1><p class="sub">${SONGS.length} louvores das suas apostilas${Store.d.custom.length ? ` + ${Store.d.custom.length} adicionadas por você` : ''}. Toque qualquer uma no Modo Acordes Caindo.</p></div><a class="btn big" href="#/musicas/nova">➕ Adicionar música</a></div>
  <div class="filters"><input id="q" placeholder="Buscar música ou artista..." value="${esc(q.q || s0.lastQ || '')}">
  <select id="fd"><option value="0">Todas as dificuldades</option><option value="1">Fácil</option><option value="2">Médio</option><option value="3">Difícil</option></select>
  <select id="ff"><option value="">Todas</option><option value="fav">⭐ Favoritas</option><option value="done">✅ Concluídas</option><option value="todo">Não tocadas</option></select>
  <select id="fa"><option value="">Todos os artistas</option>${[...new Set(SONGS.map(s => s.a))].sort().map(a => `<option>${esc(a)}</option>`).join('')}</select></div>
  <div id="list" class="songlist"></div>`;
  const draw = () => {
    const t = $('#q').value.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
    s0.lastQ = $('#q').value; Store.save();
    const fd = +$('#fd').value, ff = $('#ff').value, fa = $('#fa').value;
    const norm = x => x.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
    const res = SONGS.filter(s => {
      if (t && !norm(s.t + ' ' + s.a).includes(t)) return false;
      if (fd && songInfo(s).diff !== fd) return false;
      if (fa && s.a !== fa) return false;
      const st = Store.d.songs[s.n];
      if (ff === 'fav' && !Store.d.fav.includes(s.n)) return false;
      if (ff === 'done' && !st?.done) return false;
      if (ff === 'todo' && st) return false;
      return true;
    });
    $('#list').innerHTML = `<p class="sub">${res.length} músicas</p>` + res.slice(0, 300).map(s => {
      const i = songInfo(s), st = Store.d.songs[s.n];
      return `<a class="song" href="#/musica/${s.n}"><span class="sn">${String(s.n).padStart(3, '0')}</span><div><b>${esc(s.t)}</b><small>${esc(s.a)}</small></div>
      <span class="tag">${keyName(i.key)}</span><span class="tag d${i.diff}">${DIFF[i.diff]}</span><span class="tag">${i.uniq.length} acordes</span>
      ${Store.d.fav.includes(s.n) ? '⭐' : ''}${st?.done ? `<span class="tag ok">✅ ${st.best}%</span>` : ''}</a>`;
    }).join('') + (res.length > 300 ? '<p class="sub">Refine a busca para ver mais.</p>' : '');
  };
  ['q', 'fd', 'ff', 'fa'].forEach(id => $('#' + id).addEventListener('input', draw));
  draw();
}
function musica(n) {
  const s = SONG_BY_N.get(+n); if (!s) return musicas();
  const inf = songInfo(s); const st = Store.d.songs[s.n] || {};
  let tr = st.tr || 0, simp = Store.d.settings.simplify || 0;
  const render = () => {
    const tk = (inf.key.k + tr + 12) % 12; const fl = M.useFlats(tk, inf.key.minor);
    const T = c => M.simplify(M.transposeChord(c, tr, fl), simp);
    const uniq = [...new Set(inf.all.map(T))];
    view().innerHTML = `<a class="back" href="#/musicas">← Músicas</a>
    <div class="songhead"><div><small>#${s.n} · ${esc(s.a)}</small><h1>${esc(s.t)}</h1>
    ${s.src || s.custom ? `<p class="sub">${s.custom ? '✍️ Adicionada por você' : '📄 ' + esc(s.src)}</p>` : ''}
    <p>Tom: <b>${M.noteName(tk, fl)}${inf.key.minor ? 'm' : ''}</b> ${tr ? `(original ${keyName(inf.key)})` : ''} · ${DIFF[inf.diff]} · ${uniq.length} acordes ${st.done ? `· ✅ melhor: ${st.best}%` : ''}</p></div>
    <div class="songctl"><button class="btn" id="fav">${Store.d.fav.includes(s.n) ? '⭐ Favorita' : '☆ Favoritar'}</button>
    <div class="grp"><button class="btn sm" id="tm">−½</button><span>Tom ${tr > 0 ? '+' : ''}${tr}</span><button class="btn sm" id="tp">+½</button></div>
    <select id="simp"><option value="0">Cifra original</option><option value="1">Simplificar (tríades + baixo)</option><option value="2">Bem simples (só maior/menor)</option></select>
    ${s.custom ? `<button class="btn ghost danger" id="del">🗑 Remover</button>` : ''}
    <a class="btn big" id="go" href="#/play/song/${s.n}">▶ Tocar — Acordes Caindo</a></div></div>
    <h2>Acordes desta música</h2><div class="chips" id="uc">${uniq.map(c => `<button class="chip" data-c="${esc(c)}">${esc(c)}</button>`).join('')}</div><div id="ckb"></div>
    ${fieldTable(tk, inf.key.minor, false)}
    <h2>Cifra</h2><div class="cifra">${s.L.map(l => {
      if (l.s) return `<div class="sec">${esc(l.s)}</div>`;
      if (!l.c.length) return `<div class="ly">${esc(l.l) || '&nbsp;'}</div>`;
      let line = '', col = 0;
      l.c.forEach(([p, c]) => { const cc = T(c); if (p < col) p = col; line += ' '.repeat(p - col) + `<b data-c="${esc(cc)}">${esc(cc)}</b>`; col = p + cc.length + 1; line += ' '; });
      return `<div class="cl">${line}</div><div class="ly">${esc(l.l) || '&nbsp;'}</div>`;
    }).join('')}</div>`;
    $('#simp').value = simp;
    if ($('#del')) $('#del').onclick = () => {
      if (!confirm('Remover "' + s.t + '" das suas músicas?')) return;
      Store.d.custom = Store.d.custom.filter(x => x.n !== s.n); SONGS.splice(SONGS.indexOf(s), 1); SONG_BY_N.delete(s.n); Store.save(); location.hash = '#/musicas';
    };
    $('#fav').onclick = () => { const f = Store.d.fav; const i = f.indexOf(s.n); i >= 0 ? f.splice(i, 1) : f.push(s.n); Store.save(); render(); };
    $('#tm').onclick = () => { tr = (tr - 1) % 12; save(); render(); };
    $('#tp').onclick = () => { tr = (tr + 1) % 12; save(); render(); };
    $('#simp').onchange = e => { simp = +e.target.value; Store.d.settings.simplify = simp; Store.save(); render(); };
    const show = c => { $('#ckb').innerHTML = chordInfoHtml(c); playChord(c); };
    $$('[data-c]').forEach(b => b.onclick = () => show(b.dataset.c));
  };
  const save = () => { const x = Store.d.songs[s.n] || {}; x.tr = tr; Store.d.songs[s.n] = x; Store.save(); };
  render();
}

function novaMusica() {
  view().innerHTML = `<a class="back" href="#/musicas">← Músicas</a><h1>➕ Adicionar música</h1>
  <p class="sub">Copie a cifra de qualquer site (acordes em cima da letra) e cole abaixo. O app separa os acordes da letra e a música fica pronta para o Modo Acordes Caindo.</p>
  <div class="card"><div class="filters"><input id="nt" placeholder="Nome da música (ex.: Santo)"><input id="na" placeholder="Artista (ex.: Fernandinho)"></div>
  <textarea id="nx" rows="16" placeholder="[Intro] G  D  Em  C

G                D
Primeira linha da letra...
Em               C
Segunda linha da letra..."></textarea>
  <p id="npv" class="sub"></p><button class="btn big" id="ns" disabled>💾 Salvar música</button></div>
  <div class="tip">💡 Funciona melhor quando cada linha de acordes fica exatamente em cima da sua linha de letra (como nos sites de cifra). Seções como [Refrão] e [Ponte] são reconhecidas. Tablaturas e linhas de riff são ignoradas.</div>
  <div id="nprev"></div>`;
  const upd = () => {
    const L = Cifra.parse($('#nx').value); const n = Cifra.chordCount(L);
    const uniq = [...new Set(L.flatMap(l => (l.c || []).map(c => c[1])))];
    $('#npv').innerHTML = n ? `✅ ${n} acordes encontrados (${uniq.length} diferentes): <b>${uniq.map(esc).join(' ')}</b>` : 'Cole a cifra para ver a prévia.';
    $('#ns').disabled = !(n >= 2 && $('#nt').value.trim());
    $('#nprev').innerHTML = n ? `<h2>Prévia</h2><div class="cifra">${L.map(l => l.s ? `<div class="sec">${esc(l.s)}</div>` : !l.c.length ? `<div class="ly">${esc(l.l) || '&nbsp;'}</div>` : (() => { let line = '', col = 0; l.c.forEach(([p, c]) => { if (p < col) p = col; line += ' '.repeat(p - col) + `<b>${esc(c)}</b> `; col = p + c.length + 1; }); return `<div class="cl">${line}</div><div class="ly">${esc(l.l) || '&nbsp;'}</div>`; })()).join('')}</div>` : '';
    return L;
  };
  ['nt', 'nx'].forEach(id => $('#' + id).addEventListener('input', upd));
  $('#ns').onclick = () => {
    const L = upd(); const first = L.find(l => l.c && l.c.length);
    const n = Math.max(2000, ...Store.d.custom.map(x => x.n)) + 1;
    const song = { n, t: $('#nt').value.trim(), a: $('#na').value.trim() || 'Desconhecido', k: first ? first.c[0][1] : '', L, v: {}, custom: true };
    Store.d.custom.push(song); SONGS.push(song); SONG_BY_N.set(n, song); Store.save();
    Store.addXp(10, 'música adicionada');
    location.hash = '#/musica/' + n;
  };
}

// ================= MODO ACORDES CAINDO =================
function playSong(n) {
  const s = SONG_BY_N.get(+n); if (!s) return musicas();
  const inf = songInfo(s); const st = Store.d.songs[s.n] || {};
  const tr = st.tr || 0; const tk = (inf.key.k + tr + 12) % 12; const fl = M.useFlats(tk, inf.key.minor);
  const simp = Store.d.settings.simplify || 0;
  const T = c => M.simplify(M.transposeChord(c, tr, fl), simp);
  const events = [], lines = [];
  s.L.forEach(l => {
    if (l.s) { lines.push({ s: l.s }); return; }
    if (!l.c.length) { if (lines.length) lines[lines.length - 1].after = (lines[lines.length - 1].after || []).concat(l.l); return; }
    const li = lines.length; lines.push({ c: l.c.map(([p, c]) => [p, T(c)]), l: l.l });
    l.c.forEach(([p, c], ci) => events.push({ c: T(c), li, ci }));
  });
  hero({ title: s.t, sub: s.a + ' · Tom ' + M.noteName(tk, fl) + (inf.key.minor ? 'm' : ''), events, lines, bpm: st.bpm || 70, back: '#/musica/' + s.n,
    onFinish: r => {
      const x = Store.d.songs[s.n] || {}; const first = !x.done;
      x.plays = (x.plays || 0) + 1; x.best = Math.max(x.best || 0, r.score); x.done = true; x.last = Store.today(); x.bpm = r.bpm;
      Store.d.songs[s.n] = x; Store.save();
      Store.addXp((first ? 30 : 10) + Math.round(r.score / 10) * 2, first ? 'música nova!' : 'música');
    } });
}
function playSeq() {
  const q = query(); const cs = (q.c || 'C G Am F').split(/\s+/).filter(Boolean);
  const events = cs.map((c, i) => ({ c, li: 0, ci: i }));
  let p = 0; const line = { c: cs.map(c => { const r = [p, c]; p += c.length + 3; return r; }), l: '' };
  hero({ title: q.t || 'Prática', sub: 'Sequência de acordes', events, lines: [line], bpm: +q.bpm || 60, beats: +q.beats || 4, back: 'back', repeatable: true,
    onFinish: r => Store.addXp(8 + Math.round(r.score / 20) * 2, 'prática') });
}

function hero(cfg) {
  const S = Store.d.settings;
  let beats = cfg.beats || S.beats || 4, bpm = cfg.bpm, mode = Detect.st.mode === 'off' ? 'manual' : 'wait', look = S.lookahead || 8;
  let guide = false, metroOn = false;
  const ev = cfg.events.map(e => {
    const v = M.voicing(e.c); const p = M.parseChord(e.c);
    return { ...e, v, req: p ? M.requiredPcs(p) : [], pcs: p ? M.chordPcs(p).concat([p.bassPc]) : [] };
  });
  const layout = () => { let b = 0; ev.forEach(e => { e.start = b; e.dur = beats; b += beats; }); };
  layout();
  ev.forEach((e, i) => { const pr = ev[i - 1]; e.needOnset = !!pr && e.req.every(p => pr.pcs.includes(p)); });
  const LO = 36, HI = 84; const g = KB.geom(LO, HI);
  view().innerHTML = `<div class="play">
   <div class="phead"><a class="back" href="${cfg.back === 'back' ? 'javascript:history.back()' : cfg.back}">← Sair</a><div><h1>${esc(cfg.title)}</h1><small>${esc(cfg.sub || '')}</small></div>
   <div class="pctl">
    <select id="mode"><option value="wait">⏸ Esperar eu tocar</option><option value="tempo">⏩ No tempo</option><option value="manual">👆 Manual (espaço)</option></select>
    <label>♩ <input id="bpm" type="number" min="30" max="200" value="${bpm}"> BPM</label>
    <label>Tempos/acorde <select id="beats">${[1, 2, 3, 4, 6, 8].map(b => `<option ${b === beats ? 'selected' : ''}>${b}</option>`).join('')}</select></label>
    <label>Zoom <input id="look" type="range" min="4" max="16" value="${look}"></label>
    <button class="btn sm ghost" id="met">⏱️ Metrônomo</button><button class="btn sm ghost" id="gd" title="Toca o acorde quando ele chega (use fone se o microfone estiver ligado)">🔊 Guia</button>
   </div></div>
   <div class="pmain">
    <div class="stage"><canvas id="cv"></canvas><div id="pkb"></div><div class="overlay" id="ov"></div></div>
    <aside class="pside">
      <div class="now"><small>Toque agora</small><div class="big" id="cur">—</div><div id="curnotes" class="cnotes"></div><button class="btn sm" id="listen">🔊 Ouvir</button></div>
      <div class="nexts"><small>Próximos</small><div id="nexts"></div></div>
      <div class="meter"><small>O que o microfone está ouvindo</small><div id="chroma" class="chroma"></div><div id="micst" class="micst"></div></div>
      <div class="score"><div><b id="sc-ok">0</b><small>no tempo</small></div><div><b id="sc-rt">–</b><small>reação média</small></div><div><b id="sc-pr">0/${ev.length}</b><small>acordes</small></div></div>
    </aside>
   </div>
   <div class="lyrics" id="lyr"></div>
   <div class="seek" id="seek"><i></i></div>
   <div class="pfoot"><button class="btn" id="restart">⟲ Recomeçar</button><button class="btn big" id="pp">▶ Começar</button><button class="btn" id="skip">Pular ⏭</button>
   <span class="hint">Atalhos: <kbd>espaço</kbd> iniciar/pausar (no modo manual: "toquei") · <kbd>→</kbd> pular · <kbd>←</kbd> voltar</span></div></div>`;
  $('#mode').value = mode;
  const cv = $('#cv'), cx = cv.getContext('2d');
  let W = 0, H = 0, dpr = 1;
  const resize = () => { const r = cv.getBoundingClientRect(); dpr = devicePixelRatio || 1; W = r.width; H = r.height; cv.width = W * dpr; cv.height = H * dpr; cx.setTransform(dpr, 0, 0, dpr, 0, 0); };
  addEventListener('resize', resize); onLeave(() => removeEventListener('resize', resize));
  // estado
  let beat = -4, cur = 0, playing = false, last = performance.now(), arrivedAt = 0, lastHitAt = 0, flash = 0, finished = false;
  const res = { ontime: 0, times: [] };
  const col = { me: '#60a5fa', md: '#f472b6', done: '#34d399' };
  let kbFor = -1;
  function drawKb() {
    const e = ev[cur]; const marks = {};
    if (e) { e.v.me.forEach(m => marks[m] = performance.now() - flash < 400 ? 'hit' : 'me'); e.v.md.forEach(m => marks[m] = performance.now() - flash < 400 ? 'hit' : 'md'); }
    $('#pkb').innerHTML = KB.svg({ lo: LO, hi: HI, marks, height: 100, labels: 'marked' });
  }
  function side() {
    const e = ev[cur];
    $('#cur').textContent = e ? e.c : '🎉';
    if (e) { const fl = /b/.test(e.c[1] || ''); $('#curnotes').innerHTML = `<span class="me">ME ${e.v.me.map(m => M.midiName(m, fl)).join(' ')}</span><span class="md">MD ${e.v.md.map(m => M.midiName(m, fl)).join(' ')}</span><span class="pt">${[...new Set(e.v.md.map(m => M.ptName(m % 12, fl)))].join(' · ')}</span>`; }
    $('#nexts').innerHTML = ev.slice(cur + 1, cur + 5).map(x => `<span class="chip">${esc(x.c)}</span>`).join('');
    $('#sc-ok').textContent = res.ontime; $('#sc-rt').textContent = res.times.length ? (res.times.reduce((a, b) => a + b, 0) / res.times.length / 1000).toFixed(1) + 's' : '–';
    $('#sc-pr').textContent = `${Math.min(cur, ev.length)}/${ev.length}`;
    $('#seek i').style.width = (cur / ev.length * 100) + '%';
    // letra
    const li = e ? e.li : null; const L = cfg.lines;
    let sec = ''; for (let i = li ?? 0; i >= 0; i--) if (L[i] && L[i].s) { sec = L[i].s; break; }
    const lineHtml = (i, curCi) => {
      const l = L[i]; if (!l || l.s) return '';
      let line = '', colN = 0;
      l.c.forEach(([p, c], ci) => { if (p < colN) p = colN; line += ' '.repeat(p - colN) + `<b class="${ci === curCi ? 'on' : ''} ${curCi != null && ci < curCi ? 'past' : ''}">${esc(c)}</b> `; colN = p + c.length + 1; });
      return `<div class="cl">${line}</div><div class="ly">${esc(l.l) || '&nbsp;'}</div>`;
    };
    let nxt = li != null ? li + 1 : 1; while (L[nxt] && L[nxt].s) nxt++;
    $('#lyr').innerHTML = (sec ? `<div class="sec">${esc(sec)}</div>` : '') + (li != null ? `<div class="lcur">${lineHtml(li, e.ci)}</div>` : '') + `<div class="lnext">${lineHtml(nxt, null)}</div>`;
  }
  function hit(auto) {
    const e = ev[cur]; if (!e) return;
    const now = performance.now();
    e.hit = true; e.late = beat - e.start;
    res.times.push(arrivedAt ? now - arrivedAt : 0);
    if (e.late <= 1.01) res.ontime++;
    lastHitAt = now; flash = now; cur++; arrivedAt = 0; kbFor = -1;
    if (cur >= ev.length) finish();
    side();
  }
  function finish() {
    if (finished) return; finished = true; playing = false; Snd.metroStop();
    const score = Math.round(res.ontime / ev.length * 100);
    const r = { score, bpm, avg: res.times.length ? res.times.reduce((a, b) => a + b, 0) / res.times.length : 0 };
    cfg.onFinish && cfg.onFinish(r);
    $('#ov').innerHTML = `<div class="done-card"><h2>${score >= 90 ? '🔥 Mandou muito!' : score >= 60 ? '👏 Muito bem!' : '💪 Continue praticando!'}</h2>
     <div class="big">${score}%</div><p>acordes no tempo · reação média ${(r.avg / 1000).toFixed(1)}s · ${bpm} BPM</p>
     <p>${score >= 90 ? 'Suba o BPM em 5 ou troque para o modo "No tempo".' : 'Repita no mesmo BPM até passar de 90%.'}</p>
     <button class="btn big" id="again">⟲ De novo</button> <a class="btn ghost" href="${cfg.back === 'back' ? 'javascript:history.back()' : cfg.back}">Sair</a></div>`;
    $('#ov').classList.add('show'); $('#again').onclick = restart; $('#pp').textContent = '▶ Começar';
  }
  function restart() {
    ev.forEach(e => { e.hit = false; e.late = 0; e.okSince = 0; }); cur = 0; beat = -4; res.ontime = 0; res.times = []; finished = false; arrivedAt = 0;
    $('#ov').classList.remove('show'); $('#ov').innerHTML = ''; kbFor = -1; side(); start();
  }
  function start() {
    if (finished) return restart();
    if (mode !== 'manual' && Detect.st.mode === 'off') { $('#ov').innerHTML = `<div class="done-card"><h2>🎤 Ligar o microfone?</h2><p>Para o app ouvir o teclado e liberar os acordes quando você tocar certo.</p><button class="btn big" id="om">Ligar microfone</button> <button class="btn ghost" id="mm">Usar modo manual</button></div>`; $('#ov').classList.add('show');
      $('#om').onclick = async () => { if (await connectMic()) { $('#ov').classList.remove('show'); start(); } };
      $('#mm').onclick = () => { mode = 'manual'; $('#mode').value = 'manual'; $('#ov').classList.remove('show'); start(); }; return; }
    playing = true; last = performance.now(); $('#pp').textContent = '⏸ Pausar';
    if (metroOn) Snd.metroStart(bpm, beats);
  }
  function pause() { playing = false; $('#pp').textContent = '▶ Continuar'; Snd.metroStop(); }
  function seekTo(i) { cur = Math.max(0, Math.min(ev.length - 1, i)); ev.forEach((e, j) => { if (j >= cur) e.hit = false; }); beat = ev[cur].start - 2; arrivedAt = 0; kbFor = -1; finished = false; $('#ov').classList.remove('show'); side(); }
  $('#pp').onclick = () => playing ? pause() : start();
  $('#restart').onclick = restart;
  $('#skip').onclick = () => { if (ev[cur]) { cur++; arrivedAt = 0; kbFor = -1; if (cur >= ev.length) finish(); else beat = Math.max(beat, ev[cur].start - 1); side(); } };
  $('#listen').onclick = () => ev[cur] && playChord(ev[cur].c);
  $('#mode').onchange = e => { mode = e.target.value; };
  $('#bpm').onchange = e => { bpm = Math.max(30, Math.min(200, +e.target.value || 60)); Snd.setBpm(bpm); };
  $('#beats').onchange = e => { const b0 = ev[cur] ? ev[cur].start : 0; const rel = beat - b0; beats = +e.target.value; S.beats = beats; Store.save(); layout(); beat = (ev[cur] ? ev[cur].start : 0) + rel; };
  $('#look').oninput = e => { look = +e.target.value; S.lookahead = look; Store.save(); };
  $('#met').onclick = e => { metroOn = !metroOn; e.target.classList.toggle('ok', metroOn); if (metroOn && playing) Snd.metroStart(bpm, beats); else Snd.metroStop(); };
  $('#gd').onclick = e => { guide = !guide; e.target.classList.toggle('ok', guide); };
  $('#seek').onclick = e => { const r = e.currentTarget.getBoundingClientRect(); seekTo(Math.floor((e.clientX - r.left) / r.width * ev.length)); };
  const key = e => {
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') return;
    if (e.code === 'Space') { e.preventDefault(); if (mode === 'manual' && playing) hit(); else playing ? pause() : start(); }
    if (e.code === 'ArrowRight') $('#skip').click();
    if (e.code === 'ArrowLeft') seekTo(cur - 1);
  };
  addEventListener('keydown', key); onLeave(() => removeEventListener('keydown', key));
  $('#chroma').innerHTML = M.SHARP.map(n => `<div><i></i><span>${n}</span></div>`).join('');

  function frame(now) {
    raf = requestAnimationFrame(frame);
    const dt = Math.min(0.1, (now - last) / 1000); last = now;
    if (!W) resize();
    const e = ev[cur];
    if (playing && !finished) {
      beat += dt * bpm / 60;
      if (e) {
        if (beat >= e.start && !arrivedAt) { arrivedAt = now; if (guide) playChord(e.c); }
        if (mode === 'wait' && beat > e.start) beat = e.start;
        if (mode === 'tempo' && beat > e.start + e.dur) { e.late = 99; cur++; arrivedAt = 0; kbFor = -1; if (cur >= ev.length) finish(); side(); }
        // detecção
        if (mode !== 'manual' && Detect.st.mode !== 'off' && ev[cur] === e && beat >= e.start - 1 && now > Detect.st.muteUntil) {
          if (Detect.match(e.req, e.pcs) === 1) {
            if (!e.okSince) e.okSince = now;
            if (now - e.okSince > 80 && (!e.needOnset || Detect.lastOnset() > lastHitAt)) hit();
          } else e.okSince = 0;
        }
      }
    }
    if (kbFor !== cur || (flash && now - flash < 450)) { drawKb(); kbFor = cur; }
    // medidor
    const mx = Math.max(1e-6, ...Detect.st.chroma); const act = Detect.activePcs();
    $$('#chroma > div').forEach((d, i) => { d.firstChild.style.height = Math.round(Detect.st.chroma[i] / mx * 100) + '%'; d.className = (e && e.req.includes(i) ? 'req ' : '') + (act.includes(i) ? 'act' : ''); });
    $('#micst').textContent = Detect.st.mode === 'off' ? 'Microfone desligado' : Detect.st.mode === 'midi' ? 'MIDI conectado' : (Detect.st.level > Detect.st.gate ? 'Ouvindo: ' + act.map(p => M.SHARP[p]).join(' ') : 'Silêncio…');
    // canvas
    cx.clearRect(0, 0, W, H);
    const ppb = H / look, sx = W / g.width;
    for (let b = Math.ceil(beat); b < beat + look + 1; b++) { const y = H - (b - beat) * ppb; cx.fillStyle = b % beats === 0 ? 'rgba(255,255,255,.10)' : 'rgba(255,255,255,.035)'; cx.fillRect(0, y, W, 1); }
    // faixas das teclas pretas
    for (const [m, k] of g.keys) if (k.black) { cx.fillStyle = 'rgba(0,0,0,.18)'; cx.fillRect(k.x * sx, 0, k.w * sx, H); }
    for (let i = Math.max(0, cur - 2); i < ev.length; i++) {
      const x = ev[i]; const yb = H - (x.start - beat) * ppb; const yt = yb - x.dur * ppb + 4;
      if (yt > H) continue; if (yb < -10) break;
      const isCur = i === cur;
      const notes = [...x.v.me.map(m => [m, 'me']), ...x.v.md.map(m => [m, 'md'])];
      let minX = 1e9;
      for (const [m, t] of notes) {
        const k = g.keys.get(m); if (!k) continue;
        const X = k.x * sx + 1, Wd = k.w * sx - 2; minX = Math.min(minX, X);
        cx.fillStyle = x.hit ? col.done : col[t]; cx.globalAlpha = x.hit ? 0.5 : isCur ? 1 : 0.8;
        if (isCur && !x.hit) { cx.shadowColor = col[t]; cx.shadowBlur = 16; }
        roundRect(cx, X, yt, Wd, yb - yt, 5); cx.fill(); cx.shadowBlur = 0; cx.globalAlpha = 1;
      }
      // nome do acorde
      const mdK = x.v.md.length ? g.keys.get(x.v.md[0]) : null; const lx = mdK ? mdK.x * sx : minX;
      cx.font = `800 ${isCur ? 22 : 16}px system-ui, sans-serif`; cx.fillStyle = x.hit ? '#a7f3d0' : '#fff'; cx.textBaseline = 'bottom';
      const tw = cx.measureText(x.c).width; const ty = Math.min(H - 6, Math.max(26, yb - 6));
      cx.fillStyle = 'rgba(10,12,24,.7)'; roundRect(cx, lx - 6 - tw - 10, ty - 26, tw + 12, 26, 6); cx.fill();
      cx.fillStyle = x.hit ? '#a7f3d0' : isCur ? '#fde047' : '#fff'; cx.fillText(x.c, lx - 10 - tw, ty - 2);
    }
    // linha de acerto
    cx.fillStyle = mode === 'wait' && e && beat >= e.start && playing ? '#fde047' : 'rgba(255,255,255,.5)'; cx.fillRect(0, H - 3, W, 3);
    if (beat < 0 && playing) { cx.font = '800 64px system-ui'; cx.fillStyle = 'rgba(255,255,255,.85)'; cx.textAlign = 'center'; cx.fillText(Math.ceil(-beat), W / 2, H / 2); cx.textAlign = 'left'; }
    if (!playing && !finished) { cx.font = '700 20px system-ui'; cx.fillStyle = 'rgba(255,255,255,.8)'; cx.textAlign = 'center'; cx.fillText(mode === 'wait' ? 'Os acordes caem até a linha e ESPERAM você tocar. Aperte ▶' : 'Aperte ▶ para começar', W / 2, H / 2); cx.textAlign = 'left'; }
  }
  let raf = requestAnimationFrame(frame);
  onLeave(() => cancelAnimationFrame(raf));
  side(); resize();
}
function roundRect(c, x, y, w, h, r) { r = Math.min(r, h / 2, w / 2); c.beginPath(); c.moveTo(x + r, y); c.arcTo(x + w, y, x + w, y + h, r); c.arcTo(x + w, y + h, x, y + h, r); c.arcTo(x, y + h, x, y, r); c.arcTo(x, y, x + w, y, r); c.closePath(); }

// ================= TREINOS =================
const DRILLS = [
  ['mic', '🎤', 'Microfone e calibração', 'Veja o que o app está ouvindo e ajuste a sensibilidade.'],
  ['notes', '🎯', 'Encontre a nota', 'Aparece uma nota — toque no teclado o mais rápido possível.'],
  ['chords', '⚡', 'Acordes relâmpago', 'Acordes aleatórios: monte cada um o mais rápido que puder.'],
  ['scale', '🎼', 'Escalas nota a nota', 'Toque a escala; o app confere cada nota.'],
  ['inversions', '🔄', 'Inversões', 'Fundamental, 1ª e 2ª inversão de cada acorde.'],
  ['ear-quality', '👂', 'Ouvido: maior x menor', 'O app toca um acorde. É maior ou menor?'],
  ['ear-intervals', '📏', 'Ouvido: intervalos', 'Duas notas: qual a distância entre elas?'],
  ['ear-degree', '🗝️', 'Ouvido: graus do campo', 'Depois da cadência, qual grau soou?']
];
function treinos() {
  view().innerHTML = `<h1>🎯 Treinos</h1><p class="sub">Treinos curtos e interativos. Com o microfone ligado, o app ouve o teclado e confere na hora.</p>
  <div class="grid3">${DRILLS.map(([id, ic, t, d]) => `<a class="card plan" href="#/treinos/${id}"><h3>${ic} ${t}</h3><p>${d}</p></a>`).join('')}</div>`;
}
function treino(id) {
  const fn = { mic: drillMic, notes: drillNotes, chords: drillChords, scale: drillScale, inversions: drillInv, 'ear-quality': () => drillEar('quality'), 'ear-intervals': () => drillEar('interval'), 'ear-degree': () => drillEar('degree') }[id];
  if (!fn) return treinos();
  const D = DRILLS.find(x => x[0] === id);
  view().innerHTML = `<a class="back" href="#/treinos">← Treinos</a><h1>${D[1]} ${D[2]}</h1><p class="sub">${D[3]}</p><div id="dr"></div>`;
  fn($('#dr'));
}
function micGate(el, then) {
  if (Detect.st.mode !== 'off') return then();
  el.innerHTML = `<div class="card warnc"><p><b>Ligue o microfone</b> para o app ouvir seu teclado. Deixe o computador perto do alto-falante do CTK-1200, volume em ~70%, timbre de piano.</p>
  <button class="btn big" id="gm">🎤 Ligar microfone</button> <button class="btn ghost" id="gx">Continuar sem microfone (clicar na tela)</button></div>`;
  $('#gm').onclick = async () => { if (await connectMic()) then(); };
  $('#gx').onclick = () => then(true);
}
function drillMic(el) {
  micGate(el, () => {
    el.innerHTML = `<div class="card"><div class="chroma big" id="chr">${M.SHARP.map(n => `<div><i></i><span>${n}</span></div>`).join('')}</div>
    <p id="hear" class="hear">…</p><div class="lvlm"><i id="lv"></i></div>
    <label>Sensibilidade <input type="range" id="sens" min="0.12" max="0.7" step="0.01" value="${Detect.st.sens}"> <span id="sv"></span></label>
    <p><button class="btn" id="cal">🔇 Calibrar silêncio (3 s)</button> <button class="btn ghost" id="midi">Usar MIDI em vez do microfone</button></p>
    <div class="tip">Como calibrar: <b>1)</b> fique em silêncio e clique em "Calibrar silêncio". <b>2)</b> toque um acorde de C (Dó Mi Sol) e segure: as barras C, E e G devem ficar acesas. <b>3)</b> Se aparecerem notas a mais, arraste a sensibilidade para a direita; se faltar nota, para a esquerda.</div>
    <div class="tip">Dicas: timbre de piano, sem ritmo, volume médio-alto, microfone apontado para o alto-falante do teclado. Ambiente silencioso ajuda muito. Fone de ouvido NO teclado desliga o alto-falante — não use fone no teclado.</div></div>`;
    const sv = () => $('#sv').textContent = Detect.st.sens < 0.25 ? 'sensível' : Detect.st.sens > 0.5 ? 'exigente' : 'equilibrada';
    sv();
    $('#sens').oninput = e => { Detect.st.sens = +e.target.value; Store.d.settings.sens = Detect.st.sens; Store.save(); sv(); };
    $('#midi').onclick = connectMidi;
    $('#cal').onclick = () => {
      const b = $('#cal'); b.disabled = true; b.textContent = 'Medindo… fique em silêncio'; const vals = [];
      const iv = setInterval(() => vals.push(Detect.st.level), 50);
      setTimeout(() => { clearInterval(iv); const mx = Math.max(...vals.filter(v => v > -200)); const gate = Math.min(-30, Math.max(-85, mx + 8)); Detect.st.gate = gate; Store.d.settings.gate = gate; Store.save(); b.disabled = false; b.textContent = `✅ Calibrado (limiar ${gate.toFixed(0)} dB) — calibrar de novo`; }, 3000);
    };
    const off = Detect.on(() => {
      const mx = Math.max(1e-6, ...Detect.st.chroma); const act = Detect.activePcs();
      $$('#chr > div').forEach((d, i) => { d.firstChild.style.height = Math.round(Detect.st.chroma[i] / mx * 100) + '%'; d.className = act.includes(i) ? 'act' : ''; });
      $('#lv').style.width = Math.max(0, Math.min(100, (Detect.st.level + 100))) + '%';
      $('#lv').className = Detect.st.level > Detect.st.gate ? 'on' : '';
      let guess = '';
      if (act.length >= 3) { for (const r of act) for (const q of ['', 'm']) { const pcs = M.chordPcs(M.noteName(r) + q); if (pcs.every(p => act.includes(p))) { guess = M.noteName(r) + q; break; } } }
      $('#hear').innerHTML = Detect.st.level > Detect.st.gate ? `Notas: <b>${act.map(p => M.SHARP[p]).join(' ')}</b>${guess ? ` · Acorde provável: <b>${guess}</b>` : ''}` : 'Silêncio…';
    });
    onLeave(off);
  });
}
// laço genérico de detecção p/ treinos
function listenLoop(check) { let r; const f = () => { r = requestAnimationFrame(f); if (performance.now() > Detect.st.muteUntil) check(); }; r = requestAnimationFrame(f); onLeave(() => cancelAnimationFrame(r)); }

function drillNotes(el) {
  micGate(el, noMic => {
    let target, t0, streak = 0, n = 0, ok = 0, times = [], holdSince = 0, lastPc = -1;
    const next = () => { let t; do t = Math.floor(Math.random() * 12); while (t === lastPc); target = t; lastPc = t; t0 = performance.now(); holdSince = 0; draw(); };
    const draw = (msg = '') => {
      el.innerHTML = `<div class="card center"><small>Toque a nota</small><div class="huge">${M.ptName(target)} <span>(${M.noteName(target)}${M.noteName(target) !== M.FLAT[target] ? ' / ' + M.FLAT[target] : ''})</span></div>
      <p class="msg">${msg}</p><div class="stats"><span>Acertos: <b>${ok}/${n}</b></span><span>Sequência: <b>${streak}</b></span><span>Tempo médio: <b>${times.length ? (times.reduce((a, b) => a + b) / times.length / 1000).toFixed(1) : '–'}s</b></span></div>
      <div id="kbn">${KB.svg({ lo: 48, hi: 83, labels: 'none', height: 110 })}</div><p class="sub">${noMic ? 'Clique na tecla certa na tela.' : 'Toque no seu teclado (qualquer oitava). Você também pode clicar na tela.'}</p>
      <button class="btn ghost" id="hint">Mostrar onde fica</button></div>`;
      $$('#kbn rect').forEach(r => r.onclick = () => answer(+r.dataset.m % 12));
      $('#hint').onclick = () => { const marks = {}; for (let m = 48; m <= 83; m++) if (m % 12 === target) marks[m] = { cls: 'md', label: M.noteName(target) }; $('#kbn').innerHTML = KB.svg({ lo: 48, hi: 83, marks, labels: 'none', height: 110 }); streak = 0; };
    };
    const answer = pcv => {
      n++;
      if (pcv === target) { ok++; streak++; const t = performance.now() - t0; times.push(t); Store.d.drills.notesOk = (Store.d.drills.notesOk || 0) + 1; if (streak % 10 === 0) Store.addXp(10, `${streak} seguidas!`); else Store.save(); next(); }
      else { streak = 0; draw('❌ Essa foi ' + M.ptName(pcv) + '. Tente de novo!'); }
    };
    next();
    if (!noMic) listenLoop(() => {
      const d = Detect.dominantPc();
      if (d === target) { if (!holdSince) holdSince = performance.now(); if (performance.now() - holdSince > 150 && Detect.lastOnset() > t0 - 50) answer(d); }
      else holdSince = 0;
    });
  });
}
function drillChords(el) {
  const q = query();
  const set = (q.set || 'C D E F G A B Cm Dm Em Fm Gm Am Bm').split(/\s+/).filter(Boolean);
  micGate(el, noMic => {
    let target, t0, n = 0, times = [], okSince = 0, last = '', showHint = false;
    const next = () => { let t; do t = set[Math.floor(Math.random() * set.length)]; while (t === last && set.length > 1); target = t; last = t; t0 = performance.now(); okSince = 0; showHint = false; draw(); };
    const draw = () => {
      el.innerHTML = `<div class="card center"><small>Monte o acorde</small><div class="huge">${esc(target)}</div>
      <div class="stats"><span>Feitos: <b>${n}</b></span><span>Média: <b>${times.length ? (times.reduce((a, b) => a + b) / times.length / 1000).toFixed(1) : '–'}s</b></span><span>Melhor: <b>${times.length ? (Math.min(...times) / 1000).toFixed(1) : '–'}s</b></span></div>
      <div id="hk">${showHint ? chordInfoHtml(target) : ''}</div>
      <p><button class="btn ghost" id="h">💡 Mostrar notas</button> <button class="btn ghost" data-play="${esc(target)}">🔊 Ouvir</button> ${noMic ? '<button class="btn" id="ok">Toquei ✔</button>' : ''} <button class="btn ghost" id="sk">Pular</button></p>
      <p class="sub">Set: ${set.join(' ')}</p></div>`;
      $('#h').onclick = () => { showHint = true; draw(); };
      $('#sk').onclick = next;
      if ($('#ok')) $('#ok').onclick = done;
    };
    const done = () => { n++; times.push(performance.now() - t0); Store.d.drills.chordOk = (Store.d.drills.chordOk || 0) + 1; if (n % 10 === 0) Store.addXp(15, `${n} acordes`); else { Store.save(); Store.checkBadges(); } next(); };
    next();
    if (!noMic) listenLoop(() => {
      const p = M.parseChord(target); const req = M.requiredPcs(p), pcs = M.chordPcs(p).concat([p.bassPc]);
      if (Detect.match(req, pcs) === 1) { if (!okSince) okSince = performance.now(); if (performance.now() - okSince > 120 && Detect.lastOnset() > t0 - 100) done(); } else okSince = 0;
    });
  });
}
function drillScale(el) {
  const q = query();
  let root = q.root || 'C', scale = q.scale || 'Maior (jônio)';
  micGate(el, noMic => {
    let seq, i, holdSince = 0, t0 = 0, lastHit = 0;
    const build = () => {
      const r = M.pc(root);
      if (scale === 'five') seq = [0, 2, 4, 5, 7, 5, 4, 2, 0].map(x => 60 + r + x);
      else { const sc = M.SCALES[scale]; const up = sc.map(x => 60 + r + x).concat([72 + r]); seq = up.concat(up.slice(0, -1).reverse()); }
      i = 0; t0 = performance.now(); draw();
    };
    const draw = () => {
      const fl = M.useFlats(M.pc(root), /menor/i.test(scale)); const marks = {};
      seq.forEach((m, j) => { if (!marks[m]) marks[m] = { cls: 'scale', label: M.noteName(m % 12, fl) }; });
      if (seq[i] != null) marks[seq[i]] = { cls: 'md', label: M.noteName(seq[i] % 12, fl) };
      el.innerHTML = `<div class="card center"><div class="filters"><select id="sr">${M.SHARP.map((n, k) => `<option value="${M.noteName(k, M.useFlats(k))}">${M.noteName(k, M.useFlats(k))}</option>`).join('')}</select>
      <select id="ss"><option value="five">5 dedos (C D E F G)</option>${Object.keys(M.SCALES).filter(k => k !== 'Cromática').map(k => `<option>${k}</option>`).join('')}</select> <button class="btn ghost sm" id="pl">🔊 Ouvir</button></div>
      ${i >= seq.length ? `<div class="huge">🎉 Escala completa!</div><p>Tempo: ${((performance.now() - t0) / 1000).toFixed(1)}s</p><button class="btn" id="rep">De novo</button>` : `<small>Próxima nota (${i + 1}/${seq.length})</small><div class="huge">${M.ptName(seq[i] % 12, fl)} <span>${M.noteName(seq[i] % 12, fl)}</span></div>`}
      <div class="seqline">${seq.map((m, j) => `<span class="${j < i ? 'past' : j === i ? 'on' : ''}">${M.noteName(m % 12, fl)}</span>`).join('')}</div>
      <div id="kbs">${KB.svg({ lo: 60, hi: 84, marks, height: 110 })}</div><p class="sub">${noMic ? 'Clique nas teclas na tela.' : 'Toque uma nota por vez, soltando a anterior.'}</p></div>`;
      $('#sr').value = root; $('#ss').value = scale;
      $('#sr').onchange = e => { root = e.target.value; build(); };
      $('#ss').onchange = e => { scale = e.target.value; build(); };
      $('#pl').onclick = () => { seq.forEach((m, j) => Snd.note(m, j * 0.3, 0.5)); Detect.st.muteUntil = performance.now() + seq.length * 300 + 800; };
      if ($('#rep')) $('#rep').onclick = build;
      $$('#kbs rect').forEach(r => r.onclick = () => { if (+r.dataset.m % 12 === seq[i] % 12) adv(); });
    };
    const adv = () => { i++; lastHit = performance.now(); holdSince = 0; if (i >= seq.length) { Store.addXp(8, 'escala'); } draw(); };
    build();
    if (!noMic) listenLoop(() => {
      if (i >= seq.length) return;
      const d = Detect.dominantPc();
      const same = i > 0 && seq[i] % 12 === seq[i - 1] % 12;
      if (d === seq[i] % 12 && (!same || Detect.lastOnset() > lastHit)) { if (!holdSince) holdSince = performance.now(); if (performance.now() - holdSince > 90 && Detect.lastOnset() > lastHit - 30) adv(); } else holdSince = 0;
    });
  });
}
function drillInv(el) {
  const chords = ['C', 'F', 'G', 'D', 'A', 'E', 'Am', 'Dm', 'Em'];
  let ci = 0, inv = 0;
  micGate(el, noMic => {
    let okSince = 0, t0 = performance.now();
    const draw = () => {
      const c = M.parseChord(chords[ci]); const base = [0, c.third, 7].map(x => 60 + c.rootPc + x);
      const notes = base.map((m, j) => j < inv ? m + 12 : m).sort((a, b) => a - b);
      const marks = {}; notes.forEach(m => marks[m] = { cls: 'md', label: M.noteName(m % 12) });
      el.innerHTML = `<div class="card center"><small>Toque</small><div class="huge">${chords[ci]} <span>${['estado fundamental', '1ª inversão', '2ª inversão'][inv]}</span></div>
      ${KB.svg({ lo: 60, hi: 84, marks, height: 110 })}<p>${notes.map(m => M.midiName(m)).join(' – ')} <button class="btn sm ghost" id="pl">🔊</button></p>
      <p class="sub">${noMic ? 'Toque no teclado e clique em "Próximo".' : 'O microfone confere as notas; a posição (inversão) é com você — olhe o desenho.'}</p>
      <button class="btn" id="nx">Próximo ▶</button></div>`;
      $('#pl').onclick = () => { Snd.chord(notes); Detect.st.muteUntil = performance.now() + 1800; };
      $('#nx').onclick = adv;
    };
    const adv = () => { inv++; if (inv > 2) { inv = 0; ci = (ci + 1) % chords.length; Store.addXp(3, 'inversões'); } t0 = performance.now(); okSince = 0; draw(); };
    draw();
    if (!noMic) listenLoop(() => {
      const p = M.parseChord(chords[ci]);
      if (Detect.match(M.requiredPcs(p), M.chordPcs(p)) === 1) { if (!okSince) okSince = performance.now(); if (performance.now() - okSince > 200 && Detect.lastOnset() > t0) adv(); } else okSince = 0;
    });
  });
}
function drillEar(kind) {
  const el = $('#dr'); let n = 0, ok = 0, streak = 0, cur = null, answered = false;
  const IV = [[3, '3ª menor'], [4, '3ª maior'], [5, '4ª justa'], [7, '5ª justa'], [12, 'Oitava'], [2, '2ª maior'], [9, '6ª maior'], [10, '7ª menor']];
  let ivSet = 4;
  const play = () => {
    Detect.st.muteUntil = performance.now() + 4000;
    if (kind === 'quality') { const r = 55 + cur.root; Snd.chord([r, r + cur.q, r + 7], 0, 1.8); }
    if (kind === 'interval') { const r = 55 + cur.root; Snd.note(r, 0, 1); Snd.note(r + cur.iv, 0.7, 1); Snd.note(r, 1.6, 1.2); Snd.note(r + cur.iv, 1.6, 1.2); }
    if (kind === 'degree') {
      const k = cur.key; const ch = (d, t) => { const f = M.field(k, false, false)[d - 1].chord; const v = M.voicing(f); Snd.chord([...v.me, ...v.md], t, 0.9); };
      ch(1, 0); ch(4, 0.9); ch(5, 1.8); ch(1, 2.7); ch(cur.deg, 4.0);
      Detect.st.muteUntil = performance.now() + 6000;
    }
  };
  const newQ = () => {
    answered = false; const root = Math.floor(Math.random() * 12);
    if (kind === 'quality') cur = { root, q: Math.random() < 0.5 ? 3 : 4 };
    if (kind === 'interval') cur = { root, iv: IV[Math.floor(Math.random() * ivSet)][0] };
    if (kind === 'degree') cur = { key: [0, 7, 2, 9, 5][Math.floor(Math.random() * 5)], deg: [1, 2, 3, 4, 5, 6][Math.floor(Math.random() * 6)] };
    draw(); setTimeout(play, 250);
  };
  const opts = () => kind === 'quality' ? [[4, 'Maior 😀'], [3, 'Menor 😔']] : kind === 'interval' ? IV.slice(0, ivSet) : [[1, 'I'], [2, 'ii'], [3, 'iii'], [4, 'IV'], [5, 'V'], [6, 'vi']];
  const correct = () => kind === 'quality' ? cur.q : kind === 'interval' ? cur.iv : cur.deg;
  const draw = (msg = '') => {
    el.innerHTML = `<div class="card center">${kind === 'degree' ? '<p class="sub">Primeiro soa a cadência I–IV–V–I (para fixar o tom), depois o acorde misterioso.</p>' : ''}
    <button class="btn big" id="rp">🔊 Ouvir de novo</button>
    <div class="opts big">${opts().map(([v, t]) => `<button class="opt" data-v="${v}">${t}</button>`).join('')}</div>
    <p class="msg">${msg}</p><div class="stats"><span>Acertos <b>${ok}/${n}</b></span><span>Sequência <b>${streak}</b></span></div>
    ${kind === 'interval' ? `<label>Dificuldade <select id="lv"><option value="4">Fácil (3ªs, 4ª, 5ª)</option><option value="6">Médio (+ oitava, 2ª)</option><option value="8">Difícil (+ 6ª, 7ª)</option></select></label>` : ''}
    ${answered ? '<button class="btn" id="nx">Próxima ▶</button>' : ''}</div>`;
    $('#rp').onclick = play;
    if ($('#lv')) { $('#lv').value = ivSet; $('#lv').onchange = e => { ivSet = +e.target.value; newQ(); }; }
    if ($('#nx')) $('#nx').onclick = newQ;
    $$('.opt', el).forEach(b => b.onclick = () => {
      if (answered) return; answered = true; n++;
      const good = +b.dataset.v === correct();
      if (good) { ok++; streak++; Store.d.drills.earOk = (Store.d.drills.earOk || 0) + 1; if (streak % 5 === 0) Store.addXp(10, `${streak} seguidas`); else { Store.save(); Store.checkBadges(); } }
      else streak = 0;
      const right = opts().find(o => o[0] === correct())[1];
      let extra = '';
      if (kind === 'degree') extra = ` (tom de ${M.noteName(cur.key)}: ${M.field(cur.key, false, false)[cur.deg - 1].chord})`;
      draw(good ? '✅ Acertou!' + extra : '❌ Era ' + right + extra);
      if (good) setTimeout(() => { if (answered) newQ(); }, 1200);
    });
  };
  newQ();
}

// ================= DICIONÁRIO DE ACORDES =================
function acordes() {
  const q = query(); let root = q.r || 'C', type = q.t ?? '';
  const draw = () => {
    const sym = root + type; const p = M.parseChord(sym);
    const ints = [...p.ints].sort((a, b) => a - b);
    const INAME = { 0: 'Fundamental', 2: '2ª/9ª', 3: '3ª menor', 4: '3ª maior', 5: '4ª', 6: '5ª diminuta', 7: '5ª justa', 8: '5ª aumentada', 9: '6ª / 7ª diminuta', 10: '7ª menor', 11: '7ª maior', 13: '9ª menor', 14: '9ª', 15: '9ª aumentada', 17: '11ª', 18: '11ª aumentada', 20: '13ª menor', 21: '13ª' };
    const fl = M.useFlats(M.pc(root), p.minor) || /b/.test(root);
    const tri = [...p.ints].filter(i => i < 12).sort((a, b) => a - b);
    const invs = [0, 1, 2].map(k => tri.map((iv, j) => 60 + p.rootPc + iv + (j < k ? 12 : 0)).sort((a, b) => a - b));
    view().innerHTML = `<h1>📘 Dicionário de acordes</h1><p class="sub">Escolha a nota e o tipo. Clique para ouvir. ME = mão esquerda (baixo), MD = mão direita.</p>
    <div class="chips">${M.SHARP.map((n, k) => { const nm = [1, 3, 6, 8, 10].includes(k) ? (k === 6 || k === 1 ? n : M.FLAT[k]) : n; return `<button class="chip ${nm === root ? 'on' : ''}" data-r="${nm}">${nm}</button>`; }).join('')}</div>
    <div class="chips">${M.DICT_TYPES.map(t => `<button class="chip sm ${t === type ? 'on' : ''}" data-t="${esc(t)}">${esc(root + t)}</button>`).join('')}</div>
    <div class="card">${chordInfoHtml(sym)}
    <p><b>${esc(M.QUALITY_NAMES[type] || '')}</b> · Fórmula: ${ints.map(i => `<span class="tag">${INAME[i] || i}: ${M.noteName((p.rootPc + i) % 12, fl)}</span>`).join(' ')}</p>
    <h3>Inversões (MD)</h3><div class="grid3">${invs.map((ns, k) => { const mk = {}; ns.forEach(m => mk[m] = 'md'); return `<div><small>${['Fundamental', '1ª inversão', '2ª inversão'][k]}: ${ns.map(m => M.noteName(m % 12, fl)).join(' ')}</small>${KB.svg({ lo: 60, hi: 83, marks: mk, height: 70 })}</div>`; }).join('')}</div>
    <p><a class="btn" href="#/play/seq?${new URLSearchParams({ c: `${sym} ${sym} ${sym} ${sym}`, bpm: 50, beats: 4, t: 'Treinar ' + sym })}">🎯 Treinar este acorde</a></p></div>`;
    $$('[data-r]').forEach(b => b.onclick = () => { root = b.dataset.r; draw(); playChord(root + type); });
    $$('[data-t]').forEach(b => b.onclick = () => { type = b.dataset.t; draw(); playChord(root + type); });
  };
  draw();
}

// ================= FERRAMENTAS =================
function ferramentas() {
  let bpm = 80, bb = 4, key = 'C', minor = false, tet = false;
  view().innerHTML = `<h1>🧰 Ferramentas</h1>
  <section class="card"><h2>⏱️ Metrônomo</h2><div class="metro"><div class="mbig" id="mb">80</div><div class="beats" id="bts"></div></div>
  <input type="range" id="mr" min="30" max="220" value="80"><div class="chips"><button class="btn sm" data-d="-5">−5</button><button class="btn sm" data-d="-1">−1</button><button class="btn sm" data-d="1">+1</button><button class="btn sm" data-d="5">+5</button>
  <select id="mbb"><option>2</option><option>3</option><option selected>4</option><option>6</option></select><button class="btn" id="tap">👆 Tap</button><button class="btn big" id="ms">▶ Iniciar</button></div></section>
  <section class="card"><h2>🗝️ Campo harmônico</h2><div class="filters"><select id="fk">${['C', 'C#', 'Db', 'D', 'Eb', 'E', 'F', 'F#', 'G', 'Ab', 'A', 'Bb', 'B'].map(k => `<option>${k}</option>`).join('')}</select>
  <select id="fm"><option value="0">Maior</option><option value="1">Menor</option></select><select id="ft"><option value="0">Tríades</option><option value="1">Tétrades</option></select></div><div id="fld"></div>
  <p class="sub">Progressões prontas neste tom:</p><div class="chips" id="progs"></div></section>
  <section class="card"><h2>🔁 Transpositor</h2><textarea id="trin" rows="2" placeholder="Cole os acordes: G D/F# Em C">G D/F# Em7 Cadd9</textarea>
  <div class="chips"><button class="btn sm" id="tdn">−½ tom</button><button class="btn sm" id="tup">+½ tom</button><span id="trs">0</span></div><div class="trout" id="trout"></div></section>
  <section class="card"><h2>⭕ Círculo das quintas</h2><div id="circ"></div><p class="sub">Vizinhos no círculo = tons parecidos (1 acidente de diferença). O de dentro é a relativa menor.</p></section>
  <section class="card"><h2>🎼 Escalas</h2><div class="filters"><select id="sr">${['C', 'Db', 'D', 'Eb', 'E', 'F', 'F#', 'G', 'Ab', 'A', 'Bb', 'B'].map(k => `<option>${k}</option>`).join('')}</select><select id="ssc">${Object.keys(M.SCALES).map(k => `<option>${k}</option>`).join('')}</select></div><div id="scv"></div></section>`;
  // metrônomo
  const setB = v => { bpm = Math.max(30, Math.min(220, v)); $('#mb').textContent = bpm; $('#mr').value = bpm; Snd.setBpm(bpm); };
  const beatsUi = () => $('#bts').innerHTML = Array.from({ length: bb }, (_, i) => `<i data-b="${i}"></i>`).join('');
  beatsUi();
  $('#mr').oninput = e => setB(+e.target.value);
  $$('[data-d]').forEach(b => b.onclick = () => setB(bpm + +b.dataset.d));
  $('#mbb').onchange = e => { bb = +e.target.value; beatsUi(); if (Snd.metro.on) start(); };
  const start = () => Snd.metroStart(bpm, bb, b => $$('#bts i').forEach((x, i) => x.className = i === b ? (b === 0 ? 'on acc' : 'on') : ''));
  $('#ms').onclick = () => { if (Snd.metro.on) { Snd.metroStop(); $('#ms').textContent = '▶ Iniciar'; } else { start(); $('#ms').textContent = '⏹ Parar'; } };
  let taps = []; $('#tap').onclick = () => { const t = performance.now(); taps = taps.filter(x => t - x < 3000); taps.push(t); if (taps.length > 1) setB(Math.round(60000 / ((taps[taps.length - 1] - taps[0]) / (taps.length - 1)))); };
  // campo
  const fdraw = () => {
    const k = M.pc(key); $('#fld').innerHTML = fieldTable(k, minor, tet);
    const P = minor ? [['i–VI–VII', [1, 6, 7]], ['i–iv–v', [1, 4, 5]], ['i–VII–VI–VII', [1, 7, 6, 7]]] : [['I–V–vi–IV', [1, 5, 6, 4]], ['vi–IV–I–V', [6, 4, 1, 5]], ['I–IV–V', [1, 4, 5, 1]], ['I–vi–ii–V', [1, 6, 2, 5]], ['I–iii–IV–V', [1, 3, 4, 5]]];
    $('#progs').innerHTML = P.map(([n, d]) => { const cs = M.progression(k, d, minor); return `<a class="btn sm" href="#/play/seq?${new URLSearchParams({ c: cs.concat(cs).join(' '), bpm: 70, beats: 4, t: n + ' em ' + key + (minor ? 'm' : '') })}">${n}: ${cs.join(' ')}</a>`; }).join('');
  };
  $('#fk').onchange = e => { key = e.target.value; fdraw(); }; $('#fm').onchange = e => { minor = e.target.value === '1'; fdraw(); }; $('#ft').onchange = e => { tet = e.target.value === '1'; fdraw(); };
  fdraw();
  // transpositor
  let ts = 0; const tdraw = () => { $('#trs').textContent = (ts > 0 ? '+' : '') + ts + ' semitons'; const cs = $('#trin').value.split(/\s+/).filter(Boolean); const k = M.detectKey(cs); const fl = k ? M.useFlats((k.k + ts + 12) % 12, k.minor) : false; $('#trout').innerHTML = cs.map(c => M.parseChord(c) ? `<button class="chip" data-play="${esc(M.transposeChord(c, ts, fl))}">${esc(M.transposeChord(c, ts, fl))}</button>` : esc(c)).join(' '); };
  $("#tdn").onclick = () => { ts--; tdraw(); }; $("#tup").onclick = () => { ts++; tdraw(); }; $('#trin').oninput = tdraw; tdraw();
  // círculo
  const order = [0, 7, 2, 9, 4, 11, 6, 1, 8, 3, 10, 5]; const R = 140, r2 = 95, cxm = 170;
  $('#circ').innerHTML = `<svg viewBox="0 0 340 340" class="circle">${order.map((k, i) => { const a = (i / 12) * Math.PI * 2 - Math.PI / 2; const fl = M.useFlats(k); const x = cxm + Math.cos(a) * R, y = cxm + Math.sin(a) * R, x2 = cxm + Math.cos(a) * r2, y2 = cxm + Math.sin(a) * r2;
    const acc = [0, 1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1][i]; return `<g class="ck" data-play="${M.noteName(k, fl)}"><circle cx="${x}" cy="${y}" r="24"/><text x="${x}" y="${y + 6}">${M.noteName(k, fl)}</text></g><g class="ck mi" data-play="${M.noteName((k + 9) % 12, fl)}m"><circle cx="${x2}" cy="${y2}" r="18"/><text x="${x2}" y="${y2 + 5}">${M.noteName((k + 9) % 12, fl)}m</text></g><text class="acc" x="${cxm + Math.cos(a) * (R + 34) - 0}" y="${cxm + Math.sin(a) * (R + 34) + 4}">${acc ? acc + (i <= 6 ? '#' : 'b') : ''}</text>`; }).join('')}</svg>`;
  // escalas
  const sdraw = () => { const el = $('#scv'); el.innerHTML = `<div class="w-scale" data-root="${$('#sr').value}" data-scale="${$('#ssc').value}"></div>`; hydrate(el); };
  $('#sr').onchange = sdraw; $('#ssc').onchange = sdraw; sdraw();
}

// ================= PROGRESSO =================
function progresso() {
  const d = Store.d, lv = Store.level();
  const allL = LESSONS.reduce((a, m) => a + m.lessons.length, 0);
  const days = []; const t = new Date();
  for (let i = 83; i >= 0; i--) { const x = new Date(t); x.setDate(t.getDate() - i); const k = x.getFullYear() + '-' + String(x.getMonth() + 1).padStart(2, '0') + '-' + String(x.getDate()).padStart(2, '0'); days.push([k, d.log[k] || 0]); }
  const tt = Store.totalTime();
  view().innerHTML = `<h1>🏆 Seu progresso</h1>
  <section class="hero-card"><h2>Nível ${lv.n} — ${lv.name}</h2>${progressBar(lv.cur / lv.need)}<p>${d.xp} XP total</p>
  <div class="today"><div><b>${Math.floor(tt / 3600)}h ${Math.floor(tt % 3600 / 60)}min</b><small>de estudo</small></div><div><b>🔥 ${Store.streak()}</b><small>dias seguidos</small></div>
  <div><b>${Object.keys(d.lessons).length}/${allL}</b><small>aulas</small></div><div><b>${Object.values(d.ex).filter(e => e.done).length}/100</b><small>exercícios</small></div><div><b>${Object.values(d.songs).filter(s => s.done).length}</b><small>músicas tocadas</small></div></div></section>
  <h2>Últimas 12 semanas</h2><div class="heat">${days.map(([k, s]) => `<i title="${k}: ${Math.round(s / 60)} min" class="h${s >= 1800 ? 4 : s >= 900 ? 3 : s >= 300 ? 2 : s > 0 ? 1 : 0}"></i>`).join('')}</div>
  <h2>Conquistas</h2><div class="badges">${Store.BADGES.map(([id, ic, name]) => `<div class="badge ${d.badges[id] ? 'on' : ''}"><span>${ic}</span><b>${name}</b><small>${d.badges[id] || 'bloqueada'}</small></div>`).join('')}</div>
  <h2>Músicas tocadas</h2><div class="songlist">${Object.entries(d.songs).filter(([, s]) => s.done).sort((a, b) => (b[1].last || '').localeCompare(a[1].last || '')).map(([n, s]) => { const so = SONG_BY_N.get(+n); return so ? `<a class="song" href="#/musica/${n}"><span class="sn">${n}</span><div><b>${esc(so.t)}</b><small>${esc(so.a)}</small></div><span class="tag ok">${s.best}%</span><span class="tag">${s.plays}x</span></a>` : ''; }).join('') || '<p class="sub">Nenhuma ainda. Bora!</p>'}</div>
  <h2>Configurações e backup</h2><div class="card"><label>Seu nome <input id="nm" value="${esc(d.settings.name)}"></label>
  <p><button class="btn" id="exp">⬇️ Exportar backup</button> <label class="btn ghost">⬆️ Importar backup<input type="file" id="imp" accept=".json" hidden></label> <button class="btn ghost danger" id="rst">Zerar progresso</button></p>
  <p class="sub">O progresso fica salvo neste navegador. Faça backup de vez em quando.</p></div>`;
  $('#nm').onchange = e => { d.settings.name = e.target.value; Store.save(); };
  $('#exp').onclick = () => { const a = document.createElement('a'); a.href = URL.createObjectURL(new Blob([Store.exportJson()], { type: 'application/json' })); a.download = 'tecladista-pro-backup-' + Store.today() + '.json'; a.click(); };
  $('#imp').onchange = async e => { const f = e.target.files[0]; if (!f) return; try { Store.importJson(await f.text()); progresso(); renderTop(); } catch (er) { alert('Arquivo inválido'); } };
  $('#rst').onclick = () => { if (prompt('Digite ZERAR para apagar todo o progresso') === 'ZERAR') { Store.reset(); progresso(); renderTop(); } };
}

route();
Store.checkBadges();
