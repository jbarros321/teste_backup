package br.com.cliente.util;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class MessageHelper {
    public static void mostrarErro(ContextoAcao contexto, String mensagem) throws Exception {
        contexto.setMensagemRetorno("<hr><b><span style=\"font-size: 1.2em; color: red;\">" + mensagem + "</span></b><hr>");
    }

    public static void mostrarSucesso(ContextoAcao contexto, String mensagem) throws Exception {
        contexto.setMensagemRetorno("<hr><b><span style=\"font-size: 1.2em; color: green;\">" + mensagem + "</span></b><hr>");
    }

    public static void mostrarInfo(ContextoAcao contexto, String mensagem) throws Exception {
        contexto.setMensagemRetorno("<hr><b><span style=\"font-size: 1.2em; color: blue;\">" + mensagem + "</span></b><hr>");
    }
}
