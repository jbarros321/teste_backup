package br.com.petkids.neogrid.repository;

import br.com.petkids.neogrid.exception.NeogridRepositoryException;
import br.com.petkids.neogrid.model.dto.ItemVendaDTO;
import br.com.petkids.neogrid.model.dto.VendaDTO;
import br.com.petkids.neogrid.model.enums.TipoFaturamento;
import br.com.petkids.neogrid.model.enums.TipoFrete;
import br.com.petkids.neogrid.model.enums.TipoNF;
import br.com.petkids.neogrid.util.NeogridFormatter;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import com.sankhya.util.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendasRepository extends AbstractNeogridRepository {

    public List<VendaDTO> buscarVendas(String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) throws NeogridRepositoryException {
        try {
            return executarConsultaComJdbc((sqlNative, jdbc) -> {
                Map<BigDecimal, VendaDTO> vendasMap = new HashMap<>(8192);
                Timestamp periodoFinAjustado = ajustarPeriodoFinal(periodoFin);
                sqlNative.appendSql("SELECT CAB.NUNOTA, CAB.NUMNOTA, CAB.SERIENOTA AS SERIENOT, " +
                    "NVL(CAB.DTFATUR, CAB.DTNEG) AS DTEMISSAO, CAB.CODPARC, CAB.CODVEND AS CODVEN, " +
                    "CASE WHEN CAB.CODTIPVENDA IS NULL OR CAB.CODTIPVENDA = 0 THEN '1' ELSE '2' END AS CONDVENDA, " +
                    "NVL(CAB.CIF_FOB, 'F') AS CIF_FOB, CASE WHEN UFS_EMISSOR.UF IS NOT NULL AND TRIM(UFS_EMISSOR.UF) != '' THEN TRIM(UFS_EMISSOR.UF) WHEN UFS_EMP.UF IS NOT NULL AND TRIM(UFS_EMP.UF) != '' THEN TRIM(UFS_EMP.UF) ELSE 'DF' END AS UF_EMISSOR, " +
                    "PAR_EMISSOR.CEP AS CEP_EMISSOR, NVL(UFS_DEST.UF, '') AS UF_DESTINATARIO, " +
                    "PAR.CEP AS CEP_DESTINATARIO, NVL(PPG_MAX.PRAZO, 0) AS DIAS_PAGAMENTO, " +
                    "ITE_COMPRA_DIST.CNPJ_INDUSTRIA, " +
                    "PAR.TIPPESSOA AS TIPO_PESSOA_CLIENTE, " +
                    "REPLACE(REPLACE(REPLACE(REPLACE(NVL(PAR.CGC_CPF, ''), '.', ''), '/', ''), '-', ''), ' ', '') AS CGC_CPF_CLIENTE, " +
                    "CAB.TIPMOV, CAB.STATUSNOTA " +
                    "FROM TGFCAB CAB " +
                    "INNER JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC " +
                    "INNER JOIN TSIEMP EMP ON CAB.CODEMP = EMP.CODEMP " +
                    "INNER JOIN (SELECT DISTINCT ITE.NUNOTA, ITE.CODPROD FROM TGFITE ITE " +
                    "INNER JOIN TGFPRO PRO ON ITE.CODPROD = PRO.CODPROD AND PRO.ATIVO = 'S') ITE_DIST " +
                    "ON CAB.NUNOTA = ITE_DIST.NUNOTA " +
                    "INNER JOIN (SELECT DISTINCT ITE_COMPRA.CODPROD, CAB_COMPRA.CODPARC, " +
                    "REPLACE(REPLACE(REPLACE(REPLACE(PAR_FORN.CGC_CPF, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ_INDUSTRIA " +
                    "FROM TGFITE ITE_COMPRA " +
                    "INNER JOIN TGFCAB CAB_COMPRA ON ITE_COMPRA.NUNOTA = CAB_COMPRA.NUNOTA AND CAB_COMPRA.TIPMOV = 'C' AND CAB_COMPRA.STATUSNOTA = 'L' " +
                    "INNER JOIN TGFPAR PAR_FORN ON CAB_COMPRA.CODPARC = PAR_FORN.CODPARC " +
                    "WHERE PAR_FORN.ATIVO = 'S' AND PAR_FORN.AD_INTEGRANEOGRID = 'S'");
                if (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty()) {
                    if (isCnpjKelco(cnpjIndustria)) {
                        sqlNative.appendSql(" AND PAR_FORN.CGC_CPF IN ('89064885000104', '13809963000344', '08811119000822')");
                    } else {
                        sqlNative.appendSql(" AND PAR_FORN.CGC_CPF = :CNPJ_INDUSTRIA");
                    }
                }
                sqlNative.appendSql(") ITE_COMPRA_DIST ON ITE_DIST.CODPROD = ITE_COMPRA_DIST.CODPROD " +
                    "LEFT JOIN TGFPAR PAR_EMISSOR ON EMP.CODPARC = PAR_EMISSOR.CODPARC " +
                    "LEFT JOIN TSICID CID_EMISSOR ON PAR_EMISSOR.CODCID = CID_EMISSOR.CODCID " +
                    "LEFT JOIN TSIUFS UFS_EMISSOR ON CID_EMISSOR.UF = UFS_EMISSOR.CODUF " +
                    "LEFT JOIN TSICID CID_EMP ON PAR_EMISSOR.CODCID = CID_EMP.CODCID " +
                    "LEFT JOIN TSIUFS UFS_EMP ON CID_EMP.UF = UFS_EMP.CODUF " +
                    "LEFT JOIN TSICID CID_DEST ON PAR.CODCID = CID_DEST.CODCID " +
                    "LEFT JOIN TSIUFS UFS_DEST ON CID_DEST.UF = UFS_DEST.CODUF " +
                    "LEFT JOIN (SELECT CODTIPVENDA, MAX(PRAZO) AS PRAZO FROM TGFPPG GROUP BY CODTIPVENDA) PPG_MAX " +
                    "ON CAB.CODTIPVENDA = PPG_MAX.CODTIPVENDA " +
                    "WHERE (CAB.TIPMOV = 'V' OR CAB.TIPMOV = 'D') AND (CAB.STATUSNOTA = 'L' OR CAB.STATUSNOTA = 'C')");
                if (periodoIni != null) sqlNative.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
                if (periodoFinAjustado != null) sqlNative.appendSql(" AND CAB.DTNEG <= :PERIODO_FIN");
                sqlNative.appendSql(" GROUP BY CAB.NUNOTA, CAB.NUMNOTA, CAB.SERIENOTA, " +
                    "NVL(CAB.DTFATUR, CAB.DTNEG), CAB.CODPARC, CAB.CODVEND, CAB.CODTIPVENDA, NVL(CAB.CIF_FOB, 'F'), " +
                    "CASE WHEN UFS_EMISSOR.UF IS NOT NULL AND TRIM(UFS_EMISSOR.UF) != '' THEN TRIM(UFS_EMISSOR.UF) WHEN UFS_EMP.UF IS NOT NULL AND TRIM(UFS_EMP.UF) != '' THEN TRIM(UFS_EMP.UF) ELSE 'DF' END, PAR_EMISSOR.CEP, " +
                    "NVL(UFS_DEST.UF, ''), PAR.CEP, NVL(PPG_MAX.PRAZO, 0), ITE_COMPRA_DIST.CNPJ_INDUSTRIA, " +
                    "PAR.TIPPESSOA, PAR.CGC_CPF, CAB.TIPMOV, CAB.STATUSNOTA " +
                    "ORDER BY NVL(CAB.DTFATUR, CAB.DTNEG), CAB.NUMNOTA");
                if (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty() && !isCnpjKelco(cnpjIndustria)) {
                    sqlNative.setNamedParameter("CNPJ_INDUSTRIA", cnpjIndustria);
                }
                sqlNative.setNamedParameter("PERIODO_INI", periodoIni);
                sqlNative.setNamedParameter("PERIODO_FIN", periodoFinAjustado);
                ResultSet rs = sqlNative.executeQuery();
                while (rs.next()) {
                    vendasMap.put(rs.getBigDecimal("NUNOTA"), mapearNotaParaDTO(rs));
                }
                rs.close();
                if (!vendasMap.isEmpty()) buscarItensVendaEmBatch(vendasMap, cnpjIndustria, jdbc);
                return new ArrayList<>(vendasMap.values());
            });
        } catch (Exception e) {
            throw new NeogridRepositoryException("Erro ao buscar vendas: " + ExceptionUtils.getMessage(e), e);
        }
    }

    private VendaDTO mapearNotaParaDTO(ResultSet rs) throws Exception {
        VendaDTO dto = new VendaDTO();
        dto.setCnpjIndustria(rs.getString("CNPJ_INDUSTRIA"));
        String condVenda = rs.getString("CONDVENDA");
        dto.setTipoFaturamento("1".equals(condVenda) ? TipoFaturamento.A_VISTA : TipoFaturamento.A_PRAZO);
        dto.setNumeroNF(rs.getBigDecimal("NUMNOTA"));
        dto.setSerieNF(rs.getString("SERIENOT"));
        String tipMov = rs.getString("TIPMOV");
        String statusNota = rs.getString("STATUSNOTA");
        if ("C".equals(statusNota)) {
            dto.setTipoNF(TipoNF.CANCELAMENTO);
        } else if ("D".equals(tipMov)) {
            dto.setTipoNF(TipoNF.DEVOLUCAO);
        } else {
            dto.setTipoNF(TipoNF.VENDAS);
        }
        dto.setDataEmissao(rs.getTimestamp("DTEMISSAO"));
        String tipoPessoaCliente = rs.getString("TIPO_PESSOA_CLIENTE");
        String cgcCpfCliente = rs.getString("CGC_CPF_CLIENTE");
        String codigoCliente;
        if (tipoPessoaCliente != null && !"F".equals(tipoPessoaCliente) &&
            cgcCpfCliente != null && !cgcCpfCliente.trim().isEmpty()) {
            codigoCliente = NeogridFormatter.formatarCnpjCpf(cgcCpfCliente);
            if (codigoCliente == null || codigoCliente.isEmpty()) {
                codigoCliente = StringUtils.getNullAsEmpty(rs.getBigDecimal("CODPARC"));
            }
        } else {
            codigoCliente = StringUtils.getNullAsEmpty(rs.getBigDecimal("CODPARC"));
        }
        dto.setCodigoCliente(codigoCliente);
        dto.setCodigoVendedor(StringUtils.getNullAsEmpty(rs.getBigDecimal("CODVEN")));
        String ufEmissor = rs.getString("UF_EMISSOR");
        if (ufEmissor == null || ufEmissor.trim().isEmpty() || ufEmissor.matches("^[0-9]+$") || ufEmissor.length() != 2) {
            ufEmissor = "DF";
        } else {
            ufEmissor = ufEmissor.trim().toUpperCase();
        }
        dto.setUfEmissor(ufEmissor);
        String cepEmissor = rs.getString("CEP_EMISSOR");
        dto.setCepEmissor(cepEmissor != null ? NeogridFormatter.formatarCnpjCpf(cepEmissor) : null);
        dto.setUfDestinatario(rs.getString("UF_DESTINATARIO"));
        String cepDest = rs.getString("CEP_DESTINATARIO");
        dto.setCepDestinatario(cepDest != null ? NeogridFormatter.formatarCnpjCpf(cepDest) : null);
        String cifFob = rs.getString("CIF_FOB");
        dto.setTipoFrete(TipoFrete.fromSankhyaValue(cifFob));
        BigDecimal diasPag = rs.getBigDecimal("DIAS_PAGAMENTO");
        dto.setDiasPagamento(diasPag != null ? diasPag : BigDecimal.ZERO);
        dto.setMetodoVenda(BigDecimal.ONE);
        return dto;
    }

    private void buscarItensVendaEmBatch(Map<BigDecimal, VendaDTO> vendasMap, String cnpjIndustria, JdbcWrapper jdbc) throws NeogridRepositoryException {
        if (vendasMap.isEmpty()) return;
        List<BigDecimal> nunotas = new ArrayList<>(vendasMap.keySet());
        for (VendaDTO venda : vendasMap.values()) venda.setItens(new ArrayList<>(20));
        int batchSize = 1000;
        for (int i = 0; i < nunotas.size(); i += batchSize) {
            int end = Math.min(i + batchSize, nunotas.size());
            List<BigDecimal> batch = nunotas.subList(i, end);
            NativeSql sqlNative = new NativeSql(jdbc);
            StringBuilder inClause = new StringBuilder(batch.size() * 15);
            for (int j = 0; j < batch.size(); j++) {
                if (j > 0) inClause.append(",");
                inClause.append(batch.get(j));
            }
            sqlNative.appendSql("SELECT ITE.NUNOTA, ITE.SEQUENCIA, CAB.NUMNOTA, CAB.SERIENOTA AS SERIENOT, " +
                "LPAD(TO_CHAR(PRO.CODPROD), 20, '0') AS CODIGO_ITEM, ITE.QTDNEG, ITE.VLRUNIT, ITE.VLRTOT AS VALOR_TOTAL_BRUTO, " +
                "ITE.VLRTOT - NVL(ITE.VLRDESC, 0) AS VALOR_TOTAL_LIQUIDO, NVL(ITE.VLRIPI, 0) AS VALOR_IPI, " +
                "0 AS VALOR_PIS_CONFINS, NVL(ITE.VLRSUBST, 0) AS VALOR_SUBST_TRIB, NVL(ITE.VLRICMS, 0) AS VALOR_ICMS, " +
                "NVL(ITE.VLRDESC, 0) AS VALOR_DESCONTOS, CASE WHEN NVL(ITE.QTDNEG, 0) < 0 THEN 'S' ELSE 'N' END AS BONIFICACAO, " +
                "CAB.TIPMOV, CAB.STATUSNOTA " +
                "FROM TGFITE ITE " +
                "INNER JOIN TGFCAB CAB ON ITE.NUNOTA = CAB.NUNOTA " +
                "INNER JOIN TGFPRO PRO ON ITE.CODPROD = PRO.CODPROD AND PRO.ATIVO = 'S' " +
                "INNER JOIN (SELECT DISTINCT ITE_COMPRA.CODPROD, CAB_COMPRA.CODPARC " +
                "FROM TGFITE ITE_COMPRA " +
                "INNER JOIN TGFCAB CAB_COMPRA ON ITE_COMPRA.NUNOTA = CAB_COMPRA.NUNOTA AND CAB_COMPRA.TIPMOV = 'C' AND CAB_COMPRA.STATUSNOTA = 'L' " +
                "INNER JOIN TGFPAR PAR_FORN ON CAB_COMPRA.CODPARC = PAR_FORN.CODPARC " +
                "WHERE PAR_FORN.ATIVO = 'S' AND PAR_FORN.AD_INTEGRANEOGRID = 'S'");
            if (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty()) {
                if (isCnpjKelco(cnpjIndustria)) {
                    sqlNative.appendSql(" AND PAR_FORN.CGC_CPF IN ('89064885000104', '13809963000344', '08811119000822')");
                } else {
                    sqlNative.appendSql(" AND PAR_FORN.CGC_CPF = :CNPJ_INDUSTRIA");
                    sqlNative.setNamedParameter("CNPJ_INDUSTRIA", cnpjIndustria);
                }
            }
            sqlNative.appendSql(") ITE_COMPRA_DIST ON PRO.CODPROD = ITE_COMPRA_DIST.CODPROD " +
                "WHERE ITE.NUNOTA IN (" + inClause.toString() + ") " +
                "ORDER BY ITE.NUNOTA, ITE.SEQUENCIA");
            ResultSet rs = null;
            try {
                rs = sqlNative.executeQuery();
                while (rs.next()) {
                    VendaDTO venda = vendasMap.get(rs.getBigDecimal("NUNOTA"));
                    if (venda != null) venda.getItens().add(mapearItemParaDTO(rs));
                }
            } catch (Exception e) {
                throw new NeogridRepositoryException("Erro ao buscar itens de venda: " + ExceptionUtils.getMessage(e), e);
            } finally {
                if (rs != null) try { rs.close(); } catch (Exception e) {}
                NativeSql.releaseResources(sqlNative);
            }
        }
    }

    private ItemVendaDTO mapearItemParaDTO(ResultSet rs) throws Exception {
        ItemVendaDTO dto = new ItemVendaDTO();
        dto.setNumeroNF(rs.getBigDecimal("NUMNOTA"));
        dto.setSerieNF(rs.getString("SERIENOT"));
        String tipMov = rs.getString("TIPMOV");
        String statusNota = rs.getString("STATUSNOTA");
        if ("C".equals(statusNota)) {
            dto.setTipoNF(TipoNF.CANCELAMENTO);
        } else if ("D".equals(tipMov)) {
            dto.setTipoNF(TipoNF.DEVOLUCAO);
        } else {
            dto.setTipoNF(TipoNF.VENDAS);
        }
        String codigoItem = rs.getString("CODIGO_ITEM");
        dto.setCodigoItem(codigoItem != null && !codigoItem.trim().isEmpty() ? codigoItem.trim() : "");
        BigDecimal qtdNeg = rs.getBigDecimal("QTDNEG");
        dto.setQuantidade(qtdNeg != null ? qtdNeg.abs() : BigDecimal.ZERO);
        dto.setValorUnitario(rs.getBigDecimal("VLRUNIT") != null ? rs.getBigDecimal("VLRUNIT") : BigDecimal.ZERO);
        dto.setBonificacao(rs.getString("BONIFICACAO"));
        dto.setValorTotalBruto(rs.getBigDecimal("VALOR_TOTAL_BRUTO") != null ? rs.getBigDecimal("VALOR_TOTAL_BRUTO").abs() : BigDecimal.ZERO);
        dto.setValorTotalLiquido(rs.getBigDecimal("VALOR_TOTAL_LIQUIDO") != null ? rs.getBigDecimal("VALOR_TOTAL_LIQUIDO").abs() : BigDecimal.ZERO);
        dto.setValorIPI(rs.getBigDecimal("VALOR_IPI") != null ? rs.getBigDecimal("VALOR_IPI").abs() : BigDecimal.ZERO);
        dto.setValorPisConfins(rs.getBigDecimal("VALOR_PIS_CONFINS") != null ? rs.getBigDecimal("VALOR_PIS_CONFINS").abs() : BigDecimal.ZERO);
        dto.setValorSubstituicaoTributaria(rs.getBigDecimal("VALOR_SUBST_TRIB") != null ? rs.getBigDecimal("VALOR_SUBST_TRIB").abs() : BigDecimal.ZERO);
        dto.setValorICMS(rs.getBigDecimal("VALOR_ICMS") != null ? rs.getBigDecimal("VALOR_ICMS").abs() : BigDecimal.ZERO);
        dto.setValorDescontos(rs.getBigDecimal("VALOR_DESCONTOS") != null ? rs.getBigDecimal("VALOR_DESCONTOS").abs() : BigDecimal.ZERO);
        return dto;
    }

    public String buscarPrimeiroCnpjIndustria(Timestamp periodoIni, Timestamp periodoFin) {
        try {
            return executarConsulta(sqlNative -> {
                Timestamp periodoFinAjustado = ajustarPeriodoFinal(periodoFin);
                sqlNative.appendSql("SELECT REPLACE(REPLACE(REPLACE(REPLACE(PAR_FORN.CGC_CPF, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ_INDUSTRIA " +
                    "FROM (SELECT DISTINCT PAR_FORN.CGC_CPF " +
                    "FROM TGFPAR PAR_FORN " +
                    "INNER JOIN TGFCAB CAB_COMPRA ON PAR_FORN.CODPARC = CAB_COMPRA.CODPARC AND CAB_COMPRA.TIPMOV = 'C' " +
                    "INNER JOIN TGFITE ITE_COMPRA ON CAB_COMPRA.NUNOTA = ITE_COMPRA.NUNOTA " +
                    "INNER JOIN TGFPRO PRO ON ITE_COMPRA.CODPROD = PRO.CODPROD AND PRO.ATIVO = 'S' " +
                    "INNER JOIN TGFITE ITE ON PRO.CODPROD = ITE.CODPROD " +
                    "INNER JOIN TGFCAB CAB ON ITE.NUNOTA = CAB.NUNOTA " +
                    "WHERE PAR_FORN.ATIVO = 'S' AND PAR_FORN.AD_INTEGRANEOGRID = 'S' " +
                    "AND PAR_FORN.CGC_CPF IS NOT NULL " +
                    "AND CAB.TIPMOV = 'V' AND CAB.STATUSNOTA = 'L'");
                if (periodoIni != null) sqlNative.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
                if (periodoFinAjustado != null) sqlNative.appendSql(" AND CAB.DTNEG <= :PERIODO_FIN");
                sqlNative.appendSql(" AND ROWNUM = 1) PAR_FORN " +
                    "ORDER BY PAR_FORN.CGC_CPF");
                sqlNative.setNamedParameter("PERIODO_INI", periodoIni);
                sqlNative.setNamedParameter("PERIODO_FIN", periodoFinAjustado);
                ResultSet rs = sqlNative.executeQuery();
                String result = rs.next() ? rs.getString("CNPJ_INDUSTRIA") : null;
                rs.close();
                return result;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isCnpjKelco(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        return "89064885000104".equals(cnpjLimpo) ||
               "13809963000344".equals(cnpjLimpo) ||
               "08811119000822".equals(cnpjLimpo);
    }
}
