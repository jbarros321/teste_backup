package br.com.satyacode.satyapass.log.repository;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusExecucaoEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import br.com.satyacode.satyapass.log.model.StatusLogEnum;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class LogRepository {

    private static String nomeTabelaDeLog = "AD_STPLOG";

    public BigDecimal incluirLogPai(BigDecimal codServico, ModalidadeEnum modalidade, StatusExecucaoEnum statusExecucao, BigDecimal codUsuario) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO logVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(nomeTabelaDeLog);
        logVO.setProperty("CODSER", codServico);
        logVO.setProperty("DTEXEC", TimeUtils.getNow());
        logVO.setProperty("MODALIDADE", modalidade.getValue());
        logVO.setProperty("STATUSEXEC", statusExecucao.getValue());
        logVO.setProperty("CODUSU", codUsuario);
        DynamicVO novoLogVO = (DynamicVO) dwfFacade.createEntity(nomeTabelaDeLog, (EntityVO) logVO).getValueObject();
        return novoLogVO.asBigDecimal("CODLOG");
    }

    public BigDecimal incluirLogPaiTM(BigDecimal codServico, ModalidadeEnum modalidade, StatusExecucaoEnum statusExecucao, BigDecimal codUsuario) throws MGEModelException {
        final BigDecimal[] codLog = {BigDecimal.ZERO};
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                    codLog[0] = incluirLogPai(codServico, modalidade, statusExecucao, codUsuario);
                }
            });
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return codLog[0];
    }

    public void atualizarStatus(BigDecimal codLog, StatusLogEnum statusLogEnum) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();

            JapeFactory.dao(nomeTabelaDeLog).
                    prepareToUpdateByPK(codLog)
                    .set("STATUSLOG", statusLogEnum.getValue())
                    .update();

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public void atualizarStatusTM(BigDecimal codLog, StatusLogEnum statusLogEnum) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                    atualizarStatus(codLog, statusLogEnum);
                }
            });
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public void atualizarStatusExecucao(BigDecimal codLog, StatusExecucaoEnum statusExecucaoEnum) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();

            JapeFactory.dao(nomeTabelaDeLog).
                    prepareToUpdateByPK(codLog)
                    .set("STATUSEXEC", statusExecucaoEnum.getValue())
                    .update();

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public void atualizarStatusExecucaoTM(BigDecimal codLog, StatusExecucaoEnum statusExecucaoEnum) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                    atualizarStatusExecucao(codLog, statusExecucaoEnum);
                }
            });
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public void deletarLog(BigDecimal codLog) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(nomeTabelaDeLog).delete(codLog);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public void deletarLogTM(BigDecimal codLog) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                    deletarLog(codLog);
                }
            });
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

}
