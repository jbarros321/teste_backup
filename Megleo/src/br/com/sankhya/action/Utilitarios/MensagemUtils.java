package br.com.sankhya.action.Utilitarios;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class MensagemUtils {
    public static void disparaErro(String msg) throws Exception {
        String msgTratada = "<hr><b><span style=\"font-size: 1.2em\">" + msg + "</span></b><hr>";
        throw new Exception(msgTratada);
    }

    public static void disparaMensagem(ContextoAcao contexto, String mensagem) throws Exception {
        String msgTratada = "<br><br><hr><b><span style=\"font-size: 1.2em\">" + mensagem + "</span></b><hr><br><br>";
        contexto.setMensagemRetorno(msgTratada);
    }
}
