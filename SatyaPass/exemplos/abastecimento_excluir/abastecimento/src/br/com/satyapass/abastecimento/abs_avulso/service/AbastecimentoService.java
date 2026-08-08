package br.com.satyapass.abastecimento.abs_avulso.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.PrecoCustoHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.util.TipoCusto;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.satyacode.framework.core.commons.dd.DataDictionaryExceptions;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import br.com.satyapass.abastecimento.abs_avulso.repository.AbastecimentoRepository;
import br.com.satyapass.abastecimento.log.AbastecimentoLogFactory;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

public class AbastecimentoService {

    private EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

    private AbastecimentoRepository abastecimentoRepository;

    public AbastecimentoService(){
        this.abastecimentoRepository = new AbastecimentoRepository();
    }

    public void gerenciarAbastecimento(BigDecimal codAbastecimento) throws Exception {
        AbastecimentoLogFactory.incluirItem("Inicio - Método: gerenciarAbastecimento: " + codAbastecimento, "", StatusItemEnum.INFO, true);

        DynamicVO abastecimentoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_ABTAVU", new Object[]{codAbastecimento});
        if (StringUtils.isNotEmpty(abastecimentoVO.asBigDecimal("NUNOTA"))){
            throw  new Exception("A requisição já foi gerada. Nro Unico: "+ abastecimentoVO.asBigDecimal("NUNOTA"));
        }else {

            if (StringUtils.isEmpty(abastecimentoVO.asBigDecimal("CODPROD")))
                throw new Exception("Para gerar a requisição, é obrigatorio ter o produto. Nro do Abastecimento: "+codAbastecimento );

            BigDecimal nroUnicoDaNota = incluirCabecalho(abastecimentoVO);
            try{
                incluirItens(nroUnicoDaNota, abastecimentoVO);
            }catch (Exception e){
                abastecimentoRepository.deletarNota(nroUnicoDaNota);
                throw new Exception("Erro ao incluir os itens: "  + ExceptionUtils.getStackTrace(e));
            }

            abastecimentoRepository.atualizarNunotaDaRequisicao(codAbastecimento, nroUnicoDaNota);
        }

    }

    public void gerenciarExclusaoDaNota(BigDecimal codAbastecimento) throws Exception {
        DynamicVO abastecimentoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_ABTAVU", new Object[]{codAbastecimento});
        if (StringUtils.isEmpty(abastecimentoVO.asBigDecimal("NUNOTA"))){
            throw new Exception("Neste abastecimento não  existe requisição. Desta maneira não é possivel excluir.");
        } else {
            abastecimentoRepository.deletarNota(abastecimentoVO.asBigDecimal("NUNOTA"));
            abastecimentoRepository.atualizarNunotaDaRequisicao(codAbastecimento, null);

        }

    }

    private BigDecimal retornarCodigoDaTop() throws Exception {
        try{
            return new BigDecimal(MGECoreParameter.getParameterAsInt("TOP_ABASTECIMEN"));
        }catch (Exception e){
            throw new Exception("Erro ao encontrar o parametro 'TOP_ABASTECIMEN'. Acionar o departamento de TI");
        }
    }

    private BigDecimal incluirCabecalho(DynamicVO abastecimentoVO) throws Exception {
        BigDecimal nunotaGerado = BigDecimal.ZERO;
        try{
            DynamicVO cabVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("CabecalhoNota");
            BigDecimal codTipOperacao = retornarCodigoDaTop();
            DynamicVO topVO = TipoOperacaoUtils.getTopVO(codTipOperacao);
            cabVO.setProperty("CODTIPOPER", topVO.asBigDecimal("CODTIPOPER"));
            cabVO.setProperty("DHTIPOPER", topVO.asTimestamp("DHALTER"));
            cabVO.setProperty("TIPMOV", topVO.asString("TIPMOV"));
            cabVO.setProperty("CODPARC", abastecimentoVO.asBigDecimalOrZero("CODPARC"));
            cabVO.setProperty("CODEMP", abastecimentoVO.asBigDecimalOrZero("CODEMP"));
            cabVO.setProperty("DTNEG", abastecimentoVO.asTimestamp("DATA"));
            cabVO.setProperty("DTFATUR", abastecimentoVO.asTimestamp("DATA"));
            cabVO.setProperty("DTENTSAI", abastecimentoVO.asTimestamp("DATA"));
            cabVO.setProperty("DTMOV", abastecimentoVO.asTimestamp("DATA"));
            cabVO.setProperty("CODNAT", BigDecimal.ZERO);
            cabVO.setProperty("CODCENCUS", BigDecimal.ZERO);
            cabVO.setProperty("CODPROJ", abastecimentoVO.asBigDecimalOrZero("CODPROJ"));
            cabVO.setProperty("CIF_FOB", "S");
            cabVO.setProperty("CODTIPVENDA", BigDecimal.ZERO);
            cabVO.setProperty("CODVEND", BigDecimal.ZERO);
            cabVO.setProperty("RATEADO", "N");
            cabVO.setProperty("CODVEICULO", abastecimentoVO.asBigDecimalOrZero("CODVEICULO"));
            cabVO.setProperty("CODUSU", AuthenticationInfo.getCurrent().getUserID());
            cabVO.setProperty("OBSERVACAO", "Gerado automaticamente pelo abastecimento avulso: " +abastecimentoVO.asBigDecimal("CODABAST") );

            CACHelper cacHelper = new CACHelper();
            JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
            PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.CABECALHO_NOTA, cabVO);
            BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(AuthenticationInfo.getCurrent(), cabPreState);
            DynamicVO newCabVO = bRegrasCab.getState().getNewVO();
            nunotaGerado = newCabVO.asBigDecimal("NUNOTA");
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("Erro ao gerar o cabeçalho: " + ExceptionUtils.getMessage(e));
        }
        return nunotaGerado;
    }

    private void incluirItens(BigDecimal nunota, DynamicVO abastecimentoVO) throws Exception {
        try {
            CACHelper cacHelper = new CACHelper();
            cacHelper.setGerarFinanceiroItemAItem(false);
            Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
            DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);

            DynamicVO produtoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("Produto", new Object[]{abastecimentoVO.asBigDecimal("CODPROD")});

            itemVO.setProperty("CODPROD", abastecimentoVO.asBigDecimal("CODPROD"));
            itemVO.setProperty("QTDNEG", abastecimentoVO.asBigDecimalOrZero("QUANTIDADE"));
            itemVO.setProperty("CODVOL", produtoVO.asString("CODVOL"));

            if(produtoVO.asString("USALOCAL").equals("S"))
                itemVO.setProperty("CODLOCALORIG", abastecimentoVO.asBigDecimalOrZero("CODLOCAL"));

            BigDecimal valorCusto = getCusto(abastecimentoVO);
            AbastecimentoLogFactory.incluirItem("Valor do custo de Reposição:  " + valorCusto, "", StatusItemEnum.OK, true);
            itemVO.setProperty("VLRUNIT", valorCusto);
            PrePersistEntityState itePreState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA, itemVO);
            itensNota.add(itePreState);
            cacHelper.incluirAlterarItem(nunota, AuthenticationInfo.getCurrent(), itensNota, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erro ao incluir item: Nota:" + nunota + " - " + ExceptionUtils.getStackTrace(e) );
        }
    }

    private BigDecimal getCusto(DynamicVO abastecimentoVO) throws Exception {
        try{
            PrecoCustoHelper precoCustoHelper = new PrecoCustoHelper();
            return precoCustoHelper.getCusto(
                    abastecimentoVO.asBigDecimal("CODPROD"),
                    abastecimentoVO.asBigDecimal("CODEMP"),
                    abastecimentoVO.asBigDecimal("CODLOCAL"),
                    " ",
                    TimeUtils.getNow(),
                    TipoCusto.CUSTO_REPOSICAO
            );
        }catch (Exception e){
            throw new Exception("Erro ao buscar o custo do produto: " + abastecimentoVO.asBigDecimal("CODPROD") +  " - " + ExceptionUtils.getStackTrace(e));
        }
    }

}
