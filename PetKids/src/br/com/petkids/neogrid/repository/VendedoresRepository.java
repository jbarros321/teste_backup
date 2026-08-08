package br.com.petkids.neogrid.repository;

import br.com.petkids.neogrid.exception.NeogridRepositoryException;
import br.com.petkids.neogrid.model.dto.VendedorDTO;
import com.sankhya.util.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class VendedoresRepository extends AbstractNeogridRepository {
    public List<VendedorDTO> buscarVendedores(Timestamp periodoIni, Timestamp periodoFin) throws NeogridRepositoryException {
        try {
            return executarConsulta(sqlNative -> {
                List<VendedorDTO> vendedores = new ArrayList<>(50);
                Timestamp periodoFinAjustado = ajustarPeriodoFinal(periodoFin);
                sqlNative.appendSql("SELECT DISTINCT VEN.APELIDO AS NOME_VENDEDOR, VEN.CODVEND AS CODIGO_VENDEDOR, " +
                    "NULL AS NOME_SUPERVISOR, NULL AS CODIGO_SUPERVISOR, GER.APELIDO AS NOME_GERENTE, " +
                    "VEN.CODGER AS CODIGO_GERENTE, CASE WHEN VEN.ATIVO = 'S' THEN 'A' ELSE 'I' END AS STATUS, " +
                    "NULL AS DATA_DESLIGAMENTO FROM TGFVEN VEN " +
                    "INNER JOIN TGFCAB CAB ON VEN.CODVEND = CAB.CODVEND AND CAB.TIPMOV = 'V'");
                if (periodoIni != null) sqlNative.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
                if (periodoFinAjustado != null) sqlNative.appendSql(" AND CAB.DTNEG <= :PERIODO_FIN");
                sqlNative.appendSql(" LEFT JOIN TGFVEN GER ON VEN.CODGER = GER.CODVEND WHERE VEN.ATIVO = 'S' ORDER BY VEN.CODVEND");
                sqlNative.setNamedParameter("PERIODO_INI", periodoIni);
                sqlNative.setNamedParameter("PERIODO_FIN", periodoFinAjustado);
                ResultSet rs = sqlNative.executeQuery();
                while (rs.next()) {
                    VendedorDTO dto = new VendedorDTO();
                    dto.setNomeVendedor(rs.getString("NOME_VENDEDOR"));
                    dto.setCodigoVendedor(StringUtils.getNullAsEmpty(rs.getBigDecimal("CODIGO_VENDEDOR")));
                    dto.setNomeSupervisor(rs.getString("NOME_SUPERVISOR"));
                    dto.setCodigoSupervisor(StringUtils.getNullAsEmpty(rs.getBigDecimal("CODIGO_SUPERVISOR")));
                    dto.setNomeGerente(rs.getString("NOME_GERENTE"));
                    dto.setCodigoGerente(StringUtils.getNullAsEmpty(rs.getBigDecimal("CODIGO_GERENTE")));
                    String statusStr = rs.getString("STATUS");
                    if (statusStr != null) dto.setStatus(br.com.petkids.neogrid.model.enums.StatusVendedor.fromString(statusStr));
                    dto.setDataDesligamento(rs.getTimestamp("DATA_DESLIGAMENTO"));
                    vendedores.add(dto);
                }
                rs.close();
                return vendedores;
            });
        } catch (Exception e) {
            throw new NeogridRepositoryException("Erro ao buscar vendedores: " + ExceptionUtils.getMessage(e), e);
        }
    }
}
