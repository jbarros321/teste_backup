package br.com.satyapass.abastecimento.log;

import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusExecucaoEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import br.com.satyacode.satyapass.log.service.LogService;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

public class AbastecimentoLogFactory {

    private static final ThreadLocal<BigDecimal> CODIGO_LOG = new ThreadLocal<>();

    public static void setCodLog(BigDecimal info) {
        CODIGO_LOG.set(info);
    }

    public static BigDecimal getCodLog() {
        return CODIGO_LOG.get();
    }

    public static void getLimparCodigoDoLog() {
        CODIGO_LOG.remove();
    }

    public static void incluirLogPai(ModalidadeEnum modalidade, BigDecimal codUsuario, boolean isTransacaoAutomatica) throws Exception {
        BigDecimal codServico = BigDecimalUtil.valueOf(MGECoreParameter.getParameterAsInt("LOGABAST"));
        incluirLogPai(codServico,modalidade, StatusExecucaoEnum.EM_ANDAMENTO, codUsuario,isTransacaoAutomatica );
    }

    public static void incluirLogPai(BigDecimal codServico, ModalidadeEnum modalidade, StatusExecucaoEnum statusExecucao, BigDecimal codUsuario, boolean isTransacaoAutomatica){
        LogService logService = new LogService();
        BigDecimal codLog = logService.incluirLog(codServico, modalidade, statusExecucao, codUsuario, isTransacaoAutomatica);
        setCodLog(codLog);
    }

    public static void incluirItem(String descricao, String atividade, String categoria, StatusItemEnum status, boolean isTransacaoAutomatica) {
        LogService logService = new LogService();
        if (StringUtils.isNotEmpty(getCodLog())){
            logService.incluirItem(getCodLog(),descricao, atividade,categoria, status, isTransacaoAutomatica);
        } else {
            System.out.println("Não foi encontrado log pai. ");
        }
    }

    public static void incluirItem(String descricao,  String categoria, StatusItemEnum status, boolean isTransacaoAutomatica) {
        incluirItem(descricao, "-", categoria, status, isTransacaoAutomatica);
    }

    public static void finalizarLog(){
        if (StringUtils.isNotEmpty(getCodLog())){
            LogService logService = new LogService();
            try {
                logService.atualizarStatusLogPai(getCodLog(), true);
                logService.atualizarStatusDaExecucao(getCodLog(), StatusExecucaoEnum.EXECUTADO, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        getLimparCodigoDoLog();
    }

}
