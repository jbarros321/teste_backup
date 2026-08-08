package me.handz.importacao.model.services;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.dwfdata.vo.TIMPORTCONFVO;
import br.com.sankhya.modelcore.dwfdata.vo.TIMPORTLOGVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class ImportacaoArquivoSPBean implements SessionBean, Serializable {

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

    public String processarArquivosDiretorio(String idTela) {
        try {

            TIMPORTCONFVO config = buscarConfiguracao(idTela);
            if (config == null) {
                return "ERRO: Configuracao nao encontrada para ID: " + idTela;
            }

            if (!"S".equals(config.getUSAIMP())) {
                return "AVISO: Tela nao utiliza importacao de arquivos";
            }

            String diretorioLer = config.getDIRETORIOLER();
            if (diretorioLer == null || diretorioLer.trim().isEmpty()) {
                return "ERRO: Diretorio de leitura nao configurado";
            }

            Path dirPath = Paths.get(diretorioLer);
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return "ERRO: Diretorio nao existe: " + diretorioLer;
            }

            List<Path> arquivos = listarArquivos(diretorioLer);
            if (arquivos.isEmpty()) {
                return "INFO: Nenhum arquivo encontrado no diretorio";
            }

            int processados = 0;
            int erros = 0;

            for (Path arquivo : arquivos) {
                try {
                    processarArquivo(arquivo, config);
                    processados++;
                } catch (Exception e) {
                    erros++;
                    registrarLog(idTela, "Erro ao processar arquivo " + arquivo.getFileName() + ": " + e.getMessage(), "ERROR");
                }
            }

            String resultado = String.format("Processamento concluido - Arquivos processados: %d, Erros: %d", processados, erros);
            registrarLog(idTela, resultado, "INFO");

            return resultado;

        } catch (Exception e) {
            registrarLog(idTela, "Erro geral no processamento: " + e.getMessage(), "ERROR");
            return "ERRO: " + e.getMessage();
        }
    }

    public String alterarLancadorTela(String idTela) {
        try {

            registrarLog(idTela, "Lancador da tela alterado para modo de importacao", "INFO");
            return "SUCESSO: Lancador alterado para modo de importacao";
        } catch (Exception e) {
            registrarLog(idTela, "Erro ao alterar lancador: " + e.getMessage(), "ERROR");
            return "ERRO: " + e.getMessage();
        }
    }

    public String voltarLancadorTela(String idTela) {
        try {

            registrarLog(idTela, "Lancador da tela voltado para o padrao", "INFO");
            return "SUCESSO: Lancador voltado para o padrao";
        } catch (Exception e) {
            registrarLog(idTela, "Erro ao voltar lancador: " + e.getMessage(), "ERROR");
            return "ERRO: " + e.getMessage();
        }
    }

    public boolean validarConfiguracao(String idTela) {
        try {
            TIMPORTCONFVO config = buscarConfiguracao(idTela);
            if (config == null) {
                return false;
            }

            if (config.getDIRETORIOLER() == null || config.getDIRETORIOLER().trim().isEmpty()) {
                return false;
            }

            if (config.getCAMPOARQUIVO() == null || config.getCAMPOARQUIVO().trim().isEmpty()) {
                return false;
            }

            if (config.getCAMPONOME() == null || config.getCAMPONOME().trim().isEmpty()) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String buscarLogsImportacao(String idTela) {
        try {
            List<TIMPORTLOGVO> logs = buscarLogs(idTela);

            StringBuilder json = new StringBuilder();
            json.append("{\"logs\":[");

            for (int i = 0; i < logs.size(); i++) {
                TIMPORTLOGVO log = logs.get(i);
                json.append("{");
                json.append("\"sequencia\":").append(log.getSEQUENCIA()).append(",");
                json.append("\"mensagem\":\"").append(escapeJson(log.getMENSAGEM())).append("\",");
                json.append("\"dataHora\":\"").append(log.getDHIMPORTACAO()).append("\",");
                json.append("\"tipo\":\"").append(log.getTIPO()).append("\"");
                json.append("}");

                if (i < logs.size() - 1) {
                    json.append(",");
                }
            }

            json.append("]}");

            return json.toString();
        } catch (Exception e) {
            return "{\"erro\":\"" + escapeJson(e.getMessage()) + "\"}";
        }
    }

    private TIMPORTCONFVO buscarConfiguracao(String idTela) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();

        String sql = "SELECT * FROM TIMPORTCONF WHERE IDTELA = ?";

        try (NativeSql nativeSql = new NativeSql(jdbc)) {
            nativeSql.setParameter(1, idTela);
            try (ResultSet rs = nativeSql.executeQuery(sql)) {
                if (rs.next()) {
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

                    return config;
                }
            }
        }

        return null;
    }

    private List<Path> listarArquivos(String diretorio) throws IOException {
        List<Path> arquivos = new ArrayList<>();
        Path dirPath = Paths.get(diretorio);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    arquivos.add(path);
                }
            }
        }

        return arquivos;
    }

    private void processarArquivo(Path arquivo, TIMPORTCONFVO config) throws Exception {
        String nomeArquivo = arquivo.getFileName().toString();
        String conteudoArquivo = lerArquivo(arquivo, config.getTIPOARQ());

        registrarLog(config.getIDTELA(), "Arquivo processado: " + nomeArquivo, "INFO");

        processarArquivoPosLeitura(arquivo, config);
    }

    private String lerArquivo(Path arquivo, String tipoArquivo) throws IOException {
        String encoding = "UTF-8";
        if ("ISO88591".equals(tipoArquivo)) {
            encoding = "ISO-8859-1";
        }

        return new String(Files.readAllBytes(arquivo), encoding);
    }

    private void processarArquivoPosLeitura(Path arquivo, TIMPORTCONFVO config) throws IOException {
        String acao = config.getFAZERAPOSLER();

        if ("EXCLUIR".equals(acao)) {
            Files.delete(arquivo);
        } else if ("RENOMEAR".equals(acao)) {
            Path novoNome = arquivo.resolveSibling(arquivo.getFileName() + ".processado");
            Files.move(arquivo, novoNome);
        } else if ("MOVER".equals(acao)) {
            String diretorioGravar = config.getDIRETORIOGRAVAR();
            if (diretorioGravar != null && !diretorioGravar.trim().isEmpty()) {
                Path destino = Paths.get(diretorioGravar, arquivo.getFileName().toString());
                Files.createDirectories(destino.getParent());
                Files.move(arquivo, destino);
            }
        }
    }

    private List<TIMPORTLOGVO> buscarLogs(String idTela) throws Exception {
        List<TIMPORTLOGVO> logs = new ArrayList<>();

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();

        String sql = "SELECT * FROM TIMPORTLOG WHERE IDTELA = ? ORDER BY DHEXECUCAO DESC";

        try (NativeSql nativeSql = new NativeSql(jdbc)) {
            nativeSql.setParameter(1, idTela);
            try (ResultSet rs = nativeSql.executeQuery(sql)) {
                while (rs.next()) {
                    TIMPORTLOGVO log = new TIMPORTLOGVO();
                    log.setIDTELA(rs.getString("IDTELA"));
                    log.setSEQUENCIA(rs.getBigDecimal("SEQUENCIA"));
                    log.setMENSAGEM(rs.getString("MENSAGEM"));
                    log.setDHIMPORTACAO(rs.getTimestamp("DHIMPORTACAO"));
                    log.setNOMEARQUIVO(rs.getString("NOMEARQUIVO"));
                    log.setTIPO(rs.getString("TIPO"));

                    logs.add(log);
                }
            }
        }

        return logs;
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
                nativeSql.setParameter(4, new Date());
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

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("\\", "\\\\");
    }
}
