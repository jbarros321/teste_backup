package br.com.satyacode.logistica.historico.repository;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.satyacode.logistica.historico.model.Historico;

public class HistoricoRepository {

    public void incluirHistorico(Historico historico) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper empresaDAO = JapeFactory.dao("AD_STLHIS");
            DynamicVO save = empresaDAO.create()
                    .set("NROUNICO", historico.getNroUnico())
                    .set("DATA", historico.getData())
                    .set("OCORRENCIA", historico.getOcorrencia())
                    .save();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }
}
