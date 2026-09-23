# Erro 2 — `As configurações do componente (...) não foram carregadas da plataforma`

## Por que a mensagem não ajuda

Ela é um **texto fixo**. Lista os quatro nomes sempre, qualquer que seja o que faltou:

```js
if (window.componentData && window.componentData.queryDados
    && window.componentData.varAno && window.componentData.varMes
    && window.componentData.varEmpresa) {
    initializeComponent();
} else if (Date.now() - startTime > maxWaitTime) {          // 5s
    // Erro Crítico: ... (queryDados, varAno, varMes, varEmpresa) ...
}
```

Basta **um** dos quatro ser `undefined`, `null` ou string vazia para cair no erro. A tela
não diz qual. Então "o varMes tem resultado" e "a tela reclama do varMes" não se
contradizem: são duas coisas diferentes.

## O que a trava testa de verdade

Ela **não** executa a query do parâmetro. Ela só lê uma propriedade do objeto
`window.componentData` pelo nome exato. Duas consequências:

1. **Nome de propriedade em JavaScript diferencia maiúscula de minúscula.** Se o
   parâmetro estiver registrado como `Varmes`, `VarMes` ou `varmes`, então
   `componentData.varMes` é `undefined` — mesmo com a query funcionando perfeitamente.
   Repare que nesta pasta os arquivos vieram com grafias diferentes: `varEmpresa.sql`
   (v minúsculo) e `Varmes.sql` (V maiúsculo). Se esses nomes espelham o que está
   registrado no componente, `Varmes` já é suficiente para derrubar a trava.
2. Um parâmetro que existe mas está com valor vazio também é *falsy* e derruba igual.

## O passo que encerra a dúvida

Com a tela aberta, no console do navegador:

```js
console.log(Object.keys(window.componentData || {}));
```

Isso lista os nomes **exatos** que a plataforma expõe. Compare com os quatro que a trava
espera — `queryDados`, `varAno`, `varMes`, `varEmpresa`. O que não aparecer igual,
letra por letra, é o culpado.

Para ver nome e valor de uma vez:

```js
Object.entries(window.componentData || {}).forEach(([k, v]) =>
    console.log(k, '=', typeof v === 'string' ? JSON.stringify(v.slice(0, 60)) : v));
```

## Os valores certos

O `queri1.sql` usa três variáveis — e trocou `:VAR_DATA_REF_DRE` (uma data) por **duas**
(ano e mês). É daí que veio um `varAno` que antes não existia:

| Parâmetro do componente | Valor |
|---|---|
| `queryDados` | conteúdo do `queri1.sql` |
| `varEmpresa` | `:VAR_EMPRESA_DRE` |
| `varMes` | `:VAR_MES_FILTRO` |
| `varAno` | `:VAR_ANO_FILTRO` |

`:VAR_MES_FILTRO` aceita `'MM/AAAA'` ou o literal `'Todos'`; com `'Todos'` (ou nulo) a
query corta em 31/12 do ano escolhido:

```sql
WHEN :VAR_MES_FILTRO IS NULL OR :VAR_MES_FILTRO = 'Todos'
  THEN LAST_DAY(STR_TO_DATE(CONCAT('31/12/', :VAR_ANO_FILTRO), '%d/%m/%Y'))
```

## A correção que evita repetir isso

`04_patch_tela_varAno.js` troca a trava por uma que:

- resolve o nome **ignorando maiúscula/minúscula** e aceitando apelidos, reaproveitando
  a ideia do `pickFirstQuery(obj, ...keys)` que já existe no `.claude/Externo.html`;
- diz **qual** parâmetro faltou, em vez de repetir a lista dos quatro;
- trata `varAno` como opcional, caindo no ano da data de referência quando ausente.
