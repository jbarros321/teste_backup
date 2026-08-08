package br.com.satyacode.primeiro_curso;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;

public class CheckingFuncionarioBT implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        System.out.println("Iniciou  CheckingFuncionarioBT");

        for (int i = 0; i < contexto.getLinhas().length; i ++ ){
            System.out.println("INDICE: " + i);

            Registro registro = contexto.getLinhas()[i];

            BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");
            BigDecimal sequencia = (BigDecimal) registro.getCampo("SEQUENCIA");

            System.out.println("Nro Unico: " + nroUnico + " -  Sequencia :" + sequencia  );

            QueryExecutor query = contexto.getQuery();
            query.setParam("NROUNICO", nroUnico);
            query.setParam("SEQUENCIA", sequencia);
            query.update("UPDATE AD_TREITE SET CHECKIN = 'S' WHERE NROUNICO = {NROUNICO} AND SEQUENCIA = {SEQUENCIA}");
            query.close();

        }

        contexto.setMensagemRetorno("Rotina executada com sucesso! ");

        System.out.println("Finalizou   CheckingFuncionarioBT");

    }
}
