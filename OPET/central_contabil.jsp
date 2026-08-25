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
    <title>Central de Atendimento Contábil</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
    <snk:load />
    <style>
        /* ============================================================
           TEMA CLARO — fiel ao design do Mitralab (prod)
           Paleta indigo / azul com superficies brancas
           ============================================================ */
        :root {
            --color-primary: #4f46e5;
            --color-primary-hover: #4338ca;
            --color-primary-light: #818cf8;
            --color-primary-bg: #eef2ff;
            --color-bg: #eef1f5;
            --color-surface: #ffffff;
            --color-surface-alt: #f8fafc;
            --color-nav: #ffffff;
            --color-nav-text: #64748b;
            --color-nav-active: #4f46e5;
            --color-nav-hover: #f1f5f9;
            --color-border: #e2e8f0;
            --color-text: #0f172a;
            --color-text-secondary: #64748b;
            --ok: #10b981;
            --warn: #f59e0b;
            --danger: #ef4444;
            --shadow-card: 0 1px 3px rgba(0,0,0,.06), 0 1px 2px rgba(0,0,0,.04);
            --shadow-lg: 0 10px 25px rgba(0,0,0,.08);
            /* KPI backgrounds */
            --kpi-bg-green: #ecfdf5;
            --kpi-bg-blue: #eff6ff;
            --kpi-bg-cyan: #ecfeff;
            --kpi-bg-amber: #fffbeb;
            --kpi-bg-purple: #f5f3ff;
            --kpi-bg-rose: #fff1f2;
            --kpi-bg-indigo: #eef2ff;
        }

        * { box-sizing: border-box; margin: 0; padding: 0; }

        html, body {
            min-height: 100vh;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: var(--color-bg);
            color: var(--color-text);
            font-size: 14px;
            -webkit-font-smoothing: antialiased;
        }

        /* ── Fundo decorativo (balanca) ── */
        .fundo-decorativo {
            position: fixed; inset: 0; z-index: -1;
            pointer-events: none; opacity: .04;
            display: flex; align-items: center; justify-content: center;
            color: var(--color-text);
        }
        .fundo-decorativo svg { width: min(62vw, 780px); height: auto; }

        /* ── Menu flutuante (pill bar) ── */
        .menu-flutuante {
            position: fixed; top: 0; left: 50%; transform: translateX(-50%);
            z-index: 50; display: flex; align-items: center; gap: 2px;
            background: rgba(255,255,255,.88);
            backdrop-filter: blur(14px); -webkit-backdrop-filter: blur(14px);
            border: 1px solid var(--color-border);
            border-radius: 9999px;
            padding: 6px 8px;
            margin-top: 12px;
            box-shadow: var(--shadow-card);
        }
        .menu-flutuante .brand-pill {
            display: flex; align-items: center; gap: 6px;
            padding: 6px 14px; font-weight: 700; font-size: .82rem;
            color: var(--color-text); cursor: default;
        }
        .menu-flutuante .brand-pill svg { width: 18px; height: 18px; }
        .nav-btn {
            display: inline-flex; align-items: center; gap: 5px;
            padding: 7px 14px; border-radius: 9999px;
            font-family: inherit; font-size: .8rem; font-weight: 500;
            border: none; background: transparent; cursor: pointer;
            color: var(--color-nav-text); transition: all .2s;
            white-space: nowrap;
        }
        .nav-btn:hover { background: var(--color-nav-hover); color: var(--color-text); }
        .nav-btn.ativa { background: var(--color-primary-bg); color: var(--color-nav-active); font-weight: 600; }
        .nav-btn svg { width: 16px; height: 16px; flex-shrink: 0; }
        .nav-config {
            width: 32px; height: 32px; border-radius: 50%; border: none;
            background: transparent; cursor: pointer; color: var(--color-nav-text);
            display: flex; align-items: center; justify-content: center;
        }
        .nav-config:hover { background: var(--color-nav-hover); }

        /* ── Layout geral ── */
        .conteudo { max-width: 1280px; margin: 0 auto; padding: 80px 24px 60px; }

        /* ── Superficie (cards) ── */
        .superficie {
            background: var(--color-surface);
            border: 1px solid var(--color-border);
            border-radius: 16px;
            box-shadow: var(--shadow-card);
        }

        /* ── VIEW INICIO ── */
        .inicio-hero { text-align: center; padding: 40px 0 24px; }
        .inicio-hero h1 { font-size: 2.2rem; font-weight: 800; letter-spacing: -.02em; }
        .inicio-hero p { font-size: .95rem; color: var(--color-text-secondary); margin-top: 6px; }

        .categorias-grid {
            display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px;
            margin-bottom: 32px;
        }
        @media (max-width: 900px) { .categorias-grid { grid-template-columns: repeat(2, 1fr); } }
        @media (max-width: 520px) { .categorias-grid { grid-template-columns: 1fr; } }

        .cat-card {
            padding: 20px; border-radius: 16px; cursor: pointer;
            border: 1px solid var(--color-border); background: var(--color-surface);
            transition: all .25s; position: relative; overflow: hidden;
        }
        .cat-card:hover {
            border-color: var(--color-primary-light);
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(79,70,229,.08);
        }
        .cat-icone {
            width: 44px; height: 44px; border-radius: 12px;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.1rem; margin-bottom: 14px;
        }
        .cat-card h3 { font-size: .82rem; font-weight: 700; text-transform: uppercase; letter-spacing: .02em; margin-bottom: 6px; }
        .cat-card p { font-size: .78rem; color: var(--color-text-secondary); line-height: 1.45; }

        .ia-section { margin-top: 8px; }
        .ia-section .rotulo-ia {
            display: flex; align-items: center; gap: 6px;
            font-size: .82rem; color: var(--color-text-secondary); margin-bottom: 10px;
        }
        .ia-input-wrap {
            display: flex; gap: 8px; align-items: center;
        }
        .ia-input {
            flex: 1; height: 48px; border-radius: 14px; padding: 0 16px;
            border: 1px solid var(--color-border); background: var(--color-surface);
            font-family: inherit; font-size: .88rem; color: var(--color-text); outline: none;
            transition: border-color .2s;
        }
        .ia-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(79,70,229,.1); }
        .ia-input::placeholder { color: #94a3b8; }
        .ia-nota { font-size: .72rem; color: var(--color-text-secondary); margin-top: 6px; }

        .btn-primario {
            height: 48px; padding: 0 24px; border-radius: 14px;
            background: var(--color-primary); color: #fff; border: none;
            font-family: inherit; font-size: .88rem; font-weight: 600;
            cursor: pointer; transition: background .2s; white-space: nowrap;
        }
        .btn-primario:hover { background: var(--color-primary-hover); }

        .link-historico {
            display: inline-flex; align-items: center; gap: 6px;
            margin-top: 16px; font-size: .82rem; color: var(--color-primary);
            font-weight: 500; cursor: pointer; text-decoration: none;
        }
        .link-historico:hover { text-decoration: underline; }

        /* ── Filtros ── */
        .filtros {
            display: flex; flex-wrap: wrap; gap: 8px; align-items: center;
            margin-bottom: 16px;
        }
        .campo, .btn {
            height: 40px; border-radius: 10px; font-family: inherit; font-size: .82rem;
            border: 1px solid var(--color-border);
            background: var(--color-surface); color: var(--color-text);
            padding: 0 12px; outline: none; transition: all .2s;
        }
        .campo:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(79,70,229,.1); }
        .campo::placeholder { color: #94a3b8; }
        select.campo { cursor: pointer; }
        .btn { cursor: pointer; font-weight: 600; display: inline-flex; align-items: center; gap: 6px; }
        .btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
        .btn-p {
            background: var(--color-primary); border-color: var(--color-primary); color: #fff;
        }
        .btn-p:hover { background: var(--color-primary-hover); color: #fff; }
        .rotulo { font-size: .74rem; color: var(--color-text-secondary); }

        /* ── Cabecalho de secao ── */
        .secao-cab { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
        .secao-cab h2 { font-size: 1.35rem; font-weight: 700; display: flex; align-items: center; gap: 8px; }
        .secao-cab .sub { font-size: .8rem; color: var(--color-text-secondary); margin-top: 2px; }

        /* ── KPIs ── */
        .grid-kpi {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 12px; margin-bottom: 16px;
        }
        .kpi { padding: 16px 18px; }
        .kpi-topo { display: flex; align-items: center; gap: 8px; }
        .kpi-icone {
            width: 28px; height: 28px; border-radius: 8px; flex-shrink: 0;
            display: flex; align-items: center; justify-content: center; font-size: .75rem;
        }
        .kpi-rotulo { font-size: .72rem; color: var(--color-text-secondary); font-weight: 500; }
        .kpi-valor { margin-top: 8px; font-size: 1.8rem; font-weight: 700; font-variant-numeric: tabular-nums; }

        /* ── Charts ── */
        .grid-charts {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
            gap: 14px; margin-bottom: 16px;
        }
        .card-chart { padding: 18px; }
        .card-titulo { font-size: .88rem; font-weight: 600; margin: 0; }
        .card-sub { font-size: .72rem; color: var(--color-text-secondary); margin: 3px 0 14px; }
        .box-chart { position: relative; height: 260px; }

        /* ── Tabelas ── */
        .tabela-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: .82rem; }
        thead th {
            text-align: left; padding: 11px 14px; font-size: .7rem; font-weight: 600;
            text-transform: uppercase; letter-spacing: .04em;
            color: var(--color-text-secondary);
            border-bottom: 1px solid var(--color-border); white-space: nowrap;
        }
        tbody td { padding: 11px 14px; border-bottom: 1px solid var(--color-border); vertical-align: middle; }
        tbody tr { transition: background .15s; }
        tbody tr:hover { background: #f8fafc; }
        tbody tr.clicavel { cursor: pointer; }
        .num { text-align: right; font-variant-numeric: tabular-nums; }
        .vazio { padding: 40px; text-align: center; color: var(--color-text-secondary); font-size: .84rem; }

        /* ── Badges ── */
        .badge {
            display: inline-flex; align-items: center; gap: 5px; white-space: nowrap;
            border-radius: 9999px; padding: 3px 10px; font-size: .69rem; font-weight: 600;
            border: 1px solid transparent;
        }
        .badge .ponto { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }

        /* ── Paginacao ── */
        .paginacao {
            display: flex; align-items: center; justify-content: space-between;
            gap: 10px; padding: 14px; border-top: 1px solid var(--color-border);
            font-size: .76rem; color: var(--color-text-secondary);
        }
        .paginacao button {
            height: 32px; min-width: 32px; padding: 0 12px; cursor: pointer;
            border-radius: 8px; border: 1px solid var(--color-border);
            background: var(--color-surface); color: var(--color-text); font-family: inherit;
            font-size: .78rem;
        }
        .paginacao button:disabled { opacity: .4; cursor: not-allowed; }

        /* ── Modal ── */
        .modal-fundo {
            display: none; position: fixed; inset: 0; z-index: 60;
            background: rgba(15,23,42,.5); backdrop-filter: blur(4px);
            align-items: center; justify-content: center; padding: 20px;
        }
        .modal {
            width: min(940px, 100%); max-height: 88vh; display: flex; flex-direction: column;
            background: var(--color-surface); border: 1px solid var(--color-border);
            border-radius: 20px; overflow: hidden; box-shadow: var(--shadow-lg);
        }
        .modal-topo {
            display: flex; align-items: flex-start; gap: 12px; padding: 18px 20px;
            border-bottom: 1px solid var(--color-border);
        }
        .modal-corpo { padding: 20px; overflow-y: auto; }
        .fechar {
            margin-left: auto; background: transparent; border: none; cursor: pointer;
            color: var(--color-text-secondary); font-size: 1.3rem; line-height: 1; padding: 2px 6px;
        }
        .fechar:hover { color: var(--color-text); }
        .secao-titulo {
            font-size: .72rem; text-transform: uppercase; letter-spacing: .05em;
            color: var(--color-text-secondary); font-weight: 600; margin: 20px 0 8px;
        }
        .grid-info { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 10px; }
        .info-item { background: var(--color-surface-alt); border-radius: 12px; padding: 10px 14px; }
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
            border-radius: 12px; padding: 12px 16px; font-size: .82rem; margin-bottom: 14px;
            border: 1px solid; display: flex; gap: 9px; align-items: flex-start;
        }
        .aviso-erro { background: #fef2f2; border-color: #fecaca; color: #b91c1c; }
        .aviso-info { background: var(--color-primary-bg); border-color: #c7d2fe; color: #3730a3; }
        .carregando { padding: 40px; text-align: center; color: var(--color-text-secondary); font-size: .84rem; }
        .oculto { display: none !important; }

        /* ── Estado vazio (Chamados) ── */
        .estado-vazio {
            display: flex; flex-direction: column; align-items: center; justify-content: center;
            padding: 60px 20px; color: var(--color-text-secondary);
        }
        .estado-vazio svg { width: 48px; height: 48px; margin-bottom: 14px; opacity: .5; }
        .estado-vazio p { font-size: .88rem; margin-bottom: 16px; }

        /* ── Animacoes ── */
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
        @keyframes scaleIn { from { opacity: 0; transform: scale(.96); } to { opacity: 1; transform: scale(1); } }
        .animate-fadeIn { animation: .25s ease-out fadeIn; }
        .animate-scaleIn { animation: .25s ease-out scaleIn; }

        @media (max-width: 640px) {
            .conteudo { padding: 72px 12px 40px; }
            .box-chart { height: 220px; }
            .categorias-grid { gap: 10px; }
        }

        /* Scrollbar */
        ::-webkit-scrollbar { width: 6px; }
        ::-webkit-scrollbar-thumb { background: var(--color-border); border-radius: 9999px; }
        ::-webkit-scrollbar-thumb:hover { background: var(--color-text-secondary); }
        ::-webkit-scrollbar-track { background: transparent; }
    </style>
</head>

<body>

<!-- ════════════════ FUNDO DECORATIVO ════════════════ -->
<div class="fundo-decorativo">
    <svg viewBox="0 0 200 200" fill="none" stroke="currentColor" stroke-width=".6">
        <circle cx="100" cy="50" r="6"/>
        <line x1="100" y1="56" x2="100" y2="90"/>
        <line x1="50" y1="100" x2="150" y2="100"/>
        <line x1="100" y1="90" x2="100" y2="100"/>
        <line x1="50" y1="100" x2="30" y2="140"/>
        <line x1="150" y1="100" x2="170" y2="140"/>
        <path d="M10 145 Q30 160 50 145" />
        <path d="M150 145 Q170 160 190 145" />
    </svg>
</div>

<!-- ════════════════ MENU FLUTUANTE ════════════════ -->
<nav class="menu-flutuante" id="nav-menu">
    <span class="brand-pill">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
        Central Contábil
    </span>
    <button class="nav-btn ativa" data-view="inicio" onclick="trocarView('inicio')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 12l9-9 9 9"/><path d="M9 21V9h6v12"/></svg>
        Início
    </button>
    <button class="nav-btn" data-view="chamados" onclick="trocarView('chamados')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M2 8h20"/></svg>
        Chamados
    </button>
    <button class="nav-btn" data-view="fila" onclick="trocarView('fila')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 3h5v5M4 20L21 3M21 16v5h-5M4 4l17 17"/></svg>
        Fila
    </button>
    <button class="nav-btn" data-view="painel" onclick="trocarView('painel')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V10M12 20V4M6 20v-6"/></svg>
        Painel
    </button>
    <button class="nav-btn" data-view="usuarios" onclick="trocarView('usuarios')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="7" r="4"/><path d="M5.5 21a6.5 6.5 0 0113 0"/></svg>
        Usuários
    </button>
    <button class="nav-btn" data-view="integracao" onclick="trocarView('integracao')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="9" height="9" rx="1"/><rect x="13" y="13" width="9" height="9" rx="1"/><path d="M13 6.5h3.5a2 2 0 012 2V13M11 17.5H7.5a2 2 0 01-2-2V11"/></svg>
        Integração
    </button>
    <button class="nav-config" title="Configurações">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>
    </button>
</nav>

<div class="conteudo">
    <div id="aviso-global"></div>

    <!-- ════════════════ VIEW: INICIO ════════════════ -->
    <section id="view-inicio" class="animate-fadeIn">
        <div class="inicio-hero">
            <h1>Como podemos ajudar?</h1>
            <p>Selecione o assunto da sua solicitação:</p>
        </div>

        <div class="categorias-grid" id="categorias-grid">
            <!-- Preenchido dinamicamente, com fallback estatico -->
        </div>

        <div class="ia-section">
            <div class="rotulo-ia">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="3"/><path d="M12 2v2m0 16v2M4.93 4.93l1.41 1.41m11.32 11.32l1.41 1.41M2 12h2m16 0h2M4.93 19.07l1.41-1.41m11.32-11.32l1.41-1.41"/></svg>
                Ou descreva o que você precisa
            </div>
            <div class="ia-input-wrap">
                <input type="text" class="ia-input" id="ia-descricao" placeholder="Ex.: preciso liberar o pedido 156946">
                <button class="btn-primario" onclick="enviarDescricaoIA()">Continuar</button>
            </div>
            <p class="ia-nota">O assistente identifica o assunto, pergunta só o que faltar e confirma antes de registrar.</p>
        </div>

        <a class="link-historico" onclick="trocarView('chamados')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            Ver histórico de chamados
        </a>
    </section>

    <!-- ════════════════ VIEW: CHAMADOS ════════════════ -->
    <section id="view-chamados" class="oculto">
        <div class="secao-cab">
            <div>
                <h2>Histórico de chamados</h2>
                <p class="sub" id="c-subtitulo">Carregando...</p>
            </div>
            <button class="btn-primario" onclick="trocarView('inicio')" style="height:40px;padding:0 18px;border-radius:10px;font-size:.82rem;">+ Nova solicitação</button>
        </div>
        <div class="filtros">
            <select class="campo" id="c-status"><option value="">Todos os status</option></select>
            <select class="campo" id="c-categoria"><option value="">Todas as categorias</option></select>
            <input type="date" class="campo" id="c-data-ini">
            <input type="date" class="campo" id="c-data-fim">
            <input type="text" class="campo" id="c-busca" placeholder="Buscar por número ou assunto" style="min-width:240px;">
            <button class="btn btn-p" onclick="carregarChamados(0)">Filtrar</button>
            <button class="btn" onclick="limparFiltrosChamados()">Limpar</button>
        </div>
        <div class="superficie">
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>Chamado</th><th>Abertura</th><th>Assunto</th><th>Categoria</th>
                        <th>Empresa</th><th>Status</th><th>Prioridade</th><th>Responsável</th>
                    </tr></thead>
                    <tbody id="tb-chamados"></tbody>
                </table>
            </div>
            <div class="paginacao">
                <span id="c-info">&mdash;</span>
                <span>
                    <button id="c-ant" onclick="paginaChamados(-1)">Anterior</button>
                    <button id="c-prox" onclick="paginaChamados(1)">Próxima</button>
                </span>
            </div>
        </div>
    </section>

    <!-- ════════════════ VIEW: FILA ════════════════ -->
    <section id="view-fila" class="oculto">
        <div class="secao-cab">
            <div>
                <h2>Fila de atendimento</h2>
                <p class="sub">Ordenada por prioridade e data de abertura. Clique em um chamado para atender.</p>
            </div>
        </div>
        <div class="filtros">
            <select class="campo" id="q-status"><option value="">Todos os status</option></select>
            <select class="campo" id="q-prioridade"><option value="">Todas as prioridades</option></select>
            <select class="campo" id="q-meus">
                <option value="N">Toda a fila</option>
                <option value="S">Somente os meus</option>
            </select>
            <input type="text" class="campo" id="q-busca" placeholder="Buscar por número ou assunto" style="min-width:240px;">
            <button class="btn btn-p" onclick="carregarFila(0)">Filtrar</button>
            <button class="btn" onclick="limparFiltrosFila()">Limpar</button>
        </div>
        <div class="superficie">
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>Chamado</th><th>Prioridade</th><th>Assunto</th><th>Solicitante</th>
                        <th>Empresa</th><th>Status</th><th>Aguardando</th><th>Responsável</th><th>Prazo</th>
                    </tr></thead>
                    <tbody id="tb-fila"></tbody>
                </table>
            </div>
            <div class="paginacao">
                <span id="q-info">&mdash;</span>
                <span>
                    <button id="q-ant" onclick="paginaFila(-1)">Anterior</button>
                    <button id="q-prox" onclick="paginaFila(1)">Próxima</button>
                </span>
            </div>
        </div>
    </section>

    <!-- ════════════════ VIEW: PAINEL ════════════════ -->
    <section id="view-painel" class="oculto">
        <div class="secao-cab">
            <div>
                <h2>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><path d="M18 20V10M12 20V4M6 20v-6"/></svg>
                    Painel analítico
                </h2>
                <p class="sub">Clique em qualquer gráfico para filtrar todo o painel. Botão direito no gráfico de categorias detalha por outra dimensão.</p>
            </div>
            <div class="filtros" style="margin-bottom:0;">
                <span class="rotulo">De</span>
                <input type="date" class="campo" id="f-data-ini" onchange="aplicarPainel()">
                <span class="rotulo">Até</span>
                <input type="date" class="campo" id="f-data-fim" onchange="aplicarPainel()">
            </div>
        </div>

        <div class="grid-kpi" id="kpis"></div>
        <div class="grid-kpi" id="indicadores"></div>

        <div class="grid-charts">
            <div class="superficie card-chart">
                <p class="card-titulo">Chamados por categoria</p>
                <p class="card-sub">Clique para filtrar o painel; botão direito para detalhar</p>
                <div class="box-chart"><canvas id="ch-categoria"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Chamados por prioridade</p>
                <p class="card-sub">Clique em uma fatia para filtrar o painel</p>
                <div class="box-chart"><canvas id="ch-prioridade"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Chamados por status</p>
                <p class="card-sub">Distribuição atual da carteira</p>
                <div class="box-chart"><canvas id="ch-status"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Chamados por empresa</p>
                <p class="card-sub">Volume por empresa de contexto</p>
                <div class="box-chart"><canvas id="ch-empresa"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Chamados por responsável</p>
                <p class="card-sub">Carga por atendente</p>
                <div class="box-chart"><canvas id="ch-responsavel"></canvas></div>
            </div>
            <div class="superficie card-chart">
                <p class="card-titulo">Volume por mês</p>
                <p class="card-sub">Série temporal de aberturas</p>
                <div class="box-chart"><canvas id="ch-mes"></canvas></div>
            </div>
        </div>

        <div class="superficie card-chart" style="margin-bottom:14px;">
            <p class="card-titulo">Principais assuntos</p>
            <p class="card-sub">Subcategorias mais demandadas, com conclusão, atraso e tempo médio</p>
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>Subcategoria</th><th>Categoria</th>
                        <th class="num">Chamados</th><th class="num">Concluídos</th>
                        <th class="num">Atrasados</th><th class="num">Tempo médio (h)</th>
                    </tr></thead>
                    <tbody id="tb-subcategoria"></tbody>
                </table>
            </div>
        </div>

        <div class="superficie card-chart">
            <p class="card-titulo">Chamados por solicitante</p>
            <p class="card-sub">Quem mais aciona a área contábil</p>
            <div class="tabela-wrap">
                <table>
                    <thead><tr><th>Solicitante</th><th class="num">Chamados</th></tr></thead>
                    <tbody id="tb-solicitante"></tbody>
                </table>
            </div>
        </div>
    </section>

    <!-- ════════════════ VIEW: USUARIOS ════════════════ -->
    <section id="view-usuarios" class="oculto">
        <div class="secao-cab">
            <div>
                <h2>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><circle cx="12" cy="7" r="4"/><path d="M5.5 21a6.5 6.5 0 0113 0"/></svg>
                    Usuários e permissões
                </h2>
                <p class="sub">Defina o papel de cada pessoa. Quem for marcado como <b>administrador</b> passa a enxergar o painel de indicadores, o histórico de acessos e todos os chamados do sistema.</p>
            </div>
        </div>
        <div class="grid-kpi" id="u-kpis"></div>
        <div class="filtros">
            <select class="campo" id="u-papel">
                <option value="">Todos os papéis</option>
                <option value="GESTOR">Administrador</option>
                <option value="ATENDENTE">Atendente</option>
                <option value="APROVADOR">Aprovador</option>
                <option value="SOLICITANTE">Solicitante</option>
            </select>
            <input type="text" class="campo" id="u-busca" placeholder="Buscar por e-mail" style="min-width:240px;">
            <button class="btn btn-p" onclick="carregarUsuarios(0)">Filtrar</button>
            <button class="btn" onclick="limparFiltrosUsuarios()">Limpar</button>
        </div>
        <div class="superficie">
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>Pessoa</th><th>Papel</th><th>Setor</th><th>Fila</th>
                        <th class="num">Chamados abertos</th><th class="num">Sob responsabilidade</th>
                        <th>Último acesso</th>
                    </tr></thead>
                    <tbody id="tb-usuarios"></tbody>
                </table>
            </div>
            <div class="paginacao">
                <span id="u-info">&mdash;</span>
                <span>
                    <button id="u-ant" onclick="paginaUsuarios(-1)">Anterior</button>
                    <button id="u-prox" onclick="paginaUsuarios(1)">Próxima</button>
                </span>
            </div>
        </div>
    </section>

    <!-- ════════════════ VIEW: INTEGRACAO ════════════════ -->
    <section id="view-integracao" class="oculto">
        <div class="secao-cab">
            <div>
                <h2>
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="22" height="22"><rect x="2" y="2" width="9" height="9" rx="1"/><rect x="13" y="13" width="9" height="9" rx="1"/><path d="M13 6.5h3.5a2 2 0 012 2V13M11 17.5H7.5a2 2 0 01-2-2V11"/></svg>
                    Integração Sankhya
                </h2>
                <p class="sub">Estado da importação de dados vinda do Sankhya — empresas, parceiros, produtos e documentos que alimentam o atendimento.</p>
            </div>
        </div>
        <div class="grid-kpi" id="i-entidades"></div>
        <div class="superficie card-chart">
            <p class="card-titulo">Histórico de importações</p>
            <p class="card-sub">50 execuções mais recentes</p>
            <div class="tabela-wrap">
                <table>
                    <thead><tr>
                        <th>Execução</th><th>Entidade</th><th>Tipo</th><th>Status</th>
                        <th class="num">Registros</th><th class="num">Duração</th><th>Quando</th><th>Erro</th>
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
                <p class="card-sub" id="m-sub" style="margin-bottom:0;">&mdash;</p>
            </div>
            <button class="fechar" onclick="fecharModal()" title="Fechar">&times;</button>
        </div>
        <div class="modal-corpo" id="m-corpo">
            <div class="carregando">Carregando chamado...</div>
        </div>
    </div>
</div>

<script>

/* ── 0) DIAGNOSTICO NA PROPRIA TELA ──
   No Sankhya a tela abre em iframe e o console do navegador nem sempre esta a mao.
   Erro nao tratado — inclusive CDN bloqueado pela rede da empresa — vira faixa de
   aviso no topo, em vez de tela em branco.

   ATENCAO ao editar este arquivo: nunca escreva um prefixo de taglib registrado
   (snk, c, fmt, fn) em formato de tag dentro de comentario ou string de JavaScript.
   O compilador de JSP nao enxerga comentario de JS e trata a mencao como tag real,
   derrubando a tela inteira com Internal Server Error. */
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
                 + '. Se a rede da empresa bloqueia CDN, publique o arquivo no proprio'
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

/* ================================================================================
   CENTRAL DE ATENDIMENTO CONTABIL — JSP autonomo (tema claro, Mitralab)

   Reproduz o projeto Mitra p-57477 com todas as 6 abas (Inicio, Chamados, Fila,
   Painel, Usuarios, Integracao). Os dados vem da API do Mitra via Server Functions.

   IMPORTANTE: JSP com isELIgnored="false" — nao usar template literals.
   ================================================================================ */

/* ── 1) CREDENCIAIS ── */
var MITRA = {
    BASE_URL:   'https://analytics2.mitrasheet.com:4437',
    PROJECT_ID: 57477,
    TOKEN:      'Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJNaXRyYSIsImp0aSI6IjI1MTQwIiwic3ViIjoiam9zZS52aWxlbGFAbmV1b25zb2x1Y29lcy5jb20iLCJpYXQiOjE3ODc2NjEwOTcsInRuaSI6NTc0NzcsImFjY2Vzc1R5cGUiOiJDUkVBVE9SIiwiYmFja1VSTCI6Imh0dHBzOi8vYW5hbHl0aWNzMi5taXRyYXNoZWV0LmNvbTo0NDM3IiwiZXhwIjoyNjUxNjYxMDk3fQ.bo5nXtxfWpjiXUWqNQ5-_O0ysI-je9hJOw1UNUmpYlQ'
};

/* ── 2) IDS DAS SERVER FUNCTIONS ── */
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

/* ── 3) CAMADA DE DADOS ── */
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
    if (typeof out === 'string') { try { out = JSON.parse(out); } catch (e) { } }
    if (out && Array.isArray(out.result)) return out.result;
    if (Array.isArray(out)) return out;
    return (out && out.rows) || [];
}

async function chamarSFUm(serverFunctionId, input) {
    var linhas = await chamarSF(serverFunctionId, input);
    return linhas[0] || {};
}

function sanitizar(v) {
    return String(v === null || v === undefined ? '' : v).replace(/'/g, "''").slice(0, 3900);
}

/* ── 4) APRESENTACAO ── */
var DICIONARIO = {
    'parametrizacao':'parametrização','parametrizacoes':'parametrizações',
    'pendencia':'pendência','pendencias':'pendências','duvida':'dúvida','duvidas':'dúvidas',
    'alteracao':'alteração','alteracoes':'alterações','municipio':'município',
    'contabil':'contábil','contabeis':'contábeis','demonstracao':'demonstração','demonstracoes':'demonstrações',
    'periodo':'período','relatorio':'relatório','relatorios':'relatórios',
    'informacao':'informação','informacoes':'informações','patrimonio':'patrimônio',
    'conciliacao':'conciliação','conciliacoes':'conciliações','divergencia':'divergência','divergencias':'divergências',
    'diferenca':'diferença','diferencas':'diferenças','solicitacao':'solicitação','solicitacoes':'solicitações',
    'numero':'número','codigo':'código','aliquota':'alíquota','retencao':'retenção',
    'servico':'serviço','servicos':'serviços','obrigacao':'obrigação',
    'descricao':'descrição','situacao':'situação','operacao':'operação',
    'inclusao':'inclusão','exclusao':'exclusão','usuario':'usuário','usuarios':'usuários','modulo':'módulo',
    'balanco':'balanço','razao':'razão','lancamento':'lançamento',
    'provisao':'provisão','criterio':'critério','calculo':'cálculo',
    'deposito':'depósito','fisica':'física','movimentacao':'movimentação',
    'aquisicao':'aquisição','transferencia':'transferência','depreciacao':'depreciação','inventario':'inventário',
    'bancaria':'bancária','titulo':'título','classificacao':'classificação',
    'orientacao':'orientação','liberacao':'liberação','devolucao':'devolução',
    'manutencao':'manutenção','apuracao':'apuração',
    'responsavel':'responsável','analise':'análise','producao':'produção',
    'aprovacao':'aprovação','concluido':'concluído','nao':'não',
    'media':'média','medio':'médio','ultimo':'último','ultima':'última','proximo':'próximo',
    'emissao':'emissão','impostos':'impostos'
};

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

function esc(v) {
    if (v === null || v === undefined) return '';
    return String(v).replace(/&/g, '&amp;').replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

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

/* Cores — tema claro */
var CORES_STATUS = {
    'NOVO':                  { fundo:'#eff6ff', texto:'#1d4ed8', ponto:'#3b82f6' },
    'EM ANALISE':            { fundo:'#f5f3ff', texto:'#6d28d9', ponto:'#8b5cf6' },
    'AGUARDANDO INFORMACAO': { fundo:'#fffbeb', texto:'#b45309', ponto:'#f59e0b' },
    'EM ATENDIMENTO':        { fundo:'#ecfeff', texto:'#0e7490', ponto:'#06b6d4' },
    'AGUARDANDO APROVACAO':  { fundo:'#fff7ed', texto:'#c2410c', ponto:'#f97316' },
    'CONCLUIDO':             { fundo:'#ecfdf5', texto:'#047857', ponto:'#10b981' },
    'CANCELADO':             { fundo:'#f1f5f9', texto:'#475569', ponto:'#64748b' }
};
var CORES_PRIORIDADE = {
    'URGENTE': { fundo:'#fef2f2', texto:'#b91c1c' },
    'ALTA':    { fundo:'#fff7ed', texto:'#c2410c' },
    'NORMAL':  { fundo:'#eef2ff', texto:'#4338ca' },
    'BAIXA':   { fundo:'#f1f5f9', texto:'#475569' }
};

function badgeStatus(status) {
    var c = CORES_STATUS[status] || CORES_STATUS['CANCELADO'];
    return '<span class="badge" style="background:' + c.fundo + ';color:' + c.texto
         + ';border-color:' + c.ponto + '33;">'
         + '<span class="ponto" style="background:' + c.ponto + ';"></span>' + esc(pt(status)) + '</span>';
}

function badgePrioridade(prioridade) {
    var c = CORES_PRIORIDADE[prioridade] || CORES_PRIORIDADE['NORMAL'];
    return '<span class="badge" style="background:' + c.fundo + ';color:' + c.texto + ';">'
         + esc(prioridade) + '</span>';
}
</script>

<script>
/* ── 5) ESTADO ── */
var TAMANHO_PAGINA = 20;
var estado = {
    perfil: {}, empresas: [], categorias: [], taxonomia: [],
    view: 'inicio',
    painel: { versao: 0, filtro: {} },
    chamados: { pagina: 0, total: 0 },
    fila: { pagina: 0, total: 0 },
    usuarios: { pagina: 0, total: 0 },
    graficos: {}
};

var STATUS_POSSIVEIS = ['NOVO','EM ANALISE','AGUARDANDO INFORMACAO','EM ATENDIMENTO',
                        'AGUARDANDO APROVACAO','CONCLUIDO','CANCELADO'];
var PRIORIDADES = ['URGENTE','ALTA','NORMAL','BAIXA'];

var PAPEIS = {
    'GESTOR':      { rotulo:'Administrador', cor:'#4f46e5', fundo:'var(--kpi-bg-purple)' },
    'ATENDENTE':   { rotulo:'Atendente',     cor:'#0891b2', fundo:'var(--kpi-bg-cyan)' },
    'APROVADOR':   { rotulo:'Aprovador',     cor:'#d97706', fundo:'var(--kpi-bg-amber)' },
    'SOLICITANTE': { rotulo:'Solicitante',   cor:'#64748b', fundo:'var(--color-surface-alt)' }
};

var PALETA = ['#ef4444','#f97316','#eab308','#22c55e','#3b82f6','#8b5cf6','#ec4899','#06b6d4'];

/* Icones e cores para categorias */
var CATEGORIAS_META = {
    'IMPOSTOS E FISCAL':              { icone:'📋', cor:'#3b82f6', fundo:'#dbeafe' },
    'CADASTROS E PARAMETRIZACOES':     { icone:'⚙️', cor:'#8b5cf6', fundo:'#ede9fe' },
    'PEDIDOS':                         { icone:'📦', cor:'#f97316', fundo:'#ffedd5' },
    'CONTABILIDADE':                   { icone:'📊', cor:'#10b981', fundo:'#d1fae5' },
    'RELATORIOS GERENCIAIS':           { icone:'📈', cor:'#6366f1', fundo:'#e0e7ff' },
    'ESTOQUE E IMOBILIZADO':           { icone:'🏭', cor:'#eab308', fundo:'#fef3c7' },
    'CONCILIACOES E DIVERGENCIAS':     { icone:'🔄', cor:'#f43f5e', fundo:'#ffe4e6' },
    'OUTROS':                          { icone:'💬', cor:'#64748b', fundo:'#e2e8f0' }
};

function el(id) { return document.getElementById(id); }

function avisar(mensagem, tipo) {
    var alvo = el('aviso-global');
    if (!mensagem) { alvo.innerHTML = ''; return; }
    alvo.innerHTML = '<div class="aviso aviso-' + (tipo || 'erro') + '">' + esc(mensagem) + '</div>';
}

/* ── 6) NAVEGACAO ── */
function trocarView(nome) {
    estado.view = nome;
    ['inicio','chamados','fila','painel','usuarios','integracao'].forEach(function (v) {
        var secao = el('view-' + v);
        if (secao) secao.classList.toggle('oculto', v !== nome);
    });
    var abas = document.querySelectorAll('#nav-menu .nav-btn');
    for (var i = 0; i < abas.length; i++) {
        abas[i].classList.toggle('ativa', abas[i].getAttribute('data-view') === nome);
    }
    avisar('');
    if (nome === 'chamados'   && !el('tb-chamados').innerHTML) carregarChamados(0);
    if (nome === 'fila'       && !el('tb-fila').innerHTML)     carregarFila(0);
    if (nome === 'painel'     && !el('kpis').innerHTML)        aplicarPainel();
    if (nome === 'usuarios'   && !el('tb-usuarios').innerHTML) carregarUsuarios(0);
    if (nome === 'integracao' && !el('tb-logs').innerHTML)     carregarIntegracao();
}

/* ── 7) VIEW INICIO — categorias ── */
function renderCategorias(taxonomia) {
    var cats = [];
    var vistas = {};
    (taxonomia || []).forEach(function (l) {
        if (l.CATNOME && !vistas[l.CATNOME]) {
            vistas[l.CATNOME] = true;
            cats.push({ nome: l.CATNOME, desc: l.CATDESC || '', icone: l.ICONE || '' });
        }
    });
    if (!cats.length) {
        /* Fallback estatico */
        cats = [
            { nome: 'IMPOSTOS E FISCAL', desc: 'Parametrizações, ISS, pendências e dúvidas fiscais.' },
            { nome: 'CADASTROS E PARAMETRIZACOES', desc: 'TOP, Natureza, Conta Contábil, Centro de Custo e acessos.' },
            { nome: 'PEDIDOS', desc: 'Liberação, alteração ou exclusão de pedidos.' },
            { nome: 'CONTABILIDADE', desc: 'Demonstrações, fechamento, período contábil e dúvidas contábeis.' },
            { nome: 'RELATORIOS GERENCIAIS', desc: 'Relatórios, consultas e informações gerenciais.' },
            { nome: 'ESTOQUE E IMOBILIZADO', desc: 'Estoque, custos, produtos, patrimônio e demais solicitações.' },
            { nome: 'CONCILIACOES E DIVERGENCIAS', desc: 'Diferenças, saldos e conciliações contábeis.' },
            { nome: 'OUTROS', desc: 'Demais dúvidas e solicitações.' }
        ];
    }
    el('categorias-grid').innerHTML = cats.map(function (c) {
        var meta = CATEGORIAS_META[c.nome] || CATEGORIAS_META['OUTROS'];
        return '<div class="cat-card superficie" onclick="selecionarCategoria(\'' + esc(c.nome).replace(/'/g, "\\'") + '\')">'
             + '<div class="cat-icone" style="background:' + meta.fundo + ';color:' + meta.cor + ';">' + meta.icone + '</div>'
             + '<h3>' + esc(pt(c.nome)) + '</h3>'
             + '<p>' + esc(pt(c.desc)) + '</p>'
             + '</div>';
    }).join('');
}

function selecionarCategoria(catNome) {
    /* Navegar para a aba de chamados filtrada por essa categoria */
    el('c-categoria').value = catNome;
    trocarView('chamados');
    carregarChamados(0);
}

function enviarDescricaoIA() {
    var texto = el('ia-descricao').value.trim();
    if (!texto) return;
    /* Placeholder — no Mitra, isso iria para uma SF de classificacao IA */
    avisar('Funcionalidade de classificação por IA disponível apenas na plataforma Mitra.', 'info');
}

/* ── 8) GRAFICOS ── */
function eixosTemaClaro() {
    return {
        grid:  { color: 'rgba(0,0,0,.06)', drawBorder: false },
        ticks: { color: '#64748b', font: { size: 11, family: 'Inter' } },
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
        : '#ef4444';

    var config = {
        type: tipoReal,
        data: {
            labels: rotulos,
            datasets: [{
                label: 'Chamados',
                data: valores,
                backgroundColor: cores,
                borderColor: (tipo === 'line') ? '#ef4444' : 'transparent',
                borderWidth: (tipo === 'line') ? 2 : 0,
                borderRadius: ehCircular ? 0 : 6,
                tension: .35,
                fill: false,
                pointBackgroundColor: '#ef4444',
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
                    labels: { color: '#334155', font: { size: 11, family: 'Inter' }, boxWidth: 10, padding: 10 }
                },
                tooltip: {
                    backgroundColor: '#fff',
                    borderColor: '#e2e8f0',
                    borderWidth: 1,
                    titleColor: '#0f172a',
                    bodyColor: '#64748b',
                    padding: 10
                }
            },
            scales: ehCircular ? {} : { x: eixosTemaClaro(), y: eixosTemaClaro() },
            onClick: function (evt, elementos) {
                if (!aoClicar || !elementos.length) return;
                aoClicar(rotulos[elementos[0].index]);
            }
        }
    };
    estado.graficos[canvasId] = new Chart(ctx, config);
}

/* ── 9) VIEW PAINEL ── */
function filtrosPainel() {
    return {
        dataIni: el('f-data-ini').value || '', dataFim: el('f-data-fim').value || '',
        status: '', prioridade: '',
        empresa: '', categoria: sanitizar(estado.painel.filtro.categoria || ''),
        subcategoria: '', responsavel: '', mes: ''
    };
}

function limparFiltrosPainel() {
    el('f-data-ini').value = ''; el('f-data-fim').value = '';
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
    if (estado.painel.versao !== v) return;
    if (r[0].status === 'rejected') {
        avisar('Não foi possível carregar o painel. Restrito ao administrador (GESTOR). Detalhe: ' + r[0].reason.message, 'erro');
        return;
    }
    var ok = function (i) { return r[i].status === 'fulfilled' ? r[i].value : (i < 2 ? {} : []); };
    renderKpis(ok(0)); renderIndicadores(ok(1));

    var cat = ok(2).map(function (l) { return { nome: pt(l.name), bruto: l.name, valor: Number(l.value) }; });
    desenharGrafico('ch-categoria', 'barH',
        cat.map(function (x) { return x.nome; }), cat.map(function (x) { return x.valor; }),
        function (rotulo) {
            var achado = cat.filter(function (x) { return x.nome === rotulo; })[0];
            estado.painel.filtro.categoria = (estado.painel.filtro.categoria === (achado && achado.bruto)) ? '' : (achado && achado.bruto);
            aplicarPainel();
        });
    desenharGrafico('ch-prioridade', 'doughnut',
        ok(5).map(function (l) { return l.name; }), ok(5).map(function (l) { return Number(l.value); }));
    desenharGrafico('ch-status', 'barH',
        ok(3).map(function (l) { return pt(l.name); }), ok(3).map(function (l) { return Number(l.value); }));
    desenharGrafico('ch-empresa', 'barH',
        ok(6).map(function (l) { return l.name; }), ok(6).map(function (l) { return Number(l.value); }));
    desenharGrafico('ch-responsavel', 'barH',
        ok(7).map(function (l) { return pt(l.name); }), ok(7).map(function (l) { return Number(l.value); }));
    var ms = ok(4);
    desenharGrafico('ch-mes', 'line',
        ms.map(function (l) { return mesLabel(l.name); }), ms.map(function (l) { return Number(l.value); }));
    renderSubcategorias(ok(8)); renderSolicitantes(ok(9));
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
         + '</div>';
}

function renderKpis(k) {
    var total = Number(k.TOTAL || 0), concluidos = Number(k.CONCLUIDOS || 0);
    el('kpis').innerHTML =
          cartaoKpi('Chamados', numero(total), '', '#3b82f6', 'var(--kpi-bg-blue)', '&#128202;')
        + cartaoKpi('Concluídos', numero(concluidos), '', '#10b981', 'var(--kpi-bg-green)', '&#10003;')
        + cartaoKpi('SLA vencido', numero(k.ATRASADOS), '', '#ef4444', 'var(--kpi-bg-rose)', '&#9888;')
        + cartaoKpi('Tempo médio', k.TEMPOMEDIO ? numero(k.TEMPOMEDIO, 1) + 'h' : '—', '', '#f59e0b', 'var(--kpi-bg-amber)', '&#9201;');
}

function renderIndicadores(ind) {
    var pct = function (v) { return (v === null || v === undefined) ? '—' : numero(v, 1) + '%'; };
    el('indicadores').innerHTML =
          cartaoKpi('Aderência ao SLA', pct(ind.ADERENCIASLA), '', '#10b981', 'var(--kpi-bg-green)', '&#9201;')
        + cartaoKpi('1a resposta (média)', ind.TEMPOPRIMRESP ? numero(ind.TEMPOPRIMRESP, 1) + 'h' : '—', '', '#6366f1', 'var(--kpi-bg-purple)', '&#8618;')
        + cartaoKpi('Acurácia da prioridade', pct(ind.ACURACIAPRIORIDADE), '', '#3b82f6', 'var(--kpi-bg-blue)', '&#8982;')
        + cartaoKpi('Resolução na 1a interação', pct(ind.RESOLUCAOPRIMEIRA), '', '#8b5cf6', 'var(--kpi-bg-purple)', '&#10004;')
        + cartaoKpi('Reabertos', numero(ind.REABERTOS), '', '#ef4444', 'var(--kpi-bg-rose)', '&#8635;');
}

function renderSubcategorias(linhas) {
    if (!linhas.length) { el('tb-subcategoria').innerHTML = '<tr><td colspan="6" class="vazio">Sem dados no período.</td></tr>'; return; }
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
    if (!linhas.length) { el('tb-solicitante').innerHTML = '<tr><td colspan="2" class="vazio">Sem dados no período.</td></tr>'; return; }
    el('tb-solicitante').innerHTML = linhas.map(function (l) {
        return '<tr><td>' + esc(l.name) + '</td><td class="num">' + numero(l.value) + '</td></tr>';
    }).join('');
}
</script>

<script>
/* ── 10) VIEW CHAMADOS ── */
function filtrosChamados() {
    return {
        busca: sanitizar(el('c-busca').value || ''), status: el('c-status').value || '',
        categoria: sanitizar(el('c-categoria').value || ''),
        dataIni: el('c-data-ini').value || '', dataFim: el('c-data-fim').value || ''
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
            chamarSF(SF.listarMeusChamados, Object.assign({}, f, { limite: TAMANHO_PAGINA, offset: pagina * TAMANHO_PAGINA })),
            chamarSFUm(SF.contarMeusChamados, f)
        ]);
        var linhas = res[0];
        estado.chamados.total = Number(res[1].TOTAL || 0);
        el('c-subtitulo').textContent = estado.chamados.total + ' chamados registrados por você';
        if (!linhas.length) {
            el('tb-chamados').innerHTML = '<tr><td colspan="8"><div class="estado-vazio">'
                + '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M2 8h20"/></svg>'
                + '<p>Você ainda não registrou nenhum chamado.</p>'
                + '<button class="btn-primario" style="height:40px;padding:0 20px;border-radius:10px;font-size:.84rem;" onclick="trocarView(\'inicio\')">Abrir a primeira solicitação</button>'
                + '</div></td></tr>';
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

/* ── 11) VIEW FILA ── */
function filtrosFila() {
    return {
        busca: sanitizar(el('q-busca').value || ''), status: el('q-status').value || '',
        prioridade: el('q-prioridade').value || '', fila: '',
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
        el('q-info').textContent = 'Página ' + (pagina + 1) + ' — ' + linhas.length + ' chamado(s) nesta página';
        el('q-ant').disabled = pagina === 0;
        el('q-prox').disabled = !temProxima;
    } catch (e) {
        el('tb-fila').innerHTML = '<tr><td colspan="9" class="vazio">' + esc(e.message) + '</td></tr>';
    }
}
function paginaFila(passo) { var nova = estado.fila.pagina + passo; if (nova < 0) return; carregarFila(nova); }

/* ── 12) VIEW USUARIOS ── */
function limparFiltrosUsuarios() { el('u-busca').value = ''; el('u-papel').value = ''; carregarUsuarios(0); }
async function carregarUsuarios(pagina) {
    estado.usuarios.pagina = pagina;
    el('tb-usuarios').innerHTML = '<tr><td colspan="7" class="vazio">Carregando...</td></tr>';
    var f = { busca: sanitizar(el('u-busca').value || ''), papel: el('u-papel').value || '' };
    try {
        var res = await Promise.all([
            chamarSF(SF.listarUsuariosProjeto, Object.assign({}, f, { limite: TAMANHO_PAGINA, offset: pagina * TAMANHO_PAGINA })),
            chamarSFUm(SF.contarUsuariosProjeto, f),
            chamarSFUm(SF.resumoPapeisUsuarios, {})
        ]);
        var linhas = res[0]; estado.usuarios.total = Number(res[1].TOTAL || 0); var resumo = res[2];
        el('u-kpis').innerHTML =
              cartaoKpi('Administradores', numero(resumo.GESTORES), '', '#4f46e5', 'var(--kpi-bg-purple)', '&#9733;')
            + cartaoKpi('Atendentes', numero(resumo.ATENDENTES), '', '#0891b2', 'var(--kpi-bg-cyan)', '&#9993;')
            + cartaoKpi('Aprovadores', numero(resumo.APROVADORES), '', '#d97706', 'var(--kpi-bg-amber)', '&#10003;')
            + cartaoKpi('Solicitantes', numero(resumo.SOLICITANTES), '', '#64748b', 'var(--color-surface-alt)', '&#9679;');
        if (!linhas.length) {
            el('tb-usuarios').innerHTML = '<tr><td colspan="7" class="vazio">Nenhum usuário encontrado.</td></tr>';
        } else {
            el('tb-usuarios').innerHTML = linhas.map(function (u) {
                var p = PAPEIS[u.PAPEL] || PAPEIS['SOLICITANTE'];
                var eu = Number(u.EUMESMO) === 1;
                var contas = Number(u.CONTAS || 1);
                return '<tr>'
                     + '<td>' + esc(u.EMAIL) + (eu ? ' <span style="background:var(--color-primary-bg);color:var(--color-primary);padding:1px 6px;border-radius:4px;font-size:.65rem;font-weight:600;">você</span>' : '')
                     + (contas > 1 ? ' <span style="color:var(--color-text-secondary);font-size:.68rem;">' + contas + ' contas</span>' : '') + '</td>'
                     + '<td><span class="badge" style="background:' + p.fundo + ';color:' + p.cor + ';">' + esc(p.rotulo) + '</span></td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(pt(u.SETOR) || '—') + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(pt(u.FILA) || '—') + '</td>'
                     + '<td class="num">' + numero(u.ABERTOS) + '</td>'
                     + '<td class="num">' + numero(u.ATRIBUIDOS) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + (u.ULTIMOACESSO ? dataHora(u.ULTIMOACESSO) : 'Nunca acessou') + '</td>'
                     + '</tr>';
            }).join('');
        }
        atualizarPaginacao('u', estado.usuarios);
    } catch (e) {
        el('tb-usuarios').innerHTML = '<tr><td colspan="7" class="vazio">' + esc(e.message) + '</td></tr>';
    }
}
function paginaUsuarios(passo) {
    var nova = estado.usuarios.pagina + passo;
    if (nova < 0 || nova * TAMANHO_PAGINA >= estado.usuarios.total) return;
    carregarUsuarios(nova);
}

/* ── 13) VIEW INTEGRACAO ── */
async function carregarIntegracao() {
    el('tb-logs').innerHTML = '<tr><td colspan="8" class="vazio">Carregando...</td></tr>';
    try {
        var res = await Promise.all([
            chamarSF(SF.obterUltimasSyncs, {}),
            chamarSF(SF.listarLogsImportacao, { entidade: '' })
        ]);
        var syncs = res[0], logs = res[1];
        el('i-entidades').innerHTML = syncs.length ? syncs.map(function (s) {
            var ok = String(s.STATUS || '').toUpperCase() === 'SUCESSO' || String(s.STATUS || '').toUpperCase() === 'SUCCESS';
            var min = Number(s.MINUTOS_ATRAS || 0);
            var quando = min < 60 ? min + ' min atrás' : (min < 1440 ? Math.floor(min / 60) + 'h atrás' : Math.floor(min / 1440) + 'd atrás');
            return cartaoKpi(pt(s.ENTIDADE), numero(s.REGISTROS_IMPORTADOS), quando,
                             ok ? '#10b981' : '#ef4444', ok ? 'var(--kpi-bg-green)' : 'var(--kpi-bg-rose)',
                             ok ? '&#10003;' : '&#9888;');
        }).join('') : '<div class="superficie kpi"><span class="kpi-rotulo">Nenhuma importação registrada.</span></div>';
        if (!logs.length) {
            el('tb-logs').innerHTML = '<tr><td colspan="8" class="vazio">Sem histórico de importação.</td></tr>';
        } else {
            el('tb-logs').innerHTML = logs.map(function (l) {
                var ok = String(l.STATUS || '').toUpperCase().indexOf('SUCES') === 0 || String(l.STATUS || '').toUpperCase() === 'SUCCESS';
                return '<tr>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(l.ID) + '</td>'
                     + '<td>' + esc(pt(l.ENTIDADE)) + '</td>'
                     + '<td style="color:var(--color-text-secondary);">' + esc(pt(l.TIPO)) + '</td>'
                     + '<td><span class="badge" style="background:' + (ok ? '#ecfdf5' : '#fef2f2')
                     + ';color:' + (ok ? '#047857' : '#b91c1c') + ';">' + esc(l.STATUS) + '</span></td>'
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

/* ── 14) MODAL DETALHE ── */
function fecharModal() { el('modal-chamado').style.display = 'none'; }
async function abrirChamado(nuChamado) {
    el('modal-chamado').style.display = 'flex';
    el('m-titulo').textContent = 'Chamado'; el('m-sub').textContent = 'Carregando...';
    el('m-corpo').innerHTML = '<div class="carregando">Carregando chamado...</div>';
    var p = { numChamado: String(nuChamado) };
    try {
        var res = await Promise.allSettled([
            chamarSFUm(SF.detalharChamado, p), chamarSF(SF.listarDetalhesChamado, p),
            chamarSF(SF.listarHistoricoChamado, p), chamarSF(SF.listarAnexosChamado, p)
        ]);
        var c = res[0].status === 'fulfilled' ? res[0].value : {};
        if (!c.NUCHAMADO) { el('m-corpo').innerHTML = '<div class="vazio">Chamado não encontrado ou sem permissão.</div>'; return; }
        var campos = res[1].status === 'fulfilled' ? res[1].value : [];
        var historico = res[2].status === 'fulfilled' ? res[2].value : [];
        var anexos = res[3].status === 'fulfilled' ? res[3].value : [];
        el('m-titulo').innerHTML = esc(c.NUMCHAMADO) + ' &nbsp; ' + badgeStatus(c.STATUS) + ' ' + badgePrioridade(c.PRIORIDADE);
        el('m-sub').textContent = pt(c.ASSUNTO || '');
        var html = '<div class="grid-info">'
            + infoItem('Solicitante', c.SOLICITANTE) + infoItem('Responsável', pt(c.RESPONSAVEL))
            + infoItem('Empresa', c.EMPRESA) + infoItem('Categoria', pt(c.CATEGORIA) + ' / ' + pt(c.SUBCATEGORIA))
            + infoItem('Aberto em', dataHora(c.DTABERTURA)) + infoItem('Prazo previsto', dataHora(c.DTPREVISTA))
            + infoItem('1a resposta', dataHora(c.DTPRIMRESP)) + infoItem('Concluído em', dataHora(c.DTCONCLUSAO))
            + infoItem('Fila', pt(c.FILA) || '—') + infoItem('Origem', pt(c.ORIGEM) || '—')
            + infoItem('Reaberturas', numero(c.REABERTURAS)) + infoItem('Tempo de atendimento', c.TEMPOATEND ? numero(c.TEMPOATEND, 1) + 'h' : '—')
            + '</div>';
        html += '<p class="secao-titulo">Descrição</p><div class="info-item"><div class="v" style="white-space:pre-wrap;">' + esc(pt(c.DESCRICAO || '')) + '</div></div>';
        if (c.SOLUCAO) html += '<p class="secao-titulo">Solução</p><div class="info-item"><div class="v" style="white-space:pre-wrap;">' + esc(pt(c.SOLUCAO)) + '</div></div>';
        if (c.CANCMOTIVO) html += '<p class="secao-titulo">Motivo do cancelamento</p><div class="info-item"><div class="v">' + esc(pt(c.CANCMOTIVO)) + '</div></div>';
        if (campos.length) {
            html += '<p class="secao-titulo">Campos da solicitação</p><div class="grid-info">'
                 + campos.map(function (d) { return infoItem(pt(d.ROTULO || d.CAMPO), pt(d.VALOR)); }).join('') + '</div>';
        }
        if (historico.length) {
            html += '<p class="secao-titulo">Linha do tempo</p><div class="linha-tempo">'
                 + historico.map(function (h) {
                     return '<div class="evento"><div class="quando">' + dataHora(h.DHEVENTO) + ' — ' + esc(h.AUTOR || 'sistema')
                          + ' &middot; ' + esc(pt(h.TIPO)) + '</div><div class="oque">' + esc(pt(h.DESCRICAO || '')) + '</div></div>';
                   }).join('') + '</div>';
        }
        if (anexos.length) {
            html += '<p class="secao-titulo">Anexos</p><div class="grid-info">'
                 + anexos.map(function (a) { return infoItem(a.NOMEARQ, numero(Number(a.TAMANHO || 0) / 1024, 1) + ' KB — ' + dataHora(a.DHUPLOAD)); }).join('')
                 + '</div>';
        }
        el('m-corpo').innerHTML = html;
    } catch (e) { el('m-corpo').innerHTML = '<div class="vazio">' + esc(e.message) + '</div>'; }
}
function infoItem(rotulo, valor) {
    return '<div class="info-item"><div class="r">' + esc(rotulo) + '</div>'
         + '<div class="v">' + esc(valor === null || valor === undefined || valor === '' ? '—' : valor) + '</div></div>';
}

/* ── 15) UTILITARIOS E PARTIDA ── */
function atualizarPaginacao(prefixo, ctx) {
    var paginas = Math.max(1, Math.ceil(ctx.total / TAMANHO_PAGINA));
    el(prefixo + '-info').textContent = ctx.total + ' pessoa(s) — ' + (ctx.pagina + 1) + ' de ' + paginas;
    el(prefixo + '-ant').disabled = ctx.pagina === 0;
    el(prefixo + '-prox').disabled = (ctx.pagina + 1) >= paginas;
}

function preencherSelect(id, valores, rotulos) {
    var alvo = el(id); if (!alvo) return;
    var primeira = alvo.options[0] ? alvo.options[0].outerHTML : '';
    alvo.innerHTML = primeira + valores.map(function (v, i) {
        return '<option value="' + esc(v) + '">' + esc(rotulos ? rotulos[i] : pt(v)) + '</option>';
    }).join('');
}

document.addEventListener('keydown', function (e) { if (e.key === 'Escape') fecharModal(); });

async function iniciar() {
    preencherSelect('c-status', STATUS_POSSIVEIS);
    preencherSelect('q-status', STATUS_POSSIVEIS);
    preencherSelect('q-prioridade', PRIORIDADES);

    try {
        var res = await Promise.allSettled([
            chamarSFUm(SF.meuPerfil, {}),
            chamarSF(SF.listarEmpresas, {}),
            chamarSF(SF.listarTaxonomia, {})
        ]);
        if (res[0].status === 'fulfilled') {
            estado.perfil = res[0].value;
        } else {
            avisar('Não foi possível autenticar. Verifique o token. Detalhe: ' + res[0].reason.message, 'erro');
            renderCategorias([]);
            return;
        }
        if (res[1].status === 'fulfilled') estado.empresas = res[1].value;
        if (res[2].status === 'fulfilled') {
            estado.taxonomia = res[2].value;
            var vistas = {};
            res[2].value.forEach(function (l) { if (l.CATNOME) vistas[l.CATNOME] = true; });
            estado.categorias = Object.keys(vistas);
            preencherSelect('c-categoria', estado.categorias);
        }
        renderCategorias(estado.taxonomia);
    } catch (e) {
        avisar('Falha ao iniciar: ' + e.message, 'erro');
        renderCategorias([]);
    }
}

iniciar();
</script>

</body>
</html>
