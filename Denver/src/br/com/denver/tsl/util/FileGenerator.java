package br.com.denver.tsl.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import com.sankhya.util.StringUtils;

public class FileGenerator {
    private static final Charset CHARSET_ANSI = Charset.forName(TSLConstants.CHARSET_ANSI);

    public static void gerarArquivo(Collection<String> linhas, String caminhoArquivo) throws IOException {
        Path path = Paths.get(caminhoArquivo);
        Path parent = path.getParent();
        if (parent != null) Files.createDirectories(parent);
        byte[] conteudo = linhas.stream().map(l -> l + TSLConstants.LINE_SEPARATOR).collect(Collectors.joining()).getBytes(CHARSET_ANSI);
        Files.write(path, conteudo, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static String gerarNomeArquivo(String interfaceTipo, String cnpj) {
        String cnpjLimpo = StringUtils.getNullAsEmpty(cnpj);
        char[] cnpjChars = new char[14];
        int pos = 0;
        for (int i = 0, len = cnpjLimpo.length(); i < len && pos < 14; i++) {
            char c = cnpjLimpo.charAt(i);
            if (c >= '0' && c <= '9') cnpjChars[pos++] = c;
        }
        String cnpjFinal = pos > 0 ? new String(cnpjChars, 0, pos) : "";
        return String.format("%s_%s_%s.txt", interfaceTipo, cnpjFinal, TSLFormatter.formatarTimestamp(new Date()));
    }
}
