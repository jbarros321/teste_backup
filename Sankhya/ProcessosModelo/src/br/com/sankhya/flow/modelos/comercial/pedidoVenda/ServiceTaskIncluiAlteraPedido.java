package br.com.sankhya.flow.modelos.comercial.pedidoVenda;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jdom.Element;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoTarefa;
import br.com.sankhya.extensions.flow.TarefaJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.vo.ValueObjectManager;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper.ItemNotaCallBack;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.ws.ServiceContext;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import com.sankhya.util.XMLUtils;

public class ServiceTaskIncluiAlteraPedido implements TarefaJava {

	private EntityFacade	dwfEntityFacade;

	public void executar(ContextoTarefa contexto) throws Exception {

		dwfEntityFacade = EntityFacadeFactory.getDWFFacade();

		Registro[] formCab = contexto.getLinhasFormulario("AD_PEDIDOVENDA");

		if (formCab.length < 1) {

			throw new IllegalStateException("No existe registro no formulrio principal (AD_TGFCAB).");

		} else {

			Registro solicitacao = formCab[0];
			Registro[] itensSolicitados = contexto.getLinhasFormulario("AD_ITEMPEDIDO");

			if (itensSolicitados.length < 1) {
				throw new Exception("No existem produtos para adcionar");
			}

			BigDecimal nuNota = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("NUNOTA"));
			boolean alteracao = nuNota != null;

			if(alteracao){
				NativeSql nativeSql = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());
				nativeSql.setNamedParameter("NUNOTA", contexto.getCampo("NUNOTA"));
				nativeSql.executeUpdate("DELETE FROM TSILIB WHERE NUCHAVE = :NUNOTA AND TABELA IN ('TGFCAB', 'TGFITE')");

				for(Registro p:contexto.getLinhasFormulario("AD_LIBERACOESVENDA")){
					p.remove();
				}
			}

			for(Registro p:contexto.getLinhasFormulario("AD_PENDENCIASVENDA")){
				p.remove();
			}

			BigDecimal centroResultado = BigDecimalUtil.getBigDecimal(contexto.getCampo("CODCENCUS"));
			BigDecimal natureza = BigDecimalUtil.getBigDecimal(contexto.getCampo("CODNAT"));
			BigDecimal TOP = BigDecimalUtil.getBigDecimal(contexto.getCampo("CODTOP"));

			BigDecimal empresa = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("CODEMP"));
			BigDecimal parceiro = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("CODPARC"));
			BigDecimal tipoNegociacao = BigDecimalUtil.getBigDecimal(solicitacao.getCampo("CODTIPVENDA"));

			ServiceContext sctx = ServiceContext.getCurrent();
			CACHelper cacHelper = new CACHelper();
			SPBeanUtils.setupContext(sctx);

			Element cabecalho = new Element("Cabecalho");
			XMLUtils.addContentElement(cabecalho, "NUNOTA", nuNota);
			XMLUtils.addContentElement(cabecalho, "NUMNOTA", BigDecimal.ZERO);
			XMLUtils.addContentElement(cabecalho, "STATUSNOTA", "A");
			XMLUtils.addContentElement(cabecalho, "DTNEG", TimeUtils.formataDDMMYYYY(TimeUtils.getNow()));

			JdbcWrapper jdbc = dwfEntityFacade.getJdbcWrapper();
			try {
				carregaTop(jdbc, TOP, cabecalho);
				carregaTpv(jdbc, tipoNegociacao, cabecalho);
			} finally {
				if (jdbc != null) {
					jdbc.closeSession();
				}
			}

			XMLUtils.addContentElement(cabecalho, "CODPARC", parceiro);
			XMLUtils.addContentElement(cabecalho, "CODNAT", natureza);
			XMLUtils.addContentElement(cabecalho, "CODCENCUS", centroResultado);
			XMLUtils.addContentElement(cabecalho, "TIPMOV", "P");
			XMLUtils.addContentElement(cabecalho, "CODEMP", empresa);

			try {
				BarramentoRegra regra = cacHelper.incluirAlterarCabecalho(sctx, cabecalho);

				Collection<EntityPrimaryKey> pksEnvolvidas = regra.getDadosBarramento().getPksEnvolvidas();
				EntityPrimaryKey cabKey = (EntityPrimaryKey) pksEnvolvidas.iterator().next();

				nuNota = new BigDecimal(cabKey.getValues()[0].toString());
				solicitacao.setCampo("NUNOTA", nuNota);
				solicitacao.save();

				contexto.setCampo("NUNOTA", String.valueOf(nuNota.longValue()));

				Map<BigDecimal, PersistentLocalEntity> itensRemover = new HashMap<BigDecimal, PersistentLocalEntity>();
				if (alteracao) {
					for (PersistentLocalEntity ple : (Collection<PersistentLocalEntity>) dwfEntityFacade.findByDynamicFinder(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", new Object[] { nuNota }))) {
						DynamicVO itemVO = (DynamicVO) ple.getValueObject();
						BigDecimal sequencia = itemVO.asBigDecimal("SEQUENCIA");
						itensRemover.put(sequencia, ple);
					}
				}

				PersistentLocalEntity ple = null;

				cacHelper.setItemNotaCallBack(new ItemNotaCallBack() {
					public void afterSave(PrePersistEntityState itemState) throws Exception {
						Registro itemRegistro = (Registro) itemState.getProperty("ITEM_INFO");
						DynamicVO vo = itemState.getNewVO();
						itemRegistro.setCampo("SEQUENCIAITE", vo.asBigDecimal("SEQUENCIA"));
						itemRegistro.save();
					}
				});

				for (Registro i : itensSolicitados) {
					ple = null;
					BigDecimal sequencia = BigDecimalUtil.getBigDecimal(i.getCampo("SEQUENCIAITE"));
					if (sequencia != null) {
						ple = itensRemover.remove(sequencia);
					}
					PrePersistEntityState pse = buildPrePersistItens(ple, sequencia, nuNota, i);
					pse.addProperty("ITEM_INFO", i);

					Collection<PrePersistEntityState> itens = new ArrayList<PrePersistEntityState>();
					itens.add(pse);

					try {
						cacHelper.incluirAlterarItem(nuNota, AuthenticationInfo.getCurrent(), itens, true);
					} catch (Exception e) {
						StringBuffer buf = new StringBuffer();
						buf.append("Erro ao tentar inserir produto: ").append(i.getCampo("CODPROD"));
						buf.append(" Quantidade: ").append(i.getCampo("QUANTIDADE"));
						buf.append(" Unidade: ").append(i.getCampo("CODVOL"));
						registraErro(contexto, e, "I", buf.toString());
					}
				}

				for (PersistentLocalEntity itemObsoleto : itensRemover.values()) {
					itemObsoleto.remove();
				}
			} catch (Exception e) {
				registraErro(contexto, e, "C", "Erro ao tentar incluir o pedido: ");
			}

		}
	}

	private void registraErro(ContextoTarefa contexto, Exception erro, String tipo, String detalhes) throws Exception {
		Registro pendencia = contexto.novaLinha("AD_PENDENCIASVENDA");
		pendencia.setCampo("IDINSTPRN", contexto.getIdInstanceProcesso());
		pendencia.setCampo("IDINSTTAR", BigDecimalUtil.ZERO_VALUE);
		pendencia.setCampo("TIPO", tipo);
		pendencia.setCampo("DETALHESERRO", detalhes + "\n\n" + StringUtils.getNullAsEmpty(erro.getMessage()));
		pendencia.save();
	}

	private void carregaTop(JdbcWrapper jdbc, BigDecimal top, Element cabecalho) throws Exception {
		XMLUtils.addContentElement(cabecalho, "CODTIPOPER", top);
		Timestamp dhalter = NativeSql.getTimestamp("MAX(DHALTER)", "TGFTOP", "CODTIPOPER = ?", new Object[] { top });
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		XMLUtils.addContentElement(cabecalho, "DHTIPOPER", sdf.format(dhalter));
	}

	private void carregaTpv(JdbcWrapper jdbc, BigDecimal tpv, Element cabecalho) throws Exception {
		XMLUtils.addContentElement(cabecalho, "CODTIPVENDA", tpv);
		Timestamp dhalter = NativeSql.getTimestamp("MAX(DHALTER)", "TGFTPV", "CODTIPVENDA = ?", new Object[] { tpv });
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		XMLUtils.addContentElement(cabecalho, "DHTIPVENDA", sdf.format(dhalter));
	}

	private PrePersistEntityState buildPrePersistItens(PersistentLocalEntity ple, BigDecimal sequencia, BigDecimal novaNota, Registro itemSolicitacao) throws Exception {
		BigDecimal produto = BigDecimalUtil.getBigDecimal(itemSolicitacao.getCampo("CODPROD"));
		BigDecimal qtdNeg = BigDecimalUtil.getBigDecimal(itemSolicitacao.getCampo("QUANTIDADE"));
		BigDecimal vlrUnit = BigDecimalUtil.getBigDecimal(itemSolicitacao.getCampo("VLRUNIT"));

		Element itemElem = new Element("item");
		if (sequencia != null) {
			XMLUtils.addContentElement(itemElem, "SEQUENCIA", sequencia);
		}
		XMLUtils.addContentElement(itemElem, "NUNOTA", novaNota);
		XMLUtils.addContentElement(itemElem, "CODPROD", produto);
		XMLUtils.addContentElement(itemElem, "CODVOL", itemSolicitacao.getCampo("CODVOL"));
		XMLUtils.addContentElement(itemElem, "QTDNEG", qtdNeg);
		if(vlrUnit != null){
			XMLUtils.addContentElement(itemElem, "VLRUNIT", vlrUnit);
		}

		EntityDAO dao = dwfEntityFacade.getDAOInstance("ItemNota");
		DynamicVO newVO = null;
		DynamicVO oldVO = null;
		if (ple == null) {
			newVO = (DynamicVO) dao.getDefaultValueObjectInstance();
		} else {
			newVO = (DynamicVO) ple.getValueObject();
			oldVO = newVO.buildClone();
		}

		ValueObjectManager.getSingleton().updateValueObject(newVO, itemElem, dao);

		PrePersistEntityState pse;
		if (ple == null) {
			pse = PrePersistEntityState.build(dwfEntityFacade, dao.getEntityName(), newVO);
		} else {
			pse = PrePersistEntityState.build(dwfEntityFacade, dao.getEntityName(), newVO, oldVO, ple);
		}
		pse.addProperty("source", itemElem);

		return pse;
	}

}
