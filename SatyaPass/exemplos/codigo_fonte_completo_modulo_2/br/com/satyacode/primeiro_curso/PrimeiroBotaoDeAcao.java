package br.com.satyacode.primeiro_curso;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class PrimeiroBotaoDeAcao  implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        System.out.println(" SATYA CODE - Primeiro Projeto");

        contexto.setMensagemRetorno("Nosso primeiro projeto!");

    }
}
