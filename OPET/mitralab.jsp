<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<<<<<<< HEAD
<%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
<%--
    mitralab.jsp - casca (shell) para rodar um projeto MitraLab dentro do Sankhya.
    A tela do Sankhya carrega este JSP; o projeto real roda dentro do iframe abaixo.
    Configuracao: bloco no inicio do scriptlet (URL, titulo, barra, query string).
--%>
<%
    /* ================== CONFIGURACAO ==================
       Troque APENAS a URL abaixo pela URL do projeto MitraLab.
       Precisa ser HTTPS se o Sankhya roda em HTTPS (senao o navegador bloqueia
       o conteudo misto e o iframe fica em branco).                            */
    String URL_APP   = "https://19825-57477.prod.mitralab.io";
    String TITULO    = "MitraLab";
    boolean BARRA    = true;   /* barra superior com titulo, usuario e recarregar    */
    boolean PASSA_QS = true;   /* repassa a query string desta tela para o iframe   */
=======
    <%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
        <%-- mitralab.jsp - casca (shell) para rodar um projeto MitraLab dentro do Sankhya. A tela do Sankhya carrega
            este JSP; o projeto real roda dentro do iframe abaixo. Configuracao: bloco no inicio do scriptlet (URL,
            titulo, barra, query string). --%>
            <% /*==================CONFIGURACAO==================Troque APENAS a URL abaixo pela URL do projeto
                MitraLab. Precisa ser HTTPS se o Sankhya roda em HTTPS (senao o navegador bloqueia o conteudo misto e o
                iframe fica em branco). */ String URL_APP="https://19825-57477.prod.mitralab.io" ; String
                TITULO="MitraLab" ; boolean BARRA=true; /* barra superior com recarregar / abrir em nova aba */ boolean
                PASSA_QS=true; /* repassa a query string desta tela para o iframe */ /* Usuario logado do Sankhya
                (STP_GET_CODUSULOGADO). PASSA_USUARIO=true -> a tela consulta quem esta logado e acrescenta os
                parametros abaixo na URL do iframe, alem de enviar um postMessage ao app.
                EXIGE_USUARIO = true -> se nao conseguir identificar o usuario, o app NAO
                e carregado (evita abrir a tela sem saber quem e). */
                boolean PASSA_USUARIO = true;
                boolean EXIGE_USUARIO = false;
                String P_CODUSU = "codusu"; /* nome do parametro do codigo do usuario */
                String P_NOMEUSU = "nomeusu"; /* nome do parametro do nome do usuario */
                String P_EMAIL = "emailusu"; /* nome do parametro do e-mail do usuario */
>>>>>>> 4ef9d691db70de3f5c50ad285a3a427110b02ffe

                /* Repassa parametros recebidos pela tela do Sankhya (ex.: ?id=123) */
                String qs = request.getQueryString();
                String urlFinal = URL_APP;
                if (PASSA_QS && qs != null && qs.length() > 0) {
                String sep = (URL_APP.indexOf(63) >= 0) ? "&" : "?"; /* 63 = caractere ? */
                urlFinal = URL_APP + sep + qs;
                }
                String urlJs = urlFinal.replace("\\", "\\\\").replace("\"", "\\\"");
                String sepJs = (urlFinal.indexOf(63) >= 0) ? "&" : "?"; /* separador p/ o usuario */
                %>
                <!DOCTYPE html>
                <html lang="pt-BR">

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>
                        <%= TITULO %>
                    </title>
                    <snk:load />
                    <style>
                        html,
                        body {
                            height: 100%;
                            margin: 0;
                            padding: 0;
                            overflow: hidden;
                            background: #eef1f5;
                            font-family: "Segoe UI", Roboto, Arial, sans-serif;
                        }

                        #wrap {
                            position: fixed;
                            top: 0;
                            right: 0;
                            bottom: 0;
                            left: 0;
                            display: flex;
                            flex-direction: column;
                        }

                        #barra {
                            flex: 0 0 auto;
                            display: flex;
                            align-items: center;
                            gap: 8px;
                            padding: 6px 12px;
                            background: #ffffff;
                            border-bottom: 1px solid #dbe1e8;
                            color: #2b3a4a;
                            font-size: 13px;
                        }

                        #barra .titulo {
                            font-weight: 600;
                            margin-right: auto;
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                        }

                        #barra .usuario {
                            color: #5c6b7a;
                            font-size: 12px;
                            white-space: nowrap;
                            margin-right: 4px;
                        }

                        #barra button,
                        #barra a {
                            border: 1px solid #dbe1e8;
                            background: #f5f7fa;
                            color: #2b3a4a;
                            border-radius: 6px;
                            padding: 4px 10px;
                            font-size: 12px;
                            cursor: pointer;
                            text-decoration: none;
                            line-height: 18px;
                        }

                        #barra button:hover,
                        #barra a:hover {
                            background: #e7ecf2;
                        }

                        #palco {
                            position: relative;
                            flex: 1 1 auto;
                            min-height: 0;
                        }

                        #app {
                            width: 100%;
                            height: 100%;
                            border: 0;
                            display: block;
                            background: #ffffff;
                        }

                        .camada {
                            position: absolute;
                            top: 0;
                            right: 0;
                            bottom: 0;
                            left: 0;
                            display: flex;
                            flex-direction: column;
                            align-items: center;
                            justify-content: center;
                            gap: 14px;
                            background: #eef1f5;
                            color: #5c6b7a;
                            font-size: 14px;
                            text-align: center;
                            padding: 24px;
                        }

                        .camada.oculto {
                            display: none;
                        }

                        .spin {
                            width: 34px;
                            height: 34px;
                            border: 3px solid #dbe1e8;
                            border-top-color: #2b6cb0;
                            border-radius: 50%;
                            animation: gira 0.9s linear infinite;
                        }

                        @keyframes gira {
                            to {
                                transform: rotate(360deg);
                            }
                        }

                        #erro h3 {
                            margin: 0;
                            color: #2b3a4a;
                            font-size: 16px;
                        }

                        #erro .cod {
                            font-family: Consolas, monospace;
                            font-size: 12px;
                            color: #2b3a4a;
                            background: #ffffff;
                            border: 1px solid #dbe1e8;
                            border-radius: 6px;
                            padding: 8px 12px;
                            max-width: 90%;
                            word-break: break-all;
                        }

                        #erro .acoes {
                            display: flex;
                            gap: 8px;
                        }

                        #erro .acoes a,
                        #erro .acoes button {
                            border: 1px solid #2b6cb0;
                            background: #2b6cb0;
                            color: #ffffff;
                            border-radius: 6px;
                            padding: 7px 14px;
                            font-size: 13px;
                            cursor: pointer;
                            text-decoration: none;
                        }

                        #erro .acoes .sec {
                            background: #ffffff;
                            color: #2b6cb0;
                        }
                    </style>
                </head>

                <body>
                    <div id="wrap">
                        <% if (BARRA) { %>
                            <div id="barra">
                                <span class="titulo">
                                    <%= TITULO %>
                                </span>
                                <span class="usuario" id="usuario" title="Usuario logado no Sankhya"></span>
                                <button type="button" onclick="recarregar()">Recarregar</button>
                                <a href="<%= urlFinal %>" target="_blank" rel="noopener">Abrir em nova aba</a>
                            </div>
                            <% } %>

<<<<<<< HEAD
<body>
    <div id="wrap">
        <% if (BARRA) { %>
        <div id="barra">
            <span class="titulo"><%= TITULO %></span>
            <span class="usuario" id="usuario" title="Usuario logado no Sankhya"></span>
            <button type="button" onclick="recarregar()">Recarregar</button>
        </div>
        <% } %>
=======
                                <div id="palco">
                                    <iframe id="app"
                                        src="<% if (!PASSA_USUARIO) { %><%= urlFinal %><% } else { %>about:blank<% } %>"
                                        title="<%= TITULO %>"
                                        allow="clipboard-read; clipboard-write; fullscreen; camera; microphone; geolocation"
                                        allowfullscreen referrerpolicy="no-referrer-when-downgrade"></iframe>
>>>>>>> 4ef9d691db70de3f5c50ad285a3a427110b02ffe

                                    <div id="carregando" class="camada">
                                        <div class="spin"></div>
                                        <div>Carregando <%= TITULO %>...</div>
                                    </div>

                                    <div id="erro" class="camada oculto">
                                        <h3>Nao foi possivel exibir o projeto aqui dentro</h3>
                                        <div>O site pode estar fora do ar ou pode estar bloqueando a exibicao em
                                            iframe (cabecalhos X-Frame-Options / Content-Security-Policy).</div>
                                        <div class="cod" id="urlErro"></div>
                                        <div class="acoes">
                                            <a id="linkNova" href="#" target="_blank" rel="noopener">Abrir em nova
                                                aba</a>
                                            <button type="button" class="sec" onclick="recarregar()">Tentar de
                                                novo</button>
                                        </div>
                                    </div>
                                </div>
                    </div>

                    <script type="text/javascript">
                        var URL_APP = "<%= urlJs %>";
                        var SEP_QS = "<%= sepJs %>";
                        var TIMEOUT_MS = 15000;
                        var timer = null;

                        /* ---------- usuario logado do Sankhya ---------- */
                        var PASSA_USUARIO = <%= PASSA_USUARIO %>;
                        var EXIGE_USUARIO = <%= EXIGE_USUARIO %>;
                        var P_CODUSU = "<%= P_CODUSU %>";
                        var P_NOMEUSU = "<%= P_NOMEUSU %>";
                        var P_EMAIL = "<%= P_EMAIL %>";
                        var USUARIO = null;   /* { codusu, nome, email } depois de identificado */

                        /* Endpoints possiveis do servico, conforme onde a tela esta publicada. */
                        var ENDPOINTS = ["/mge/service.sbr", "../service.sbr", "service.sbr"];

                        /* Variacoes de SQL: Oracle usa FROM DUAL, MySQL/SQLServer usam funcao.
                           A primeira que responder com um codigo valido e usada.             */
                        var SQLS = [
                            "SELECT STP_GET_CODUSULOGADO() AS CODUSU, " +
                            "       (SELECT NOMEUSU FROM TSIUSU WHERE CODUSU = STP_GET_CODUSULOGADO()) AS NOMEUSU, " +
                            "       (SELECT EMAIL   FROM TSIUSU WHERE CODUSU = STP_GET_CODUSULOGADO()) AS EMAIL",
                            "SELECT STP_GET_CODUSULOGADO AS CODUSU, U.NOMEUSU, U.EMAIL " +
                            "  FROM TSIUSU U WHERE U.CODUSU = STP_GET_CODUSULOGADO",
                            "SELECT STP_GET_CODUSULOGADO AS CODUSU, U.NOMEUSU, U.EMAIL " +
                            "  FROM TSIUSU U, DUAL D WHERE U.CODUSU = STP_GET_CODUSULOGADO"
                        ];

                        function post(url, corpo, ok, falha) {
                            var x = new XMLHttpRequest();
                            try { x.open("POST", url, true); } catch (e) { falha(); return; }
                            x.withCredentials = true;   /* leva o JSESSIONID da sessao do Sankhya */
                            x.setRequestHeader("Content-Type", "application/json");
                            x.onreadystatechange = function () {
                                if (x.readyState !== 4) { return; }
                                if (x.status < 200 || x.status >= 300) { falha(); return; }
                                var r = null;
                                try { r = JSON.parse(x.responseText); } catch (e) { falha(); return; }
                                ok(r);
                            };
                            x.onerror = function () { falha(); };
                            try { x.send(JSON.stringify(corpo)); } catch (e) { falha(); }
                        }

                        /* Le a primeira linha do retorno do DbExplorerSP.executeQuery. */
                        function primeiraLinha(r) {
                            if (!r || String(r.status) !== "1") { return null; }
                            var b = r.responseBody;
                            if (!b || !b.rows || !b.rows.length) { return null; }
                            return b.rows[0];
                        }

                        function consultarUsuario(iEnd, iSql, pronto) {
                            if (iEnd >= ENDPOINTS.length) { pronto(null); return; }
                            if (iSql >= SQLS.length) { consultarUsuario(iEnd + 1, 0, pronto); return; }

                            var url = ENDPOINTS[iEnd] +
                                "?serviceName=DbExplorerSP.executeQuery&outputType=json";
                            var corpo = {
                                serviceName: "DbExplorerSP.executeQuery",
                                requestBody: { sql: SQLS[iSql] }
                            };
                            var proximo = function () { consultarUsuario(iEnd, iSql + 1, pronto); };

                            post(url, corpo, function (r) {
                                var linha = primeiraLinha(r);
                                var cod = linha ? linha[0] : null;
                                if (cod === null || cod === "" || typeof cod === "undefined") { proximo(); return; }
                                pronto({
                                    codusu: String(cod),
                                    nome: (linha.length > 1 && linha[1] !== null) ? String(linha[1]) : "",
                                    email: (linha.length > 2 && linha[2] !== null) ? String(linha[2]) : ""
                                });
                            }, proximo);
                        }

                        function montarUrlComUsuario(u) {
                            if (!u) { return URL_APP; }
                            var q = P_CODUSU + "=" + encodeURIComponent(u.codusu);
                            if (u.nome) { q += "&" + P_NOMEUSU + "=" + encodeURIComponent(u.nome); }
                            if (u.email) { q += "&" + P_EMAIL + "=" + encodeURIComponent(u.email); }
                            return URL_APP + SEP_QS + q;
                        }

                        function mostrarUsuario(u) {
                            var alvo = el("usuario");
                            if (!alvo) { return; }
                            alvo.innerHTML = "";
                            var txt = u ? ("Usuario: " + (u.nome ? u.nome + " (" + u.codusu + ")" : u.codusu))
                                : "Usuario nao identificado";
                            alvo.appendChild(document.createTextNode(txt));
                        }

                        /* O app dentro do iframe tambem pode ouvir o usuario por postMessage:
                           window.addEventListener("message", function (e) {
                               if (e.data && e.data.tipo === "SANKHYA_USUARIO") { ... e.data.usuario ... }
                           });                                                                */
                        function avisarApp() {
                            if (!USUARIO) { return; }
                            var f = el("app");
                            if (!f || !f.contentWindow || !f.contentWindow.postMessage) { return; }
                            try {
                                f.contentWindow.postMessage(
                                    { tipo: "SANKHYA_USUARIO", usuario: USUARIO }, "*");
                            } catch (e) { /* origem diferente sem permissao: ignora */ }
                        }

                        function erroUsuario() {
                            if (timer) { clearTimeout(timer); timer = null; }
                            el("carregando").className = "camada oculto";
                            el("urlErro").innerHTML = "";
                            el("urlErro").appendChild(document.createTextNode(
                                "STP_GET_CODUSULOGADO nao retornou um usuario para esta sessao."));
                            el("linkNova").href = URL_APP;
                            el("erro").className = "camada";
                        }

                        function el(id) { return document.getElementById(id); }

                        function mostrarErro() {
                            if (timer) { clearTimeout(timer); timer = null; }
                            el("carregando").className = "camada oculto";
                            el("urlErro").innerHTML = "";
                            el("urlErro").appendChild(document.createTextNode(URL_APP));
                            el("linkNova").href = URL_APP;
                            el("erro").className = "camada";
                        }

                        function aoCarregar() {
                            /* o about:blank inicial tambem dispara load: nao conta como pronto */
                            var atual = "";
                            try { atual = el("app").getAttribute("src") || ""; } catch (e) { atual = ""; }
                            if (atual === "" || atual === "about:blank") { return; }
                            if (timer) { clearTimeout(timer); timer = null; }
                            el("carregando").className = "camada oculto";
                            el("erro").className = "camada oculto";
                            avisarApp();
                        }

                        function recarregar() {
                            el("erro").className = "camada oculto";
                            el("carregando").className = "camada";
                            armarTimeout();
                            var destino = montarUrlComUsuario(USUARIO);
                            var f = el("app");
                            f.src = "about:blank";
                            setTimeout(function () { f.src = destino; }, 60);
                        }

                        function armarTimeout() {
                            if (timer) { clearTimeout(timer); }
                            timer = setTimeout(mostrarErro, TIMEOUT_MS);
                        }

                        (function iniciar() {
                            var f = el("app");
                            if (f.addEventListener) {
                                f.addEventListener("load", aoCarregar, false);
                                f.addEventListener("error", mostrarErro, false);
                            } else {
                                f.onload = aoCarregar;
                            }
                            armarTimeout();

                            if (!PASSA_USUARIO) { return; }   /* iframe ja carregou pelo src do JSP */

                            consultarUsuario(0, 0, function (u) {
                                USUARIO = u;
                                mostrarUsuario(u);
                                if (!u && EXIGE_USUARIO) { erroUsuario(); return; }
                                el("app").src = montarUrlComUsuario(u);
                            });
                        })();
                    </script>
                </body>

                </html>