package br.com.jonatanCode.primiero;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;


public class Primeiro_botao_acao implements AcaoRotinaJava

{
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        System.out.println("Testes de Mensagem!!");

        contextoAcao.setMensagemRetorno("Meu primiero Pojeto satya");

    }
}
