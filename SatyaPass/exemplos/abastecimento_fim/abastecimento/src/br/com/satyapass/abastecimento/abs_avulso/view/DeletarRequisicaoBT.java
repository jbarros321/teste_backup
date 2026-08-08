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

public class DeletarRequisicaoBT implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        try{
            AbastecimentoLogFactory.incluirLogPai( ModalidadeEnum.BOTAO_ACAO,  contexto.getUsuarioLogado(), true);
            AbastecimentoLogFactory.incluirItem("Inicio Deleção da Nota", "DeletarRequisicaoBT", StatusItemEnum.OK, true);
            for (Registro registro : contexto.getLinhas()){
                BigDecimal codAbastecimento = (BigDecimal) registro.getCampo("CODABAST");
                AbastecimentoService abastecimentoService = new AbastecimentoService();
                abastecimentoService.gerenciarExclusaoDaNota(codAbastecimento);
                contexto.setMensagemRetorno("Rotina executada com sucesso!");
            }
        }catch (Exception e){
            AbastecimentoLogFactory.incluirItem("Erro ao deletar Nota:  ", ExceptionUtils.getStackTrace(e), "GerarRequisicaoBT", StatusItemEnum.ERRO, true);
            throw new Exception(e);
        }
        finally {
            AbastecimentoLogFactory.finalizarLog();
        }
    }
}
