package br.com.pd.action.botaoAcao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

public class ConsolidarItensNota implements AcaoRotinaJava {

    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        System.out.println("=== INICIO ConsolidarItensNota.doAction ===");
        try {
            Object paramValue = contexto.getParam("CODTIPOPER_DEST");
            if (paramValue == null) {
                contexto.setMensagemRetorno("ERRO: Parâmetro CODTIPOPER_DEST não informado!");
                return;
            }

            BigDecimal codtipoperDest = null;
            if (paramValue instanceof Registro) {
                Object campoObj = ((Registro) paramValue).getCampo("CODTIPOPER");
                if (campoObj != null) {
                    codtipoperDest = campoObj instanceof BigDecimal ?
                        (BigDecimal) campoObj : BigDecimalUtil.valueOf(campoObj.toString());
                }
            } else if (paramValue instanceof DynamicVO) {
                codtipoperDest = ((DynamicVO) paramValue).asBigDecimal("CODTIPOPER");
            } else if (paramValue instanceof BigDecimal) {
                codtipoperDest = (BigDecimal) paramValue;
            } else {
                String str = paramValue.toString().trim();
                if (StringUtils.isNotEmpty(str)) {
                    codtipoperDest = BigDecimalUtil.valueOf(str);
                }
            }

            Object serienotaObj = contexto.getParam("SERIENOTA");
            String serienota = null;
            if (serienotaObj != null) {
                serienota = serienotaObj.toString();
            }

            BigDecimal codtipoperDestSeguro = BigDecimalUtil.getValueOrZero(codtipoperDest);
            if (codtipoperDest == null || codtipoperDestSeguro.compareTo(BigDecimal.ZERO) == 0) {
                contexto.setMensagemRetorno("TOP de destino não informada. Verifique se o parâmetro 'CODTIPOPER_DEST' foi preenchido no formulário.");
                return;
            }
            codtipoperDest = codtipoperDestSeguro;

            DynamicVO topVO = TipoOperacaoUtils.getTopVO(codtipoperDest);
            if (topVO == null) {
                contexto.setMensagemRetorno("TOP de destino " + codtipoperDest + " não encontrada.");
                return;
            }

            String adAgrupatdItens = topVO.asString("AD_AGRUPATDITENS");
            BigDecimal codprodServ = topVO.asBigDecimal("AD_SERVEMPREITADA");
            if (!"S".equals(adAgrupatdItens) || codprodServ == null) {
                contexto.setMensagemRetorno("TOP de destino não está configurada para agrupamento de itens (AD_AGRUPATDITENS = 'S' e AD_SERVEMPREITADA preenchido).");
                return;
            }

            Registro[] linhas = contexto.getLinhas();
            if (linhas == null || linhas.length == 0) {
                contexto.setMensagemRetorno("Nenhum registro selecionado.");
                return;
            }

            int processadas = 0;
            int erros = 0;
            StringBuilder mensagens = new StringBuilder();

            for (Registro linha : linhas) {
                try {
                    BigDecimal nunotaOrig = linha.getCampo("NUNOTA") != null ?
                        BigDecimalUtil.valueOf(linha.getCampo("NUNOTA").toString()) : null;
                    BigDecimal codemp = linha.getCampo("CODEMP") != null ?
                        BigDecimalUtil.valueOf(linha.getCampo("CODEMP").toString()) : null;
                    BigDecimal codparc = linha.getCampo("CODPARC") != null ?
                        BigDecimalUtil.valueOf(linha.getCampo("CODPARC").toString()) : null;

                    if (nunotaOrig == null) {
                        continue;
                    }

                    processarConsolidacao(nunotaOrig, codemp, codparc, codtipoperDest,
                                        topVO, codprodServ, serienota);
                    processadas++;

                } catch (Exception e) {
                    erros++;
                    String erroMsg = "Erro ao processar nota: " + e.getMessage();
                    mensagens.append(erroMsg).append("\n");
                    System.err.println(erroMsg);
                    e.printStackTrace();
                }
            }

            StringBuilder mensagemFinal = new StringBuilder();
            mensagemFinal.append("Processamento concluído.\n");
            mensagemFinal.append("Notas processadas: ").append(processadas).append("\n");
            if (erros > 0) {
                mensagemFinal.append("Erros: ").append(erros).append("\n");
                mensagemFinal.append(mensagens.toString());
            }

            contexto.setMensagemRetorno(mensagemFinal.toString());

        } catch (Exception e) {
            String mensagem = "Erro na execução: " + e.getMessage();
            contexto.setMensagemRetorno(mensagem);
            System.err.println(mensagem);
            e.printStackTrace();
            throw e;
        }
    }

    private void processarConsolidacao(BigDecimal nunotaOrig, BigDecimal codemp,
                                      BigDecimal codparc, BigDecimal codtipoperDest,
                                      DynamicVO topVO, BigDecimal codprodServ,
                                      String serienota) throws Exception {

        DynamicVO notaOrigVO = (DynamicVO) facade.findEntityByPrimaryKeyAsVO(
            DynamicEntityNames.CABECALHO_NOTA, new Object[]{nunotaOrig});

        if (notaOrigVO == null) {
            throw new Exception("Nota de origem " + nunotaOrig + " não encontrada.");
        }

        String pendente = notaOrigVO.asString("PENDENTE");
        String statusnota = notaOrigVO.asString("STATUSNOTA");
        System.out.println("[DEBUG] Validação nota origem - PENDENTE: " + pendente + ", STATUSNOTA: " + statusnota);

        if (!"S".equals(pendente)) {
            throw new Exception("Nota de origem " + nunotaOrig + " não está pendente (PENDENTE = '" + pendente + "'). A nota deve estar com PENDENTE = 'S' para ser consolidada.");
        }

        if (!"L".equals(statusnota)) {
            throw new Exception("Nota de origem " + nunotaOrig + " não está liberada (STATUSNOTA = '" + statusnota + "'). A nota deve estar com STATUSNOTA = 'L' para ser consolidada.");
        }

        System.out.println("[DEBUG] Nota origem validada com sucesso - PENDENTE = 'S' e STATUSNOTA = 'L'");

        System.out.println("[DEBUG] Calculando totais dos itens da nota origem: " + nunotaOrig);
        TotaisItens totais = calcularTotaisItens(nunotaOrig);
        System.out.println("[DEBUG] Totais calculados - Total Geral: " + totais.totalGeral +
                          ", Total Mão de Obra: " + totais.totalMaoObra +
                          ", Total Material: " + totais.totalMaterial);

        BigDecimal totalGeralSeguro = BigDecimalUtil.getValueOrZero(totais.totalGeral);
        if (totalGeralSeguro.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("[DEBUG] ERRO: Total geral negativo detectado: " + totalGeralSeguro);

            totalGeralSeguro = BigDecimal.ZERO;
            System.err.println("[DEBUG] Total geral corrigido para zero.");
        }

        totais.totalGeral = totalGeralSeguro;

        BigDecimal pctMaoObra = BigDecimal.ZERO;
        BigDecimal pctMaterial = BigDecimal.ZERO;
        BigDecimal totalGeralParaCalculo = BigDecimalUtil.getValueOrZero(totais.totalGeral);
        BigDecimal totalMaoObraSeguro = BigDecimalUtil.getValueOrZero(totais.totalMaoObra);
        if (totalGeralParaCalculo.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal cem = BigDecimalUtil.valueOf("100");
            pctMaoObra = totalMaoObraSeguro
                .divide(totalGeralParaCalculo, 4, RoundingMode.HALF_UP)
                .multiply(cem)
                .setScale(2, RoundingMode.HALF_UP);
            pctMaterial = cem.subtract(pctMaoObra);
        }

        String obsItem = "Composição: " + pctMaoObra + "% mão de obra e " +
                       pctMaterial + "% material";

        DynamicVO novoCabVO = criarCabecalhoNota(notaOrigVO, codemp, codparc,
                                                 codtipoperDest, topVO, serienota, obsItem);

        BigDecimal nunotaNovo = novoCabVO.asBigDecimal("NUNOTA");

        criarItemConsolidado(novoCabVO, codprodServ, totais.totalGeral, obsItem);

        marcarNotaNaoPendente(nunotaOrig);
    }

    private static class TotaisItens {
        BigDecimal totalGeral;
        BigDecimal totalMaoObra;
        BigDecimal totalMaterial;
    }

    private TotaisItens calcularTotaisItens(BigDecimal nunota) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;

        try {
            jdbc = facade.getJdbcWrapper();
            jdbc.openSession();
            sql = new NativeSql(jdbc);

            sql.appendSql("SELECT ");
            sql.appendSql("  NVL(ITE.VLRTOT, ITE.QTDNEG * ITE.VLRUNIT) AS VLRTOT, ");
            sql.appendSql("  NVL(PRO.USOPROD, 'P') AS USOPROD ");
            sql.appendSql("FROM TGFITE ITE ");
            sql.appendSql("JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD ");
            sql.appendSql("WHERE ITE.NUNOTA = :NUNOTA");
            sql.setNamedParameter("NUNOTA", nunota);

            TotaisItens totais = new TotaisItens();
            totais.totalGeral = BigDecimal.ZERO;
            totais.totalMaoObra = BigDecimal.ZERO;
            totais.totalMaterial = BigDecimal.ZERO;

            ResultSet rs = sql.executeQuery();
            int itemCount = 0;
            while (rs.next()) {
                itemCount++;
                BigDecimal vlrtot = BigDecimalUtil.getValueOrZero(rs.getBigDecimal("VLRTOT"));
                BigDecimal vlrtotOriginal = vlrtot;

                if (vlrtot.compareTo(BigDecimal.ZERO) < 0) {
                    System.err.println("[DEBUG] Item " + itemCount + " com VLRTOT negativo: " + vlrtotOriginal + ". Convertendo para zero.");
                    vlrtot = BigDecimal.ZERO;
                }
                String usoprod = rs.getString("USOPROD");
                if (usoprod == null) {
                    usoprod = "P";
                }

                totais.totalGeral = totais.totalGeral.add(vlrtot);
                if ("S".equals(usoprod)) {
                    totais.totalMaoObra = totais.totalMaoObra.add(vlrtot);
                } else {
                    totais.totalMaterial = totais.totalMaterial.add(vlrtot);
                }
                System.out.println("[DEBUG] Item " + itemCount + " - VLRTOT: " + vlrtot +
                                  " (original: " + vlrtotOriginal + "), USOPROD: " + usoprod);
            }
            System.out.println("[DEBUG] Total de itens processados: " + itemCount);

            totais.totalGeral = BigDecimalUtil.getValueOrZero(totais.totalGeral);
            if (totais.totalGeral.compareTo(BigDecimal.ZERO) < 0) {
                totais.totalGeral = BigDecimal.ZERO;
            }

            return totais;

        } finally {
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private DynamicVO criarCabecalhoNota(DynamicVO notaOrigVO, BigDecimal codemp,
                                        BigDecimal codparc, BigDecimal codtipoperDest,
                                        DynamicVO topVO, String serienota, String obsItem) throws Exception {

        DynamicVO cabVO = (DynamicVO) facade.getDefaultValueObjectInstance(
            DynamicEntityNames.CABECALHO_NOTA);

        cabVO.setProperty("CODEMP", codemp != null ? codemp : notaOrigVO.asBigDecimal("CODEMP"));
        cabVO.setProperty("CODPARC", codparc != null ? codparc : notaOrigVO.asBigDecimal("CODPARC"));
        cabVO.setProperty("CODTIPOPER", topVO.asBigDecimal("CODTIPOPER"));
        cabVO.setProperty("TIPMOV", topVO.asString("TIPMOV"));
        cabVO.setProperty("CODTIPVENDA", notaOrigVO.asBigDecimal("CODTIPVENDA"));
        cabVO.setProperty("CODNAT", notaOrigVO.asBigDecimal("CODNAT"));
        cabVO.setProperty("CODCENCUS", notaOrigVO.asBigDecimal("CODCENCUS"));
        cabVO.setProperty("CODVEND", notaOrigVO.asBigDecimal("CODVEND"));
        cabVO.setProperty("DTNEG", notaOrigVO.asTimestamp("DTNEG") != null ?
                         notaOrigVO.asTimestamp("DTNEG") : TimeUtils.getNow());
        cabVO.setProperty("SERIENOTA", serienota != null ? serienota : "");
        cabVO.setProperty("OBSERVACAO", obsItem);

        CACHelper cacHelper = new CACHelper();
        JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
        PrePersistEntityState cabPreState = PrePersistEntityState.build(
            facade, DynamicEntityNames.CABECALHO_NOTA, cabVO);
        BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(
            AuthenticationInfo.getCurrent(), cabPreState);

        return bRegrasCab.getState().getNewVO();
    }

    private void criarItemConsolidado(DynamicVO notaVO, BigDecimal codprodServ,
                                     BigDecimal vlrtot, String obsItem) throws Exception {

        DynamicVO itemVO = (DynamicVO) facade.getDefaultValueObjectInstance(
            DynamicEntityNames.ITEM_NOTA);

        itemVO.setProperty("CODPROD", codprodServ);
        itemVO.setProperty("QTDNEG", BigDecimal.ONE);
        itemVO.setProperty("VLRUNIT", vlrtot);
        itemVO.setProperty("OBSERVACAO", obsItem);

        CACHelper cacHelper = new CACHelper();
        Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
        PrePersistEntityState itePreState = PrePersistEntityState.build(
            facade, DynamicEntityNames.ITEM_NOTA, itemVO);
        itensNota.add(itePreState);

        BigDecimal nunota = notaVO.asBigDecimal("NUNOTA");
        cacHelper.incluirAlterarItem(nunota, AuthenticationInfo.getCurrent(), itensNota, true);
    }

    private void marcarNotaNaoPendente(BigDecimal nunotaOrig) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        try {
            jdbc = facade.getJdbcWrapper();
            jdbc.openSession();
            sql = new NativeSql(jdbc);

            sql.appendSql("UPDATE TGFITE SET PENDENTE = 'N' WHERE NUNOTA = :NUNOTA");
            sql.setNamedParameter("NUNOTA", nunotaOrig);
            sql.executeUpdate();

            NativeSql.releaseResources(sql);
            sql = new NativeSql(jdbc);
            sql.appendSql("UPDATE TGFCAB SET PENDENTE = 'N' WHERE NUNOTA = :NUNOTA");
            sql.setNamedParameter("NUNOTA", nunotaOrig);
            sql.executeUpdate();
        } finally {
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }
    }

}
