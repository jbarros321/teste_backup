/* ============================================================
   APP — junta tudo: braço, tablatura, modos e correção pelo ouvido
   ============================================================ */

/* ---------- constantes do instrumento ---------- */
const CORDAS_ORDEM = ['G', 'D', 'A', 'E'];   // como aparecem na tela (aguda em cima)
const CORDA = {
  E: { pt: 'Mi',  en: 'E', num: '4ª', midi: 28, obs: 'a mais grossa' },
  A: { pt: 'Lá',  en: 'A', num: '3ª', midi: 33, obs: '' },
  D: { pt: 'Ré',  en: 'D', num: '2ª', midi: 38, obs: '' },
  G: { pt: 'Sol', en: 'G', num: '1ª', midi: 43, obs: 'a mais fina' },
};
const DEDOS = { 0: '—', 1: '1', 2: '2', 3: '3', 4: '4' };
const DEDOS_NOME = { 0: 'corda solta, não aperte', 1: 'indicador', 2: 'médio', 3: 'anelar', 4: 'mindinho' };
const NOME_PT = ['Dó', 'Dó#', 'Ré', 'Ré#', 'Mi', 'Fá', 'Fá#', 'Sol', 'Sol#', 'Lá', 'Lá#', 'Si'];
const NOME_EN = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B'];
const MAX_CASA = 12;
const MARCADORES = [3, 5, 7, 9, 12];

const midiDaNota = n => CORDA[n.c].midi + n.f;
const nomePt = m => NOME_PT[((m % 12) + 12) % 12];
const nomeEn = m => NOME_EN[((m % 12) + 12) % 12] + (Math.floor(m / 12) - 1);

/* ---------- estado ---------- */
const est = {
  musica: null,
  modo: 'aprender',
  bpm: 80,
  indice: 0,
  rodando: false,
  travado: false,
  acertosSeguidos: 0,
  resultados: [],     // 'ok' | 'erro' | null, por nota
  inicio: 0,          // instante (AudioContext) do tempo 0
  raf: null,
  proximoClique: -4,
};

const $ = id => document.getElementById(id);

/* ============================================================
   MONTAGEM DA TELA
   ============================================================ */
function montarBraco() {
  const cols = `72px 62px repeat(${MAX_CASA}, minmax(46px,1fr))`;
  const braco = $('braco');
  braco.innerHTML = '';

  for (const c of CORDAS_ORDEM) {
    const linha = document.createElement('div');
    linha.className = 'braco-linha';
    linha.dataset.corda = c;
    linha.style.gridTemplateColumns = cols;

    const rot = document.createElement('div');
    rot.className = 'braco-corda';
    rot.textContent = `${CORDA[c].num} ${CORDA[c].pt} (${CORDA[c].en})`;
    linha.appendChild(rot);

    for (let casa = 0; casa <= MAX_CASA; casa++) {
      const cel = document.createElement('div');
      cel.className = 'casa' + (casa === 0 ? ' solta' : '');
      cel.id = `fb-${c}-${casa}`;
      cel.innerHTML = '<div class="fio"></div>' +
        ((c === 'D' || c === 'A') && MARCADORES.includes(casa) ? '<div class="marcador"></div>' : '') +
        '<div class="bolinha"></div>';
      linha.appendChild(cel);
    }
    braco.appendChild(linha);
  }

  const nums = document.createElement('div');
  nums.className = 'numeros-casas';
  nums.style.gridTemplateColumns = cols;
  nums.innerHTML = '<div></div><div>solta</div>' +
    Array.from({ length: MAX_CASA }, (_, i) => `<div>${i + 1}</div>`).join('');
  braco.appendChild(nums);
}

function limparBraco() {
  document.querySelectorAll('.casa').forEach(c => {
    c.classList.remove('on', 'alvo', 'prox', 'certo', 'errado');
    c.querySelector('.bolinha').textContent = '';
  });
}

function marcarBraco(nota, tipo) {
  if (!nota || nota.f > MAX_CASA) return;
  const cel = $(`fb-${nota.c}-${nota.f}`);
  if (!cel) return;
  cel.classList.add('on', tipo);
  cel.querySelector('.bolinha').textContent = nota.f === 0 ? '0' : DEDOS[nota.d];
}

function montarListaMusicas() {
  const sel = $('sel-musica');
  sel.innerHTML = MUSICAS.map(m =>
    `<option value="${m.id}">${'★'.repeat(m.nivel)}${'☆'.repeat(3 - m.nivel)}  ${m.nome}</option>`
  ).join('');
}

function montarTab() {
  const tab = $('tab');
  tab.innerHTML = '';
  if (!est.musica) return;

  const grade = document.createElement('div');
  grade.className = 'tab-grade';

  const rot = document.createElement('div');
  rot.className = 'tab-rotulos';
  rot.innerHTML = CORDAS_ORDEM.map(c => `<div>${CORDA[c].en} |</div>`).join('');
  grade.appendChild(rot);

  const comp = est.musica.compasso || 4;
  est.musica.notas.forEach((n, i) => {
    const col = document.createElement('div');
    col.className = 'tab-col';
    col.id = 'tabcol-' + i;
    if (i > 0 && Math.floor(n.t / comp) !== Math.floor(est.musica.notas[i - 1].t / comp)) {
      col.classList.add('barra');
    }
    col.innerHTML = CORDAS_ORDEM.map(c =>
      c === n.c ? `<div class="nota">${n.f}</div>` : '<div>—</div>'
    ).join('');
    col.onclick = () => { if (!est.rodando) irPara(i); };
    grade.appendChild(col);
  });

  tab.appendChild(grade);
}

/* ============================================================
   SELEÇÃO DE MÚSICA
   ============================================================ */
function selecionarMusica(id) {
  parar();
  est.musica = MUSICAS.find(m => m.id === id) || MUSICAS[0];
  est.indice = 0;
  est.resultados = new Array(est.musica.notas.length).fill(null);
  est.bpm = est.musica.bpm;

  $('musica-nome').textContent = est.musica.nome;
  $('musica-artista').textContent = est.musica.artista;
  $('musica-dica').textContent = '💡 ' + est.musica.dica;
  $('bpm').value = est.bpm;
  $('bpm-valor').textContent = est.bpm;
  $('na-total').textContent = est.musica.notas.length;
  $('resultado').classList.add('oculto');

  montarTab();
  irPara(0);
  feedback('Clique em <b>Começar</b> — eu te mostro nota por nota e escuto se você acertou.', 'espera');
}

/* ============================================================
   MOSTRAR A NOTA ATUAL
   ============================================================ */
function notaAtual() { return est.musica ? est.musica.notas[est.indice] : null; }

function irPara(i) {
  if (!est.musica) return;
  est.indice = Math.max(0, Math.min(i, est.musica.notas.length - 1));
  est.acertosSeguidos = 0;
  est.travado = false;
  desenharNota();
}

function desenharNota() {
  const n = notaAtual();
  limparBraco();
  if (!n) return;

  // já tocadas: pinta o resultado no tab
  est.musica.notas.forEach((_, i) => {
    const col = $('tabcol-' + i);
    if (!col) return;
    col.classList.remove('atual', 'ok', 'falhou');
    if (est.resultados[i] === 'ok') col.classList.add('ok');
    if (est.resultados[i] === 'erro') col.classList.add('falhou');
  });

  const prox = est.musica.notas[est.indice + 1];
  if (prox) marcarBraco(prox, 'prox');
  marcarBraco(n, 'alvo');

  const col = $('tabcol-' + est.indice);
  if (col) {
    col.classList.add('atual');
    col.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
  }

  const info = CORDA[n.c];
  const midi = midiDaNota(n);
  $('na-indice').textContent = est.indice + 1;
  $('na-corda').textContent = `${info.pt} (${info.en})`;
  $('na-corda-sub').textContent = `${info.num} corda${info.obs ? ' — ' + info.obs : ''}`;
  $('na-casa').textContent = n.f === 0 ? 'solta' : n.f;
  $('na-casa-sub').textContent = n.f === 0 ? 'não aperte nada' : 'aperte logo atrás do traste';
  $('na-dedo').textContent = n.f === 0 ? '—' : DEDOS[n.d];
  $('na-dedo-sub').textContent = DEDOS_NOME[n.f === 0 ? 0 : n.d];
  $('na-mao').textContent = est.indice % 2 === 0 ? 'indicador' : 'médio';
  $('na-nota').textContent = nomePt(midi);
  $('na-nota-sub').textContent = `${nomeEn(midi)} · ${(440 * Math.pow(2, (midi - 69) / 12)).toFixed(1)} Hz`;
}

function feedback(html, classe) {
  const el = $('feedback');
  el.className = 'feedback' + (classe ? ' ' + classe : '');
  el.innerHTML = html;
}

/* ============================================================
   MODO 1 — APRENDER (sem pressa: espera você acertar)
   ============================================================ */
function comecarAprender() {
  if (!Audio2.microfoneLigado()) {
    feedback('Ligue o microfone ali em cima 👆 para eu conseguir te ouvir.', 'errado');
    return;
  }
  est.rodando = true;
  est.travado = false;
  est.acertosSeguidos = 0;
  botoes(true);
  desenharNota();
  feedback('Toque a nota que está no braço. Estou ouvindo… 👂', 'espera');
}

function avaliarAprender(det) {
  if (!est.rodando || est.travado || est.modo !== 'aprender') return;
  const n = notaAtual();
  if (!n || !det || det.clareza < 0.6) return;

  const alvo = midiDaNota(n);
  const d = det.midiExato - alvo;
  const certo = Math.abs(d) < 0.4 || Math.abs(d - 12) < 0.4 || Math.abs(d + 12) < 0.4;

  if (certo) {
    est.acertosSeguidos++;
    if (est.acertosSeguidos >= 2) acertou();
  } else {
    est.acertosSeguidos = 0;
    if (Math.abs(d) <= 14) {
      const direcao = d < 0 ? 'grave demais — tente uma casa mais alta (para a direita)'
                            : 'aguda demais — tente uma casa mais baixa (para a esquerda)';
      const dist = Math.round(Math.abs(d));
      feedback(`Saiu <b>${nomePt(det.midi)}</b>, mas eu esperava <b>${nomePt(alvo)}</b>. ` +
               `Está ${direcao}${dist ? ` (${dist} casa${dist > 1 ? 's' : ''} de diferença)` : ''}.`, 'errado');
      const cel = $(`fb-${n.c}-${n.f}`);
      if (cel) {
        cel.classList.add('errado');
        setTimeout(() => cel.classList.remove('errado'), 600);
      }
    }
  }
}

function acertou() {
  est.travado = true;
  est.resultados[est.indice] = est.resultados[est.indice] === 'erro' ? 'erro' : 'ok';
  limparBraco();
  marcarBraco(notaAtual(), 'certo');
  const col = $('tabcol-' + est.indice);
  if (col) { col.classList.remove('atual'); col.classList.add('ok'); }
  feedback('✅ Isso! Nota certa.', 'certo');
  Audio2.somAcerto();

  setTimeout(() => {
    if (!est.rodando) return;
    if (est.indice >= est.musica.notas.length - 1) {
      est.rodando = false;
      botoes(false);
      feedback('🎉 Você tocou a música inteira! Agora tente o modo <b>Tocar no tempo</b>.', 'certo');
      return;
    }
    irPara(est.indice + 1);
    feedback('Próxima nota. Estou ouvindo… 👂', 'espera');
  }, 450);
}

/* ============================================================
   MODO 2 — TOCAR NO TEMPO (metrônomo + nota)
   ============================================================ */
function comecarTocar() {
  if (!Audio2.microfoneLigado()) {
    feedback('Ligue o microfone ali em cima 👆 para eu conseguir avaliar.', 'errado');
    return;
  }
  const spb = 60 / est.bpm;
  est.resultados = new Array(est.musica.notas.length).fill(null);
  est.rodando = true;
  est.indice = 0;
  est.proximoClique = -4;
  est.inicio = Audio2.agora() + 0.35 + 4 * spb;   // 4 batidas de contagem
  botoes(true);
  $('resultado').classList.add('oculto');
  montarTab();
  loopTocar();
}

function loopTocar() {
  if (!est.rodando) return;
  const spb = 60 / est.bpm;
  const t = Audio2.agora();
  const beat = (t - est.inicio) / spb;
  const notas = est.musica.notas;
  const ultima = notas[notas.length - 1];
  const fim = ultima.t + ultima.dur;

  // agenda o metrônomo com um pouco de antecedência
  if ($('chk-metronomo').checked) {
    const comp = est.musica.compasso || 4;
    while (est.proximoClique < fim + 1 &&
           est.inicio + est.proximoClique * spb < t + 0.35) {
      const b = est.proximoClique;
      const forte = b < 0 ? b === -4 : (Math.round(b) % comp === 0 && Math.abs(b - Math.round(b)) < 0.01);
      Audio2.clique(est.inicio + b * spb, forte);
      est.proximoClique++;
    }
  } else {
    est.proximoClique = Math.max(est.proximoClique, Math.ceil(beat));
  }

  if (beat < 0) {
    const faltam = Math.ceil(-beat);
    feedback(`Prepara… <b>${faltam}</b>`, 'espera');
  } else if (beat > fim + 0.5) {
    return terminarTocar();
  } else {
    let idx = 0;
    for (let i = 0; i < notas.length; i++) if (notas[i].t <= beat + 0.05) idx = i;
    if (idx !== est.indice) { est.indice = idx; desenharNota(); }
    const feitas = est.resultados.filter(r => r === 'ok').length;
    feedback(`Tocando… acertos até agora: <b>${feitas}</b> de ${notas.length}`, '');
  }

  est.raf = requestAnimationFrame(loopTocar);
}

function avaliarTocar(det) {
  if (!est.rodando || est.modo !== 'tocar' || !det || det.clareza < 0.55) return;
  const spb = 60 / est.bpm;
  const t = Audio2.agora();
  const notas = est.musica.notas;

  for (let i = 0; i < notas.length; i++) {
    if (est.resultados[i] === 'ok') continue;
    const ini = est.inicio + notas[i].t * spb - 0.14;
    const fim = est.inicio + (notas[i].t + notas[i].dur) * spb - 0.04;
    if (t >= ini && t <= Math.max(fim, ini + 0.2)) {
      const alvo = midiDaNota(notas[i]);
      const d = det.midiExato - alvo;
      if (Math.abs(d) < 0.45 || Math.abs(d - 12) < 0.45 || Math.abs(d + 12) < 0.45) {
        est.resultados[i] = 'ok';
        const col = $('tabcol-' + i);
        if (col) col.classList.add('ok');
      }
      break;
    }
  }
}

function terminarTocar() {
  est.rodando = false;
  botoes(false);
  const notas = est.musica.notas;
  est.resultados = est.resultados.map(r => r === 'ok' ? 'ok' : 'erro');
  const acertos = est.resultados.filter(r => r === 'ok').length;
  const pct = Math.round(100 * acertos / notas.length);

  const erradas = [];
  est.resultados.forEach((r, i) => { if (r !== 'ok') erradas.push(i); });

  let msg;
  if (pct >= 90) msg = 'Mandou muito bem! Já pode subir o BPM.';
  else if (pct >= 65) msg = 'Bom! Repita algumas vezes nesse andamento antes de acelerar.';
  else msg = 'Diminua a velocidade (tente 50%) e volte pro modo Aprender nos trechos difíceis.';

  $('resultado').classList.remove('oculto');
  $('resultado-corpo').innerHTML =
    `<div class="placar" style="color:${pct >= 90 ? 'var(--verde)' : pct >= 65 ? 'var(--laranja)' : 'var(--vermelho)'}">${pct}%</div>` +
    `<p>${acertos} de ${notas.length} notas no tempo certo, a ${est.bpm} BPM. ${msg}</p>` +
    (erradas.length ? `<table><tr><th>Nota nº</th><th>Corda</th><th>Casa</th><th>Dedo</th><th>Som</th></tr>` +
      erradas.slice(0, 20).map(i => {
        const n = notas[i];
        return `<tr><td>${i + 1}</td><td>${CORDA[n.c].pt} (${n.c})</td><td>${n.f === 0 ? 'solta' : n.f}</td>` +
               `<td>${n.f === 0 ? '—' : DEDOS[n.d]}</td><td>${nomePt(midiDaNota(n))}</td></tr>`;
      }).join('') +
      `</table>` + (erradas.length > 20 ? `<p class="sub">…e mais ${erradas.length - 20}.</p>` : '')
      : '<p>Não escapou nenhuma. 👏</p>');

  desenharNota();
  feedback(`Fim! Você acertou <b>${pct}%</b>.`, pct >= 65 ? 'certo' : 'errado');
}

/* ============================================================
   OUVIR EXEMPLO
   ============================================================ */
function ouvirExemplo() {
  if (!est.musica) return;
  const spb = 60 / est.bpm;
  const t0 = Audio2.agora() + 0.25;
  const comp = est.musica.compasso || 4;
  const ultima = est.musica.notas[est.musica.notas.length - 1];
  const fim = ultima.t + ultima.dur;
  if ($('chk-metronomo').checked) {
    for (let b = 0; b < fim; b++) Audio2.clique(t0 + b * spb, b % comp === 0);
  }
  est.musica.notas.forEach(n => {
    Audio2.tocarNota(midiDaNota(n), t0 + n.t * spb, n.dur * spb * 0.95);
  });
  feedback('🔊 Ouça e depois tente reproduzir.', 'espera');
}

/* ============================================================
   PARAR
   ============================================================ */
function parar() {
  est.rodando = false;
  est.travado = false;
  if (est.raf) cancelAnimationFrame(est.raf);
  est.raf = null;
  botoes(false);
}

function botoes(rodando) {
  $('btn-comecar').disabled = rodando;
  $('btn-parar').disabled = !rodando;
  $('btn-exemplo').disabled = rodando;
}

/* ============================================================
   AFINADOR
   ============================================================ */
function montarAfinador() {
  $('af-cordas').innerHTML = CORDAS_ORDEM.map(c =>
    `<div class="af-corda" id="af-${c}">
       <b>${CORDA[c].pt} (${CORDA[c].en})</b>
       <span>${CORDA[c].num} corda · ${(440 * Math.pow(2, (CORDA[c].midi - 69) / 12)).toFixed(2)} Hz</span>
     </div>`).join('');
}

function atualizarAfinador(det) {
  if (!det) return;
  // corda solta mais próxima
  let alvo = null, menor = 99;
  for (const c of CORDAS_ORDEM) {
    const dif = Math.abs(det.midiExato - CORDA[c].midi);
    if (dif < menor) { menor = dif; alvo = c; }
  }
  document.querySelectorAll('.af-corda').forEach(e => e.classList.remove('ativa'));
  if (menor < 3) $('af-' + alvo).classList.add('ativa');

  $('af-nota').textContent = `${nomePt(det.midi)}`;
  const cents = det.cents;
  $('af-agulha').style.left = (50 + Math.max(-50, Math.min(50, cents)) * 0.9) + '%';
  const cor = Math.abs(cents) <= 5 ? 'var(--verde)' : 'var(--laranja)';
  $('af-agulha').style.background = cor;
  $('af-nota').style.color = cor;
  $('af-texto').innerHTML = Math.abs(cents) <= 5
    ? `<b style="color:var(--verde)">Afinado!</b> ${nomeEn(det.midi)} · ${det.freq.toFixed(2)} Hz`
    : (cents < 0
        ? `Está <b>baixo</b> ${Math.abs(cents)} cents — aperte a tarraxa (aumente a tensão).`
        : `Está <b>alto</b> ${cents} cents — solte a tarraxa (diminua a tensão).`) +
      ` · ${det.freq.toFixed(2)} Hz`;
}

/* ============================================================
   MICROFONE + ROTEAMENTO DA DETECÇÃO
   ============================================================ */
const escapar = t => String(t).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

/** Preenche a lista de entradas de áudio. Os nomes só aparecem depois da permissão. */
async function listarMics() {
  const sel = $('sel-mic');
  try {
    const disp = await Audio2.listarMicrofones();
    const escolhido = Audio2.dispositivoAtual() || sel.value;
    sel.innerHTML = '<option value="">Microfone padrão do sistema</option>' +
      disp.map((d, i) => {
        const nome = d.label || `Entrada de áudio ${i + 1} (permita o microfone para ver o nome)`;
        return `<option value="${escapar(d.deviceId)}">${escapar(nome)}</option>`;
      }).join('');
    if (escolhido && [...sel.options].some(o => o.value === escolhido)) sel.value = escolhido;
  } catch (e) {
    /* navegador sem suporte a enumerateDevices: fica só o padrão */
  }
}

async function ligarMic(idDispositivo) {
  const trocando = Audio2.microfoneLigado();
  try {
    $('mic-status').textContent = trocando ? 'Trocando de entrada…' : 'Pedindo permissão…';
    const rotulo = await Audio2.ligarMicrofone(idDispositivo || undefined);
    $('barra-mic').classList.remove('mic-off');
    $('barra-mic').classList.add('mic-on');
    $('btn-mic').textContent = '🎤 Microfone ligado';
    $('btn-mic').disabled = true;
    $('mic-status').textContent = (rotulo ? `Ouvindo por: ${rotulo}. ` : 'Estou te ouvindo. ') +
      'Use fone de ouvido se puder — evita eco.';
    await listarMics();
  } catch (e) {
    if (idDispositivo && (e.name === 'OverconstrainedError' || e.name === 'NotFoundError')) {
      $('sel-mic').value = '';
      $('mic-status').textContent = 'Esse microfone não está mais disponível — voltando para o padrão.';
      return ligarMic(null);
    }
    $('barra-mic').classList.add('mic-off');
    $('barra-mic').classList.remove('mic-on');
    $('btn-mic').disabled = false;
    $('btn-mic').textContent = '🎤 Ligar microfone';
    $('mic-status').textContent = e.name === 'NotAllowedError'
      ? 'Você bloqueou o microfone. Clique no cadeado 🔒 ao lado do endereço e libere o microfone para este site.'
      : 'Não consegui acessar o microfone (' + e.name + '). Abra pelo endereço http://localhost e permita o microfone no navegador.';
  }
}

Audio2.aoOuvir(det => {
  // medidor de nível + nota detectada
  if (det) {
    $('nivel-barra').style.width = Math.min(100, det.rms * 400) + '%';
    $('mic-nota').textContent = `${nomePt(det.midi)} ${det.cents > 0 ? '+' : ''}${det.cents}¢`;
    $('mic-nota').style.color = Math.abs(det.cents) <= 8 ? 'var(--verde)' : 'var(--texto)';
  } else {
    $('nivel-barra').style.width = '0%';
    $('mic-nota').textContent = '—';
    $('mic-nota').style.color = 'var(--suave)';
  }

  if (est.modo === 'afinador') atualizarAfinador(det);
  else if (est.modo === 'aprender') avaliarAprender(det);
  else if (est.modo === 'tocar') avaliarTocar(det);
});

/* ============================================================
   EVENTOS
   ============================================================ */
/* Cada modo mostra um painel. Os modos 'aprender' e 'tocar' compartilham o painel da música.
   'usaMic' diz se a barra do microfone faz sentido naquele modo. */
const PAINEIS = {
  aprender:   { painel: 'painel-musica',     usaMic: true },
  tocar:      { painel: 'painel-musica',     usaMic: true },
  afinador:   { painel: 'painel-afinador',   usaMic: true },
  metronomo:  { painel: 'painel-metronomo',  usaMic: false },
  escalas:    { painel: 'painel-escalas',    usaMic: false },
  playalong:  { painel: 'painel-playalong',  usaMic: false },
  partituras: { painel: 'painel-partituras', usaMic: false },
};

function trocarModo(modo) {
  parar();
  if (window.Metronomo && est.modo === 'metronomo' && modo !== 'metronomo') Metronomo.parar();
  if (window.PlayAlong && est.modo === 'playalong' && modo !== 'playalong') PlayAlong.parar();
  est.modo = modo;
  const cfg = PAINEIS[modo] || PAINEIS.aprender;

  document.querySelectorAll('.modo').forEach(b => b.classList.toggle('ativo', b.dataset.modo === modo));
  Object.values(PAINEIS).forEach(p => {
    const el = $(p.painel);
    if (el) el.classList.add('oculto');
  });
  $(cfg.painel).classList.remove('oculto');

  $('barra-mic').classList.toggle('oculto', !cfg.usaMic);
  $('campo-musica').classList.toggle('oculto', cfg.painel !== 'painel-musica');

  if (modo === 'aprender') feedback('Modo <b>Aprender</b>: sem pressa. Eu só passo pra próxima nota quando você acertar.', 'espera');
  if (modo === 'tocar') feedback('Modo <b>Tocar no tempo</b>: metrônomo rodando, você acompanha. No fim eu te dou a nota.', 'espera');
  if (modo === 'playalong' && window.PlayAlong) PlayAlong.aoEntrar();
}

document.addEventListener('DOMContentLoaded', () => {
  montarBraco();
  montarListaMusicas();
  montarAfinador();
  selecionarMusica(MUSICAS[0].id);

  $('sel-musica').onchange = e => selecionarMusica(e.target.value);
  $('modos').onclick = e => { if (e.target.dataset.modo) trocarModo(e.target.dataset.modo); };

  // --- escolha da entrada de áudio ---
  listarMics();
  $('btn-mic').onclick = () => ligarMic($('sel-mic').value || null);
  $('btn-atualizar-mics').onclick = () => listarMics();
  $('sel-mic').onchange = e => {
    if (!Audio2.microfoneLigado()) {
      $('mic-status').textContent = 'Entrada escolhida. Clique em "Ligar microfone".';
      return;
    }
    parar();
    ligarMic(e.target.value || null);
  };
  Audio2.aoTrocarDispositivos(() => listarMics());

  $('bpm').oninput = e => {
    est.bpm = +e.target.value;
    $('bpm-valor').textContent = est.bpm;
  };
  document.querySelectorAll('[data-bpm-pct]').forEach(b => {
    b.onclick = () => {
      if (!est.musica) return;
      est.bpm = Math.round(est.musica.bpm * (+b.dataset.bpmPct) / 100);
      $('bpm').value = est.bpm;
      $('bpm-valor').textContent = est.bpm;
    };
  });

  $('btn-comecar').onclick = () => {
    Audio2.contexto();
    est.modo === 'tocar' ? comecarTocar() : comecarAprender();
  };
  $('btn-parar').onclick = () => { parar(); feedback('Parado. Clique em <b>Começar</b> quando quiser.', 'espera'); };
  $('btn-exemplo').onclick = () => { Audio2.contexto(); ouvirExemplo(); };
  $('btn-anterior').onclick = () => irPara(est.indice - 1);
  $('btn-pular').onclick = () => {
    if (est.musica && est.resultados[est.indice] === null) est.resultados[est.indice] = 'erro';
    irPara(est.indice + 1);
  };

  document.addEventListener('keydown', e => {
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT' || e.target.tagName === 'TEXTAREA') return;

    if (est.modo === 'metronomo') {
      if (e.code === 'Space' && window.Metronomo) { e.preventDefault(); Metronomo.alternar(); }
      return;
    }
    if (est.modo === 'playalong') {
      if (e.code === 'Space' && window.PlayAlong) { e.preventDefault(); PlayAlong.alternar(); }
      return;
    }
    if (est.modo === 'escalas' || est.modo === 'partituras') return;

    if (e.code === 'Space') { e.preventDefault(); est.rodando ? parar() : $('btn-comecar').click(); }
    if (e.code === 'ArrowRight') $('btn-pular').click();
    if (e.code === 'ArrowLeft') $('btn-anterior').click();
  });
});

/* Ponte para os outros arquivos (escalas.js precisa carregar uma música gerada) */
window.Aula = {
  est,
  selecionarMusica,
  montarListaMusicas,
  trocarModo,
  ouvirExemplo,
  midiDaNota,
  nomePt,
  nomeEn,
  CORDA,
  CORDAS_ORDEM,
  DEDOS,
  MAX_CASA,
  MARCADORES,
  NOME_PT,
  NOME_EN,
};
