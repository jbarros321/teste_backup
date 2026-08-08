package br.com.petkids.neogrid.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.List;

public class FileGenerator {

    private static final int BUFFER_SIZE = 8192;
    private static final Charset CHARSET_ANSI = Charset.forName(NeogridConstants.CHARSET_ANSI);
    private static final byte[] LINE_SEPARATOR_BYTES = NeogridConstants.LINE_SEPARATOR.getBytes(CHARSET_ANSI);

    static {
        if (CHARSET_ANSI == null) {
            throw new RuntimeException("Charset " + NeogridConstants.CHARSET_ANSI + " não está disponível no sistema!");
        }
        try {
            CHARSET_ANSI.newEncoder();
        } catch (Exception e) {
            throw new RuntimeException("Charset " + NeogridConstants.CHARSET_ANSI + " não suporta encoding: " + e.getMessage(), e);
        }
    }

    public static void gerarArquivo(List<String> linhas, String caminhoArquivo) throws IOException {
        if (linhas == null) {
            throw new IllegalArgumentException("Lista de linhas não pode ser nula");
        }

        File arquivo = new File(caminhoArquivo);
        File diretorio = arquivo.getParentFile();
        if (diretorio != null && !diretorio.exists()) {
            boolean criado = diretorio.mkdirs();
            if (!criado && !diretorio.exists()) {
                throw new IOException("Não foi possível criar o diretório: " + diretorio.getAbsolutePath());
            }
        }

        try (FileOutputStream fos = new FileOutputStream(arquivo);
             BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE)) {

            for (String linha : linhas) {
                if (linha != null && !linha.isEmpty()) {
                    while (linha.length() > 0 && Character.isWhitespace(linha.charAt(0))) {
                        linha = linha.substring(1);
                    }
                    while (linha.length() > 0 && Character.isWhitespace(linha.charAt(linha.length() - 1))) {
                        linha = linha.substring(0, linha.length() - 1);
                    }
                    if (!linha.isEmpty()) {
                        byte[] linhaBytes = linha.getBytes(CHARSET_ANSI);
                        bos.write(linhaBytes);
                        bos.write(LINE_SEPARATOR_BYTES);
                    }
                }
            }

            bos.flush();
            fos.getFD().sync();
        }

        if (!arquivo.exists() || arquivo.length() == 0) {
            throw new IOException("Falha ao gerar arquivo: arquivo não foi criado ou está vazio - " + caminhoArquivo);
        }
    }

    public static String gerarNomeArquivo(String mascaraDocumento, String cnpjFilial, String cnpjIndustria, Timestamp dataHora) {
        String timestamp = NeogridFormatter.formatarDataHora(dataHora);
        String cnpjFilialFormatado = NeogridFormatter.formatarCnpjCpf(cnpjFilial);
        if (cnpjIndustria != null && !cnpjIndustria.isEmpty()) {
            return String.format("%s_%s_%s_%s.txt", mascaraDocumento, cnpjFilialFormatado, NeogridFormatter.formatarCnpjCpf(cnpjIndustria), timestamp);
        }
        return String.format("%s_%s_%s.txt", mascaraDocumento, cnpjFilialFormatado, timestamp);
    }

    public static boolean validarCaminho(String caminhoArquivo) {
        if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) return false;
        try {
            File diretorio = new File(caminhoArquivo).getParentFile();
            return diretorio != null && (diretorio.exists() || diretorio.mkdirs());
        } catch (Exception e) {
            return false;
        }
    }

}
