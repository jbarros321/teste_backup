package br.com.satyacode.satyapass.integracaovalecard.rotinas.cabecalho.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.satyacode.satyapass.integracaovalecard.rotinas.depara.service.DeParaService;
import br.com.satyacode.satyapass.integracaovalecard.shared.helper.ExcelHelper;
import br.com.satyacode.satyapass.integracaovalecard.shared.model.ItemSankhya;
import br.com.satyacode.satyapass.integracaovalecard.rotinas.cabecalho.repository.CabecalhoRepository;
import br.com.satyacode.satyapass.integracaovalecard.rotinas.item.repository.ItemRespository;
import br.com.satyacode.satyapass.integracaovalecard.shared.utils.ArquivoUtils;
import com.sankhya.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Native;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

public class CabecalhoService {

    private CabecalhoRepository cabecalhoRepository;
    private ItemRespository itemRespository;

    private EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

    public CabecalhoService(){
        this.itemRespository = new ItemRespository();
        this.cabecalhoRepository = new CabecalhoRepository();
    }
    public void gerenciarImportacao(BigDecimal nroUnico) throws Exception {

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

    public void gerenciarDePara(BigDecimal nroUnico) throws Exception {

        DynamicVO cabecalhoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_IMPCAB", new Object[]{nroUnico});
        if (!(cabecalhoVO.asString("STATUS").equals("I"))){
            throw new Exception("Atenção, só é possivel realizar o de/para quando a etapa anterior foi Importado.");
        }

        DeParaService deParaService = new DeParaService();
        deParaService.gerenciarDePara(nroUnico);

        BigDecimal qtdDeLinhasDeProdutoASerVinculado = NativeSql.getBigDecimal("COUNT(*)", "AD_IMPDP", "CODPROD IS NULL");
        if (qtdDeLinhasDeProdutoASerVinculado.compareTo(BigDecimal.ZERO) != 0){
            throw new Exception("Atenção,antes de seguir na vinculação do de/para, abra a tela " +
                    "'De/Para Integração ValeCard' e realize a vinculação");

        }

        FinderWrapper finder = new FinderWrapper("AD_IMPITE", "this.NROUNICO = ? AND (CODPROD IS NULL OR CODVEICULO IS NULL)", new Object[]{nroUnico});
        Collection<DynamicVO> itensVO = dwfFacade.findByDynamicFinderAsVO(finder);
        if (!itensVO.isEmpty()){
            for (DynamicVO itemVO: itensVO){
                if (StringUtils.isEmpty(itemVO.asString("PRODUTO"))){
                    throw new Exception("O produto está vazio! Sequencia: " +itemVO.asBigDecimal("SEQUENCIA") );
                }
                String nomeProduto = itemVO.asString("PRODUTO").trim();
                DynamicVO deparaVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_IMPDP", new Object[]{nomeProduto});
                BigDecimal codProduto = deparaVO.asBigDecimal("CODPROD");

                String placa =  itemVO.asString("PLACA").trim();
                BigDecimal qtdVeiculosComAPlaca = NativeSql.getBigDecimal("COUNT(*)", "TGFVEI", "PLACA LIKE ?", placa);
                if (qtdVeiculosComAPlaca.compareTo(BigDecimal.ZERO) == 0){
                    throw new Exception("Não foi possivel identificar o veiculo com a placa: "+ placa + " - Sequencia: " + itemVO.asBigDecimal("SEQUENCIA") );
                }

                BigDecimal codVeiculo = BigDecimal.ZERO;
                finder = new FinderWrapper("Veiculo", "this.PLACA like ? AND ATIVO = 'S'", new Object[]{placa});
                Collection<DynamicVO> veiculosVO = dwfFacade.findByDynamicFinderAsVO(finder);
                if (!veiculosVO.isEmpty()){
                    for (DynamicVO veiculoVO: veiculosVO){
                        codVeiculo = veiculoVO.asBigDecimal("CODVEICULO");
                    }
                }

                itemRespository.atualizarCamposDeParaNosItens(nroUnico,itemVO.asBigDecimal("SEQUENCIA"), "CODPROD", codProduto );
                itemRespository.atualizarCamposDeParaNosItens(nroUnico,itemVO.asBigDecimal("SEQUENCIA"), "CODVEICULO", codVeiculo );
            }
        }
        cabecalhoRepository.atualizarStatusDoCabecalho(nroUnico, "DP");

    }
}
