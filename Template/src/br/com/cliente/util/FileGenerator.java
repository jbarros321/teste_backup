package br.com.cliente.util;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

public class FileGenerator {
    private static final Charset CHARSET_ANSI = Charset.forName(Constants.CHARSET_ANSI);
    private static final String LINE_SEPARATOR = Constants.LINE_SEPARATOR;

    public static void gerarArquivo(Collection<String> linhas, String caminhoArquivo) throws IOException {
        Optional.ofNullable(linhas).orElseThrow(() -> new IllegalArgumentException("Coleção de linhas não pode ser nula"));
        Path path = Paths.get(caminhoArquivo);
        Optional.ofNullable(path.getParent()).ifPresent(p -> { try { Files.createDirectories(p); } catch (IOException ignored) {} });
        Files.write(path, linhas.stream().map(l -> l + LINE_SEPARATOR).collect(Collectors.joining()).getBytes(CHARSET_ANSI), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static String gerarNomeArquivo(String prefixo, String identificador) {
        String prefixoValido = Optional.ofNullable(prefixo).filter(p -> !StringUtils.isEmpty(p)).orElseThrow(() -> new IllegalArgumentException("Prefixo não pode ser nulo ou vazio"));
        String idLimpo = StringUtils.getNullAsEmpty(identificador);
        char[] idChars = new char[14];
        int pos = 0;
        for (int i = 0, len = idLimpo.length(); i < len && pos < 14; i++) {
            char c = idLimpo.charAt(i);
            if (c >= '0' && c <= '9') idChars[pos++] = c;
        }
        return String.format("%s_%s_%s.txt", prefixoValido, pos > 0 ? new String(idChars, 0, pos) : "", Formatter.formatarTimestamp(TimeUtils.getNow())); }
}
