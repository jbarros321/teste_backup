package br.com.sankhya.extensions.rateio.holding.facades;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.JdbcUtils;
import com.sankhya.util.StringUtils;

public class DescontoVagHelper {
	private EntityFacade	dwfEntityFacade;
	private JdbcWrapper		jdbc;
	private BigDecimal		codParceiro;
	private BigDecimal		codUnidade;
	private String			contratos;

	public DescontoVagHelper(EntityFacade dwfEntityFacade, JdbcWrapper jdbc) {
		this.dwfEntityFacade = dwfEntityFacade;
	}

	public void fechamentoDescontoVag() throws Exception {
		jdbc = dwfEntityFacade.getJdbcWrapper();

		NativeSql sql = new NativeSql(jdbc, this.getClass(), "queBuscaFinVagAtrasadas.sql");

		if (BigDecimalUtil.getValueOrZero(codParceiro).intValue() > 0 &&
				BigDecimalUtil.getValueOrZero(codUnidade).intValue() > 0) {
			sql.appendSql(" AND CODPARC = ? AND CODCENCUS = ?");
			sql.addParameter(codParceiro);
			sql.addParameter(codUnidade);
		} else if (BigDecimalUtil.getValueOrZero(codParceiro).intValue() > 0) {
			sql.appendSql(" AND CODPARC = ?");
			sql.addParameter(codParceiro);
		} else if (BigDecimalUtil.getValueOrZero(codUnidade).intValue() > 0) {
			sql.appendSql(" AND CODCENCUS = ?");
			sql.addParameter(codUnidade);
		}

		if(!"".equals(StringUtils.getNullAsEmpty(contratos))){
			String contratosIn = " AND NUMCONTRATO IN (" + contratos + ")";
			sql.appendSql(contratosIn);
		}

		ResultSet rset = sql.executeQuery();

		while (rset.next()) {
			try{
				DynamicVO descontoRepasseVagVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("DescontoRepasseVisita");

				setDescontoRepasseVag(descontoRepasseVagVO, rset);

				dwfEntityFacade.createEntity("DescontoRepasseVisita", (EntityVO) descontoRepasseVagVO);
			}catch(Exception e){
				System.out.println(e);
			}
		}

		JdbcUtils.closeResultSet(rset);
	}

	private void setDescontoRepasseVag(DynamicVO descontoRepasseVagVO, ResultSet rset) throws SQLException {
		descontoRepasseVagVO.setProperty("NUMCONTRATO", rset.getBigDecimal("NUMCONTRATO"));
		descontoRepasseVagVO.setProperty("NUFINORIG", rset.getBigDecimal("NUFINORIGEM"));
		descontoRepasseVagVO.setProperty("PERCDESCREPASSE", rset.getBigDecimal("PERCDESCONTO"));
		descontoRepasseVagVO.setProperty("VLRDESCREPASSE", rset.getBigDecimal("VLRDESCONTO"));
		descontoRepasseVagVO.setProperty("REFERENCIA", rset.getTimestamp("REFERENCIA"));
		descontoRepasseVagVO.setProperty("DIASATRASO", rset.getBigDecimal("DIASEMATRASO"));
		descontoRepasseVagVO.setProperty("CODPARC", rset.getBigDecimal("CODPARC"));
		descontoRepasseVagVO.setProperty("CODCENCUS", rset.getBigDecimal("CODCENCUS"));
	}

	public void setCodParceiro(BigDecimal codParceiro) {
		this.codParceiro = codParceiro;
	}

	public void setCodUnidade(BigDecimal codUnidade) {
		this.codUnidade = codUnidade;
	}

	public void setContratos(String contratos) {
		this.contratos = contratos;
	}
}
