package br.com.sankhya.action.funcoes;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import java.math.BigDecimal;

class EnviaPedido.1
implements JapeSession.TXBlock {
    final  BigDecimal val$nunota;
    final  BigDecimal val$codparc;
    final  String val$tipfrete;
    final  String val$cif_fob;
    final  BigDecimal val$finalValor_fatura_float;

    EnviaPedido.1(BigDecimal bigDecimal, BigDecimal bigDecimal2, String string, String string2, BigDecimal bigDecimal3) {
        this.val$nunota = bigDecimal;
        this.val$codparc = bigDecimal2;
        this.val$tipfrete = string;
        this.val$cif_fob = string2;
        this.val$finalValor_fatura_float = bigDecimal3;
    }

    public void doWithTx() throws Exception {
        ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)JapeFactory.dao((String)"CabecalhoNota").prepareToUpdateByPK(new Object[]{this.val$nunota}).set("CODPARCTRANSP", (Object)this.val$codparc)).set("TIPFRETE", (Object)this.val$tipfrete)).set("CIF_FOB", (Object)this.val$cif_fob)).set("VLRFRETE", (Object)this.val$finalValor_fatura_float)).update();
        System.out.println("Megleo - #7 Nota atualizada com os valores recebidos. \nC\u00f3d. Parceiro: " + this.val$codparc + "\nTipo de Frete: " + this.val$tipfrete + "\nCIF_FOB: " + this.val$cif_fob + "\nValor do Frete: " + this.val$finalValor_fatura_float);
    }
}
