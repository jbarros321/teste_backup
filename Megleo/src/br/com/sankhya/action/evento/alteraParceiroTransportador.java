package br.com.sankhya.action.evento;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import java.math.BigDecimal;

public class alteraParceiroTransportador
implements EventoProgramavelJava {
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO)persistenceEvent.getVo();
        try {
            ModifingFields modifingFields = persistenceEvent.getModifingFields();
            if (modifingFields.isModifingAny("AD_NUCOTMEG") && modifingFields.getNewValue("AD_NUCOTMEG") != null) {
                JapeWrapper cabDAO = JapeFactory.dao((String)"CabecalhoNota");
                JapeWrapper pedDAO = JapeFactory.dao((String)"AD_PEDMEG");
                BigDecimal nunota = cabVO.asBigDecimal("NUNOTA");
                DynamicVO pedVO = pedDAO.findOne("NUNOTA = ?", new Object[]{nunota});
                String atualizaFrete = pedVO.asString("ATUALIZA_FRETE");
                BigDecimal vlrFreteInicial = pedVO.asBigDecimal("VALOR_FRETE");
                if (vlrFreteInicial == null) {
                    vlrFreteInicial = new BigDecimal(0);
                }
                JapeWrapper cotDAO = JapeFactory.dao((String)"AD_COTMEG");
                BigDecimal nunicoCotacao = cabVO.asBigDecimal("AD_NUCOTMEG");
                DynamicVO cotVO = cotDAO.findOne("NUNICO = ?", new Object[]{nunicoCotacao});
                BigDecimal vlrFrete = cotVO.asBigDecimal("VLRFRETE");
                BigDecimal codparc = cotVO.asBigDecimal("CODPARC");
                BigDecimal vlrFreteAtualizar = new BigDecimal(0);
                if (atualizaFrete != null) {
                    if (atualizaFrete.trim().equalsIgnoreCase("N")) {
                        vlrFreteAtualizar = new BigDecimal(0);
                    }
                    if (atualizaFrete.trim().equalsIgnoreCase("S")) {
                        vlrFreteAtualizar = vlrFrete;
                    }
                    if (atualizaFrete.trim().equalsIgnoreCase("D")) {
                        vlrFreteAtualizar = vlrFrete.subtract(vlrFreteInicial);
                    }
                }
                ((FluidUpdateVO)((FluidUpdateVO)cabDAO.prepareToUpdate(cabVO).set("CODPARCTRANSP", (Object)codparc)).set("VLRFRETE", (Object)vlrFreteAtualizar)).update();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {
    }

    public void beforeCommit(TransactionContext transactionContext) throws Exception {
    }
}
