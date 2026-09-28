// Síntese simples de piano + metrônomo (Web Audio)
const Snd = (() => {
  let ctx = null, master = null;
  function ac() {
    if (!ctx) {
      ctx = new (window.AudioContext || window.webkitAudioContext)();
      master = ctx.createGain(); master.gain.value = 0.5;
      const comp = ctx.createDynamicsCompressor();
      master.connect(comp); comp.connect(ctx.destination);
    }
    if (ctx.state === 'suspended') ctx.resume();
    return ctx;
  }
  function note(midi, when = 0, dur = 1.6, vel = 0.8) {
    const c = ac(); const t = c.currentTime + when;
    const f = M.freq(midi);
    const g = c.createGain();
    const lp = c.createBiquadFilter(); lp.type = 'lowpass'; lp.frequency.value = Math.min(9000, f * 8); lp.Q.value = 0.5;
    g.gain.setValueAtTime(0.0001, t);
    g.gain.exponentialRampToValueAtTime(0.35 * vel, t + 0.005);
    g.gain.exponentialRampToValueAtTime(0.12 * vel, t + 0.25);
    g.gain.exponentialRampToValueAtTime(0.0001, t + dur);
    [[1, 'triangle', 1], [2, 'sine', 0.35], [3, 'sine', 0.12], [4, 'sine', 0.05]].forEach(([h, type, amp]) => {
      const o = c.createOscillator(); o.type = type; o.frequency.value = f * h;
      o.detune.value = (Math.random() - 0.5) * 4;
      const og = c.createGain(); og.gain.value = amp;
      o.connect(og); og.connect(lp); o.start(t); o.stop(t + dur + 0.05);
    });
    lp.connect(g); g.connect(master);
  }
  function chord(midis, when = 0, dur = 1.8, arp = 0) { midis.forEach((m, i) => note(m, when + i * arp, dur, 0.7)); }
  function click(when, accent) {
    const c = ac(); const t = c.currentTime + when;
    const o = c.createOscillator(); const g = c.createGain();
    o.frequency.value = accent ? 1600 : 1000;
    g.gain.setValueAtTime(accent ? 0.5 : 0.3, t); g.gain.exponentialRampToValueAtTime(0.0001, t + 0.05);
    o.connect(g); g.connect(master); o.start(t); o.stop(t + 0.06);
  }

  // Metrônomo com agendamento antecipado
  const metro = { on: false, bpm: 80, beats: 4, beat: 0, next: 0, timer: null, onBeat: null };
  function metroStart(bpm, beats, onBeat) {
    metroStop(); const c = ac();
    Object.assign(metro, { on: true, bpm, beats, beat: 0, next: c.currentTime + 0.1, onBeat });
    metro.timer = setInterval(() => {
      while (metro.next < c.currentTime + 0.12) {
        const b = metro.beat;
        click(metro.next - c.currentTime, b === 0);
        const delay = Math.max(0, (metro.next - c.currentTime) * 1000);
        if (metro.onBeat) setTimeout(() => metro.onBeat && metro.onBeat(b), delay);
        metro.next += 60 / metro.bpm; metro.beat = (b + 1) % metro.beats;
      }
    }, 25);
  }
  function metroStop() { metro.on = false; clearInterval(metro.timer); metro.timer = null; }
  function setBpm(b) { metro.bpm = b; }

  return { ac, note, chord, click, metroStart, metroStop, setBpm, metro };
})();
