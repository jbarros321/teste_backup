package br.com.performaxxi.action.acaoAgendada;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import br.com.performaxxi.shared.PerformaxxiAPI;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ComprovantesEntrega implements ScheduledAction {

    @Override
    public void onTime(ScheduledActionContext ctx) {
        try {
            executarConsultaComprovantes();
            ctx.info("Consulta de comprovantes executada com sucesso");
        } catch (Exception e) {
            String errorMsg = "Erro na execucao da consulta de comprovantes: " + e.getMessage();
            PerformaxxiAPI.logError(errorMsg, e);
            ctx.info(errorMsg);
        }
    }

    private void executarConsultaComprovantes() throws Exception {
        long inicio = System.currentTimeMillis();
        String correlationId = PerformaxxiAPI.generateEventCorrelationId();

        try {
            List<VeiculoComprovante> veiculos = buscarVeiculosComOrdemCarga();
            int totalComprovantes = 0;

            for (VeiculoComprovante veiculo : veiculos) {
                try {
                    List<Object> comprovantes = consultarComprovantesVeiculo(veiculo);
                    totalComprovantes += comprovantes.size();

                    if (!comprovantes.isEmpty()) {
                        salvarComprovantes(comprovantes, veiculo);
                    }
                } catch (Exception e) {
                    PerformaxxiAPI.logError("Erro ao processar veiculo " + veiculo.idRastreador + ": " + e.getMessage(), e);
                }
            }

            long duracao = System.currentTimeMillis() - inicio;
            String mensagem = String.format("Processados %d comprovantes de %d veiculos em %s",
                totalComprovantes, veiculos.size(), PerformaxxiAPI.formatDuration(duracao));

            PerformaxxiAPI.logInfo("Consulta finalizada - " + mensagem);

        } catch (Exception e) {
            long duracao = System.currentTimeMillis() - inicio;
            PerformaxxiAPI.logError("Erro na consulta de comprovantes: " + e.getMessage(), e);
            enviarEmailErro(correlationId, e, duracao);
            throw e;
        }
    }

    private static class VeiculoComprovante {
        public String data, idRastreador, classe, conjunto;

        public VeiculoComprovante(String data, String idRastreador, String classe, String conjunto) {
            this.data = data;
            this.idRastreador = idRastreador;
            this.classe = classe;
            this.conjunto = conjunto;
        }
    }

    private List<VeiculoComprovante> buscarVeiculosComOrdemCarga() throws Exception {
        List<VeiculoComprovante> veiculos = new java.util.ArrayList<>();
        EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
        JdbcWrapper jdbc = dwfEntityFacade.getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);

        String query = "SELECT CAB.DTNEG AS \"data\", VEI.PLACA AS \"idRastreador\" " +
                       "  FROM TGFCAB CAB " +
                       " INNER JOIN TGFORD ORD ON CAB.ORDEMCARGA = ORD.ORDEMCARGA " +
                       " INNER JOIN TGFVEI VEI ON VEI.CODVEICULO = ORD.CODVEICULO " +
                       " WHERE CAB.ORDEMCARGA IS NOT NULL " +
                       "   AND CAB.ORDEMCARGA != 0 " +
                       "   AND CAB.DTNEG = TRUNC(SYSDATE)";

        ResultSet rs = sql.executeQuery(query);
        while (rs.next()) {
            String dataAPI = new SimpleDateFormat("yyyy-MM-dd").format(rs.getDate("data"));
            String idRastreador = rs.getString("idRastreador");
            veiculos.add(new VeiculoComprovante(dataAPI, idRastreador, "ENTREGA", "COMPROVANTES"));
        }

        PerformaxxiAPI.logInfo("Encontrados " + veiculos.size() + " veiculos com ordem de carga");
        return veiculos;
    }

    private List<Object> consultarComprovantesVeiculo(VeiculoComprovante veiculo) throws Exception {
        return PerformaxxiAPI.consultarComprovantesEntrega(
            veiculo.data, veiculo.idRastreador, veiculo.classe, veiculo.conjunto);
    }

    private void salvarComprovantes(List<Object> comprovantes, VeiculoComprovante veiculo) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.execWithTX(() -> {
                JapeWrapper comprovanteDAO = JapeFactory.dao("AD_COMPROVANTES");
                for (Object comprovante : comprovantes) {
                    PerformaxxiAPI.ComprovanteEntrega comp = (PerformaxxiAPI.ComprovanteEntrega) comprovante;
                    FluidCreateVO comprovanteVO = comprovanteDAO.create()
                        .set("CODIGO_ENTREGA", comp.codigoEntrega)
                        .set("IDENTIFICADOR_CLIENTE", comp.identificadorCliente)
                        .set("URL_ACESSO", comp.urlAcesso)
                        .set("ID_RASTREADOR", veiculo.idRastreador)
                        .set("DATA_ENTREGA", veiculo.data)
                        .set("CLASSE", veiculo.classe)
                        .set("CONJUNTO", veiculo.conjunto)
                        .set("DHEXECUCAO", new Timestamp(System.currentTimeMillis()))
                        .set("STATUS", "PROCESSADO");
                    comprovanteVO.save();
                }
            });
        } finally {
            JapeSession.close(hnd);
        }
    }

    private void enviarEmailErro(String correlationId, Exception erro, long duracao) {
        try {
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            String mensagem = String.format(
                "<h2>[ERRO] Comprovantes de Entrega - %s</h2>" +
                "<p><strong>Erro:</strong> %s</p>" +
                "<p><strong>Tempo:</strong> %s</p>",
                correlationId, erro.getMessage(), PerformaxxiAPI.formatDuration(duracao));

            DynamicVO emailFilaVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.FILA_MSG);
            emailFilaVO.setProperty("ASSUNTO", "[ERRO] Comprovantes de Entrega - " + correlationId);
            emailFilaVO.setProperty("MENSAGEM", mensagem.toCharArray());
            emailFilaVO.setProperty("EMAIL", "suporte@guaranamineiro.com.br");
            emailFilaVO.setProperty("CODUSUREMET", BigDecimal.ZERO);
            emailFilaVO.setProperty("TIPOENVIO", "E");
            emailFilaVO.setProperty("CODCON", BigDecimal.ZERO);
            emailFilaVO.setProperty("MAXTENTENVIO", BigDecimal.valueOf(3));
            emailFilaVO.setProperty("STATUS", "Pendente");
            emailFilaVO.setProperty("MIMETYPE", "text/html");

            dwfEntityFacade.createEntity(DynamicEntityNames.FILA_MSG, (EntityVO) emailFilaVO);
        } catch (Exception emailError) {
            PerformaxxiAPI.logError("Erro ao enviar email: " + emailError.getMessage(), emailError);
        }
    }
}
