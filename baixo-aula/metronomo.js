/* ============================================================
   METRÔNOMO — modo 4, sozinho, sem música nenhuma

   Como o tempo é contado:
   - tudo é agendado no relógio do AudioContext (não no setInterval),
     por isso não desanda nem quando o navegador engasga;
   - um "passo" é uma subdivisão. Passos por compasso = batidas × subdivisão.
   ============================================================ */

const Metronomo = (() => {

  const $ = id => document.getElementById(id);

  const ESPIA = 0.12;    // quanto tempo à frente eu agendo o som (segundos)
  const TIQUE = 25;      // de quanto em quanto tempo eu olho para agendar (ms)

  const cfg = {
    bpm: 90,
    compasso: 4,
    subdiv: 1,
    volume: 1,               // 1 = os 70% que o slider já vem marcando
    tipo: 'click',
    acentos: [2, 1, 1, 1],   // 2 = forte · 1 = normal · 0 = mudo
  };

  const est = {
    rodando: false,
    passo: 0,            // passo dentro do compasso
    proximo: 0,          // instante (AudioContext) do próximo passo
    compassos: 0,
    timer: null,
    raf: null,
    fila: [],            // [{t, batida, sub}] para acender as bolinhas na hora certa
    taps: [],
  };

  /* ---------- nome do andamento (o que está escrito nas partituras) ---------- */
  function andamento(bpm) {
    if (bpm < 40) return 'Largo — bem devagar';
    if (bpm < 60) return 'Larghetto';
    if (bpm < 72) return 'Adagio';
    if (bpm < 80) return 'Andantino';
    if (bpm < 108) return 'Andante — andando';
    if (bpm < 120) return 'Moderato';
    if (bpm < 140) return 'Allegro';
    if (bpm < 168) return 'Vivace';
    if (bpm < 200) return 'Presto — bem rápido';
    return 'Prestissimo';
  }

  /* ---------- desenho ---------- */
  function montarPontos() {
    const alvo = $('mt-pontos');
    if (!alvo) return;
    alvo.innerHTML = '';
    for (let b = 0; b < cfg.compasso; b++) {
      const p = document.createElement('button');
      p.className = 'metro-ponto acento-' + cfg.acentos[b];
      p.id = 'mt-ponto-' + b;
      p.textContent = b + 1;
      p.title = 'Clique para trocar forte / normal / mudo';
      p.onclick = () => {
        cfg.acentos[b] = (cfg.acentos[b] + 2) % 3;   // 1 -> 0 -> 2 -> 1
        p.className = 'metro-ponto acento-' + cfg.acentos[b];
      };
      alvo.appendChild(p);
    }
  }

  function ajustarAcentos() {
    const novo = [];
    for (let b = 0; b < cfg.compasso; b++) {
      novo.push(cfg.acentos[b] === undefined ? (b === 0 ? 2 : 1) : cfg.acentos[b]);
    }
    if (novo.every(a => a !== 2)) novo[0] = 2;   // sempre pelo menos um forte
    cfg.acentos = novo;
    montarPontos();
  }

  function mostrarBpm() {
    $('mt-bpm').textContent = cfg.bpm;
    $('mt-slider').value = cfg.bpm;
    $('mt-andamento').textContent = andamento(cfg.bpm);
  }

  function definirBpm(v) {
    cfg.bpm = Math.max(20, Math.min(280, Math.round(v)));
    mostrarBpm();
  }

  /* ---------- agendamento ---------- */
  function passosPorCompasso() { return cfg.compasso * cfg.subdiv; }
  function segundosPorPasso() { return 60 / cfg.bpm / cfg.subdiv; }

  function agendar() {
    if (!est.rodando) return;
    const agora = Audio2.agora();

    while (est.proximo < agora + ESPIA) {
      const batida = Math.floor(est.passo / cfg.subdiv);
      const eSub = (est.passo % cfg.subdiv) !== 0;
      const acento = cfg.acentos[batida];

      if (acento !== 0) {
        const nivel = eSub ? 'sub' : (acento === 2 ? 'forte' : 'normal');
        Audio2.clique(est.proximo, nivel === 'forte', { nivel, tipo: cfg.tipo, volume: cfg.volume });
      }
      est.fila.push({ t: est.proximo, batida, sub: eSub });

      est.proximo += segundosPorPasso();
      est.passo++;

      if (est.passo >= passosPorCompasso()) {
        est.passo = 0;
        est.compassos++;
        $('mt-compassos').textContent = est.compassos;
        progredir();
      }
    }
  }

  /** Treino progressivo: sobe o BPM sozinho a cada N compassos. */
  function progredir() {
    if (!$('mt-prog').checked) return;
    const aCada = Math.max(1, +$('mt-prog-comp').value || 4);
    if (est.compassos % aCada !== 0) return;
    const passo = Math.max(1, +$('mt-prog-passo').value || 5);
    const teto = Math.max(20, +$('mt-prog-max').value || 140);
    if (cfg.bpm >= teto) return;
    definirBpm(Math.min(teto, cfg.bpm + passo));
  }

  /* ---------- visual em cima do relógio do áudio ---------- */
  function pintar() {
    if (!est.rodando) return;
    const agora = Audio2.agora();
    while (est.fila.length && est.fila[0].t <= agora) {
      const item = est.fila.shift();
      if (!item.sub) acender(item.batida);
    }
    est.raf = requestAnimationFrame(pintar);
  }

  function acender(batida) {
    document.querySelectorAll('.metro-ponto').forEach(p => p.classList.remove('batendo'));
    const p = $('mt-ponto-' + batida);
    if (p) {
      p.classList.add('batendo');
      setTimeout(() => p.classList.remove('batendo'), Math.min(140, 60000 / cfg.bpm * 0.5));
    }
  }

  /* ---------- ligar / desligar ---------- */
  function iniciar() {
    if (est.rodando) return;
    Audio2.contexto();
    est.rodando = true;
    est.passo = 0;
    est.compassos = 0;
    est.fila = [];
    est.proximo = Audio2.agora() + 0.12;
    $('mt-compassos').textContent = '0';
    $('mt-iniciar').disabled = true;
    $('mt-parar').disabled = false;
    est.timer = setInterval(agendar, TIQUE);
    agendar();
    pintar();
  }

  function parar() {
    est.rodando = false;
    if (est.timer) clearInterval(est.timer);
    if (est.raf) cancelAnimationFrame(est.raf);
    est.timer = est.raf = null;
    est.fila = [];
    const ini = $('mt-iniciar'), par = $('mt-parar');
    if (ini) ini.disabled = false;
    if (par) par.disabled = true;
    document.querySelectorAll('.metro-ponto').forEach(p => p.classList.remove('batendo'));
  }

  const alternar = () => (est.rodando ? parar() : iniciar());

  /* ---------- bater o tempo com o dedo ---------- */
  function tap() {
    const agora = performance.now();
    if (est.taps.length && agora - est.taps[est.taps.length - 1] > 2500) est.taps = [];
    est.taps.push(agora);
    if (est.taps.length > 5) est.taps.shift();
    if (est.taps.length < 2) { $('mt-andamento').textContent = 'bata mais algumas vezes…'; return; }
    const intervalos = [];
    for (let i = 1; i < est.taps.length; i++) intervalos.push(est.taps[i] - est.taps[i - 1]);
    const media = intervalos.reduce((a, b) => a + b, 0) / intervalos.length;
    definirBpm(60000 / media);
  }

  /* ---------- ligações da tela ---------- */
  function ligar() {
    if (!$('mt-bpm')) return;

    ajustarAcentos();
    mostrarBpm();

    $('mt-slider').oninput = e => definirBpm(+e.target.value);
    $('mt-menos1').onclick = () => definirBpm(cfg.bpm - 1);
    $('mt-mais1').onclick = () => definirBpm(cfg.bpm + 1);
    $('mt-menos5').onclick = () => definirBpm(cfg.bpm - 5);
    $('mt-mais5').onclick = () => definirBpm(cfg.bpm + 5);
    document.querySelectorAll('[data-mt-bpm]').forEach(b => {
      b.onclick = () => definirBpm(+b.dataset.mtBpm);
    });
    $('mt-tap').onclick = tap;

    $('mt-compasso').onchange = e => {
      cfg.compasso = +e.target.value;
      cfg.acentos = cfg.acentos.slice(0, cfg.compasso);
      ajustarAcentos();
      est.passo = 0;
    };
    $('mt-subdiv').onchange = e => { cfg.subdiv = +e.target.value; est.passo = 0; };
    $('mt-som').onchange = e => { cfg.tipo = e.target.value; };
    $('mt-volume').oninput = e => {
      cfg.volume = +e.target.value / 70;      // 70% = volume de referência
      $('mt-vol-valor').textContent = e.target.value;
    };

    $('mt-iniciar').onclick = iniciar;
    $('mt-parar').onclick = parar;
  }

  document.addEventListener('DOMContentLoaded', ligar);

  return { iniciar, parar, alternar, definirBpm, cfg };
})();

window.Metronomo = Metronomo;   // deixa o app.js enxergar (const nao vai pro window sozinho)
