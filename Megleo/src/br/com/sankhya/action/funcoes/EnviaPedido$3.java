package br.com.sankhya.action.funcoes;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import java.math.BigDecimal;

class EnviaPedido.3
implements JapeSession.TXBlock {
    final  String val$chave_cotacao;
    final  String val$etapa;
    final  BigDecimal val$nunota;
    final  String val$finalTransportadora_nome1;
    final  String val$finalTransportadora_cnpj1;
    final  BigDecimal val$finalValor_fatura_float2;
    final  String val$dataFormatada;
    final  String val$finalPrazo1;
    final  String val$paramVlrFrete;

    EnviaPedido.3(String string, String string2, BigDecimal bigDecimal, String string3, String string4, BigDecimal bigDecimal2, String string5, String string6, String string7) {
        this.val$chave_cotacao = string;
        this.val$etapa = string2;
        this.val$nunota = bigDecimal;
        this.val$finalTransportadora_nome1 = string3;
        this.val$finalTransportadora_cnpj1 = string4;
        this.val$finalValor_fatura_float2 = bigDecimal2;
        this.val$dataFormatada = string5;
        this.val$finalPrazo1 = string6;
        this.val$paramVlrFrete = string7;
    }

    public void doWithTx() throws Exception {
        ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)JapeFactory.dao((String)"AD_PEDMEG").prepareToUpdateByPK(new Object[]{this.val$chave_cotacao}).set("ETAPA", (Object)this.val$etapa)).set("NUNOTA", (Object)this.val$nunota)).set("TRANSPORTADORA", (Object)this.val$finalTransportadora_nome1)).set("CNPJ_TRANSPORTADORA", (Object)this.val$finalTransportadora_cnpj1)).set("VALOR_FRETE", (Object)this.val$finalValor_fatura_float2)).set("DATA_CRIACAO", (Object)this.val$dataFormatada)).set("PRAZO", (Object)this.val$finalPrazo1)).set("ATUALIZA_FRETE", (Object)this.val$paramVlrFrete)).update();
        System.out.println("Megleo - #10 Criado registro na tabela de Pedidos Megleo. Chave de cota\u00e7\u00e3o: " + this.val$chave_cotacao);
    }
}
