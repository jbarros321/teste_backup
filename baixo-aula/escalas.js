/* ============================================================
   ESCALAS E TONS — modo 5

   Você escolhe o tom (a tônica) e o que quer estudar; este arquivo
   escreve a tablatura, o dedilhado e joga tudo dentro do mesmo motor
   da aula — então o metrônomo, o braço e a correção pelo microfone
   funcionam igualzinho às músicas.
   ============================================================ */

const Escalas = (() => {

  const $ = id => document.getElementById(id);
  const A = () => window.Aula;

  const CORDAS_GRAVE_AGUDA = ['E', 'A', 'D', 'G'];   // da mais grossa para a mais fina
  const CASA_MAX = 12;

  /* ---------- o que dá para estudar ---------- */
  const TIPOS = [
    { id: 'maior',      nome: 'Escala maior (jônio)',        int: [0, 2, 4, 5, 7, 9, 11], dica: 'A escala mais usada de todas. Som alegre, "do-ré-mi".' },
    { id: 'menor',      nome: 'Escala menor natural (eólio)', int: [0, 2, 3, 5, 7, 8, 10], dica: 'A menor "triste". É a maior começando pelo 6º grau.' },
    { id: 'pent-maior', nome: 'Pentatônica maior',            int: [0, 2, 4, 7, 9],        dica: 'Cinco notas, nenhuma erra. Ótima para improvisar.' },
    { id: 'pent-menor', nome: 'Pentatônica menor',            int: [0, 3, 5, 7, 10],       dica: 'A base do rock e do blues no baixo. Decore esta primeiro.' },
    { id: 'blues',      nome: 'Escala de blues',              int: [0, 3, 5, 6, 7, 10],    dica: 'Pentatônica menor + a "nota azul" (b5). Sujinha, do jeito certo.' },
    { id: 'dorico',     nome: 'Dórico',                       int: [0, 2, 3, 5, 7, 9, 10], dica: 'Menor com o 6º maior. Som de funk e de soul.' },
    { id: 'frigio',     nome: 'Frígio',                       int: [0, 1, 3, 5, 7, 8, 10], dica: 'Menor com o 2º menor. Cheiro de flamenco e de metal.' },
    { id: 'lidio',      nome: 'Lídio',                        int: [0, 2, 4, 6, 7, 9, 11], dica: 'Maior com o 4º aumentado. Som de trilha de filme.' },
    { id: 'mixo',       nome: 'Mixolídio',                    int: [0, 2, 4, 5, 7, 9, 10], dica: 'Maior com o 7º menor. É o modo do acorde dominante (7).' },
    { id: 'locrio',     nome: 'Lócrio',                       int: [0, 1, 3, 5, 6, 8, 10], dica: 'O modo do acorde meio-diminuto (m7b5). Tenso.' },
    { id: 'men-harm',   nome: 'Menor harmônica',              int: [0, 2, 3, 5, 7, 8, 11], dica: 'Menor com o 7º maior. Aquele salto árabe entre o 6º e o 7º.' },
    { id: 'men-mel',    nome: 'Menor melódica',               int: [0, 2, 3, 5, 7, 9, 11], dica: 'Menor com 6º e 7º maiores. Muito usada no jazz.' },
    { id: 'cromatica',  nome: 'Cromática (todas as notas)',   int: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11], dica: 'Exercício de mão: um dedo por casa, sem pular nada.' },
    { id: 'arp-maior',  nome: 'Arpejo maior (1-3-5)',         int: [0, 4, 7],              dica: 'As notas do acorde maior. É o esqueleto de qualquer linha de baixo.' },
    { id: 'arp-menor',  nome: 'Arpejo menor (1-b3-5)',        int: [0, 3, 7],              dica: 'As notas do acorde menor.' },
    { id: 'arp-7',      nome: 'Arpejo dominante (1-3-5-b7)',  int: [0, 4, 7, 10],          dica: 'O arpejo do acorde 7 — blues, samba, funk.' },
    { id: 'arp-m7',     nome: 'Arpejo menor 7 (1-b3-5-b7)',   int: [0, 3, 7, 10],          dica: 'O arpejo do acorde m7.' },
    { id: 'arp-maj7',   nome: 'Arpejo maior 7 (1-3-5-7)',     int: [0, 4, 7, 11],          dica: 'O arpejo do acorde maj7. Som de bossa.' },
  ];

  let gerada = null;   // último objeto de música gerado

  const abertaMidi = c => A().CORDA[c].midi;
  const pc = m => ((m % 12) + 12) % 12;

  /* ============================================================
     1) As notas (em MIDI) que a escala tem, subindo
     ============================================================ */
  function sequenciaMidi(inicio, intervalos, oitavas, volta) {
    const subindo = [];
    for (let o = 0; o < oitavas; o++) {
      for (const iv of intervalos) subindo.push(inicio + o * 12 + iv);
    }
    subindo.push(inicio + oitavas * 12);        // fecha na tônica de cima
    if (!volta) return subindo;
    const descendo = subindo.slice(0, -1).reverse();
    return subindo.concat(descendo);
  }

  /* ============================================================
     2) Onde apertar cada nota — mão parada numa posição
        pos = casa onde fica o indicador. A caixa vai de pos-1 a pos+4.
     ============================================================ */
  function dedilhar(midis, pos) {
    const saida = [];
    let posAtual = pos;      // casa onde está o indicador agora
    let cordaAtual = 0;

    for (let k = 0; k < midis.length; k++) {
      const midi = midis[k];
      const subindo = k === 0 || midi >= midis[k - 1];
      let achou = null;
      let deslizou = false;

      // 1) tenta sem tirar a mão do lugar, trocando de corda.
      //    Entre as opções que cabem na caixa, fica com a casa mais baixa —
      //    é isso que faz a mão passar para a corda de cima em vez de esticar.
      for (const folga of [0, 1]) {
        const min = Math.max(0, posAtual - 1 - folga);
        const max = posAtual + 3 + folga;
        for (let i = 0; i < CORDAS_GRAVE_AGUDA.length; i++) {
          const c = CORDAS_GRAVE_AGUDA[i];
          const casa = midi - abertaMidi(c);
          if (casa < 0 || casa > CASA_MAX) continue;
          if (casa < min || casa > max) continue;
          // na primeira tentativa evito pular de volta para cordas bem mais graves
          if (folga === 0 && i < cordaAtual - 1) continue;
          if (!achou || casa < achou.f) achou = { c, f: casa, corda: i };
        }
        if (achou) break;
      }

      // 2) não deu: a mão desliza para a casa mais perto que serve
      if (!achou) {
        let melhor = null;
        for (let i = CORDAS_GRAVE_AGUDA.length - 1; i >= 0; i--) {
          const c = CORDAS_GRAVE_AGUDA[i];
          const casa = midi - abertaMidi(c);
          if (casa < 0 || casa > CASA_MAX) continue;
          if (i < cordaAtual - 1) continue;
          const custo = Math.abs(casa - posAtual);
          if (!melhor || custo < melhor.custo) melhor = { c, f: casa, corda: i, custo };
        }
        if (!melhor) return null;                  // não cabe no braço mesmo
        achou = melhor;
        deslizou = true;
        // subindo, a mão pousa com o indicador; descendo, com o mindinho
        posAtual = Math.max(0, subindo ? achou.f : achou.f - 3);
      }

      cordaAtual = achou.corda;
      const dedo = achou.f === 0 ? 0 : Math.max(1, Math.min(4, achou.f - posAtual + 1));
      saida.push({ c: achou.c, f: achou.f, d: dedo, desliza: deslizou || undefined });
    }
    return saida;
  }

  /* ============================================================
     3) Posições possíveis para a tônica escolhida
     ============================================================ */
  function posicoesPossiveis(tonica, intervalos, oitavas, volta) {
    const lista = [];
    for (const c of ['E', 'A', 'D']) {
      for (let casa = 0; casa <= CASA_MAX; casa++) {
        const midi = abertaMidi(c) + casa;
        if (pc(midi) !== tonica) continue;
        const midis = sequenciaMidi(midi, intervalos, oitavas, volta);
        // nas escalas maiores a tônica cai no dedo médio (é o desenho clássico do baixo);
        // nas menores e pentatônicas ela cai no indicador
        const pos = casa === 0 ? 0 : (intervalos.includes(4) ? Math.max(0, casa - 1) : casa);
        if (!dedilhar(midis, pos)) continue;
        lista.push({
          valor: c + '-' + casa,
          corda: c,
          casa,
          midi,
          pos,
          rotulo: `Corda ${A().CORDA[c].pt} (${A().CORDA[c].en}) · ` +
                  (casa === 0 ? 'corda solta' : `casa ${casa}`) +
                  ` — ${A().nomeEn(midi)}`,
        });
      }
    }
    return lista;
  }

  /* ============================================================
     4) Tablatura em texto (dá para copiar e imprimir)
     ============================================================ */
  function tabTexto(notas, compasso) {
    const linhas = { G: '', D: '', A: '', E: '' };
    const ordem = ['G', 'D', 'A', 'E'];
    let compassoAtual = 0;

    notas.forEach(n => {
      const comp = Math.floor(n.t / compasso);
      if (comp !== compassoAtual) {
        ordem.forEach(c => { linhas[c] += '|'; });
        compassoAtual = comp;
      }
      const txt = String(n.f);
      const larg = Math.max(2, txt.length + 1);
      ordem.forEach(c => {
        linhas[c] += c === n.c ? (txt + '-'.repeat(larg - txt.length)) : '-'.repeat(larg);
      });
    });

    return ordem.map(c => `${c}|-${linhas[c]}-|`).join('\n');
  }

  /* ============================================================
     5) Mapa do braço com TODAS as notas do tom
     ============================================================ */
  function desenharMapa(tonica, intervalos) {
    const alvo = $('esc-braco');
    if (!alvo) return;
    const graus = new Set(intervalos.map(i => pc(tonica + i)));
    const cols = `72px 62px repeat(${CASA_MAX}, minmax(34px,1fr))`;
    alvo.innerHTML = '';

    for (const c of A().CORDAS_ORDEM) {
      const linha = document.createElement('div');
      linha.className = 'braco-linha';
      linha.style.gridTemplateColumns = cols;

      const rot = document.createElement('div');
      rot.className = 'braco-corda';
      rot.textContent = `${A().CORDA[c].num} ${A().CORDA[c].pt} (${A().CORDA[c].en})`;
      linha.appendChild(rot);

      for (let casa = 0; casa <= CASA_MAX; casa++) {
        const midi = abertaMidi(c) + casa;
        const cel = document.createElement('div');
        cel.className = 'casa' + (casa === 0 ? ' solta' : '');
        const dentro = graus.has(pc(midi));
        const eTonica = pc(midi) === tonica;
        cel.innerHTML = '<div class="fio"></div>' +
          ((c === 'D' || c === 'A') && A().MARCADORES.includes(casa) ? '<div class="marcador"></div>' : '') +
          `<div class="bolinha${dentro ? (eTonica ? ' tonica' : ' dotom') : ''}">${dentro ? A().nomePt(midi) : ''}</div>`;
        linha.appendChild(cel);
      }
      alvo.appendChild(linha);
    }

    const nums = document.createElement('div');
    nums.className = 'numeros-casas';
    nums.style.gridTemplateColumns = cols;
    nums.innerHTML = '<div></div><div>solta</div>' +
      Array.from({ length: CASA_MAX }, (_, i) => `<div>${i + 1}</div>`).join('');
    alvo.appendChild(nums);
  }

  /* ============================================================
     6) Gerar tudo
     ============================================================ */
  function gerar() {
    const tonica = +$('esc-tonica').value;
    const tipo = TIPOS.find(t => t.id === $('esc-tipo').value) || TIPOS[0];
    const ext = $('esc-oitavas').value;
    const oitavas = ext.startsWith('2') ? 2 : 1;
    const volta = ext.endsWith('v');
    const dur = +$('esc-ritmo').value;
    const bpm = +$('esc-bpm').value;

    const posicoes = posicoesPossiveis(tonica, tipo.int, oitavas, volta);
    if (!posicoes.length) {
      $('esc-saida').classList.remove('oculto');
      $('esc-titulo').textContent = 'Não cabe no braço';
      $('esc-notas-nomes').textContent = '';
      $('esc-dica').textContent = 'Esse tom com essa extensão passa da casa 12. Tente 1 oitava.';
      $('esc-tab').textContent = '';
      return;
    }

    const escolhida = posicoes.find(p => p.valor === $('esc-posicao').value) || posicoes[0];
    const midis = sequenciaMidi(escolhida.midi, tipo.int, oitavas, volta);
    const dedos = dedilhar(midis, escolhida.pos);

    const compasso = dur === 1 ? 4 : 4;
    let t = 0;
    const notas = dedos.map(n => {
      const nota = { c: n.c, f: n.f, d: n.d, t: +t.toFixed(4), dur };
      t += dur;
      return nota;
    });

    const nomeTom = A().NOME_PT[tonica] + ' (' + A().NOME_EN[tonica] + ')';
    gerada = {
      id: 'escala-gerada',
      nome: `${nomeTom} — ${tipo.nome}`,
      artista: `${oitavas} oitava${oitavas > 1 ? 's' : ''}${volta ? ', subindo e descendo' : ', subindo'} · ${escolhida.rotulo}`,
      nivel: 2,
      bpm,
      compasso,
      dica: tipo.dica + ' Mão parada: o indicador cobre a casa ' + (escolhida.pos || 1) +
            ', e cada dedo cuida de uma casa. Comece devagar e só acelere quando sair limpo.',
      notas,
    };

    // joga dentro do motor da aula (braço, tab, metrônomo, microfone)
    const MU = MUSICAS;   // vem do musicas.js
    const jaTem = MU.findIndex(m => m.id === 'escala-gerada');
    if (jaTem >= 0) MU.splice(jaTem, 1, gerada); else MU.push(gerada);
    A().montarListaMusicas();
    document.getElementById('sel-musica').value = 'escala-gerada';
    A().selecionarMusica('escala-gerada');

    // mostra a partitura aqui
    $('esc-saida').classList.remove('oculto');
    $('esc-titulo').textContent = gerada.nome;
    $('esc-notas-nomes').textContent = 'Notas do tom: ' +
      tipo.int.map(i => A().NOME_PT[pc(tonica + i)]).join(' · ') +
      `  |  ${notas.length} notas · ${bpm} BPM`;
    $('esc-dica').textContent = '💡 ' + gerada.dica;
    $('esc-tab').textContent = tabTexto(notas, compasso);
    desenharMapa(tonica, tipo.int);

    ['esc-ouvir', 'esc-metronomo', 'esc-aprender', 'esc-copiar'].forEach(id => { $(id).disabled = false; });
  }

  /* ============================================================
     Montagem da tela
     ============================================================ */
  function preencherPosicoes() {
    const sel = $('esc-posicao');
    if (!sel) return;
    const tonica = +$('esc-tonica').value;
    const tipo = TIPOS.find(t => t.id === $('esc-tipo').value) || TIPOS[0];
    const ext = $('esc-oitavas').value;
    const anterior = sel.value;
    const lista = posicoesPossiveis(tonica, tipo.int, ext.startsWith('2') ? 2 : 1, ext.endsWith('v'));
    sel.innerHTML = lista.length
      ? lista.map(p => `<option value="${p.valor}">${p.rotulo}</option>`).join('')
      : '<option value="">— não cabe no braço, use 1 oitava —</option>';
    if (lista.some(p => p.valor === anterior)) sel.value = anterior;
  }

  function ligar() {
    if (!$('esc-tonica')) return;

    $('esc-tonica').innerHTML = A().NOME_PT
      .map((n, i) => `<option value="${i}"${i === 7 ? ' selected' : ''}>${n} (${A().NOME_EN[i]})</option>`)
      .join('');
    $('esc-tipo').innerHTML = TIPOS.map(t => `<option value="${t.id}">${t.nome}</option>`).join('');
    preencherPosicoes();

    ['esc-tonica', 'esc-tipo', 'esc-oitavas'].forEach(id => {
      $(id).onchange = preencherPosicoes;
    });
    $('esc-bpm').oninput = e => { $('esc-bpm-valor').textContent = e.target.value; };

    $('esc-gerar').onclick = gerar;
    $('esc-ouvir').onclick = () => { Audio2.contexto(); A().ouvirExemplo(); };
    $('esc-metronomo').onclick = () => {
      A().trocarModo('tocar');
      document.getElementById('btn-comecar').click();
    };
    $('esc-aprender').onclick = () => {
      A().trocarModo('aprender');
      document.getElementById('btn-comecar').click();
    };
    $('esc-copiar').onclick = async () => {
      const txt = `${$('esc-titulo').textContent}\n${$('esc-notas-nomes').textContent}\n\n${$('esc-tab').textContent}\n`;
      try {
        await navigator.clipboard.writeText(txt);
        $('esc-copiar').textContent = '✅ Copiado!';
        setTimeout(() => { $('esc-copiar').textContent = '📋 Copiar tablatura'; }, 1500);
      } catch (e) {
        $('esc-copiar').textContent = 'Selecione o texto e copie com Ctrl+C';
      }
    };
  }

  document.addEventListener('DOMContentLoaded', ligar);

  // as funções internas ficam expostas para dar para testar/depurar no console
  return { gerar, TIPOS, sequenciaMidi, dedilhar, posicoesPossiveis, tabTexto };
})();

window.Escalas = Escalas;
