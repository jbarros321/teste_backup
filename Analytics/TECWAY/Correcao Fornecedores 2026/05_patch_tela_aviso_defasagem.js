/* =====================================================================
   05 - AVISO DE DEFASAGEM NA TELA DO BP INTERNO
   Aplicar em Analytics/TECWAY/BP Interno/bp.html

   Por que: em 22/09/2026 a tela mostrou 1.434.848,92 em Fornecedores,
   que e o saldo de 31/03/2026 - a ultima competencia carregada. Nada na
   tela indicava isso. O usuario comparou com o Sankhya de 21/09 e achou
   que a conta estava errada. Enquanto a carga do balancete puder atrasar,
   a tela precisa dizer ate quando ela tem dado.

   Depende da query do arquivo 04, que devolve duas colunas novas:
   REF_MAX_DISPONIVEL ('MM/AAAA') e MESES_DEFASAGEM (inteiro).
   ===================================================================== */

/* ---------------------------------------------------------------------
   PASSO 1 - No HTML, logo depois de <div id="report-container" ...>,
   inserir o container do aviso:

       <div id="aviso-defasagem" class="hidden"></div>
   ------------------------------------------------------------------- */

/* ---------------------------------------------------------------------
   PASSO 2 - Adicionar esta funcao junto das outras de render.
   ------------------------------------------------------------------- */
function renderAvisoDefasagem(refMax, mesesDefasagem, dataReferencia) {
    const el = document.getElementById('aviso-defasagem');
    if (!el) return;

    if (!refMax) {
        el.className = 'mb-4 rounded-lg border border-destructive bg-destructive/10 px-4 py-3 text-sm text-destructive';
        el.textContent = 'Não há balancete carregado para esta empresa. Os valores abaixo são todos zero.';
        el.classList.remove('hidden');
        return;
    }

    if (!mesesDefasagem || mesesDefasagem <= 0) {
        el.classList.add('hidden');
        el.textContent = '';
        return;
    }

    const refPedida = String(dataReferencia.getMonth() + 1).padStart(2, '0')
                    + '/' + dataReferencia.getFullYear();
    const plural = mesesDefasagem > 1 ? 'meses' : 'mês';

    el.className = 'mb-4 rounded-lg border border-amber-400 bg-amber-50 px-4 py-3 text-sm text-amber-900';
    el.innerHTML =
        '<strong>Balancete desatualizado.</strong> Você pediu ' + refPedida
      + ', mas a última competência carregada é <strong>' + refMax + '</strong> ('
      + mesesDefasagem + ' ' + plural + ' de atraso). Os saldos abaixo são os de '
      + refMax + ' — não compare com o Sankhya em data posterior sem antes '
      + 'atualizar a carga.';
    el.classList.remove('hidden');
}

/* ---------------------------------------------------------------------
   PASSO 3 - Em fetchAndRenderData(), no bloco que monta o colMap,
   acrescentar as duas colunas novas e chamar o aviso.

   Trocar:

       const colMap = {
           ordem: headers.indexOf('ORDEM'),
           nomeGrupo: headers.indexOf('NOME_GRUPO'),
           valorAtual: headers.indexOf('ANO_ATUAL'),
           valorAnterior: headers.indexOf('ANO_ANTERIOR')
       };

       if (Object.values(colMap).some(index => index === -1)) {

   por:
   ------------------------------------------------------------------- */
const colMap = {
    ordem: headers.indexOf('ORDEM'),
    nomeGrupo: headers.indexOf('NOME_GRUPO'),
    valorAtual: headers.indexOf('ANO_ATUAL'),
    valorAnterior: headers.indexOf('ANO_ANTERIOR')
};

/* colunas de defasagem: opcionais, para a tela seguir funcionando com a
   query antiga enquanto o patch do arquivo 04 nao for publicado */
const idxRefMax  = headers.indexOf('REF_MAX_DISPONIVEL');
const idxAtraso  = headers.indexOf('MESES_DEFASAGEM');

if (Object.values(colMap).some(index => index === -1)) {
    /* ... resto do tratamento de erro, sem alteração ... */
}

/* ---------------------------------------------------------------------
   PASSO 4 - Logo antes da chamada de renderReport(...), inserir:
   ------------------------------------------------------------------- */
if (idxRefMax !== -1 && reportResult.data.length > 0) {
    renderAvisoDefasagem(
        reportResult.data[0][idxRefMax],
        idxAtraso !== -1 ? parseInt(reportResult.data[0][idxAtraso], 10) : 0,
        dataReferencia
    );
}

renderReport(processedData, currentYear, previousYear);

/* ---------------------------------------------------------------------
   PASSO 5 - Mesma coisa nas outras telas que leem IMP_BASE_BALANCETE:
   BP Externo, DFC Externo e Indicadores. A carga atrasada nao e um
   problema so do BP Interno - todas mostram saldo de marco hoje.
   ------------------------------------------------------------------- */
