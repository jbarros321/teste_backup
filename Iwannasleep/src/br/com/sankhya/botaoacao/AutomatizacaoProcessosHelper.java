package br.com.sankhya.botaoacao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.CanceledTransactionException;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.AtributosRegras;
import br.com.sankhya.modelcore.comercial.CentralItemNota;
import br.com.sankhya.modelcore.comercial.CentralItemNota.ParamsInicializacaoProduto;
import br.com.sankhya.modelcore.comercial.ComercialUtils.PrecoUnitarioInfo;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.UsuarioVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;

public class AutomatizacaoProcessosHelper {

	private SessionHandle		hnd;
	private EntityFacade		dwfFacade;
	private AuthenticationInfo	auth;
	private List<MensagemErro>	listaErros	= new ArrayList<MensagemErro>();

	public AutomatizacaoProcessosHelper(SessionHandle hnd) throws Exception {
		this.hnd = hnd;

		dwfFacade = EntityFacadeFactory.getDWFFacade();

		auth = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, 0);
		auth.makeCurrent();

		setupContext();
	}

	public List<MensagemErro> getMensagensErro(){
		return listaErros;
	}

	private void setupContext() throws Exception {
		UsuarioVO usuVO = (UsuarioVO) ((DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.USUARIO, new Object[] { auth.getUserID() })).wrapInterface(UsuarioVO.class);

		JapeSessionContext.putProperty("usuario_logado", auth.getUserID());
		JapeSessionContext.putProperty("emp_usu_logado", usuVO.getCODEMP());
		JapeSessionContext.putProperty("dh_atual", new Timestamp(System.currentTimeMillis()));
		JapeSessionContext.putProperty("d_atual", new Timestamp(TimeUtils.getToday()));
		JapeSessionContext.putProperty("usuarioVO", usuVO);
		JapeSessionContext.putProperty("authInfo", auth);
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
		JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
		JapeSession.putProperty(AtributosRegras.INC_UPD_ITEM_CENTRAL, Boolean.TRUE);
	}

	public void confirmarReservasEstoque() throws Exception {
		List<BigDecimal> reservasPendentes = getReservasEstoquePendentes();

		for(final BigDecimal nunotaNota : reservasPendentes) {
			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {
					try {
						confirmarNotaReserva(nunotaNota);
					} catch (Exception e) {
						StringBuilder error = new StringBuilder();

						error.append("<b>Confirmando Reserva de Estoque - Nota de Transferncia (").append(nunotaNota).append(")</b><br>");
						error.append("No foi possvel confirmar a reserva de estoque na Matriz:<br>");
						error.append("<b>" + nunotaNota + "</b><br>");

						if (e.getMessage() != null) {
							error.append("<b>Motivo:</b><br>");
							error.append(e.getMessage());
							error.append("<br><br>");
						}

						e.printStackTrace();

						MensagemErro msg = new MensagemErro();
						msg.mensagem = error.toString();
						listaErros.add(msg);

						CanceledTransactionException cancelEx = new CanceledTransactionException();
						cancelEx.initCause(e);
						throw cancelEx;
					}
				}
			});
		}
	}

	public void cancelarReservasEstoqueAntigas() throws Exception {
		List<BigDecimal> pedidosParaCancelarReservas = getPedidosComReservasEstoqueAntigas();

		for(final BigDecimal nunotaPedido : pedidosParaCancelarReservas) {
			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {
					try {
						removerReservaEstoqueMatriz(nunotaPedido, true);
					} catch (Exception e) {
						StringBuilder error = new StringBuilder();

						error.append("<b>Cancelando Reserva Antiga - Pedido de Venda (").append(nunotaPedido).append(")</b><br>");
						error.append("No foi possvel cancelar a reserva de estoque na Matriz do pedido de venda:<br>");
						error.append("<b>" + nunotaPedido + "</b><br>");

						if (e.getMessage() != null) {
							error.append("<b>Motivo:</b><br>");
							error.append(e.getMessage());
							error.append("<br><br>");
						}

						MensagemErro msg = new MensagemErro();
						msg.mensagem = error.toString();
						listaErros.add(msg);

						CanceledTransactionException cancelEx = new CanceledTransactionException();
						cancelEx.initCause(e);
						throw cancelEx;
					}
				}
			});
		}
	}

	private void removerReservaEstoqueMatriz(BigDecimal nunotaOrig, boolean reservaExpirou) throws Exception {
		if(nunotaOrig != null) {
			PersistentLocalEntity notaOrigEntity = dwfFacade.findEntityByPrimaryKey(DynamicEntityNames.CABECALHO_NOTA, new Object[] { nunotaOrig });
			DynamicVO notaOrigVO = (DynamicVO) notaOrigEntity.getValueObject();

			BigDecimal codTopReserva = notaOrigVO.asBigDecimal("TipoOperacao.AD_CODTIPOPERRESERVA");

			if(codTopReserva != null) {
				removerReservaEstoqueMatriz(nunotaOrig, codTopReserva);
			}

			if(reservaExpirou) {
				notaOrigVO.setProperty("AD_RESERVAEXPIROU", "S");
				notaOrigEntity.setValueObject((EntityVO) notaOrigVO);
			}
		}
	}

	public void removerReservaEstoqueMatriz(BigDecimal nunotaOrig, BigDecimal codTopReserva) throws Exception {
		BigDecimal nunotaReserva = getReservaEstoquePeloPedido(nunotaOrig, codTopReserva);

		if(nunotaReserva != null) {
			dwfFacade.removeEntity(DynamicEntityNames.CABECALHO_NOTA, new Object[] { nunotaReserva });
		}
	}

	private BigDecimal getMaxQtdDiasReserva(JdbcWrapper jdbc) throws Exception {
		NativeSql sqlMaxQtdDiasReserva = null;

		try {
			sqlMaxQtdDiasReserva = new NativeSql(jdbc);
			ResultSet rsMaxQtdDiasReserva = sqlMaxQtdDiasReserva.executeQuery("SELECT INTEIRO FROM TSIPAR PR WHERE PR.CHAVE = 'MAXDIASRESERVA'");

			if(rsMaxQtdDiasReserva.next()) {
				return rsMaxQtdDiasReserva.getBigDecimal(1);
			}
		} finally {
			NativeSql.releaseResources(sqlMaxQtdDiasReserva);
		}

		return null;
	}

	private List<BigDecimal> getReservasEstoquePendentes() throws Exception {
		JdbcWrapper jdbc = null;
		NativeSql sqlReservasPendentes = null;
		List<BigDecimal> reservasPendentes = new ArrayList<BigDecimal>();

		try {
			jdbc = dwfFacade.getJdbcWrapper();
			jdbc.openSession();

			sqlReservasPendentes = new NativeSql(jdbc);

			sqlReservasPendentes.appendSql(" SELECT ");
			sqlReservasPendentes.appendSql("     CAB.NUNOTA");
			sqlReservasPendentes.appendSql(" FROM ");
			sqlReservasPendentes.appendSql("     TGFCAB CAB");
			sqlReservasPendentes.appendSql(" WHERE ");
			sqlReservasPendentes.appendSql("     CAB.TIPMOV = 'T' AND");
			sqlReservasPendentes.appendSql("     CAB.STATUSNOTA = 'A' AND ");
			sqlReservasPendentes.appendSql("     EXISTS (SELECT 1 ");
			sqlReservasPendentes.appendSql("             FROM ");
			sqlReservasPendentes.appendSql("                 TGFVAR VR, ");
			sqlReservasPendentes.appendSql("                 TGFCAB ORIG");
			sqlReservasPendentes.appendSql("                 INNER JOIN TGFTOP TP ON(TP.CODTIPOPER = ORIG.CODTIPOPER AND TP.DHALTER = ORIG.DHTIPOPER)");
			sqlReservasPendentes.appendSql("             WHERE ");
			sqlReservasPendentes.appendSql("                 VR.NUNOTA = CAB.NUNOTA AND ");
			sqlReservasPendentes.appendSql("                 ORIG.NUNOTA = VR.NUNOTAORIG AND");
			sqlReservasPendentes.appendSql("                 TP.AD_CODTIPOPERRESERVA IS NOT NULL AND");
			sqlReservasPendentes.appendSql("                 AD_CODLOCALRESERVA IS NOT NULL");
			sqlReservasPendentes.appendSql("     ) ");

			ResultSet rsReservasPendentes = sqlReservasPendentes.executeQuery();

			while(rsReservasPendentes.next()) {
				reservasPendentes.add(rsReservasPendentes.getBigDecimal("NUNOTA"));
			}
		}finally {
			NativeSql.releaseResources(sqlReservasPendentes);
			JdbcWrapper.closeSession(jdbc);
		}

		return reservasPendentes;
	}

	private List<BigDecimal> getPedidosComReservasEstoqueAntigas() throws Exception {
		JdbcWrapper jdbc = null;
		NativeSql sqlReservasAntigas = null;
		List<BigDecimal> pedidosAntigos = new ArrayList<BigDecimal>();

		try {
			jdbc = dwfFacade.getJdbcWrapper();
			jdbc.openSession();

			BigDecimal maxQtdDiasReserva = getMaxQtdDiasReserva(jdbc);

			if(maxQtdDiasReserva != null) {
				sqlReservasAntigas = new NativeSql(jdbc);

				sqlReservasAntigas.appendSql(" SELECT ");
				sqlReservasAntigas.appendSql("     CAB.NUNOTA ");
				sqlReservasAntigas.appendSql(" FROM  ");
				sqlReservasAntigas.appendSql("     TGFCAB CAB  ");
				sqlReservasAntigas.appendSql("     INNER JOIN TGFTOP TOP ON (TOP.CODTIPOPER = CAB.CODTIPOPER AND TOP.DHALTER = CAB.DHTIPOPER) ");
				sqlReservasAntigas.appendSql(" WHERE ");
				sqlReservasAntigas.appendSql("     TOP.AD_CODTIPOPERRESERVA IS NOT NULL AND ");
				sqlReservasAntigas.appendSql("     TOP.AD_CODLOCALRESERVA IS NOT NULL AND ");
				sqlReservasAntigas.appendSql("     CAB.STATUSNOTA = 'L' AND ");
				sqlReservasAntigas.appendSql("     CAB.PENDENTE = 'S' AND ");
				sqlReservasAntigas.appendSql("     diffdays(onlydate(dbDate()), onlydate(CAB.DTFATUR)) > :MAXDIASRESERVA AND ");
				sqlReservasAntigas.appendSql("     EXISTS (SELECT 1 FROM TGFVAR VR, TGFCAB RSV WHERE VR.NUNOTAORIG = CAB.NUNOTA AND RSV.NUNOTA = VR.NUNOTA AND VR.SEQUENCIA < 0) ");

				sqlReservasAntigas.setNamedParameter("MAXDIASRESERVA", maxQtdDiasReserva);

				ResultSet rsReservasAntigas = sqlReservasAntigas.executeQuery();

				while(rsReservasAntigas.next()) {
					pedidosAntigos.add(rsReservasAntigas.getBigDecimal("NUNOTA"));
				}
			}
		}finally {
			NativeSql.releaseResources(sqlReservasAntigas);
			JdbcWrapper.closeSession(jdbc);
		}

		return pedidosAntigos;
	}

	public BigDecimal getReservaEstoquePeloPedido(BigDecimal nunotaPedido, BigDecimal codTopReserva) throws Exception {
		JdbcWrapper jdbc = null;
		NativeSql sqlNotaReserva = null;

		try {
			jdbc = dwfFacade.getJdbcWrapper();
			jdbc.openSession();

			sqlNotaReserva = new NativeSql(jdbc);

			sqlNotaReserva.appendSql(" SELECT ");
			sqlNotaReserva.appendSql("     VR.NUNOTA ");
			sqlNotaReserva.appendSql(" FROM  ");
			sqlNotaReserva.appendSql(" 	TGFVAR VR INNER JOIN TGFCAB CAB ON(CAB.NUNOTA = VR.NUNOTA) ");
			sqlNotaReserva.appendSql(" WHERE  ");
			sqlNotaReserva.appendSql(" 	VR.NUNOTAORIG = :NUNOTAORIG AND ");
			sqlNotaReserva.appendSql(" 	CAB.TIPMOV = 'T' AND ");
			sqlNotaReserva.appendSql(" 	CAB.CODTIPOPER = :CODTOPRESERVA ");

			sqlNotaReserva.setNamedParameter("NUNOTAORIG", nunotaPedido);
			sqlNotaReserva.setNamedParameter("CODTOPRESERVA", codTopReserva);

			ResultSet rsNotaReserva = sqlNotaReserva.executeQuery();

			if(rsNotaReserva.next()) {
				return rsNotaReserva.getBigDecimal("NUNOTA");
			}
		}finally {
			NativeSql.releaseResources(sqlNotaReserva);
			JdbcWrapper.closeSession(jdbc);
		}

		return null;
	}

	public void reservarEstoqueMatriz(DynamicVO pedidoVendaVO, DynamicVO topReservaVO) throws Exception {
		BigDecimal nunotaOrig = pedidoVendaVO.asBigDecimal("NUNOTA");

		FinderWrapper finderItensNotaOrig = new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ? AND this.AD_MODALIDADEVENDA = 'B'", new Object[] { nunotaOrig });
		finderItensNotaOrig.setOrderBy("SEQUENCIA");

		Collection<DynamicVO> itensNotaOrig = dwfFacade.findByDynamicFinderAsVO(finderItensNotaOrig);

		if( ! itensNotaOrig.isEmpty()) {
			PrePersistEntityState cabState = createCabecalhoReservaPrePersistEntityState(pedidoVendaVO, topReservaVO);

			CACHelper cacHelper = new CACHelper();
			cacHelper.incluirAlterarCabecalho(auth, cabState);

			DynamicVO notaReservaVO = (DynamicVO) cabState.getEntity().getValueObject();
			BigDecimal nunotaDest = notaReservaVO.asBigDecimal("NUNOTA");

			Collection<PrePersistEntityState> itensState = createItensReservaPrePersistEntityState(itensNotaOrig, notaReservaVO, nunotaOrig, topReservaVO);
			cacHelper.incluirAlterarItem(nunotaDest, null, auth, itensState, false);

			criarVinculoEntreDocumentos(nunotaOrig, nunotaDest);
		}
	}

	private void confirmarNotaReserva(BigDecimal nunota) throws Exception {
		JdbcWrapper jdbc = null;

		try {
			jdbc = dwfFacade.getJdbcWrapper();
			jdbc.openSession();

			DynamicVO notaVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, new Object[] { nunota });
			DynamicVO topVO = notaVO.asDymamicVO("TipoOperacao");

			Connection conn = jdbc.getConnection();

			CallableStatement cstmt = conn.prepareCall("{call STP_CONFIRMANOTA2(?,?,?)}");

			cstmt.setBigDecimal(1, nunota);
			cstmt.setString(2, "N");
			cstmt.setBigDecimal(3, topVO.asBigDecimal("ATUALFIN"));
			cstmt.execute();

			dwfFacade.clearSessionCache("CabecalhoNota");
			dwfFacade.clearSessionCache("Financeiro");
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}

	public void gerarDocumentosMatriz() throws Exception {

		gerarPedidoCompraViaNotaVenda();

		gerarPedidoVendaViaPedidoCompra();

		gerarPedidoCompraViaPedidoVenda();
	}

	private void gerarPedidoCompraViaNotaVenda() throws Exception {
		List<Map<String, Object>> listNotaVenda = construirListaNotaVendaOrig();

		for (final Map<String, Object> notaVenda : listNotaVenda) {

			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {
					try {
						gerarDocumentoDestino(notaVenda);
					} catch (Exception e) {
						StringBuilder error = new StringBuilder();

						error.append("<b>Etapa 1 - Nota Fiscal de Venda (").append(notaVenda.get("NUNOTA")).append(") > Pedido de Compra</b><br>");
						error.append("No foi possvel gerar o pedido de compra a partir da nota fiscal de venda com nmero nico:<br>");
						error.append("<b>" + notaVenda.get("NUNOTA") + "</b><br>");

						if (e.getMessage() != null) {
							error.append("<b>Motivo:</b><br>");
							error.append(e.getMessage());
							error.append("<br><br>");
						}

						MensagemErro msg = new MensagemErro();
						msg.mensagem = error.toString();
						listaErros.add(msg);

						CanceledTransactionException cancelEx = new CanceledTransactionException();
						cancelEx.initCause(e);
						throw cancelEx;
					}
				}
			});
		}
	}

	private void gerarPedidoVendaViaPedidoCompra() throws Exception {
		List<Map<String, Object>> listPedidoCompra = construirListaPedidoCompraOrig();

		for (final Map<String, Object> pedidoCompra : listPedidoCompra) {

			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {
					try {
						BigDecimal nunotaPedidoCompra = (BigDecimal) pedidoCompra.get("NUNOTA");

						if(temItensProntaEntrega(nunotaPedidoCompra)) {

							BigDecimal pedidoOrig = (BigDecimal) pedidoCompra.get("AD_NUNOTAVENDA");
							removerReservaEstoqueMatriz(pedidoOrig, false);
						}

						gerarDocumentoDestino(pedidoCompra);
					} catch (Exception e) {
						StringBuilder error = new StringBuilder();

						error.append("<b>Etapa 2 - Pedido de Compra (").append(pedidoCompra.get("NUNOTA")).append(") > Pedido de Venda</b><br>");
						error.append("No foi possvel gerar o pedido de venda a partir do pedido de compra com nmero nico: <br>");
						error.append("<b>" + pedidoCompra.get("NUNOTA") + "</b><br>");

						if (e.getMessage() != null) {
							error.append("<b>Motivo:</b><br>");
							error.append(e.getMessage());
							error.append("<br><br>");
						}

						MensagemErro msg = new MensagemErro();
						msg.mensagem = error.toString();
						listaErros.add(msg);

						CanceledTransactionException cancelEx = new CanceledTransactionException();
						cancelEx.initCause(e);
						throw cancelEx;
					}
				}
			});
		}
	}

	private boolean temItensProntaEntrega(BigDecimal nunota) throws Exception {
		FinderWrapper finderItensNota = new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ? AND this.AD_MODALIDADEVENDA = 'B'", new Object[] { nunota });
		finderItensNota.setOrderBy("SEQUENCIA");

		Collection<DynamicVO> itensNota = dwfFacade.findByDynamicFinderAsVO(finderItensNota);

		if( ! itensNota.isEmpty()) {
			return true;
		}

		return false;
	}

	private void gerarPedidoCompraViaPedidoVenda() throws Exception {
		List<Map<String, Object>> listaPedidoVenda = contruirListaPedidoVendaOrig();

		for (final Map<String, Object> pedidoVenda : listaPedidoVenda) {

			hnd.execWithTX(new JapeSession.TXBlock() {
				public void doWithTx() throws Exception {
					try {
						gerarPedidoCompraPorParceiro(pedidoVenda);
					} catch (Exception e) {
						StringBuilder error = new StringBuilder();

						error.append("<b>Etapa 3 - Pedido de Venda ").append(pedidoVenda.get("NUNOTA")).append(" > Pedido de Compra</b><br>");
						error.append("No foi possvel gerar o pedido de compra a partir do pedido de venda com nmero nico:");
						error.append("<b>" + pedidoVenda.get("NUNOTA") + "</b><br>");

						if (e.getMessage() != null) {
							error.append("<b>Motivo:</b><br>");
							error.append(e.getMessage());
							error.append("<br><br>");
						}

						MensagemErro msg = new MensagemErro();
						msg.mensagem = error.toString();
						listaErros.add(msg);

						CanceledTransactionException cancelEx = new CanceledTransactionException();
						cancelEx.initCause(e);
						throw cancelEx;
					}
				}
			});
		}
	}

	private List<Map<String, Object>> construirListaNotaVendaOrig() throws Exception {
		JdbcWrapper jdbc = null;
		NativeSql sqlNotaVendaOrig = null;

		try {
			jdbc = dwfFacade.getJdbcWrapper();
			jdbc.openSession();

			sqlNotaVendaOrig = new NativeSql(jdbc);

			sqlNotaVendaOrig.appendSql(" SELECT CAB.NUNOTA,  ");
			sqlNotaVendaOrig.appendSql("     CAB.CODEMP,  ");
			sqlNotaVendaOrig.appendSql("     EMP.NOMEFANTASIA, ");
			sqlNotaVendaOrig.appendSql("     CAB.CODCENCUS,  ");
			sqlNotaVendaOrig.appendSql("     CAB.CODEMPNEGOC,  ");
			sqlNotaVendaOrig.appendSql("     CAB.CIF_FOB,  ");
			sqlNotaVendaOrig.appendSql("     CAB.QTDVOL,  ");
			sqlNotaVendaOrig.appendSql("     CAB.CODPARC, ");
			sqlNotaVendaOrig.appendSql("     PAR.NOMEPARC, ");
			sqlNotaVendaOrig.appendSql("     CAB.OBSERVACAO, ");
			sqlNotaVendaOrig.appendSql("     CAB.AD_NUNOTAVENDA, ");
			sqlNotaVendaOrig.appendSql("     (SELECT DISTINCT VR.NUNOTAORIG FROM TGFVAR VR WHERE VR.NUNOTA = CAB.NUNOTA) AS PEDIDO_ORIG, ");
			sqlNotaVendaOrig.appendSql("     TOP.AD_CODPARC AS CODPARC_DEST,   ");
			sqlNotaVendaOrig.appendSql("     TOP.AD_CODTIPOPER AS CODTIPOPER_DEST,  ");
			sqlNotaVendaOrig.appendSql("     TOP.AD_CODNAT AS CODNAT_DEST,  ");
			sqlNotaVendaOrig.appendSql("     TOP.AD_CODCENCUS AS CODCENCUS_DEST,  ");
			sqlNotaVendaOrig.appendSql("     TOP.AD_CODLOCAL AS CODLOCAL_DEST,  ");
			sqlNotaVendaOrig.appendSql("     (SELECT DISTINCT TIPMOV FROM TGFTOP WHERE CODTIPOPER = TOP.AD_CODTIPOPER) AS TIPMOV_DEST,  ");
			sqlNotaVendaOrig.appendSql("     CASE WHEN (CPL.SUGTIPNEGENTR IS NULL OR CPL.SUGTIPNEGENTR = 0 ) THEN CAB.CODTIPVENDA ELSE CPL.SUGTIPNEGENTR END AS CODTIPVENDA_DEST  ");
			sqlNotaVendaOrig.appendSql(" FROM TGFCAB CAB  ");
			sqlNotaVendaOrig.appendSql("     INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP ");
			sqlNotaVendaOrig.appendSql("     INNER JOIN TGFPAR PAR ON PAR.CODPARC = CAB.CODPARC ");
			sqlNotaVendaOrig.appendSql("     INNER JOIN TGFTOP TOP ON TOP.CODTIPOPER = CAB.CODTIPOPER AND TOP.DHALTER = CAB.DHTIPOPER  ");
			sqlNotaVendaOrig.appendSql("     LEFT JOIN TGFCPL CPL ON CPL.CODPARC = TOP.AD_CODPARC  ");
			sqlNotaVendaOrig.appendSql(" WHERE CAB.STATUSNFE = 'A'  ");
			sqlNotaVendaOrig.appendSql(" 	 AND TOP.AD_GERANF = 'S'  ");
			sqlNotaVendaOrig.appendSql(" 	 AND TOP.TIPMOV = 'V'  ");
			sqlNotaVendaOrig.appendSql(" 	 AND (CAB.AD_PROCESSADOAUTOMACAO IS NULL OR CAB.AD_PROCESSADOAUTOMACAO = 'N') ");
			sqlNotaVendaOrig.appendSql(" 	 AND NOT EXISTS(SELECT 1 FROM TGFVAR V WHERE V.NUNOTAORIG = CAB.NUNOTA) ");

			return executaQueryListaNotaOrig(sqlNotaVendaOrig, jdbc, true);
		}finally {
			NativeSql.releaseResources(sqlNotaVendaOrig);
			JdbcWrapper.closeSession(jdbc);
		}
	}

	private List<Map<String, Object>> construirListaPedidoCompraOrig() throws Exception {
		JdbcWrapper jdbc = null;
		NativeSql sqlPedidoCompraOrig = null;

		try {
			jdbc = dwfFacade.getJdbcWrapper();
			jdbc.openSession();

			sqlPedidoCompraOrig = new NativeSql(jdbc);

			sqlPedidoCompraOrig.appendSql(" SELECT CAB.NUNOTA, ");
			sqlPedidoCompraOrig.appendSql("        CAB.CODCENCUS, ");
			sqlPedidoCompraOrig.appendSql("        CAB.CODEMPNEGOC, ");
			sqlPedidoCompraOrig.appendSql("        CAB.CIF_FOB, ");
			sqlPedidoCompraOrig.appendSql("        CAB.QTDVOL, ");
			sqlPedidoCompraOrig.appendSql("        DECODE(TOP.AD_CODEMP,NULL,CAB.CODEMP,TOP.AD_CODEMP) AS CODEMP, ");
			sqlPedidoCompraOrig.appendSql("        EMP.CODPARC AS CODPARC_DEST,  ");
			sqlPedidoCompraOrig.appendSql("	       CAB.OBSERVACAO, ");
			sqlPedidoCompraOrig.appendSql("	       CAB.AD_NUNOTAVENDA, ");
			sqlPedidoCompraOrig.appendSql("        TOP.AD_CODTIPOPER AS CODTIPOPER_DEST, ");
			sqlPedidoCompraOrig.appendSql("        TOP.AD_CODNAT AS CODNAT_DEST, ");
			sqlPedidoCompraOrig.appendSql("        TOP.AD_CODCENCUS AS CODCENCUS_DEST, ");
			sqlPedidoCompraOrig.appendSql("        TOP.AD_CODLOCAL AS CODLOCAL_DEST, ");
			sqlPedidoCompraOrig.appendSql("        (SELECT DISTINCT TIPMOV FROM TGFTOP WHERE CODTIPOPER = TOP.AD_CODTIPOPER) AS TIPMOV_DEST, ");
			sqlPedidoCompraOrig.appendSql("        CASE WHEN (CPL.SUGTIPNEGSAID IS NULL OR CPL.SUGTIPNEGSAID = 0 ) THEN CAB.CODTIPVENDA ELSE CPL.SUGTIPNEGSAID END AS CODTIPVENDA_DEST ");
			sqlPedidoCompraOrig.appendSql("   FROM TGFCAB CAB ");
			sqlPedidoCompraOrig.appendSql("        INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP ");
			sqlPedidoCompraOrig.appendSql("        INNER JOIN TGFTOP TOP ON TOP.CODTIPOPER = CAB.CODTIPOPER AND TOP.DHALTER = CAB.DHTIPOPER ");
			sqlPedidoCompraOrig.appendSql("        LEFT JOIN TGFCPL CPL ON CPL.CODPARC = EMP.CODPARC ");
			sqlPedidoCompraOrig.appendSql("  WHERE CAB.STATUSNOTA = 'L' ");
			sqlPedidoCompraOrig.appendSql("    AND TOP.AD_GERANF = 'S' ");
			sqlPedidoCompraOrig.appendSql("    AND TOP.TIPMOV = 'O' ");
			sqlPedidoCompraOrig.appendSql("	   AND (CAB.AD_PROCESSADOAUTOMACAO IS NULL OR CAB.AD_PROCESSADOAUTOMACAO = 'N') ");
			sqlPedidoCompraOrig.appendSql("    AND NOT EXISTS(SELECT 1 FROM TGFVAR V WHERE V.NUNOTAORIG = CAB.NUNOTA) ");

			return executaQueryListaNotaOrig(sqlPedidoCompraOrig, jdbc);
		} finally {
			NativeSql.releaseResources(sqlPedidoCompraOrig);
			JdbcWrapper.closeSession(jdbc);
		}
	}

	private List<Map<String, Object>> contruirListaPedidoVendaOrig() throws Exception {
		JdbcWrapper jdbc = null;
		NativeSql sqlPedidoVendaOrig = null;

		try {
			jdbc = dwfFacade.getJdbcWrapper();
			jdbc.openSession();

			sqlPedidoVendaOrig = new NativeSql(jdbc);

			sqlPedidoVendaOrig.appendSql(" SELECT CAB.NUNOTA, ");
			sqlPedidoVendaOrig.appendSql("        CAB.CODCENCUS, ");
			sqlPedidoVendaOrig.appendSql("        CAB.CODEMPNEGOC, ");
			sqlPedidoVendaOrig.appendSql("        CAB.CIF_FOB, ");
			sqlPedidoVendaOrig.appendSql("        CAB.AD_NUNOTAVENDA, ");
			sqlPedidoVendaOrig.appendSql("        0 AS QTDVOL, ");
			sqlPedidoVendaOrig.appendSql("        0 AS CODPARC_DEST,  ");
			sqlPedidoVendaOrig.appendSql("        DECODE(TOP.AD_CODEMP,NULL,CAB.CODEMP,TOP.AD_CODEMP) AS CODEMP, ");
			sqlPedidoVendaOrig.appendSql("        CAB.OBSERVACAO, ");
			sqlPedidoVendaOrig.appendSql("        TOP.AD_CODTIPOPER AS CODTIPOPER_DEST, ");
			sqlPedidoVendaOrig.appendSql("        TOP.AD_CODNAT AS CODNAT_DEST, ");
			sqlPedidoVendaOrig.appendSql("        TOP.AD_CODCENCUS AS CODCENCUS_DEST, ");
			sqlPedidoVendaOrig.appendSql("        TOP.AD_CODLOCAL AS CODLOCAL_DEST, ");
			sqlPedidoVendaOrig.appendSql("        (SELECT DISTINCT TIPMOV FROM TGFTOP WHERE CODTIPOPER = TOP.AD_CODTIPOPER) AS TIPMOV_DEST, ");
			sqlPedidoVendaOrig.appendSql("        0 AS CODTIPVENDA_DEST ");
			sqlPedidoVendaOrig.appendSql("   FROM TGFCAB CAB ");
			sqlPedidoVendaOrig.appendSql("        INNER JOIN TSIEMP EMP ON EMP.CODEMP = CAB.CODEMP ");
			sqlPedidoVendaOrig.appendSql("        INNER JOIN TGFTOP TOP ON TOP.CODTIPOPER = CAB.CODTIPOPER AND TOP.DHALTER = CAB.DHTIPOPER ");
			sqlPedidoVendaOrig.appendSql("  WHERE CAB.STATUSNOTA = 'L' ");
			sqlPedidoVendaOrig.appendSql("    AND TOP.AD_GERANF = 'S' ");
			sqlPedidoVendaOrig.appendSql("    AND TOP.TIPMOV = 'P' ");
			sqlPedidoVendaOrig.appendSql("	  AND (CAB.AD_PROCESSADOAUTOMACAO IS NULL OR CAB.AD_PROCESSADOAUTOMACAO = 'N') ");
			sqlPedidoVendaOrig.appendSql("    AND NOT EXISTS(SELECT 1 FROM TGFVAR V WHERE V.NUNOTAORIG = CAB.NUNOTA) ");

			return executaQueryListaNotaOrig(sqlPedidoVendaOrig, jdbc);
		} finally {
			NativeSql.releaseResources(sqlPedidoVendaOrig);
			JdbcWrapper.closeSession(jdbc);
		}
	}

	private List<Map<String, Object>> executaQueryListaNotaOrig(NativeSql sqlNotaOrig, JdbcWrapper jdbc) throws Exception {
		return executaQueryListaNotaOrig(sqlNotaOrig, jdbc, false);
	}

	private List<Map<String, Object>> executaQueryListaNotaOrig(NativeSql sqlNotaOrig, JdbcWrapper jdbc, boolean rastrearOrigem) throws Exception {
		ResultSet rsNotaOrig = null;

		try {
			rsNotaOrig = sqlNotaOrig.executeQuery();
			List<Map<String, Object>> listNotaOrig = new ArrayList<Map<String, Object>>();

			while (rsNotaOrig.next()) {
				Map<String, Object> notaOrig = new HashMap<String, Object>();

				notaOrig.put("NUNOTA", rsNotaOrig.getBigDecimal("NUNOTA"));
				notaOrig.put("CODEMP", rsNotaOrig.getBigDecimal("CODEMP"));
				notaOrig.put("CODCENCUS", rsNotaOrig.getBigDecimal("CODCENCUS"));
				notaOrig.put("QTDVOL", rsNotaOrig.getBigDecimal("QTDVOL"));
				notaOrig.put("CIF_FOB", rsNotaOrig.getString("CIF_FOB"));
				notaOrig.put("CODEMPNEGOC", rsNotaOrig.getBigDecimal("CODEMPNEGOC"));
				notaOrig.put("CODPARC_DEST", rsNotaOrig.getBigDecimal("CODPARC_DEST"));
				notaOrig.put("CODTIPOPER_DEST", rsNotaOrig.getBigDecimal("CODTIPOPER_DEST"));
				notaOrig.put("CODNAT_DEST", rsNotaOrig.getBigDecimal("CODNAT_DEST"));
				notaOrig.put("CODCENCUS_DEST", rsNotaOrig.getBigDecimal("CODCENCUS_DEST"));
				notaOrig.put("CODLOCAL_DEST", rsNotaOrig.getBigDecimal("CODLOCAL_DEST"));
				notaOrig.put("TIPMOV_DEST", rsNotaOrig.getString("TIPMOV_DEST"));
				notaOrig.put("CODTIPVENDA_DEST", rsNotaOrig.getBigDecimal("CODTIPVENDA_DEST"));
				notaOrig.put("OBSERVACAO", rsNotaOrig.getString("OBSERVACAO"));
				notaOrig.put("AD_NUNOTAVENDA", rsNotaOrig.getBigDecimal("AD_NUNOTAVENDA"));

				if(rastrearOrigem) {
					notaOrig.put("PEDIDO_ORIG", rsNotaOrig.getBigDecimal("PEDIDO_ORIG"));
					notaOrig.put("CODPARC", rsNotaOrig.getBigDecimal("CODPARC"));
					notaOrig.put("NOMEPARC", rsNotaOrig.getString("NOMEPARC"));
					notaOrig.put("NOMEFANTASIA", rsNotaOrig.getString("NOMEFANTASIA"));
				}

				listNotaOrig.add(notaOrig);
			}

			return listNotaOrig;
		} finally {
			if (rsNotaOrig != null) {
				rsNotaOrig.close();
			}
		}
	}

	private void gerarPedidoCompraPorParceiro(Map<String, Object> pedidoVendaOrig) throws Exception {
		BigDecimal nunotaOrig = (BigDecimal) pedidoVendaOrig.get("NUNOTA");
		List<Map<String, Object>> listParceiroFornecedor = constroiListaParceiroFornecedor(nunotaOrig);

		for (Map<String, Object> parceiroFornecedor : listParceiroFornecedor) {

			Map<String, Object> clonePedidoVendaOrig = new HashMap<String, Object>(pedidoVendaOrig);
			BigDecimal codParcFornecedor = (BigDecimal) parceiroFornecedor.get("CODPARC");

			clonePedidoVendaOrig.put("QTDVOL", parceiroFornecedor.get("QTDVOL"));
			clonePedidoVendaOrig.put("CODPARC_DEST", codParcFornecedor);

			PrePersistEntityState cabState = createCabecalhoPrePersistEntityState(clonePedidoVendaOrig);

			CACHelper cacHelper = new CACHelper();
			cacHelper.incluirAlterarCabecalho(auth, cabState);

			DynamicVO notaDestinoVO = (DynamicVO) cabState.getEntity().getValueObject();
			BigDecimal nunotaDest = notaDestinoVO.asBigDecimal("NUNOTA");
			BigDecimal codLocalOrig = (BigDecimal) clonePedidoVendaOrig.get("CODLOCAL_DEST");

			Collection<PrePersistEntityState> itensState = createItensByParceiroPrePersistEntityState(codParcFornecedor, notaDestinoVO, nunotaOrig, codLocalOrig, dwfFacade);
			cacHelper.incluirAlterarItem(nunotaDest, null, auth, itensState, false);

			criarVinculoEntreDocumentos(nunotaOrig, nunotaDest);
		}

		atualizarParaProcessado(nunotaOrig);
	}

	private List<Map<String, Object>> constroiListaParceiroFornecedor(BigDecimal nunotaOrig) throws Exception {
		JdbcWrapper jdbc = null;
		NativeSql sqlParceiroFornecedor = null;

		try {
			jdbc = dwfFacade.getJdbcWrapper();
			jdbc.openSession();

			sqlParceiroFornecedor = new NativeSql(jdbc);

			sqlParceiroFornecedor.appendSql(" SELECT ");
			sqlParceiroFornecedor.appendSql("  PRO.CODPARCFORN AS CODPARC, ");
			sqlParceiroFornecedor.appendSql("  CASE WHEN (CPL.SUGTIPNEGENTR IS NULL OR CPL.SUGTIPNEGENTR = 0 ) THEN 12 ELSE CPL.SUGTIPNEGENTR END AS CODTIPVENDA_DEST, ");
			sqlParceiroFornecedor.appendSql("  SUM(ITE.QTDNEG) AS QTDVOL ");
			sqlParceiroFornecedor.appendSql(" FROM ");
			sqlParceiroFornecedor.appendSql("  TGFITE ITE ");
			sqlParceiroFornecedor.appendSql("  INNER JOIN TGFPRO PRO ON (PRO.CODPROD = ITE.CODPROD) ");
			sqlParceiroFornecedor.appendSql("  LEFT JOIN TGFCPL CPL ON (CPL.CODPARC = PRO.CODPARCFORN) ");
			sqlParceiroFornecedor.appendSql(" WHERE ");
			sqlParceiroFornecedor.appendSql("  ITE.NUNOTA = :NUNOTA_ORIG ");
			sqlParceiroFornecedor.appendSql(" GROUP BY ");
			sqlParceiroFornecedor.appendSql("  PRO.CODPARCFORN,CPL.SUGTIPNEGENTR ");

			sqlParceiroFornecedor.setNamedParameter("NUNOTA_ORIG", nunotaOrig);

			ResultSet rsParceiroFornecedor = sqlParceiroFornecedor.executeQuery();
			List<Map<String, Object>> listParceiroFornecedor = new ArrayList<Map<String, Object>>();

			while (rsParceiroFornecedor.next()) {
				Map<String, Object> parceiroFornecedor = new HashMap<String, Object>();

				parceiroFornecedor.put("CODPARC", rsParceiroFornecedor.getBigDecimal("CODPARC"));
				parceiroFornecedor.put("CODTIPVENDA", rsParceiroFornecedor.getBigDecimal("CODTIPVENDA_DEST"));
				parceiroFornecedor.put("QTDVOL", rsParceiroFornecedor.getBigDecimal("QTDVOL"));

				listParceiroFornecedor.add(parceiroFornecedor);
			}

			return listParceiroFornecedor;
		} finally {
			NativeSql.releaseResources(sqlParceiroFornecedor);
			JdbcWrapper.closeSession(jdbc);
		}
	}

	private void gerarDocumentoDestino(Map<String, Object> notaOrig) throws Exception {
		PrePersistEntityState cabState = createCabecalhoPrePersistEntityState(notaOrig);

		CACHelper cacHelper = new CACHelper();
		cacHelper.incluirAlterarCabecalho(auth, cabState);

		DynamicVO notaDestinoVO = (DynamicVO) cabState.getEntity().getValueObject();
		BigDecimal nunotaDest = notaDestinoVO.asBigDecimal("NUNOTA");
		BigDecimal codLocalOrig = (BigDecimal) notaOrig.get("CODLOCAL_DEST");
		BigDecimal nunotaOrig = (BigDecimal) notaOrig.get("NUNOTA");

		Collection<PrePersistEntityState> itensState = createItensPrePersistEntityState(notaDestinoVO, nunotaOrig, codLocalOrig);
		cacHelper.incluirAlterarItem(nunotaDest, null, auth, itensState, false);

		criarVinculoEntreDocumentos(nunotaOrig, nunotaDest);
		atualizarParaProcessado(nunotaOrig);
	}

	private void atualizarParaProcessado(BigDecimal nunota) throws Exception {
		PersistentLocalEntity cabEntity = dwfFacade.findEntityByPrimaryKey(DynamicEntityNames.CABECALHO_NOTA, new Object[] { nunota });

		DynamicVO cabVO = (DynamicVO) cabEntity.getValueObject();
		cabVO.setProperty("AD_PROCESSADOAUTOMACAO", "S");

		cabEntity.setValueObject((EntityVO) cabVO);
	}

	private void criarVinculoEntreDocumentos(BigDecimal nunotaOrig, BigDecimal nunotaDest) throws Exception {
		JdbcWrapper txJdbc = null;
		NativeSql sqlConexaoDocs = null;

		try {
			txJdbc = dwfFacade.getJdbcWrapper();
			txJdbc.openSession();

			sqlConexaoDocs = new NativeSql(txJdbc);

			sqlConexaoDocs.appendSql(" SELECT ");
			sqlConexaoDocs.appendSql("     	ITE_ORIG.NUNOTA AS NUNOTA_ORIG, ");
			sqlConexaoDocs.appendSql(" 		ITE_ORIG.SEQUENCIA AS SEQUENCIA_ORIG, ");
			sqlConexaoDocs.appendSql(" 		ITE_ORIG.CODPROD AS CODPROD_ORIG, ");
			sqlConexaoDocs.appendSql(" 		ITE_DEST.NUNOTA AS NUNOTA_DEST, ");
			sqlConexaoDocs.appendSql(" 		ITE_DEST.SEQUENCIA AS SEQUENCIA_DEST, ");
			sqlConexaoDocs.appendSql(" 		ITE_DEST.CODPROD AS CODPROD_DEST, ");
			sqlConexaoDocs.appendSql(" 		ITE_ORIG.QTDNEG AS QTDNEG_ORIG, ");
			sqlConexaoDocs.appendSql(" 		ITE_ORIG.AD_MODALIDADEVENDA AS AD_MODALIDADEVENDA ");
			sqlConexaoDocs.appendSql(" FROM  ");
			sqlConexaoDocs.appendSql(" 		TGFITE ITE_DEST INNER JOIN TGFITE ITE_ORIG ON(ITE_DEST.CODPROD = ITE_ORIG.CODPROD) ");
			sqlConexaoDocs.appendSql(" WHERE ");
			sqlConexaoDocs.appendSql(" 		ITE_DEST.NUNOTA = :NUNOTA_DEST AND ");
			sqlConexaoDocs.appendSql(" 		ITE_ORIG.NUNOTA = :NUNOTA_ORIG ");

			sqlConexaoDocs.setNamedParameter("NUNOTA_ORIG", nunotaOrig);
			sqlConexaoDocs.setNamedParameter("NUNOTA_DEST", nunotaDest);

			ResultSet rsConexaoDocs = sqlConexaoDocs.executeQuery();

			while (rsConexaoDocs.next()) {
				DynamicVO tgfvarVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO);

				tgfvarVO.setProperty("NUNOTA", rsConexaoDocs.getBigDecimal("NUNOTA_DEST"));
				tgfvarVO.setProperty("NUNOTAORIG", rsConexaoDocs.getBigDecimal("NUNOTA_ORIG"));
				tgfvarVO.setProperty("SEQUENCIA", rsConexaoDocs.getBigDecimal("SEQUENCIA_DEST"));
				tgfvarVO.setProperty("SEQUENCIAORIG", rsConexaoDocs.getBigDecimal("SEQUENCIA_ORIG"));
				tgfvarVO.setProperty("QTDATENDIDA", new BigDecimal(0));

				dwfFacade.createEntity(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, (EntityVO) tgfvarVO);
			}
		}finally {
			NativeSql.releaseResources(sqlConexaoDocs);
			JdbcWrapper.closeSession(txJdbc);
		}
	}

	private List<PrePersistEntityState> createItensPrePersistEntityState(DynamicVO notaVO, BigDecimal nunotaOrig, BigDecimal codLocalOrig) throws Exception {
		List<PrePersistEntityState> itensState = new ArrayList<PrePersistEntityState>();

		FinderWrapper finderItensNotaOrig = new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", new Object[] { nunotaOrig });
		finderItensNotaOrig.setOrderBy("SEQUENCIA");

		Collection<DynamicVO> itensNotaOrig = dwfFacade.findByDynamicFinderAsVO(finderItensNotaOrig);

		for (DynamicVO itemNotaOrig : itensNotaOrig) {

			DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);

			itemVO.setProperty("NUNOTA", notaVO.getProperty("NUNOTA"));
			itemVO.setProperty("CODEMP", notaVO.getProperty("CODEMP"));
			itemVO.setProperty("CODPROD", itemNotaOrig.getProperty("CODPROD"));
			itemVO.setProperty("USOPROD", itemNotaOrig.getProperty("USOPROD"));
			itemVO.setProperty("CODVOL", itemNotaOrig.getProperty("CODVOL"));
			itemVO.setProperty("CONTROLE", itemNotaOrig.getProperty("CONTROLE"));
			itemVO.setProperty("QTDNEG", itemNotaOrig.getProperty("QTDNEG"));
			itemVO.setProperty("QTDVOL", itemNotaOrig.getProperty("QTDVOL"));
			itemVO.setProperty("PENDENTE", "N");
			itemVO.setProperty("CODLOCALORIG", codLocalOrig);
			itemVO.setProperty("PERCDESC", BigDecimal.ZERO);
			itemVO.setProperty("VLRDESC", BigDecimal.ZERO);
			itemVO.setProperty("AD_MODALIDADEVENDA", itemNotaOrig.getProperty("AD_MODALIDADEVENDA"));

			inicializaProduto(itemVO);

			PrePersistEntityState itemState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA, itemVO);

			itensState.add(itemState);
		}

		return itensState;
	}

	private List<PrePersistEntityState> createItensByParceiroPrePersistEntityState(BigDecimal codParcFornecedor, DynamicVO notaVO, BigDecimal nunotaOrig, BigDecimal codLocalOrig, EntityFacade dwfFacade) throws Exception {
		List<PrePersistEntityState> itensState = new ArrayList<PrePersistEntityState>();

		FinderWrapper finderItensNotaOrig = new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ? AND Produto->CODPARCFORN = ?", new Object[] { nunotaOrig, codParcFornecedor });
		finderItensNotaOrig.setOrderBy("SEQUENCIA");

		Collection<DynamicVO> itensNotaOrig = dwfFacade.findByDynamicFinderAsVO(finderItensNotaOrig);

		for (DynamicVO itemNotaOrig : itensNotaOrig) {

			DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);

			itemVO.setProperty("NUNOTA", notaVO.getProperty("NUNOTA"));
			itemVO.setProperty("CODEMP", notaVO.getProperty("CODEMP"));
			itemVO.setProperty("CODPROD", itemNotaOrig.getProperty("CODPROD"));
			itemVO.setProperty("USOPROD", itemNotaOrig.getProperty("USOPROD"));
			itemVO.setProperty("CODVOL", itemNotaOrig.getProperty("CODVOL"));
			itemVO.setProperty("CONTROLE", itemNotaOrig.getProperty("CONTROLE"));
			itemVO.setProperty("QTDNEG", itemNotaOrig.getProperty("QTDNEG"));
			itemVO.setProperty("QTDVOL", itemNotaOrig.getProperty("QTDVOL"));
			itemVO.setProperty("PENDENTE", "N");
			itemVO.setProperty("CODLOCALORIG", codLocalOrig);
			itemVO.setProperty("PERCDESC", BigDecimal.ZERO);
			itemVO.setProperty("VLRDESC", BigDecimal.ZERO);
			itemVO.setProperty("AD_MODALIDADEVENDA", itemNotaOrig.getProperty("AD_MODALIDADEVENDA"));

			inicializaProduto(itemVO);

			PrePersistEntityState itemState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA, itemVO);

			itensState.add(itemState);
		}

		return itensState;
	}

	private List<PrePersistEntityState> createItensReservaPrePersistEntityState(Collection<DynamicVO> itensNotaOrig, DynamicVO notaDestVO, BigDecimal nunotaOrig, DynamicVO topReservaVO) throws Exception {
		BigDecimal codLocalOrigem = topReservaVO.asBigDecimal("AD_CODLOCAL");
		BigDecimal codLocalDestino = topReservaVO.asBigDecimal("AD_CODLOCALRESERVA");

		List<PrePersistEntityState> itensState = new ArrayList<PrePersistEntityState>();

		for (DynamicVO itemNotaOrig : itensNotaOrig) {

			DynamicVO itemVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);

			itemVO.setProperty("NUNOTA", notaDestVO.getProperty("NUNOTA"));
			itemVO.setProperty("CODEMP", notaDestVO.getProperty("CODEMP"));
			itemVO.setProperty("CODPROD", itemNotaOrig.getProperty("CODPROD"));
			itemVO.setProperty("USOPROD", itemNotaOrig.getProperty("USOPROD"));
			itemVO.setProperty("CODVOL", itemNotaOrig.getProperty("CODVOL"));
			itemVO.setProperty("CONTROLE", itemNotaOrig.getProperty("CONTROLE"));
			itemVO.setProperty("QTDNEG", itemNotaOrig.getProperty("QTDNEG"));
			itemVO.setProperty("QTDVOL", itemNotaOrig.getProperty("QTDVOL"));
			itemVO.setProperty("PENDENTE", "N");
			itemVO.setProperty("CODLOCALORIG", codLocalOrigem);
			itemVO.setProperty("CODLOCALDEST", codLocalDestino);
			itemVO.setProperty("PERCDESC", BigDecimal.ZERO);
			itemVO.setProperty("VLRDESC", BigDecimal.ZERO);

			inicializaProduto(itemVO);

			PrePersistEntityState itemState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.ITEM_NOTA, itemVO);

			itensState.add(itemState);
		}

		return itensState;
	}

	private void inicializaProduto(DynamicVO itemVO) throws Exception {
		CentralItemNota centralItemNota = new CentralItemNota();

		ParamsInicializacaoProduto params = new CentralItemNota.ParamsInicializacaoProduto();
		params.codProd = itemVO.asBigDecimal("CODPROD");
		params.codVol = itemVO.asString("CODVOL");
		params.qtdNeg = itemVO.asBigDecimal("QTDNEG");
		params.codLocal = itemVO.asBigDecimal("CODLOCALORIG");
		params.controle = itemVO.asString("CONTROLE");
		params.nuNota = itemVO.asBigDecimal("NUNOTA");
		params.chamadoPelaTela = true;

		PrecoUnitarioInfo pui = centralItemNota.inicializaProduto(params);

		itemVO.setProperty("VLRUNIT", pui.getVlrUnit());
		itemVO.setProperty("PRECOBASE", pui.getPrecoBase());
		itemVO.setProperty("NUTAB", pui.getNuTab());

		centralItemNota.recalcularValores("QTDNEG", "", itemVO, itemVO.asBigDecimal("NUNOTA"));
	}

	private PrePersistEntityState createCabecalhoPrePersistEntityState(Map<String, Object> notaVenda) throws Exception {
		DynamicVO newCabVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.CABECALHO_NOTA);

		newCabVO.setProperty("CODPARC", notaVenda.get("CODPARC_DEST"));
		newCabVO.setProperty("CODEMP", notaVenda.get("CODEMP"));
		newCabVO.setProperty("CODCENCUS", notaVenda.get("CODCENCUS"));
		newCabVO.setProperty("CODEMPNEGOC", notaVenda.get("CODEMPNEGOC"));
		newCabVO.setProperty("QTDVOL", notaVenda.get("QTDVOL"));
		newCabVO.setProperty("CIF_FOB", notaVenda.get("CIF_FOB"));
		newCabVO.setProperty("CODCENCUS", notaVenda.get("CODCENCUS"));
		newCabVO.setProperty("CODTIPOPER", notaVenda.get("CODTIPOPER_DEST"));
		newCabVO.setProperty("CODNAT", notaVenda.get("CODNAT_DEST"));
		newCabVO.setProperty("CODCENCUS", notaVenda.get("CODCENCUS_DEST"));
		newCabVO.setProperty("CODTIPVENDA", notaVenda.get("CODTIPVENDA_DEST"));
		newCabVO.setProperty("TIPMOV", notaVenda.get("TIPMOV_DEST"));
		newCabVO.setProperty("DTNEG", TimeUtils.getNow());
		newCabVO.setProperty("DTALTER", TimeUtils.getNow());
		newCabVO.setProperty("HRENTSAI", TimeUtils.getNow());

		StringBuilder obs = new StringBuilder();

		if(notaVenda.get("PEDIDO_ORIG") != null) {
			newCabVO.setProperty("AD_NUNOTAVENDA", notaVenda.get("PEDIDO_ORIG"));

			obs.append("Pedido de Venda: ").append(notaVenda.get("PEDIDO_ORIG"));
			obs.append(", Parceiro: ");
			obs.append(notaVenda.get("CODPARC")).append(" - ").append(notaVenda.get("NOMEPARC"));
			obs.append(", Empresa: ");
			obs.append(notaVenda.get("CODEMP")).append(" - ").append(notaVenda.get("NOMEFANTASIA"));
		} else {
			newCabVO.setProperty("AD_NUNOTAVENDA", notaVenda.get("AD_NUNOTAVENDA"));
		}

		if(notaVenda.get("OBSERVACAO") != null) {
			obs.append(obs.length() > 0 ? "\n---" : "");
			obs.append(notaVenda.get("OBSERVACAO"));
		}

		newCabVO.setProperty("OBSERVACAO", obs.toString());

		PrePersistEntityState cabState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.CABECALHO_NOTA, newCabVO);

		return cabState;
	}

	private PrePersistEntityState createCabecalhoReservaPrePersistEntityState(DynamicVO cabOrigVO, DynamicVO topReservaVO) throws Exception {
		BigDecimal codTopReserva = topReservaVO.asBigDecimal("AD_CODTIPOPERRESERVA");
		BigDecimal codNat = topReservaVO.asBigDecimal("AD_CODNAT");
		BigDecimal codCencus = topReservaVO.asBigDecimal("AD_CODCENCUS");
		BigDecimal codEmpresaMatriz = topReservaVO.asBigDecimal("AD_CODEMP");

		DynamicVO newCabVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.CABECALHO_NOTA);

		newCabVO.setProperty("CODPARC", cabOrigVO.asBigDecimal("CODPARC"));
		newCabVO.setProperty("CODEMP", codEmpresaMatriz);
		newCabVO.setProperty("CODTIPOPER", codTopReserva);
		newCabVO.setProperty("CODNAT", codNat);
		newCabVO.setProperty("CODCENCUS", codCencus);
		newCabVO.setProperty("TIPMOV", "T");
		newCabVO.setProperty("DTNEG", TimeUtils.getNow());
		newCabVO.setProperty("DTALTER", TimeUtils.getNow());
		newCabVO.setProperty("HRENTSAI", TimeUtils.getNow());
		newCabVO.setProperty("AD_NUNOTAVENDA", cabOrigVO.asBigDecimal("NUNOTA"));

		StringBuilder obs = new StringBuilder();

		obs.append("Pedido de Venda: ").append(cabOrigVO.asBigDecimal("NUNOTA"));
		obs.append(", Parceiro: ");
		obs.append(cabOrigVO.asBigDecimal("CODPARC")).append(" - ").append(cabOrigVO.asString("Parceiro.NOMEPARC"));
		obs.append(", Empresa: ");
		obs.append(cabOrigVO.asBigDecimal("CODEMP")).append(" - ").append(cabOrigVO.asString("Empresa.NOMEFANTASIA"));

		if(cabOrigVO.getProperty("OBSERVACAO") != null) {
			obs.append(obs.length() > 0 ? "\n---" : "");
			obs.append(cabOrigVO.asString("OBSERVACAO") );
		}

		newCabVO.setProperty("OBSERVACAO", obs.toString());

		PrePersistEntityState cabState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.CABECALHO_NOTA, newCabVO);

		return cabState;
	}

	public static class MensagemErro {
		String mensagem;
	}
}
