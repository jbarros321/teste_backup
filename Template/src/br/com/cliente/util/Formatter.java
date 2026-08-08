package br.com.cliente.util;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

public class Formatter {
    private static final Pattern PATTERN_NON_DIGITS = Pattern.compile("[^0-9]");
    private static final TimeZone TIMEZONE_BRASIL = TimeZone.getTimeZone("America/Sao_Paulo");
    private static final ConcurrentHashMap<Integer, String> ESPACOS_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, char[]> ESPACOS_CHAR_CACHE = new ConcurrentHashMap<>();

    private static String cacheEspacos(int t) { return ESPACOS_CACHE.computeIfAbsent(t, tamanho -> { char[] espacos = new char[tamanho]; Arrays.fill(espacos, ' '); return new String(espacos); }); }
    private static char[] cacheEspacosChar(int t) { return ESPACOS_CHAR_CACHE.computeIfAbsent(t, tamanho -> { char[] espacos = new char[tamanho]; Arrays.fill(espacos, ' '); return espacos; }); }

    private static String preencherDireita(String texto, int tamanho) {
        if (StringUtils.isEmpty(texto)) return cacheEspacos(tamanho);
        char[] resultado = new char[tamanho];
        int len = Math.min(texto.length(), tamanho);
        texto.getChars(0, len, resultado, 0);
        Arrays.fill(resultado, len, tamanho, ' ');
        return new String(resultado);
    }

    private static String preencherEsquerda(String texto, int tamanho) {
        if (StringUtils.isEmpty(texto)) return cacheEspacos(tamanho);
        char[] resultado = new char[tamanho];
        int len = Math.min(texto.length(), tamanho);
        int offset = tamanho - len;
        Arrays.fill(resultado, 0, offset, ' ');
        texto.getChars(Math.max(0, texto.length() - len), texto.length(), resultado, offset);
        return new String(resultado);
    }

    private static String preencherEsquerdaZeros(String texto, int tamanho) {
        char[] resultado = new char[tamanho];
        if (StringUtils.isEmpty(texto)) {
            Arrays.fill(resultado, '0');
            return new String(resultado);
        }
        int len = Math.min(texto.length(), tamanho);
        int offset = tamanho - len;
        Arrays.fill(resultado, 0, offset, '0');
        texto.getChars(Math.max(0, texto.length() - len), texto.length(), resultado, offset);
        return new String(resultado);
    }

    private static String formatarNumero(String valor, int tamanhoTotal, int casasDecimais) {
        int tamanhoNumerico = tamanhoTotal - 1;
        return Optional.ofNullable(valor).filter(v -> !StringUtils.isEmpty(v)).map(v -> {
            try {
                double num = Double.parseDouble(v.replace(',', '.'));
                int tamanhoInteiro = tamanhoNumerico - casasDecimais - 1;
                String numStr = String.format(Locale.US, "%0" + tamanhoInteiro + "." + casasDecimais + "f", num);
                int ponto = numStr.indexOf('.');
                String numFormatado = ponto > 0 ? new StringBuilder(tamanhoNumerico).append(numStr, 0, ponto).append(',').append(numStr, ponto + 1, numStr.length()).toString() : numStr;
                String numPreenchido = preencherEsquerdaZeros(numFormatado, tamanhoNumerico);
                numPreenchido = numPreenchido.length() > tamanhoNumerico ? numPreenchido.substring(numPreenchido.length() - tamanhoNumerico) : numPreenchido;
                return criarResultadoNumerico(numPreenchido, tamanhoTotal, tamanhoNumerico);
            } catch (Exception e) { return criarResultadoNumerico("", tamanhoTotal, tamanhoNumerico); }
        }).orElseGet(() -> criarResultadoNumerico("", tamanhoTotal, tamanhoNumerico));
    }

    private static String criarResultadoNumerico(String numPreenchido, int tamanhoTotal, int tamanhoNumerico) {
        char[] resultado = new char[tamanhoTotal];
        int len = Math.min(numPreenchido.length(), tamanhoNumerico);
        if (len > 0) numPreenchido.getChars(0, len, resultado, 0);
        if (len < tamanhoNumerico) Arrays.fill(resultado, len, tamanhoNumerico, '0');
        resultado[tamanhoNumerico] = ' ';
        return new String(resultado);
    }

    private static String formatarNumerico(String valor, int tamanho) {
        int tamanhoNumerico = tamanho - 1;
        return Optional.ofNullable(valor).filter(v -> !StringUtils.isEmpty(v)).map(v -> {
            String numLimpo = PATTERN_NON_DIGITS.matcher(v).replaceAll("");
            numLimpo = numLimpo.length() > tamanhoNumerico ? numLimpo.substring(numLimpo.length() - tamanhoNumerico) : numLimpo;
            String numPreenchido = preencherEsquerdaZeros(numLimpo, tamanhoNumerico);
            numPreenchido = numPreenchido.length() > tamanhoNumerico ? numPreenchido.substring(numPreenchido.length() - tamanhoNumerico) : numPreenchido;
            return criarResultadoNumerico(numPreenchido, tamanho, tamanhoNumerico);
        }).orElseGet(() -> criarResultadoNumerico("", tamanho, tamanhoNumerico));
    }

    public static String formatarCnpj(String cnpj) {
        return Optional.ofNullable(cnpj).filter(c -> !StringUtils.isEmpty(c)).map(c -> {
            String limpo = PATTERN_NON_DIGITS.matcher(c).replaceAll("");
            String cnpjLimpo = limpo.substring(0, Math.min(limpo.length(), 14));
            char[] resultado = new char[15];
            int len = cnpjLimpo.length();
            cnpjLimpo.getChars(0, len, resultado, 0);
            Arrays.fill(resultado, len, 15, ' ');
            return new String(resultado);
        }).orElseGet(() -> cacheEspacos(15));
    }

    public static String formatarNotaFiscal(String nf) { return formatarNumerico(nf, 13); }
    public static String formatarItem(String item) { return formatarNumerico(item, 7); }
    public static String formatarTexto(String texto, int tamanho) { return preencherDireita(texto, tamanho); }
    public static String formatarPeso(String peso) { return formatarPeso(peso, 18); }
    public static String formatarPeso(String peso, int tamanho) { return formatarNumero(peso, tamanho, 2); }
    public static String formatarData(Date data) {
        return Optional.ofNullable(data).map(d -> {
            Timestamp ts = d instanceof Timestamp ? (Timestamp) d : new Timestamp(d.getTime());
            Timestamp tsBrasil = converterParaTimezoneBrasil(ts);
            return StringUtils.formatTimestamp(tsBrasil, "dd/MM/yyyy") + " ";
        }).orElseGet(() -> cacheEspacos(11));
    }

    private static Timestamp converterParaTimezoneBrasil(Timestamp ts) { return ts == null ? null : new Timestamp(ts.getTime() + TIMEZONE_BRASIL.getOffset(ts.getTime()) - TimeZone.getDefault().getOffset(ts.getTime())); }

    public static String formatarLote(String lote) { return preencherDireita(lote, 26); }
    public static String formatarValorUnitario(String valor) { return formatarNumero(valor, 9, 2); }
    public static String formatarQuantidade(String quantidade) { return formatarNumero(quantidade, 17, 2); }
    public static String formatarTimestamp(Date data) {
        return Optional.ofNullable(data).map(d -> {
            Timestamp ts = d instanceof Timestamp ? (Timestamp) d : new Timestamp(d.getTime());
            return StringUtils.formatTimestamp(converterParaTimezoneBrasil(ts), "yyyyMMddHHmmss");
        }).orElse("");
    }
    public static char[] obterEspacosChar(int tamanho) { return cacheEspacosChar(tamanho); }
}
