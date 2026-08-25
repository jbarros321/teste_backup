<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<!DOCTYPE html>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html lang="pt-BR">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Central de Atendimento Contabil</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <snk:load />
    <style>
        /* ============================================================
           Identidade visual — os mesmos tokens de frontend/src/index.css
           do projeto Mitra (tema escuro, primaria azul).
           ============================================================ */
        :root {
            --color-primary: #60a5fa;
            --color-primary-hover: #3b82f6;
            --color-primary-light: #93c5fd;
            --color-primary-bg: #1e3a5f;
            --color-bg: #0f1117;
            --color-surface: #1a1d27;
            --color-surface-alt: #20242f;
            --color-nav: #141620;
            --color-nav-text: #94a3b8;
            --color-nav-active: #60a5fa;
            --color-nav-hover: #1e2230;
            --color-border: #2a2d3a;
            --color-text: #f1f5f9;
            --color-text-secondary: #94a3b8;
            --kpi-bg-green: #0d2818;
            --kpi-bg-blue: #1a1a3e;
            --kpi-bg-cyan: #0c2d3e;
            --kpi-bg-amber: #2a2008;
            --kpi-bg-purple: #1e1040;
            --ok: #34d399;
            --warn: #fbbf24;
            --danger: #f87171;
        }

        * { box-sizing: border-box; }

        html, body {
            margin: 0; padding: 0; min-height: 100%;
            font-family: 'Inter', 'Segoe UI', sans-serif;
            background: var(--color-bg);
            color: var(--color-text);
            font-size: 14px;
        }

        .wrap { padding: 18px 24px 48px; }

        /* ── Barra superior ── */
        .topbar {
            display: flex; flex-wrap: wrap; align-items: center; gap: 12px;
            padding-bottom: 14px; margin-bottom: 16px;
            border-bottom: 1px solid var(--color-border);
        }
        .brand { display: flex; align-items: center; gap: 10px; margin-right: auto; }
        .brand-icon {
            width: 34px; height: 34px; border-radius: 10px; flex-shrink: 0;
            background: var(--color-primary-bg); color: var(--color-primary);
            display: flex; align-items: center; justify-content: center; font-weight: 800;
        }
        .brand h1 { margin: 0; font-size: 1.05rem; font-weight: 700; letter-spacing: -.01em; }
        .brand p { margin: 2px 0 0; font-size: .74rem; color: var(--color-text-secondary); }

        .chip-user {
            display: inline-flex; align-items: center; gap: 7px;
            background: var(--color-surface); border: 1px solid var(--color-border);
            border-radius: 999px; padding: 6px 12px; font-size: .75rem;
            color: var(--color-text-secondary);
        }
        .chip-user b { color: var(--color-text); font-weight: 600; }
        .dot { width: 7px; height: 7px; border-radius: 50%; background: var(--ok); flex-shrink: 0; }

        /* ── Navegacao por abas ── */
        .tabs { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 18px; }
        .tab {
            border: 1px solid transparent; background: transparent; cursor: pointer;
            color: var(--color-nav-text); font-family: inherit; font-size: .82rem; font-weight: 500;
            height: 36px; padding: 0 15px; border-radius: 999px; transition: all .2s;
        }
        .tab:hover { background: var(--color-nav-hover); color: var(--color-text); }
        .tab.ativa { background: var(--color-primary-bg); color: var(--color-nav-active); font-weight: 600; }

        /* ── Filtros ── */
        .filtros {
            display: flex; flex-wrap: wrap; gap: 8px; align-items: center;
            margin-bottom: 16px;
        }
        .campo, .btn {
            height: 38px; border-radius: 9px; font-family: inherit; font-size: .82rem;
            border: 1px solid var(--color-border);
            background: var(--color-surface); color: var(--color-text);
            padding: 0 11px; outline: none; transition: all .2s;
        }
        .campo:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(96,165,250,.15); }
        .campo::placeholder { color: #64748b; }
        select.campo { cursor: pointer; }
        .btn { cursor: pointer; font-weight: 600; display: inline-flex; align-items: center; gap: 6px; }
        .btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
        .btn-primario {
            background: var(--color-primary); border-color: var(--color-primary); color: #0b1220;
        }
        .btn-primario:hover { background: var(--color-primary-hover); color: #0b1220; }
        .rotulo { font-size: .74rem; color: var(--color-text-secondary); }

        /* ── Cards / superficies ── */
        .superficie {
            background: var(--color-surface); border: 1px solid var(--color-border);
            border-radius: 14px; box-shadow: 0 1px 3px rgba(0,0,0,.35);
        }
        .grid-kpi {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(178px, 1fr));
            gap: 12px; margin-bottom: 16px;
        }
        .kpi { padding: 15px 16px; }
        .kpi-topo { display: flex; align-items: center; gap: 9px; }
        .kpi-icone {
            width: 30px; height: 30px; border-radius: 50%; flex-shrink: 0;
            display: flex; align-items: center; justify-content: center; font-size: .8rem;
        }
        .kpi-rotulo { font-size: .73rem; color: var(--color-text-secondary); }
        .kpi-valor { margin-top: 9px; font-size: 1.65rem; font-weight: 700; font-variant-numeric: tabular-nums; }
        .kpi-nota { font-size: .7rem; color: var(--color-text-secondary); margin-top: 2px; }

        .grid-charts {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
            gap: 14px; margin-bottom: 16px;
        }
        .card-chart { padding: 16px; }
        .card-titulo { font-size: .88rem; font-weight: 600; margin: 0; }
        .card-sub { font-size: .72rem; color: var(--color-text-secondary); margin: 3px 0 12px; }
        .box-chart { position: relative; height: 260px; }

        /* ── Tabelas ── */
        .tabela-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: .8rem; }
        thead th {
            text-align: left; padding: 10px 12px; font-size: .7rem; font-weight: 600;
            text-transform: uppercase; letter-spacing: .04em;
            color: var(--color-text-secondary);
            border-bottom: 1px solid var(--color-border); white-space: nowrap;
        }
        tbody td { padding: 10px 12px; border-bottom: 1px solid var(--color-border); vertical-align: middle; }
        tbody tr { transition: background .15s; }
        tbody tr:hover { background: var(--color-nav-hover); }
        tbody tr.clicavel { cursor: pointer; }
        .num { text-align: right; font-variant-numeric: tabular-nums; }
        .vazio { padding: 34px; text-align: center; color: var(--color-text-secondary); font-size: .82rem; }

        /* ── Badges ── */
        .badge {
            display: inline-flex; align-items: center; gap: 6px; white-space: nowrap;
            border-radius: 999px; padding: 3px 9px; font-size: .69rem; font-weight: 600;
            border: 1px solid transparent;
        }
        .badge .ponto { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }

        /* ── Paginacao ── */
        .paginacao {
            display: flex; align-items: center; justify-content: space-between;
            gap: 10px; padding: 12px; border-top: 1px solid var(--color-border);
            font-size: .76rem; color: var(--color-text-secondary);
        }
        .paginacao button {
            height: 32px; min-width: 32px; padding: 0 10px; cursor: pointer;
            border-radius: 8px; border: 1px solid var(--color-border);
            background: var(--color-surface); color: var(--color-text); font-family: inherit;
        }
        .paginacao button:disabled { opacity: .4; cursor: not-allowed; }

        /* ── Modal ── */
        .modal-fundo {
            display: none; position: fixed; inset: 0; z-index: 60;
            background: rgba(5,7,12,.72); backdrop-filter: blur(2px);
            align-items: center; justify-content: center; padding: 20px;
        }
        .modal {
            width: min(940px, 100%); max-height: 88vh; display: flex; flex-direction: column;
            background: var(--color-surface); border: 1px solid var(--color-border);
            border-radius: 16px; overflow: hidden;
        }
        .modal-topo {
            display: flex; align-items: flex-start; gap: 12px; padding: 16px 18px;
            border-bottom: 1px solid var(--color-border);
        }
        .modal-corpo { padding: 18px; overflow-y: auto; }
        .fechar {
            margin-left: auto; background: transparent; border: none; cursor: pointer;
            color: var(--color-text-secondary); font-size: 1.25rem; line-height: 1; padding: 2px 6px;
        }
        .fechar:hover { color: var(--color-text); }
        .secao-titulo {
            font-size: .72rem; text-transform: uppercase; letter-spacing: .05em;
            color: var(--color-text-secondary); font-weight: 600; margin: 18px 0 8px;
        }
        .grid-info { display: grid; grid-template-columns: repeat(auto-fit, minmax(190px, 1fr)); gap: 12px; }
        .info-item { background: var(--color-surface-alt); border-radius: 10px; padding: 10px 12px; }
        .info-item .r { font-size: .69rem; color: var(--color-text-secondary); }
        .info-item .v { font-size: .84rem; font-weight: 500; margin-top: 3px; word-break: break-word; }
        .linha-tempo { border-left: 2px solid var(--color-border); margin-left: 6px; padding-left: 16px; }
        .evento { position: relative; padding-bottom: 15px; }
        .evento::before {
            content: ''; position: absolute; left: -22px; top: 4px;
            width: 9px; height: 9px; border-radius: 50%;
            background: var(--color-primary); border: 2px solid var(--color-bg);
        }
        .evento .quando { font-size: .69rem; color: var(--color-text-secondary); }
        .evento .oque { font-size: .82rem; margin-top: 2px; }

        /* ── Avisos ── */
        .aviso {
            border-radius: 11px; padding: 11px 14px; font-size: .8rem; margin-bottom: 14px;
            border: 1px solid; display: flex; gap: 9px; align-items: flex-start;
        }
        .aviso-erro { background: rgba(248,113,113,.09); border-color: rgba(248,113,113,.35); color: #fca5a5; }
        .aviso-info { background: var(--color-primary-bg); border-color: rgba(96,165,250,.35); color: var(--color-primary-light); }
        .carregando { padding: 40px; text-align: center; color: var(--color-text-secondary); font-size: .84rem; }
        .oculto { display: none !important; }

        @media (max-width: 640px) {
            .wrap { padding: 14px 12px 40px; }
            .box-chart { height: 220px; }
        }
    </style>
</head>

<body>
<div class="wrap">

    <!-- ════════════════ BARRA SUPERIOR ════════════════ -->
    <div class="topbar">
        <div class="brand">
            <div class="brand-icon">CC</div>
            <div>
                <h1>Central de Atendimento Contabil</h1>
                <p>Chamados, fila de atendimento e indicadores</p>
            </div>
        </div>
        <span class="chip-user"><span class="dot"></span> <b id="cab-usuario">conectando...</b> <span id="cab-papel"></span></span>
        <button class="btn" id="btn-atualizar" onclick="recarregarTudo()">Atualizar</button>
    </div>

    <!-- ════════════════ ABAS ════════════════ -->
    <div class="tabs" id="abas">
        <button class="tab ativa" data-view="painel"     onclick="trocarView('painel')">Painel</button>
        <button class="tab"       data-view="chamados"   onclick="trocarView('chamados')">Chamados</button>
        <button class="tab"       data-view="fila"       onclick="trocarView('fila')">Fila</button>
        <button class="tab"       data-view="usuarios"   onclick="trocarView('usuarios')">Usuarios</button>
        <button class="tab"       data-view="integracao" onclick="trocarView('integracao')">Integracao</button>
    </div>

    <div id="aviso-global"></div>

    <!-- ════════════════ VIEW: PAINEL ════════════════ -->
    <section id="view-painel">
        <div class="filtros">
            <span class="rotulo">Periodo</span>
            <input type="date" class="campo" id="f-data-ini" onchange="aplicarPainel()">
            <span class="rotulo">ate</span>
            <input type="date" class="campo" id="f-data-fim" onchange="aplicarPainel()">
            <select class="campo" id="f-status" onchange="aplicarPainel()"><option value="">Todos os status</option></select>
            <select class="campo" id="f-prioridade" onchange="aplicarPainel()"><option value="">Todas as prioridades</option></select>
            <select class="campo" id="f-empresa" onchange="aplicarPainel()"><option value="">Todas as empresas</option></select>
            <button class="btn" onclick="limparFiltrosPainel()">Limpar</button>
        </div>

        <div class="grid-kpi" id="kpis"></div>
        <div class="grid-kpi" id="indicadores"></div>

        <div class="grid-charts">
            <div class="superficie card-chart">
                <p class="card-titulo">Chamados por categoria</p>
                <p class="card-sub">Clique em uma barra para filtrar o painel</p>
                <div class="box-chart"><canvas id="ch-categoria"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Distribuicao por status</p>
                <p class="card-sub">Situacao atual da carteira</p>
                <div class="box-chart"><canvas id="ch-status"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Evolucao mensal</p>
                <p class="card-sub">Volume de chamados abertos por mes</p>
                <div class="box-chart"><canvas id="ch-mes"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Chamados por prioridade</p>
                <p class="card-sub">Urgencia declarada no atendimento</p>
                <div class="box-chart"><canvas id="ch-prioridade"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Chamados por empresa</p>
                <p class="card-sub">Origem da solicitacao</p>
                <div class="box-chart"><canvas id="ch-empresa"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Carga por responsavel</p>
                <p class="card-sub">Distribuicao do atendimento na equipe</p>
                <div class="box-chart"><canvas id="ch-responsavel"></canvas></div>
            </div>
        </div>

        <div class="superficie card-chart" style="margin-bottom:14px;">
            <p class="card-titulo">Detalhe por subcategoria</p>
            <p class="card-sub">Volume, conclusao, atraso e tempo medio de atendimento</p>
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>Subcategoria</th><th>Categoria</th>
                        <th class="num">Total</th><th class="num">Concluidos</th>
                        <th class="num">Atrasados</th><th class="num">Tempo medio (h)</th>
                    </tr></thead>
                    <tbody id="tb-subcategoria"></tbody>
                </table>
            </div>
        </div>

        <div class="superficie card-chart">
            <p class="card-titulo">Quem mais abre chamados</p>
            <p class="card-sub">15 maiores solicitantes no periodo</p>
            <div class="tabela-wrap">
                <table>
                    <thead><tr><th>Solicitante</th><th class="num">Chamados</th></tr></thead>
                    <tbody id="tb-solicitante"></tbody>
                </table>
            </div>
        </div>
    </section>

    <!-- ════════════════ VIEW: CHAMADOS ════════════════ -->
    <section id="view-chamados" class="oculto">
        <div class="filtros">
            <input type="text" class="campo" id="c-busca" placeholder="Buscar por numero, assunto ou descricao" style="min-width:280px;">
            <select class="campo" id="c-status"><option value="">Todos os status</option></select>
            <select class="campo" id="c-categoria"><option value="">Todas as categorias</option></select>
            <input type="date" class="campo" id="c-data-ini">
            <input type="date" class="campo" id="c-data-fim">
            <button class="btn btn-primario" onclick="carregarChamados(0)">Filtrar</button>
            <button class="btn" onclick="limparFiltrosChamados()">Limpar</button>
        </div>
        <div class="superficie">
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>Chamado</th><th>Abertura</th><th>Assunto</th><th>Categoria</th>
                        <th>Empresa</th><th>Status</th><th>Prioridade</th><th>Responsavel</th>
                    </tr></thead>
                    <tbody id="tb-chamados"></tbody>
                </table>
            </div>
            <div class="paginacao">
                <span id="c-info">—</span>
                <span>
                    <button id="c-ant" onclick="paginaChamados(-1)">Anterior</button>
                    <button id="c-prox" onclick="paginaChamados(1)">Proxima</button>
                </span>
            </div>
        </div>
    </section>

    <!-- ════════════════ VIEW: FILA ════════════════ -->
    <section id="view-fila" class="oculto">
        <div class="filtros">
            <input type="text" class="campo" id="q-busca" placeholder="Buscar por numero ou assunto" style="min-width:260px;">
            <select class="campo" id="q-status"><option value="">Todos os status</option></select>
            <select class="campo" id="q-prioridade"><option value="">Todas as prioridades</option></select>
            <select class="campo" id="q-meus">
                <option value="N">Toda a fila</option>
                <option value="S">Somente os meus</option>
            </select>
            <button class="btn btn-primario" onclick="carregarFila(0)">Filtrar</button>
            <button class="btn" onclick="limparFiltrosFila()">Limpar</button>
        </div>
        <div class="superficie">
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>Chamado</th><th>Prioridade</th><th>Assunto</th><th>Solicitante</th>
                        <th>Empresa</th><th>Status</th><th>Aguardando</th><th>Responsavel</th><th>Prazo</th>
                    </tr></thead>
                    <tbody id="tb-fila"></tbody>
                </table>
            </div>
            <div class="paginacao">
                <span id="q-info">—</span>
                <span>
                    <button id="q-ant" onclick="paginaFila(-1)">Anterior</button>
                    <button id="q-prox" onclick="paginaFila(1)">Proxima</button>
                </span>
            </div>
        </div>
    </section>

    <!-- ════════════════ VIEW: USUARIOS ════════════════ -->
    <section id="view-usuarios" class="oculto">
        <div class="grid-kpi" id="u-kpis"></div>
        <div class="filtros">
            <input type="text" class="campo" id="u-busca" placeholder="Buscar por e-mail" style="min-width:260px;">
            <select class="campo" id="u-papel">
                <option value="">Todos os papeis</option>
                <option value="GESTOR">Administrador</option>
                <option value="ATENDENTE">Atendente</option>
                <option value="APROVADOR">Aprovador</option>
                <option value="SOLICITANTE">Solicitante</option>
            </select>
            <button class="btn btn-primario" onclick="carregarUsuarios(0)">Filtrar</button>
            <button class="btn" onclick="limparFiltrosUsuarios()">Limpar</button>
        </div>
        <div class="superficie">
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>E-mail</th><th>Papel</th><th>Setor</th><th>Fila</th>
                        <th class="num">Contas</th><th class="num">Abertos</th>
                        <th class="num">Atribuidos</th><th>Ultimo acesso</th>
                    </tr></thead>
                    <tbody id="tb-usuarios"></tbody>
                </table>
            </div>
            <div class="paginacao">
                <span id="u-info">—</span>
                <span>
                    <button id="u-ant" onclick="paginaUsuarios(-1)">Anterior</button>
                    <button id="u-prox" onclick="paginaUsuarios(1)">Proxima</button>
                </span>
            </div>
        </div>
    </section>

    <!-- ════════════════ VIEW: INTEGRACAO ════════════════ -->
    <section id="view-integracao" class="oculto">
        <div class="aviso aviso-info">
            Estado da importacao de dados vinda do Sankhya — empresas, parceiros, produtos e documentos
            que alimentam o atendimento.
        </div>
        <div class="grid-kpi" id="i-entidades"></div>
        <div class="superficie card-chart">
            <p class="card-titulo">Historico de importacoes</p>
            <p class="card-sub">50 execucoes mais recentes</p>
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>Execucao</th><th>Entidade</th><th>Tipo</th><th>Status</th>
                        <th class="num">Registros</th><th class="num">Duracao</th><th>Quando</th><th>Erro</th>
                    </tr></thead>
                    <tbody id="tb-logs"></tbody>
                </table>
            </div>
        </div>
    </section>

</div>

<!-- ════════════════ MODAL DE DETALHE DO CHAMADO ════════════════ -->
<div class="modal-fundo" id="modal-chamado" onclick="if(event.target===this) fecharModal()">
    <div class="modal">
        <div class="modal-topo">
            <div>
                <p class="card-titulo" id="m-titulo">Chamado</p>
                <p class="card-sub" id="m-sub" style="margin-bottom:0;">—</p>
            </div>
            <button class="fechar" onclick="fecharModal()" title="Fechar">&times;</button>
        </div>
        <div class="modal-corpo" id="m-corpo">
            <div class="carregando">Carregando chamado...</div>
        </div>
    </div>
</div>

<script>
/* ================================================================================
   CENTRAL DE ATENDIMENTO CONTABIL — tela JSP autonoma

   Esta tela e uma reproducao do projeto Mitra p-57477 num unico arquivo JSP, para
   ser publicada como tela do Sankhya. Ela nao consulta o banco do Sankhya (nenhuma
   tag de query do sankhyaUtil e usada aqui): os dados estao no banco do projeto
   Mitra. Por isso a tela fala com a API do Mitra por HTTP, executando as MESMAS
   Server Functions que o projeto usa.

   IMPORTANTE — regra numero 1 para nao derrubar a tela no Sankhya:
   NUNCA escreva um prefixo de taglib registrado seguido de dois-pontos dentro de
   um sinal de menor, nem mesmo em comentario ou string de JavaScript. O compilador
   de JSP do servidor nao enxerga comentario de JS: ele le o arquivo inteiro como
   template e trata qualquer coisa no formato de tag customizada como tag de
   verdade. Uma mencao dessas em comentario ja foi a causa do erro interno que
   impedia esta tela de abrir (o Jasper acusava tag nao terminada).

   O arquivo tambem esta com isELIgnored="true": expressao no formato cifrao-chave
   nao e interpretada pelo servidor. Ainda assim, todo o JS abaixo usa CONCATENACAO
   de string, entao mantenha o padrao para o codigo continuar homogeneo.
   ================================================================================ */

/* ────────────────────────────────────────────────────────────────────────────────
   0) DIAGNOSTICO NA PROPRIA TELA

   No Sankhya a tela abre dentro de um iframe e o console do navegador nem sempre
   esta a mao. Qualquer erro nao tratado — inclusive recurso externo que a rede da
   empresa tenha bloqueado — vira uma faixa de aviso no topo, com o texto do erro,
   em vez de deixar a tela em branco.
   ──────────────────────────────────────────────────────────────────────────────── */
function avisoBruto(texto) {
    var alvo = document.getElementById('aviso-global');
    if (!alvo) { return; }
    var caixa = document.createElement('div');
    caixa.className = 'aviso aviso-erro';
    caixa.textContent = texto;
    alvo.innerHTML = '';
    alvo.appendChild(caixa);
}

window.addEventListener('error', function (evento) {
    var alvo = evento && evento.target;
    if (alvo && (alvo.tagName === 'SCRIPT' || alvo.tagName === 'LINK')) {
        avisoBruto('Nao foi possivel carregar o recurso externo ' + (alvo.src || alvo.href || '')
                 + '. Se a rede da empresa bloqueia CDN, publique esse arquivo no proprio'
                 + ' servidor do Sankhya e troque o endereco no topo desta tela.');
        return;
    }
    avisoBruto('Erro na tela: ' + ((evento && evento.message) || 'falha nao identificada'));
}, true);

window.addEventListener('unhandledrejection', function (evento) {
    var motivo = evento && evento.reason;
    avisoBruto('Falha ao falar com a plataforma Mitra: '
             + ((motivo && motivo.message) || motivo || 'causa desconhecida'));
});

/* ────────────────────────────────────────────────────────────────────────────────
   1) CREDENCIAIS — as mesmas do projeto Mitra (backend/.env)

   BASE_URL   = MITRA_BASE_URL
   PROJECT_ID = MITRA_PROJECT_ID
   TOKEN      = MITRA_TOKEN  (portador: jose.vilela@neuonsolucoes.com, papel GESTOR)

   Toda Server Function roda no contexto do dono deste token: e ele que a variavel
   :VAR_USER assume dentro do SQL. Como o portador e GESTOR, a tela enxerga os
   chamados de toda a operacao. Trocar o token troca o que a tela enxerga.
   ──────────────────────────────────────────────────────────────────────────────── */
var MITRA = {
    BASE_URL:   'https://analytics2.mitrasheet.com:4437',
    PROJECT_ID: 57477,
    TOKEN:      'Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJNaXRyYSIsImp0aSI6IjI1MTQwIiwic3ViIjoiam9zZS52aWxlbGFAbmV1b25zb2x1Y29lcy5jb20iLCJpYXQiOjE3ODc2NjEwOTcsInRuaSI6NTc0NzcsImFjY2Vzc1R5cGUiOiJDUkVBVE9SIiwiYmFja1VSTCI6Imh0dHBzOi8vYW5hbHl0aWNzMi5taXRyYXNoZWV0LmNvbTo0NDM3IiwiZXhwIjoyNjUxNjYxMDk3fQ.bo5nXtxfWpjiXUWqNQ5-_O0ysI-je9hJOw1UNUmpYlQ'
};

/* ────────────────────────────────────────────────────────────────────────────────
   2) IDS DAS SERVER FUNCTIONS — identicos a frontend/src/lib/sf.ts do projeto
   ──────────────────────────────────────────────────────────────────────────────── */
var SF = {
    listarTaxonomia: 1,        listarEmpresas: 2,          meuPerfil: 3,
    listarEquipe: 4,           listarMeusChamados: 11,     contarMeusChamados: 12,
    detalharChamado: 13,       listarDetalhesChamado: 14,  listarHistoricoChamado: 15,
    listarAnexosChamado: 16,   listarChamadosFila: 20,
    painelKpis: 25,            painelPorCategoria: 26,     painelPorPrioridade: 27,
    painelPorEmpresa: 28,      painelPorResponsavel: 29,   painelPorStatus: 30,
    painelPorMes: 31,          painelPorSubcategoria: 32,  painelPorSolicitante: 33,
    painelIndicadores: 34,
    drillPorSubcategoria: 35,  drillPorEmpresa: 36,        drillPorResponsavel: 37,
    drillPorPrioridade: 38,    drillPorStatus: 39,
    listarUsuariosProjeto: 66, contarUsuariosProjeto: 67,  resumoPapeisUsuarios: 68,
    listarSetores: 177,
    obterUltimasSyncs: 223,    listarLogsImportacao: 224
};

/* ────────────────────────────────────────────────────────────────────────────────
   3) CATALOGO DE SQL

   O SQL abaixo e o codigo real de cada Server Function usada por esta tela, copiado
   verbatim da plataforma. Ele NAO e enviado na chamada — quem executa e a Server
   Function, pelo id, no servidor. Esta copia existe para que o arquivo seja
   autoexplicativo: da pra ler aqui exatamente o que cada tela consulta, quais
   parametros aceita ({{nome}}) e como o controle de acesso e aplicado (:VAR_USER).

   Para inspecionar no navegador:  console.log(SQL_SF.painelKpis)
   ──────────────────────────────────────────────────────────────────────────────── */
var SQL_SF = {

    /* [3] meuPerfil (SQL)
       Retorna o usuario logado e seu papel (SOLICITANTE, ATENDENTE, GESTOR ou APROVADOR). */
    meuPerfil:
        "SELECT U.ID AS CODUSU, U.DESCR AS EMAIL,\n" +
        "       COALESCE(A.PAPEL, 'SOLICITANTE') AS PAPEL, A.CODFILA\n" +
        "FROM INT_USER U\n" +
        "LEFT JOIN AD_CTBATEND A ON A.CODUSU = U.ID AND A.ATIVO = 1\n" +
        "WHERE U.ID = :VAR_USER",

    /* [1] listarTaxonomia (SQL)
       Taxonomia completa: categorias, subcategorias ativas e campos exigidos de cada uma. Sem parametros. */
    listarTaxonomia:
        "SELECT CAT.ID AS CATID, CAT.NOME AS CATNOME, CAT.DESCRICAO AS CATDESC, CAT.ICONE, CAT.ORDEM AS CATORDEM,\n" +
        "       CAT.PROPOSTA AS CATPROPOSTA,\n" +
        "       SUB.ID AS SUBID, SUB.NOME AS SUBNOME, SUB.ORDEM AS SUBORDEM, SUB.PRIORIDADEPADRAO, SUB.SLAHORAS,\n" +
        "       SUB.EXIGEAPROVACAO, SUB.PEDEANEXO, SUB.PROPOSTA AS SUBPROPOSTA,\n" +
        "       F.CAMPO, F.ROTULO, F.TIPO, F.OBRIGATORIO, F.DOMINIO, F.AJUDA, F.ORDEM AS CAMPORDEM\n" +
        "FROM AD_CTBCAT CAT\n" +
        "JOIN AD_CTBSUB SUB ON SUB.CODCATEG = CAT.ID AND SUB.ATIVO = 1\n" +
        "LEFT JOIN AD_CTBCAMPO F ON F.CODSUBCAT = SUB.ID\n" +
        "WHERE CAT.ATIVO = 1\n" +
        "ORDER BY CAT.ORDEM, SUB.ORDEM, F.ORDEM",

    /* [2] listarEmpresas (SQL)
       Empresas disponiveis para o contexto da solicitacao. */
    listarEmpresas:
        "SELECT ID, CODEMP, NOME, CNPJ FROM AD_CTBEMP WHERE ATIVO = 1 ORDER BY ID",

    /* [25] painelKpis (SQL)
       KPIs do painel analitico com todos os filtros de cross-filter. Restrita a equipe. */
    painelKpis:
        "SELECT COUNT(*) AS TOTAL,\n" +
        "  SUM(CASE WHEN C.STATUS = 'CONCLUIDO' THEN 1 ELSE 0 END) AS CONCLUIDOS,\n" +
        "  SUM(CASE WHEN C.STATUS NOT IN ('CONCLUIDO','CANCELADO') THEN 1 ELSE 0 END) AS ABERTOS,\n" +
        "  SUM(CASE WHEN C.STATUS NOT IN ('CONCLUIDO','CANCELADO') AND C.DTPREVISTA < DATE_FORMAT(NOW(),'%Y-%m-%dT%H:%i:%s') THEN 1 ELSE 0 END) AS ATRASADOS,\n" +
        "  ROUND(AVG(C.TEMPOATEND), 1) AS TEMPOMEDIO,\n" +
        "  ROUND(AVG(CASE WHEN C.DTPRIMRESP IS NOT NULL\n" +
        "    THEN TIMESTAMPDIFF(MINUTE, STR_TO_DATE(C.DTABERTURA,'%Y-%m-%dT%H:%i:%s'),\n" +
        "                       STR_TO_DATE(C.DTPRIMRESP,'%Y-%m-%dT%H:%i:%s')) / 60 END), 1) AS TEMPOPRIMRESP,\n" +
        "  SUM(CASE WHEN C.REABERTURAS > 0 THEN 1 ELSE 0 END) AS REABERTOS\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{mes}}' = '' OR SUBSTRING(C.DTABERTURA,1,7) = '{{mes}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')",

    /* [34] painelIndicadores (SQL)
       Indicadores complementares: aderencia ao SLA, acuracia da triagem (prioridade e categoria), resolucao na primeira interacao e reaberturas. */
    painelIndicadores:
        "SELECT\n" +
        "  ROUND(100 * SUM(CASE WHEN C.STATUS IN ('CONCLUIDO','CANCELADO')\n" +
        "      AND C.DTCONCLUSAO <= C.DTPREVISTA THEN 1 ELSE 0 END)\n" +
        "    / NULLIF(SUM(CASE WHEN C.STATUS IN ('CONCLUIDO','CANCELADO') THEN 1 ELSE 0 END), 0), 1) AS ADERENCIASLA,\n" +
        "  ROUND(100 * SUM(CASE WHEN C.PRIORIDADESUG = C.PRIORIDADE THEN 1 ELSE 0 END)\n" +
        "    / NULLIF(COUNT(*), 0), 1) AS ACURACIAPRIORIDADE,\n" +
        "  ROUND(100 * SUM(CASE WHEN C.CATEGSUG IS NULL OR C.CATEGSUG = C.CODCATEG THEN 1 ELSE 0 END)\n" +
        "    / NULLIF(COUNT(*), 0), 1) AS ACURACIACATEGORIA,\n" +
        "  ROUND(100 * SUM(CASE WHEN C.STATUS = 'CONCLUIDO'\n" +
        "      AND (SELECT COUNT(*) FROM AD_CTBHIST H WHERE H.NUCHAMADO = C.NUCHAMADO\n" +
        "           AND H.TIPO IN ('SOLICINFO','RESPINFO')) = 0 THEN 1 ELSE 0 END)\n" +
        "    / NULLIF(SUM(CASE WHEN C.STATUS = 'CONCLUIDO' THEN 1 ELSE 0 END), 0), 1) AS RESOLUCAOPRIMEIRA,\n" +
        "  SUM(CASE WHEN C.REABERTURAS > 0 THEN 1 ELSE 0 END) AS REABERTOS,\n" +
        "  COUNT(*) AS BASE\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{mes}}' = '' OR SUBSTRING(C.DTABERTURA,1,7) = '{{mes}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')",

    /* [26] painelPorCategoria (SQL)
       Chamados agrupados por categoria. Aceita os filtros de todos os outros graficos (cross-filter). */
    painelPorCategoria:
        "SELECT CAT.NOME AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{mes}}' = '' OR SUBSTRING(C.DTABERTURA,1,7) = '{{mes}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY CAT.NOME\n" +
        "ORDER BY value DESC",

    /* [30] painelPorStatus (SQL)
       Chamados agrupados por status. Aceita os filtros de todos os outros graficos (cross-filter). */
    painelPorStatus:
        "SELECT C.STATUS AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{mes}}' = '' OR SUBSTRING(C.DTABERTURA,1,7) = '{{mes}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "GROUP BY C.STATUS\n" +
        "ORDER BY value DESC",

    /* [31] painelPorMes (SQL)
       Serie temporal de chamados por mes. Aceita os filtros de todos os outros graficos (cross-filter). */
    painelPorMes:
        "SELECT SUBSTRING(C.DTABERTURA, 1, 7) AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY SUBSTRING(C.DTABERTURA, 1, 7)\n" +
        "ORDER BY name",

    /* [27] painelPorPrioridade (SQL)
       Chamados agrupados por prioridade. Aceita os filtros de todos os outros graficos (cross-filter). */
    painelPorPrioridade:
        "SELECT C.PRIORIDADE AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{mes}}' = '' OR SUBSTRING(C.DTABERTURA,1,7) = '{{mes}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY C.PRIORIDADE\n" +
        "ORDER BY value DESC",

    /* [28] painelPorEmpresa (SQL)
       Chamados agrupados por empresa. Aceita os filtros de todos os outros graficos (cross-filter). */
    painelPorEmpresa:
        "SELECT E.NOME AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{mes}}' = '' OR SUBSTRING(C.DTABERTURA,1,7) = '{{mes}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY E.NOME\n" +
        "ORDER BY value DESC",

    /* [29] painelPorResponsavel (SQL)
       Chamados agrupados por responsavel. Aceita os filtros de todos os outros graficos (cross-filter). */
    painelPorResponsavel:
        "SELECT COALESCE(UR.DESCR, 'Nao atribuido') AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{mes}}' = '' OR SUBSTRING(C.DTABERTURA,1,7) = '{{mes}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY COALESCE(UR.DESCR, 'Nao atribuido')\n" +
        "ORDER BY value DESC",

    /* [32] painelPorSubcategoria (SQL)
       Principais assuntos: chamados por subcategoria, com conclusao, atraso e tempo medio. Indicador 10 e 11 da secao 17. */
    painelPorSubcategoria:
        "SELECT SUB.NOME AS SUBCATEGORIA, CAT.NOME AS CATEGORIA, COUNT(*) AS TOTAL,\n" +
        "  SUM(CASE WHEN C.STATUS = 'CONCLUIDO' THEN 1 ELSE 0 END) AS CONCLUIDOS,\n" +
        "  SUM(CASE WHEN C.STATUS NOT IN ('CONCLUIDO','CANCELADO') AND C.DTPREVISTA < DATE_FORMAT(NOW(),'%Y-%m-%dT%H:%i:%s') THEN 1 ELSE 0 END) AS ATRASADOS,\n" +
        "  ROUND(AVG(C.TEMPOATEND), 1) AS TEMPOMEDIO\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{mes}}' = '' OR SUBSTRING(C.DTABERTURA,1,7) = '{{mes}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY SUB.NOME, CAT.NOME\n" +
        "ORDER BY TOTAL DESC\n" +
        "LIMIT 30",

    /* [33] painelPorSolicitante (SQL)
       Chamados por usuario solicitante (indicador 4 da secao 17). */
    painelPorSolicitante:
        "SELECT US.DESCR AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "JOIN INT_USER US ON US.ID = C.CODUSUSOLIC\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{mes}}' = '' OR SUBSTRING(C.DTABERTURA,1,7) = '{{mes}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY US.DESCR\n" +
        "ORDER BY value DESC\n" +
        "LIMIT 15",

    /* [35] drillPorSubcategoria (SQL)
       Drill por subcategoria. Aceita TODOS os filterKeys de drill como parametros opcionais, para nao perder o filtro dos niveis anteriores. */
    drillPorSubcategoria:
        "SELECT SUB.NOME AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY SUB.NOME\n" +
        "ORDER BY value DESC",

    /* [36] drillPorEmpresa (SQL)
       Drill por empresa. Aceita TODOS os filterKeys de drill como parametros opcionais, para nao perder o filtro dos niveis anteriores. */
    drillPorEmpresa:
        "SELECT E.NOME AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY E.NOME\n" +
        "ORDER BY value DESC",

    /* [37] drillPorResponsavel (SQL)
       Drill por responsavel. Aceita TODOS os filterKeys de drill como parametros opcionais, para nao perder o filtro dos niveis anteriores. */
    drillPorResponsavel:
        "SELECT COALESCE(UR.DESCR, 'Nao atribuido') AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY COALESCE(UR.DESCR, 'Nao atribuido')\n" +
        "ORDER BY value DESC",

    /* [38] drillPorPrioridade (SQL)
       Drill por prioridade. Aceita TODOS os filterKeys de drill como parametros opcionais, para nao perder o filtro dos niveis anteriores. */
    drillPorPrioridade:
        "SELECT C.PRIORIDADE AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "GROUP BY C.PRIORIDADE\n" +
        "ORDER BY value DESC",

    /* [39] drillPorStatus (SQL)
       Drill por status. Aceita TODOS os filterKeys de drill como parametros opcionais, para nao perder o filtro dos niveis anteriores. */
    drillPorStatus:
        "SELECT C.STATUS AS name, COUNT(*) AS value\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{subcategoria}}' = '' OR SUB.NOME = '{{subcategoria}}')\n" +
        "  AND ('{{empresa}}' = '' OR E.NOME = '{{empresa}}')\n" +
        "  AND ('{{responsavel}}' = '' OR COALESCE(UR.DESCR,'Nao atribuido') = '{{responsavel}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "GROUP BY C.STATUS\n" +
        "ORDER BY value DESC",

    /* [11] listarMeusChamados (SQL)
       Lista os chamados do usuario logado, com filtros e paginacao. Filtro por usuario aplicado na propria SF. */
    listarMeusChamados:
        "SELECT C.NUCHAMADO, C.NUMCHAMADO, C.DTABERTURA, CAT.NOME AS CATEGORIA, SUB.NOME AS SUBCATEGORIA,\n" +
        "       C.ASSUNTO, C.STATUS, C.PRIORIDADE, E.NOME AS EMPRESA,\n" +
        "       COALESCE(UR.DESCR, 'Nao atribuido') AS RESPONSAVEL, C.DTALTER, C.DTPREVISTA,\n" +
        "  CASE\n" +
        "    WHEN C.STATUS IN ('CONCLUIDO','CANCELADO') THEN 'ENCERRADO'\n" +
        "    WHEN C.STATUS = 'AGUARDANDO INFORMACAO' THEN 'SOLICITANTE'\n" +
        "    WHEN C.STATUS = 'AGUARDANDO APROVACAO' THEN 'APROVADOR'\n" +
        "    WHEN C.CODUSURESP IS NULL THEN 'ATENDIMENTO'\n" +
        "    ELSE 'RESPONSAVEL' END AS AGUARDANDO\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE C.CODUSUSOLIC = :VAR_USER\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{busca}}' = '' OR C.ASSUNTO LIKE '%{{busca}}%' OR C.DESCRICAO LIKE '%{{busca}}%'\n" +
        "       OR C.NUMCHAMADO LIKE '%{{busca}}%')\n" +
        "ORDER BY C.DTALTER DESC\n" +
        "LIMIT {{limite}} OFFSET {{offset}}",

    /* [12] contarMeusChamados (SQL)
       Total de chamados do usuario logado para a paginacao. */
    contarMeusChamados:
        "SELECT COUNT(*) AS TOTAL\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "WHERE C.CODUSUSOLIC = :VAR_USER\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "  AND ('{{categoria}}' = '' OR CAT.NOME = '{{categoria}}')\n" +
        "  AND ('{{dataIni}}' = '' OR C.DTABERTURA >= '{{dataIni}}')\n" +
        "  AND ('{{dataFim}}' = '' OR C.DTABERTURA <= CONCAT('{{dataFim}}','T23:59:59'))\n" +
        "  AND ('{{busca}}' = '' OR C.ASSUNTO LIKE '%{{busca}}%' OR C.DESCRICAO LIKE '%{{busca}}%'\n" +
        "       OR C.NUMCHAMADO LIKE '%{{busca}}%')",

    /* [20] listarChamadosFila (SQL)
       Fila de atendimento. Restrita a membros da equipe contabil (validacao dentro da SF). */
    listarChamadosFila:
        "SELECT C.NUCHAMADO, C.NUMCHAMADO, C.DTABERTURA, CAT.NOME AS CATEGORIA, SUB.NOME AS SUBCATEGORIA,\n" +
        "       C.ASSUNTO, C.STATUS, C.PRIORIDADE, E.NOME AS EMPRESA, US.DESCR AS SOLICITANTE,\n" +
        "       COALESCE(UR.DESCR, 'Nao atribuido') AS RESPONSAVEL, C.CODUSURESP, FI.NOME AS FILA,\n" +
        "       C.DTPREVISTA, C.DTALTER, SUB.EXIGEAPROVACAO,\n" +
        "       CASE WHEN C.STATUS NOT IN ('CONCLUIDO','CANCELADO') AND C.DTPREVISTA < DATE_FORMAT(NOW(),'%Y-%m-%dT%H:%i:%s')\n" +
        "            THEN 1 ELSE 0 END AS ATRASADO,\n" +
        "  CASE\n" +
        "    WHEN C.STATUS IN ('CONCLUIDO','CANCELADO') THEN 'ENCERRADO'\n" +
        "    WHEN C.STATUS = 'AGUARDANDO INFORMACAO' THEN 'SOLICITANTE'\n" +
        "    WHEN C.STATUS = 'AGUARDANDO APROVACAO' THEN 'APROVADOR'\n" +
        "    WHEN C.CODUSURESP IS NULL THEN 'ATENDIMENTO'\n" +
        "    ELSE 'RESPONSAVEL' END AS AGUARDANDO\n" +
        "FROM AD_CTBCHAM C\n" +
        "  JOIN AD_CTBCAT CAT ON CAT.ID = C.CODCATEG\n" +
        "  JOIN AD_CTBSUB SUB ON SUB.ID = C.CODSUBCAT\n" +
        "  JOIN AD_CTBEMP E ON E.ID = C.CODEMP\n" +
        "  LEFT JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "JOIN INT_USER US ON US.ID = C.CODUSUSOLIC\n" +
        "LEFT JOIN AD_CTBFILA FI ON FI.ID = C.CODFILA\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1)\n" +
        "  AND (EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR') OR C.CODUSURESP = :VAR_USER OR C.CODUSUSOLIC = :VAR_USER)\n" +
        "  AND ('{{status}}' = '' OR C.STATUS = '{{status}}')\n" +
        "  AND ('{{prioridade}}' = '' OR C.PRIORIDADE = '{{prioridade}}')\n" +
        "  AND ('{{fila}}' = '' OR FI.NOME = '{{fila}}')\n" +
        "  AND ('{{apenasMeus}}' = 'N' OR C.CODUSURESP = :VAR_USER)\n" +
        "  AND ('{{busca}}' = '' OR C.ASSUNTO LIKE '%{{busca}}%' OR C.NUMCHAMADO LIKE '%{{busca}}%')\n" +
        "ORDER BY FIELD(C.PRIORIDADE,'URGENTE','ALTA','NORMAL','BAIXA'), C.DTABERTURA\n" +
        "LIMIT {{limite}} OFFSET {{offset}}",

    /* [66] listarUsuariosProjeto (SQL)
       Lista as pessoas com acesso ao projeto (agrupadas por e-mail, unificando as varias contas internas de um mesmo usuario) com papel atual, fila, volume de chamados e ultimo acesso. Exclusiva do papel GESTOR (validado dentro da SF). Parametros: busca (vazio = todas), papel (vazio = todos), limite, offset. */
    listarUsuariosProjeto:
        "SELECT U.DESCR AS EMAIL,\n" +
        "       MIN(U.ID) AS CODUSU,\n" +
        "       COUNT(DISTINCT U.ID) AS CONTAS,\n" +
        "       CASE\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'GESTOR'    THEN 1 ELSE 0 END) = 1 THEN 'GESTOR'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'ATENDENTE' THEN 1 ELSE 0 END) = 1 THEN 'ATENDENTE'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'APROVADOR' THEN 1 ELSE 0 END) = 1 THEN 'APROVADOR'\n" +
        "         ELSE 'SOLICITANTE' END AS PAPEL,\n" +
        "       COALESCE(MAX(A.CODFILA), 0) AS CODFILA,\n" +
        "       COALESCE(MAX(F.NOME), '') AS FILA,\n" +
        "       COALESCE(MAX(S.ID), 0) AS CODSETOR,\n" +
        "       COALESCE(MAX(S.NOME), '') AS SETOR,\n" +
        "       (SELECT COUNT(1) FROM AD_CTBCHAM C JOIN INT_USER US ON US.ID = C.CODUSUSOLIC\n" +
        "         WHERE US.DESCR = U.DESCR) AS ABERTOS,\n" +
        "       (SELECT COUNT(1) FROM AD_CTBCHAM C JOIN INT_USER UR ON UR.ID = C.CODUSURESP\n" +
        "         WHERE UR.DESCR = U.DESCR) AS ATRIBUIDOS,\n" +
        "       DATE_FORMAT(MAX(L.ULTIMO), '%Y-%m-%dT%H:%i:%s') AS ULTIMOACESSO,\n" +
        "       MAX(CASE WHEN U.ID = :VAR_USER THEN 1 ELSE 0 END) AS EUMESMO\n" +
        "FROM INT_USER U\n" +
        "LEFT JOIN AD_CTBATEND A ON A.CODUSU = U.ID AND A.ATIVO = 1\n" +
        "LEFT JOIN AD_CTBFILA F ON F.ID = A.CODFILA\n" +
        "LEFT JOIN AD_CTBUSRSETOR US ON US.CODUSU = U.ID\n" +
        "LEFT JOIN AD_CTBSETOR S ON S.ID = US.CODSETOR AND S.ATIVO = 1\n" +
        "LEFT JOIN (SELECT USERID, MAX(INIT) AS ULTIMO FROM INT_USERLOG GROUP BY USERID) L ON L.USERID = U.ID\n" +
        "WHERE U.DESCR LIKE '%@%'\n" +
        "  AND EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  AND ('{{busca}}' = '' OR U.DESCR LIKE '%{{busca}}%')\n" +
        "GROUP BY U.DESCR\n" +
        "HAVING ('{{papel}}' = '' OR CASE\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'GESTOR'    THEN 1 ELSE 0 END) = 1 THEN 'GESTOR'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'ATENDENTE' THEN 1 ELSE 0 END) = 1 THEN 'ATENDENTE'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'APROVADOR' THEN 1 ELSE 0 END) = 1 THEN 'APROVADOR'\n" +
        "         ELSE 'SOLICITANTE' END = '{{papel}}')\n" +
        "ORDER BY CASE CASE\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'GESTOR'    THEN 1 ELSE 0 END) = 1 THEN 'GESTOR'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'ATENDENTE' THEN 1 ELSE 0 END) = 1 THEN 'ATENDENTE'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'APROVADOR' THEN 1 ELSE 0 END) = 1 THEN 'APROVADOR'\n" +
        "         ELSE 'SOLICITANTE' END\n" +
        "           WHEN 'GESTOR' THEN 0 WHEN 'ATENDENTE' THEN 1 WHEN 'APROVADOR' THEN 2 ELSE 3 END,\n" +
        "         U.DESCR\n" +
        "LIMIT {{limite}} OFFSET {{offset}}",

    /* [67] contarUsuariosProjeto (SQL)
       Conta as pessoas que atendem aos filtros da tela de gestao de usuarios, para a paginacao. Exclusiva do papel GESTOR. Parametros: busca, papel. */
    contarUsuariosProjeto:
        "SELECT COUNT(1) AS TOTAL FROM (\n" +
        "  SELECT U.DESCR\n" +
        "  FROM INT_USER U\n" +
        "LEFT JOIN AD_CTBATEND A ON A.CODUSU = U.ID AND A.ATIVO = 1\n" +
        "WHERE U.DESCR LIKE '%@%'\n" +
        "  AND ('{{busca}}' = '' OR U.DESCR LIKE '%{{busca}}%')\n" +
        "GROUP BY U.DESCR\n" +
        "HAVING ('{{papel}}' = '' OR CASE\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'GESTOR'    THEN 1 ELSE 0 END) = 1 THEN 'GESTOR'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'ATENDENTE' THEN 1 ELSE 0 END) = 1 THEN 'ATENDENTE'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'APROVADOR' THEN 1 ELSE 0 END) = 1 THEN 'APROVADOR'\n" +
        "         ELSE 'SOLICITANTE' END = '{{papel}}')\n" +
        "     AND EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        ") T",

    /* [68] resumoPapeisUsuarios (SQL)
       Resumo da quantidade de pessoas por papel, para os indicadores da tela de gestao de usuarios. Exclusiva do papel GESTOR. Sem parametros. */
    resumoPapeisUsuarios:
        "SELECT COUNT(1) AS TOTAL,\n" +
        "       SUM(CASE WHEN T.PAPEL = 'GESTOR'     THEN 1 ELSE 0 END) AS GESTORES,\n" +
        "       SUM(CASE WHEN T.PAPEL = 'ATENDENTE'  THEN 1 ELSE 0 END) AS ATENDENTES,\n" +
        "       SUM(CASE WHEN T.PAPEL = 'APROVADOR'  THEN 1 ELSE 0 END) AS APROVADORES,\n" +
        "       SUM(CASE WHEN T.PAPEL = 'SOLICITANTE' THEN 1 ELSE 0 END) AS SOLICITANTES\n" +
        "FROM (\n" +
        "  SELECT CASE\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'GESTOR'    THEN 1 ELSE 0 END) = 1 THEN 'GESTOR'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'ATENDENTE' THEN 1 ELSE 0 END) = 1 THEN 'ATENDENTE'\n" +
        "         WHEN MAX(CASE WHEN A.PAPEL = 'APROVADOR' THEN 1 ELSE 0 END) = 1 THEN 'APROVADOR'\n" +
        "         ELSE 'SOLICITANTE' END AS PAPEL\n" +
        "  FROM INT_USER U\n" +
        "  LEFT JOIN AD_CTBATEND A ON A.CODUSU = U.ID AND A.ATIVO = 1\n" +
        "  WHERE U.DESCR LIKE '%@%' AND EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1 AND A.PAPEL = 'GESTOR')\n" +
        "  GROUP BY U.DESCR\n" +
        ") T",

    /* [177] listarSetores (SQL)
       Lista os setores ativos aos quais uma pessoa pode pertencer. Sem parametros. */
    listarSetores:
        "SELECT ID, NOME FROM AD_CTBSETOR WHERE ATIVO = 1 ORDER BY NOME",

    /* [223] obterUltimasSyncs (SQL)
       Ultima sincronizacao de cada entidade do Sankhya, para os cards de status. Restrita a equipe contabil. */
    obterUltimasSyncs:
        "SELECT L.ENTIDADE, L.STATUS, L.REGISTROS_IMPORTADOS, L.DURACAO_MS, L.TIPO,\n" +
        "       DATE_FORMAT(L.EXECUTADO_EM, '%Y-%m-%dT%H:%i:%s') AS EXECUTADO_EM,\n" +
        "       TIMESTAMPDIFF(MINUTE, L.EXECUTADO_EM, NOW()) AS MINUTOS_ATRAS, L.MENSAGEM_ERRO\n" +
        "FROM LOG_IMPORTACOES L\n" +
        "JOIN (SELECT ENTIDADE, MAX(ID) AS ULTIMO FROM LOG_IMPORTACOES GROUP BY ENTIDADE) U\n" +
        "  ON U.ULTIMO = L.ID\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1)\n" +
        "ORDER BY L.ENTIDADE",

    /* [224] listarLogsImportacao (SQL)
       Historico de execucoes da importacao do Sankhya, com filtro opcional por entidade. Restrita a equipe contabil. */
    listarLogsImportacao:
        "SELECT ID, ENTIDADE, TIPO, STATUS, REGISTROS_IMPORTADOS, DURACAO_MS,\n" +
        "       DATE_FORMAT(EXECUTADO_EM, '%Y-%m-%dT%H:%i:%s') AS EXECUTADO_EM, MENSAGEM_ERRO, ETAPAS_JSON\n" +
        "FROM LOG_IMPORTACOES\n" +
        "WHERE EXISTS (SELECT 1 FROM AD_CTBATEND A WHERE A.CODUSU = :VAR_USER AND A.ATIVO = 1)\n" +
        "  AND ('{{entidade}}' = '' OR ENTIDADE = '{{entidade}}')\n" +
        "ORDER BY ID DESC\n" +
        "LIMIT 50",

};

/* ────────────────────────────────────────────────────────────────────────────────
   4) CAMADA DE ACESSO A DADOS
   ──────────────────────────────────────────────────────────────────────────────── */

/** Executa uma Server Function e devolve as linhas (colunas em MAIUSCULAS). */
async function chamarSF(serverFunctionId, input) {
    var resp = await fetch(MITRA.BASE_URL + '/interactions/executeServerFunction', {
        method: 'POST',
        headers: { 'Authorization': MITRA.TOKEN, 'Content-Type': 'application/json' },
        body: JSON.stringify({
            projectId: MITRA.PROJECT_ID,
            serverFunctionId: serverFunctionId,
            input: input || {}
        })
    });
    if (!resp.ok) throw new Error('HTTP ' + resp.status + ' ao executar a funcao ' + serverFunctionId);

    var json = await resp.json();
    var r = json && json.result;
    if (!r || r.executionStatus === 'FAILED') {
        throw new Error((r && r.error) || 'Falha ao executar a funcao ' + serverFunctionId);
    }

    var out = r.output;
    if (typeof out === 'string') { try { out = JSON.parse(out); } catch (e) { /* mantem string */ } }
    if (out && Array.isArray(out.result)) return out.result;
    if (Array.isArray(out)) return out;
    return (out && out.rows) || [];
}

/** Primeira linha do resultado, ou objeto vazio. */
async function chamarSFUm(serverFunctionId, input) {
    var linhas = await chamarSF(serverFunctionId, input);
    return linhas[0] || {};
}

/** Escapa aspas simples antes de mandar para uma SF parametrizada por chaves duplas. */
function sanitizar(v) {
    return String(v === null || v === undefined ? '' : v).replace(/'/g, "''").slice(0, 3900);
}

/* ────────────────────────────────────────────────────────────────────────────────
   5) APRESENTACAO — acentuacao, datas, numeros e badges

   O banco da plataforma nao aceita acentuacao no conteudo, entao a acentuacao e
   reaplicada na fronteira com a interface (mesmo criterio de frontend/src/lib/pt.ts).
   ──────────────────────────────────────────────────────────────────────────────── */
var DICIONARIO = {
    'parametrizacao':'parametrização','parametrizacoes':'parametrizações',
    'pendencia':'pendência','pendencias':'pendências','duvida':'dúvida','duvidas':'dúvidas',
    'alteracao':'alteração','alteracoes':'alterações','municipio':'município','municipios':'municípios',
    'contabil':'contábil','contabeis':'contábeis','demonstracao':'demonstração','demonstracoes':'demonstrações',
    'periodo':'período','periodos':'períodos','relatorio':'relatório','relatorios':'relatórios',
    'informacao':'informação','informacoes':'informações','patrimonio':'patrimônio',
    'conciliacao':'conciliação','conciliacoes':'conciliações','divergencia':'divergência','divergencias':'divergências',
    'diferenca':'diferença','diferencas':'diferenças','solicitacao':'solicitação','solicitacoes':'solicitações',
    'numero':'número','codigo':'código','aliquota':'alíquota','retencao':'retenção',
    'servico':'serviço','servicos':'serviços','obrigacao':'obrigação','obrigacoes':'obrigações',
    'descricao':'descrição','situacao':'situação','operacao':'operação','operacoes':'operações',
    'inclusao':'inclusão','exclusao':'exclusão','usuario':'usuário','usuarios':'usuários','modulo':'módulo',
    'balanco':'balanço','razao':'razão','lancamento':'lançamento','lancamentos':'lançamentos',
    'provisao':'provisão','provisoes':'provisões','criterio':'critério','calculo':'cálculo',
    'deposito':'depósito','fisica':'física','fisico':'físico','movimentacao':'movimentação',
    'aquisicao':'aquisição','transferencia':'transferência','reavaliacao':'reavaliação',
    'depreciacao':'depreciação','inventario':'inventário','bancaria':'bancária',
    'titulo':'título','titulos':'títulos','classificacao':'classificação',
    'orientacao':'orientação','orientacoes':'orientações','liberacao':'liberação',
    'devolucao':'devolução','manutencao':'manutenção','apuracao':'apuração',
    'responsavel':'responsável','responsaveis':'responsáveis','analise':'análise',
    'analitico':'analítico','analitica':'analítica','producao':'produção','logistica':'logística',
    'aprovacao':'aprovação','concluido':'concluído','nao':'não','atendimento':'atendimento',
    'media':'média','medio':'médio','ultimo':'último','ultima':'última','proximo':'próximo',
    'cadastro':'cadastro','fiscal':'fiscal','emissao':'emissão','impostos':'impostos'
};

/** Reacentua preservando a caixa original de cada palavra. */
function pt(texto) {
    if (!texto) return '';
    return String(texto).replace(/[A-Za-zÀ-ÿ]+/g, function (palavra) {
        var certa = DICIONARIO[palavra.toLowerCase()];
        if (!certa) return palavra;
        if (palavra === palavra.toUpperCase()) return certa.toUpperCase();
        if (palavra[0] === palavra[0].toUpperCase()) return certa.charAt(0).toUpperCase() + certa.slice(1);
        return certa;
    });
}

/** Escapa HTML — todo dado vindo do banco passa por aqui antes de virar markup. */
function esc(v) {
    if (v === null || v === undefined) return '';
    return String(v).replace(/&/g, '&amp;').replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/** Datas do banco vem como texto ISO sem fuso (AAAA-MM-DDTHH:MM:SS). */
function dataHora(iso) {
    if (!iso) return '—';
    var s = String(iso).replace(' ', 'T');
    var p = s.split('T');
    var d = (p[0] || '').split('-');
    if (d.length !== 3) return String(iso);
    var hora = (p[1] || '').slice(0, 5);
    return d[2] + '/' + d[1] + '/' + d[0] + (hora ? ' ' + hora : '');
}

function dataCurta(iso) {
    if (!iso) return '—';
    var d = String(iso).split('T')[0].split('-');
    if (d.length !== 3) return String(iso);
    return d[2] + '/' + d[1] + '/' + d[0];
}

function numero(v, casas) {
    var n = Number(v);
    if (!isFinite(n)) return '—';
    return n.toLocaleString('pt-BR', {
        minimumFractionDigits: casas || 0, maximumFractionDigits: casas || 0
    });
}

function mesLabel(aaaaMm) {
    var MESES = ['jan','fev','mar','abr','mai','jun','jul','ago','set','out','nov','dez'];
    var p = String(aaaaMm || '').split('-');
    if (p.length < 2) return String(aaaaMm || '');
    return MESES[Number(p[1]) - 1] + '/' + p[0].slice(2);
}

/* Cores de status e prioridade — as mesmas de frontend/src/components/Badges.tsx */
var CORES_STATUS = {
    'NOVO':                  { fundo:'#1e3a5f', texto:'#93c5fd', ponto:'#3b82f6' },
    'EM ANALISE':            { fundo:'#2e1065', texto:'#c4b5fd', ponto:'#8b5cf6' },
    'AGUARDANDO INFORMACAO': { fundo:'#2a2008', texto:'#fcd34d', ponto:'#f59e0b' },
    'EM ATENDIMENTO':        { fundo:'#0c2d3e', texto:'#67e8f9', ponto:'#06b6d4' },
    'AGUARDANDO APROVACAO':  { fundo:'#2d1608', texto:'#fdba74', ponto:'#f97316' },
    'CONCLUIDO':             { fundo:'#0d2818', texto:'#6ee7b7', ponto:'#10b981' },
    'CANCELADO':             { fundo:'#20242f', texto:'#94a3b8', ponto:'#64748b' }
};
var CORES_PRIORIDADE = {
    'URGENTE': { fundo:'#3b0d0d', texto:'#fca5a5' },
    'ALTA':    { fundo:'#2d1608', texto:'#fdba74' },
    'NORMAL':  { fundo:'#1a1a3e', texto:'#a5b4fc' },
    'BAIXA':   { fundo:'#20242f', texto:'#94a3b8' }
};

function badgeStatus(status) {
    var c = CORES_STATUS[status] || CORES_STATUS['CANCELADO'];
    return '<span class="badge" style="background:' + c.fundo + ';color:' + c.texto
         + ';border-color:' + c.ponto + '55;">'
         + '<span class="ponto" style="background:' + c.ponto + ';"></span>' + esc(pt(status)) + '</span>';
}

function badgePrioridade(prioridade) {
    var c = CORES_PRIORIDADE[prioridade] || CORES_PRIORIDADE['NORMAL'];
    return '<span class="badge" style="background:' + c.fundo + ';color:' + c.texto + ';">'
         + esc(prioridade) + '</span>';
}
</script>

<script>
/* ────────────────────────────────────────────────────────────────────────────────
   6) ESTADO DA TELA
   ──────────────────────────────────────────────────────────────────────────────── */
var TAMANHO_PAGINA = 20;
var estado = {
    perfil:      {},                    // meuPerfil: CODUSU, EMAIL, PAPEL
    empresas:    [],
    categorias:  [],
    view:        'painel',
    painel:      { versao: 0, filtro: {} },
    chamados:    { pagina: 0, total: 0 },
    fila:        { pagina: 0, total: 0 },
    usuarios:    { pagina: 0, total: 0 },
    graficos:    {}                     // instancias Chart.js por canvas
};

var STATUS_POSSIVEIS = ['NOVO','EM ANALISE','AGUARDANDO INFORMACAO','EM ATENDIMENTO',
                        'AGUARDANDO APROVACAO','CONCLUIDO','CANCELADO'];
var PRIORIDADES = ['URGENTE','ALTA','NORMAL','BAIXA'];

/* Papeis do sistema — mesma definicao de frontend/src/pages/UsuariosPage.tsx.
   GESTOR e o administrador: unico que enxerga painel, acessos e todos os chamados. */
var PAPEIS = {
    'GESTOR':      { rotulo:'Administrador', cor:'#6366f1', fundo:'var(--kpi-bg-purple)' },
    'ATENDENTE':   { rotulo:'Atendente',     cor:'#06b6d4', fundo:'var(--kpi-bg-cyan)' },
    'APROVADOR':   { rotulo:'Aprovador',     cor:'#f59e0b', fundo:'var(--kpi-bg-amber)' },
    'SOLICITANTE': { rotulo:'Solicitante',   cor:'#94a3b8', fundo:'var(--color-surface-alt)' }
};

var PALETA = ['#60a5fa','#34d399','#fbbf24','#f87171','#a78bfa','#22d3ee','#fb923c','#f472b6'];

function el(id) { return document.getElementById(id); }

function avisar(mensagem, tipo) {
    var alvo = el('aviso-global');
    if (!mensagem) { alvo.innerHTML = ''; return; }
    alvo.innerHTML = '<div class="aviso aviso-' + (tipo || 'erro') + '">' + esc(mensagem) + '</div>';
}

/* ────────────────────────────────────────────────────────────────────────────────
   7) NAVEGACAO ENTRE VIEWS
   ──────────────────────────────────────────────────────────────────────────────── */
function trocarView(nome) {
    estado.view = nome;
    ['painel','chamados','fila','usuarios','integracao'].forEach(function (v) {
        el('view-' + v).classList.toggle('oculto', v !== nome);
    });
    var abas = el('abas').querySelectorAll('.tab');
    for (var i = 0; i < abas.length; i++) {
        abas[i].classList.toggle('ativa', abas[i].getAttribute('data-view') === nome);
    }
    avisar('');
    if (nome === 'chamados'   && !el('tb-chamados').innerHTML) carregarChamados(0);
    if (nome === 'fila'       && !el('tb-fila').innerHTML)     carregarFila(0);
    if (nome === 'usuarios'   && !el('tb-usuarios').innerHTML) carregarUsuarios(0);
    if (nome === 'integracao' && !el('tb-logs').innerHTML)     carregarIntegracao();
}

/* ────────────────────────────────────────────────────────────────────────────────
   8) GRAFICOS — Chart.js com o tema escuro do projeto
   ──────────────────────────────────────────────────────────────────────────────── */
function eixosEscuros() {
    return {
        grid:  { color: 'rgba(148,163,184,.12)', drawBorder: false },
        ticks: { color: '#94a3b8', font: { size: 11, family: 'Inter' } },
        border: { display: false }
    };
}

function desenharGrafico(canvasId, tipo, rotulos, valores, aoClicar) {
    if (typeof Chart === 'undefined') {
        avisoBruto('A biblioteca de graficos nao carregou (CDN provavelmente bloqueado pela '
                 + 'rede). Os numeros continuam corretos; apenas os graficos ficam de fora.');
        return;
    }
    var ctx = el(canvasId);
    if (!ctx) return;
    if (estado.graficos[canvasId]) estado.graficos[canvasId].destroy();

    var ehCircular = (tipo === 'doughnut' || tipo === 'pie');
    var horizontal = (tipo === 'barH');
    var tipoReal   = horizontal ? 'bar' : tipo;

    var cores = ehCircular
        ? rotulos.map(function (_, i) { return PALETA[i % PALETA.length]; })
        : '#60a5fa';

    var config = {
        type: tipoReal,
        data: {
            labels: rotulos,
            datasets: [{
                label: 'Chamados',
                data: valores,
                backgroundColor: cores,
                borderColor: (tipo === 'line') ? '#60a5fa' : 'transparent',
                borderWidth: (tipo === 'line') ? 2 : 0,
                borderRadius: ehCircular ? 0 : 6,
                tension: .35,
                fill: false,
                pointBackgroundColor: '#60a5fa',
                pointRadius: 3
            }]
        },
        options: {
            indexAxis: horizontal ? 'y' : 'x',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: ehCircular,
                    position: 'right',
                    labels: { color: '#94a3b8', font: { size: 11, family: 'Inter' }, boxWidth: 10, padding: 10 }
                },
                tooltip: {
                    backgroundColor: '#1a1d27',
                    borderColor: '#2a2d3a',
                    borderWidth: 1,
                    titleColor: '#f1f5f9',
                    bodyColor: '#94a3b8',
                    padding: 10
                }
            },
            scales: ehCircular ? {} : { x: eixosEscuros(), y: eixosEscuros() },
            onClick: function (evt, elementos) {
                if (!aoClicar || !elementos.length) return;
                aoClicar(rotulos[elementos[0].index]);
            }
        }
    };
    estado.graficos[canvasId] = new Chart(ctx, config);
}

/* ────────────────────────────────────────────────────────────────────────────────
   9) VIEW PAINEL

   Le os filtros da tela e dispara, em paralelo, as dez Server Functions do painel
   (SQL em SQL_SF.painelKpis, painelPorCategoria, ...). Todas exigem papel GESTOR:
   o WHERE de cada uma tem EXISTS sobre AD_CTBATEND com :VAR_USER.
   ──────────────────────────────────────────────────────────────────────────────── */
function filtrosPainel() {
    return {
        dataIni:     el('f-data-ini').value || '',
        dataFim:     el('f-data-fim').value || '',
        status:      el('f-status').value || '',
        prioridade:  el('f-prioridade').value || '',
        empresa:     sanitizar(el('f-empresa').value || ''),
        categoria:   sanitizar(estado.painel.filtro.categoria || ''),
        subcategoria:'',
        responsavel: '',
        mes:         ''
    };
}

function limparFiltrosPainel() {
    el('f-data-ini').value = '';
    el('f-data-fim').value = '';
    el('f-status').value = '';
    el('f-prioridade').value = '';
    el('f-empresa').value = '';
    estado.painel.filtro = {};
    aplicarPainel();
}

async function aplicarPainel() {
    var v = ++estado.painel.versao;
    var f = filtrosPainel();

    var r = await Promise.allSettled([
        chamarSFUm(SF.painelKpis, f),
        chamarSFUm(SF.painelIndicadores, f),
        chamarSF(SF.painelPorCategoria, f),
        chamarSF(SF.painelPorStatus, f),
        chamarSF(SF.painelPorMes, f),
        chamarSF(SF.painelPorPrioridade, f),
        chamarSF(SF.painelPorEmpresa, f),
        chamarSF(SF.painelPorResponsavel, f),
        chamarSF(SF.painelPorSubcategoria, f),
        chamarSF(SF.painelPorSolicitante, f)
    ]);
    if (estado.painel.versao !== v) return;   // resultado obsoleto: chegou filtro mais novo

    if (r[0].status === 'rejected') {
        avisar('Nao foi possivel carregar o painel. Ele e restrito ao administrador (papel GESTOR). '
             + 'Detalhe: ' + r[0].reason.message, 'erro');
        return;
    }
    var ok = function (i) { return r[i].status === 'fulfilled' ? r[i].value : (i < 2 ? {} : []); };

    renderKpis(ok(0), ok(1));
    renderIndicadores(ok(1));

    var cat = ok(2).map(function (l) { return { nome: pt(l.name), bruto: l.name, valor: Number(l.value) }; });
    desenharGrafico('ch-categoria', 'bar',
        cat.map(function (x) { return x.nome; }),
        cat.map(function (x) { return x.valor; }),
        function (rotulo) {
            var achado = cat.filter(function (x) { return x.nome === rotulo; })[0];
            estado.painel.filtro.categoria =
                (estado.painel.filtro.categoria === (achado && achado.bruto)) ? '' : (achado && achado.bruto);
            aplicarPainel();
        });

    var st = ok(3);
    desenharGrafico('ch-status', 'doughnut',
        st.map(function (l) { return pt(l.name); }),
        st.map(function (l) { return Number(l.value); }));

    var ms = ok(4);
    desenharGrafico('ch-mes', 'line',
        ms.map(function (l) { return mesLabel(l.name); }),
        ms.map(function (l) { return Number(l.value); }));

    var pr = ok(5);
    desenharGrafico('ch-prioridade', 'bar',
        pr.map(function (l) { return l.name; }),
        pr.map(function (l) { return Number(l.value); }));

    var em = ok(6);
    desenharGrafico('ch-empresa', 'barH',
        em.map(function (l) { return l.name; }),
        em.map(function (l) { return Number(l.value); }));

    var re = ok(7);
    desenharGrafico('ch-responsavel', 'barH',
        re.map(function (l) { return pt(l.name); }),
        re.map(function (l) { return Number(l.value); }));

    renderSubcategorias(ok(8));
    renderSolicitantes(ok(9));

    var ativo = estado.painel.filtro.categoria;
    avisar(ativo ? 'Filtrando pela categoria "' + pt(ativo) + '". Clique na mesma barra para limpar.' : '', 'info');
}

function cartaoKpi(rotulo, valor, nota, cor, fundo, simbolo) {
    return '<div class="superficie kpi">'
         + '<div class="kpi-topo">'
         + '<span class="kpi-icone" style="background:' + fundo + ';color:' + cor + ';">' + simbolo + '</span>'
         + '<span class="kpi-rotulo">' + esc(rotulo) + '</span>'
         + '</div>'
         + '<p class="kpi-valor" style="color:' + cor + ';">' + valor + '</p>'
         + (nota ? '<p class="kpi-nota">' + esc(nota) + '</p>' : '')
         + '</div>';
}

function renderKpis(k) {
    var total = Number(k.TOTAL || 0);
    var concluidos = Number(k.CONCLUIDOS || 0);
    var taxa = total ? Math.round(100 * concluidos / total) : 0;
    el('kpis').innerHTML =
          cartaoKpi('Total de chamados', numero(total), 'no periodo filtrado',
                    '#60a5fa', 'var(--kpi-bg-blue)', '#')
        + cartaoKpi('Em aberto', numero(k.ABERTOS), 'ainda em atendimento',
                    '#fbbf24', 'var(--kpi-bg-amber)', '!')
        + cartaoKpi('Atrasados', numero(k.ATRASADOS), 'passaram do prazo previsto',
                    '#f87171', 'var(--kpi-bg-purple)', '&#9888;')
        + cartaoKpi('Concluidos', numero(concluidos), taxa + '% do total',
                    '#34d399', 'var(--kpi-bg-green)', '&#10003;')
        + cartaoKpi('Tempo medio', k.TEMPOMEDIO ? numero(k.TEMPOMEDIO, 1) + 'h' : '—',
                    'do inicio ao encerramento', '#22d3ee', 'var(--kpi-bg-cyan)', '&#8987;')
        + cartaoKpi('1a resposta', k.TEMPOPRIMRESP ? numero(k.TEMPOPRIMRESP, 1) + 'h' : '—',
                    'ate o primeiro retorno', '#a78bfa', 'var(--kpi-bg-purple)', '&#8618;');
}

function renderIndicadores(ind) {
    var pct = function (v) { return (v === null || v === undefined) ? '—' : numero(v, 1) + '%'; };
    el('indicadores').innerHTML =
          cartaoKpi('Aderencia ao SLA', pct(ind.ADERENCIASLA), 'encerrados dentro do prazo',
                    '#34d399', 'var(--kpi-bg-green)', '&#9201;')
        + cartaoKpi('Acuracia da prioridade', pct(ind.ACURACIAPRIORIDADE), 'sugerida x aplicada',
                    '#60a5fa', 'var(--kpi-bg-blue)', '&#8982;')
        + cartaoKpi('Acuracia da categoria', pct(ind.ACURACIACATEGORIA), 'classificacao mantida',
                    '#22d3ee', 'var(--kpi-bg-cyan)', '&#8982;')
        + cartaoKpi('Resolucao na 1a', pct(ind.RESOLUCAOPRIMEIRA), 'sem pedir informacao extra',
                    '#a78bfa', 'var(--kpi-bg-purple)', '&#10004;')
        + cartaoKpi('Reaberturas', numero(ind.REABERTOS), 'chamados reabertos',
                    '#f87171', 'var(--kpi-bg-amber)', '&#8635;');
}

function renderSubcategorias(linhas) {
    if (!linhas.length) { el('tb-subcategoria').innerHTML = '<tr><td colspan="6" class="vazio">Sem dados no periodo.</td></tr>'; return; }
    el('tb-subcategoria').innerHTML = linhas.map(function (l) {
        return '<tr>'
             + '<td>' + esc(pt(l.SUBCATEGORIA)) + '</td>'
             + '<td style="color:var(--color-text-secondary);">' + esc(pt(l.CATEGORIA)) + '</td>'
             + '<td class="num">' + numero(l.TOTAL) + '</td>'
             + '<td class="num" style="color:var(--ok);">' + numero(l.CONCLUIDOS) + '</td>'
             + '<td class="num" style="color:' + (Number(l.ATRASADOS) ? 'var(--danger)' : 'var(--color-text-secondary)') + ';">'
             + numero(l.ATRASADOS) + '</td>'
             + '<td class="num">' + (l.TEMPOMEDIO ? numero(l.TEMPOMEDIO, 1) : '—') + '</td>'
             + '</tr>';
    }).join('');
}

function renderSolicitantes(linhas) {
    if (!linhas.length) { el('tb-solicitante').innerHTML = '<tr><td colspan="2" class="vazio">Sem dados no periodo.</td></tr>'; return; }
    el('tb-solicitante').innerHTML = linhas.map(function (l) {
        return '<tr><td>' + esc(l.name) + '</td><td class="num">' + numero(l.value) + '</td></tr>';
    }).join('');
}
</script>

<script>
/* ────────────────────────────────────────────────────────────────────────────────
   10) VIEW CHAMADOS — os chamados abertos pelo portador do token

   SQL_SF.listarMeusChamados filtra por C.CODUSUSOLIC = :VAR_USER. Ou seja: esta
   aba mostra o que o dono do token abriu. Para ver a operacao inteira, use a Fila
   (SQL_SF.listarChamadosFila libera tudo para quem tem papel GESTOR).
   ──────────────────────────────────────────────────────────────────────────────── */
function filtrosChamados() {
    return {
        busca:     sanitizar(el('c-busca').value || ''),
        status:    el('c-status').value || '',
        categoria: sanitizar(el('c-categoria').value || ''),
        dataIni:   el('c-data-ini').value || '',
        dataFim:   el('c-data-fim').value || ''
    };
}

function limparFiltrosChamados() {
    el('c-busca').value = ''; el('c-status').value = '';
    el('c-categoria').value = ''; el('c-data-ini').value = ''; el('c-data-fim').value = '';
    carregarChamados(0);
}

async function carregarChamados(pagina) {
    estado.chamados.pagina = pagina;
    el('tb-chamados').innerHTML = '<tr><td colspan="8" class="vazio">Carregando...</td></tr>';
    var f = filtrosChamados();
    try {
        var res = await Promise.all([
            chamarSF(SF.listarMeusChamados, Object.assign({}, f, {
                limite: TAMANHO_PAGINA, offset: pagina * TAMANHO_PAGINA
            })),
            chamarSFUm(SF.contarMeusChamados, f)
        ]);
        var linhas = res[0];
        estado.chamados.total = Number(res[1].TOTAL || 0);

        if (!linhas.length) {
            el('tb-chamados').innerHTML = '<tr><td colspan="8" class="vazio">Nenhum chamado encontrado.</td></tr>';
        } else {
            el('tb-chamados').innerHTML = linhas.map(function (c) {
                return '<tr class="clicavel" onclick="abrirChamado(' + Number(c.NUCHAMADO) + ')">'
                     + '<td style="font-weight:600;color:var(--color-primary);">' + esc(c.NUMCHAMADO) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + dataHora(c.DTABERTURA) + '</td>'
                     + '<td>' + esc(pt(c.ASSUNTO)) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(pt(c.CATEGORIA)) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(c.EMPRESA) + '</td>'
                     + '<td>' + badgeStatus(c.STATUS) + '</td>'
                     + '<td>' + badgePrioridade(c.PRIORIDADE) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(pt(c.RESPONSAVEL)) + '</td>'
                     + '</tr>';
            }).join('');
        }
        atualizarPaginacao('c', estado.chamados);
    } catch (e) {
        el('tb-chamados').innerHTML = '<tr><td colspan="8" class="vazio">' + esc(e.message) + '</td></tr>';
    }
}

function paginaChamados(passo) {
    var nova = estado.chamados.pagina + passo;
    if (nova < 0 || nova * TAMANHO_PAGINA >= estado.chamados.total) return;
    carregarChamados(nova);
}

/* ────────────────────────────────────────────────────────────────────────────────
   11) VIEW FILA — atendimento de toda a operacao

   SQL_SF.listarChamadosFila exige vinculo ativo em AD_CTBATEND e, para enxergar
   chamados de terceiros, papel GESTOR. Ordena por urgencia e data de abertura.
   ──────────────────────────────────────────────────────────────────────────────── */
function filtrosFila() {
    return {
        busca:      sanitizar(el('q-busca').value || ''),
        status:     el('q-status').value || '',
        prioridade: el('q-prioridade').value || '',
        fila:       '',
        apenasMeus: el('q-meus').value || 'N'
    };
}

function limparFiltrosFila() {
    el('q-busca').value = ''; el('q-status').value = '';
    el('q-prioridade').value = ''; el('q-meus').value = 'N';
    carregarFila(0);
}

async function carregarFila(pagina) {
    estado.fila.pagina = pagina;
    el('tb-fila').innerHTML = '<tr><td colspan="9" class="vazio">Carregando...</td></tr>';
    var f = filtrosFila();
    try {
        var linhas = await chamarSF(SF.listarChamadosFila, Object.assign({}, f, {
            limite: TAMANHO_PAGINA + 1, offset: pagina * TAMANHO_PAGINA
        }));
        // Uma linha a mais so para saber se existe proxima pagina (a SF nao tem contador proprio).
        var temProxima = linhas.length > TAMANHO_PAGINA;
        if (temProxima) linhas = linhas.slice(0, TAMANHO_PAGINA);
        estado.fila.total = pagina * TAMANHO_PAGINA + linhas.length + (temProxima ? 1 : 0);

        if (!linhas.length) {
            el('tb-fila').innerHTML = '<tr><td colspan="9" class="vazio">Nenhum chamado na fila.</td></tr>';
        } else {
            el('tb-fila').innerHTML = linhas.map(function (c) {
                var atrasado = Number(c.ATRASADO) === 1;
                return '<tr class="clicavel" onclick="abrirChamado(' + Number(c.NUCHAMADO) + ')">'
                     + '<td style="font-weight:600;color:var(--color-primary);">' + esc(c.NUMCHAMADO) + '</td>'
                     + '<td>' + badgePrioridade(c.PRIORIDADE) + '</td>'
                     + '<td>' + esc(pt(c.ASSUNTO)) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(c.SOLICITANTE) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(c.EMPRESA) + '</td>'
                     + '<td>' + badgeStatus(c.STATUS) + '</td>'
                     + '<td style="color:var(--color-text-secondary);font-size:.72rem;">' + esc(pt(c.AGUARDANDO)) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(pt(c.RESPONSAVEL)) + '</td>'
                     + '<td style="color:' + (atrasado ? 'var(--danger)' : 'var(--color-text-secondary)') + ';">'
                     + dataCurta(c.DTPREVISTA) + (atrasado ? ' (atrasado)' : '') + '</td>'
                     + '</tr>';
            }).join('');
        }
        el('q-info').textContent = 'Pagina ' + (pagina + 1) + ' — ' + linhas.length + ' chamado(s) nesta pagina';
        el('q-ant').disabled = pagina === 0;
        el('q-prox').disabled = !temProxima;
    } catch (e) {
        el('tb-fila').innerHTML = '<tr><td colspan="9" class="vazio">' + esc(e.message) + '</td></tr>';
    }
}

function paginaFila(passo) {
    var nova = estado.fila.pagina + passo;
    if (nova < 0) return;
    carregarFila(nova);
}

/* ────────────────────────────────────────────────────────────────────────────────
   12) VIEW USUARIOS — quem tem qual papel

   SQL_SF.listarUsuariosProjeto agrupa por e-mail: a mesma pessoa tem uma conta em
   INT_USER por origem de login, e o papel exibido e o mais alto entre as contas.
   Consulta restrita a GESTOR.
   ──────────────────────────────────────────────────────────────────────────────── */
function limparFiltrosUsuarios() {
    el('u-busca').value = ''; el('u-papel').value = '';
    carregarUsuarios(0);
}

async function carregarUsuarios(pagina) {
    estado.usuarios.pagina = pagina;
    el('tb-usuarios').innerHTML = '<tr><td colspan="8" class="vazio">Carregando...</td></tr>';
    var f = { busca: sanitizar(el('u-busca').value || ''), papel: el('u-papel').value || '' };
    try {
        var res = await Promise.all([
            chamarSF(SF.listarUsuariosProjeto, Object.assign({}, f, {
                limite: TAMANHO_PAGINA, offset: pagina * TAMANHO_PAGINA
            })),
            chamarSFUm(SF.contarUsuariosProjeto, f),
            chamarSFUm(SF.resumoPapeisUsuarios, {})
        ]);
        var linhas = res[0];
        estado.usuarios.total = Number(res[1].TOTAL || 0);
        var resumo = res[2];

        el('u-kpis').innerHTML =
              cartaoKpi('Administradores', numero(resumo.GESTORES), 'enxergam tudo',
                        '#6366f1', 'var(--kpi-bg-purple)', '&#9733;')
            + cartaoKpi('Atendentes', numero(resumo.ATENDENTES), 'atendem os proprios chamados',
                        '#06b6d4', 'var(--kpi-bg-cyan)', '&#9993;')
            + cartaoKpi('Aprovadores', numero(resumo.APROVADORES), 'aprovam etapas',
                        '#f59e0b', 'var(--kpi-bg-amber)', '&#10003;')
            + cartaoKpi('Solicitantes', numero(resumo.SOLICITANTES), 'abrem e acompanham',
                        '#94a3b8', 'var(--color-surface-alt)', '&#9679;');

        if (!linhas.length) {
            el('tb-usuarios').innerHTML = '<tr><td colspan="8" class="vazio">Nenhum usuario encontrado.</td></tr>';
        } else {
            el('tb-usuarios').innerHTML = linhas.map(function (u) {
                var p = PAPEIS[u.PAPEL] || PAPEIS['SOLICITANTE'];
                var eu = Number(u.EUMESMO) === 1;
                return '<tr>'
                     + '<td>' + esc(u.EMAIL) + (eu ? ' <span style="color:var(--color-primary);font-size:.7rem;">(voce)</span>' : '') + '</td>'
                     + '<td><span class="badge" style="background:' + p.fundo + ';color:' + p.cor + ';">'
                     + esc(p.rotulo) + '</span></td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(pt(u.SETOR) || '—') + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(pt(u.FILA) || '—') + '</td>'
                     + '<td class="num">' + numero(u.CONTAS) + '</td>'
                     + '<td class="num">' + numero(u.ABERTOS) + '</td>'
                     + '<td class="num">' + numero(u.ATRIBUIDOS) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + dataHora(u.ULTIMOACESSO) + '</td>'
                     + '</tr>';
            }).join('');
        }
        atualizarPaginacao('u', estado.usuarios);
    } catch (e) {
        el('tb-usuarios').innerHTML = '<tr><td colspan="8" class="vazio">' + esc(e.message) + '</td></tr>';
    }
}

function paginaUsuarios(passo) {
    var nova = estado.usuarios.pagina + passo;
    if (nova < 0 || nova * TAMANHO_PAGINA >= estado.usuarios.total) return;
    carregarUsuarios(nova);
}

/* ────────────────────────────────────────────────────────────────────────────────
   13) VIEW INTEGRACAO — estado da importacao vinda do Sankhya
   ──────────────────────────────────────────────────────────────────────────────── */
async function carregarIntegracao() {
    el('tb-logs').innerHTML = '<tr><td colspan="8" class="vazio">Carregando...</td></tr>';
    try {
        var res = await Promise.all([
            chamarSF(SF.obterUltimasSyncs, {}),
            chamarSF(SF.listarLogsImportacao, { entidade: '' })
        ]);
        var syncs = res[0], logs = res[1];

        el('i-entidades').innerHTML = syncs.length ? syncs.map(function (s) {
            var ok = String(s.STATUS || '').toUpperCase() === 'SUCESSO'
                  || String(s.STATUS || '').toUpperCase() === 'SUCCESS';
            var min = Number(s.MINUTOS_ATRAS || 0);
            var quando = min < 60 ? min + ' min atras'
                       : (min < 1440 ? Math.floor(min / 60) + 'h atras' : Math.floor(min / 1440) + 'd atras');
            return cartaoKpi(pt(s.ENTIDADE), numero(s.REGISTROS_IMPORTADOS), quando,
                             ok ? '#34d399' : '#f87171',
                             ok ? 'var(--kpi-bg-green)' : 'var(--kpi-bg-amber)',
                             ok ? '&#10003;' : '&#9888;');
        }).join('') : '<div class="superficie kpi"><span class="kpi-rotulo">Nenhuma importacao registrada.</span></div>';

        if (!logs.length) {
            el('tb-logs').innerHTML = '<tr><td colspan="8" class="vazio">Sem historico de importacao.</td></tr>';
        } else {
            el('tb-logs').innerHTML = logs.map(function (l) {
                var ok = String(l.STATUS || '').toUpperCase().indexOf('SUCES') === 0
                      || String(l.STATUS || '').toUpperCase() === 'SUCCESS';
                return '<tr>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(l.ID) + '</td>'
                     + '<td>' + esc(pt(l.ENTIDADE)) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(pt(l.TIPO)) + '</td>'
                     + '<td><span class="badge" style="background:' + (ok ? '#0d2818' : '#3b0d0d')
                     + ';color:' + (ok ? '#6ee7b7' : '#fca5a5') + ';">' + esc(l.STATUS) + '</span></td>'
                     + '<td class="num">' + numero(l.REGISTROS_IMPORTADOS) + '</td>'
                     + '<td class="num">' + (l.DURACAO_MS ? numero(Number(l.DURACAO_MS) / 1000, 1) + 's' : '—') + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + dataHora(l.EXECUTADO_EM) + '</td>'
                     + '<td style="color:var(--danger);font-size:.72rem;">' + esc(l.MENSAGEM_ERRO || '') + '</td>'
                     + '</tr>';
            }).join('');
        }
    } catch (e) {
        el('tb-logs').innerHTML = '<tr><td colspan="8" class="vazio">' + esc(e.message) + '</td></tr>';
    }
}

/* ────────────────────────────────────────────────────────────────────────────────
   14) MODAL DE DETALHE — cabecalho, campos, linha do tempo e anexos
   ──────────────────────────────────────────────────────────────────────────────── */
function fecharModal() { el('modal-chamado').style.display = 'none'; }

async function abrirChamado(nuChamado) {
    el('modal-chamado').style.display = 'flex';
    el('m-titulo').textContent = 'Chamado';
    el('m-sub').textContent = 'Carregando...';
    el('m-corpo').innerHTML = '<div class="carregando">Carregando chamado...</div>';

    var p = { numChamado: String(nuChamado) };
    try {
        var res = await Promise.allSettled([
            chamarSFUm(SF.detalharChamado, p),
            chamarSF(SF.listarDetalhesChamado, p),
            chamarSF(SF.listarHistoricoChamado, p),
            chamarSF(SF.listarAnexosChamado, p)
        ]);
        var c = res[0].status === 'fulfilled' ? res[0].value : {};
        if (!c.NUCHAMADO) {
            el('m-corpo').innerHTML = '<div class="vazio">Chamado nao encontrado ou sem permissao de leitura.</div>';
            return;
        }
        var campos    = res[1].status === 'fulfilled' ? res[1].value : [];
        var historico = res[2].status === 'fulfilled' ? res[2].value : [];
        var anexos    = res[3].status === 'fulfilled' ? res[3].value : [];

        el('m-titulo').innerHTML = esc(c.NUMCHAMADO) + ' &nbsp; ' + badgeStatus(c.STATUS)
                                 + ' ' + badgePrioridade(c.PRIORIDADE);
        el('m-sub').textContent = pt(c.ASSUNTO || '');

        var html = '<div class="grid-info">'
            + infoItem('Solicitante', c.SOLICITANTE)
            + infoItem('Responsavel', pt(c.RESPONSAVEL))
            + infoItem('Empresa', c.EMPRESA)
            + infoItem('Categoria', pt(c.CATEGORIA) + ' / ' + pt(c.SUBCATEGORIA))
            + infoItem('Aberto em', dataHora(c.DTABERTURA))
            + infoItem('Prazo previsto', dataHora(c.DTPREVISTA))
            + infoItem('1a resposta', dataHora(c.DTPRIMRESP))
            + infoItem('Concluido em', dataHora(c.DTCONCLUSAO))
            + infoItem('Fila', pt(c.FILA) || '—')
            + infoItem('Origem', pt(c.ORIGEM) || '—')
            + infoItem('Reaberturas', numero(c.REABERTURAS))
            + infoItem('Tempo de atendimento', c.TEMPOATEND ? numero(c.TEMPOATEND, 1) + 'h' : '—')
            + '</div>';

        html += '<p class="secao-titulo">Descricao</p>'
             +  '<div class="info-item"><div class="v" style="white-space:pre-wrap;">'
             +  esc(pt(c.DESCRICAO || '')) + '</div></div>';

        if (c.SOLUCAO) {
            html += '<p class="secao-titulo">Solucao</p>'
                 +  '<div class="info-item"><div class="v" style="white-space:pre-wrap;">'
                 +  esc(pt(c.SOLUCAO)) + '</div></div>';
        }
        if (c.CANCMOTIVO) {
            html += '<p class="secao-titulo">Motivo do cancelamento</p>'
                 +  '<div class="info-item"><div class="v">' + esc(pt(c.CANCMOTIVO)) + '</div></div>';
        }

        if (campos.length) {
            html += '<p class="secao-titulo">Campos da solicitacao</p><div class="grid-info">'
                 + campos.map(function (d) { return infoItem(pt(d.ROTULO || d.CAMPO), pt(d.VALOR)); }).join('')
                 + '</div>';
        }

        if (historico.length) {
            html += '<p class="secao-titulo">Linha do tempo</p><div class="linha-tempo">'
                 + historico.map(function (h) {
                     return '<div class="evento">'
                          + '<div class="quando">' + dataHora(h.DHEVENTO) + ' — ' + esc(h.AUTOR || 'sistema')
                          + ' &middot; ' + esc(pt(h.TIPO)) + '</div>'
                          + '<div class="oque">' + esc(pt(h.DESCRICAO || '')) + '</div>'
                          + '</div>';
                   }).join('')
                 + '</div>';
        }

        if (anexos.length) {
            html += '<p class="secao-titulo">Anexos</p><div class="grid-info">'
                 + anexos.map(function (a) {
                     return infoItem(a.NOMEARQ, numero(Number(a.TAMANHO || 0) / 1024, 1) + ' KB — '
                                              + dataHora(a.DHUPLOAD));
                   }).join('')
                 + '</div>';
        }

        el('m-corpo').innerHTML = html;
    } catch (e) {
        el('m-corpo').innerHTML = '<div class="vazio">' + esc(e.message) + '</div>';
    }
}

function infoItem(rotulo, valor) {
    return '<div class="info-item"><div class="r">' + esc(rotulo) + '</div>'
         + '<div class="v">' + esc(valor === null || valor === undefined || valor === '' ? '—' : valor)
         + '</div></div>';
}

/* ────────────────────────────────────────────────────────────────────────────────
   15) UTILITARIOS E PARTIDA
   ──────────────────────────────────────────────────────────────────────────────── */
function atualizarPaginacao(prefixo, ctx) {
    var paginas = Math.max(1, Math.ceil(ctx.total / TAMANHO_PAGINA));
    el(prefixo + '-info').textContent = numero(ctx.total) + ' registro(s) — pagina '
                                      + (ctx.pagina + 1) + ' de ' + paginas;
    el(prefixo + '-ant').disabled  = ctx.pagina === 0;
    el(prefixo + '-prox').disabled = (ctx.pagina + 1) >= paginas;
}

function preencherSelect(id, valores, rotulos) {
    var alvo = el(id);
    if (!alvo) return;
    var primeira = alvo.options[0] ? alvo.options[0].outerHTML : '';
    alvo.innerHTML = primeira + valores.map(function (v, i) {
        return '<option value="' + esc(v) + '">' + esc(rotulos ? rotulos[i] : pt(v)) + '</option>';
    }).join('');
}

function recarregarTudo() {
    estado.painel.filtro = {};
    aplicarPainel();
    if (el('tb-chamados').innerHTML) carregarChamados(estado.chamados.pagina);
    if (el('tb-fila').innerHTML)     carregarFila(estado.fila.pagina);
    if (el('tb-usuarios').innerHTML) carregarUsuarios(estado.usuarios.pagina);
    if (el('tb-logs').innerHTML)     carregarIntegracao();
}

document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') fecharModal();
});

async function iniciar() {
    // Selects que nao dependem do banco
    preencherSelect('f-status', STATUS_POSSIVEIS);
    preencherSelect('c-status', STATUS_POSSIVEIS);
    preencherSelect('q-status', STATUS_POSSIVEIS);
    preencherSelect('f-prioridade', PRIORIDADES);
    preencherSelect('q-prioridade', PRIORIDADES);

    // Identidade do portador do token e listas de apoio
    try {
        var res = await Promise.allSettled([
            chamarSFUm(SF.meuPerfil, {}),
            chamarSF(SF.listarEmpresas, {}),
            chamarSF(SF.listarTaxonomia, {})
        ]);

        if (res[0].status === 'fulfilled') {
            estado.perfil = res[0].value;
            el('cab-usuario').textContent = estado.perfil.EMAIL || 'sem identificacao';
            var p = PAPEIS[estado.perfil.PAPEL] || PAPEIS['SOLICITANTE'];
            el('cab-papel').innerHTML = '&middot; <span style="color:' + p.cor + ';">' + p.rotulo + '</span>';
        } else {
            el('cab-usuario').textContent = 'falha na autenticacao';
            avisar('Nao foi possivel autenticar na plataforma. Verifique o token no bloco MITRA '
                 + 'no topo do script. Detalhe: ' + res[0].reason.message, 'erro');
            return;
        }

        if (res[1].status === 'fulfilled') {
            estado.empresas = res[1].value;
            preencherSelect('f-empresa', estado.empresas.map(function (e) { return e.NOME; }));
        }

        if (res[2].status === 'fulfilled') {
            var vistas = {};
            res[2].value.forEach(function (l) { if (l.CATNOME) vistas[l.CATNOME] = true; });
            estado.categorias = Object.keys(vistas);
            preencherSelect('c-categoria', estado.categorias);
        }

        await aplicarPainel();
    } catch (e) {
        avisar('Falha ao iniciar a tela: ' + e.message, 'erro');
    }
}

iniciar();
</script>

</body>
</html>