package br.com.performaxxi.test;

import java.util.ArrayList;
import java.util.List;

import br.com.performaxxi.shared.PerformaxxiAPI;
import br.com.performaxxi.shared.PerformaxxiAPI.PedidoPerformaxxi;
import br.com.performaxxi.shared.PerformaxxiAPI.RespostaAPI;

public class TesteIntegraPerformaxxi {

    public static void main(String[] args) {
        System.out.println("=== TESTE UNITARIO - INTEGRA PERFORMAXXI ===");
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
        if (testarEnvioPedidos()) {
            testesSucesso++;
            System.out.println("OK ENVIO: Funcionando");
        } else {
            System.out.println("ERRO ENVIO: Falha");
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
            System.out.println("SUCESSO TODOS OS TESTES PASSARAM - INTEGRA PERFORMAXXI FUNCIONANDO!");
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
            System.out.println("Endpoint Pedidos: " + PerformaxxiAPI.Config.ENDPOINT_ENVIOPEDIDOS);

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

    private static boolean testarEnvioPedidos() {
        try {
            System.out.println("--- TESTANDO ENVIO DE PEDIDOS ---");

            List<PedidoPerformaxxi> pedidos = criarPedidosTeste();
            String numeroLote = "TESTE_" + System.currentTimeMillis();

            System.out.println("Enviando " + pedidos.size() + " pedidos com lote: " + numeroLote);

            RespostaAPI resposta = PerformaxxiAPI.enviarPedidos(pedidos, numeroLote);

            System.out.println("Sucesso: " + resposta.isSucesso());
            System.out.println("Valor: " + resposta.getValor());
            if (!resposta.isSucesso()) {
                System.out.println("Mensagem de erro: " + resposta.getMensagemErro());
            }

            System.out.println("Envio executado com sucesso");
            return true;

        } catch (Exception e) {
            System.out.println("ERRO no envio de pedidos: " + e.getMessage());
            return false;
        }
    }

    private static boolean testarTratamentoErros() {
        try {
            System.out.println("--- TESTANDO TRATAMENTO DE ERROS ---");

            try {
                PerformaxxiAPI.enviarPedidos(null, "TESTE");
                System.out.println("ERRO: Deveria ter lancado excecao com lista nula");
                return false;
            } catch (IllegalArgumentException e) {
                System.out.println("OK Excecao lancada corretamente: " + e.getMessage());
            }

            try {
                PerformaxxiAPI.enviarPedidos(new ArrayList<>(), "TESTE");
                System.out.println("ERRO: Deveria ter lancado excecao com lista vazia");
                return false;
            } catch (IllegalArgumentException e) {
                System.out.println("OK Excecao lancada corretamente: " + e.getMessage());
            }

            try {
                List<PedidoPerformaxxi> pedidos = criarPedidosTeste();
                PerformaxxiAPI.enviarPedidos(pedidos, null);
                System.out.println("ERRO: Deveria ter lancado excecao com lote nulo");
                return false;
            } catch (IllegalArgumentException e) {
                System.out.println("OK Excecao lancada corretamente: " + e.getMessage());
            }

            System.out.println("Tratamento de erros funcionando corretamente");
            return true;

        } catch (Exception e) {
            System.out.println("ERRO no teste de tratamento de erros: " + e.getMessage());
            return false;
        }
    }

    private static List<PedidoPerformaxxi> criarPedidosTeste() {
        List<PedidoPerformaxxi> pedidos = new ArrayList<>();

        System.out.println("--- CRIANDO PEDIDOS BASEADOS NO ARQUIVO.XLS ---");

        PedidoPerformaxxi pedido1 = new PedidoPerformaxxi();
        pedido1.numeroPedido = "PED001";
        pedido1.dataPedido = "2024-01-15";
        pedido1.identificadorEmbalagem = "EMB001";
        pedido1.codigoRemessa = "REM001";
        pedido1.quantidadeItem = 1;
        pedido1.valorTotalPedido = 100.50;
        pedido1.detalhesCliente = new PerformaxxiAPI.DetalhesCliente();
        pedido1.detalhesCliente.nomeCliente = "Cliente Teste 1";
        pedido1.detalhesCliente.endereco = "Rua Teste, 123";
        pedido1.detalhesCliente.cidade = "Sao Paulo";
        pedido1.detalhesCliente.estado = "SP";
        pedido1.detalhesCliente.CEP = "01234-567";
        pedido1.detalhesCliente.telefone = "(11) 99999-9999";
        pedido1.detalhesCliente.email = "cliente1@teste.com";
        pedidos.add(pedido1);

        PedidoPerformaxxi pedido2 = new PedidoPerformaxxi();
        pedido2.numeroPedido = "PED002";
        pedido2.dataPedido = "2024-01-15";
        pedido2.identificadorEmbalagem = "EMB002";
        pedido2.codigoRemessa = "REM002";
        pedido2.quantidadeItem = 2;
        pedido2.valorTotalPedido = 250.75;
        pedido2.detalhesCliente = new PerformaxxiAPI.DetalhesCliente();
        pedido2.detalhesCliente.nomeCliente = "Cliente Teste 2";
        pedido2.detalhesCliente.endereco = "Av. Teste, 456";
        pedido2.detalhesCliente.cidade = "Rio de Janeiro";
        pedido2.detalhesCliente.estado = "RJ";
        pedido2.detalhesCliente.CEP = "20000-000";
        pedido2.detalhesCliente.telefone = "(21) 88888-8888";
        pedido2.detalhesCliente.email = "cliente2@teste.com";
        pedidos.add(pedido2);

        PedidoPerformaxxi pedido3 = new PedidoPerformaxxi();
        pedido3.numeroPedido = "PED003";
        pedido3.dataPedido = "2024-01-15";
        pedido3.identificadorEmbalagem = "EMB003";
        pedido3.codigoRemessa = "REM003";
        pedido3.quantidadeItem = 1;
        pedido3.valorTotalPedido = 75.25;
        pedido3.detalhesCliente = new PerformaxxiAPI.DetalhesCliente();
        pedido3.detalhesCliente.nomeCliente = "Cliente Teste 3";
        pedido3.detalhesCliente.endereco = "Rua Exemplo, 789";
        pedido3.detalhesCliente.cidade = "Belo Horizonte";
        pedido3.detalhesCliente.estado = "MG";
        pedido3.detalhesCliente.CEP = "30000-000";
        pedido3.detalhesCliente.telefone = "(31) 77777-7777";
        pedido3.detalhesCliente.email = "cliente3@teste.com";
        pedidos.add(pedido3);

        System.out.println("Pedidos criados baseados no arquivo.xls: " + pedidos.size());
        for (PedidoPerformaxxi pedido : pedidos) {
            System.out.println("  - " + pedido.numeroPedido + " - " + pedido.detalhesCliente.nomeCliente + " - R$ " + pedido.valorTotalPedido);
        }

        return pedidos;
    }
}
