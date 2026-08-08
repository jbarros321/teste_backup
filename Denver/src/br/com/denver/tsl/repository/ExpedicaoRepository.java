package br.com.denver.tsl.repository;

import br.com.denver.tsl.model.dto.ExpedicaoDTO;
import com.sankhya.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ExpedicaoRepository extends AbstractTSLRepository {

    private static final String SUBQUERY_COMPRA =
        "(SELECT ITE_COMPRA.AD_DATAPRODUCAO FROM TGFITE ITE_COMPRA " +
        "INNER JOIN TGFCAB CAB_COMPRA ON CAB_COMPRA.NUNOTA = ITE_COMPRA.NUNOTA " +
        "WHERE CAB_COMPRA.TIPMOV = 'C' AND CAB_COMPRA.STATUSNOTA = 'L' AND CAB_COMPRA.CODEMP = CAB.CODEMP " +
        "AND ITE_COMPRA.CODPROD = ITE.CODPROD AND ((ITE_COMPRA.CONTROLE IS NULL AND ITE.CONTROLE IS NULL) OR ITE_COMPRA.CONTROLE = ITE.CONTROLE) " +
        "AND ROWNUM = 1)";

    private static final String SQL_BASE =
        "SELECT DISTINCT REPLACE(REPLACE(REPLACE(REPLACE(EMP.CGC, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ, " +
        "NVL(CAB.ORDEMCARGA, CAB.NUMNOTA) AS ORDEMFRETE, CAB.NUMNOTA AS NUMEROPEDIDO, ITE.SEQUENCIA AS ITEMPEDIDO, " +
        "LPAD(TO_CHAR(PRO.CODPROD), 13, '0') AS CODIGOPRODUTO, " +
        "ABS(ITE.QTDNEG) AS QUANTIDADE, NVL(PRO.PESOLIQ, 0) * ABS(ITE.QTDNEG) AS PESO, " +
        "NVL(ITE.AD_DATAPRODUCAO, " + SUBQUERY_COMPRA + ") AS DATAFABRICACAODE, " +
        "NVL(ITE.AD_DATAPRODUCAO, " + SUBQUERY_COMPRA + ") AS DATAFABRICACAOATE, " +
        "ITE.CONTROLE AS LOTE, REPLACE(REPLACE(REPLACE(REPLACE(NVL(PAR.CGC_CPF, ''), '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJCLIENTE, " +
        "PRO.AD_NUMEROPALETE AS NUMEROPALETE FROM TGFCAB CAB " +
        "INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA " +
        "INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD AND PRO.ATIVO = 'S' AND PRO.AD_INTEGTOTALLOGISTICA = 'S' " +
        "INNER JOIN TGFPAR PAR ON PAR.CODPARC = CAB.CODPARC INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP " +
        "WHERE CAB.TIPMOV = 'V' AND CAB.STATUSNOTA = 'L'";

    public Set<ExpedicaoDTO> buscarExpedicoesPorNunota(BigDecimal nunota) throws Exception {
        return executarQuery(SQL_BASE, nunota, rs -> {
            ExpedicaoDTO dto = new ExpedicaoDTO();
            dto.setCnpj(rs.getString("CNPJ"));
            dto.setOrdemFrete(rs.getString("ORDEMFRETE"));
            dto.setNumeroPedido(rs.getString("NUMEROPEDIDO"));
            dto.setItemPedido(rs.getString("ITEMPEDIDO"));
            dto.setCodigoProduto(rs.getString("CODIGOPRODUTO"));
            dto.setNumeroPalete(rs.getString("NUMEROPALETE"));
            dto.setQuantidade(BigDecimalUtil.getValueOrZero(rs.getBigDecimal("QUANTIDADE")).toString());
            dto.setPeso(BigDecimalUtil.getValueOrZero(rs.getBigDecimal("PESO")).toString());

            java.util.Date dataFabricacaoDe = toDate(rs.getTimestamp("DATAFABRICACAODE"));
            java.util.Date dataFabricacaoAte = toDate(rs.getTimestamp("DATAFABRICACAOATE"));

            if (dataFabricacaoDe == null) {
                throw new IllegalStateException(
                    String.format("Data de Fabricação (DE) é obrigatória mas não foi informada. " +
                        "Pedido: %s, Item: %s, Produto: %s, Lote: %s. " +
                        "Verifique se o campo ITE.AD_DATAPRODUCAO ou dados da nota de compra estão preenchidos.",
                        rs.getString("NUMEROPEDIDO"), rs.getString("ITEMPEDIDO"),
                        rs.getString("CODIGOPRODUTO"), rs.getString("LOTE"))
                );
            }
            if (dataFabricacaoAte == null) {
                throw new IllegalStateException(
                    String.format("Data de Fabricação (ATÉ) é obrigatória mas não foi informada. " +
                        "Pedido: %s, Item: %s, Produto: %s, Lote: %s. " +
                        "Verifique se o campo ITE.AD_DATAPRODUCAO ou dados da nota de compra estão preenchidos.",
                        rs.getString("NUMEROPEDIDO"), rs.getString("ITEMPEDIDO"),
                        rs.getString("CODIGOPRODUTO"), rs.getString("LOTE"))
                );
            }

            dto.setDataFabricacaoDe(dataFabricacaoDe);
            dto.setDataFabricacaoAte(dataFabricacaoAte);
            dto.setLote(rs.getString("LOTE"));
            dto.setCnpjCliente(rs.getString("CNPJCLIENTE"));
            return dto;
        });
    }
}
