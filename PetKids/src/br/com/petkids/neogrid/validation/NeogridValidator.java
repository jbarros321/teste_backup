package br.com.petkids.neogrid.validation;

import br.com.petkids.neogrid.exception.NeogridValidationException;
import br.com.petkids.neogrid.model.enums.TipoRelatorio;
import br.com.petkids.neogrid.util.NeogridConstants;
import com.sankhya.util.StringUtils;

import java.io.File;

public class NeogridValidator {

    public static void validarCnpjCpf(String cnpjCpf) throws NeogridValidationException {
        if (StringUtils.isEmpty(cnpjCpf)) {
            throw new NeogridValidationException("CNPJ/CPF não pode ser vazio");
        }
        String cnpjCpfLimpo = cnpjCpf.replaceAll("[^0-9]", "");
        if (cnpjCpfLimpo.length() != NeogridConstants.TAMANHO_CNPJ &&
            cnpjCpfLimpo.length() != NeogridConstants.TAMANHO_CPF) {
            throw new NeogridValidationException(
                String.format("CNPJ/CPF deve ter %d (CNPJ) ou %d (CPF) dígitos. Valor informado: %s",
                    NeogridConstants.TAMANHO_CNPJ, NeogridConstants.TAMANHO_CPF, cnpjCpf));
        }
    }

    public static void validarCnpj(String cnpj) throws NeogridValidationException {
        if (StringUtils.isEmpty(cnpj)) {
            throw new NeogridValidationException("CNPJ não pode ser vazio");
        }
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        if (cnpjLimpo.length() != NeogridConstants.TAMANHO_CNPJ) {
            throw new NeogridValidationException(
                String.format("CNPJ deve ter %d dígitos. Valor informado: %s",
                    NeogridConstants.TAMANHO_CNPJ, cnpj));
        }
    }

    public static String validarCaminhoExportacao(String caminho) throws NeogridValidationException {
        if (StringUtils.isEmpty(caminho)) {
            String tempDir = System.getProperty("java.io.tmpdir");
            if (StringUtils.isEmpty(tempDir)) {
                throw new NeogridValidationException("Não foi possível obter o diretório temporário do sistema");
            }
            caminho = tempDir + File.separator + "neogrid_export";
        }
        File dir = new File(caminho);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new NeogridValidationException("Não foi possível criar o diretório de exportação: " + caminho);
            }
        }
        if (!dir.isDirectory()) {
            throw new NeogridValidationException("Caminho de exportação não é um diretório: " + caminho);
        }
        if (!dir.canWrite()) {
            throw new NeogridValidationException("Sem permissão de escrita no diretório: " + caminho);
        }
        return caminho;
    }

    public static void validarTipoRelatorio(String tipoRelatorio) throws NeogridValidationException {
        if (StringUtils.isEmpty(tipoRelatorio)) {
            throw new NeogridValidationException("Tipo de relatório é obrigatório");
        }
        String tipoUpper = tipoRelatorio.toUpperCase();
        if (tipoUpper.equals(NeogridConstants.TIPO_TODOS)) return;
        try {
            TipoRelatorio.fromString(tipoRelatorio);
            return;
        } catch (IllegalArgumentException e) {
            if (!tipoUpper.equals(NeogridConstants.TIPO_VENDEDORES) &&
                !tipoUpper.equals(NeogridConstants.TIPO_CLIENTES) &&
                !tipoUpper.equals(NeogridConstants.TIPO_PRODUTOS) &&
                !tipoUpper.equals(NeogridConstants.TIPO_VENDAS) &&
                !tipoUpper.equals(NeogridConstants.TIPO_ESTOQUE)) {
                throw new NeogridValidationException(
                    String.format("Tipo de relatório inválido: %s. Valores válidos: %s, %s, %s, %s, %s, %s",
                        tipoRelatorio, NeogridConstants.TIPO_TODOS, NeogridConstants.TIPO_VENDEDORES,
                        NeogridConstants.TIPO_CLIENTES, NeogridConstants.TIPO_PRODUTOS,
                        NeogridConstants.TIPO_VENDAS, NeogridConstants.TIPO_ESTOQUE));
            }
        }
    }

    public static void validarParametrosGeracao(String tipoRelatorio, String cnpjFilial)
            throws NeogridValidationException {
        validarTipoRelatorio(tipoRelatorio);
    }

    public static void validarParametrosComIndustria(String tipoRelatorio, String cnpjFilial, String cnpjIndustria)
            throws NeogridValidationException {
        validarParametrosGeracao(tipoRelatorio, cnpjFilial);
    }

    public static void validarCnpjDestinatario(String cnpjDestinatario, boolean permitirIndustria) throws NeogridValidationException {
        if (StringUtils.isEmpty(cnpjDestinatario)) {
            throw new NeogridValidationException("CNPJ do destinatário não pode ser vazio");
        }
        String cnpjLimpo = cnpjDestinatario.replaceAll("[^0-9]", "");
        if (cnpjLimpo.length() != NeogridConstants.TAMANHO_CNPJ) {
            throw new NeogridValidationException(
                String.format("CNPJ do destinatário inválido. Valor informado: %s", cnpjDestinatario));
        }
        if (!permitirIndustria && !cnpjLimpo.equals(NeogridConstants.CNPJ_NEOGRID)) {
            throw new NeogridValidationException(
                String.format("CNPJ do destinatário deve ser %s (Neogrid). Valor informado: %s",
                    NeogridConstants.CNPJ_NEOGRID, cnpjDestinatario));
        }
    }

    public static void validarCnpjDestinatario(String cnpjDestinatario) throws NeogridValidationException {
        validarCnpjDestinatario(cnpjDestinatario, false);
    }

    public static void validarDadosDisponiveis(int quantidadeRegistros, String tipoRelatorio)
            throws NeogridValidationException {
        if (quantidadeRegistros == 0) {
            throw new NeogridValidationException(
                String.format("Não há dados disponíveis para gerar o relatório: %s", tipoRelatorio));
        }
    }
}
