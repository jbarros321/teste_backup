package br.com.denver.tsl.util;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

public class TSLFormatter {
    private static final Pattern PATTERN_NON_DIGITS = Pattern.compile("[^0-9]");
    private static final TimeZone TIMEZONE_BRASIL = TimeZone.getTimeZone("America/Sao_Paulo");
    private static final ConcurrentHashMap<Integer, String> ESPACOS_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, char[]> ESPACOS_CHAR_CACHE = new ConcurrentHashMap<>();

    private static String cacheEspacos(int t) {
        return ESPACOS_CACHE.computeIfAbsent(t, tamanho -> {
            char[] espacos = new char[tamanho];
            Arrays.fill(espacos, ' ');
            return new String(espacos);
        });
    }

    private static char[] cacheEspacosChar(int t) {
        return ESPACOS_CHAR_CACHE.computeIfAbsent(t, tamanho -> {
            char[] espacos = new char[tamanho];
            Arrays.fill(espacos, ' ');
            return espacos;
        });
    }

    private static String preencherDireita(String texto, int tamanho) {
        if (StringUtils.isEmpty(texto)) return cacheEspacos(tamanho);
        int len = Math.min(texto.length(), tamanho);
        char[] resultado = new char[tamanho];
        texto.getChars(0, len, resultado, 0);
        Arrays.fill(resultado, len, tamanho, ' ');
        return new String(resultado);
    }

    private static String preencherEsquerda(String texto, int tamanho) {
        if (StringUtils.isEmpty(texto)) return cacheEspacos(tamanho);
        int len = Math.min(texto.length(), tamanho);
        int offset = tamanho - len;
        char[] resultado = new char[tamanho];
        Arrays.fill(resultado, 0, offset, ' ');
        texto.getChars(Math.max(0, texto.length() - len), texto.length(), resultado, offset);
        return new String(resultado);
    }

    private static String preencherEsquerdaZeros(String texto, int tamanho) {
        if (StringUtils.isEmpty(texto)) {
            char[] zeros = new char[tamanho];
            Arrays.fill(zeros, '0');
            return new String(zeros);
        }
        int len = Math.min(texto.length(), tamanho);
        int offset = tamanho - len;
        char[] resultado = new char[tamanho];
        Arrays.fill(resultado, 0, offset, '0');
        texto.getChars(Math.max(0, texto.length() - len), texto.length(), resultado, offset);
        return new String(resultado);
    }

    private static String formatarNumero(String valor, int tamanhoTotal, int casasDecimais) {
        int tamanhoNumerico = tamanhoTotal - 1;
        if (StringUtils.isEmpty(valor)) {
            char[] resultado = new char[tamanhoTotal];
            Arrays.fill(resultado, 0, tamanhoNumerico, '0');
            resultado[tamanhoNumerico] = ' ';
            return new String(resultado);
        }
        try {
            double num = Double.parseDouble(valor.replace(',', '.'));
            int tamanhoInteiro = tamanhoNumerico - casasDecimais - 1;
            String numStr = String.format(Locale.US, "%0" + tamanhoInteiro + "." + casasDecimais + "f", num);
            int ponto = numStr.indexOf('.');
            String numFormatado = ponto > 0 ? new StringBuilder(tamanhoNumerico).append(numStr, 0, ponto).append(',').append(numStr, ponto + 1, numStr.length()).toString() : numStr;
            String numPreenchido = preencherEsquerdaZeros(numFormatado, tamanhoNumerico);
            if (numPreenchido.length() > tamanhoNumerico) {
                numPreenchido = numPreenchido.substring(numPreenchido.length() - tamanhoNumerico);
            }
            char[] resultado = new char[tamanhoTotal];
            int len = Math.min(numPreenchido.length(), tamanhoNumerico);
            numPreenchido.getChars(0, len, resultado, 0);
            if (len < tamanhoNumerico) {
                Arrays.fill(resultado, len, tamanhoNumerico, '0');
            }
            resultado[tamanhoNumerico] = ' ';
            return new String(resultado);
        } catch (Exception e) {
            char[] resultado = new char[tamanhoTotal];
            Arrays.fill(resultado, 0, tamanhoNumerico, '0');
            resultado[tamanhoNumerico] = ' ';
            return new String(resultado);
        }
    }

    private static String formatarNumerico(String valor, int tamanho) {
        int tamanhoNumerico = tamanho - 1;
        if (StringUtils.isEmpty(valor)) {
            char[] resultado = new char[tamanho];
            Arrays.fill(resultado, 0, tamanhoNumerico, '0');
            resultado[tamanhoNumerico] = ' ';
            return new String(resultado);
        }
        String numLimpo = PATTERN_NON_DIGITS.matcher(valor).replaceAll("");
        if (numLimpo.length() > tamanhoNumerico) {
            numLimpo = numLimpo.substring(numLimpo.length() - tamanhoNumerico);
        }
        String numPreenchido = preencherEsquerdaZeros(numLimpo, tamanhoNumerico);
        if (numPreenchido.length() > tamanhoNumerico) {
            numPreenchido = numPreenchido.substring(numPreenchido.length() - tamanhoNumerico);
        }
        char[] resultado = new char[tamanho];
        int len = Math.min(numPreenchido.length(), tamanhoNumerico);
        numPreenchido.getChars(0, len, resultado, 0);
        if (len < tamanhoNumerico) {
            Arrays.fill(resultado, len, tamanhoNumerico, '0');
        }
        resultado[tamanhoNumerico] = ' ';
        return new String(resultado);
    }

    public static String formatarCnpj(String cnpj) {
        if (StringUtils.isEmpty(cnpj)) return cacheEspacos(15);
        String limpo = PATTERN_NON_DIGITS.matcher(cnpj).replaceAll("");
        String cnpjLimpo = limpo.substring(0, Math.min(limpo.length(), 14));
        char[] resultado = new char[15];
        int len = cnpjLimpo.length();
        cnpjLimpo.getChars(0, len, resultado, 0);
        Arrays.fill(resultado, len, 15, ' ');
        return new String(resultado);
    }

    public static String formatarNotaFiscal(String nf) { return formatarNumerico(nf, 13); }
    public static String formatarItem(String item) { return formatarNumerico(item, 7); }
    public static String formatarPalete(String palete) { return preencherDireita(palete, 26); }
    public static String formatarCodigoProduto(String codigo) { return preencherDireita(codigo, 51); }
    public static String formatarIdentificadorCaixa(String identificador) { return preencherDireita(identificador, 31); }
    public static String formatarPeso(String peso) { return formatarPeso(peso, 18); }
    public static String formatarPeso(String peso, int tamanho) { return formatarNumero(peso, tamanho, 2); }
    public static String formatarData(Date data) {
        if (data == null) return cacheEspacos(11);
        Timestamp ts = data instanceof Timestamp ? (Timestamp) data : new Timestamp(data.getTime());
        Timestamp tsBrasil = converterParaTimezoneBrasil(ts);
        return StringUtils.formatTimestamp(tsBrasil, "dd/MM/yyyy") + " ";
    }

    private static Timestamp converterParaTimezoneBrasil(Timestamp ts) {
        if (ts == null) return null;
        long offsetBrasil = TIMEZONE_BRASIL.getOffset(ts.getTime());
        long offsetSistema = TimeZone.getDefault().getOffset(ts.getTime());
        return new Timestamp(ts.getTime() + offsetBrasil - offsetSistema);
    }
    public static String formatarLote(String lote) { return preencherDireita(lote, 26); }
    public static String formatarInfoComplementar(String info) {
        if (StringUtils.isEmpty(info)) return cacheEspacos(401);
        return preencherDireita(info.substring(0, Math.min(info.length(), 401)), 401);
    }
    public static String formatarValorUnitario(String valor) { return formatarNumero(valor, 9, 2); }
    public static String formatarOrdemFrete(String ordem) { return formatarNumerico(ordem, 21); }
    public static String formatarNumeroPedido(String pedido) { return formatarNumerico(pedido, 13); }
    public static String formatarQuantidade(String quantidade) { return formatarNumero(quantidade, 17, 2); }
    public static String formatarCnpjCliente(String cnpj) { return formatarCnpj(cnpj); }
    public static String formatarTimestamp(Date data) {
        if (data == null) return "";
        Timestamp ts = data instanceof Timestamp ? (Timestamp) data : new Timestamp(data.getTime());
        Timestamp tsBrasil = converterParaTimezoneBrasil(ts);
        return StringUtils.formatTimestamp(tsBrasil, "yyyyMMddHHmmss");
    }
    public static char[] obterEspacosChar(int tamanho) { return cacheEspacosChar(tamanho); }
}
