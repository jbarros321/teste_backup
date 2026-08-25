<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%@ taglib prefix="snk" uri="/WEB-INF/tld/sankhyaUtil.tld" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head><meta charset="UTF-8"><title>Canario 3 - ambiente</title><snk:load /></head>
<body style="font-family:sans-serif;padding:24px">
<h2>Canario 3 &mdash; ambiente do servidor</h2>
<table border="1" cellpadding="6" style="border-collapse:collapse">
<tr><td>Servidor</td><td><%= application.getServerInfo() %></td></tr>
<tr><td>Versao de Servlet</td><td><%= application.getMajorVersion() %>.<%= application.getMinorVersion() %></td></tr>
<tr><td>Java</td><td><%= System.getProperty("java.version") %> (<%= System.getProperty("java.vendor") %>)</td></tr>
<tr><td>file.encoding</td><td><%= System.getProperty("file.encoding") %></td></tr>
<tr><td>Acentuacao</td><td>ção  ç  á  é  &mdash;  ✓</td></tr>
<tr><td>Caminho real desta pagina</td><td><%= application.getRealPath(request.getServletPath()) %></td></tr>
</table>
<p>Se este abre e a tela de chamados nao, me mande o conteudo desta tabela.</p>
</body>
</html>
