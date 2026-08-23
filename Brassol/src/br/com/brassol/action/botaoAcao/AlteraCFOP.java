package br.com.brassol.action.botaoAcao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class AlteraCFOP implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        System.out.println("=== INICIO AlteraCFOP BRASOL ===");

        if (ctx.getLinhas() == null || ctx.getLinhas().length == 0) {
            ctx.setMensagemRetorno("Nenhum registro selecionado!");
            return;
        }

        Object paramValue = ctx.getParam("CODCFO");
        if (paramValue == null) {
            ctx.setMensagemRetorno("ERRO: Parametro CODCFO nao informado!");
            return;
        }

        String newCodCFOP = paramValue.toString();
        disableTrigger(ctx);

        try {
            int alteracoes = 0;
            for (int i = 0; i < ctx.getLinhas().length; i++) {
                try {
                    ctx.getLinhas()[i].setCampo("CODCFO", newCodCFOP);
                    alteracoes++;
                } catch (Exception e) {
                    System.err.println("Erro linha " + (i + 1) + ": " + e.getMessage());
                }
            }
            ctx.setMensagemRetorno("Processamento concluido! " + alteracoes + " registro(s) alterado(s).");
        } finally {
            enableTrigger(ctx);
        }

        System.out.println("=== FIM AlteraCFOP BRASOL ===");
    }

    private void disableTrigger(ContextoAcao ctx) throws Exception {
        ctx.getQuery().update("ALTER TRIGGER TRG_UPT_TGFITE DISABLE");
    }

    private void enableTrigger(ContextoAcao ctx) throws Exception {
        ctx.getQuery().update("ALTER TRIGGER TRG_UPT_TGFITE ENABLE");
    }
}
