package br.com.ermaq.EventoProgramavelJava;

import java.math.BigDecimal;

import com.sankhya.util.StringUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;


//TGFITE
public class ControlaEstoque implements EventoProgramavelJava{

	@Override
	public void afterDelete(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterUpdate(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCommit(TransactionContext tranCtx) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
	//	validarTipoRetalhoCompra(event);
		marcaControlaEstoque(event);
		
	}

	

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		//validarTipoRetalhoCompra(event);
		marcaControlaEstoque(event);
		
	}
	
	@SuppressWarnings("unused")
	private void validarTipoRetalhoCompra(PersistenceEvent event) throws Exception {
		System.out.println("validarTipoRetalhoe");
		DynamicVO vo = (DynamicVO) event.getVo();
		BigDecimal nuNota = vo.asBigDecimalOrZero("NUNOTA");
		
		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		DynamicVO cabVO = cabDAO.findByPK(nuNota);
		String tipMov = cabVO.asString("TIPMOV");
		
		if (tipMov.equals("C")){
			
			JapeWrapper proDAO = JapeFactory.dao(DynamicEntityNames.PRODUTO);
			DynamicVO prodVO = proDAO.findByPK(vo.asBigDecimal("CODPROD"));
			
			String controlaRetalho = prodVO.asString("AD_CONTROLARETALHO");
			String tipRet = prodVO.asString("AD_TIPRET");
			
			if(StringUtils.isNotEmpty(controlaRetalho) && controlaRetalho.equals("S")) {
				
				if(StringUtils.isEmpty(tipRet)) {
					throw new MGEModelException("Produto está marcado como Controla Retalho e o Tipo de Retalho não foi informado. Favor verificar!");
				}
				
				switch (tipRet) {
				case "CH":
					if(vo.asBigDecimalOrZero("AD_BO20000X1200").intValue() > 0 || vo.asBigDecimalOrZero("AD_BO20000X1400").intValue() > 0 || vo.asBigDecimalOrZero("AD_BORETALHO").intValue() > 0 || vo.asBigDecimalOrZero("AD_BR6000").intValue() > 0 || vo.asBigDecimalOrZero("AD_BRRETALHO").intValue() > 0) {
						throw new MGEModelException("Produto do tipo CHAPA . Somente os campos CH podem ser preenchidos.\n\n <b>Ação Cancelada!</b>");
					}
					if(vo.asBigDecimalOrZero("AD_CH2000X1200").intValue() == 0 && vo.asBigDecimalOrZero("AD_CH2000X1500").intValue() == 0 && vo.asBigDecimalOrZero("AD_CH3000X1200").intValue() == 0 && vo.asBigDecimalOrZero("AD_CH3000X1500").intValue() == 0 || vo.asBigDecimalOrZero("AD_CHRETALHO").intValue() == 0) {
						throw new MGEModelException("Produto do tipo CHAPA . Campo CH deve ser preenchidos.\n\n <b>Ação Cancelada!</b>");
					}
					
					
					
					break;
				case "BR":
					if(vo.asBigDecimalOrZero("AD_BO20000X1200").intValue() > 0 || vo.asBigDecimalOrZero("AD_BO20000X1400").intValue() > 0 || vo.asBigDecimalOrZero("AD_BORETALHO").intValue() > 0 || vo.asBigDecimalOrZero("AD_CH2000X1200").intValue() > 0 || vo.asBigDecimalOrZero("AD_CH2000X1500").intValue() > 0 || vo.asBigDecimalOrZero("AD_CH3000X1200").intValue() > 0 || vo.asBigDecimalOrZero("AD_CH3000X1500").intValue() > 0 || vo.asBigDecimalOrZero("AD_CHRETALHO").intValue() > 0) {
						throw new MGEModelException("Produto do tipo BARRA . Somente os campos BR podem ser preenchidos.\n\n <b>Ação Cancelada!</b>");
					}
					if(vo.asBigDecimalOrZero("AD_BR6000").intValue() == 0 && vo.asBigDecimalOrZero("AD_BRRETALHO").intValue() == 0) {
						throw new MGEModelException("Produto do tipo BARRA . Campo BR deve ser preenchidos.\n\n <b>Ação Cancelada!</b>");
					}
					
					break;
					
				case "BO":
					
					if(vo.asBigDecimalOrZero("AD_BR6000").intValue() > 0 || vo.asBigDecimalOrZero("AD_BRRETALHO").intValue() > 0 || vo.asBigDecimalOrZero("AD_CH2000X1200").intValue() > 0 || vo.asBigDecimalOrZero("AD_CH2000X1500").intValue() > 0 || vo.asBigDecimalOrZero("AD_CH3000X1200").intValue() > 0 || vo.asBigDecimalOrZero("AD_CH3000X1500").intValue() > 0 || vo.asBigDecimalOrZero("AD_CHRETALHO").intValue() > 0) {
						throw new MGEModelException("Produto do tipo BORRACHA . Somente os campos BO podem ser preenchidos.\n\n <b>Ação Cancelada!</b>");
					}
					if(vo.asBigDecimalOrZero("AD_BO20000X1200").intValue() == 0 && vo.asBigDecimalOrZero("AD_BO20000X1400").intValue() == 0 && vo.asBigDecimalOrZero("AD_BORETALHO").intValue() == 0 ) {
						throw new MGEModelException("Produto do tipo BORRACHA . Campo BO deve ser preenchidos.\n\n <b>Ação Cancelada!</b>");
					}
	
					break;

				}

			}
			
			
		}
		

		
		
	}

	private void marcaControlaEstoque(PersistenceEvent event) throws Exception {
		System.out.println("marcaControlaEstoque");
		DynamicVO vo = (DynamicVO) event.getVo();
		BigDecimal nuNota = vo.asBigDecimalOrZero("NUNOTA");
		
		JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
		DynamicVO cabVO = cabDAO.findByPK(nuNota);		
		
		
		String tipMov = cabVO.asString("TIPMOV");
		String controlaEstoque = cabVO.asString("AD_CONTROLAESTOQUE");

//		JdbcWrapper jdbc = null;
//		NativeSql sql = null;
//		ResultSet rs = null;
//		SessionHandle hnd = null;

		if (tipMov.equals("C") && StringUtils.isEmpty(controlaEstoque)) {
			
			JapeWrapper prodDAO = JapeFactory.dao(DynamicEntityNames.PRODUTO);
			DynamicVO prodVO = prodDAO.findByPK(vo.asBigDecimal("CODPROD"));		
			
			
			
			if(StringUtils.isNotEmpty(prodVO.asString("AD_CONTROLARETALHO")) && prodVO.asString("AD_CONTROLARETALHO").equals("S")) {
				
				if(vo.asString("CONTROLE").contains("Inteiro")) {
					
					if(vo.asBigDecimalOrZero("AD_BO20000X1200").intValue() > 0 || vo.asBigDecimalOrZero("AD_BO20000X1400").intValue() > 0 ||vo.asBigDecimalOrZero("AD_BORETALHO").intValue() > 0 ||vo.asBigDecimalOrZero("AD_BR6000").intValue() > 0 ||vo.asBigDecimalOrZero("AD_BRRETALHO").intValue() > 0 ||vo.asBigDecimalOrZero("AD_CH2000X1200").intValue() > 0 ||vo.asBigDecimalOrZero("AD_CH2000X1500").intValue() > 0 ||vo.asBigDecimalOrZero("AD_CH3000X1200").intValue() > 0 ||vo.asBigDecimalOrZero("AD_CH3000X1500").intValue() > 0 ||vo.asBigDecimalOrZero("AD_CHRETALHO").intValue() > 0) {
						
						cabDAO.prepareToUpdate(cabVO)
						.set("AD_CONTROLAESTOQUE", "S")
						.set("AD_NUREQBAIXA", null)
						.set("AD_NUREQENTRADA", null)
						.update();
					}
						
				}
			
			}
			

//			try {
//
//				hnd = JapeSession.open();
//				EntityFacade entity = EntityFacadeFactory.getDWFFacade();
//				jdbc = entity.getJdbcWrapper();
//				jdbc.openSession();
//
//				sql = new NativeSql(jdbc);
//				sql.appendSql("SELECT ITE.CODPROD ");
//				sql.appendSql("FROM TGFITE ITE ");
//				sql.appendSql("INNER JOIN TGFPRO PRO ");
//				sql.appendSql("ON ITE.CODPROD = PRO.CODPROD ");
//				sql.appendSql("WHERE PRO.AD_CONTROLARETALHO = 'S' ");
//				sql.appendSql("AND ITE.CONTROLE LIKE '%Inteiro' ");
//				sql.appendSql("AND (AD_BO20000X1200 IS NOT NULL OR AD_BO20000X1400 IS NOT NULL OR AD_BORETALHO IS NOT NULL OR AD_BR6000 IS NOT NULL OR AD_BRRETALHO IS NOT NULL OR AD_CH2000X1200 IS NOT NULL OR AD_CH2000X1500 IS NOT NULL OR AD_CH3000X1200 IS NOT NULL OR AD_CH3000X1500 IS NOT NULL OR AD_CHRETALHO IS NOT NULL) ");
//				sql.appendSql("AND  ITE.NUNOTA = :NUNOTA ");
//				sql.setNamedParameter("NUNOTA", nuNota);
//
//				rs = sql.executeQuery();
//				if (rs.next()) {
//					
//					cabDAO.prepareToUpdate(cabVO)
//					.set("AD_CONTROLAESTOQUE", "S")
//					.set("AD_NUREQBAIXA", null)
//					.set("AD_NUREQENTRADA", null)
//					.update();
//					
//				}
//
//			} catch (Exception e) {
//				MGEModelException.throwMe(e);
//			} finally {
//				JdbcUtils.closeResultSet(rs);
//				NativeSql.releaseResources(sql);
//				JdbcWrapper.closeSession(jdbc);
//				JapeSession.close(hnd);
//
//			}

		} else if ((tipMov.equals("V") || tipMov.equals("F")) && StringUtils.isEmpty(controlaEstoque)) {
			
			
			JapeWrapper prodDAO = JapeFactory.dao(DynamicEntityNames.PRODUTO);
			DynamicVO prodVO = prodDAO.findByPK(vo.asBigDecimal("CODPROD"));	
			
			if(StringUtils.isNotEmpty(prodVO.asString("AD_CONTROLARETALHO")) && prodVO.asString("AD_CONTROLARETALHO").equals("S")) {
				
				if(vo.asString("CONTROLE").contains("Inteiro")) {
					
					JapeWrapper voaDAO = JapeFactory.dao(DynamicEntityNames.VOLUME_ALTERNATIVO);
					
					DynamicVO voaVo = voaDAO.findOne("CODPROD = ? AND CODVOL = ? AND CONTROLE = ?",
							vo.asBigDecimal("CODPROD"), "UN", vo.asString("CONTROLE"));
					
					if(voaVo!=null) {
						
						BigDecimal medidaPadrao = voaVo.asBigDecimal("QUANTIDADE");
						BigDecimal qtdneg = vo.asBigDecimal("QTDNEG");
						
						BigDecimal consumo = qtdneg.remainder(medidaPadrao);
						
						if(consumo.intValue() != 0) {
							
							cabDAO.prepareToUpdate(cabVO)
							.set("AD_CONTROLAESTOQUE", "S")
							.set("AD_NUREQBAIXA", null)
							.set("AD_NUREQENTRADA", null)
							.update();
							
						}
						
						
						
					}
					
					
					
					
				}
				
				
				
			}
			

//			try {
//
//				hnd = JapeSession.open();
//				EntityFacade entity = EntityFacadeFactory.getDWFFacade();
//				jdbc = entity.getJdbcWrapper();
//				jdbc.openSession();
//
//				sql = new NativeSql(jdbc);
//				sql.appendSql("SELECT ITE.CODPROD ");
//				sql.appendSql("FROM TGFITE ITE ");
//				sql.appendSql("INNER JOIN TGFPRO PRO ");
//				sql.appendSql("ON ITE.CODPROD = PRO.CODPROD ");
//				sql.appendSql("INNER JOIN TGFVOA VOA ");
//				sql.appendSql("ON VOA.CODPROD = ITE.CODPROD AND VOA.CODVOL = 'UN' AND ITE.CONTROLE = VOA.CONTROLE ");
//				sql.appendSql("WHERE PRO.AD_CONTROLARETALHO = 'S' ");
//				sql.appendSql("AND ITE.CONTROLE LIKE '%Inteiro' ");
//				sql.appendSql("AND MOD(ITE.QTDNEG,VOA.QUANTIDADE) <> 0 ");
//				sql.appendSql("AND  ITE.NUNOTA = :NUNOTA ");
//				sql.setNamedParameter("NUNOTA", nuNota);
//
//				rs = sql.executeQuery();
//				if (rs.next()) {
//					cabDAO.prepareToUpdate(cabVO)
//					.set("AD_CONTROLAESTOQUE", "S")
//					.set("AD_NUREQBAIXA", null)
//					.set("AD_NUREQENTRADA", null)
//					.update();
//				}
//
//			} catch (Exception e) {
//				MGEModelException.throwMe(e);
//			} finally {
//				JdbcUtils.closeResultSet(rs);
//				NativeSql.releaseResources(sql);
//				JdbcWrapper.closeSession(jdbc);
//				JapeSession.close(hnd);
//
//			}
		}

	}

}
