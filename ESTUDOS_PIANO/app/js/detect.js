// Detecção do que você toca: microfone (o CTK-1200 não tem USB/MIDI) ou MIDI, se houver interface.
// Microfone: espectro -> picos -> cromagrama (12 classes de nota). Compara com as notas do acorde esperado.
const Detect = (() => {
  const st = {
    mode: 'off', // off | mic | midi
    chroma: new Float32Array(12), level: -100, onsetAt: 0, lastLevels: [],
    midiHeld: new Map(), midiOnsetAt: 0,
    sens: 0.35, // limiar relativo (0.15 = sensível, 0.6 = exigente)
    gate: -62,  // dB mínimo para considerar que há som
    listeners: new Set(), err: null
  };
  let analyser, buf, stream, raf, srcNode;

  async function startMic() {
    stop();
    const c = Snd.ac();
    stream = await navigator.mediaDevices.getUserMedia({ audio: { echoCancellation: false, noiseSuppression: false, autoGainControl: false } });
    attach(c.createMediaStreamSource(stream));
  }
  // conecta qualquer fonte de áudio ao analisador (microfone ou arquivo de teste)
  function attach(node) {
    const c = Snd.ac();
    srcNode = node;
    analyser = c.createAnalyser(); analyser.fftSize = 16384; analyser.smoothingTimeConstant = 0.2;
    srcNode.connect(analyser);
    buf = new Float32Array(analyser.frequencyBinCount);
    st.mode = 'mic'; loop();
  }

  function loop() {
    raf = requestAnimationFrame(loop);
    analyser.getFloatFrequencyData(buf);
    const sr = Snd.ac().sampleRate, binHz = sr / analyser.fftSize;
    const lo = Math.floor(60 / binHz), hi = Math.min(buf.length - 2, Math.floor(2200 / binHz));
    // nível global (dB máx da faixa)
    let peakDb = -200;
    for (let i = lo; i < hi; i++) if (buf[i] > peakDb) peakDb = buf[i];
    const now = performance.now();
    st.lastLevels.push([now, peakDb]);
    while (st.lastLevels.length && now - st.lastLevels[0][0] > 180) st.lastLevels.shift();
    const minRecent = Math.min(...st.lastLevels.map(x => x[1]));
    if (peakDb > st.gate && peakDb - minRecent > 7 && now - st.onsetAt > 120) st.onsetAt = now;
    st.level = peakDb;
    const ch = new Float32Array(12);
    if (peakDb > st.gate) {
      const floor = peakDb - 45;
      for (let i = lo + 1; i < hi; i++) {
        const v = buf[i];
        if (v < floor || v < buf[i - 1] || v < buf[i + 1]) continue;
        // interpolação parabólica
        const a = buf[i - 1], b = v, cc = buf[i + 1];
        const p = 0.5 * (a - cc) / (a - 2 * b + cc || 1);
        const f = (i + p) * binHz;
        const midi = 69 + 12 * Math.log2(f / 440);
        const r = Math.round(midi), dev = Math.abs(midi - r);
        if (dev > 0.4) continue;
        const lin = Math.pow(10, (v - peakDb) / 20);
        const w = lin * (1 - dev) * (f < 1000 ? 1 : 1000 / f);
        ch[((r % 12) + 12) % 12] += w;
      }
    }
    const fresh = now - st.onsetAt < 60;
    const k = fresh ? 1 : 0.35;
    for (let i = 0; i < 12; i++) st.chroma[i] = st.chroma[i] * (1 - k) + ch[i] * k;
    st.listeners.forEach(fn => fn());
  }

  async function startMidi() {
    stop();
    if (!navigator.requestMIDIAccess) throw new Error('Este navegador não suporta Web MIDI (use o Chrome).');
    const acc = await navigator.requestMIDIAccess();
    const ins = [...acc.inputs.values()];
    if (!ins.length) throw new Error('Nenhum dispositivo MIDI encontrado. O CTK-1200 não tem saída USB/MIDI — use o microfone.');
    ins.forEach(inp => inp.onmidimessage = e => {
      const [s, n, v] = e.data; const cmd = s & 0xf0;
      if (cmd === 0x90 && v > 0) { st.midiHeld.set(n, performance.now()); st.midiOnsetAt = performance.now(); }
      else if (cmd === 0x80 || (cmd === 0x90 && v === 0)) st.midiHeld.delete(n);
      st.listeners.forEach(fn => fn());
    });
    st.mode = 'midi';
  }

  function stop() {
    cancelAnimationFrame(raf);
    if (stream) stream.getTracks().forEach(t => t.stop());
    stream = null; st.mode = 'off'; st.chroma.fill(0);
  }

  // Classes de nota "ativas"
  function activePcs() {
    if (st.mode === 'midi') return [...new Set([...st.midiHeld.keys()].map(n => n % 12))];
    const mx = Math.max(...st.chroma);
    if (mx < 1e-4 || st.level < st.gate) return [];
    const out = [];
    for (let i = 0; i < 12; i++) if (st.chroma[i] / mx >= st.sens) out.push(i);
    return out;
  }
  function lastOnset() { return st.mode === 'midi' ? st.midiOnsetAt : st.onsetAt; }

  // Confere se as notas exigidas estão soando. Retorna 0..1 (1 = acertou)
  function match(required, allowed) {
    const act = activePcs();
    if (!act.length) return 0;
    const hit = required.filter(p => act.includes(p)).length / required.length;
    if (hit < 1) return hit * 0.9;
    if (st.mode === 'mic') {
      // energia fora do acorde não pode dominar
      const tot = st.chroma.reduce((a, b) => a + b, 0);
      const inside = allowed.reduce((a, p) => a + st.chroma[p], 0);
      if (inside / tot < 0.5) return 0.8;
    }
    return 1;
  }
  // Nota mais forte (para exercícios de nota única)
  function dominantPc() {
    if (st.mode === 'midi') { const a = [...st.midiHeld.entries()].sort((x, y) => y[1] - x[1]); return a.length ? a[0][0] % 12 : null; }
    if (st.level < st.gate) return null;
    let b = 0; for (let i = 1; i < 12; i++) if (st.chroma[i] > st.chroma[b]) b = i;
    return st.chroma[b] > 1e-4 ? b : null;
  }
  function on(fn) { st.listeners.add(fn); return () => st.listeners.delete(fn); }

  return { st, startMic, attach, startMidi, stop, activePcs, match, dominantPc, lastOnset, on };
})();
