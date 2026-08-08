package br.com.performaxxi.action.botaoAcao;

import java.util.List;
import java.util.Map;

import br.com.performaxxi.shared.PerformaxxiAPI;
import br.com.performaxxi.shared.PerformaxxiIntegracaoHelper;
import br.com.performaxxi.shared.PerformaxxiIntegracaoHelper.PedidoData;
import br.com.performaxxi.shared.PerformaxxiIntegracaoHelper.ResultadoIntegracao;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class IntegraPerformaxxi implements AcaoRotinaJava {

    private static final String TABELA_LOG = "AD_INTPERFORMAXXILOG";
    private static final String STATUS_SUCESSO = "SUCESSO";
    private static final String STATUS_ERRO = "ERRO";

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        String numeroLote = gerarNumeroLote();
        long tempoInicio = System.currentTimeMillis();

        try {
            Map<String, Object> params = PerformaxxiIntegracaoHelper.extrairParametros(ctx);

            List<PedidoData> pedidos = PerformaxxiIntegracaoHelper.executarQuery(params);

            if (pedidos.isEmpty()) {
                ctx.setMensagemRetorno(PerformaxxiAPI.createInfoMessage(
                    "Nenhum pedido encontrado para integracao - Verifique os parametros de periodo e filtros utilizados."));
                return;
            }

            boolean incluirEnviados = Boolean.parseBoolean(String.valueOf(params.getOrDefault("INCLUIR_ENVIADOS", "false")));
            if (!incluirEnviados) {
                pedidos = PerformaxxiIntegracaoHelper.filtrarPedidosNaoEnviados(pedidos, TABELA_LOG);
            }

            if (pedidos.isEmpty()) {
                ctx.setMensagemRetorno(PerformaxxiAPI.createInfoMessage("Todos os pedidos ja foram enviados anteriormente."));
                return;
            }

            ResultadoIntegracao resultado = processarIntegracao(pedidos, numeroLote);

            long tempoExecucao = System.currentTimeMillis() - tempoInicio;
            String statusFinal = resultado.getErros() == 0 ? "SUCESSO" : (resultado.getSucessos() > 0 ? "PARCIAL" : "ERRO");
            String mensagemFinal = String.format("Integracao concluida - Sucessos: %d | Erros: %d",
                resultado.getSucessos(), resultado.getErros());
            String parametrosJson = criarJsonParametros(params);

            PerformaxxiIntegracaoHelper.registrarExecucao(numeroLote, statusFinal, mensagemFinal,
                pedidos.size(), resultado.getSucessos(), resultado.getErros(), tempoExecucao, parametrosJson, TABELA_LOG);

            definirMensagemRetorno(ctx, resultado, pedidos);

        } catch (Exception e) {
            long tempoExecucao = System.currentTimeMillis() - tempoInicio;
            try {
                String parametrosJson = "{}";
                PerformaxxiIntegracaoHelper.registrarExecucao(numeroLote, "ERRO", e.getMessage(),
                    0, 0, 1, tempoExecucao, parametrosJson, TABELA_LOG);
            } catch (Exception logError) {
                System.err.println("Erro ao registrar log de erro: " + logError.getMessage());
            }

            String erro = PerformaxxiAPI.tratarErroPerformaxxi(e);
            if (erro != null) {
                ctx.setMensagemRetorno(erro);
                throw new Exception(erro);
            } else {
                throw e;
            }
        }
    }

    private ResultadoIntegracao processarIntegracao(List<PedidoData> pedidos, String numeroLote) throws Exception {
        try {
            List<PerformaxxiAPI.PedidoPerformaxxi> pedidosAPI =
                PerformaxxiIntegracaoHelper.converterParaPedidosAPISimples(pedidos);

            PerformaxxiAPI.RespostaAPI resposta = PerformaxxiAPI.enviarPedidos(pedidosAPI, numeroLote);

            if (resposta.sucesso) {
                PerformaxxiIntegracaoHelper.registrarPedidosEnviados(pedidos, numeroLote, STATUS_SUCESSO,
                    String.valueOf(resposta.valor), TABELA_LOG);
                return new ResultadoIntegracao(pedidos.size(), 0, resposta);
            } else {
                PerformaxxiIntegracaoHelper.registrarPedidosEnviados(pedidos, numeroLote, STATUS_ERRO,
                    resposta.mensagemErro, TABELA_LOG);

                if (resposta.mensagemErro != null &&
                    (resposta.mensagemErro.contains("401") || resposta.mensagemErro.contains("autenticacao"))) {
                    throw new Exception("ERRO DE AUTENTICACAO - API PERFORMAXXI: " + resposta.mensagemErro);
                }

                return new ResultadoIntegracao(0, pedidos.size(), resposta);
            }

        } catch (Exception e) {
            String mensagemErro = e.getMessage() != null ? e.getMessage() : "Sem mensagem";

            if (mensagemErro.contains("401") || mensagemErro.contains("autenticacao")) {
                throw new Exception("ERRO DE AUTENTICACAO - API PERFORMAXXI: " + mensagemErro, e);
            }

            throw e;
        }
    }

    private void definirMensagemRetorno(ContextoAcao ctx, ResultadoIntegracao resultado, List<PedidoData> pedidos) {
        int sucessos = resultado.getSucessos();
        int erros = resultado.getErros();
        PerformaxxiAPI.RespostaAPI respostaAPI = resultado.getRespostaAPI();

        String retornoAPI = criarMensagemRetornoAPI(respostaAPI);

        if (erros == 0) {

            popularCamposRespostaAPI(pedidos, respostaAPI);
            String registrosEnviados = PerformaxxiIntegracaoHelper.criarListaRegistrosEnviados(pedidos);
            String mensagemCompleta = "Integracao Performaxxi concluida com sucesso!\n\n" + retornoAPI + "\n\n" + registrosEnviados;
            ctx.setMensagemRetorno(PerformaxxiAPI.createSuccessMessageWithRecords(
                mensagemCompleta,
                pedidos.size(),
                System.currentTimeMillis(),
                ""));
        } else if (sucessos > 0) {
            String mensagemCompleta = String.format("Integracao concluida parcialmente - Sucessos: %d | Erros: %d\n\n%s",
                sucessos, erros, retornoAPI);
            ctx.setMensagemRetorno(PerformaxxiAPI.createWarningMessage(mensagemCompleta));
        } else {
            String mensagemCompleta = String.format("Integracao falhou - %d erro(s) encontrado(s)\n\n%s",
                erros, retornoAPI);
            ctx.setMensagemRetorno(PerformaxxiAPI.createErrorMessage(mensagemCompleta, System.currentTimeMillis()));
        }
    }

    private String criarMensagemRetornoAPI(PerformaxxiAPI.RespostaAPI respostaAPI) {
        if (respostaAPI == null) {
            return "Retorno da API: Não disponível";
        }

        StringBuilder retorno = new StringBuilder();
        retorno.append("=== RETORNO DA API PERFORMAXXI ===\n");
        retorno.append("{\n");
        retorno.append("  \"Valor\": ").append(respostaAPI.valor).append(",\n");
        retorno.append("  \"Sucesso\": ").append(respostaAPI.sucesso ? "true" : "false").append(",\n");
        retorno.append("  \"MensagemErro\": ").append(respostaAPI.mensagemErro != null ? "\"" + respostaAPI.mensagemErro + "\"" : "null").append(",\n");
        retorno.append("  \"CodigoErro\": ").append(respostaAPI.codigoErro != null ? "\"" + respostaAPI.codigoErro + "\"" : "null").append("\n");
        retorno.append("}");

        return retorno.toString();
    }

    private String gerarNumeroLote() {
        return "LOTE_" + System.currentTimeMillis();
    }

    private String criarJsonParametros(Map<String, Object> params) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        boolean primeiro = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (!primeiro) {
                json.append(", ");
            }
            json.append("\"").append(entry.getKey()).append("\": ");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            primeiro = false;
        }
        json.append("}");
        return json.toString();
    }

    private void popularCamposRespostaAPI(List<PedidoData> pedidos, PerformaxxiAPI.RespostaAPI respostaAPI) {
        if (respostaAPI != null) {
            for (PedidoData pedido : pedidos) {
                pedido.valorResposta = respostaAPI.valor;
                pedido.sucessoResposta = respostaAPI.sucesso;
            }
        }
    }
}
