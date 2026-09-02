/* ============================================================
   BUSCAR PARTITURA — modo 7

   Você digita o nome da música e escolhe o instrumento; eu procuro no
   catálogo do Songsterr (partitura + tablatura interativa, com player) e
   mostro só as músicas que têm faixa daquele instrumento.

   Quem faz a chamada é o servidor local (servidor.js, rota /api/partituras):
   o navegador sozinho é barrado pela política de CORS.
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

  /** Afinação em MIDI -> texto ("Mi Lá Ré Sol", da grave para a aguda). */
  function afinacao(tuning) {
    if (!tuning || !tuning.length) return '';
    const nomes = [...tuning].reverse().map(m => window.Aula.NOME_PT[((m % 12) + 12) % 12]);
    const padrao4 = tuning.length === 4 && tuning.join() === '43,38,33,28';
    return `${tuning.length} cordas · ${nomes.join('–')}${padrao4 ? ' (afinação padrão do seu baixo)' : ''}`;
  }

  /** Endereço canônico do Songsterr. */
  function endereco(musica, indiceFaixa) {
    const limpar = s => String(s || '')
      .normalize('NFD').replace(/[̀-ͯ]/g, '')
      .toLowerCase()
      .replace(/^the\s+/, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-|-$/g, '');
    const slug = `${limpar(musica.artist)}-${limpar(musica.title)}`;
    const base = `https://www.songsterr.com/a/wsa/${slug}-tab-s${musica.songId}`;
    return indiceFaixa === undefined ? base : `${base}?t=${indiceFaixa}`;
  }

  function cartao(musica, filtro) {
    const faixas = (musica.tracks || [])
      .map((f, i) => ({ ...f, indice: i, fam: familia(f) }))
      .filter(f => filtro === 'todos' || f.fam === filtro)
      .sort((a, b) => (b.views || 0) - (a.views || 0));

    if (!faixas.length) return '';

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

  async function buscar() {
    const termo = $('pt-q').value.trim();
    const filtro = $('pt-instrumento').value;
    if (!termo) { $('pt-status').textContent = 'Escreva o nome da música primeiro.'; return; }

    $('pt-status').textContent = 'Procurando “' + termo + '”…';
    $('pt-resultados').innerHTML = '';

    let dados;
    try {
      const r = await fetch('/api/partituras?q=' + encodeURIComponent(termo));
      const txt = await r.text();
      try { dados = JSON.parse(txt); } catch (e) { dados = null; }
      if (!r.ok || !dados) throw new Error((dados && dados.erro) || 'resposta inesperada');
    } catch (e) {
      $('pt-status').innerHTML =
        'Não consegui buscar. Duas causas possíveis: (1) a página foi aberta sem o servidor — ' +
        'feche e abra pelo <code>iniciar.bat</code> (ele precisa do <b>Node</b> instalado); ' +
        '(2) o computador está sem internet.';
      $('pt-resultados').innerHTML = linksDeReserva(termo);
      return;
    }

    const lista = Array.isArray(dados) ? dados : [];
    const cartoes = lista.map(m => cartao(m, filtro)).filter(Boolean);

    if (!cartoes.length) {
      $('pt-status').textContent = lista.length
        ? `Achei ${lista.length} música(s), mas nenhuma tem faixa de ${FAMILIAS[filtro] || filtro}. Tente "Todos os instrumentos".`
        : 'Não achei nada com esse nome. Tente escrever também o artista.';
      $('pt-resultados').innerHTML = linksDeReserva(termo);
      return;
    }

    $('pt-status').innerHTML =
      `${cartoes.length} música(s) com faixa de <b>${escapar(FAMILIAS[filtro] || 'todos os instrumentos')}</b>. ` +
      'Clique na faixa que quer — abre a partitura interativa numa aba nova.';
    $('pt-resultados').innerHTML = cartoes.join('') + linksDeReserva(termo);
  }

  function ligar() {
    if (!$('pt-buscar')) return;
    $('pt-buscar').onclick = buscar;
    $('pt-q').addEventListener('keydown', e => { if (e.key === 'Enter') buscar(); });
  }

  document.addEventListener('DOMContentLoaded', ligar);

  return { buscar };
})();

window.Partituras = Partituras;
