package me.handz.importacao.model.services;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.dwfdata.vo.TIMPORTCONFVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProcessarArquivosDiretorioJobBean implements SessionBean, Serializable {

    private static final long serialVersionUID = 1L;

    private SessionContext sessionContext;

    @Override
    public void setSessionContext(SessionContext sessionContext) throws EJBException {
        this.sessionContext = sessionContext;
    }

    @Override
    public void ejbRemove() throws EJBException {

    }

    @Override
    public void ejbActivate() throws EJBException {

    }

    @Override
    public void ejbPassivate() throws EJBException {

    }

    public void executarProcessamento() {
        try {

            List<TIMPORTCONFVO> configuracoes = buscarConfiguracoesAtivas();

            if (configuracoes.isEmpty()) {
                return;
            }

            for (TIMPORTCONFVO config : configuracoes) {
                try {
                    processarConfiguracao(config);
                } catch (Exception e) {

                    System.err.println("Erro ao processar configuracao " + config.getIDTELA() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new EJBException("Erro no processamento dos arquivos", e);
        }
    }

    private void processarConfiguracao(TIMPORTCONFVO config) throws Exception {
        if (!"S".equals(config.getUSAIMP())) {
            return;
        }

        String diretorioLer = config.getDIRETORIOLER();
        if (diretorioLer == null || diretorioLer.trim().isEmpty()) {
            return;
        }

        registrarLog(config.getIDTELA(), "Job executado para configuracao: " + config.getNOMEIMPORT(), "INFO");
    }

    private List<TIMPORTCONFVO> buscarConfiguracoesAtivas() throws Exception {
        List<TIMPORTCONFVO> configuracoes = new ArrayList<>();

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();

        String sql = "SELECT * FROM TIMPORTCONF WHERE USAIMP = 'SIM' ORDER BY IDTELA";

        try (NativeSql nativeSql = new NativeSql(jdbc)) {
            try (ResultSet rs = nativeSql.executeQuery(sql)) {
                while (rs.next()) {
                    TIMPORTCONFVO config = new TIMPORTCONFVO();
                    config.setIDTELA(rs.getString("IDTELA"));
                    config.setNOMEIMPORT(rs.getString("NOMEIMPORT"));
                    config.setINSTANCIA(rs.getString("INSTANCIA"));
                    config.setCAMPOARQUIVO(rs.getString("CAMPOARQUIVO"));
                    config.setCAMPONOME(rs.getString("CAMPONOME"));
                    config.setSTPFINAL(rs.getString("STPFINAL"));
                    config.setEVENTOANT(rs.getString("EVENTOANT"));
                    config.setSTPLINHA(rs.getString("STPLINHA"));
                    config.setTIPOARQ(rs.getString("TIPOARQ"));
                    config.setDIRETORIOLER(rs.getString("DIRETORIOLER"));
                    config.setDIRETORIOGRAVAR(rs.getString("DIRETORIOGRAVAR"));
                    config.setFAZERAPOSLER(rs.getString("FAZERAPOSLER"));

                    configuracoes.add(config);
                }
            }
        }

        return configuracoes;
    }

    private void registrarLog(String idTela, String mensagem, String tipo) {
        try {
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();

            BigDecimal proximaSequencia = buscarProximaSequencia(idTela);

            String sql = "INSERT INTO TIMPORTLOG (IDTELA, SEQUENCIA, MENSAGEM, DHIMPORTACAO, TIPO) VALUES (?, ?, ?, ?, ?)";

            try (NativeSql nativeSql = new NativeSql(jdbc)) {
                nativeSql.setParameter(1, idTela);
                nativeSql.setParameter(2, proximaSequencia);
                nativeSql.setParameter(3, mensagem);
                nativeSql.setParameter(4, new java.util.Date());
                nativeSql.setParameter(5, tipo);

                nativeSql.executeUpdate();
            }
        } catch (Exception e) {

            System.err.println("Erro ao registrar log: " + e.getMessage());
        }
    }

    private BigDecimal buscarProximaSequencia(String idTela) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();

        String sql = "SELECT NVL(MAX(SEQUENCIA), 0) + 1 AS PROXIMA_SEQ FROM TIMPORTLOG WHERE IDTELA = ?";

        try (NativeSql nativeSql = new NativeSql(jdbc)) {
            nativeSql.setParameter(1, idTela);
            try (ResultSet rs = nativeSql.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getBigDecimal("PROXIMA_SEQ");
                }
            }
        }

        return BigDecimal.ONE;
    }
}
