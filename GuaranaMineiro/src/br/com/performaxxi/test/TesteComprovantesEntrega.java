package br.com.performaxxi.test;

import java.util.List;

import br.com.performaxxi.shared.PerformaxxiAPI;

public class TesteComprovantesEntrega {

    public static void main(String[] args) {
        System.out.println("=== TESTE UNITARIO - COMPROVANTES ENTREGA ===");
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
        if (testarConsultaComprovantes()) {
            testesSucesso++;
            System.out.println("OK CONSULTA: Funcionando");
        } else {
            System.out.println("ERRO CONSULTA: Falha");
        }
        System.out.println();

        totalTestes++;
        if (testarPlacasReais()) {
            testesSucesso++;
            System.out.println("OK PLACAS: Funcionando");
        } else {
            System.out.println("ERRO PLACAS: Falha");
        }
        System.out.println();

        System.out.println("=== RESUMO FINAL ===");
        System.out.println("Total de testes: " + totalTestes);
        System.out.println("Testes com sucesso: " + testesSucesso);
        System.out.println("Taxa de sucesso: " + String.format("%.1f%%", (testesSucesso * 100.0 / totalTestes)));

        if (testesSucesso == totalTestes) {
            System.out.println("SUCESSO TODOS OS TESTES PASSARAM - COMPROVANTES ENTREGA FUNCIONANDO!");
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

    private static boolean testarConsultaComprovantes() {
        try {
            System.out.println("--- TESTANDO CONSULTA DE COMPROVANTES ---");

            String data = "2024-01-15";
            String idRastreador = "HIG2956";
            String classe = "ENTREGA";
            String conjunto = "COMPROVANTES";

            System.out.println("Parametros: Data=" + data + ", ID=" + idRastreador + ", Classe=" + classe + ", Conjunto=" + conjunto);

            List<Object> comprovantes = PerformaxxiAPI.consultarComprovantesEntrega(data, idRastreador, classe, conjunto);

            System.out.println("Comprovantes encontrados: " + comprovantes.size());
            System.out.println("Consulta executada com sucesso");

            return true;

        } catch (Exception e) {
            System.out.println("ERRO na consulta de comprovantes: " + e.getMessage());
            return false;
        }
    }

    private static boolean testarPlacasReais() {
        try {
            System.out.println("--- TESTANDO COM PLACAS REAIS ---");

            String[] placas = {"HIG2956", "HIG4117", "HLC4806", "OPT6H80", "HIG8224"};
            String data = "2024-01-15";
            String classe = "ENTREGA";
            String conjunto = "COMPROVANTES";

            int placasTestadas = 0;
            int placasComSucesso = 0;

            for (String placa : placas) {
                try {
                    System.out.println("Testando placa: " + placa);
                    List<Object> comprovantes = PerformaxxiAPI.consultarComprovantesEntrega(data, placa, classe, conjunto);
                    placasTestadas++;
                    placasComSucesso++;
                    System.out.println("  OK Sucesso - Comprovantes: " + comprovantes.size());
                } catch (Exception e) {
                    placasTestadas++;
                    System.out.println("  ERRO: " + e.getMessage());
                }
            }

            System.out.println("Placas testadas: " + placasTestadas);
            System.out.println("Placas com sucesso: " + placasComSucesso);
            System.out.println("Taxa de sucesso: " + String.format("%.1f%%", (placasComSucesso * 100.0 / placasTestadas)));

            return placasComSucesso > 0;

        } catch (Exception e) {
            System.out.println("ERRO no teste com placas: " + e.getMessage());
            return false;
        }
    }
}
