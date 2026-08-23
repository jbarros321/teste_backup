package br.com.cicrano.dash.repository;

import br.com.cicrano.dash.dto.FluxoCaixaDTO;
import br.com.cicrano.dash.dto.ProvisaoDTO;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FinanceiroRepository {

    private static final String SQL_FLUXO_CAIXA_REAL = 
        "SELECT " +
        "    TRUNC(FIN.DHBAIXA) AS DATA, " +
        "    SUM(CASE WHEN FIN.RECDESP = 1 THEN FIN.VLRDESDOB ELSE 0 END) AS RECEITAS, " +
        "    SUM(CASE WHEN FIN.RECDESP = -1 THEN FIN.VLRDESDOB ELSE 0 END) AS DESPESAS " +
        "FROM TGFFIN FIN " +
        "WHERE FIN.DHBAIXA IS NOT NULL " +
        "    AND FIN.PROVISAO = 'N' " +
        "    AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99) " +
        "    AND TRUNC(FIN.DHBAIXA) BETWEEN :DATA_INI AND :DATA_FIM " +
        "GROUP BY TRUNC(FIN.DHBAIXA) " +
        "ORDER BY TRUNC(FIN.DHBAIXA)";

    private static final String SQL_PROVISAO_RECEITA = 
        "SELECT " +
        "    TRUNC(FIN.DTVENC) AS DATA, " +
        "    SUM(FIN.VLRDESDOB) AS VALOR " +
        "FROM TGFFIN FIN " +
        "WHERE FIN.RECDESP = 1 " +
        "    AND FIN.PROVISAO = 'S' " +
        "    AND FIN.DHBAIXA IS NULL " +
        "    AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99) " +
        "    AND TRUNC(FIN.DTVENC) BETWEEN :DATA_INI AND :DATA_FIM " +
        "GROUP BY TRUNC(FIN.DTVENC) " +
        "ORDER BY TRUNC(FIN.DTVENC)";

    private static final String SQL_PROVISAO_DESPESA = 
        "SELECT " +
        "    TRUNC(FIN.DTVENC) AS DATA, " +
        "    SUM(FIN.VLRDESDOB) AS VALOR " +
        "FROM TGFFIN FIN " +
        "WHERE FIN.RECDESP = -1 " +
        "    AND FIN.PROVISAO = 'S' " +
        "    AND FIN.DHBAIXA IS NULL " +
        "    AND FIN.CODTIPTIT NOT IN (0, 18, 27, 99) " +
        "    AND TRUNC(FIN.DTVENC) BETWEEN :DATA_INI AND :DATA_FIM " +
        "GROUP BY TRUNC(FIN.DTVENC) " +
        "ORDER BY TRUNC(FIN.DTVENC)";

    public List<FluxoCaixaDTO> buscarFluxoCaixaReal(Date dataIni, Date dataFim) {
        JdbcWrapper jdbc = null;
        NativeSql nativeSql = null;
        List<FluxoCaixaDTO> resultado = new ArrayList<>(100);
        
        try {
            jdbc = br.com.sankhya.modelcore.util.EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            nativeSql = new NativeSql(jdbc);
            nativeSql.appendSql(SQL_FLUXO_CAIXA_REAL);
            nativeSql.setNamedParameter("DATA_INI", dataIni);
            nativeSql.setNamedParameter("DATA_FIM", dataFim);
            
            ResultSet rs = nativeSql.executeQuery();
            while (rs.next()) {
                Date data = rs.getDate("DATA");
                BigDecimal receitas = rs.getBigDecimal("RECEITAS");
                BigDecimal despesas = rs.getBigDecimal("DESPESAS");
                
                resultado.add(new FluxoCaixaDTO(data, receitas, despesas));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar fluxo de caixa real", e);
        } finally {
            NativeSql.releaseResources(nativeSql);
            JdbcWrapper.closeSession(jdbc);
        }
        
        return resultado;
    }

    public List<ProvisaoDTO> buscarProvisaoReceita(Date dataIni, Date dataFim) {
        JdbcWrapper jdbc = null;
        NativeSql nativeSql = null;
        List<ProvisaoDTO> resultado = new ArrayList<>(100);
        
        try {
            jdbc = br.com.sankhya.modelcore.util.EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            nativeSql = new NativeSql(jdbc);
            nativeSql.appendSql(SQL_PROVISAO_RECEITA);
            nativeSql.setNamedParameter("DATA_INI", dataIni);
            nativeSql.setNamedParameter("DATA_FIM", dataFim);
            
            ResultSet rs = nativeSql.executeQuery();
            while (rs.next()) {
                Date data = rs.getDate("DATA");
                BigDecimal valor = rs.getBigDecimal("VALOR");
                
                resultado.add(new ProvisaoDTO(data, valor));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar provisão de receita", e);
        } finally {
            NativeSql.releaseResources(nativeSql);
            JdbcWrapper.closeSession(jdbc);
        }
        
        return resultado;
    }

    public List<ProvisaoDTO> buscarProvisaoDespesa(Date dataIni, Date dataFim) {
        JdbcWrapper jdbc = null;
        NativeSql nativeSql = null;
        List<ProvisaoDTO> resultado = new ArrayList<>(100);
        
        try {
            jdbc = br.com.sankhya.modelcore.util.EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            nativeSql = new NativeSql(jdbc);
            nativeSql.appendSql(SQL_PROVISAO_DESPESA);
            nativeSql.setNamedParameter("DATA_INI", dataIni);
            nativeSql.setNamedParameter("DATA_FIM", dataFim);
            
            ResultSet rs = nativeSql.executeQuery();
            while (rs.next()) {
                Date data = rs.getDate("DATA");
                BigDecimal valor = rs.getBigDecimal("VALOR");
                
                resultado.add(new ProvisaoDTO(data, valor));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar provisão de despesa", e);
        } finally {
            NativeSql.releaseResources(nativeSql);
            JdbcWrapper.closeSession(jdbc);
        }
        
        return resultado;
    }
}

