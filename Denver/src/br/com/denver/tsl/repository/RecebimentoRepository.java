package br.com.denver.tsl.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import br.com.denver.tsl.model.dto.RecebimentoDTO;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

public class RecebimentoRepository extends AbstractTSLRepository {

    private static final String SQL_BASE =
        "SELECT REPLACE(REPLACE(REPLACE(REPLACE(EMP.CGC, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ, " +
        "CAB.NUMNOTA AS NOTAFISCAL, ITE.SEQUENCIA AS ITEMNOTA, " +
        "LPAD(TO_CHAR(PRO.CODPROD), 13, '0') AS CODIGOPRODUTO, " +
        "NVL(PRO.PESOLIQ, 0) AS PESOCAIXA, " +
        "ITE.AD_DATAPRODUCAO AS DATAPRODUCAO, ITE.AD_DATAVALIDADE AS DATAVENCIMENTO, " +
        "ITE.CONTROLE AS LOTE, PRO.COMPLDESC AS INFOCOMPLEMENTAR, NVL(ITE.VLRUNIT, 0) AS VALORUNITARIO, " +
        "PRO.AD_NUMEROPALETE AS NUMEROPALETE, NVL(PRO.AD_IDENTIFICADORCAIXA, ITE.AD_CODBARRAS) AS IDENTIFICADORCAIXA " +
        "FROM TGFCAB CAB " +
        "INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA " +
        "INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD AND PRO.ATIVO = 'S' AND PRO.AD_INTEGTOTALLOGISTICA = 'S' " +
        "INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP " +
        "WHERE CAB.TIPMOV = 'C' AND CAB.STATUSNOTA = 'L'";

    public Set<RecebimentoDTO> buscarRecebimentosPorLote(String lote) throws Exception {
        if (StringUtils.isEmpty(lote)) throw new IllegalArgumentException("LOTE não pode ser nulo ou vazio.");

        Set<RecebimentoDTO> conjunto = new LinkedHashSet<>(1024);
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        ResultSet rs = null;

        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            sqlNative.appendSql(SQL_BASE).appendSql(" AND ITE.CONTROLE = :LOTE");
            sqlNative.setNamedParameter("LOTE", lote);
            rs = sqlNative.executeQuery();
            while (rs.next()) {
                RecebimentoDTO dto = new RecebimentoDTO();
                dto.setCnpj(rs.getString("CNPJ"));
                dto.setNotaFiscal(rs.getString("NOTAFISCAL"));
                dto.setItemNotaFiscal(rs.getString("ITEMNOTA"));
                dto.setNumeroPalete(rs.getString("NUMEROPALETE"));
                dto.setCodigoProduto(rs.getString("CODIGOPRODUTO"));
                dto.setIdentificadorCaixa(rs.getString("IDENTIFICADORCAIXA"));
                dto.setPesoCaixa(BigDecimalUtil.getValueOrZero(rs.getBigDecimal("PESOCAIXA")).toString());

                java.util.Date dataProducao = toDate(rs.getTimestamp("DATAPRODUCAO"));
                java.util.Date dataVencimento = toDate(rs.getTimestamp("DATAVENCIMENTO"));

                if (dataProducao == null) {
                    throw new IllegalStateException(
                        String.format("Data de Produção é obrigatória mas não foi informada. " +
                            "Nota Fiscal: %s, Item: %s, Produto: %s, Lote: %s. " +
                            "Verifique se o campo ITE.AD_DATAPRODUCAO está preenchido.",
                            rs.getString("NOTAFISCAL"), rs.getString("ITEMNOTA"),
                            rs.getString("CODIGOPRODUTO"), rs.getString("LOTE"))
                    );
                }
                if (dataVencimento == null) {
                    throw new IllegalStateException(
                        String.format("Data de Vencimento é obrigatória mas não foi informada. " +
                            "Nota Fiscal: %s, Item: %s, Produto: %s, Lote: %s. " +
                            "Verifique se o campo ITE.AD_DATAVALIDADE está preenchido.",
                            rs.getString("NOTAFISCAL"), rs.getString("ITEMNOTA"),
                            rs.getString("CODIGOPRODUTO"), rs.getString("LOTE"))
                    );
                }

                dto.setDataProducao(dataProducao);
                dto.setDataVencimento(dataVencimento);
                dto.setLote(rs.getString("LOTE"));
                dto.setInfoComplementar(rs.getString("INFOCOMPLEMENTAR"));
                dto.setValorUnitario(BigDecimalUtil.getValueOrZero(rs.getBigDecimal("VALORUNITARIO")).toString());
                conjunto.add(dto);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (sqlNative != null) try { NativeSql.releaseResources(sqlNative); } catch (Exception ignored) {}
            if (jdbc != null) try { JdbcWrapper.closeSession(jdbc); } catch (Exception ignored) {}
        }
        return conjunto;
    }

    public Set<String> buscarLotesPorNunota(BigDecimal nunota) throws Exception {
        if (nunota == null) throw new IllegalArgumentException("NUNOTA não pode ser nulo.");
        Set<String> lotes = new HashSet<>();
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        ResultSet rs = null;
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            sqlNative.appendSql("SELECT DISTINCT ITE.CONTROLE FROM TGFITE ITE WHERE ITE.NUNOTA = :NUNOTA");
            sqlNative.setNamedParameter("NUNOTA", nunota);
            rs = sqlNative.executeQuery();
            while (rs.next()) {
                String controle = rs.getString("CONTROLE");
                if (StringUtils.isNotEmpty(controle)) lotes.add(controle);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (sqlNative != null) try { NativeSql.releaseResources(sqlNative); } catch (Exception ignored) {}
            if (jdbc != null) try { JdbcWrapper.closeSession(jdbc); } catch (Exception ignored) {}
        }
        return lotes;
    }
}
