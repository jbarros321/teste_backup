package br.com.satyacode.primeiro_curso;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;

public class DelecaoDeFuncionarioBT implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        System.out.println("Iniciou  DelecaoDeFuncionarioBT");

        for (int i = 0; i < contexto.getLinhas().length; i ++ ){
            System.out.println("INDICE: " + i);

            Registro registro = contexto.getLinhas()[i];

            BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");
            BigDecimal sequencia = (BigDecimal) registro.getCampo("SEQUENCIA");

            System.out.println("Nro Unico: " + nroUnico + " -  Sequencia :" + sequencia  );

            boolean isBotao = contexto.confirmarSimNao("Atenção", "Tem certeza em realizar esta exclusão?", 1);
            if (isBotao){
                registro.remove();

                contexto.setMensagemRetorno("Voce acabou de remover o registro com o incide "+ sequencia);
            } else {

                contexto.setMensagemRetorno("Cancelado ");
            }

        }

        contexto.eMail("Deleção de uma linha", "Estamos tesntaod o envio do email", "daniel.araujo@satyacode.com.br");

        System.out.println("Finalizou   DelecaoDeFuncionarioBT");

    }
}
