package br.com.satyacode.satyapass.acessodados;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.satyacode.satyapass.acessodados.crud.EntityFacadeExemplo;
import br.com.satyacode.satyapass.acessodados.crud.JapewrapperExemplo;
import br.com.satyacode.satyapass.acessodados.crud.NativeSqlExemplo;

import java.math.BigDecimal;

public class BotaoDeAcao implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        System.out.println("Acesso a Dados - Satya Code");

        JapewrapperExemplo japewrapperExemplo = new JapewrapperExemplo();

        for (Registro registro: contexto.getLinhas()){
            BigDecimal sequencia = (BigDecimal) registro.getCampo("SEQUENCIA");

            japewrapperExemplo.removerPelaPk(sequencia);
        }

        EntityFacadeExemplo entityFacadeExemplo = new EntityFacadeExemplo();

        NativeSqlExemplo nativeSqlExemplo = new NativeSqlExemplo();

        for (Registro registro: contexto.getLinhas()){
            BigDecimal sequencia = (BigDecimal) registro.getCampo("SEQUENCIA");

            nativeSqlExemplo.remover(sequencia);
        }

    }
}
