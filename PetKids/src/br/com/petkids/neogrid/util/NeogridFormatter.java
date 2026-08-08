package br.com.petkids.neogrid.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.sankhya.util.StringUtils;

public class NeogridFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(NeogridConstants.DECIMAL_FORMAT, new DecimalFormatSymbols(Locale.US));
    private static final ThreadLocal<java.text.SimpleDateFormat> DATE_FORMATTER = ThreadLocal.withInitial(() -> new java.text.SimpleDateFormat("yyyyMMdd"));
    private static final ThreadLocal<java.text.SimpleDateFormat> DATETIME_FORMATTER = ThreadLocal.withInitial(() -> new java.text.SimpleDateFormat("yyyyMMddHHmm"));
    private static final ConcurrentHashMap<Integer, DecimalFormat> DECIMAL_FORMAT_CACHE = new ConcurrentHashMap<>();
    private static final Pattern PATTERN_NON_DIGITS = Pattern.compile("[^0-9]");
    private static final Pattern PATTERN_COMBINING_DIACRITICAL = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern PATTERN_MN = Pattern.compile("\\p{Mn}+");
    private static final Pattern PATTERN_SPECIAL_CHARS = Pattern.compile("[^a-zA-Z0-9\\s\\.\\-]");
    private static final Pattern PATTERN_MULTIPLE_SPACES = Pattern.compile("\\s+");
    private static final DecimalFormatSymbols US_SYMBOLS = new DecimalFormatSymbols(Locale.US);

    public static String removerAcentosEspeciais(String texto) {
        if (texto == null || texto.isEmpty()) return "";
        try {
            texto = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
            texto = PATTERN_COMBINING_DIACRITICAL.matcher(texto).replaceAll("");
            texto = PATTERN_MN.matcher(texto).replaceAll("");
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder(texto.length());
            for (int i = 0; i < texto.length(); i++) {
                char c = texto.charAt(i);
                switch (c) {
                    case 'à': case 'á': case 'â': case 'ã': case 'ä': case 'å': sb.append('a'); break;
                    case 'è': case 'é': case 'ê': case 'ë': sb.append('e'); break;
                    case 'ì': case 'í': case 'î': case 'ï': sb.append('i'); break;
                    case 'ò': case 'ó': case 'ô': case 'õ': case 'ö': sb.append('o'); break;
                    case 'ù': case 'ú': case 'û': case 'ü': sb.append('u'); break;
                    case 'ý': case 'ÿ': sb.append('y'); break;
                    case 'À': case 'Á': case 'Â': case 'Ã': case 'Ä': case 'Å': sb.append('A'); break;
                    case 'È': case 'É': case 'Ê': case 'Ë': sb.append('E'); break;
                    case 'Ì': case 'Í': case 'Î': case 'Ï': sb.append('I'); break;
                    case 'Ò': case 'Ó': case 'Ô': case 'Õ': case 'Ö': sb.append('O'); break;
                    case 'Ù': case 'Ú': case 'Û': case 'Ü': sb.append('U'); break;
                    case 'Ý': case 'Ÿ': sb.append('Y'); break;
                    case 'ç': case 'Ç': sb.append('c'); break;
                    case 'ñ': case 'Ñ': sb.append('n'); break;
                    case 'ß': sb.append("ss"); break;
                    case 'æ': sb.append("ae"); break;
                    case 'Æ': sb.append("AE"); break;
                    case 'œ': sb.append("oe"); break;
                    case 'Œ': sb.append("OE"); break;
                    default: sb.append(c);
                }
            }
            texto = sb.toString();
        }
        texto = PATTERN_SPECIAL_CHARS.matcher(texto).replaceAll("");
        texto = PATTERN_MULTIPLE_SPACES.matcher(texto).replaceAll(" ");
        return texto.trim();
    }

    public static String formatarDecimal(double valor, int casasDecimais) {
        if (casasDecimais == 2) return DECIMAL_FORMAT.format(valor);
        DecimalFormat format = DECIMAL_FORMAT_CACHE.get(casasDecimais);
        if (format == null) {
            StringBuilder formato = new StringBuilder("0.");
            for (int i = 0; i < casasDecimais; i++) formato.append("0");
            format = new DecimalFormat(formato.toString(), US_SYMBOLS);
            DECIMAL_FORMAT_CACHE.putIfAbsent(casasDecimais, format);
            format = DECIMAL_FORMAT_CACHE.get(casasDecimais);
        }
        return format.format(valor);
    }

    public static String formatarData(Timestamp data) {
        return data == null ? "" : DATE_FORMATTER.get().format(data);
    }

    public static String formatarDataHora(Timestamp data) {
        return data == null ? "" : DATETIME_FORMATTER.get().format(data);
    }

    public static String formatarCnpjCpf(String cnpjCpf) {
        return (cnpjCpf == null || cnpjCpf.isEmpty()) ? "" : PATTERN_NON_DIGITS.matcher(cnpjCpf).replaceAll("");
    }

    public static String formatarAlfanumerico(String texto, int tamanhoMax) {
        String textoLimpo = StringUtils.getNullAsEmpty(texto);
        if (StringUtils.isEmpty(textoLimpo)) return "";
        textoLimpo = removerAcentosEspeciais(textoLimpo);
        return textoLimpo.length() > tamanhoMax ? textoLimpo.substring(0, tamanhoMax) : textoLimpo;
    }

    public static String formatarNumeroComZeros(long valor, int tamanho) {
        return String.format("%0" + tamanho + "d", valor);
    }

    public static String criarLinha(String... campos) {
        if (campos == null || campos.length == 0) return "";
        StringBuilder linha = new StringBuilder(campos.length * 20);
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) linha.append(NeogridConstants.FIELD_SEPARATOR);
            String campo = campos[i];
            if (campo != null) linha.append(campo);
        }
        return linha.toString();
    }

}
