package br.com.satyacode.satyapass.log.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class LogItemRepository {

    private static String nomeTabelaDeLog = "AD_STPLOGITE";

    public void incluirLogFilho(BigDecimal codLog, String descricao, String atividade, String categoria, StatusItemEnum status) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(nomeTabelaDeLog);
        itemVO.setProperty("CODLOG", codLog);
        itemVO.setProperty("TEMPORIZADOR", TimeUtils.getNow());
        itemVO.setProperty("DESCRICAO", descricao);
        itemVO.setProperty("ATIVIDADE", atividade.toCharArray());
        itemVO.setProperty("CATEGORIA", categoria);
        itemVO.setProperty("STATUS", status.getValue());
        dwfFacade.createEntity(nomeTabelaDeLog, (EntityVO) itemVO);
    }

    public void incluirLogFilhoTM(BigDecimal codLog, String descricao, String atividade, String categoria, StatusItemEnum status) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                    incluirLogFilho(codLog, descricao, atividade, categoria, status);
                }
            });
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public boolean isApresentouErro(BigDecimal codLog) throws Exception {

        BigDecimal qtdErro = NativeSql.getBigDecimal("COUNT(*)", "AD_STPLOGITE", "CODLOG = " + codLog + " AND STATUS = 'ERRO'");
        if (qtdErro.compareTo(BigDecimal.ZERO) > 0)
            return true;
        else
            return false;
    }

    public void deletarLogFilho(BigDecimal codLog) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper dao = JapeFactory.dao(nomeTabelaDeLog);
            dao.deleteByCriteria("CODLOG = ? ", codLog);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public void deletarLogFilhoTM(BigDecimal codLog) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                    deletarLogFilho(codLog);
                }
            });
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }
}
