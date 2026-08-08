package br.com.ermaq.EventoProgramavelJava;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;

// TGFCAB
public class ExcluirRequisicoesControleEstoque implements EventoProgramavelJava {

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
        
    }

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception { }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception { }

    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception { }

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception { 
    	excluirRequisicoesControleEstoque(event);
    }

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {
        limparControleDuplicacao(event);
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception { }
    

    private void limparControleDuplicacao(PersistenceEvent event) {
        DynamicVO vo = (DynamicVO) event.getVo();
        vo.setProperty("AD_CONTROLAESTOQUE", null);
        vo.setProperty("AD_NUREQBAIXA", null);
        vo.setProperty("AD_NUREQENTRADA", null);
    }
    
    /**
     * Exclui as requisições vinculadas à nota de compra (primeiro baixa, depois entrada)
     */
    private void excluirRequisicoesControleEstoque(PersistenceEvent event) throws Exception {
        System.out.println(">> Excluindo requisições de controle de estoque...");
        
        DynamicVO vo = (DynamicVO) event.getVo();
        BigDecimal nuReqBaixa   = vo.asBigDecimalOrZero("AD_NUREQBAIXA");
        BigDecimal nuReqEntrada = vo.asBigDecimalOrZero("AD_NUREQENTRADA");
        
        CACHelper cacHelper = new CACHelper();
        
        // Exclusão em ordem: primeiro baixa, depois entrada
        if (nuReqBaixa != null && nuReqBaixa.intValue() > 0) {
            System.out.println("Excluindo requisição de BAIXA: " + nuReqBaixa);
            cacHelper.excluirNota(nuReqBaixa);
        }
        
        if (nuReqEntrada != null && nuReqEntrada.intValue() > 0) {
            System.out.println("Excluindo requisição de ENTRADA: " + nuReqEntrada);
            cacHelper.excluirNota(nuReqEntrada);
        }
    }
}
