package br.com.emfal.action.botaoAcao;

import br.com.emfal.serasa.SerasaHelper;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class ConsultaSerasa implements AcaoRotinaJava {

    private final SerasaHelper serasaHelper = new SerasaHelper();

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        System.out.println("=== INICIO CONSULTA SERASA SANKHYA ===");

        if (ctx.getLinhas() == null || ctx.getLinhas().length == 0) {
            ctx.setMensagemRetorno("Nenhum registro selecionado!");
            return;
        }

        try {
            for (int i = 0; i < ctx.getLinhas().length; i++) {
                processarLinha(ctx.getLinhas()[i]);
            }

            ctx.setMensagemRetorno("Consulta Serasa realizada com sucesso!");

        } catch (Exception e) {
            ctx.setMensagemRetorno("ERRO na consulta Serasa: " + e.getMessage());
            throw e;
        }

        System.out.println("=== FIM CONSULTA SERASA SANKHYA ===");
    }

    private void processarLinha(Object linha) {
        try {
            System.out.println("Processando linha: " + linha);

        } catch (Exception e) {
            System.err.println("Erro ao processar linha: " + e.getMessage());
        }
    }
}
