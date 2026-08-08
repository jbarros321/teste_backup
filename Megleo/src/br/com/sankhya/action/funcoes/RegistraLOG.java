package br.com.sankhya.action.funcoes;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class RegistraLOG {
    public void insereRegistro(String titulo, String conteudo) throws Exception {
        this.insereRegistro(titulo, conteudo, null);
    }

    public void insereRegistro(String titulo, String conteudo, BigDecimal nunota) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            hnd.execWithTX(() -> {
                JapeWrapper logDAO = JapeFactory.dao((String)"AD_LOGMEG");
                ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)logDAO.create().set("TITULO", (Object)titulo)).set("CONTEUDO", (Object)conteudo)).set("DHEXECUCAO", (Object)new Timestamp(System.currentTimeMillis()))).set("NUNOTA", (Object)nunota)).save();
            });
        }
        finally {
            JapeSession.close((JapeSession.SessionHandle)hnd);
        }
    }

    public void insereRegistroTransacaoAutomatica(String titulo, String conteudo, BigDecimal nunota) throws Exception {
        Object hnd = null;
        JapeWrapper logDAO = JapeFactory.dao((String)"AD_LOGMEG");
        ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)logDAO.create().set("TITULO", (Object)titulo)).set("CONTEUDO", (Object)conteudo)).set("DHEXECUCAO", (Object)new Timestamp(System.currentTimeMillis()))).set("NUNOTA", (Object)nunota)).save();
    }
}
