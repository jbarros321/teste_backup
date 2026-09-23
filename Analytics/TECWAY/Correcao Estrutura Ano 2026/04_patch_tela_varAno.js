/* =====================================================================
   04 - TRAVA DE INICIALIZACAO RESISTENTE A NOME DE PARAMETRO
   Aplicar na tela do BP (bp.html / a versao publicada do BP Externo).

   Problema: a trava le `componentData.varMes` / `.varAno` pelo nome
   exato. Nome de propriedade em JS diferencia maiuscula de minuscula,
   entao um parametro registrado como `Varmes` derruba a tela mesmo com a
   query funcionando. E a mensagem de erro e um texto fixo que cita os
   quatro nomes, sem dizer qual faltou.

   Ver 07_config_componente.md.
   ===================================================================== */

/* ---------------------------------------------------------------------
   PASSO 1 - resolvedor de parametro tolerante.
   Mesma ideia do pickFirstQuery(obj, ...keys) que ja existe no
   .claude/Externo.html, mas ignorando caixa.
   ------------------------------------------------------------------- */
function resolveParam(obj, ...nomes) {
    if (!obj) return null;

    /* 1a tentativa: nome exato, na ordem em que foram passados */
    for (const n of nomes) {
        const v = obj[n];
        if (typeof v === 'string' && v.trim()) return v.trim();
        if (typeof v === 'number') return String(v);
    }

    /* 2a tentativa: mesmo nome ignorando maiuscula/minuscula */
    const mapa = new Map(
        Object.keys(obj).map(k => [k.toLowerCase(), k])
    );
    for (const n of nomes) {
        const real = mapa.get(n.toLowerCase());
        if (!real) continue;
        const v = obj[real];
        if (typeof v === 'string' && v.trim()) {
            console.warn(`[BP] parâmetro "${real}" encontrado com grafia diferente de `
                       + `"${n}". Funciona, mas vale padronizar na plataforma.`);
            return v.trim();
        }
        if (typeof v === 'number') return String(v);
    }

    return null;
}

/* ---------------------------------------------------------------------
   PASSO 2 - trocar a trava.

   Substituir o bloco:

       if (window.componentData && window.componentData.queryDados
           && window.componentData.varAno && window.componentData.varMes
           && window.componentData.varEmpresa) {
           clearInterval(checkInterval);
           initializeComponent();
       } else if (Date.now() - startTime > maxWaitTime) {
           clearInterval(checkInterval);
           console.error("Erro: As configurações do componente (componentData) não foram carregadas a tempo.");
           document.getElementById('report-container').innerHTML = `... (queryDados, varAno, varMes, varEmpresa) ...`;
       }

   por:
   ------------------------------------------------------------------- */

/* obrigatorios: sem eles a tela nao tem o que mostrar */
const PARAMS_OBRIGATORIOS = {
    queryDados: ['queryDados', 'queryBP', 'queryBP1'],
    varEmpresa: ['varEmpresa', 'varEmpresaDre'],
    varMes:     ['varMes', 'varMesFiltro']
};
/* opcional: se faltar, cai no ano da data de referencia */
const PARAMS_OPCIONAIS = {
    varAno: ['varAno', 'varAnoFiltro']
};

/* preenchido pela trava, usado pelo fetch (passo 3) */
const params = {};

const checkInterval = setInterval(() => {
    const cd = window.componentData;

    const faltando = [];
    for (const [nome, apelidos] of Object.entries(PARAMS_OBRIGATORIOS)) {
        const v = resolveParam(cd, ...apelidos);
        if (v) params[nome] = v; else faltando.push(nome);
    }

    if (faltando.length === 0) {
        clearInterval(checkInterval);

        for (const [nome, apelidos] of Object.entries(PARAMS_OPCIONAIS)) {
            const v = resolveParam(cd, ...apelidos);
            if (v) params[nome] = v;
            else console.warn(`[BP] parâmetro opcional "${nome}" ausente. `
                            + `Usando o ano da data de referência.`);
        }

        initializeComponent();
        return;
    }

    if (Date.now() - startTime > maxWaitTime) {
        clearInterval(checkInterval);
        const disponiveis = Object.keys(cd || {});
        console.error('[BP] faltando: ' + faltando.join(', ')
                    + ' | componentData expõe: ' + (disponiveis.join(', ') || '(vazio)'));
        document.getElementById('report-container').innerHTML =
            '<div class="p-8 text-center text-red-700 bg-red-50 h-full flex items-center '
          + 'justify-center"><div>Erro Crítico: faltam as configurações <strong>'
          + faltando.join(', ')
          + '</strong> na configuração do componente.<br><span class="text-xs">'
          + 'A plataforma expôs: ' + (disponiveis.join(', ') || '(nada)')
          + '</span></div></div>';
    }
}, intervalTime);

/* ---------------------------------------------------------------------
   PASSO 3 - usar `params` no fetch, com o ano opcional.

   Em fetchAndRenderData(), trocar:

       const [reportResult, mesResult] = await Promise.all([
           queryMitra({ query: componentData.queryDados }),
           queryMitra({ query: `SELECT ${componentData.varMes}` })
       ]);

   por:
   ------------------------------------------------------------------- */
const consultas = [
    queryMitra({ query: params.queryDados }),
    queryMitra({ query: `SELECT ${params.varMes}` })
];
if (params.varAno) {
    consultas.push(queryMitra({ query: `SELECT ${params.varAno}` }));
}

const [reportResult, mesResult, anoResult] = await Promise.all(consultas);

/* ... depois de calcular dataReferencia, como hoje ... */

const anoSelecionado = (anoResult && anoResult.data && anoResult.data[0]
                        && anoResult.data[0][0])
    ? parseInt(anoResult.data[0][0], 10)
    : dataReferencia.getFullYear();

if (!Number.isInteger(anoSelecionado)) {
    throw new Error('Ano de referência inválido: '
        + JSON.stringify(anoResult && anoResult.data && anoResult.data[0]
                         && anoResult.data[0][0]));
}

/* ---------------------------------------------------------------------
   PASSO 4 - antes de tudo, confirmar os nomes reais no console:

       console.log(Object.keys(window.componentData || {}));

   O que nao aparecer igual, letra por letra, aos nomes esperados e o
   culpado. Com o patch acima a propria tela passa a mostrar essa lista
   na mensagem de erro.
   ------------------------------------------------------------------- */
