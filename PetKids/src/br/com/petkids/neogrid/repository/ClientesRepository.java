package br.com.petkids.neogrid.repository;

import br.com.petkids.neogrid.exception.NeogridRepositoryException;
import br.com.petkids.neogrid.model.dto.ClienteDTO;
import com.sankhya.util.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ClientesRepository extends AbstractNeogridRepository {
    public List<ClienteDTO> buscarClientes(Timestamp periodoIni, Timestamp periodoFin) throws NeogridRepositoryException {
        try {
            return executarConsulta(sqlNative -> {
                List<ClienteDTO> clientes = new ArrayList<>(500);
                Timestamp periodoFinAjustado = ajustarPeriodoFinal(periodoFin);
                sqlNative.appendSql("SELECT DISTINCT PAR.CODPARC AS CODIGO_CLIENTE, PAR.CEP, PAR.TIPPESSOA AS TIPO_PESSOA, " +
                    "REPLACE(REPLACE(REPLACE(REPLACE(NVL(PAR.CGC_CPF, ''), '.', ''), '/', ''), '-', ''), ' ', '') AS CGC_CPF, " +
                    "CASE WHEN UFS.UF IS NULL OR TRIM(UFS.UF) = '' THEN 'DF' ELSE TRIM(UFS.UF) END AS UF, CID.NOMECID AS CIDADE, " +
                    "CASE WHEN PAR.TIPPESSOA = 'F' THEN 'Pessoa Fisica' ELSE NVL(EN.NOMEEND, '') END AS ENDERECO, " +
                    "NVL(BAI.NOMEBAI, '') AS BAIRRO, " +
                    "CASE WHEN PAR.TIPPESSOA = 'F' THEN 'Pessoa Fisica' ELSE NVL(PAR.RAZAOSOCIAL, PAR.NOMEPARC) END AS NOME_CLIENTE, " +
                    "'169' AS CODIGO_SEGMENTO, '04' AS FREQUENCIA_VISITA, " +
                    "CASE WHEN PAR.TIPPESSOA = 'F' THEN 'Pessoa Fisica' WHEN NVL(TRIM(PAR.TELEFONE), '') = '' THEN '00000000000' ELSE REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(NVL(TRIM(PAR.TELEFONE), ''), ' ', ''), '(', ''), ')', ''), '-', ''), '.', '') END AS TELEFONE, " +
                    "CASE WHEN PAR.TIPPESSOA = 'F' THEN 'Pessoa Fisica' ELSE NVL(PAR.NOMEPARC, '') END AS CONTATO " +
                    "FROM TGFPAR PAR " +
                    "INNER JOIN TGFCAB CAB ON PAR.CODPARC = CAB.CODPARC AND CAB.TIPMOV = 'V'");
                if (periodoIni != null) sqlNative.appendSql(" AND CAB.DTNEG >= :PERIODO_INI");
                if (periodoFinAjustado != null) sqlNative.appendSql(" AND CAB.DTNEG <= :PERIODO_FIN");
                sqlNative.appendSql(" LEFT JOIN TSIEND EN ON PAR.CODEND = EN.CODEND " +
                    "LEFT JOIN TSICID CID ON PAR.CODCID = CID.CODCID " +
                    "LEFT JOIN TSIUFS UFS ON CID.UF = UFS.CODUF " +
                    "LEFT JOIN TSIBAI BAI ON PAR.CODBAI = BAI.CODBAI " +
                    "WHERE PAR.CLIENTE = 'S' AND PAR.ATIVO = 'S' ORDER BY PAR.CODPARC");
                sqlNative.setNamedParameter("PERIODO_INI", periodoIni);
                sqlNative.setNamedParameter("PERIODO_FIN", periodoFinAjustado);
                ResultSet rs = sqlNative.executeQuery();
                while (rs.next()) {
                    ClienteDTO dto = new ClienteDTO();
                    dto.setCodigoCliente(StringUtils.getNullAsEmpty(rs.getBigDecimal("CODIGO_CLIENTE")));
                    dto.setTipoPessoa(rs.getString("TIPO_PESSOA"));
                    dto.setCgcCpf(rs.getString("CGC_CPF"));
                    dto.setCep(rs.getString("CEP"));
                    String uf = rs.getString("UF");
                    dto.setUf(uf != null && !uf.trim().isEmpty() ? uf.trim() : "DF");
                    dto.setCidade(rs.getString("CIDADE"));
                    dto.setEndereco(rs.getString("ENDERECO"));
                    dto.setBairro(rs.getString("BAIRRO"));
                    dto.setNomeCliente(rs.getString("NOME_CLIENTE"));
                    dto.setCodigoSegmento(rs.getString("CODIGO_SEGMENTO"));
                    dto.setFrequenciaVisita(rs.getString("FREQUENCIA_VISITA"));
                    dto.setTelefone(rs.getString("TELEFONE"));
                    dto.setContato(rs.getString("CONTATO"));
                    clientes.add(dto);
                }
                rs.close();
                return clientes;
            });
        } catch (Exception e) {
            throw new NeogridRepositoryException("Erro ao buscar clientes: " + ExceptionUtils.getMessage(e), e);
        }
    }
}
