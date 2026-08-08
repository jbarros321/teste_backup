package br.com.petkids.neogrid.repository;

import br.com.petkids.neogrid.exception.NeogridRepositoryException;
import br.com.petkids.neogrid.model.dto.EstoqueDTO;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EstoqueRepository extends AbstractNeogridRepository {
    public List<EstoqueDTO> buscarEstoque(String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) throws NeogridRepositoryException {
        try {
            return executarConsulta(sqlNative -> {
                List<EstoqueDTO> estoques = new ArrayList<>(2000);
                Timestamp periodoFinAjustado = ajustarPeriodoFinal(periodoFin);
                sqlNative.appendSql("SELECT PRO.CODPROD AS CODIGO_ITEM, " +
                    "COALESCE(SUM(EST.ESTOQUE), 0) AS QUANTIDADE_ESTOQUE, " +
                    "SYSDATE AS DATA_HORA_ESTOQUE, " +
                    "0 AS QUANTIDADE_ESTOQUE_TRANSITO, " +
                    "REPLACE(REPLACE(REPLACE(REPLACE(PAR_FORN.CGC_CPF, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ_INDUSTRIA " +
                    "FROM TGFPRO PRO " +
                    "INNER JOIN TGFEST EST ON PRO.CODPROD = EST.CODPROD " +
                    "INNER JOIN (SELECT DISTINCT ITE_COMPRA.CODPROD, CAB_COMPRA.CODPARC " +
                    "FROM TGFITE ITE_COMPRA " +
                    "INNER JOIN TGFCAB CAB_COMPRA ON ITE_COMPRA.NUNOTA = CAB_COMPRA.NUNOTA AND CAB_COMPRA.TIPMOV = 'C'");
                if (periodoIni != null) sqlNative.appendSql(" AND CAB_COMPRA.DTNEG >= :PERIODO_INI");
                if (periodoFinAjustado != null) sqlNative.appendSql(" AND CAB_COMPRA.DTNEG <= :PERIODO_FIN");
                sqlNative.appendSql(") ITE_COMPRA_DIST " +
                    "ON PRO.CODPROD = ITE_COMPRA_DIST.CODPROD " +
                    "INNER JOIN TGFPAR PAR_FORN ON ITE_COMPRA_DIST.CODPARC = PAR_FORN.CODPARC " +
                    "WHERE PAR_FORN.ATIVO = 'S' AND PAR_FORN.AD_INTEGRANEOGRID = 'S'");
                if (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty()) {
                    if (isCnpjKelco(cnpjIndustria)) {
                        sqlNative.appendSql(" AND PAR_FORN.CGC_CPF IN ('89064885000104', '13809963000344', '08811119000822')");
                    } else {
                        sqlNative.appendSql(" AND PAR_FORN.CGC_CPF = :CNPJ_INDUSTRIA");
                    }
                }
                sqlNative.appendSql(" AND PRO.ATIVO = 'S' GROUP BY PRO.CODPROD, PAR_FORN.CGC_CPF ORDER BY PRO.CODPROD");
                if (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty() && !isCnpjKelco(cnpjIndustria)) {
                    sqlNative.setNamedParameter("CNPJ_INDUSTRIA", cnpjIndustria);
                }
                sqlNative.setNamedParameter("PERIODO_INI", periodoIni);
                sqlNative.setNamedParameter("PERIODO_FIN", periodoFinAjustado);
                ResultSet rs = sqlNative.executeQuery();
                while (rs.next()) {
                    EstoqueDTO dto = new EstoqueDTO();
                    dto.setCnpjIndustria(rs.getString("CNPJ_INDUSTRIA"));
                    dto.setDataHoraEstoque(rs.getTimestamp("DATA_HORA_ESTOQUE") != null ? rs.getTimestamp("DATA_HORA_ESTOQUE") : TimeUtils.getNow());
                    dto.setCodigoItem(StringUtils.getNullAsEmpty(rs.getBigDecimal("CODIGO_ITEM")));
                    dto.setQuantidadeEstoque(rs.getBigDecimal("QUANTIDADE_ESTOQUE") != null ? rs.getBigDecimal("QUANTIDADE_ESTOQUE") : BigDecimal.ZERO);
                    dto.setQuantidadeEstoqueTransito(rs.getBigDecimal("QUANTIDADE_ESTOQUE_TRANSITO") != null ? rs.getBigDecimal("QUANTIDADE_ESTOQUE_TRANSITO") : BigDecimal.ZERO);
                    estoques.add(dto);
                }
                rs.close();
                return estoques;
            });
        } catch (Exception e) {
            throw new NeogridRepositoryException("Erro ao buscar estoque: " + ExceptionUtils.getMessage(e), e);
        }
    }

    public String buscarPrimeiroCnpjIndustria(Timestamp periodoIni, Timestamp periodoFin) {
        try {
            return executarConsulta(sqlNative -> {
                Timestamp periodoFinAjustado = ajustarPeriodoFinal(periodoFin);
                sqlNative.appendSql("SELECT REPLACE(REPLACE(REPLACE(REPLACE(PAR_FORN.CGC_CPF, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ_INDUSTRIA " +
                    "FROM (SELECT DISTINCT PAR_FORN.CGC_CPF " +
                    "FROM TGFPAR PAR_FORN " +
                    "INNER JOIN TGFCAB CAB_COMPRA ON PAR_FORN.CODPARC = CAB_COMPRA.CODPARC AND CAB_COMPRA.TIPMOV = 'C'");
                if (periodoIni != null) sqlNative.appendSql(" AND CAB_COMPRA.DTNEG >= :PERIODO_INI");
                if (periodoFinAjustado != null) sqlNative.appendSql(" AND CAB_COMPRA.DTNEG <= :PERIODO_FIN");
                sqlNative.appendSql(" INNER JOIN TGFITE ITE_COMPRA ON CAB_COMPRA.NUNOTA = ITE_COMPRA.NUNOTA " +
                    "INNER JOIN TGFPRO PRO ON ITE_COMPRA.CODPROD = PRO.CODPROD AND PRO.ATIVO = 'S' " +
                    "WHERE PAR_FORN.ATIVO = 'S' AND PAR_FORN.AD_INTEGRANEOGRID = 'S' AND PAR_FORN.CGC_CPF IS NOT NULL " +
                    "AND ROWNUM = 1) PAR_FORN " +
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
