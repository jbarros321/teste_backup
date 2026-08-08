package br.com.universaleletronic.action.botaoAcao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.financeiro.helper.GeracaoRemessaHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.util.troubleshooting.SKError;
import br.com.sankhya.util.troubleshooting.TSLevel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class GerarArquivoRemessa implements AcaoRotinaJava {
    private StringBuilder msgErro = new StringBuilder();
    private StringBuilder arqGerado = new StringBuilder();

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        if (contexto.getLinhas() == null || contexto.getLinhas().length == 0) {
            contexto.setMensagemRetorno("Nenhum registro selecionado.");
            return;
        }

        IntStream.range(0, contexto.getLinhas().length).forEach(i -> {
            Registro registro = contexto.getLinhas()[i];
            try {
                gerarArquivo(registro);
            } catch (Exception e) {
                msgErro.append(e.getMessage()).append(" <br>");
                e.printStackTrace();
            }
        });

        StringBuilder mensagem = new StringBuilder();
        if (arqGerado.length() > 0) {
            mensagem.append("Arquivo gerado com sucesso");
        }
        if (msgErro.length() > 0) {
            mensagem.append(msgErro);
        }
        contexto.setMensagemRetorno(mensagem.toString());
    }

    private void gerarArquivo(Registro registro) throws Exception {
        EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbcWrapper = entityFacade.getJdbcWrapper();
        JapeSession.SessionHandle sessionHandle = null;
        try {
            sessionHandle = JapeSession.open();
            BigDecimal codCtabCoInt = (BigDecimal) registro.getCampo("CODCTABCOINT");
            BigDecimal codigo = (BigDecimal) registro.getCampo("CODIGO");
            BigDecimal nroUnico = (BigDecimal) registro.getCampo("NROUNICO");
            String tipo = (String) registro.getCampo("TIPO");
            String agrupaPagto = (String) registro.getCampo("AGRUPAPAGTO");
            String geraNossoNum = (String) registro.getCampo("GERANOSSONRO");
            String geraLinhaDig = (String) registro.getCampo("GERALINHADIG");
            String registraBcoCta = (String) registro.getCampo("REGISTRABCOCTA");
            String utilizaCtaTitulo = (String) registro.getCampo("UTILIZACTATITULO");

            GeracaoRemessaHelper.RemessaParam remessaParam = new GeracaoRemessaHelper.RemessaParam();
            remessaParam.setStrRecDesp(tipo);
            remessaParam.setContaBancaria(codCtabCoInt);
            remessaParam.setCodLayout(codigo);
            remessaParam.setAgruparPagamentos("S".equals(agrupaPagto));
            remessaParam.setGerarNossoNum("S".equals(geraNossoNum));
            remessaParam.setGerarLinhaDigitavel("S".equals(geraLinhaDig));
            remessaParam.setRegistrarBancoConta("S".equals(registraBcoCta));
            remessaParam.setFiltro(null);
            remessaParam.setCriteria(null);

            Map<Long, Long> financeirosRemessa = getFinanceirosRemessa(nroUnico);
            if (!financeirosRemessa.isEmpty()) {
                remessaParam.addAllTitulo(financeirosRemessa.keySet());
            }

            GeracaoRemessaHelper.RemessaResult remessaResult = GeracaoRemessaHelper.gerarArquivoRemessa(jdbcWrapper, remessaParam);
            BigDecimal numeroRemessa = remessaResult.getNumeroRemessa();
            gravarHistoricoRemessa(entityFacade, remessaResult, numeroRemessa, remessaResult.getNomeArquivo());

            String diretorio = buscarDiretorio(codigo);
            criarArquivoRemessa(diretorio, "Arquivo", remessaResult.getNomeArquivo(), remessaResult.getArquivoGerado());
        } finally {
            JapeSession.close(sessionHandle);
            JdbcWrapper.closeSession(jdbcWrapper);
        }
    }

    private Map<Long, Long> getFinanceirosRemessa(BigDecimal nroUnico) throws Exception {
        Map<Long, Long> financeirosMap = new HashMap<>();
        JdbcWrapper jdbcWrapper = null;
        NativeSql nativeSql = null;
        ResultSet resultSet = null;
        try {
            EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
            jdbcWrapper = entityFacade.getJdbcWrapper();
            nativeSql = new NativeSql(jdbcWrapper);
            nativeSql.appendSql("SELECT NUFIN FROM AD_TSIREMITE WHERE NROUNICO = :NROUNICO");
            nativeSql.setNamedParameter("NROUNICO", nroUnico);
            resultSet = nativeSql.executeQuery();

            while (resultSet.next()) {
                String nufin = resultSet.getString("NUFIN");
                if (nufin != null && !nufin.isEmpty()) {
                    financeirosMap.put(Long.valueOf(nufin), 0L);
                }
            }
            resultSet.close();
            NativeSql.releaseResources(nativeSql);

            nativeSql = new NativeSql(jdbcWrapper);
            nativeSql.appendSql("UPDATE AD_TSIREMCAB SET ARQGERADO = 'S' WHERE NROUNICO = :NROUNICO");
            nativeSql.setNamedParameter("NROUNICO", nroUnico);
            nativeSql.executeUpdate();
        } finally {
            if (resultSet != null) {
                try { resultSet.close(); } catch (Exception e) { }
            }
            NativeSql.releaseResources(nativeSql);
            JdbcWrapper.closeSession(jdbcWrapper);
        }
        return financeirosMap;
    }

    private String buscarDiretorio(BigDecimal codigo) throws Exception {
        JdbcWrapper jdbcWrapper = null;
        NativeSql nativeSql = null;
        ResultSet resultSet = null;
        try {
            EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
            jdbcWrapper = entityFacade.getJdbcWrapper();
            nativeSql = new NativeSql(jdbcWrapper);
            nativeSql.appendSql("SELECT DIRETORIO FROM AD_LAYOUTDIR WHERE CODIGO = :CODIGO");
            nativeSql.setNamedParameter("CODIGO", codigo);
            resultSet = nativeSql.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("DIRETORIO");
            }
            throw new Exception("Diretório não encontrado para o código de layout: " + codigo);
        } finally {
            if (resultSet != null) {
                try { resultSet.close(); } catch (Exception e) { }
            }
            NativeSql.releaseResources(nativeSql);
            JdbcWrapper.closeSession(jdbcWrapper);
        }
    }

    private void gravarHistoricoRemessa(EntityFacade entityFacade, GeracaoRemessaHelper.RemessaResult remessaResult, BigDecimal numeroRemessa, String nomeArquivo) throws Exception {
        DynamicVO dynamicVO = (DynamicVO) entityFacade.getDefaultValueObjectInstance("HistoricoRemessaBancaria");
        dynamicVO.setProperty("NUMREMESSA", numeroRemessa);
        dynamicVO.setProperty("NOMEARQUIVO", nomeArquivo);
        dynamicVO.setProperty("DTGERACAO", remessaResult.getDataGeracao());
        entityFacade.createEntity("HistoricoRemessaBancaria", (EntityVO) dynamicVO);
    }

    private void criarArquivoRemessa(String diretorioDestino, String destino, String nomeArquivo, File arquivoGerado) throws Exception {
        File fileDestino = new File(diretorioDestino, destino);
        if (!fileDestino.exists() && !fileDestino.mkdirs()) {
            throw (Exception) SKError.registry(TSLevel.ERROR, "CORE_E03389", 
                new Exception("Não foi possível criar o repositório de destino no local especificado: " + fileDestino.getAbsolutePath()));
        }

        byte[] buffer = new byte[(int) arquivoGerado.length()];
        try (FileInputStream fileInputStream = new FileInputStream(arquivoGerado);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                 new FileOutputStream(new File(fileDestino, nomeArquivo)), 8192)) {
            int bytesRead = fileInputStream.read(buffer);
            if (bytesRead > 0) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw (Exception) SKError.registry(TSLevel.ERROR, "CORE_E03388", 
                new Exception("Erro ao criar arquivo de remessa.")).initCause(e);
        }
    }
}
