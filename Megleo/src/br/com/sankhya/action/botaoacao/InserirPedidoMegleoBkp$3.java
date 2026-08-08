package br.com.sankhya.action.botaoacao;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import java.math.BigDecimal;

class InserirPedidoMegleoBkp.3
implements JapeSession.TXBlock {
    final  String val$chave_cotacao;
    final  String val$etapa;
    final  BigDecimal val$nunota;
    final  String val$transportadora_nome;
    final  String val$transportadora_cnpj;
    final  BigDecimal val$valor_fatura_float;
    final  String val$dataFormatada;
    final  String val$prazo;

    InserirPedidoMegleoBkp.3(String string, String string2, BigDecimal bigDecimal, String string3, String string4, BigDecimal bigDecimal2, String string5, String string6) {
        this.val$chave_cotacao = string;
        this.val$etapa = string2;
        this.val$nunota = bigDecimal;
        this.val$transportadora_nome = string3;
        this.val$transportadora_cnpj = string4;
        this.val$valor_fatura_float = bigDecimal2;
        this.val$dataFormatada = string5;
        this.val$prazo = string6;
    }

    public void doWithTx() throws Exception {
        ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)JapeFactory.dao((String)"AD_PEDMEG").prepareToUpdateByPK(new Object[]{this.val$chave_cotacao}).set("ETAPA", (Object)this.val$etapa)).set("NUNOTA", (Object)this.val$nunota)).set("TRANSPORTADORA", (Object)this.val$transportadora_nome)).set("CNPJ_TRANSPORTADORA", (Object)this.val$transportadora_cnpj)).set("VALOR_FRETE", (Object)this.val$valor_fatura_float)).set("DATA_CRIACAO", (Object)this.val$dataFormatada)).set("PRAZO", (Object)this.val$prazo)).update();
    }
}
