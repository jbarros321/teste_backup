package br.com.petkids.neogrid.repository;

import br.com.petkids.neogrid.exception.NeogridRepositoryException;
import br.com.petkids.neogrid.model.dto.ProdutoDTO;
import br.com.petkids.neogrid.util.NeogridFormatter;
import com.sankhya.util.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ProdutosRepository extends AbstractNeogridRepository {
    public List<ProdutoDTO> buscarProdutos(String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) throws NeogridRepositoryException {
        try {
            return executarConsulta(sqlNative -> {
                List<ProdutoDTO> produtos = new ArrayList<>(1000);
                Timestamp periodoFinAjustado = ajustarPeriodoFinal(periodoFin);
                boolean isKelco = isCnpjKelco(cnpjIndustria);
                sqlNative.appendSql("SELECT DISTINCT PRO.CODPROD AS CODIGO_PRODUTO_INTERNO, LPAD(TO_CHAR(PRO.CODPROD), 20, '0') AS CODIGO_ITEM, PRO.REFERENCIA AS CODIGO_BARRAS_EAN, " +
                    "PRO.DESCRPROD AS DESCRICAO, NVL(PRO.CODVOL, PRO.UNIDADE) AS UNIDADE_MEDIDA, " +
                    "NVL(PRO.QTDEMB, 1) AS QUANTIDADE_EMBALAGEM, 0 AS PRECO_TABELA, " +
                    "CASE WHEN PRO.ATIVO = 'S' THEN '01' ELSE '02' END AS STATUS_PRODUTO, " +
                    "CASE WHEN PRO.USOPROD = 'S' THEN '02' ELSE '01' END AS TIPO_ITEM, " +
                    "REPLACE(REPLACE(REPLACE(REPLACE(PAR.CGC_CPF, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ_INDUSTRIA " +
                    "FROM TGFPRO PRO " +
                    "INNER JOIN (SELECT DISTINCT ITE.CODPROD, CAB.CODPARC " +
                    "FROM TGFITE ITE " +
                    "INNER JOIN TGFCAB CAB ON ITE.NUNOTA = CAB.NUNOTA AND CAB.TIPMOV = 'C' AND CAB.STATUSNOTA = 'L'");
                if (periodoIni != null) sqlNative.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
                if (periodoFinAjustado != null) sqlNative.appendSql(" AND CAB.DTNEG <= :PERIODO_FIN");
                sqlNative.appendSql(") ITE_COMPRA ON PRO.CODPROD = ITE_COMPRA.CODPROD " +
                    "INNER JOIN TGFPAR PAR ON ITE_COMPRA.CODPARC = PAR.CODPARC " +
                    "WHERE PAR.ATIVO = 'S' AND PAR.AD_INTEGRANEOGRID = 'S'");
                if (isKelco) {
                    sqlNative.appendSql(" AND PAR.CGC_CPF IN ('89064885000104', '13809963000344', '08811119000822')");
                } else if (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty()) {
                    sqlNative.appendSql(" AND PAR.CGC_CPF = :CNPJ_INDUSTRIA");
                }
                sqlNative.appendSql(" AND PRO.ATIVO = 'S' " +
                    "UNION " +
                    "SELECT DISTINCT PRO.CODPROD AS CODIGO_PRODUTO_INTERNO, LPAD(TO_CHAR(PRO.CODPROD), 20, '0') AS CODIGO_ITEM, PRO.REFERENCIA AS CODIGO_BARRAS_EAN, " +
                    "PRO.DESCRPROD AS DESCRICAO, NVL(PRO.CODVOL, PRO.UNIDADE) AS UNIDADE_MEDIDA, " +
                    "NVL(PRO.QTDEMB, 1) AS QUANTIDADE_EMBALAGEM, 0 AS PRECO_TABELA, " +
                    "CASE WHEN PRO.ATIVO = 'S' THEN '01' ELSE '02' END AS STATUS_PRODUTO, " +
                    "CASE WHEN PRO.USOPROD = 'S' THEN '02' ELSE '01' END AS TIPO_ITEM, " +
                    "REPLACE(REPLACE(REPLACE(REPLACE(PAR.CGC_CPF, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ_INDUSTRIA " +
                    "FROM TGFPRO PRO " +
                    "INNER JOIN (SELECT DISTINCT ITE.CODPROD, CAB.CODPARC " +
                    "FROM TGFITE ITE " +
                    "INNER JOIN TGFCAB CAB ON ITE.NUNOTA = CAB.NUNOTA AND CAB.TIPMOV = 'V' AND CAB.STATUSNOTA = 'L'");
                if (periodoIni != null) sqlNative.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
                if (periodoFinAjustado != null) sqlNative.appendSql(" AND CAB.DTNEG <= :PERIODO_FIN");
                sqlNative.appendSql(") ITE_VENDA ON PRO.CODPROD = ITE_VENDA.CODPROD " +
                    "INNER JOIN (SELECT DISTINCT ITE_COMPRA.CODPROD, CAB_COMPRA.CODPARC " +
                    "FROM TGFITE ITE_COMPRA " +
                    "INNER JOIN TGFCAB CAB_COMPRA ON ITE_COMPRA.NUNOTA = CAB_COMPRA.NUNOTA AND CAB_COMPRA.TIPMOV = 'C' AND CAB_COMPRA.STATUSNOTA = 'L' " +
                    "INNER JOIN TGFPAR PAR_FORN ON CAB_COMPRA.CODPARC = PAR_FORN.CODPARC " +
                    "WHERE PAR_FORN.ATIVO = 'S' AND PAR_FORN.AD_INTEGRANEOGRID = 'S'");
                if (isKelco) {
                    sqlNative.appendSql(" AND PAR_FORN.CGC_CPF IN ('89064885000104', '13809963000344', '08811119000822')");
                } else if (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty()) {
                    sqlNative.appendSql(" AND PAR_FORN.CGC_CPF = :CNPJ_INDUSTRIA");
                }
                sqlNative.appendSql(") ITE_COMPRA_DIST ON ITE_VENDA.CODPROD = ITE_COMPRA_DIST.CODPROD " +
                    "INNER JOIN TGFPAR PAR ON ITE_COMPRA_DIST.CODPARC = PAR.CODPARC " +
                    "WHERE PAR.ATIVO = 'S' AND PAR.AD_INTEGRANEOGRID = 'S'");
                if (isKelco) {
                    sqlNative.appendSql(" AND PAR.CGC_CPF IN ('89064885000104', '13809963000344', '08811119000822')");
                } else if (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty()) {
                    sqlNative.appendSql(" AND PAR.CGC_CPF = :CNPJ_INDUSTRIA");
                }
                sqlNative.appendSql(" AND PRO.ATIVO = 'S' " +
                    "ORDER BY CNPJ_INDUSTRIA, CODIGO_PRODUTO_INTERNO");
                if (!isKelco && cnpjIndustria != null && !cnpjIndustria.trim().isEmpty()) {
                    sqlNative.setNamedParameter("CNPJ_INDUSTRIA", cnpjIndustria);
                }
                sqlNative.setNamedParameter("PERIODO_INI", periodoIni);
                sqlNative.setNamedParameter("PERIODO_FIN", periodoFinAjustado);
                ResultSet rs = sqlNative.executeQuery();
                while (rs.next()) {
                    ProdutoDTO dto = new ProdutoDTO();
                    String cnpjInd = rs.getString("CNPJ_INDUSTRIA");
                    dto.setCnpjIndustria((cnpjInd == null || cnpjInd.trim().isEmpty()) && cnpjIndustria != null ?
                        NeogridFormatter.formatarCnpjCpf(cnpjIndustria) : cnpjInd);
                    dto.setCodigoItem(rs.getString("CODIGO_ITEM"));
                    String codigoBarras = rs.getString("CODIGO_BARRAS_EAN");
                    if (codigoBarras == null || codigoBarras.trim().isEmpty()) {
                        codigoBarras = StringUtils.getNullAsEmpty(rs.getBigDecimal("CODIGO_PRODUTO_INTERNO"));
                    }
                    dto.setCodigoProduto(codigoBarras);
                    dto.setTipoItem(rs.getString("TIPO_ITEM"));
                    dto.setQuantidadeEmbalagem(rs.getBigDecimal("QUANTIDADE_EMBALAGEM") != null ? rs.getBigDecimal("QUANTIDADE_EMBALAGEM") : BigDecimal.ONE);
                    dto.setPrecoTabelaUnidade(rs.getBigDecimal("PRECO_TABELA") != null ? rs.getBigDecimal("PRECO_TABELA") : BigDecimal.ZERO);
                    dto.setDescricaoInterna(rs.getString("DESCRICAO"));
                    dto.setStatusProduto(rs.getString("STATUS_PRODUTO"));
                    produtos.add(dto);
                }
                rs.close();
                return produtos;
            });
        } catch (Exception e) {
            throw new NeogridRepositoryException("Erro ao buscar produtos: " + ExceptionUtils.getMessage(e), e);
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
