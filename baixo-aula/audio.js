/* ============================================================
   AUDIO — microfone, detecção de nota (pitch), metrônomo e som de exemplo
   ============================================================ */

const Audio2 = (() => {

  let ctx = null;          // AudioContext (um só para tudo)
  let analisador = null;   // AnalyserNode ligado ao microfone
  let bufferTempo = null;  // Float32Array com as amostras cruas
  let fluxo = null;        // MediaStream do microfone
  let nos = [];            // nós ligados na cadeia (para desconectar na troca)
  let idAtual = '';        // deviceId do microfone em uso
  let timerDeteccao = null;
  let aoDetectar = null;   // callback(resultado|null)

  /* ---------- contexto ---------- */
  function contexto() {
    if (!ctx) ctx = new (window.AudioContext || window.webkitAudioContext)();
    if (ctx.state === 'suspended') ctx.resume();
    return ctx;
  }
  const agora = () => contexto().currentTime;

  /* ---------- microfone ---------- */

  /** Lista as entradas de áudio disponíveis.
   *  Antes de o usuário dar permissão, os nomes vêm em branco (regra do navegador). */
  async function listarMicrofones() {
    if (!navigator.mediaDevices || !navigator.mediaDevices.enumerateDevices) return [];
    const todos = await navigator.mediaDevices.enumerateDevices();
    return todos.filter(d => d.kind === 'audioinput');
  }

  function dispositivoAtual() { return idAtual; }

  /** Solta o microfone atual sem derrubar o resto do áudio. */
  function soltarEntrada() {
    nos.forEach(n => { try { n.disconnect(); } catch (e) {} });
    nos = [];
    if (fluxo) fluxo.getTracks().forEach(t => t.stop());
    fluxo = null;
    analisador = null;
  }

  /** Liga (ou troca) o microfone. Passe o deviceId para escolher a entrada. */
  async function ligarMicrofone(idDispositivo) {
    const c = contexto();
    soltarEntrada();

    const restricoes = {
      echoCancellation: false,   // esses três atrapalham a leitura de afinação
      noiseSuppression: false,
      autoGainControl: false
    };
    if (idDispositivo) restricoes.deviceId = { exact: idDispositivo };

    fluxo = await navigator.mediaDevices.getUserMedia({ audio: restricoes });

    const faixa = fluxo.getAudioTracks()[0];
    idAtual = idDispositivo || (faixa && faixa.getSettings ? (faixa.getSettings().deviceId || '') : '');

    const fonte = c.createMediaStreamSource(fluxo);

    // Corta o ronco abaixo do Mi grave e tudo acima da região do baixo:
    const passaAlta = c.createBiquadFilter();
    passaAlta.type = 'highpass'; passaAlta.frequency.value = 28; passaAlta.Q.value = 0.7;
    const passaBaixa = c.createBiquadFilter();
    passaBaixa.type = 'lowpass'; passaBaixa.frequency.value = 1200; passaBaixa.Q.value = 0.7;

    analisador = c.createAnalyser();
    analisador.fftSize = 8192;          // ~170 ms de áudio: suficiente pro Mi grave (41 Hz)
    bufferTempo = new Float32Array(analisador.fftSize);

    fonte.connect(passaAlta);
    passaAlta.connect(passaBaixa);
    passaBaixa.connect(analisador);     // não liga na saída: evita microfonia
    nos = [fonte, passaAlta, passaBaixa, analisador];

    if (timerDeteccao) clearInterval(timerDeteccao);
    timerDeteccao = setInterval(loopDeteccao, 55);
    return faixa ? (faixa.label || '') : '';
  }

  function microfoneLigado() { return !!analisador; }

  function desligarMicrofone() {
    if (timerDeteccao) { clearInterval(timerDeteccao); timerDeteccao = null; }
    soltarEntrada();
    idAtual = '';
  }

  /** Avisa quando um microfone é plugado/desplugado. */
  function aoTrocarDispositivos(cb) {
    if (navigator.mediaDevices && 'ondevicechange' in navigator.mediaDevices) {
      navigator.mediaDevices.addEventListener('devicechange', cb);
    }
  }

  function aoOuvir(cb) { aoDetectar = cb; }

  function loopDeteccao() {
    if (!analisador) return;
    analisador.getFloatTimeDomainData(bufferTempo);
    const r = detectarNota(bufferTempo, contexto().sampleRate);
    if (aoDetectar) aoDetectar(r);
  }

  /* ------------------------------------------------------------
     DETECÇÃO DE ALTURA (pitch)
     Método NSDF / McLeod: robusto para instrumento grave, onde a
     fundamental é fraca e a FFT erra a oitava com facilidade.
     ------------------------------------------------------------ */
  function detectarNota(bruto, taxa) {
    // Decima por 2 (média de pares) -> metade do custo, sobra precisão de sobra
    const N = bruto.length >> 1;
    const x = new Float32Array(N);
    for (let i = 0; i < N; i++) x[i] = (bruto[2 * i] + bruto[2 * i + 1]) * 0.5;
    const taxa2 = taxa / 2;

    // volume
    let soma = 0;
    for (let i = 0; i < N; i++) soma += x[i] * x[i];
    const rms = Math.sqrt(soma / N);
    if (rms < 0.005) return null;              // silêncio

    // tira a média (DC)
    let media = 0;
    for (let i = 0; i < N; i++) media += x[i];
    media /= N;
    for (let i = 0; i < N; i++) x[i] -= media;

    const lagMin = Math.max(2, Math.floor(taxa2 / 420));   // até ~420 Hz (casa 12 da corda Sol)
    const lagMax = Math.min(N - 2, Math.floor(taxa2 / 33)); // até ~33 Hz (abaixo do Mi grave)

    const nsdf = new Float32Array(lagMax + 2);
    for (let lag = lagMin; lag <= lagMax; lag++) {
      let ac = 0, m = 0;
      // janela limitada: segura o custo da correlação sem perder precisão
      const lim = Math.min(N - lag, 2800);
      for (let i = 0; i < lim; i++) {
        const a = x[i], b = x[i + lag];
        ac += a * b;
        m += a * a + b * b;
      }
      nsdf[lag] = m > 0 ? (2 * ac) / m : 0;
    }

    // Picos: pega o PRIMEIRO pico forte (evita errar a oitava pra baixo)
    let maior = 0;
    const picos = [];
    for (let lag = lagMin + 1; lag < lagMax; lag++) {
      if (nsdf[lag] > nsdf[lag - 1] && nsdf[lag] >= nsdf[lag + 1] && nsdf[lag] > 0) {
        picos.push(lag);
        if (nsdf[lag] > maior) maior = nsdf[lag];
      }
    }
    if (!picos.length || maior < 0.5) return null;   // som confuso / ruído

    const limite = maior * 0.88;
    let escolhido = picos[picos.length - 1];
    for (const p of picos) { if (nsdf[p] >= limite) { escolhido = p; break; } }

    // interpolação parabólica para afinar o período
    const y0 = nsdf[escolhido - 1], y1 = nsdf[escolhido], y2 = nsdf[escolhido + 1];
    const den = (y0 - 2 * y1 + y2);
    const ajuste = den !== 0 ? 0.5 * (y0 - y2) / den : 0;
    const periodo = escolhido + ajuste;
    const freq = taxa2 / periodo;
    if (!isFinite(freq) || freq < 30 || freq > 450) return null;

    const midiExato = 69 + 12 * Math.log2(freq / 440);
    const midi = Math.round(midiExato);
    return {
      freq,
      midiExato,
      midi,
      cents: Math.round((midiExato - midi) * 100),
      clareza: nsdf[escolhido],
      rms
    };
  }

  /* ---------- metrônomo ----------
     clique(quando, forte)                  -> uso antigo (2 níveis)
     clique(quando, false, {nivel:'sub'})   -> subdivisão (mais fraca e mais aguda)
     opções: { nivel: 'forte'|'normal'|'sub', tipo: 'click'|'madeira'|'bip', volume: 0..1.5 }
  */
  const TIMBRES = {
    click:   { onda: 'square',   forte: 1600, normal: 1000, sub: 2200, dur: 0.05 },
    madeira: { onda: 'triangle', forte: 1200, normal:  820, sub: 1600, dur: 0.04, ruido: true },
    bip:     { onda: 'sine',     forte: 1760, normal: 1174, sub: 2349, dur: 0.07 },
  };
  const GANHO = { forte: 0.24, normal: 0.13, sub: 0.06 };

  function clique(quando, forte, opts) {
    const c = contexto();
    opts = opts || {};
    const nivel = opts.nivel || (forte ? 'forte' : 'normal');
    const timbre = TIMBRES[opts.tipo] || TIMBRES.click;
    const vol = Math.max(0.0002, GANHO[nivel] * (opts.volume === undefined ? 1 : opts.volume));

    const o = c.createOscillator();
    const g = c.createGain();
    o.type = timbre.onda;
    o.frequency.value = timbre[nivel];         // agudo: não confunde a detecção do baixo
    g.gain.setValueAtTime(0.0001, quando);
    g.gain.exponentialRampToValueAtTime(vol, quando + 0.002);
    g.gain.exponentialRampToValueAtTime(0.0001, quando + timbre.dur);
    o.connect(g); g.connect(c.destination);
    o.start(quando); o.stop(quando + timbre.dur + 0.02);

    if (timbre.ruido) {          // estalo curto de madeira por cima do tom
      const n = c.createBufferSource();
      const buf = c.createBuffer(1, Math.ceil(c.sampleRate * 0.02), c.sampleRate);
      const dados = buf.getChannelData(0);
      for (let i = 0; i < dados.length; i++) {
        dados[i] = (Math.random() * 2 - 1) * (1 - i / dados.length);
      }
      n.buffer = buf;
      const pb = c.createBiquadFilter();
      pb.type = 'bandpass'; pb.frequency.value = timbre[nivel]; pb.Q.value = 1.2;
      const gn = c.createGain();
      gn.gain.value = vol * 0.9;
      n.connect(pb); pb.connect(gn); gn.connect(c.destination);
      n.start(quando);
    }
  }

  /* ---------- som de baixo sintetizado (para "ouvir exemplo") ---------- */
  function tocarNota(midi, quando, dur, volume) {
    const c = contexto();
    const f = 440 * Math.pow(2, (midi - 69) / 12);
    const o = c.createOscillator();
    o.type = 'sawtooth';
    o.frequency.value = f;
    const filtro = c.createBiquadFilter();
    filtro.type = 'lowpass';
    filtro.Q.value = 4;
    filtro.frequency.setValueAtTime(Math.min(f * 9, 3000), quando);
    filtro.frequency.exponentialRampToValueAtTime(Math.max(f * 2, 90), quando + Math.max(dur, .2));
    const g = c.createGain();
    const vol = volume === undefined ? 0.32 : volume;
    g.gain.setValueAtTime(0.0001, quando);
    g.gain.exponentialRampToValueAtTime(vol, quando + 0.012);
    g.gain.exponentialRampToValueAtTime(0.0001, quando + Math.max(dur, 0.18));
    o.connect(filtro); filtro.connect(g); g.connect(c.destination);
    o.start(quando); o.stop(quando + Math.max(dur, 0.2) + 0.05);
  }

  /* ---------- efeitos de acerto/erro ---------- */
  function bip(freq, dur, vol) {
    const c = contexto();
    const o = c.createOscillator(); const g = c.createGain();
    o.type = 'sine'; o.frequency.value = freq;
    g.gain.setValueAtTime(0.0001, c.currentTime);
    g.gain.exponentialRampToValueAtTime(vol || 0.15, c.currentTime + 0.01);
    g.gain.exponentialRampToValueAtTime(0.0001, c.currentTime + dur);
    o.connect(g); g.connect(c.destination);
    o.start(); o.stop(c.currentTime + dur + 0.05);
  }
  const somAcerto = () => { bip(880, 0.12); setTimeout(() => bip(1320, 0.14), 90); };

  return {
    contexto, agora, ligarMicrofone, desligarMicrofone, microfoneLigado,
    listarMicrofones, dispositivoAtual, aoTrocarDispositivos,
    aoOuvir, clique, tocarNota, somAcerto, bip, detectarNota
  };
})();
