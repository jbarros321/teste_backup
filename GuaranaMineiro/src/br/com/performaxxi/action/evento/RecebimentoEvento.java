package br.com.performaxxi.action.evento;

import java.math.BigDecimal;
import java.sql.Timestamp;

import br.com.performaxxi.shared.PerformaxxiAPI;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class RecebimentoEvento implements EventoProgramavelJava {

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        processarEvento(persistenceEvent, "INSERT");
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        ModifingFields modifingFields = persistenceEvent.getModifingFields();

            if (modifingFields.isModifingAny("CODTIPOPERBAIXA,DHTIPOPERBAIXA,DHBAIXA,VLRBAIXA")) {
            processarEvento(persistenceEvent, "UPDATE");
        }
    }

    private void processarEvento(PersistenceEvent persistenceEvent, String tipoEvento) throws Exception {
        DynamicVO finVO = (DynamicVO) persistenceEvent.getVo();
        if (!isReceitaComBaixa(finVO)) return;

        try {
            enviarParaPerformaxxi(criarRecebimentoDTO(finVO));
            inserirLogRecebimentoIndependente(finVO, tipoEvento, "SUCESSO", null);
        } catch (Exception e) {
            inserirLogRecebimentoIndependente(finVO, tipoEvento, "ERRO", e.getMessage());

            if (e.getMessage() != null && e.getMessage().contains("401")) {
                throw new RuntimeException(e.getMessage());
            }
            throw e;
        }
    }

    private boolean isReceitaComBaixa(DynamicVO finVO) {
        Integer recdesp = finVO.asInt("RECDESP");
        Integer codtipoperbaixa = finVO.asInt("CODTIPOPERBAIXA");
        return recdesp != null && recdesp == 1 && codtipoperbaixa != null &&
               finVO.asTimestamp("DHTIPOPERBAIXA") != null && finVO.asTimestamp("DHBAIXA") != null &&
               finVO.asBigDecimal("VLRBAIXA") != null && finVO.asBigDecimal("VLRBAIXA").compareTo(BigDecimal.ZERO) > 0;
    }

    private RecebimentoDTO criarRecebimentoDTO(DynamicVO finVO) throws Exception {
        DadosAdicionais dados = buscarDadosAdicionais(finVO.asInt("NUNOTA"), finVO.asInt("CODPARC"));
        RecebimentoDTO dto = new RecebimentoDTO();
        dto.setNufin(finVO.asInt("NUFIN"));
        dto.setCodParc(finVO.asInt("CODPARC"));
        dto.setValorRecebido(finVO.asBigDecimal("VLRBAIXA"));
        dto.setDataRecebimento(finVO.asTimestamp("DHBAIXA"));
        dto.setRecdesp(finVO.asInt("RECDESP"));
        dto.setOrdemCarga(dados.ordemCarga);
        dto.setNomeCliente(dados.nomeCliente);
        dto.setCodEmp(finVO.asInt("CODEMP"));
        dto.setCodTipoOperBaixa(finVO.asInt("CODTIPOPERBAIXA"));
        dto.setDataTipoOperBaixa(finVO.asTimestamp("DHTIPOPERBAIXA"));
        return dto;
    }

    private DadosAdicionais buscarDadosAdicionais(Integer nunota, Integer codparc) throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        try {
            BigDecimal ordemCargaBD = NativeSql.getBigDecimal("NVL(ORDEMCARGA, 1)", "TGFCAB", "NUNOTA = ?", new Object[]{nunota});
            String nomeCliente = NativeSql.getString("NOMEPARC", "TGFPAR", "CODPARC = ?", new Object[]{codparc});
            return new DadosAdicionais(ordemCargaBD != null ? ordemCargaBD.intValue() : 1,
                                     nomeCliente != null && !nomeCliente.trim().isEmpty() ? nomeCliente : "Cliente " + codparc);
        } catch (Exception e) {
            return new DadosAdicionais(1, "Cliente " + codparc);
        } finally {
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private void enviarParaPerformaxxi(RecebimentoDTO recebimento) throws Exception {
        String jsonRecebimento = PerformaxxiAPI.converterParaRecebimentoPerformaxxi(
            recebimento.getNufin(), recebimento.getCodParc(), recebimento.getValorRecebido().doubleValue(),
            recebimento.getDataRecebimento() != null ? recebimento.getDataRecebimento().toString() : null,
            recebimento.getRecdesp(), recebimento.getOrdemCarga(), recebimento.getNomeCliente(),
            recebimento.getCodEmp(), recebimento.getCodTipoOperBaixa(),
            recebimento.getDataTipoOperBaixa() != null ? recebimento.getDataTipoOperBaixa().toString() : null);

        PerformaxxiAPI.RespostaMensagemRota resposta = PerformaxxiAPI.enviarMensagemRecebimento(jsonRecebimento);

        if (!resposta.isSucesso()) {
            throw new RuntimeException("Erro ao enviar recebimento para Performaxxi: " + resposta.getMensagemErro());
        }

        System.out.println("[PERFORMAXXI] Recebimento enviado com sucesso para Performaxxi");
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {}

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {
        SessionHandle hnd = JapeSession.open();
        try {
            for (EntityPrimaryKey pk : transactionContext.getInserted()) {
                try {
                    DynamicVO vo = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO("FinRecebimento", pk);
                    if (vo != null && isReceitaComBaixa(vo)) inserirLogRecebimento(vo, "INSERT", hnd);
                } catch (Exception e) {}
            }
            for (EntityPrimaryKey pk : transactionContext.getUpdated()) {
                try {
                    DynamicVO vo = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO("FinRecebimento", pk);
                    if (vo != null && isReceitaComBaixa(vo)) inserirLogRecebimento(vo, "UPDATE", hnd);
                } catch (Exception e) {}
            }
        } finally {
            JapeSession.close(hnd);
        }
    }

    private void inserirLogRecebimentoIndependente(DynamicVO recebimentoVO, String tipoEvento, String status, String mensagemErro) {
        Thread logThread = new Thread(() -> {
            SessionHandle hnd = null;
            try {
                System.out.println("[BUSCA] [THREAD_LOG] Iniciando thread de log independente...");
                hnd = JapeSession.open();
                System.out.println("[BUSCA] [THREAD_LOG] JapeSession aberta com sucesso");

                hnd.execWithTX(() -> {
                    System.out.println("[BUSCA] [THREAD_LOG] Dentro de execWithTX - transacao ativa");

                    String correlationId = "EVT_" + System.currentTimeMillis();
                    System.out.println("[BUSCA] [THREAD_LOG] Correlation ID: " + correlationId);
                    System.out.println("[BUSCA] [THREAD_LOG] NUFIN: " + recebimentoVO.asBigDecimal("NUFIN"));
                    System.out.println("[BUSCA] [THREAD_LOG] Status: " + status);

                    JapeFactory.dao("AD_RECEBIMENTOLOG").create()
                        .set("CORRELATION_ID", correlationId)
                        .set("NUFIN", recebimentoVO.asBigDecimal("NUFIN"))
                        .set("CODPARC", recebimentoVO.asBigDecimal("CODPARC"))
                        .set("VALOR_RECEBIDO", recebimentoVO.asBigDecimal("VLRBAIXA"))
                        .set("DATA_RECEBIMENTO", recebimentoVO.asTimestamp("DHBAIXA"))
                        .set("TIPO_EVENTO", tipoEvento)
                        .set("STATUS_PROCESSAMENTO", status)
                        .set("MENSAGEM_ERRO", mensagemErro != null ? mensagemErro.toCharArray() : null)
                        .set("DHEXECUCAO", new Timestamp(System.currentTimeMillis()))
                        .set("USUARIO_EXECUCAO", "SISTEMA")
                        .save();

                    System.out.println("[OK] [THREAD_LOG] INSERT executado com sucesso!");
                });

                System.out.println("[OK] [THREAD_LOG] Transacao commitada com sucesso!");

            } catch (Exception e) {
                System.err.println("[ERRO] [THREAD_LOG] Erro ao inserir log independente: " + e.getMessage());
                e.printStackTrace();
            } finally {
                JapeSession.close(hnd);
                System.out.println("[BUSCA] [THREAD_LOG] JapeSession fechada");
            }
        });
        logThread.start();
        System.out.println("[BUSCA] [THREAD_LOG] Thread de log iniciada");
    }

    private void inserirLogRecebimento(DynamicVO recebimentoVO, String tipoEvento, String status, String mensagemErro, SessionHandle hnd) throws Exception {
        try {
            JapeFactory.dao("AD_RECEBIMENTOLOG").create()
                .set("CORRELATION_ID", "EVT_" + System.currentTimeMillis())
                .set("NUFIN", recebimentoVO.asBigDecimal("NUFIN"))
                .set("CODPARC", recebimentoVO.asBigDecimal("CODPARC"))
                .set("VALOR_RECEBIDO", recebimentoVO.asBigDecimal("VLRBAIXA"))
                .set("DATA_RECEBIMENTO", recebimentoVO.asTimestamp("DHBAIXA"))
                .set("TIPO_EVENTO", tipoEvento)
                .set("STATUS_PROCESSAMENTO", status)
                .set("MENSAGEM_ERRO", mensagemErro != null ? mensagemErro.toCharArray() : null)
                .set("DHEXECUCAO", new Timestamp(System.currentTimeMillis()))
                .set("USUARIO_EXECUCAO", "SISTEMA")
                .save();
        } catch (Exception e) {}
    }

    private void inserirLogRecebimento(DynamicVO recebimentoVO, String tipoEvento, SessionHandle hnd) throws Exception {
        inserirLogRecebimento(recebimentoVO, tipoEvento, "PROCESSADO", null, hnd);
    }

    private static class DadosAdicionais {
        final int ordemCarga;
        final String nomeCliente;

        DadosAdicionais(int ordemCarga, String nomeCliente) {
            this.ordemCarga = ordemCarga;
            this.nomeCliente = nomeCliente;
        }
    }

    private static class RecebimentoDTO {
        private int nufin;
        private int codParc;
        private BigDecimal valorRecebido;
        private Timestamp dataRecebimento;
        private int recdesp;
        private int ordemCarga;
        private String nomeCliente;
        private int codEmp;
        private int codTipoOperBaixa;
        private Timestamp dataTipoOperBaixa;

        public int getNufin() { return nufin; }
        public void setNufin(int nufin) { this.nufin = nufin; }
        public int getCodParc() { return codParc; }
        public void setCodParc(int codParc) { this.codParc = codParc; }
        public BigDecimal getValorRecebido() { return valorRecebido; }
        public void setValorRecebido(BigDecimal valorRecebido) { this.valorRecebido = valorRecebido; }
        public Timestamp getDataRecebimento() { return dataRecebimento; }
        public void setDataRecebimento(Timestamp dataRecebimento) { this.dataRecebimento = dataRecebimento; }
        public int getRecdesp() { return recdesp; }
        public void setRecdesp(int recdesp) { this.recdesp = recdesp; }
        public int getOrdemCarga() { return ordemCarga; }
        public void setOrdemCarga(int ordemCarga) { this.ordemCarga = ordemCarga; }
        public String getNomeCliente() { return nomeCliente; }
        public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
        public int getCodEmp() { return codEmp; }
        public void setCodEmp(int codEmp) { this.codEmp = codEmp; }
        public int getCodTipoOperBaixa() { return codTipoOperBaixa; }
        public void setCodTipoOperBaixa(int codTipoOperBaixa) { this.codTipoOperBaixa = codTipoOperBaixa; }
        public Timestamp getDataTipoOperBaixa() { return dataTipoOperBaixa; }
        public void setDataTipoOperBaixa(Timestamp dataTipoOperBaixa) { this.dataTipoOperBaixa = dataTipoOperBaixa; }
    }
}
