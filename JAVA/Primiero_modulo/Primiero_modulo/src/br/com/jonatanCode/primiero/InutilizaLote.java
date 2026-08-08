package br.com.jonatanCode.primiero;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

public class InutilizaLote implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        System.out.println("InutilizaLote inicio de Inutilização");

        Registro codprod = contextoAcao.getLinhaPai();
        BigDecimal nuunico = (BigDecimal) codprod.getCampo("CODPROD");

        String codstatus = (String) contextoAcao.getParam("P_STATUS");

        if(StringUtils.isEmpty(codstatus)){
            // Lança um erro quando o lote fo vazio
            System.out.println("Erro! Status de Lote informado Vazio!");

            contextoAcao.mostraErro("Erro! Informe o Status do Produto, valor não pode ser Vazio");
            return;
        }

        System.out.println("Codido de Status passado "+ codstatus);

        QueryExecutor query = contextoAcao.getQuery();
        query.setParam("P_STATUS", codstatus);
        query.nativeSelect("Select CODUSU from TSIUSU");
        int contador = 0;
        while(query.next()){

        BigDecimal codusu = (BigDecimal) query.getBigDecimal("CODUSU");

            System.out.println("Cod usuario "+codusu);



        contador ++;
        }
        query.close();

        if(contador == 0){
            contextoAcao.setMensagemRetorno("Nenhum produto foi encontrado!");
        }else {
            contextoAcao.setMensagemRetorno("Produto foi encontrado!");
        }
        System.out.println("Finalização de Intilização");
    }

}
