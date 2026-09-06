/* ============================================================
   TOCAR JUNTO — modo 6 (a ideia do Moises, aqui dentro)

   O que dá para fazer com um arquivo do seu computador:
   - mudar a VELOCIDADE sem mudar o tom (o navegador faz o time-stretch);
   - TRANSPOR o tom sem mudar a velocidade (pitch shifter próprio, abaixo);
   - abafar o baixo da gravação (para você ser o baixo);
   - tirar o que está no meio da imagem estéreo (costuma ser a voz);
   - repetir um trecho A–B sem parar;
   - pôr um metrônomo por cima.

   Separação de faixas de verdade (baixo/voz/bateria) é matemática pesada e
   roda no servidor — para isso ficam os links do Moises no topo do painel.
   ============================================================ */

const PlayAlong = (() => {

  const $ = id => document.getElementById(id);

  const est = {
    montado: false,
    linksCarregados: false,
    fonte: null,        // MediaElementAudioSourceNode
    nos: null,          // os nós do caminho do áudio
    shifter: null,      // pitch shifter (AudioWorkletNode) ou null
    shifterPronto: false,
    semitons: 0,
    a: null,
    b: null,
    metro: { ligado: false, proximo: 0, passo: 0, timer: null },
  };

  const audio = () => $('pa-audio');
  const tempoTxt = s => {
    if (!isFinite(s)) return '0:00';
    const m = Math.floor(s / 60), r = Math.floor(s % 60);
    return `${m}:${String(r).padStart(2, '0')}`;
  };

  /* ============================================================
     PITCH SHIFTER — overlap-add com duas leituras cruzadas
     Roda dentro de um AudioWorklet (thread de áudio), gerado aqui mesmo.
     ============================================================ */
  const CODIGO_WORKLET = `
class OlaShifter extends AudioWorkletProcessor {
  static get parameterDescriptors() {
    return [{ name: 'ratio', defaultValue: 1, minValue: 0.25, maxValue: 4, automationRate: 'k-rate' }];
  }
  constructor() {
    super();
    this.W = 2048;               // tamanho do grão em amostras (~46 ms)
    this.L = this.W * 4;         // buffer circular
    this.buf = [new Float32Array(this.L), new Float32Array(this.L)];
    this.escrita = 0;
    this.fase = 0;
  }
  ler(canal, pos) {
    const L = this.L, b = this.buf[canal];
    let p = pos % L; if (p < 0) p += L;
    const i = Math.floor(p), f = p - i;
    const a = b[i], c = b[(i + 1) % L];
    return a + (c - a) * f;
  }
  process(entradas, saidas, params) {
    const ent = entradas[0], sai = saidas[0];
    if (!sai || !sai.length) return true;
    const n = sai[0].length;
    const ratio = params.ratio[0];
    const W = this.W;

    for (let i = 0; i < n; i++) {
      for (let ch = 0; ch < sai.length; ch++) {
        const canalEnt = ent && ent[Math.min(ch, ent.length - 1)];
        this.buf[ch][this.escrita] = canalEnt ? canalEnt[i] : 0;
      }
      let f1 = this.fase;
      let f2 = f1 + W / 2; if (f2 >= W) f2 -= W;
      const g1 = 0.5 * (1 - Math.cos(2 * Math.PI * f1 / W));
      const g2 = 0.5 * (1 - Math.cos(2 * Math.PI * f2 / W));
      for (let ch = 0; ch < sai.length; ch++) {
        const base = this.escrita - W;
        sai[ch][i] = g1 * this.ler(ch, base - f1) + g2 * this.ler(ch, base - f2);
      }
      this.fase += (1 - ratio);
      while (this.fase >= W) this.fase -= W;
      while (this.fase < 0) this.fase += W;
      this.escrita = (this.escrita + 1) % this.L;
    }
    return true;
  }
}
registerProcessor('ola-shifter', OlaShifter);
`;

  async function prepararShifter(ctx) {
    if (est.shifterPronto || !ctx.audioWorklet) return;
    try {
      const url = URL.createObjectURL(new Blob([CODIGO_WORKLET], { type: 'application/javascript' }));
      await ctx.audioWorklet.addModule(url);
      URL.revokeObjectURL(url);
      est.shifter = new AudioWorkletNode(ctx, 'ola-shifter', {
        numberOfInputs: 1, numberOfOutputs: 1, outputChannelCount: [2],
      });
      est.shifterPronto = true;
    } catch (e) {
      est.shifterPronto = false;   // segue sem transposição
    }
  }

  /* ============================================================
     CAMINHO DO ÁUDIO
     elemento → corta-grave → [estéreo | só os lados] → pitch → volume → alto-falante
     ============================================================ */
  async function montarCadeia() {
    if (est.montado) return;
    const ctx = Audio2.contexto();
    await prepararShifter(ctx);

    const src = ctx.createMediaElementSource(audio());

    const corteGrave = ctx.createBiquadFilter();     // abafa o baixo da gravação
    corteGrave.type = 'lowshelf';
    corteGrave.frequency.value = 220;
    corteGrave.gain.value = 0;

    const passaAlta = ctx.createBiquadFilter();      // reforça o corte quando vai a 100%
    passaAlta.type = 'highpass';
    passaAlta.frequency.value = 20;
    passaAlta.Q.value = 0.7;

    // caminho 1: o som como veio
    const ganhoEstereo = ctx.createGain();
    ganhoEstereo.gain.value = 1;

    // caminho 2: só a diferença entre os canais (tira o que está no centro)
    const divisor = ctx.createChannelSplitter(2);
    const invertido = ctx.createGain(); invertido.gain.value = -1;
    const ganhoLados = ctx.createGain(); ganhoLados.gain.value = 0;

    const mistura = ctx.createGain();
    const volume = ctx.createGain();
    volume.gain.value = 1;

    src.connect(corteGrave);
    corteGrave.connect(passaAlta);

    passaAlta.connect(ganhoEstereo);
    ganhoEstereo.connect(mistura);

    passaAlta.connect(divisor);
    divisor.connect(ganhoLados, 0);      // canal esquerdo
    divisor.connect(invertido, 1);       // canal direito invertido
    invertido.connect(ganhoLados);
    ganhoLados.connect(mistura);

    mistura.connect(volume);
    volume.connect(ctx.destination);

    est.nos = { ctx, src, corteGrave, passaAlta, ganhoEstereo, ganhoLados, mistura, volume };
    est.montado = true;
    aplicarTransposicao();
    aplicarControles();
  }

  /** Joga nos nós o que já estiver marcado nos sliders. */
  function aplicarControles() {
    if (!est.nos) return;
    const kGrave = +$('pa-kill').value / 100;
    const kCentro = +$('pa-centro').value / 100;
    est.nos.corteGrave.gain.value = -30 * kGrave;
    est.nos.passaAlta.frequency.value = 20 + 130 * kGrave;
    est.nos.ganhoEstereo.gain.value = 1 - kCentro;
    est.nos.ganhoLados.gain.value = kCentro;
    est.nos.volume.gain.value = +$('pa-volume').value / 100;
    audio().volume = 1;                  // daqui pra frente quem manda no volume é o nó
  }

  /** Liga ou desliga o pitch shifter no meio do caminho. */
  function aplicarTransposicao() {
    if (!est.nos) return;
    const { mistura, volume } = est.nos;
    try { mistura.disconnect(); } catch (e) {}
    if (est.shifter) { try { est.shifter.disconnect(); } catch (e) {} }

    if (est.semitons === 0 || !est.shifterPronto) {
      mistura.connect(volume);
    } else {
      est.shifter.parameters.get('ratio').value = Math.pow(2, est.semitons / 12);
      mistura.connect(est.shifter);
      est.shifter.connect(volume);
    }
  }

  /* ============================================================
     LINKS DO tex.md
     ============================================================ */
  function cartaoLink(url, i) {
    const seguro = url.replace(/"/g, '%22');
    const rotulo = url.includes('moises') ? `🎚️ Projeto do Moises ${i + 1}` : `🔗 Link ${i + 1}`;
    return `<a class="pa-link" href="${seguro}" target="_blank" rel="noopener">
              <b>${rotulo}</b><span>${seguro.length > 70 ? seguro.slice(0, 70) + '…' : seguro}</span>
            </a>`;
  }

  function extras() {
    try { return JSON.parse(localStorage.getItem('baixo-aula-links') || '[]'); } catch (e) { return []; }
  }

  async function carregarLinks() {
    if (est.linksCarregados) return;
    est.linksCarregados = true;
    const alvo = $('pa-links');
    let urls = [];
    try {
      const r = await fetch('tex.md', { cache: 'no-store' });
      if (r.ok) {
        const txt = await r.text();
        urls = txt.match(/https?:\/\/[^\s)"'<>]+/g) || [];
      }
    } catch (e) { /* aberto sem servidor: só os links salvos */ }

    urls = urls.concat(extras());
    alvo.innerHTML = urls.length
      ? urls.map(cartaoLink).join('')
      : '<span class="sub">Nenhum link ainda. Cole um link do Moises no campo abaixo — ' +
        'ou escreva ele no arquivo <code>tex.md</code>, um por linha.</span>';
  }

  /* ============================================================
     METRÔNOMO POR CIMA DA MÚSICA
     ============================================================ */
  function agendarMetro() {
    if (!est.metro.ligado || audio().paused) return;
    const bpm = +$('pa-bpm').value;
    const spb = 60 / bpm;
    const agora = Audio2.agora();
    while (est.metro.proximo < agora + 0.15) {
      Audio2.clique(est.metro.proximo, est.metro.passo % 4 === 0, { volume: 0.8 });
      est.metro.proximo += spb;
      est.metro.passo++;
    }
  }

  function ligarMetro(ligado) {
    est.metro.ligado = ligado;
    if (est.metro.timer) { clearInterval(est.metro.timer); est.metro.timer = null; }
    if (!ligado) return;
    est.metro.proximo = Audio2.agora() + 0.1;
    est.metro.passo = 0;
    est.metro.timer = setInterval(agendarMetro, 25);
  }

  /* ============================================================
     TRANSPORTE
     ============================================================ */
  async function tocar() {
    const el = audio();
    if (!el.src) return;
    await montarCadeia();
    Audio2.contexto();
    if (el.paused) {
      await el.play();
      if ($('pa-chk-metronomo').checked) ligarMetro(true);
    } else {
      el.pause();
      ligarMetro(false);
    }
    $('pa-play').textContent = el.paused ? '▶ Tocar' : '⏸ Pausar';
  }

  function carregarArquivo(arquivo) {
    if (!arquivo) return;
    const el = audio();
    if (el.src && el.src.startsWith('blob:')) URL.revokeObjectURL(el.src);
    el.src = URL.createObjectURL(arquivo);
    el.load();
    $('pa-nome').textContent = '🎵 ' + arquivo.name;
    $('pa-player').classList.remove('oculto');
    est.a = est.b = null;
    marcarAB();
  }

  function marcarAB() {
    const el = audio();
    const dur = el.duration || 0;
    const faixa = $('pa-loop-faixa');
    if (est.a === null || est.b === null || !dur) {
      faixa.style.left = '0%'; faixa.style.width = '0%';
      $('pa-ab-txt').textContent = est.a !== null
        ? `A em ${tempoTxt(est.a)} — falta marcar o B`
        : 'Sem repetição marcada';
      return;
    }
    faixa.style.left = (100 * est.a / dur) + '%';
    faixa.style.width = (100 * (est.b - est.a) / dur) + '%';
    $('pa-ab-txt').textContent = `Repetindo ${tempoTxt(est.a)} → ${tempoTxt(est.b)}`;
  }

  function relogio() {
    const el = audio();
    $('pa-tempo').textContent = tempoTxt(el.currentTime);
    $('pa-duracao').textContent = tempoTxt(el.duration);
    if (el.duration) $('pa-seek').value = Math.round(1000 * el.currentTime / el.duration);

    if ($('pa-chk-loop').checked && est.a !== null && est.b !== null && el.currentTime >= est.b) {
      el.currentTime = est.a;
    }
    requestAnimationFrame(relogio);
  }

  /* ============================================================
     Montagem da tela
     ============================================================ */
  function ligar() {
    if (!$('pa-audio')) return;
    const el = audio();

    // arquivo
    $('pa-arquivo').onchange = e => carregarArquivo(e.target.files[0]);
    const drop = $('pa-drop');
    ['dragenter', 'dragover'].forEach(ev => drop.addEventListener(ev, e => {
      e.preventDefault(); drop.classList.add('sobre');
    }));
    ['dragleave', 'drop'].forEach(ev => drop.addEventListener(ev, e => {
      e.preventDefault(); drop.classList.remove('sobre');
    }));
    drop.addEventListener('drop', e => {
      const f = e.dataTransfer.files && e.dataTransfer.files[0];
      if (f) carregarArquivo(f);
    });

    // transporte
    $('pa-play').onclick = tocar;
    $('pa-voltar10').onclick = () => { el.currentTime = Math.max(0, el.currentTime - 10); };
    $('pa-frente10').onclick = () => { el.currentTime = Math.min(el.duration || 0, el.currentTime + 10); };
    $('pa-seek').oninput = e => { if (el.duration) el.currentTime = el.duration * e.target.value / 1000; };
    el.onended = () => { $('pa-play').textContent = '▶ Tocar'; ligarMetro(false); };
    el.onloadedmetadata = () => { marcarAB(); $('pa-duracao').textContent = tempoTxt(el.duration); };

    $('pa-marcar-a').onclick = () => {
      est.a = el.currentTime;
      if (est.b !== null && est.b <= est.a) est.b = null;
      marcarAB();
    };
    $('pa-marcar-b').onclick = () => {
      if (est.a === null) est.a = 0;
      est.b = Math.max(est.a + 0.3, el.currentTime);
      marcarAB();
    };
    $('pa-limpar-ab').onclick = () => { est.a = est.b = null; marcarAB(); };

    // sliders
    $('pa-velocidade').oninput = e => {
      const v = +e.target.value / 100;
      $('pa-vel-valor').textContent = e.target.value;
      el.preservesPitch = true;
      el.mozPreservesPitch = true;
      el.webkitPreservesPitch = true;
      el.playbackRate = v;
    };
    $('pa-tom').oninput = async e => {
      est.semitons = +e.target.value;
      $('pa-tom-valor').textContent = (est.semitons > 0 ? '+' : '') + est.semitons;
      await montarCadeia();
      if (est.semitons !== 0 && !est.shifterPronto) {
        $('pa-tom-valor').textContent = '0 (seu navegador não deixa transpor)';
        est.semitons = 0;
        e.target.value = 0;
      }
      aplicarTransposicao();
    };
    $('pa-kill').oninput = e => {
      const k = +e.target.value / 100;
      $('pa-kill-valor').textContent = e.target.value;
      if (!est.nos) return;
      est.nos.corteGrave.gain.value = -30 * k;               // até -30 dB nos graves
      est.nos.passaAlta.frequency.value = 20 + 130 * k;      // e ainda corta abaixo de 150 Hz
    };
    $('pa-centro').oninput = e => {
      const k = +e.target.value / 100;
      $('pa-centro-valor').textContent = e.target.value;
      if (!est.nos) return;
      est.nos.ganhoEstereo.gain.value = 1 - k;
      est.nos.ganhoLados.gain.value = k;
    };
    $('pa-volume').oninput = e => {
      $('pa-vol-valor').textContent = e.target.value;
      const v = +e.target.value / 100;
      if (est.nos) est.nos.volume.gain.value = v; else el.volume = Math.min(1, v);
    };
    $('pa-bpm').oninput = e => { $('pa-bpm-valor').textContent = e.target.value; };
    $('pa-chk-metronomo').onchange = e => ligarMetro(e.target.checked && !el.paused);

    $('pa-reset-audio').onclick = () => {
      [['pa-velocidade', 100], ['pa-tom', 0], ['pa-kill', 0], ['pa-centro', 0], ['pa-volume', 100]]
        .forEach(([id, v]) => {
          const c = $(id);
          c.value = v;
          c.dispatchEvent(new Event('input'));
        });
    };

    // links
    $('pa-abrir-link').onclick = () => {
      const url = $('pa-novo-link').value.trim();
      if (!/^https?:\/\//.test(url)) { $('pa-novo-link').placeholder = 'O link precisa começar com http…'; return; }
      const lista = extras();
      if (!lista.includes(url)) {
        lista.push(url);
        try { localStorage.setItem('baixo-aula-links', JSON.stringify(lista)); } catch (e) {}
      }
      est.linksCarregados = false;
      carregarLinks();
      $('pa-novo-link').value = '';
      window.open(url, '_blank', 'noopener');
    };

    requestAnimationFrame(relogio);
  }

  document.addEventListener('DOMContentLoaded', ligar);

  return {
    aoEntrar: carregarLinks,
    alternar: tocar,
    parar: () => { const el = audio(); if (el && !el.paused) el.pause(); ligarMetro(false); },
  };
})();

window.PlayAlong = PlayAlong;   // deixa o app.js enxergar
