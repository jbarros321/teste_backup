package br.com.petkids.neogrid.test;

import br.com.petkids.neogrid.util.NeogridConstants;
import br.com.petkids.neogrid.util.NeogridFormatter;
import br.com.petkids.neogrid.validation.NeogridValidator;
import br.com.petkids.neogrid.exception.NeogridValidationException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ValidacaoTestes {

    private static int testesPassaram = 0;
    private static int testesFalharam = 0;
    private static int totalTestes = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("TESTES E VALIDAÇÃO - INTEGRAÇÃO NEOGRID");
        System.out.println("========================================\n");

        try {
            testarNeogridConstants();
            testarNeogridFormatter();
            testarNeogridValidator();

            System.out.println("\n========================================");
            System.out.println("RESUMO DOS TESTES");
            System.out.println("========================================");
            System.out.println("Total de testes: " + totalTestes);
            System.out.println("✅ Testes que passaram: " + testesPassaram);
            System.out.println("❌ Testes que falharam: " + testesFalharam);
            System.out.println("Taxa de sucesso: " +
                (totalTestes > 0 ? (testesPassaram * 100 / totalTestes) + "%" : "0%"));

            if (testesFalharam == 0) {
                System.out.println("\n🎉 TODOS OS TESTES PASSARAM!");
                System.exit(0);
            } else {
                System.out.println("\n⚠️  ALGUNS TESTES FALHARAM!");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("\n❌ ERRO CRÍTICO NOS TESTES: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testarNeogridConstants() {
        System.out.println("\n📋 TESTANDO NeogridConstants");
        System.out.println("----------------------------------------");

        testar("CNPJ_NEOGRID deve ser 03887830009046",
            NeogridConstants.CNPJ_NEOGRID.equals("03887830009046"));

        testar("TAMANHO_CNPJ deve ser 14",
            NeogridConstants.TAMANHO_CNPJ == 14);
        testar("TAMANHO_CPF deve ser 11",
            NeogridConstants.TAMANHO_CPF == 11);

        testar("IDENTIFICACAO_RELVEN deve ser RELVEN",
            NeogridConstants.IDENTIFICACAO_RELVEN.equals("RELVEN"));
        testar("IDENTIFICACAO_RELCLI deve ser RELCLI",
            NeogridConstants.IDENTIFICACAO_RELCLI.equals("RELCLI"));
        testar("IDENTIFICACAO_RELPRO deve ser RELPRO",
            NeogridConstants.IDENTIFICACAO_RELPRO.equals("RELPRO"));
        testar("IDENTIFICACAO_VENDAS deve ser VENDAS",
            NeogridConstants.IDENTIFICACAO_VENDAS.equals("VENDAS"));
        testar("IDENTIFICACAO_RELEST deve ser RELEST",
            NeogridConstants.IDENTIFICACAO_RELEST.equals("RELEST"));

        testar("VERSAO_RELVEN deve ser 052",
            NeogridConstants.VERSAO_RELVEN.equals("052"));
        testar("VERSAO_RELCLI deve ser 042",
            NeogridConstants.VERSAO_RELCLI.equals("042"));
        testar("VERSAO_RELPRO deve ser 051",
            NeogridConstants.VERSAO_RELPRO.equals("051"));
        testar("VERSAO_VENDAS deve ser 052",
            NeogridConstants.VERSAO_VENDAS.equals("052"));
        testar("VERSAO_RELEST deve ser 050",
            NeogridConstants.VERSAO_RELEST.equals("050"));

        testar("TIPO_REGISTRO_CABECALHO deve ser 01",
            NeogridConstants.TIPO_REGISTRO_CABECALHO.equals("01"));
        testar("TIPO_REGISTRO_DADOS deve ser 02",
            NeogridConstants.TIPO_REGISTRO_DADOS.equals("02"));
        testar("TIPO_REGISTRO_ITENS deve ser 03",
            NeogridConstants.TIPO_REGISTRO_ITENS.equals("03"));

        testar("CHARSET_ANSI deve ser Windows-1252",
            NeogridConstants.CHARSET_ANSI.equals("Windows-1252"));
        testar("LINE_SEPARATOR deve ser CRLF",
            NeogridConstants.LINE_SEPARATOR.equals("\r\n"));
        testar("FIELD_SEPARATOR deve ser PIPE",
            NeogridConstants.FIELD_SEPARATOR.equals("|"));

        testar("LOG_PREFIX deve ser [NEOGRID]",
            NeogridConstants.LOG_PREFIX.equals("[NEOGRID]"));
    }

    private static void testarNeogridFormatter() {
        System.out.println("\n📋 TESTANDO NeogridFormatter");
        System.out.println("----------------------------------------");

        String textoComAcentos = "São Paulo - Açúcar";
        String resultado = NeogridFormatter.removerAcentosEspeciais(textoComAcentos);
        testar("Remover acentos de 'São Paulo - Açúcar'",
            resultado.equals("Sao Paulo - Acucar"));

        String textoEspecial = "Teste@#123";
        resultado = NeogridFormatter.removerAcentosEspeciais(textoEspecial);
        testar("Remover caracteres especiais",
            resultado.equals("Teste123"));

        resultado = NeogridFormatter.removerAcentosEspeciais(null);
        testar("Remover acentos de null retorna string vazia",
            resultado != null && resultado.isEmpty());

        String decimal = NeogridFormatter.formatarDecimal(125.50, 2);
        testar("Formatar decimal 125.50 com 2 casas",
            decimal.equals("125.50"));

        decimal = NeogridFormatter.formatarDecimal(10.12345, 5);
        testar("Formatar decimal 10.12345 com 5 casas",
            decimal.equals("10.12345"));

        String cnpj = NeogridFormatter.formatarCnpjCpf("07.056.359/0001-20");
        testar("Formatar CNPJ removendo formatação",
            cnpj.equals("07056359000120") && cnpj.length() == 14);

        String numero = NeogridFormatter.formatarNumeroComZeros(5, 3);
        testar("Formatar número 5 com 3 dígitos",
            numero.equals("005"));

        Date data = new Date();
        String dataFormatada = NeogridFormatter.formatarData(data);
        testar("Formatar data no formato AAAAMMDD",
            dataFormatada != null && dataFormatada.length() == 8);

        String dataHoraFormatada = NeogridFormatter.formatarDataHora(data);
        testar("Formatar data/hora no formato AAAAMMDDHHMM",
            dataHoraFormatada != null && dataHoraFormatada.length() == 12);

        String linha = NeogridFormatter.criarLinha("01", "RELVEN", "052");
        testar("Criar linha com separador PIPE",
            linha.equals("01|RELVEN|052"));
    }

    private static void testarNeogridValidator() {
        System.out.println("\n📋 TESTANDO NeogridValidator");
        System.out.println("----------------------------------------");

        try {
            NeogridValidator.validarCnpj("07056359000120");
            testar("Validar CNPJ válido (14 dígitos)", true);
        } catch (NeogridValidationException e) {
            testar("Validar CNPJ válido (14 dígitos)", false);
        }

        try {
            NeogridValidator.validarCnpj("123456789");
            testar("Validar CNPJ inválido (tamanho errado) deve lançar exceção", false);
        } catch (NeogridValidationException e) {
            testar("Validar CNPJ inválido (tamanho errado) deve lançar exceção", true);
        }

        try {
            NeogridValidator.validarCnpj("");
            testar("Validar CNPJ vazio deve lançar exceção", false);
        } catch (NeogridValidationException e) {
            testar("Validar CNPJ vazio deve lançar exceção", true);
        }

        try {
            NeogridValidator.validarCnpjCpf("07056359000120");
            testar("Validar CNPJ/CPF válido (CNPJ)", true);
        } catch (NeogridValidationException e) {
            testar("Validar CNPJ/CPF válido (CNPJ)", false);
        }

        try {
            NeogridValidator.validarCnpjCpf("12345678901");
            testar("Validar CNPJ/CPF válido (CPF)", true);
        } catch (NeogridValidationException e) {
            testar("Validar CNPJ/CPF válido (CPF)", false);
        }

        try {
            NeogridValidator.validarCnpjDestinatario("03887830009046");
            testar("Validar CNPJ destinatário (Neogrid)", true);
        } catch (NeogridValidationException e) {
            testar("Validar CNPJ destinatário (Neogrid)", false);
        }

        try {
            NeogridValidator.validarCnpjDestinatario("12345678901234");
            testar("Validar CNPJ destinatário inválido deve lançar exceção", false);
        } catch (NeogridValidationException e) {
            testar("Validar CNPJ destinatário inválido deve lançar exceção", true);
        }

        try {
            NeogridValidator.validarTipoRelatorio("TODOS");
            testar("Validar tipo de relatório 'TODOS'", true);
        } catch (NeogridValidationException e) {
            testar("Validar tipo de relatório 'TODOS'", false);
        }

        try {
            NeogridValidator.validarTipoRelatorio("INVALIDO");
            testar("Validar tipo de relatório inválido deve lançar exceção", false);
        } catch (NeogridValidationException e) {
            testar("Validar tipo de relatório inválido deve lançar exceção", true);
        }

        try {
            NeogridValidator.validarDadosDisponiveis(10, "RELVEN");
            testar("Validar dados disponíveis (com dados)", true);
        } catch (NeogridValidationException e) {
            testar("Validar dados disponíveis (com dados)", false);
        }

        try {
            NeogridValidator.validarDadosDisponiveis(0, "RELVEN");
            testar("Validar dados disponíveis (sem dados) deve lançar exceção", false);
        } catch (NeogridValidationException e) {
            testar("Validar dados disponíveis (sem dados) deve lançar exceção", true);
        }

        try {
            String caminho = NeogridValidator.validarCaminhoExportacao(null);
            testar("Validar caminho de exportação (null usa temp)",
                caminho != null && !caminho.isEmpty());
        } catch (NeogridValidationException e) {
            testar("Validar caminho de exportação (null usa temp)", false);
        }
    }

    private static void testar(String descricao, boolean passou) {
        totalTestes++;
        if (passou) {
            testesPassaram++;
            System.out.println("✅ " + descricao);
        } else {
            testesFalharam++;
            System.out.println("❌ " + descricao);
        }
    }
}
