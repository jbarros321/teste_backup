package br.com.cliente.util;
import com.sankhya.util.StringUtils;
import java.io.File;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern CPF_PATTERN_REPETIDO = Pattern.compile("(\\d)\\1{10}");
    private static final Pattern CNPJ_PATTERN_REPETIDO = Pattern.compile("(\\d)\\1{13}");

    public static void validarCnpjCpf(String cnpjCpf) throws IllegalArgumentException {
        if (StringUtils.isEmpty(cnpjCpf)) throw new IllegalArgumentException("CNPJ/CPF não pode ser vazio");
        String limpo = cnpjCpf.replaceAll("[^0-9]", "");
        if (limpo.length() != Constants.TAMANHO_CNPJ && limpo.length() != Constants.TAMANHO_CPF) {
            throw new IllegalArgumentException(String.format("CNPJ/CPF deve ter %d (CNPJ) ou %d (CPF) dígitos. Valor: %s", Constants.TAMANHO_CNPJ, Constants.TAMANHO_CPF, cnpjCpf));
        }
    }

    public static void validarCnpj(String cnpj) throws IllegalArgumentException {
        if (StringUtils.isEmpty(cnpj)) throw new IllegalArgumentException("CNPJ não pode ser vazio");
        String limpo = cnpj.replaceAll("[^0-9]", "");
        if (limpo.length() != Constants.TAMANHO_CNPJ) throw new IllegalArgumentException(String.format("CNPJ deve ter %d dígitos. Valor: %s", Constants.TAMANHO_CNPJ, cnpj));
        if (CNPJ_PATTERN_REPETIDO.matcher(limpo).matches()) throw new IllegalArgumentException("CNPJ inválido: todos os dígitos são iguais");
    }

    public static void validarCpf(String cpf) throws IllegalArgumentException {
        if (StringUtils.isEmpty(cpf)) throw new IllegalArgumentException("CPF não pode ser vazio");
        String limpo = cpf.replaceAll("[^0-9]", "");
        if (limpo.length() != Constants.TAMANHO_CPF) throw new IllegalArgumentException(String.format("CPF deve ter %d dígitos. Valor: %s", Constants.TAMANHO_CPF, cpf));
        if (CPF_PATTERN_REPETIDO.matcher(limpo).matches()) throw new IllegalArgumentException("CPF inválido: todos os dígitos são iguais");
    }

    public static String validarECriarDiretorio(String caminho) throws IllegalArgumentException {
        if (StringUtils.isEmpty(caminho)) {
            String tmpDir = System.getProperty("java.io.tmpdir");
            if (StringUtils.isEmpty(tmpDir)) throw new IllegalArgumentException("Diretório temporário não configurado");
            caminho = tmpDir;
        }
        File dir = new File(caminho);
        if (!dir.exists() && !dir.mkdirs()) throw new IllegalArgumentException("Não foi possível criar diretório: " + caminho);
        if (!dir.isDirectory()) throw new IllegalArgumentException("Caminho não é um diretório: " + caminho);
        if (!dir.canWrite()) throw new IllegalArgumentException("Sem permissão de escrita: " + caminho);
        return caminho;
    }

    public static void validarNaoVazio(String valor, String nomeCampo) throws IllegalArgumentException {
        if (StringUtils.isEmpty(valor)) throw new IllegalArgumentException(nomeCampo + " não pode ser vazio");
    }
}
