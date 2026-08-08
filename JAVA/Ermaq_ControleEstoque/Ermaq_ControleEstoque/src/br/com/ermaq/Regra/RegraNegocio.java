package br.com.ermaq.Regra;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

public class RegraNegocio implements Regra {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void afterDelete(ContextoRegra ctx) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterInsert(ContextoRegra ctx) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterUpdate(ContextoRegra ctx) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeDelete(ContextoRegra ctx) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeInsert(ContextoRegra ctx) throws Exception {
		validarUnidadesAlternativas(ctx);

	}

	@Override
	public void beforeUpdate(ContextoRegra ctx) throws Exception {
		validarUnidadesAlternativas(ctx);

	}

	private void validarUnidadesAlternativas(ContextoRegra ctx) throws Exception {
		PrePersistEntityState state = ctx.getPrePersistEntityState();
		DynamicVO vo = state.getNewVO();

		boolean isCabecalho = vo.getValueObjectID().contains(DynamicEntityNames.CABECALHO_NOTA);

		boolean confirmando = JapeSession.getProperty("CabecalhoNota.confirmando.nota") != null;
		
		if (isCabecalho) {

			String tipMov = vo.asString("TIPMOV");

			if (confirmando && (tipMov.equals("V") || tipMov.equals("C") || tipMov.equals("F"))) {

				BigDecimal nuNota = vo.asBigDecimalOrZero("NUNOTA");

				JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
				Collection<DynamicVO> iteVOs = iteDAO.find("NUNOTA = ?", nuNota);

				for (DynamicVO iteVo : iteVOs) {

					JapeWrapper prodDAO = JapeFactory.dao(DynamicEntityNames.PRODUTO);
					JapeWrapper voaDAO = JapeFactory.dao(DynamicEntityNames.VOLUME_ALTERNATIVO);
					DynamicVO prodVo = prodDAO.findByPK(iteVo.asBigDecimal("CODPROD"));
					String controlaRetalho = prodVo.asString("AD_CONTROLARETALHO");
					String tipRet = prodVo.asString("AD_TIPRET");

					if (StringUtils.isNotEmpty(controlaRetalho) && controlaRetalho.equals("S")) {

						DynamicVO voaVo = null;

						switch (tipRet) {
						case "CH":

							ArrayList<String> ch = new ArrayList<String>();
							ch.add("2000x1200Inteiro");
							ch.add("2000X1500Inteiro");
							ch.add("3000x1200Inteiro");
							ch.add("3000x1500Inteiro");

							for (String c : ch) {

								voaVo = voaDAO.findOne("CODPROD = ? AND CODVOL = ? AND CONTROLE = ?",
										iteVo.asBigDecimal("CODPROD"), "UN", c);
								if (voaVo == null) {
									throw new MGEModelException("Unidade Alternativa não cadastrada para o produto "
											+ iteVo.asBigDecimal("CODPROD").intValue() + ",Unidade UN, Controle " + c
											+ ". \n\n <b>Ação Cancelada!</b>");
								}

							}
//							voaVo = voaDAO.findOne("CODPROD = ? AND CODVOL = ?", iteVo.asBigDecimal("CODPROD"), "KG");
//							if (voaVo == null) {
//								throw new MGEModelException("Unidade Alternativa não cadastrada para o produto "
//										+ iteVo.asBigDecimal("CODPROD").intValue()
//										+ ",Unidade KG. \n\n <b>Ação Cancelada!</b>");
//							}

							break;
						case "BR":

							ArrayList<String> br = new ArrayList<String>();
							br.add("6000Inteiro");

							for (String b : br) {

								voaVo = voaDAO.findOne("CODPROD = ? AND CODVOL = ? AND CONTROLE = ?",
										iteVo.asBigDecimal("CODPROD"), "UN", b);
								if (voaVo == null) {
									throw new MGEModelException("Unidade Alternativa não cadastrada para o produto "
											+ iteVo.asBigDecimal("CODPROD").intValue() + ",Unidade UN, Controle " + b
											+ ". \n\n <b>Ação Cancelada!</b>");
								}

							}
//							voaVo = voaDAO.findOne("CODPROD = ? AND CODVOL = ?", iteVo.asBigDecimal("CODPROD"), "KG");
//							if (voaVo == null) {
//								throw new MGEModelException("Unidade Alternativa não cadastrada para o produto "
//										+ iteVo.asBigDecimal("CODPROD").intValue()
//										+ ",Unidade KG. \n\n <b>Ação Cancelada!</b>");
//							}

							break;

						case "BO":

							ArrayList<String> bo = new ArrayList<String>();
							bo.add("20000x1200Inteiro");
							bo.add("20000x1400Inteiro");

							for (String b : bo) {

								voaVo = voaDAO.findOne("CODPROD = ? AND CODVOL = ? AND CONTROLE = ?",
										iteVo.asBigDecimal("CODPROD"), "UN", b);
								if (voaVo == null) {
									throw new MGEModelException("Unidade Alternativa não cadastrada para o produto "
											+ iteVo.asBigDecimal("CODPROD").intValue() + ",Unidade UN, Controle " + b
											+ ". \n\n <b>Ação Cancelada!</b>");
								}

							}
//							voaVo = voaDAO.findOne("CODPROD = ? AND CODVOL = ?", iteVo.asBigDecimal("CODPROD"), "KG");
//							if (voaVo == null) {
//								throw new MGEModelException("Unidade Alternativa não cadastrada para o produto "
//										+ iteVo.asBigDecimal("CODPROD").intValue()
//										+ ",Unidade KG. \n\n <b>Ação Cancelada!</b>");
//							}

							break;

						}

					}

				}

			}

		}

	}

}
