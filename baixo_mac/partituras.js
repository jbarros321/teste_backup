/* ============================================================
   BUSCAR PARTITURA — modo 7

   Você digita o nome da música e escolhe o instrumento; eu procuro no
   catálogo do Songsterr (partitura + tablatura interativa, com player) e
   mostro só as músicas que têm faixa daquele instrumento.

   Quem faz a chamada é o servidor local (rota /api/partituras): a API do
   Songsterr não manda cabeçalho CORS, então o navegador sozinho é barrado.
   ============================================================ */

const Partituras = (() => {

  const $ = id => document.getElementById(id);

  const FAMILIAS = {
    baixo:    'Baixo',
    guitarra: 'Guitarra / violão',
    bateria:  'Bateria',
    vocal:    'Voz',
    teclado:  'Teclado / piano',
    outro:    'Outros',
  };

  const escapar = t => String(t == null ? '' : t)
    .replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

  /** Em que família cada faixa se encaixa. */
  function familia(faixa) {
    const texto = ((faixa.instrument || '') + ' ' + (faixa.name || '') + ' ' + (faixa.hash || '')).toLowerCase();
    const id = faixa.instrumentId;

    if (faixa.isVocalTrack || /vocal|voz|singer/.test(texto)) return 'vocal';
    if (id === 1024 || /drum|percuss/.test(texto)) return 'bateria';
    if (/\bbass\b|baixo/.test(texto) || (id >= 32 && id <= 39)) return 'baixo';
    if (/guitar|violão|violao|acoustic/.test(texto) || (id >= 24 && id <= 31)) return 'guitarra';
    if (/piano|organ|keyboard|synth|rhodes/.test(texto) || (id >= 0 && id <= 7) || (id >= 16 && id <= 23)) return 'teclado';
    return 'outro';
  }

  /** Afinação em MIDI -> texto ("Mi–Lá–Ré–Sol", da grave para a aguda). */
  function afinacao(tuning) {
    if (!tuning || !tuning.length) return '';
    const nomes = [...tuning].reverse().map(m => window.Aula.NOME_PT[((m % 12) + 12) % 12]);
    const padrao4 = tuning.length === 4 && tuning.join() === '43,38,33,28';
    return `${tuning.length} cordas · ${nomes.join('–')}${padrao4 ? ' (afinação padrão do seu baixo)' : ''}`;
  }

  const semAcento = s => String(s || '')
    .normalize('NFD').replace(/[̀-ͯ]/g, '');

  /** Endereço do Songsterr.
      A faixa NÃO vai como "?t=2" (o site ignora esse parâmetro e abre sempre a
      faixa padrão) — ela é um sufixo do id: ...-tab-s432003t2. O texto do slug
      é enfeite: o Songsterr redireciona pelo id e conserta o resto sozinho. */
  function endereco(musica, indiceFaixa) {
    const limpar = s => semAcento(s)
      .toLowerCase()
      .replace(/^the\s+/, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-|-$/g, '');
    const slug = `${limpar(musica.artist)}-${limpar(musica.title)}` || 'tab';
    const faixa = (indiceFaixa === undefined || indiceFaixa === null) ? '' : 't' + indiceFaixa;
    return `https://www.songsterr.com/a/wsa/${slug}-tab-s${musica.songId}${faixa}`;
  }

  /** As faixas daquela música que interessam ao filtro, mais vistas primeiro. */
  function faixasDe(musica, filtro) {
    return (musica.tracks || [])
      .map((f, i) => ({ ...f, indice: i, fam: familia(f) }))
      .filter(f => filtro === 'todos' || f.fam === filtro)
      .sort((a, b) => (b.views || 0) - (a.views || 0));
  }

  /** Quão bem a música casa com o que foi digitado (0 a 3) — só para ordenar. */
  function relevancia(musica, termo) {
    const alvo = semAcento(`${musica.artist} ${musica.title}`).toLowerCase();
    const palavras = semAcento(termo).toLowerCase().split(/\s+/).filter(Boolean);
    if (!palavras.length) return 0;
    const acertos = palavras.filter(p => alvo.includes(p)).length;
    return acertos / palavras.length;
  }

  function cartao(musica, filtro) {
    const faixas = musica._faixas;
    if (!faixas || !faixas.length) return '';

    const chips = faixas.map(f => `
      <a class="pt-faixa fam-${f.fam}" href="${endereco(musica, f.indice)}" target="_blank" rel="noopener"
         title="${escapar(f.name || f.instrument)}">
        <b>${escapar(FAMILIAS[f.fam])}</b>
        <span>${escapar(f.instrument || '')}</span>
        <small>${escapar(afinacao(f.tuning))}</small>
      </a>`).join('');

    return `
      <div class="pt-cartao">
        <div class="pt-cabeca">
          <div>
            <b>${escapar(musica.title)}</b>
            <span class="sub">${escapar(musica.artist)}</span>
          </div>
          <a class="botao pequeno" href="${endereco(musica)}" target="_blank" rel="noopener">Abrir no Songsterr ↗</a>
        </div>
        <div class="pt-faixas">${chips}</div>
      </div>`;
  }

  function linksDeReserva(termo) {
    const q = encodeURIComponent(termo);
    return `
      <div class="pt-reserva">
        Não achou? Procure também em:
        <a href="https://www.cifraclub.com.br/?q=${q}" target="_blank" rel="noopener">Cifra Club</a> ·
        <a href="https://www.ultimate-guitar.com/search.php?search_type=title&value=${q}" target="_blank" rel="noopener">Ultimate Guitar</a> ·
        <a href="https://www.songsterr.com/?pattern=${q}" target="_blank" rel="noopener">Songsterr</a>
      </div>`;
  }

  /** Diagnóstico honesto: diz qual das causas é, em vez de listar todas. */
  async function explicarFalha(motivo) {
    if (location.protocol === 'file:') {
      return 'A página foi aberta com dois cliques no <code>index.html</code>. ' +
             'Feche e abra pelo <code>iniciar.command</code> — a busca precisa do servidor local.';
    }
    try {
      const r = await fetch('/api/partituras?q=', { cache: 'no-store' });
      if (r.status === 400) {
        return 'O servidor está de pé, mas não conseguiu falar com o Songsterr. ' +
               'Normalmente é internet fora do ar (ou o site do Songsterr fora). ' +
               'Detalhe técnico: ' + escapar(motivo);
      }
      if (r.status === 404) {
        return 'Esta página está sendo servida por um servidor sem a rota de busca ' +
               '(o plano B de Python antigo). Feche a janela do Terminal e abra de novo ' +
               'pelo <code>iniciar.command</code>.';
      }
    } catch (e) {
      return 'O servidor local caiu — a janela do Terminal foi fechada? ' +
             'Abra de novo pelo <code>iniciar.command</code>.';
    }
    return 'Não consegui buscar. Detalhe técnico: ' + escapar(motivo);
  }

  async function buscar() {
    const termo = $('pt-q').value.trim();
    const filtro = $('pt-instrumento').value;
    if (!termo) { $('pt-status').textContent = 'Escreva o nome da música primeiro.'; return; }

    $('pt-status').textContent = 'Procurando “' + termo + '”…';
    $('pt-resultados').innerHTML = '';

    let dados;
    try {
      const r = await fetch('/api/partituras?q=' + encodeURIComponent(termo), { cache: 'no-store' });
      const txt = await r.text();
      try { dados = JSON.parse(txt); } catch (e) { dados = null; }
      if (!r.ok || !Array.isArray(dados)) {
        throw new Error((dados && dados.erro) || ('HTTP ' + r.status));
      }
    } catch (e) {
      $('pt-status').innerHTML = await explicarFalha(e.message);
      $('pt-resultados').innerHTML = linksDeReserva(termo);
      return;
    }

    // Fora o lixo do catálogo; guarda as faixas que sobraram; ordena por
    // relevância e depois por quantas vezes a faixa foi aberta.
    const vistos = new Set();
    const lista = dados
      .filter(m => m && m.songId && !m.isJunk)
      .filter(m => { const k = m.songId; if (vistos.has(k)) return false; vistos.add(k); return true; });

    const comFaixa = lista
      .map(m => ({ ...m, _faixas: faixasDe(m, filtro) }))
      .filter(m => m._faixas.length)
      .sort((a, b) => {
        const dr = relevancia(b, termo) - relevancia(a, termo);
        if (Math.abs(dr) > 0.001) return dr;
        return (b._faixas[0].views || 0) - (a._faixas[0].views || 0);
      });

    if (!comFaixa.length) {
      $('pt-status').textContent = lista.length
        ? `Achei ${lista.length} música(s), mas nenhuma tem faixa de ${FAMILIAS[filtro] || filtro}. Tente "Todos os instrumentos".`
        : 'Não achei nada com esse nome. Tente escrever também o artista.';
      $('pt-resultados').innerHTML = linksDeReserva(termo);
      return;
    }

    $('pt-status').innerHTML =
      `${comFaixa.length} música(s) com faixa de <b>${escapar(FAMILIAS[filtro] || 'todos os instrumentos')}</b>. ` +
      'Clique na faixa que quer — abre a partitura interativa já naquela faixa, numa aba nova.';
    $('pt-resultados').innerHTML = comFaixa.map(m => cartao(m, filtro)).join('') + linksDeReserva(termo);
  }

  function ligar() {
    if (!$('pt-buscar')) return;
    $('pt-buscar').onclick = buscar;
    $('pt-q').addEventListener('keydown', e => { if (e.key === 'Enter') buscar(); });
  }

  document.addEventListener('DOMContentLoaded', ligar);

  return { buscar, familia, afinacao, endereco, faixasDe, relevancia };
})();

window.Partituras = Partituras;
