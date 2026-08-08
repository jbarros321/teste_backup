package br.com.satyacode.satyapass.log.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.satyapass.log.exception.LogServiceException;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusExecucaoEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import br.com.satyacode.satyapass.log.model.StatusLogEnum;
import br.com.satyacode.satyapass.log.repository.LogItemRepository;
import br.com.satyacode.satyapass.log.repository.LogRepository;
import br.com.satyacode.satyapass.log.repository.LogServicoRepository;
import com.sankhya.util.TimeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

public class LogService {

    private LogRepository logRepository;
    private LogItemRepository logItemRepository;
    private LogServicoRepository logServicoRepository;

    public LogService() {
        this.logRepository = new LogRepository();
        this.logItemRepository = new LogItemRepository();
        this.logServicoRepository = new LogServicoRepository();
    }

    public BigDecimal incluirLog(BigDecimal codServico, ModalidadeEnum modalidade, StatusExecucaoEnum statusExecucao, BigDecimal codUsuario, boolean isTransacaoAutomatica) throws LogServiceException {
        try {
            BigDecimal codLog = BigDecimal.ZERO;
            if (logServicoRepository.isServicoEstaAtivo(codServico)) {
                if (isTransacaoAutomatica) {
                    codLog = logRepository.incluirLogPai(codServico, modalidade, statusExecucao, codUsuario);
                } else {
                    codLog = logRepository.incluirLogPaiTM(codServico, modalidade, statusExecucao, codUsuario);
                }
            } else {
                System.out.println(String.format("LOG: => Cód. Serviço: %s | Modalidade: %s | Status Execução: %s | Cód. Usuário: %s", codServico, modalidade, statusExecucao, codUsuario));
            }
            return codLog;
        } catch (Exception e) {
            System.out.println("Erro ao incluir Log: " + ExceptionUtils.getStackTrace(e));
            throw new LogServiceException("Erro ao incluir Log: ", e);
        }
    }

    public void incluirItem(BigDecimal codLog, String descricao, String atividade, String categoria, StatusItemEnum status, boolean isTransacaoAutomatica) throws LogServiceException {
        try {
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            String mensagemConsole = "LOG ITEM: Cód. Log: " + codLog + " | Descrição: " + descricao + " | Atividade: " + atividade + " | Status:" + status;
            if (codLog.compareTo(BigDecimal.ZERO) > 0) {
                DynamicVO logVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_STPLOG", new Object[]{codLog});
                if (logServicoRepository.isServicoEstaAtivo(logVO.asBigDecimal("CODSER"))) {
                    if (isTransacaoAutomatica) {
                        logItemRepository.incluirLogFilho(codLog, descricao, atividade, categoria, status);
                    } else {
                        logItemRepository.incluirLogFilhoTM(codLog, descricao, atividade, categoria, status);
                    }
                } else {
                    System.out.println(mensagemConsole);
                }
            } else {
                System.out.println(mensagemConsole);
            }
        } catch (Exception e) {
            System.out.println("Erro ao incluir log filho: " + ExceptionUtils.getStackTrace(e));
            throw new LogServiceException("Erro ao incluir log filho: ", e);
        }
    }

    public void atualizarStatusLogPai(BigDecimal codLog, boolean isTransacaoAutomatica) throws Exception {
        try {
            if (codLog.compareTo(BigDecimal.ZERO) == 0)
                return;

            boolean isErro = logItemRepository.isApresentouErro(codLog);
            StatusLogEnum statusLogEnum = null;
            if (isErro) {
                statusLogEnum = StatusLogEnum.INCONSISTENCIA;
            } else {
                statusLogEnum = StatusLogEnum.SUCESSO;
            }
            if (isTransacaoAutomatica) {
                logRepository.atualizarStatus(codLog, statusLogEnum);
            } else {
                logRepository.atualizarStatusTM(codLog, statusLogEnum);
            }
        } catch (Exception e) {
            System.out.println("Erro ao atualizar status pai: " + ExceptionUtils.getStackTrace(e));
            throw new LogServiceException("Erro ao atualizar status pai: ", e);
        }
    }

    public void atualizarStatusDaExecucao(BigDecimal codLog, StatusExecucaoEnum statusExecucaoEnum, boolean isTransacaoAutomatica){
        try {
            if (codLog.compareTo(BigDecimal.ZERO) == 0)
                return;

            if (isTransacaoAutomatica){
                logRepository.atualizarStatusExecucao(codLog, statusExecucaoEnum);
            } else {
                logRepository.atualizarStatusExecucaoTM(codLog, statusExecucaoEnum);
            }

        } catch (Exception e) {
            System.out.println("Erro ao atualizar status da execução: " + ExceptionUtils.getStackTrace(e));
            throw new LogServiceException("Erro ao atualizar status da execução: ", e);
        }
    }

    public void gerenciarExclusaodaRotinaDeLogs(boolean isTransacaoAutomatica) throws Exception {
        try {
            EntityFacade dwffacade = EntityFacadeFactory.getDWFFacade();
            FinderWrapper finder = new FinderWrapper("AD_STPSER", "this.ATIVO = 'S'");
            Collection<DynamicVO> servicosVO = dwffacade.findByDynamicFinderAsVO(finder);
            if (!servicosVO.isEmpty()) {
                for (DynamicVO servicoVO : servicosVO) {
                    int qtdDias = servicoVO.asBigDecimalOrZero("QTDDIAS").intValue() * -1;
                    Timestamp dataDeExclusao = TimeUtils.dataAddDay(TimeUtils.getNow(), qtdDias);
                    finder = new FinderWrapper("AD_STPLOG", "this.DTEXEC <= ?", new Object[]{dataDeExclusao});
                    Collection<DynamicVO> logsCabecalhoVO = dwffacade.findByDynamicFinderAsVO(finder);
                    if (!logsCabecalhoVO.isEmpty()) {
                        for (DynamicVO logVO : logsCabecalhoVO) {
                            BigDecimal codLog = logVO.asBigDecimal("CODLOG");
                            if (isTransacaoAutomatica) {
                                logItemRepository.deletarLogFilho(codLog);
                                logRepository.deletarLog(codLog);
                            } else {
                                logItemRepository.deletarLogFilhoTM(codLog);
                                logRepository.deletarLogTM(codLog);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao deletar log: " + ExceptionUtils.getStackTrace(e));
            throw new LogServiceException("Erro ao deletar log: ", e);
        }
    }
}
