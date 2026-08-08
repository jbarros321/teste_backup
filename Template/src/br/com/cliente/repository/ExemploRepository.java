package br.com.cliente.repository;

import br.com.cliente.model.dto.ExemploDTO;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Set;

public class ExemploRepository extends AbstractRepository {
    private static final String SQL_BASE =
        "SELECT REPLACE(REPLACE(REPLACE(REPLACE(EMP.CGC, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ, " +
        "NVL(PRO.REFERENCIA, LPAD(TO_CHAR(PRO.CODPROD), 13, '0')) AS CODIGOPRODUTO, " +
        "NVL(PRO.DESCRPROD, '') AS DESCRICAO, CAB.DTNEG AS DATAEMISSAO " +
        "FROM TGFCAB CAB " +
        "INNER JOIN TGFITE ITE ON ITE.NUNOTA = CAB.NUNOTA " +
        "INNER JOIN TGFPRO PRO ON PRO.CODPROD = ITE.CODPROD AND PRO.ATIVO = 'S' " +
        "INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP AND EMP.ATIVO = 'S' " +
        "WHERE CAB.STATUSNOTA = 'L'";

    public Set<ExemploDTO> buscarDadosPorNunota(BigDecimal nunota) throws Exception { return executarQuery(SQL_BASE, nunota, this::mapearDTO); }

    private ExemploDTO mapearDTO(ResultSet rs) throws Exception {
        ExemploDTO dto = new ExemploDTO();
        dto.setCnpj(rs.getString("CNPJ"));
        dto.setCodigoProduto(rs.getString("CODIGOPRODUTO"));
        dto.setDescricao(rs.getString("DESCRICAO"));
        dto.setDataEmissao(toDate(rs.getTimestamp("DATAEMISSAO")));
        return dto; }
}
