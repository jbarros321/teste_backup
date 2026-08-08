package br.com.monteccer.util;

public class DownloadHelper {

    private static String escaparParaJavaScript(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("'", "\\'")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("</script>", "<\\/script>");
    }

    private static String escaparParaHTML(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    private static String gerarScriptDownload(String nomeArquivo, String contentType, String labelArquivo) {
        if (nomeArquivo == null || nomeArquivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do arquivo não pode ser nulo ou vazio");
        }

        String nomeArquivoEscapado = nomeArquivo.replace(" ", "%20");
        String urlDownload = "/mge/downloadTempFile.mge?fileName=" + nomeArquivoEscapado;
        if (contentType != null && !contentType.trim().isEmpty()) {
            urlDownload += "&contentType=" + contentType;
        }

        String urlEscapada = escaparParaJavaScript(urlDownload);

        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<iframe id=\"downloadIframe_").append(System.currentTimeMillis()).append("\" src=\"").append(escaparParaHTML(urlDownload)).append("\" style=\"display:none;width:0;height:0;border:none;position:absolute;left:-9999px;\"></iframe>");

        htmlBuilder.append("<script type=\"text/javascript\">");
        htmlBuilder.append("(function(){");
        htmlBuilder.append("var url=\"").append(urlEscapada).append("\";");
        htmlBuilder.append("try{");
        htmlBuilder.append("var skApp=null;");
        htmlBuilder.append("if(typeof SkApplicationInstance!==\"undefined\"&&SkApplicationInstance&&typeof SkApplicationInstance.openWindow===\"function\"){");
        htmlBuilder.append("skApp=SkApplicationInstance;");
        htmlBuilder.append("}else if(typeof window!==\"undefined\"&&window.SkApplicationInstance&&typeof window.SkApplicationInstance.openWindow===\"function\"){");
        htmlBuilder.append("skApp=window.SkApplicationInstance;");
        htmlBuilder.append("}else if(typeof window!==\"undefined\"&&window.parent&&window.parent!==window&&typeof window.parent.SkApplicationInstance!==\"undefined\"&&window.parent.SkApplicationInstance&&typeof window.parent.SkApplicationInstance.openWindow===\"function\"){");
        htmlBuilder.append("skApp=window.parent.SkApplicationInstance;");
        htmlBuilder.append("}else if(typeof window!==\"undefined\"&&window.top&&window.top!==window&&typeof window.top.SkApplicationInstance!==\"undefined\"&&window.top.SkApplicationInstance&&typeof window.top.SkApplicationInstance.openWindow===\"function\"){");
        htmlBuilder.append("skApp=window.top.SkApplicationInstance;");
        htmlBuilder.append("}");
        htmlBuilder.append("if(skApp){");
        htmlBuilder.append("skApp.openWindow(url,\"_blank\");");
        htmlBuilder.append("}");
        htmlBuilder.append("}catch(e){}");
        htmlBuilder.append("})();");
        htmlBuilder.append("</script>");

        htmlBuilder.append("<div style=\"margin:10px 0;padding:10px;border:1px solid #ccc;background-color:#f9f9f9;border-radius:4px;text-align:center;\">");
        htmlBuilder.append("<p style=\"margin:0 0 10px 0;font-weight:bold;\">").append(escaparParaHTML(labelArquivo)).append(": <strong>").append(escaparParaHTML(nomeArquivo)).append("</strong></p>");
        htmlBuilder.append("<p style=\"margin:0 0 10px 0;color:#666;font-size:12px;\">Clique abaixo se o download não iniciar automaticamente:</p>");
        htmlBuilder.append("<a href=\"").append(escaparParaHTML(urlDownload)).append("\" target=\"_blank\" style=\"display:inline-block;padding:8px 16px;background-color:#4CAF50 !important;color:white !important;text-decoration:none;border-radius:4px;font-weight:bold;\">");
        htmlBuilder.append("Clique aqui para baixar manualmente");
        htmlBuilder.append("</a>");
        htmlBuilder.append("</div>");

        return htmlBuilder.toString();
    }

    public static String gerarScriptDownload(String nomeArquivo) {
        return gerarScriptDownload(nomeArquivo, "text/plain;charset=Windows-1252", "Download do arquivo");
    }

    public static String gerarScriptDownloadZip(String nomeArquivo) {
        return gerarScriptDownload(nomeArquivo, "application/zip", "Download do arquivo ZIP");
    }
}
