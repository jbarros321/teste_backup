package br.com.sankhya.action.botaoacao;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import java.math.BigDecimal;

class InserirPedidoMegleoBkp.1
implements JapeSession.TXBlock {
    final  BigDecimal val$nunota;
    final  BigDecimal val$CODPARC;
    final  String val$tipfrete;
    final  String val$cif_fob;
    final  BigDecimal val$valor_fatura_float;

    InserirPedidoMegleoBkp.1(BigDecimal bigDecimal, BigDecimal bigDecimal2, String string, String string2, BigDecimal bigDecimal3) {
        this.val$nunota = bigDecimal;
        this.val$CODPARC = bigDecimal2;
        this.val$tipfrete = string;
        this.val$cif_fob = string2;
        this.val$valor_fatura_float = bigDecimal3;
    }

    public void doWithTx() throws Exception {
        ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)JapeFactory.dao((String)"CabecalhoNota").prepareToUpdateByPK(new Object[]{this.val$nunota}).set("CODPARCTRANSP", (Object)this.val$CODPARC)).set("TIPFRETE", (Object)this.val$tipfrete)).set("CIF_FOB", (Object)this.val$cif_fob)).set("VLRFRETE", (Object)this.val$valor_fatura_float)).update();
    }
}
