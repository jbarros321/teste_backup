package br.com.sankhya.action.funcoes;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import java.math.BigDecimal;

class EnviaPedido.2
implements JapeSession.TXBlock {
    final  String val$chave_cotacao;
    final  String val$etapa;
    final  BigDecimal val$nunota;
    final  String val$finalTransportadora_nome;
    final  String val$finalTransportadora_cnpj;
    final  BigDecimal val$finalValor_fatura_float1;
    final  String val$dataFormatada;
    final  String val$finalPrazo;
    final  String val$paramVlrFrete;

    EnviaPedido.2(String string, String string2, BigDecimal bigDecimal, String string3, String string4, BigDecimal bigDecimal2, String string5, String string6, String string7) {
        this.val$chave_cotacao = string;
        this.val$etapa = string2;
        this.val$nunota = bigDecimal;
        this.val$finalTransportadora_nome = string3;
        this.val$finalTransportadora_cnpj = string4;
        this.val$finalValor_fatura_float1 = bigDecimal2;
        this.val$dataFormatada = string5;
        this.val$finalPrazo = string6;
        this.val$paramVlrFrete = string7;
    }

    public void doWithTx() throws Exception {
        JapeWrapper insert1 = JapeFactory.dao((String)"AD_PEDMEG");
        DynamicVO insertVO = ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)insert1.create().set("CHAVERASTREIO", (Object)this.val$chave_cotacao)).set("ETAPA", (Object)this.val$etapa)).set("NUNOTA", (Object)this.val$nunota)).set("TRANSPORTADORA", (Object)this.val$finalTransportadora_nome)).set("CNPJ_TRANSPORTADORA", (Object)this.val$finalTransportadora_cnpj)).set("VALOR_FRETE", (Object)this.val$finalValor_fatura_float1)).set("DATA_CRIACAO", (Object)this.val$dataFormatada)).set("PRAZO", (Object)this.val$finalPrazo)).set("ATUALIZA_FRETE", (Object)this.val$paramVlrFrete)).save();
        System.out.println("Megleo - #9 Criado registro na tabela de Pedidos Megleo. Chave de cota\u00e7\u00e3o: " + this.val$chave_cotacao);
    }
}
