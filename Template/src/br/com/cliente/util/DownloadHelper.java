package br.com.cliente.util;
import com.sankhya.util.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadHelper {
    private static final int BUFFER_SIZE = Constants.BUFFER_SIZE;

    private static String validarECriarDiretorioTemporario() throws IOException {
        String tmpDir = Optional.ofNullable(System.getProperty("java.io.tmpdir")).filter(d -> !StringUtils.isEmpty(d)).orElseThrow(() -> new IOException("Diretório temporário não configurado"));
        File dirTmp = new File(tmpDir);
        Optional.of(dirTmp).filter(d -> d.exists() || d.mkdirs()).filter(d -> d.isDirectory() && d.canWrite()).orElseThrow(() -> new IOException("Diretório temporário inválido ou sem permissão de escrita: " + tmpDir));
        return tmpDir;
    }

    public static String salvarArquivoParaDownload(String caminhoArquivoOrigem) throws IOException {
        String caminhoValido = Optional.ofNullable(caminhoArquivoOrigem).filter(c -> !StringUtils.isEmpty(c)).orElseThrow(() -> new IllegalArgumentException("Caminho do arquivo não pode ser nulo ou vazio"));
        File arquivoOrigem = new File(caminhoValido);
        Optional.of(arquivoOrigem).filter(File::exists).orElseThrow(() -> new IOException("Arquivo não encontrado: " + caminhoValido));
        long tamanhoOrigem = arquivoOrigem.length();
        String nomeArquivo = arquivoOrigem.getName();
        String tmpDir = validarECriarDiretorioTemporario();
        String caminhoDestino = tmpDir + File.separator + nomeArquivo;
        File arquivoDestino = new File(caminhoDestino);
        if (arquivoOrigem.getAbsolutePath().equals(arquivoDestino.getAbsolutePath())) return nomeArquivo;
        arquivoDestino.delete();
        try (FileInputStream fis = new FileInputStream(arquivoOrigem); FileOutputStream fos = new FileOutputStream(arquivoDestino)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) fos.write(buffer, 0, bytesRead);
            fos.flush();
        }
        Optional.of(arquivoDestino).filter(File::exists).filter(a -> a.length() == tamanhoOrigem).orElseThrow(() -> new IOException("Arquivo não foi criado corretamente no destino: " + caminhoDestino));
        return nomeArquivo;
    }

    private static String escaparParaJavaScript(String str) { return StringUtils.getNullAsEmpty(str).replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t").replace("</script>", "<\\/script>"); }
    private static String escaparParaHTML(String str) { return StringUtils.getNullAsEmpty(str).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }

    private static String gerarScriptDownload(String nomeArquivo, String contentType, String labelArquivo) {
        String nomeValido = Optional.ofNullable(nomeArquivo).filter(n -> !StringUtils.isEmpty(n)).orElseThrow(() -> new IllegalArgumentException("Nome do arquivo não pode ser nulo ou vazio"));
        String nomeArquivoEscapado = nomeValido.replace(" ", "%20");
        String urlDownload = "/mge/downloadTempFile.mge?fileName=" + nomeArquivoEscapado + Optional.ofNullable(contentType).filter(c -> StringUtils.isNotEmpty(c)).map(c -> "&contentType=" + c).orElse("");
        String urlEscapada = escaparParaJavaScript(urlDownload);
        StringBuilder htmlBuilder = new StringBuilder(500);
        htmlBuilder.append("<iframe id=\"downloadIframe_").append(System.currentTimeMillis()).append("\" src=\"").append(escaparParaHTML(urlDownload)).append("\" style=\"display:none;width:0;height:0;border:none;position:absolute;left:-9999px;\"></iframe>");
        htmlBuilder.append("<script type=\"text/javascript\">(function(){var url=\"").append(urlEscapada).append("\";try{var skApp=null;if(typeof SkApplicationInstance!==\"undefined\"&&SkApplicationInstance&&typeof SkApplicationInstance.openWindow===\"function\"){skApp=SkApplicationInstance;}else if(typeof window!==\"undefined\"&&window.SkApplicationInstance&&typeof window.SkApplicationInstance.openWindow===\"function\"){skApp=window.SkApplicationInstance;}else if(typeof window!==\"undefined\"&&window.parent&&window.parent!==window&&typeof window.parent.SkApplicationInstance!==\"undefined\"&&window.parent.SkApplicationInstance&&typeof window.parent.SkApplicationInstance.openWindow===\"function\"){skApp=window.parent.SkApplicationInstance;}else if(typeof window!==\"undefined\"&&window.top&&window.top!==window&&typeof window.top.SkApplicationInstance!==\"undefined\"&&window.top.SkApplicationInstance&&typeof window.top.SkApplicationInstance.openWindow===\"function\"){skApp=window.top.SkApplicationInstance;}if(skApp){skApp.openWindow(url,\"_blank\");}}catch(e){}})();</script>");
        htmlBuilder.append("<div style=\"margin:10px 0;padding:10px;border:1px solid #ccc;background-color:#f9f9f9;border-radius:4px;text-align:center;\">");
        htmlBuilder.append("<p style=\"margin:0 0 10px 0;font-weight:bold;\">").append(escaparParaHTML(labelArquivo)).append(": <strong>").append(escaparParaHTML(nomeValido)).append("</strong></p>");
        htmlBuilder.append("<p style=\"margin:0 0 10px 0;color:#666;font-size:12px;\">Clique abaixo se o download não iniciar automaticamente:</p>");
        htmlBuilder.append("<a href=\"").append(escaparParaHTML(urlDownload)).append("\" target=\"_blank\" style=\"display:inline-block;padding:8px 16px;background-color:#4CAF50 !important;color:white !important;text-decoration:none;border-radius:4px;font-weight:bold;\">");
        htmlBuilder.append("Clique aqui para baixar manualmente</a></div>");
        return htmlBuilder.toString();
    }

    public static String gerarScriptDownload(String nomeArquivo) { return gerarScriptDownload(nomeArquivo, "text/plain;charset=Windows-1252", "Download do arquivo"); }
    public static String prepararDownload(String caminhoArquivoOrigem) throws IOException { return gerarScriptDownload(salvarArquivoParaDownload(caminhoArquivoOrigem)); }

    public static String criarZip(Collection<String> arquivos, String nomeZip) throws IOException {
        Collection<String> arquivosValidos = Optional.ofNullable(arquivos).filter(a -> !a.isEmpty()).orElseThrow(() -> new IllegalArgumentException("Coleção de arquivos não pode ser nula ou vazia"));
        String nomeZipValido = Optional.ofNullable(nomeZip).filter(n -> !StringUtils.isEmpty(n)).orElseThrow(() -> new IllegalArgumentException("Nome do arquivo ZIP não pode ser nulo ou vazio"));
        nomeZipValido = nomeZipValido.toLowerCase().endsWith(".zip") ? nomeZipValido : nomeZipValido + ".zip";
        String caminhoZip = validarECriarDiretorioTemporario() + File.separator + nomeZipValido;
        File arquivoZip = new File(caminhoZip);
        arquivoZip.delete();
        int[] arquivosAdicionados = {0};
        try (FileOutputStream fos = new FileOutputStream(arquivoZip); ZipOutputStream zos = new ZipOutputStream(fos)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for (String caminhoArquivo : arquivosValidos) {
                if (StringUtils.isEmpty(caminhoArquivo)) continue;
                File arquivo = new File(caminhoArquivo);
                if (!arquivo.exists() || !arquivo.isFile()) continue;
                String nomeArquivo = Optional.ofNullable(arquivo.getName()).filter(n -> !StringUtils.isEmpty(n)).orElseGet(() -> {
                    int lastSeparator = caminhoArquivo.lastIndexOf(File.separator);
                    return lastSeparator >= 0 && lastSeparator < caminhoArquivo.length() - 1 ? caminhoArquivo.substring(lastSeparator + 1) : caminhoArquivo;
                });
                if (StringUtils.isEmpty(nomeArquivo)) continue;
                try (FileInputStream fis = new FileInputStream(arquivo)) {
                    zos.putNextEntry(new ZipEntry(nomeArquivo));
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) zos.write(buffer, 0, bytesRead);
                    zos.closeEntry();
                    arquivosAdicionados[0]++;
                }
            }
            zos.finish();
            zos.flush();
        }
        if (arquivosAdicionados[0] == 0) { arquivoZip.delete(); throw new IOException("Nenhum arquivo válido foi encontrado para compactar no ZIP"); }
        Optional.of(arquivoZip).filter(File::exists).orElseThrow(() -> new IOException("ZIP não foi criado: " + caminhoZip));
        return nomeZipValido;
    }

    public static String gerarScriptDownloadZip(String nomeArquivo) { return gerarScriptDownload(nomeArquivo, "application/zip", "Download do arquivo ZIP"); }
}
