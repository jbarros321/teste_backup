import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class EventoAuditoriaParamArz implements EventoProgramavelJava {

    @Override
    public void beforeInsert(PersistenceEvent e) throws Exception {
        DynamicVO vo = (DynamicVO) e.getVo();

        BigDecimal usuLogado = getUsuarioLogado();
        vo.setProperty("DTINS", new Timestamp(System.currentTimeMillis()));
        vo.setProperty("USUINC", usuLogado);

        BigDecimal idRegistro = (BigDecimal) vo.getProperty("ID");

        inserirHistorico(
            idRegistro,
            "Registro Criado",
            "Inserção do registro ID: " + idRegistro,
            usuLogado
        );
    }

    @Override
    public void beforeUpdate(PersistenceEvent e) throws Exception {
        DynamicVO voNovo  = (DynamicVO) e.getVo();
        DynamicVO voAntigo = (DynamicVO) e.getOldVO();

        StringBuilder resumo   = new StringBuilder();
        StringBuilder detalhe  = new StringBuilder();

        // Campos texto
        compararTexto(voAntigo, voNovo, "DESCR", "Descrição Armazenagem", resumo, detalhe);

        // Campos numéricos
        compararNumero(voAntigo, voNovo, "CODPARC", "Parceiro", resumo, detalhe);
        compararNumero(voAntigo, voNovo, "ARMAZENAGEM", "Armazenagem", resumo, detalhe);
        compararNumero(voAntigo, voNovo, "DESCPALETIZADA", "Descarga Paletizada", resumo, detalhe);
        compararNumero(voAntigo, voNovo, "CARGPALETIZADA", "Carga Paletizada", resumo, detalhe);
        compararNumero(voAntigo, voNovo, "PICKING", "Picking", resumo, detalhe);
        compararNumero(voAntigo, voNovo, "RECFRIO", "Recuperação de Frio", resumo, detalhe);
        compararNumero(voAntigo, voNovo, "HREXTRA", "Hora Extra/Hora Espera", resumo, detalhe);
        compararNumero(voAntigo, voNovo, "ISSQN", "ISSQN", resumo, detalhe);
        compararNumero(voAntigo, voNovo, "SEGURO", "Seguro", resumo, detalhe);
        compararNumero(voAntigo, voNovo, "OUTROS", "Outros Serviços", resumo, detalhe);

        if (resumo.length() > 0) {
            // Remove última vírgula
            String textoResumo = "Campos alterados: " + resumo.substring(0, resumo.length() - 2);
            BigDecimal idRegistro = (BigDecimal) voNovo.getProperty("ID");
            BigDecimal usuLogado  = getUsuarioLogado();

            inserirHistorico(idRegistro, textoResumo, detalhe.toString(), usuLogado);
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent e) throws Exception {
        // Não utilizado
    }

    @Override
    public void afterInsert(PersistenceEvent e) throws Exception {
        // Não utilizado
    }

    @Override
    public void afterUpdate(PersistenceEvent e) throws Exception {
        // Não utilizado
    }

    @Override
    public void afterDelete(PersistenceEvent e) throws Exception {
        // Não utilizado
    }

    @Override
    public void beforeCommit(TransactionContext ctx) throws Exception {
        // Não utilizado
    }

    // ======================== MÉTODOS AUXILIARES ========================

    private void compararTexto(DynamicVO antigo, DynamicVO novo, String campo, String label,
                               StringBuilder resumo, StringBuilder detalhe) throws Exception {
        String valAntigo = antigo.getAsString(campo);
        String valNovo   = novo.getAsString(campo);

        String oldVal = (valAntigo == null) ? "" : valAntigo;
        String newVal = (valNovo == null) ? "" : valNovo;

        if (!oldVal.equals(newVal)) {
            resumo.append(label).append(", ");
            detalhe.append(label).append(": [").append(oldVal).append("] -> [").append(newVal).append("]\n");
        }
    }

    private void compararNumero(DynamicVO antigo, DynamicVO novo, String campo, String label,
                                StringBuilder resumo, StringBuilder detalhe) throws Exception {
        BigDecimal valAntigo = (BigDecimal) antigo.getProperty(campo);
        BigDecimal valNovo   = (BigDecimal) novo.getProperty(campo);

        BigDecimal oldVal = (valAntigo == null) ? BigDecimal.ZERO : valAntigo;
        BigDecimal newVal = (valNovo == null) ? BigDecimal.ZERO : valNovo;

        if (oldVal.compareTo(newVal) != 0) {
            resumo.append(label).append(", ");
            detalhe.append(label).append(": [").append(oldVal).append("] -> [").append(newVal).append("]\n");
        }
    }

    private BigDecimal getUsuarioLogado() {
        try {
            return AuthenticationInfo.getCurrent().getUserID();
        } catch (Exception ex) {
            return BigDecimal.ZERO;
        }
    }

    private void inserirHistorico(BigDecimal id, String descricao, String auditoria, BigDecimal usuario)
            throws Exception {
        JapeWrapper histDAO = JapeFactory.dao("AD_HISTALTERARMAZ");
        DynamicVO histVO = histDAO.create();

        histVO.setProperty("ID", id);
        histVO.setProperty("DHALTER", new Timestamp(System.currentTimeMillis()));
        histVO.setProperty("DESCRALT", descricao);
        histVO.setProperty("USUALTER", usuario);
        histVO.setProperty("AUDITALTER", auditoria);

        histDAO.save(histVO);
    }
}
