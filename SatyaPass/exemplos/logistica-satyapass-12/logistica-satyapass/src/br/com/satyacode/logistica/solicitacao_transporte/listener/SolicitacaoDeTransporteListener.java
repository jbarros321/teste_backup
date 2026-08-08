package br.com.satyacode.logistica.solicitacao_transporte.listener;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.UsuarioVO;
import br.com.satyacode.logistica.historico.model.Historico;
import br.com.satyacode.logistica.historico.service.HistoricoService;
import br.com.satyacode.logistica.usuario.service.UsuarioService;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class SolicitacaoDeTransporteListener implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO solicitacaoVO = (DynamicVO) event.getVo();
        solicitacaoVO.setProperty("DTCRIACAO", TimeUtils.getNow());
        if (StringUtils.isEmpty(solicitacaoVO.asString("IDENTIFICADOR")))
            solicitacaoVO.setProperty("IDENTIFICADOR", "SK-"+solicitacaoVO.asBigDecimal("NROUNICO"));
        if (StringUtils.isEmpty(solicitacaoVO.asString("PRIORIDADE")))
            solicitacaoVO.setProperty("PRIORIDADE", "B");
        solicitacaoVO.setProperty("STATUS", "S");
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO solicitacaoVO = (DynamicVO) event.getVo();
        DynamicVO solicitacaoOLDVO = (DynamicVO) event.getOldVO();
        if (StringUtils.isEmpty(solicitacaoVO.asString("IDENTIFICADOR")))
            solicitacaoVO.setProperty("IDENTIFICADOR", "SK-"+solicitacaoVO.asBigDecimal("NROUNICO"));

        if (solicitacaoVO.asString("STATUS").equals("CA") || solicitacaoVO.asString("STATUS").equals("C")){

            if (StringUtils.isNotEmpty(solicitacaoOLDVO.asTimestamp("DTSOLICITACAO"))  ){
                if (!solicitacaoVO.asTimestamp("DTSOLICITACAO").equals(solicitacaoOLDVO.asTimestamp("DTSOLICITACAO"))){
                    lancarErro("Solicitante");
                }
            }

            if (solicitacaoVO.asBigDecimal("CODPARC").compareTo(solicitacaoOLDVO.asBigDecimal("CODPARC")) > 0){
                lancarErro("Solicitante");
            }

            if (solicitacaoVO.asBigDecimal("CODLOCO").compareTo(solicitacaoOLDVO.asBigDecimal("CODLOCO")) > 0){
                lancarErro("Local de Origem");
            }

            if (solicitacaoVO.asBigDecimal("CODLOCD").compareTo(solicitacaoOLDVO.asBigDecimal("CODLOCD")) > 0){
                lancarErro("Local de Destino");
            }

            if(StringUtils.isNotEmpty(solicitacaoOLDVO.asBigDecimal("CODPROJ"))){
                if (solicitacaoVO.asBigDecimal("CODPROJ").compareTo(solicitacaoOLDVO.asBigDecimal("CODPROJ")) > 0){
                    lancarErro("Projeto");
                }
            }

            if(StringUtils.isNotEmpty(solicitacaoOLDVO.asString("SERVICO"))){
                if (!solicitacaoVO.asString("SERVICO").equals(solicitacaoOLDVO.asString("SERVICO"))){
                    lancarErro("Serviço");
                }
            }

        }
    }

    private void lancarErro(String nomeCampo) throws Exception {
        throw  new Exception(String.format("Não é possivel editar o campo %s após a finalização ou cancelamento.", nomeCampo));
    }

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO solicitacaoVO = (DynamicVO) event.getVo();
        String ocorrencia = new UsuarioService().getUsuarioLogado() +  " - Registro Incluido ";
        new HistoricoService().incluirHistorico(new Historico(solicitacaoVO.asBigDecimal("NROUNICO"),ocorrencia));

    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {

    }
}
