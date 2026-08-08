package br.com.cliente.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralItemNota;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import com.sankhya.util.TimeUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class DocumentoService implements Service {
    private final EntityFacade facade = EntityFacadeFactory.getDWFFacade();

    public BigDecimal criarNotaFiscal(BigDecimal codemp, BigDecimal codparc, BigDecimal codtipoper, String observacao) throws Exception {
        DynamicVO cabVO = (DynamicVO) facade.getDefaultValueObjectInstance(DynamicEntityNames.CABECALHO_NOTA);
        cabVO.setProperty("CODEMP", codemp);
        cabVO.setProperty("CODPARC", codparc);
        cabVO.setProperty("CODTIPOPER", codtipoper);
        cabVO.setProperty("DTNEG", TimeUtils.getNow());
        cabVO.setProperty("OBSERVACAO", Optional.ofNullable(observacao).orElse(""));
        JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
        BarramentoRegra barramento = new CACHelper().incluirAlterarCabecalho(AuthenticationInfo.getCurrent(), PrePersistEntityState.build(facade, DynamicEntityNames.CABECALHO_NOTA, cabVO));
        return barramento.getState().getNewVO().asBigDecimal("NUNOTA");
    }

    public void criarItemNota(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal vlrunit) throws Exception {
        criarItemNotaInterno(nunota, codprod, qtdneg, itemVO -> itemVO.setProperty("VLRUNIT", vlrunit));
    }

    public void criarItemNotaCompleto(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, BigDecimal codlocalorig, String codvol, String controle) throws Exception {
        DynamicVO itemVO = (DynamicVO) facade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
        itemVO.setProperty("NUNOTA", nunota);
        itemVO.setProperty("CODPROD", codprod);
        itemVO.setProperty("QTDNEG", qtdneg);
        itemVO.setProperty("CODLOCALORIG", codlocalorig);
        itemVO.setProperty("CODVOL", Optional.ofNullable(codvol).orElse(""));
        itemVO.setProperty("CONTROLE", Optional.ofNullable(controle).orElse(""));
        inicializarProduto(itemVO, nunota);
        CACHelper cacHelper = new CACHelper();
        Collection<PrePersistEntityState> itens = new ArrayList<>();
        itens.add(PrePersistEntityState.build(facade, DynamicEntityNames.ITEM_NOTA, itemVO));
        cacHelper.incluirAlterarItem(nunota, AuthenticationInfo.getCurrent(), itens, true);
    }

    private void criarItemNotaInterno(BigDecimal nunota, BigDecimal codprod, BigDecimal qtdneg, java.util.function.Consumer<DynamicVO> configurador) throws Exception {
        DynamicVO itemVO = (DynamicVO) facade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
        itemVO.setProperty("NUNOTA", nunota);
        itemVO.setProperty("CODPROD", codprod);
        itemVO.setProperty("QTDNEG", qtdneg);
        Optional.ofNullable(configurador).ifPresent(c -> c.accept(itemVO));
        CACHelper cacHelper = new CACHelper();
        Collection<PrePersistEntityState> itens = new ArrayList<>();
        itens.add(PrePersistEntityState.build(facade, DynamicEntityNames.ITEM_NOTA, itemVO));
        cacHelper.incluirAlterarItem(nunota, AuthenticationInfo.getCurrent(), itens, true);
    }

    private void inicializarProduto(DynamicVO itemVO, BigDecimal nunota) throws Exception {
        CentralItemNota.ParamsInicializacaoProduto params = new CentralItemNota.ParamsInicializacaoProduto();
        params.codProd = itemVO.asBigDecimal("CODPROD");
        params.codVol = itemVO.asString("CODVOL");
        params.qtdNeg = itemVO.asBigDecimal("QTDNEG");
        params.codLocal = itemVO.asBigDecimal("CODLOCALORIG");
        params.controle = itemVO.asString("CONTROLE");
        params.nuNota = nunota;
        params.chamadoPelaTela = true;
        CentralItemNota centralItemNota = new CentralItemNota();
        ComercialUtils.PrecoUnitarioInfo pui = centralItemNota.inicializaProduto(params);
        itemVO.setProperty("VLRUNIT", pui.getVlrUnit());
        itemVO.setProperty("PRECOBASE", pui.getPrecoBase());
        itemVO.setProperty("NUTAB", pui.getNuTab());
        centralItemNota.recalcularValores("QTDNEG", "", itemVO, nunota);
    }

    @Override
    public String gerarArquivo(String caminhoExportacao, BigDecimal nunota) throws Exception { throw new UnsupportedOperationException("Use criarNotaFiscal e criarItemNota"); }
}
