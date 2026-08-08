package br.com.sankhya.botaoacao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralItemNota;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.nfe.ServicosNFeHelper2;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.UsuarioVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class AutomatizacaoProcessosHelper {
	private JapeSession.SessionHandle hnd;

	private EntityFacade dwfFacade;

	private AuthenticationInfo auth;

	private Collection<BigDecimal> numeroNotas = new ArrayList<>();

	public AutomatizacaoProcessosHelper(JapeSession.SessionHandle hnd) throws Exception {
		this.hnd = hnd;
		this.dwfFacade = EntityFacadeFactory.getDWFFacade();
		this.auth = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, Integer.valueOf(0));
		this.auth.makeCurrent();
		setupContext();
	}

	public Boolean existeNotas() {
		if (this.numeroNotas.isEmpty())
			return Boolean.valueOf(false);
		return Boolean.valueOf(true);
	}

	private void setupContext() throws Exception {
		UsuarioVO usuVO = (UsuarioVO) ((DynamicVO) this.dwfFacade.findEntityByPrimaryKeyAsVO("Usuario",
				new Object[] { this.auth.getUserID() })).wrapInterface(UsuarioVO.class);
		JapeSessionContext.putProperty("usuario_logado", this.auth.getUserID());
		JapeSessionContext.putProperty("emp_usu_logado", usuVO.getCODEMP());
		JapeSessionContext.putProperty("dh_atual", new Timestamp(System.currentTimeMillis()));
		JapeSessionContext.putProperty("d_atual", new Timestamp(TimeUtils.getToday()));
		JapeSessionContext.putProperty("usuarioVO", usuVO);
		JapeSessionContext.putProperty("authInfo", this.auth);
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
		JapeSession.putProperty("ItemNota.incluindo.alterando.pela.central", Boolean.TRUE);
		JapeSession.putProperty("jape.session.no.query.timeout", Boolean.FALSE);
	}

	public void notasPendentes() throws Exception {
		JdbcWrapper jdbc = null;
		NativeSql sqlVerificaNotas = null;
		try {
			jdbc = this.dwfFacade.getJdbcWrapper();
			jdbc.openSession();
			sqlVerificaNotas = new NativeSql(jdbc);
			sqlVerificaNotas.appendSql(" SELECT NUNOTA ");
			sqlVerificaNotas.appendSql("   FROM TGFCAB ");
			sqlVerificaNotas.appendSql("  WHERE CODTIPOPER = 622 ");
			sqlVerificaNotas.appendSql("    AND DTENTSAI >= TRUNC(SYSDATE-7) ");
			sqlVerificaNotas.appendSql("    AND STATUSNFE IS NULL ");
			sqlVerificaNotas.appendSql("    AND OBSERVACAO = 'Transferencia Gerada Automática'");
			ResultSet rsVerificaNotas = sqlVerificaNotas.executeQuery();
			while (rsVerificaNotas.next())
				this.numeroNotas.add(rsVerificaNotas.getBigDecimal("NUNOTA"));
		} finally {
			NativeSql.releaseResources(sqlVerificaNotas);
			JdbcWrapper.closeSession(jdbc);
		}
	}

	@SuppressWarnings("unused")
	private void recalculaNota(BigDecimal nunota) throws Exception {
		DynamicVO notaVO = (DynamicVO) this.dwfFacade.findEntityByPrimaryKeyAsVO("CabecalhoNota",
				new Object[] { nunota });
		notaVO.setProperty("CODPARC", BigDecimalUtil.getBigDecimal(Integer.valueOf(14541)));
		PrePersistEntityState cabState = PrePersistEntityState.build(this.dwfFacade, "CabecalhoNota", notaVO);
		CACHelper cacHelper = new CACHelper();
		cacHelper.incluirAlterarCabecalho(this.auth, cabState);
	}

	private List<DynamicVO> verificaEstoque() throws Exception {
		List<DynamicVO> itens = new ArrayList<>();
		JdbcWrapper jdbc = null;
		NativeSql sqlVerificaEstoque = null;
		try {
			jdbc = this.dwfFacade.getJdbcWrapper();
			jdbc.openSession();
			sqlVerificaEstoque = new NativeSql(jdbc);
			sqlVerificaEstoque.appendSql(" SELECT EST.CODPROD ");
			sqlVerificaEstoque.appendSql("      , PRO.USOPROD ");
			sqlVerificaEstoque.appendSql("      , PRO.CODVOL ");
			sqlVerificaEstoque.appendSql("      , EST.CODLOCAL AS CODLOCALORIG ");
			sqlVerificaEstoque.appendSql("      , EST.CONTROLE ");
			sqlVerificaEstoque.appendSql("      , EST.ESTOQUE * (-1) AS QTDNEG ");
			sqlVerificaEstoque.appendSql("      , (SELECT E.ESTOQUE ");
			sqlVerificaEstoque.appendSql("           FROM TGFEST E ");
			sqlVerificaEstoque.appendSql("          WHERE E.CODPROD = EST.CODPROD ");
			sqlVerificaEstoque.appendSql("            AND E.CODLOCAL = EST.CODLOCAL ");
			sqlVerificaEstoque.appendSql("            AND E.CONTROLE = EST.CONTROLE ");
			sqlVerificaEstoque.appendSql("            AND E.CODPARC = EST.CODPARC ");
			sqlVerificaEstoque.appendSql("            AND E.TIPO = EST.TIPO ");
			sqlVerificaEstoque.appendSql("            AND E.CODEMP = 6 ) AS ESTCD ");
			sqlVerificaEstoque.appendSql("  FROM TGFEST EST ");
			sqlVerificaEstoque.appendSql("     , TGFPRO PRO ");
			sqlVerificaEstoque.appendSql(" WHERE EST.CODEMP = 1 ");
			sqlVerificaEstoque.appendSql("   AND EST.ESTOQUE < 0 ");
			sqlVerificaEstoque.appendSql("   AND EST.CODLOCAL = 1 ");
			sqlVerificaEstoque.appendSql("   AND PRO.CODPROD = EST.CODPROD ");
			sqlVerificaEstoque.appendSql("   AND PRO.USOPROD = 'R' ");
			sqlVerificaEstoque.appendSql("   AND (SELECT SUM(E.ESTOQUE) ");
			sqlVerificaEstoque.appendSql("          FROM TGFEST E ");
			sqlVerificaEstoque.appendSql("         WHERE E.CODPROD = EST.CODPROD ");
			sqlVerificaEstoque.appendSql("           AND E.CODEMP = 6) >= EST.ESTOQUE * (-1) ");
			sqlVerificaEstoque.appendSql("  AND ((PRO.TIPCONTEST = 'S' AND EST.CONTROLE <> ' ') ");
			sqlVerificaEstoque.appendSql("   OR (PRO.TIPCONTEST = 'N' AND EST.CONTROLE  = ' ')) ");
			ResultSet rsVerificaEstoque = sqlVerificaEstoque.executeQuery();
			while (rsVerificaEstoque.next()) {
				BigDecimal estcd = rsVerificaEstoque.getBigDecimal("ESTCD");
				BigDecimal qtdneg = rsVerificaEstoque.getBigDecimal("QTDNEG");
				if (estcd.compareTo(qtdneg) >= 0) {
					DynamicVO itemVO = (DynamicVO) this.dwfFacade.getDefaultValueObjectInstance("ItemNota");
					itemVO.setProperty("CODPROD", rsVerificaEstoque.getBigDecimal("CODPROD"));
					itemVO.setProperty("USOPROD", rsVerificaEstoque.getString("USOPROD"));
					itemVO.setProperty("CODVOL", rsVerificaEstoque.getString("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", rsVerificaEstoque.getBigDecimal("CODLOCALORIG"));
					itemVO.setProperty("CONTROLE", rsVerificaEstoque.getString("CONTROLE"));
					itemVO.setProperty("QTDNEG", qtdneg);
					itens.add(itemVO);
				}
			}
		} finally {
			NativeSql.releaseResources(sqlVerificaEstoque);
			JdbcWrapper.closeSession(jdbc);
		}
		return itens;
	}

	private PrePersistEntityState createCabecalhoPrePersistEntityState() throws Exception {
		DynamicVO newCabVO = (DynamicVO) this.dwfFacade.getDefaultValueObjectInstance("CabecalhoNota");
		newCabVO.setProperty("CODEMP", BigDecimalUtil.getBigDecimal(Integer.valueOf(6)));
		newCabVO.setProperty("CODEMPNEGOC", BigDecimalUtil.getBigDecimal(Integer.valueOf(1)));
		newCabVO.setProperty("CODPARC", BigDecimalUtil.getBigDecimal(Integer.valueOf(14541)));
		newCabVO.setProperty("CODTIPOPER", BigDecimalUtil.getBigDecimal(Integer.valueOf(622)));
		newCabVO.setProperty("CODTIPVENDA", BigDecimal.ZERO);
		newCabVO.setProperty("TIPMOV", new String("T"));
		newCabVO.setProperty("CODCENCUS", BigDecimal.ZERO);
		newCabVO.setProperty("NUMNOTA", BigDecimal.ZERO);
		newCabVO.setProperty("SERIENOTA", new String("1"));
		newCabVO.setProperty("DTNEG", TimeUtils.getNow());
		newCabVO.setProperty("DTFATUR", TimeUtils.getNow());
		newCabVO.setProperty("DTMOV", TimeUtils.getNow());
		newCabVO.setProperty("DTENTSAI", TimeUtils.getNow());
		newCabVO.setProperty("HRENTSAI", TimeUtils.getNow());
		newCabVO.setProperty("DTALTER", TimeUtils.getNow());
		newCabVO.setProperty("CIF_FOB", new String("S"));
		newCabVO.setProperty("CODVEND", BigDecimalUtil.getBigDecimal(Integer.valueOf(20)));
		newCabVO.setProperty("OBSERVACAO", new String("Transferencia Gerada Automatica"));
		PrePersistEntityState cabState = PrePersistEntityState.build(this.dwfFacade, "CabecalhoNota", newCabVO);
		return cabState;
	}

	private List<PrePersistEntityState> createItensPrePersistEntityState(DynamicVO notaVO, List<DynamicVO> itens)
			throws Exception {
		List<PrePersistEntityState> itensState = new ArrayList<>();
		for (DynamicVO item : itens) {
			DynamicVO itemVO = (DynamicVO) this.dwfFacade.getDefaultValueObjectInstance("ItemNota");
			itemVO.setProperty("NUNOTA", notaVO.getProperty("NUNOTA"));
			itemVO.setProperty("CODEMP", notaVO.getProperty("CODEMP"));
			itemVO.setProperty("CODPROD", item.getProperty("CODPROD"));
			itemVO.setProperty("USOPROD", item.getProperty("USOPROD"));
			itemVO.setProperty("CODVOL", item.getProperty("CODVOL"));
			itemVO.setProperty("CONTROLE", item.getProperty("CONTROLE"));
			itemVO.setProperty("CONTROLEDEST", item.getProperty("CONTROLE"));
			itemVO.setProperty("QTDNEG", item.getProperty("QTDNEG"));
			itemVO.setProperty("PENDENTE", new String("N"));
			itemVO.setProperty("CODLOCALORIG", item.getProperty("CODLOCALORIG"));
			itemVO.setProperty("CODLOCALDEST", item.getProperty("CODLOCALORIG"));
			itemVO.setProperty("PERCDESC", BigDecimal.ZERO);
			itemVO.setProperty("VLRDESC", BigDecimal.ZERO);
			inicializaProduto(itemVO);
			PrePersistEntityState itemState = PrePersistEntityState.build(this.dwfFacade, "ItemNota", itemVO);
			itensState.add(itemState);
		}
		return itensState;
	}

	private void inicializaProduto(DynamicVO itemVO) throws Exception {
		CentralItemNota centralItemNota = new CentralItemNota();
		CentralItemNota.ParamsInicializacaoProduto params = new CentralItemNota.ParamsInicializacaoProduto();
		params.codProd = itemVO.asBigDecimal("CODPROD");
		params.codVol = itemVO.asString("CODVOL");
		params.qtdNeg = itemVO.asBigDecimal("QTDNEG");
		params.codLocal = itemVO.asBigDecimal("CODLOCALORIG");
		params.controle = itemVO.asString("CONTROLE");
		params.nuNota = itemVO.asBigDecimal("NUNOTA");
		params.chamadoPelaTela = true;
		ComercialUtils.PrecoUnitarioInfo pui = centralItemNota.inicializaProduto(params);
		itemVO.setProperty("VLRUNIT", pui.getVlrUnit());
		itemVO.setProperty("PRECOBASE", pui.getPrecoBase());
		itemVO.setProperty("NUTAB", pui.getNuTab());
		itemVO.setProperty("CODLOCALORIG", itemVO.asBigDecimal("CODLOCALORIG"));
		itemVO.setProperty("CODLOCALDEST", itemVO.asBigDecimal("CODLOCALORIG"));
		itemVO.setProperty("CONTROLE", itemVO.asString("CONTROLE"));
		itemVO.setProperty("CONTROLEDEST", itemVO.asString("CONTROLE"));
		centralItemNota.recalcularValores("QTDNEG", "", itemVO, itemVO.asBigDecimal("NUNOTA"));
	}

	public void gerarTransferencia() throws Exception {
		PrePersistEntityState cabState = createCabecalhoPrePersistEntityState();
		CACHelper cacHelper = new CACHelper();
		List<DynamicVO> itens = verificaEstoque();
		if (itens.size() >= 1)
			cacHelper.incluirAlterarCabecalho(this.auth, cabState);

		DynamicVO notaVO = cabState.getNewVO();
		BigDecimal nunota = notaVO.asBigDecimal("NUNOTA");
		this.numeroNotas.add(nunota);
		Collection<PrePersistEntityState> itensState = createItensPrePersistEntityState(notaVO, itens);
		if (this.numeroNotas.size() >= 1)
			cacHelper.incluirAlterarItem(nunota, null, this.auth, itensState, false);
		System.out.println("Nota de transferencia gerada com sucesso." + nunota.toString());
	}

	public void confirmarNotas() throws Exception {
		BarramentoRegra barramento = BarramentoRegra.build(CACHelper.class, "regrasConfirmacaoCAC.xml", this.auth);
		for (BigDecimal nunota : this.numeroNotas)
			ConfirmacaoNotaHelper.confirmarNota(nunota, barramento, true);
	}

	public void gerarLoteNotas() throws Exception {
		this.hnd.execWithTX(new JapeSession.TXBlock() {
			public void doWithTx() throws Exception {
				if (AutomatizacaoProcessosHelper.this.numeroNotas.size() == 1)
					JapeSession.putProperty("br.com.sankhya.com.habilita.client.event", Boolean.valueOf(true));
				ServicosNFeHelper2.gerarLoteNFeVersao2SemInteracao(AutomatizacaoProcessosHelper.this.numeroNotas);
			}
		});
	}
}
