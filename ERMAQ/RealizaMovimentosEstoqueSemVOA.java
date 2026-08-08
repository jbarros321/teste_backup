package br.com.ermaq.ScheduledAction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import com.sankhya.util.JdbcUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.ws.ServiceContext;
import java.sql.Timestamp;

public class RealizaMovimentosEstoqueSemVOA implements ScheduledAction {

    @Override
    public void onTime(ScheduledActionContext arg0) {
        inserirLog("=== INICIANDO PROCESSAMENTO DE MOVIMENTOS DE ESTOQUE (PROPOSTA SEM VOA) ===", "S");
        ServiceContext ctx = null;

        try {
            AuthenticationInfo auth = AuthenticationInfo.getCurrent();
            auth.makeCurrent();
            ctx = new ServiceContext(null);
            ctx.setAutentication(auth);
            ctx.makeCurrent();
            SPBeanUtils.setupContext(ctx);
            inserirLog("Contexto configurado com sucesso", "S");

            JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
            Collection<DynamicVO> cabVOs = cabDAO.find("AD_CONTROLAESTOQUE = 'S' AND STATUSNOTA = 'L'");
            inserirLog("Total de notas encontradas: " + cabVOs.size(), "S");

            int notasProcessadas = 0;
            int notasComSucesso = 0;
            int notasComErro = 0;

            for (DynamicVO cabVO : cabVOs) {
                final BigDecimal nuNota = cabVO.asBigDecimal("NUNOTA");
                final String tipMov = cabVO.asString("TIPMOV");

                notasProcessadas++;
                inserirLog("=== PROCESSANDO NOTA " + nuNota + " (" + notasProcessadas + "/" + cabVOs.size() + ") ===",
                        "S");

                JapeSession.SessionHandle hnd = null;

                try {
                    hnd = JapeSession.open();
                    hnd.setCanTimeout(false);
                    hnd.execWithTX(new JapeSession.TXBlock() {
                        public void doWithTx() throws Exception {
                            if ("C".equals(tipMov)) {
                                BigDecimal nuReqBaixa = geraBaixaCompra(nuNota);
                                BigDecimal nuReqEntrada = geraEntradaCompra(nuNota);
                                confirmarNota(nuReqBaixa);
                                confirmarNota(nuReqEntrada);
                                atualizarStatus(nuNota, nuReqBaixa, nuReqEntrada);
                            } else if ("F".equals(tipMov) || "V".equals(tipMov)) {
                                BigDecimal nuReqBaixa = geraBaixaRetalho(nuNota);
                                BigDecimal nuReqEntrada = geraEntradaRetalho(nuNota);
                                confirmarNota(nuReqBaixa);
                                confirmarNota(nuReqEntrada);
                                atualizarStatus(nuNota, nuReqBaixa, nuReqEntrada);
                            }
                        }
                    });
                    notasComSucesso++;
                } catch (Exception e) {
                    notasComErro++;
                    inserirLog("[NOTA: " + nuNota + "] Erro: " + e.getMessage(), "E");
                    logarErroDetalhado("ERRO NA TRANSAÇÃO", nuNota, e);
                } finally {
                    JapeSession.close(hnd);
                }
            }
            inserirLog("Processamento finalizado. Sucesso: " + notasComSucesso + " | Erro: " + notasComErro, "S");

        } catch (Exception e) {
            inserirLog("Erro crítico: " + e.getMessage(), "E");
        } finally {
            if (ctx != null)
                ctx.unregistry();
            AuthenticationInfo.unregistry();
        }
    }

    private BigDecimal geraBaixaRetalho(BigDecimal nuNota) throws Exception {
        inserirLog("Gera Baixa Retalho (Nova Lógica)...", "S");
        BigDecimal nuReqBaixa = null;
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rs = null;
        SessionHandle hnd = null;

        try {
            JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            AuthenticationInfo auth = AuthenticationInfo.getCurrent();
            JapeSessionContext.putProperty("usuario_logado", auth.getUserID());

            DynamicVO reqVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
            reqVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
            reqVO.setProperty("CODTIPOPER", new BigDecimal(MGECoreParameter.getParameterAsInt("TOPBAIXAAJUSTE")));
            reqVO.setProperty("TIPMOV", "Q");
            reqVO.setProperty("DTNEG", TimeUtils.getNow());
            reqVO.setProperty("CODNAT", new BigDecimal(MGECoreParameter.getParameterAsInt("NATREQBAIXARET")));
            reqVO.setProperty("CODCENCUS", new BigDecimal(MGECoreParameter.getParameterAsInt("CRREQBAIXARET")));

            CACHelper cacHelper = new CACHelper();
            JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
            PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade,
                    DynamicEntityNames.CABECALHO_NOTA, reqVO);
            BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);
            nuReqBaixa = bRegrasCab.getState().getNewVO().asBigDecimal("NUNOTA");

            Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();

            hnd = JapeSession.open();
            jdbc = dwfEntityFacade.getJdbcWrapper();
            jdbc.openSession();
            sql = new NativeSql(jdbc);

            // Query sem TGFVOA - Buscando campos customizados
            sql.appendSql(
                    "SELECT ITE.CODEMP, ITE.CODPROD, ITE.QTDNEG, ITE.CODVOL, ITE.CODLOCALORIG, ITE.CONTROLE, PRO.AD_TIPRET, ");
            sql.appendSql(
                    "ITE.AD_BO20000X1200, ITE.AD_BO20000X1400, ITE.AD_BR6000, ITE.AD_CH2000X1200, ITE.AD_CH2000X1500, ITE.AD_CH3000X1200, ITE.AD_CH3000X1500 ");
            sql.appendSql("FROM TGFITE ITE INNER JOIN TGFPRO PRO ON ITE.CODPROD = PRO.CODPROD ");
            sql.appendSql(
                    "WHERE PRO.AD_CONTROLARETALHO = 'S' AND ITE.CONTROLE LIKE '%Inteiro' AND ITE.NUNOTA = :NUNOTA");
            sql.setNamedParameter("NUNOTA", nuNota);
            rs = sql.executeQuery();

            while (rs.next()) {
                BigDecimal qtdNeg = rs.getBigDecimal("QTDNEG");
                String controle = rs.getString("CONTROLE");
                BigDecimal medidaPadrao = obterMedidaPadrao(rs, controle);

                if (medidaPadrao != null && medidaPadrao.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal consumo = qtdNeg.remainder(medidaPadrao);
                    if (consumo.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal retalho = medidaPadrao.subtract(consumo);

                        DynamicVO itemVO = (DynamicVO) dwfEntityFacade
                                .getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
                        itemVO.setProperty("NUNOTA", nuReqBaixa);
                        itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
                        itemVO.setProperty("CODPROD", rs.getBigDecimal("CODPROD"));
                        itemVO.setProperty("CODVOL", rs.getString("CODVOL"));
                        itemVO.setProperty("CODLOCALORIG", rs.getBigDecimal("CODLOCALORIG"));
                        itemVO.setProperty("QTDNEG", retalho);
                        itemVO.setProperty("CONTROLE", controle);

                        itensNota.add(
                                PrePersistEntityState.build(dwfEntityFacade, DynamicEntityNames.ITEM_NOTA, itemVO));
                    }
                }
            }

            if (!itensNota.isEmpty()) {
                cacHelper.incluirAlterarItem(nuReqBaixa, auth, itensNota, true);
            }

        } finally {
            JdbcUtils.closeResultSet(rs);
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);
        }
        return nuReqBaixa;
    }

    private BigDecimal geraEntradaRetalho(BigDecimal nuNota) throws Exception {
        inserirLog("Gera Entrada Retalho (Nova Lógica)...", "S");
        BigDecimal nuReqEntrada = null;
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rs = null;
        SessionHandle hnd = null;

        try {
            JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            AuthenticationInfo auth = AuthenticationInfo.getCurrent();
            JapeSessionContext.putProperty("usuario_logado", auth.getUserID());

            DynamicVO reqVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
            reqVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
            reqVO.setProperty("CODTIPOPER", new BigDecimal(MGECoreParameter.getParameterAsInt("TOPENTRAAJUSTE")));
            reqVO.setProperty("TIPMOV", "Q");
            reqVO.setProperty("DTNEG", TimeUtils.getNow());
            reqVO.setProperty("CODNAT", new BigDecimal(MGECoreParameter.getParameterAsInt("NATREQENTRET")));
            reqVO.setProperty("CODCENCUS", new BigDecimal(MGECoreParameter.getParameterAsInt("CRREQENTRET")));

            CACHelper cacHelper = new CACHelper();
            JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
            PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade,
                    DynamicEntityNames.CABECALHO_NOTA, reqVO);
            BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);
            nuReqEntrada = bRegrasCab.getState().getNewVO().asBigDecimal("NUNOTA");

            Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();

            hnd = JapeSession.open();
            jdbc = dwfEntityFacade.getJdbcWrapper();
            jdbc.openSession();
            sql = new NativeSql(jdbc);

            sql.appendSql(
                    "SELECT ITE.CODEMP, ITE.CODPROD, ITE.QTDNEG, ITE.CODVOL, ITE.CODLOCALORIG, ITE.CONTROLE, PRO.AD_TIPRET, ");
            sql.appendSql(
                    "ITE.AD_BO20000X1200, ITE.AD_BO20000X1400, ITE.AD_BR6000, ITE.AD_CH2000X1200, ITE.AD_CH2000X1500, ITE.AD_CH3000X1200, ITE.AD_CH3000X1500 ");
            sql.appendSql("FROM TGFITE ITE INNER JOIN TGFPRO PRO ON ITE.CODPROD = PRO.CODPROD ");
            sql.appendSql(
                    "WHERE PRO.AD_CONTROLARETALHO = 'S' AND ITE.CONTROLE LIKE '%Inteiro' AND ITE.NUNOTA = :NUNOTA");
            sql.setNamedParameter("NUNOTA", nuNota);
            rs = sql.executeQuery();

            while (rs.next()) {
                BigDecimal qtdNeg = rs.getBigDecimal("QTDNEG");
                String controle = rs.getString("CONTROLE");
                BigDecimal medidaPadrao = obterMedidaPadrao(rs, controle);

                if (medidaPadrao != null && medidaPadrao.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal consumo = qtdNeg.remainder(medidaPadrao);
                    if (consumo.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal retalho = medidaPadrao.subtract(consumo);

                        DynamicVO itemVO = (DynamicVO) dwfEntityFacade
                                .getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
                        itemVO.setProperty("NUNOTA", nuReqEntrada);
                        itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
                        itemVO.setProperty("CODPROD", rs.getBigDecimal("CODPROD"));
                        itemVO.setProperty("CODVOL", rs.getString("CODVOL"));
                        itemVO.setProperty("CODLOCALORIG", rs.getBigDecimal("CODLOCALORIG"));
                        itemVO.setProperty("QTDNEG", retalho);

                        String tipRet = rs.getString("AD_TIPRET");
                        itemVO.setProperty("CONTROLE", definirControleRetalho(retalho, tipRet));

                        itensNota.add(
                                PrePersistEntityState.build(dwfEntityFacade, DynamicEntityNames.ITEM_NOTA, itemVO));
                    }
                }
            }

            if (!itensNota.isEmpty()) {
                cacHelper.incluirAlterarItem(nuReqEntrada, auth, itensNota, true);
            }

        } finally {
            JdbcUtils.closeResultSet(rs);
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);
        }
        return nuReqEntrada;
    }

    private BigDecimal obterMedidaPadrao(ResultSet rs, String controle) throws Exception {
        if (controle == null)
            return BigDecimal.ZERO;

        // Mapeamento dinâmico baseado no Controle
        if (controle.contains("20000x1200"))
            return rs.getBigDecimal("AD_BO20000X1200");
        if (controle.contains("20000x1400"))
            return rs.getBigDecimal("AD_BO20000X1400");
        if (controle.contains("6000"))
            return rs.getBigDecimal("AD_BR6000");
        if (controle.contains("2000x1200"))
            return rs.getBigDecimal("AD_CH2000X1200");
        if (controle.contains("2000x1500"))
            return rs.getBigDecimal("AD_CH2000X1500");
        if (controle.contains("3000x1200"))
            return rs.getBigDecimal("AD_CH3000X1200");
        if (controle.contains("3000x1500"))
            return rs.getBigDecimal("AD_CH3000X1500");

        return BigDecimal.ZERO;
    }

    private String definirControleRetalho(BigDecimal retalho, String tipRet) {
        if ("CH".equals(tipRet)) {
            if (retalho.compareTo(new BigDecimal("0.75")) <= 0)
                return "0x750Retalho";
            if (retalho.compareTo(new BigDecimal("1.5")) <= 0)
                return "751x1500Retalho";
            return ">1500Retalho";
        } else if ("BR".equals(tipRet)) {
            if (retalho.compareTo(new BigDecimal("0.5")) <= 0)
                return "0x500Retalho";
            if (retalho.compareTo(new BigDecimal("1.5")) <= 0)
                return "501x1500Retalho";
            if (retalho.compareTo(new BigDecimal("3.0")) <= 0)
                return "1501x3000Retalho";
            return ">3000Retalho";
        } else if ("BO".equals(tipRet)) {
            if (retalho.compareTo(new BigDecimal("0.5")) <= 0)
                return "0x500Retalho";
            if (retalho.compareTo(new BigDecimal("1.0")) <= 0)
                return "501x1000Retalho";
            if (retalho.compareTo(new BigDecimal("2.0")) <= 0)
                return "1001x2000Retalho";
            return ">2000Retalho";
        }
        return "Retalho";
    }

    private BigDecimal geraBaixaCompra(BigDecimal nuNota) throws Exception {
        inserirLog("[NOTA: " + nuNota + "] - Iniciando geração de baixa de compra", "S");
        BigDecimal nuReqBaixa = null;

        try {
            JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            if (cabVO == null) {
                throw new Exception("Cabeçalho de nota não encontrado para NUNOTA=" + nuNota);
            }

            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            AuthenticationInfo auth = AuthenticationInfo.getCurrent();
            JapeSessionContext.putProperty("usuario_logado", auth.getUserID());

            DynamicVO reqVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
            reqVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
            reqVO.setProperty("CODTIPOPER", new BigDecimal(MGECoreParameter.getParameterAsInt("TOPBAIXACONTEST")));
            reqVO.setProperty("TIPMOV", "Q");
            reqVO.setProperty("DTNEG", TimeUtils.getNow());
            reqVO.setProperty("CODNAT", new BigDecimal(MGECoreParameter.getParameterAsInt("CODNATBAIXACOMP")));
            reqVO.setProperty("CODCENCUS", new BigDecimal(MGECoreParameter.getParameterAsInt("CODCRBAIXACOMP")));

            CACHelper cacHelper = new CACHelper();
            JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);

            PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade,
                    DynamicEntityNames.CABECALHO_NOTA, reqVO);
            BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);
            nuReqBaixa = bRegrasCab.getState().getNewVO().asBigDecimal("NUNOTA");

            JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
            Collection<DynamicVO> itens = iteDAO.find(
                    "NUNOTA = ? AND CONTROLE LIKE '%Inteiro' AND (AD_BO20000X1200 IS NOT NULL OR AD_BO20000X1400 IS NOT NULL OR AD_BORETALHO IS NOT NULL OR AD_BR6000 IS NOT NULL OR AD_BRRETALHO IS NOT NULL OR AD_CH2000X1200 IS NOT NULL OR AD_CH2000X1500 IS NOT NULL OR AD_CH3000X1200 IS NOT NULL OR AD_CH3000X1500 IS NOT NULL OR AD_CHRETALHO IS NOT NULL)",
                    nuNota);

            Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();

            for (DynamicVO ite : itens) {
                DynamicVO itemVO = (DynamicVO) dwfEntityFacade
                        .getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
                itemVO.setProperty("NUNOTA", nuReqBaixa);
                itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
                itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
                itemVO.setProperty("QTDNEG", ite.getProperty("QTDNEG"));
                itemVO.setProperty("CODVOL", ite.getProperty("CODVOL"));
                itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
                itemVO.setProperty("CONTROLE", ite.getProperty("CONTROLE"));
                itemVO.setProperty("VLRUNIT", ite.getProperty("VLRUNIT"));
                itemVO.setProperty("VLRDESC", new BigDecimal(0));
                itemVO.setProperty("PERCDESC", new BigDecimal(0));

                itensNota.add(PrePersistEntityState.build(dwfEntityFacade, DynamicEntityNames.ITEM_NOTA, itemVO));
            }

            cacHelper.incluirAlterarItem(nuReqBaixa, auth, itensNota, true);

        } catch (Exception e) {
            logarErroDetalhado("Erro na geração de baixa de compra", nuNota, e);
            throw e;
        }

        return nuReqBaixa;
    }

    private BigDecimal geraEntradaCompra(BigDecimal nuNota) throws Exception {
        inserirLog("Iniciando geração de entrada de compra para nota: " + nuNota, "S");
        BigDecimal nuReqEntrada = null;
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rs = null;
        SessionHandle hnd = null;

        try {
            hnd = JapeSession.open();
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbc = dwfEntityFacade.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.appendSql("SELECT VLRDESTAQUE FROM TGFCAB WHERE NUNOTA = :NUNOTA");
            sql.setNamedParameter("NUNOTA", nuNota);
            rs = sql.executeQuery();

            BigDecimal valorDestaque = BigDecimal.ZERO;
            if (rs.next()) {
                valorDestaque = rs.getBigDecimal("VLRDESTAQUE");
            }

            JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
            DynamicVO cabVO = cabDAO.findByPK(nuNota);

            AuthenticationInfo auth = AuthenticationInfo.getCurrent();
            JapeSessionContext.putProperty("usuario_logado", auth.getUserID());

            DynamicVO reqVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
            reqVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
            reqVO.setProperty("CODTIPOPER", new BigDecimal(MGECoreParameter.getParameterAsInt("TOPENTRACONTEST")));
            reqVO.setProperty("TIPMOV", "Q");
            reqVO.setProperty("DTNEG", TimeUtils.getNow());
            reqVO.setProperty("CODNAT", new BigDecimal(MGECoreParameter.getParameterAsInt("CODNATENTCOMP")));
            reqVO.setProperty("CODCENCUS", new BigDecimal(MGECoreParameter.getParameterAsInt("CODCRENTCOMP")));
            reqVO.setProperty("VLRDESTAQUE", valorDestaque);

            CACHelper cacHelper = new CACHelper();
            JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);

            PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade,
                    DynamicEntityNames.CABECALHO_NOTA, reqVO);
            BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);
            nuReqEntrada = bRegrasCab.getState().getNewVO().asBigDecimal("NUNOTA");

            JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
            Collection<DynamicVO> itens = iteDAO.find(
                    "NUNOTA = ? AND CONTROLE LIKE '%Inteiro' AND (AD_BO20000X1200 IS NOT NULL OR AD_BO20000X1400 IS NOT NULL OR AD_BORETALHO IS NOT NULL OR AD_BR6000 IS NOT NULL OR AD_BRRETALHO IS NOT NULL OR AD_CH2000X1200 IS NOT NULL OR AD_CH2000X1500 IS NOT NULL OR AD_CH3000X1200 IS NOT NULL OR AD_CH3000X1500 IS NOT NULL OR AD_CHRETALHO IS NOT NULL)",
                    nuNota);

            Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();

            for (DynamicVO ite : itens) {
                JapeWrapper prodDAO = JapeFactory.dao(DynamicEntityNames.PRODUTO);
                DynamicVO prodVO = prodDAO.findByPK(ite.getProperty("CODPROD"));

                String[] campos = { "AD_BO20000X1200", "AD_BO20000X1400", "AD_BR6000", "AD_CH2000X1200",
                        "AD_CH2000X1500", "AD_CH3000X1200", "AD_CH3000X1500", "AD_CHRETALHO",
                        "AD_BRRETALHO", "AD_BORETALHO" };
                String[] controles = { "20000x1200Inteiro", "20000x1400Inteiro", "6000Inteiro", "2000x1200Inteiro",
                        "2000x1500Inteiro", "3000x1200Inteiro", "3000x1500Inteiro", "Retalho",
                        "Retalho", "Retalho" };

                for (int i = 0; i < campos.length; i++) {
                    BigDecimal vlrCampo = ite.asBigDecimalOrZero(campos[i]);
                    if (vlrCampo.compareTo(BigDecimal.ZERO) > 0) {
                        DynamicVO itemVO = (DynamicVO) dwfEntityFacade
                                .getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
                        itemVO.setProperty("NUNOTA", nuReqEntrada);
                        itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
                        itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
                        itemVO.setProperty("QTDNEG", vlrCampo);
                        itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
                        itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
                        itemVO.setProperty("CONTROLE", controles[i]);

                        if (!"Retalho".equals(controles[i])) {
                            itemVO.setProperty("VLRUNIT",
                                    ite.asBigDecimal("VLRTOT").divide(vlrCampo, 2, RoundingMode.HALF_UP));
                        } else {
                            itemVO.setProperty("VLRUNIT",
                                    ite.asBigDecimal("VLRTOT").divide(vlrCampo, 2, RoundingMode.HALF_UP));
                            // Lógica extra de controle de retalho se necessário
                        }
                        itemVO.setProperty("VLRDESC", new BigDecimal(0));
                        itemVO.setProperty("PERCDESC", new BigDecimal(0));

                        itensNota.add(
                                PrePersistEntityState.build(dwfEntityFacade, DynamicEntityNames.ITEM_NOTA, itemVO));
                    }
                }
            }
            cacHelper.incluirAlterarItem(nuReqEntrada, auth, itensNota, true);

        } catch (Exception e) {
            logarErroDetalhado("Erro na geração de entrada de compra", nuNota, e);
            throw e;
        } finally {
            JdbcUtils.closeResultSet(rs);
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);
        }
        return nuReqEntrada;
    }

    private void atualizarStatus(BigDecimal nuNota, BigDecimal nuReqBaixa, BigDecimal nuReqEntrada) throws Exception {
        JapeWrapper isDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
        isDAO.prepareToUpdateByPK(nuNota)
                .set("AD_CONTROLAESTOQUE", "N")
                .set("AD_NUREQBAIXA", nuReqBaixa)
                .set("AD_NUREQENTRADA", nuReqEntrada)
                .update();
    }

    private void confirmarNota(BigDecimal nuNota) throws Exception {
        BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(CentralFaturamento.class,
                "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
        barramentoConfirmacao.setValidarSilencioso(true);
        ConfirmacaoNotaHelper.confirmarNota(nuNota, barramentoConfirmacao);
    }

    private void inserirLog(String mensagem, String resultado) {
        new Thread(() -> {
            try {
                JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
                jdbc.openSession();
                NativeSql sqlSeq = new NativeSql(jdbc);
                sqlSeq.appendSql("SELECT NVL(MAX(SEQUENCIA), 0) + 1 AS SEQ FROM AD_DETACAOLOG WHERE NUACAO = 2");
                ResultSet rs = sqlSeq.executeQuery();
                BigDecimal seq = rs.next() ? rs.getBigDecimal("SEQ") : BigDecimal.ONE;

                NativeSql sqlInsert = new NativeSql(jdbc);
                sqlInsert.appendSql(
                        "INSERT INTO AD_DETACAOLOG (SEQUENCIA, NUACAO, DTINCLUSAO, STATUS, RESULTADO) VALUES (:SEQ, 2, SYSDATE, :MS, :RS)");
                sqlInsert.setNamedParameter("SEQ", seq);
                sqlInsert.setNamedParameter("MS", mensagem);
                sqlInsert.setNamedParameter("RS", resultado);
                sqlInsert.executeUpdate();
                JdbcUtils.closeResultSet(rs);
                NativeSql.releaseResources(sqlSeq);
                NativeSql.releaseResources(sqlInsert);
                JdbcWrapper.closeSession(jdbc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void logarErroDetalhado(String opt, BigDecimal nu, Exception e) {
        inserirLog(opt + " " + (nu != null ? nu : "") + ": " + e.getMessage(), "E");
    }
}
