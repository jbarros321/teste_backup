package br.com.emfal.serasa.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import br.com.emfal.serasa.constants.SerasaConstants;

public class SerasaUtils {

    private static final Pattern CPF_PATTERN = Pattern.compile("(\\d)\\1{10}");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("(\\d)\\1{13}");

    public static boolean isValidDocument(String documento) {
        return Optional.ofNullable(documento)
            .map(doc -> doc.replaceAll("[^0-9]", ""))
            .filter(doc -> doc.length() == 11 || doc.length() == 14)
            .map(SerasaUtils::validateDocument)
            .orElse(false);
    }

    public static boolean validarCPF(String cpf) {
        return Optional.ofNullable(cpf)
            .map(doc -> doc.replaceAll("[^0-9]", ""))
            .filter(doc -> doc.length() == 11)
            .filter(doc -> !CPF_PATTERN.matcher(doc).matches())
            .map(SerasaUtils::validateCPF)
            .orElse(false);
    }

    public static boolean validarCNPJ(String cnpj) {
        return Optional.ofNullable(cnpj)
            .map(doc -> doc.replaceAll("[^0-9]", ""))
            .filter(doc -> doc.length() == 14)
            .filter(doc -> !CNPJ_PATTERN.matcher(doc).matches())
            .map(SerasaUtils::validateCNPJ)
            .orElse(false);
    }

    private static boolean validateDocument(String doc) {
        return doc.length() == 11 ? validateCPF(doc) : validateCNPJ(doc);
    }

    private static boolean validateCPF(String cpf) {
        return calculateDigit(cpf, 9, 10) == Character.getNumericValue(cpf.charAt(9)) &&
               calculateDigit(cpf, 10, 11) == Character.getNumericValue(cpf.charAt(10));
    }

    private static boolean validateCNPJ(String cnpj) {
        return calculateCNPJDigit(cnpj, 12) == Character.getNumericValue(cnpj.charAt(12)) &&
               calculateCNPJDigit(cnpj, 13) == Character.getNumericValue(cnpj.charAt(13));
    }

    private static int calculateDigit(String doc, int length, int multiplier) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(doc.charAt(i)) * (multiplier - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static int calculateCNPJDigit(String cnpj, int position) {
        int sum = 0;
        int weight = 2;
        for (int i = position - 1; i >= 0; i--) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weight;
            weight = weight == 9 ? 2 : weight + 1;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    public static String formatarCPF(String cpf) {
        return Optional.ofNullable(cpf)
            .map(doc -> doc.replaceAll("[^0-9]", ""))
            .filter(doc -> doc.length() == 11)
            .map(doc -> String.format("%s.%s.%s-%s",
                doc.substring(0, 3), doc.substring(3, 6),
                doc.substring(6, 9), doc.substring(9, 11)))
            .orElse(cpf);
    }

    public static String formatarCNPJ(String cnpj) {
        return Optional.ofNullable(cnpj)
            .map(doc -> doc.replaceAll("[^0-9]", ""))
            .filter(doc -> doc.length() == 14)
            .map(doc -> String.format("%s.%s.%s/%s-%s",
                doc.substring(0, 2), doc.substring(2, 5),
                doc.substring(5, 8), doc.substring(8, 12), doc.substring(12, 14)))
            .orElse(cnpj);
    }

    public static String determinarTipoDocumento(String documento) {
        return Optional.ofNullable(documento)
            .map(doc -> doc.replaceAll("[^0-9]", ""))
            .map(doc -> doc.length() == 11 ? SerasaConstants.TIPO_PF :
                       doc.length() == 14 ? SerasaConstants.TIPO_PJ : "INDEFINIDO")
            .orElse("INDEFINIDO");
    }

    public static String calcularRisco(int score) {
        return score >= SerasaConstants.SCORE_APROVADO ? SerasaConstants.RISCO_BAIXO :
               score >= SerasaConstants.SCORE_APROVADO_RESTRICOES ? SerasaConstants.RISCO_MEDIO :
               score >= SerasaConstants.SCORE_ANALISE_MANUAL ? SerasaConstants.RISCO_ALTO :
               SerasaConstants.RISCO_MUITO_ALTO;
    }

    public static String gerarRecomendacao(int score, int totalPendencias, int totalAlertas) {
        return score >= SerasaConstants.SCORE_APROVADO && totalPendencias == 0 && totalAlertas == 0 ?
               SerasaConstants.RECOMENDACAO_APROVADO :
               score >= SerasaConstants.SCORE_APROVADO_RESTRICOES && totalPendencias <= 2 && totalAlertas <= 1 ?
               SerasaConstants.RECOMENDACAO_APROVADO_RESTRICOES :
               score >= SerasaConstants.SCORE_ANALISE_MANUAL ?
               SerasaConstants.RECOMENDACAO_ANALISE_MANUAL :
               SerasaConstants.RECOMENDACAO_REPROVADO;
    }

    public static void logInfo(String mensagem) {
        System.out.println("INFO: " + mensagem);
    }

    public static void logErro(String mensagem) {
        System.err.println("ERRO: " + mensagem);
    }

    public static void logDebug(String mensagem) {
        System.out.println("DEBUG: " + mensagem);
    }

    public static Map<String, Object> criarResultadoSucesso(Object dados) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", SerasaConstants.STATUS_SUCESSO);
        resultado.put("data", dados);
        resultado.put("data_consulta", new Date());
        return resultado;
    }

    public static Map<String, Object> criarResultadoErro(String mensagem) {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", SerasaConstants.STATUS_ERRO);
        resultado.put("mensagem", mensagem);
        resultado.put("data_consulta", new Date());
        return resultado;
    }
}
