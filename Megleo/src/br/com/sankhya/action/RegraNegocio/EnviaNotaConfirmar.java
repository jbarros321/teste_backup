package br.com.sankhya.action.RegraNegocio;

import br.com.sankhya.action.funcoes.EnviaPedido;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import java.math.BigDecimal;

public class EnviaNotaConfirmar
implements Regra {
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
    }

    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
    }

    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {
    }

    public void afterInsert(ContextoRegra contextoRegra) throws Exception {
    }

    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {
        boolean isFinanceiro;
        PrePersistEntityState state = contextoRegra.getPrePersistEntityState();
        DynamicVO newVO = state.getNewVO();
        boolean isCabecalho = newVO.getValueObjectID().indexOf("CabecalhoNota") > -1;
        boolean isItem = newVO.getValueObjectID().indexOf("ItemNota") > -1;
        boolean bl = isFinanceiro = newVO.getValueObjectID().indexOf("Financeiro") > -1;
        if (isCabecalho && JapeSession.getProperty((String)"CabecalhoNota.confirmando.nota") != null) {
            BigDecimal codtipoper = newVO.asBigDecimal("CODTIPOPER");
            BigDecimal nunota = newVO.asBigDecimal("NUNOTA");
            BigDecimal codparctransp = newVO.asBigDecimal("CODPARCTRANSP");
            if (codparctransp == null) {
                return;
            }
            if (codparctransp.compareTo(new BigDecimal(0)) == 0) {
                return;
            }
            JapeWrapper topDAO = JapeFactory.dao((String)"TipoOperacao");
            DynamicVO topVO = topDAO.findOne("CODTIPOPER = ? AND DHALTER = (SELECT MAX(DHALTER) FROM TGFTOP WHERE CODTIPOPER = ?) ", new Object[]{codtipoper, codtipoper});
            String criaRegistroVenda = topVO.asString("AD_MEGENVIADOC");
            if (criaRegistroVenda != null && criaRegistroVenda.equalsIgnoreCase("S")) {
                EnviaPedido enviaPedido = new EnviaPedido();
                enviaPedido.enviaPedidoMegleo(nunota, false);
                contextoRegra.getBarramentoRegra().addMensagem("Documento enviado \u00e0 Megleo.");
            }
        }
    }

    public void afterDelete(ContextoRegra contextoRegra) throws Exception {
    }
}
