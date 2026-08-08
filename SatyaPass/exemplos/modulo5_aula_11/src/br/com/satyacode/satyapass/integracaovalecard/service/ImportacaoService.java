package br.com.satyacode.satyapass.integracaovalecard.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.satyapass.integracaovalecard.helper.ExcelHelper;
import br.com.satyacode.satyapass.integracaovalecard.model.ItemSankhya;
import br.com.satyacode.satyapass.integracaovalecard.repository.CabecalhoRepository;
import br.com.satyacode.satyapass.integracaovalecard.repository.ItemRespository;
import br.com.satyacode.satyapass.integracaovalecard.utils.ArquivoUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;

public class ImportacaoService {

    private CabecalhoRepository cabecalhoRepository;
    private ItemRespository itemRespository;

    public ImportacaoService(){
        this.itemRespository = new ItemRespository();
        this.cabecalhoRepository = new CabecalhoRepository();
    }
    public void gerenciarImportacao(BigDecimal nroUnico) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO cabecalhoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_IMPCAB", new Object[]{nroUnico});
        if (!(cabecalhoVO.asString("STATUS").equals("P") || cabecalhoVO.asString("STATUS").equals("I"))){
            throw new Exception("Atenção, não é possivel importar o arquivo, pois o status está diferente de Pendente");
        }
        BigDecimal quantidadeDeLinhasFilhas  = NativeSql.getBigDecimal("COUNT(*)", "AD_IMPITE", "NROUNICO = ?", nroUnico);
        if (quantidadeDeLinhasFilhas.compareTo(BigDecimal.ZERO) != 0){
            itemRespository.deletarItens(nroUnico);
        }
        if (cabecalhoVO.asBlob("ARQUIVO") == null){
            throw new Exception("Para importar a rotina, é necessário que o arquivo anexado no campo 'Arquivo'.");
        }
        InputStream fileInputStream = ArquivoUtils.getLerArquivo(new ByteArrayInputStream(cabecalhoVO.asBlob("ARQUIVO")));
        ArrayList<ItemSankhya> itensSankhya =  ExcelHelper.processarArquivo(fileInputStream);
        if (!itensSankhya.isEmpty()){
            for (ItemSankhya item: itensSankhya){
                item.setNroUnico(nroUnico);
                itemRespository.incluirItens(item);
            }
            cabecalhoRepository.atualizarStatusDoCabecalho(nroUnico, "I");
        }
    }
}
