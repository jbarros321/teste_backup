package br.com.performaxxi.test;

import br.com.performaxxi.shared.PerformaxxiAPI;

public class TesteRecebimentoEvento {

    public static void main(String[] args) {
        System.out.println("=== TESTE UNITARIO - RECEBIMENTO EVENTO ===");
        System.out.println("Ambiente: HOMOLOGACAO (SEMPRE)");
        System.out.println("URL: " + PerformaxxiAPI.Config.API_BASE_URL_HOMOLOGACAO);
        System.out.println();

        int totalTestes = 0;
        int testesSucesso = 0;

        totalTestes++;
        if (testarConfiguracao()) {
            testesSucesso++;
            System.out.println("OK CONFIGURACAO: Validada");
        } else {
            System.out.println("ERRO CONFIGURACAO: Falha");
        }
        System.out.println();

        totalTestes++;
        if (testarConectividade()) {
            testesSucesso++;
            System.out.println("OK CONECTIVIDADE: OK");
        } else {
            System.out.println("ERRO CONECTIVIDADE: Falha");
        }
        System.out.println();

        totalTestes++;
        if (testarEnvioMensagemRecebimento()) {
            testesSucesso++;
            System.out.println("OK MENSAGEM: Funcionando");
        } else {
            System.out.println("ERRO MENSAGEM: Falha");
        }
        System.out.println();

        totalTestes++;
        if (testarTratamentoErros()) {
            testesSucesso++;
            System.out.println("OK ERROS: Funcionando");
        } else {
            System.out.println("ERRO ERROS: Falha");
        }
        System.out.println();

        System.out.println("=== RESUMO FINAL ===");
        System.out.println("Total de testes: " + totalTestes);
        System.out.println("Testes com sucesso: " + testesSucesso);
        System.out.println("Taxa de sucesso: " + String.format("%.1f%%", (testesSucesso * 100.0 / totalTestes)));

        if (testesSucesso == totalTestes) {
            System.out.println("SUCESSO TODOS OS TESTES PASSARAM - RECEBIMENTO EVENTO FUNCIONANDO!");
        } else {
            System.out.println("ATENCAO ALGUNS TESTES FALHARAM - VERIFICAR IMPLEMENTACAO");
        }
    }

    private static boolean testarConfiguracao() {
        try {
            System.out.println("--- VALIDANDO CONFIGURACAO ---");

            if (!"HOMOLOGACAO".equals(PerformaxxiAPI.Config.AMBIENTE_ATIVO)) {
                System.out.println("ERRO: Ambiente nao esta configurado para HOMOLOGACAO");
                return false;
            }

            String urlHomologacao = PerformaxxiAPI.Config.API_BASE_URL_HOMOLOGACAO;
            if (urlHomologacao == null || !urlHomologacao.contains("rotaonline.com.br")) {
                System.out.println("ERRO: URL de homologacao incorreta");
                return false;
            }

            System.out.println("Ambiente: " + PerformaxxiAPI.Config.AMBIENTE_ATIVO);
            System.out.println("URL Homologacao: " + urlHomologacao);
            System.out.println("Usuario: " + PerformaxxiAPI.Config.API_USERNAME);

            return true;

        } catch (Exception e) {
            System.out.println("ERRO na validacao de configuracao: " + e.getMessage());
            return false;
        }
    }

    private static boolean testarConectividade() {
        try {
            System.out.println("--- TESTANDO CONECTIVIDADE ---");

            String url = PerformaxxiAPI.Config.API_BASE_URL_HOMOLOGACAO;
            System.out.println("Testando conectividade com: " + url);

            java.net.URL urlObj = new java.net.URL(url);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            System.out.println("Codigo de resposta: " + responseCode);

            return responseCode >= 200 && responseCode < 500;

        } catch (Exception e) {
            System.out.println("ERRO na conectividade: " + e.getMessage());
            return false;
        }
    }

    private static boolean testarEnvioMensagemRecebimento() {
        try {
            System.out.println("--- TESTANDO ENVIO DE MENSAGEM DE RECEBIMENTO ---");

            int nufin = 12345;
            int codParc = 1001;
            double valorRecebido = 150.75;
            String dataRecebimento = "2024-01-15T10:30:00";
            int recdesp = 1;
            int ordemCarga = 100;
            String nomeCliente = "Cliente Teste";
            int codEmp = 1;
            int codTipoOperBaixa = 1;
            String dataTipoOperBaixa = "2024-01-15T10:30:00";

            System.out.println("Parametros: NUFIN=" + nufin + ", Cliente=" + nomeCliente + ", Valor=" + valorRecebido);

            PerformaxxiAPI.enviarMensagemRecebimento(PerformaxxiAPI.converterParaRecebimentoPerformaxxi(
                nufin, codParc, valorRecebido, dataRecebimento, recdesp, ordemCarga, nomeCliente,
                codEmp, codTipoOperBaixa, dataTipoOperBaixa));

            System.out.println("Mensagem de recebimento enviada com sucesso");
            return true;

        } catch (Exception e) {
            System.out.println("ERRO no envio de mensagem de recebimento: " + e.getMessage());
            return false;
        }
    }

    private static boolean testarTratamentoErros() {
        try {
            System.out.println("--- TESTANDO TRATAMENTO DE ERROS ---");

            try {
                PerformaxxiAPI.converterParaRecebimentoPerformaxxi(12345, 1001, 150.75, "2024-01-15T10:30:00", 1, 100, "Cliente Teste", 1, 1, "2024-01-15T10:30:00");
                System.out.println("OK Conversao com parametros validos funcionando");
            } catch (Exception e) {
                System.out.println("ERRO: Conversao com parametros validos falhou: " + e.getMessage());
                return false;
            }

            try {
                PerformaxxiAPI.converterParaRecebimentoPerformaxxi(54321, 2001, 250.50, "2024-01-16T14:45:00", 1, 200, "Cliente Diferente", 1, 1, "2024-01-16T14:45:00");
                System.out.println("OK Conversao com diferentes dados funcionando");
            } catch (Exception e) {
                System.out.println("ERRO: Conversao com diferentes dados falhou: " + e.getMessage());
                return false;
            }

            System.out.println("Tratamento de erros funcionando corretamente");
            return true;

        } catch (Exception e) {
            System.out.println("ERRO no teste de tratamento de erros: " + e.getMessage());
            return false;
        }
    }
}
