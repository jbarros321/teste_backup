package br.com.satyapass.abastecimento.abs_avulso.view;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.satyacode.satyapass.log.model.ModalidadeEnum;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import br.com.satyapass.abastecimento.abs_avulso.service.AbastecimentoService;
import br.com.satyapass.abastecimento.log.AbastecimentoLogFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.math.BigDecimal;

public class GerarRequisicaoBT implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        try{
            AbastecimentoLogFactory.incluirLogPai( ModalidadeEnum.BOTAO_ACAO,  contexto.getUsuarioLogado(), true);
            AbastecimentoLogFactory.incluirItem("INICIO Geração da Requisição", "GerarRequisicaoBT", StatusItemEnum.OK, true);
            for (Registro registro : contexto.getLinhas()){
                BigDecimal codAbastecimento = (BigDecimal) registro.getCampo("CODABAST");
                AbastecimentoLogFactory.incluirItem("Inicio - Cód. Abastecimento: " + codAbastecimento, "", StatusItemEnum.OK, true);
                AbastecimentoService abastecimentoService = new AbastecimentoService();
                abastecimentoService.gerenciarAbastecimento(codAbastecimento);
                AbastecimentoLogFactory.incluirItem("Fim - Cód. Abastecimento: " + codAbastecimento, "", StatusItemEnum.OK, true);
                contexto.setMensagemRetorno("Rotina executada com sucesso!");
            }
        }catch (Exception e){
            AbastecimentoLogFactory.incluirItem("Erro ao gerar requisição:  ", ExceptionUtils.getStackTrace(e), "GerarRequisicaoBT", StatusItemEnum.INFO, true);
            throw new Exception(e);
        }
        finally {
            AbastecimentoLogFactory.finalizarLog();
        }

    }
}
