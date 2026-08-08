package br.com.sankhya.action.RegraNegocio;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;

public class Modelo
implements Regra {
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
        boolean isFinanceiro;
        PrePersistEntityState state = contextoRegra.getPrePersistEntityState();
        DynamicVO newVO = state.getNewVO();
        boolean isCabecalho = newVO.getValueObjectID().indexOf("CabecalhoNota") > -1;
        boolean isItem = newVO.getValueObjectID().indexOf("ItemNota") > -1;
        boolean bl = isFinanceiro = newVO.getValueObjectID().indexOf("Financeiro") > -1;
        if (isCabecalho) {

        }
        if (isItem) {

        }
        if (isFinanceiro) {

        }
    }

    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
    }

    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {
    }

    public void afterInsert(ContextoRegra contextoRegra) throws Exception {
    }

    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {
    }

    public void afterDelete(ContextoRegra contextoRegra) throws Exception {
    }
}
