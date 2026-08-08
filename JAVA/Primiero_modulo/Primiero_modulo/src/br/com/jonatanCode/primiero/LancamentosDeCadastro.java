package br.com.jonatanCode.primiero;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class LancamentosDeCadastro implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        System.out.println("Iniico de Operação");

        // Aqui sera os parametros atribuidos

        BigDecimal codprod = BigDecimalUtil.valueOf((String)contextoAcao.getParam("P_PROD"));
        String controle = (String)contextoAcao.getParam("P_LOTE");

        if(StringUtils.isEmpty(controle)){
                                                                   // Lança um erro quando o lote fo vazio
            System.out.println("Erro! Informe o Status do Produto");
            contextoAcao.mostraErro("Erro! Informe o Status do Produto, valor não pode ser Vazio");
        }

        // Incluindo um novo cadastro

        Registro produto = contextoAcao.novaLinha("AD_APONTLOTEINU");
        produto.setCampo("CODPROD",codprod );
        produto.setCampo("CONTROLE",controle );
        produto.setCampo("DTINCLUSAO", TimeUtils.getNow() );
        produto.setCampo("CODUSU", contextoAcao.getUsuarioLogado());
        produto.save();

        Object sequencia = produto.getCampo("SEQUENCIA"); // Pega a PK da tabela.

        StringBuilder mensagem = new StringBuilder();
        mensagem.append("Lancamento realizado com sucesso!").append(" Nro unico: ").append(sequencia);

        contextoAcao.setMensagemRetorno(mensagem.toString());

        System.out.println("Fim de Operação");


    }
}
