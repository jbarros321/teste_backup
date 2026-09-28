// Progresso salvo no navegador (localStorage) + XP, níveis, conquistas e tempo de estudo.
const Store = (() => {
  const KEY = 'tecladista-pro-v1';
  const blank = () => ({ xp: 0, lessons: {}, ex: {}, songs: {}, fav: [], log: {}, drills: {}, badges: {}, custom: [],
    settings: { sens: 0.35, beats: 4, simplify: 0, lookahead: 8, name: '' } });
  let d = blank();
  try { const raw = localStorage.getItem(KEY); if (raw) d = Object.assign(blank(), JSON.parse(raw)); d.settings = Object.assign(blank().settings, d.settings); } catch (e) {}
  function save() { try { localStorage.setItem(KEY, JSON.stringify(d)); } catch (e) {} }

  const LV = [0, 100, 250, 500, 850, 1300, 1900, 2700, 3700, 5000, 6600, 8600, 11000, 14000, 18000];
  const LVN = ['Primeiro Toque', 'Aprendiz', 'Aprendiz II', 'Músico da Igreja', 'Músico da Igreja II', 'Tecladista', 'Tecladista II', 'Tecladista de Banda', 'Ministro de Louvor', 'Ministro de Louvor II', 'Arranjador', 'Virtuoso', 'Virtuoso II', 'Mestre', 'Lenda do Teclado'];
  function level() {
    let i = 0; while (i + 1 < LV.length && d.xp >= LV[i + 1]) i++;
    const next = LV[i + 1] ?? null;
    return { n: i + 1, name: LVN[i], cur: d.xp - LV[i], need: next ? next - LV[i] : 1, max: !next };
  }
  let toast = () => {};
  function onToast(fn) { toast = fn; }
  function addXp(n, why) { if (!n) return; d.xp += n; save(); toast(`+${n} XP${why ? ' · ' + why : ''}`); checkBadges(); }

  const today = () => { const t = new Date(); return t.getFullYear() + '-' + String(t.getMonth() + 1).padStart(2, '0') + '-' + String(t.getDate()).padStart(2, '0'); };
  function addTime(sec) { const k = today(); d.log[k] = (d.log[k] || 0) + sec; save(); }
  function streak() {
    let s = 0; const t = new Date();
    for (let i = 0; i < 400; i++) {
      const dt = new Date(t); dt.setDate(t.getDate() - i);
      const k = dt.getFullYear() + '-' + String(dt.getMonth() + 1).padStart(2, '0') + '-' + String(dt.getDate()).padStart(2, '0');
      if ((d.log[k] || 0) >= 300) s++; else if (i > 0) break;
    }
    return s;
  }
  const totalTime = () => Object.values(d.log).reduce((a, b) => a + b, 0);

  const BADGES = [
    ['first-lesson', '🎓', 'Primeira aula', () => Object.keys(d.lessons).length >= 1],
    ['ten-lessons', '📚', '10 aulas', () => Object.keys(d.lessons).length >= 10],
    ['all-lessons', '🏛️', 'Trilha completa', () => Object.keys(d.lessons).length >= LESSONS.reduce((a, m) => a + m.lessons.length, 0)],
    ['first-song', '🎵', 'Primeira música', () => Object.values(d.songs).some(s => s.done)],
    ['ten-songs', '🎼', '10 músicas', () => Object.values(d.songs).filter(s => s.done).length >= 10],
    ['fifty-songs', '💿', '50 músicas', () => Object.values(d.songs).filter(s => s.done).length >= 50],
    ['perfect', '💯', 'Música 100% no tempo', () => Object.values(d.songs).some(s => s.best >= 100)],
    ['ex10', '💪', '10 exercícios', () => Object.values(d.ex).filter(e => e.done).length >= 10],
    ['ex50', '🏋️', '50 exercícios', () => Object.values(d.ex).filter(e => e.done).length >= 50],
    ['ex100', '🏆', 'Os 100 exercícios', () => Object.values(d.ex).filter(e => e.done).length >= 100],
    ['streak3', '🔥', '3 dias seguidos', () => streak() >= 3],
    ['streak7', '⚡', '7 dias seguidos', () => streak() >= 7],
    ['streak30', '🌋', '30 dias seguidos', () => streak() >= 30],
    ['h1', '⏱️', '1 hora de estudo', () => totalTime() >= 3600],
    ['h10', '⌛', '10 horas de estudo', () => totalTime() >= 36000],
    ['h50', '🕰️', '50 horas de estudo', () => totalTime() >= 180000],
    ['ear50', '👂', '50 acertos de ouvido', () => (d.drills.earOk || 0) >= 50],
    ['chords100', '🤘', '100 acordes no treino', () => (d.drills.chordOk || 0) >= 100],
    ['lvl5', '⭐', 'Nível Tecladista', () => level().n >= 6],
    ['lvl9', '👑', 'Ministro de Louvor', () => level().n >= 9]
  ];
  function checkBadges() {
    for (const [id, ic, name, fn] of BADGES) if (!d.badges[id]) { try { if (fn()) { d.badges[id] = today(); save(); toast(`${ic} Conquista: ${name}!`, true); } } catch (e) {} }
  }
  function exportJson() { return JSON.stringify(d, null, 1); }
  function importJson(txt) { d = Object.assign(blank(), JSON.parse(txt)); save(); }
  function reset() { d = blank(); save(); }
  return { get d() { return d; }, save, addXp, level, onToast, addTime, streak, totalTime, today, BADGES, checkBadges, exportJson, importJson, reset };
})();
