<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
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
    boolean BARRA    = true;   /* barra superior com recarregar / abrir em nova aba */
    boolean PASSA_QS = true;   /* repassa a query string desta tela para o iframe   */

    /* Repassa parametros recebidos pela tela do Sankhya (ex.: ?id=123) */
    String qs = request.getQueryString();
    String urlFinal = URL_APP;
    if (PASSA_QS && qs != null && qs.length() > 0) {
        String sep = (URL_APP.indexOf(63) >= 0) ? "&" : "?";   /* 63 = caractere ? */
        urlFinal = URL_APP + sep + qs;
    }
    String urlJs = urlFinal.replace("\\", "\\\\").replace("\"", "\\\"");
%>
<!DOCTYPE html>
<html lang="pt-BR">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= TITULO %></title>
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
            <span class="titulo"><%= TITULO %></span>
            <button type="button" onclick="recarregar()">Recarregar</button>
            <a href="<%= urlFinal %>" target="_blank" rel="noopener">Abrir em nova aba</a>
        </div>
        <% } %>

        <div id="palco">
            <iframe id="app" src="<%= urlFinal %>" title="<%= TITULO %>"
                allow="clipboard-read; clipboard-write; fullscreen; camera; microphone; geolocation"
                allowfullscreen referrerpolicy="no-referrer-when-downgrade"></iframe>

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
                    <a id="linkNova" href="#" target="_blank" rel="noopener">Abrir em nova aba</a>
                    <button type="button" class="sec" onclick="recarregar()">Tentar de novo</button>
                </div>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        var URL_APP = "<%= urlJs %>";
        var TIMEOUT_MS = 15000;
        var timer = null;

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
            if (timer) { clearTimeout(timer); timer = null; }
            el("carregando").className = "camada oculto";
            el("erro").className = "camada oculto";
        }

        function recarregar() {
            el("erro").className = "camada oculto";
            el("carregando").className = "camada";
            armarTimeout();
            var f = el("app");
            f.src = "about:blank";
            setTimeout(function () { f.src = URL_APP; }, 60);
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
        })();
    </script>
</body>

</html>
