package br.com.satyacode.satyapass.integracaovalecard.rotinas.notas.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.PlatformService;
import br.com.sankhya.modelcore.PlatformServiceFactory;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.PrecoCustoHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.util.TipoCusto;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.satyacode.satyapass.integracaovalecard.rotinas.item.service.ItemService;
import br.com.satyacode.satyapass.integracaovalecard.rotinas.notas.repository.NotaRepository;
import com.sankhya.util.TimeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

public class NotaService {

    private EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
    private BigDecimal retornarCodigoDaTop() throws Exception {
        try {
            return new BigDecimal(MGECoreParameter.getParameterAsInt("TOP_ABASTECIMEN"));
        } catch (Exception e) {
            throw new Exception("Erro ao encontrar o parametro 'TOP_ABASTECIMEN'. Acionar o departamento de TI");
        }
    }
    private BigDecimal incluirCabecalho(DynamicVO cabecalhoVO, DynamicVO itemVO) throws Exception {
        BigDecimal nunotaGerado = BigDecimal.ZERO;
        try {
            DynamicVO cabVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("CabecalhoNota");
            BigDecimal codTipOperacao = retornarCodigoDaTop();
            DynamicVO topVO = TipoOperacaoUtils.getTopVO(codTipOperacao);
            cabVO.setProperty("CODTIPOPER", topVO.asBigDecimal("CODTIPOPER"));
            cabVO.setProperty("DHTIPOPER", topVO.asTimestamp("DHALTER"));
            cabVO.setProperty("TIPMOV", topVO.asString("TIPMOV"));
            cabVO.setProperty("CODPARC", cabecalhoVO.asBigDecimalOrZero("CODPARC"));
            cabVO.setProperty("CODEMP", cabecalhoVO.asBigDecimalOrZero("CODEMP"));
            cabVO.setProperty("DTNEG", itemVO.asTimestamp("DATA"));
            cabVO.setProperty("DTFATUR", itemVO.asTimestamp("DATA"));
            cabVO.setProperty("DTENTSAI", itemVO.asTimestamp("DATA"));
            cabVO.setProperty("DTMOV", itemVO.asTimestamp("DATA"));
            cabVO.setProperty("CODNAT", cabecalhoVO.asBigDecimalOrZero("CODNAT"));
            cabVO.setProperty("CODCENCUS", cabecalhoVO.asBigDecimalOrZero("CODCENCUS"));
            cabVO.setProperty("CODPROJ", BigDecimal.ZERO);
            cabVO.setProperty("CIF_FOB", "S");
            cabVO.setProperty("CODTIPVENDA", BigDecimal.ZERO);
            cabVO.setProperty("CODVEND", BigDecimal.ZERO);
            cabVO.setProperty("RATEADO", "N");
            cabVO.setProperty("CODVEICULO", itemVO.asBigDecimalOrZero("CODVEICULO"));
            cabVO.setProperty("CODUSU", AuthenticationInfo.getCurrent().getUserID());
            cabVO.setProperty("OBSERVACAO", "Gerado automaticamente pela integração da ValeCard: Infomormações: " +
                    "" + cabecalhoVO.asBigDecimal("NROUNICO") + " - Sequencia: " + itemVO.asBigDecimal("SEQUENCIA") );
            CACHelper cacHelper = new CACHelper();
            JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
            PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.CABECALHO_NOTA, cabVO);
            BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(AuthenticationInfo.getCurrent(), cabPreState);
            DynamicVO newCabVO = bRegrasCab.getState().getNewVO();
            nunotaGerado = newCabVO.asBigDecimal("NUNOTA");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erro ao gerar o cabeçalho: " + ExceptionUtils.getMessage(e));
        }
        return nunotaGerado;
    }

    private void incluirItens(BigDecimal nunota, DynamicVO cabecalhoVO,  DynamicVO itemImportacaoVO) throws Exception {
        try {
            CACHelper cacHelper = new CACHelper();
            cacHelper.setGerarFinanceiroItemAItem(false);
            Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
            DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
            DynamicVO produtoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("Produto", new Object[]{itemImportacaoVO.asBigDecimal("CODPROD")});
            itemVO.setProperty("CODPROD", itemImportacaoVO.asBigDecimal("CODPROD"));
            itemVO.setProperty("QTDNEG", itemImportacaoVO.asBigDecimalOrZero("QUANTIDADE"));
            itemVO.setProperty("CODVOL", produtoVO.asString("CODVOL"));
            if (produtoVO.asString("USALOCAL").equals("S"))
                itemVO.setProperty("CODLOCALORIG", cabecalhoVO.asBigDecimalOrZero("CODLOCAL"));

            BigDecimal valorCusto = getCusto(cabecalhoVO, itemImportacaoVO);
            itemVO.setProperty("VLRUNIT", valorCusto);
            PrePersistEntityState itePreState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA, itemVO);
            itensNota.add(itePreState);
            cacHelper.incluirAlterarItem(nunota, AuthenticationInfo.getCurrent(), itensNota, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erro ao incluir item: Nota:" + nunota + " - " + ExceptionUtils.getStackTrace(e));
        }
    }

    private BigDecimal getCusto(DynamicVO cabecalhoVO,  DynamicVO itemImportacaoVO) throws Exception {
        try {
            PrecoCustoHelper precoCustoHelper = new PrecoCustoHelper();
            return precoCustoHelper.getCusto(
                    itemImportacaoVO.asBigDecimal("CODPROD"),
                    cabecalhoVO.asBigDecimal("CODEMP"),
                    cabecalhoVO.asBigDecimal("CODLOCAL"),
                    " ",
                    TimeUtils.getNow(),
                    TipoCusto.CUSTO_REPOSICAO
            );
        } catch (Exception e) {
            throw new Exception("Erro ao buscar o custo do produto: " + itemImportacaoVO.asBigDecimal("CODPROD") + " - " + ExceptionUtils.getStackTrace(e));
        }
    }

    private void confirmarNota(BigDecimal nroNota) throws Exception {
        PlatformService confirmaNotaService = PlatformServiceFactory.getInstance().lookupService("@core:confirmacao.nota.service");
        confirmaNotaService.set("NUNOTA", nroNota);
        confirmaNotaService.set("CODUSUAUTHINFO", AuthenticationInfo.getCurrent().getUserID());
        confirmaNotaService.execute();
    }

    private void confirmarNota2(BigDecimal nroNota) throws Exception {
        JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
        BarramentoRegra barramento = null;
        try {
            barramento = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
            ConfirmacaoNotaHelper.confirmarNota(nroNota, barramento);
        } catch (Exception e) {
            throw new Exception(String.format("Erro ao confirmar: ID: %s  MOTIVO: %s ", nroNota, ExceptionUtils.getMessage(e)));
        } finally {
            jdbc.closeSession();
        }
    }

    public void gerenciarNota(DynamicVO cabecalhoVO, DynamicVO itemVO) throws Exception {
        BigDecimal nroUnicoDaNota = incluirCabecalho(cabecalhoVO, itemVO);
        try {
            incluirItens(nroUnicoDaNota, cabecalhoVO, itemVO);
        } catch (Exception e) {
            new NotaRepository().deletarNota(nroUnicoDaNota);
            nroUnicoDaNota = null;
            throw new Exception("Erro ao incluir os itens: " + ExceptionUtils.getStackTrace(e));
        }

        new ItemService().atualizarCamposNosItens(itemVO, "NUNOTA", nroUnicoDaNota, false );
    }

    public void gerenciarDelecaoDasNotas(DynamicVO itemVO) throws Exception {
        new NotaRepository().deletarNota(itemVO.asBigDecimal("NUNOTA"));
        new ItemService().atualizarCamposNosItens(itemVO, "NUNOTA", null, false );
    }
}
