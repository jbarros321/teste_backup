package br.com.sankhya.botaoacao.evento;

import java.math.BigDecimal;

import br.com.sankhya.botaoacao.AutomatizacaoProcessosHelper;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.AtributosRegras;

public class ReservaEstoqueMatrizEventoProgramavel implements EventoProgramavelJava {

	private static final String SEM_EMPRESA_MATRIZ_MSG = "A TOP {0} foi configurada para reservar o estoque na Matriz mas no foi informado nela qual o cdigo da empresa Matriz.\n\nConfigure o campo 'Cd. Empresa Matriz' na TOP antes de confirmar essa venda.";
	private static final String SEM_LOCAL_MATRIZ_MSG = "A TOP {0} foi configurada para reservar o estoque na Matriz mas no foi informado nela qual o cdigo do local onde esto os produtos na Matriz.\n\nConfigure o campo 'Cd. Local Destino' na TOP antes de confirmar essa venda.";

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {

		DynamicVO notaVO = (DynamicVO) event.getVo();
		notaVO.setProperty("AD_RESERVAEXPIROU", "N");
		notaVO.setProperty("AD_PROCESSADOAUTOMACAO", "N");
	}

	@Override
	public void afterUpdate(PersistenceEvent event) throws Exception {
		boolean ehConfirmacao = JapeSession.getPropertyAsBoolean(AtributosRegras.CONFIRMANDO, Boolean.FALSE);

		if (ehConfirmacao) {
			DynamicVO pedidoVO = (DynamicVO) event.getVo();
			DynamicVO topVO = (DynamicVO) pedidoVO.getProperty("TipoOperacao");

			BigDecimal codTopReserva = topVO.asBigDecimal("AD_CODTIPOPERRESERVA");
			BigDecimal codLocalOrigem = topVO.asBigDecimal("AD_CODLOCAL");
			BigDecimal codLocalDestino = topVO.asBigDecimal("AD_CODLOCALRESERVA");
			BigDecimal codEmpresaMatriz = topVO.asBigDecimal("AD_CODEMP");

			if(codTopReserva != null && codLocalDestino != null) {
				if(codEmpresaMatriz == null) {
					String msg = SEM_EMPRESA_MATRIZ_MSG.replace("{0}", topVO.asBigDecimal("CODTIPOPER").toString());
					throw new IllegalStateException(msg);
				}

				if(codLocalOrigem == null) {
					String msg = SEM_LOCAL_MATRIZ_MSG.replace("{0}", topVO.asBigDecimal("CODTIPOPER").toString());
					throw new IllegalStateException(msg);
				}

				AutomatizacaoProcessosHelper automatizacao = new AutomatizacaoProcessosHelper(null);

				BigDecimal nunotaPedido = pedidoVO.asBigDecimal("NUNOTA");
				BigDecimal nunotaReserva = automatizacao.getReservaEstoquePeloPedido(nunotaPedido, codTopReserva);

				if(nunotaReserva == null) {
					automatizacao.reservarEstoqueMatriz(pedidoVO, topVO);
				}
			}
		}
	}

	@Override
	public void beforeDelete(PersistenceEvent event) throws Exception {
		DynamicVO pedidoVO = (DynamicVO) event.getVo();
		DynamicVO topVO = (DynamicVO) pedidoVO.getProperty("TipoOperacao");

		BigDecimal codTopReserva = topVO.asBigDecimal("AD_CODTIPOPERRESERVA");
		BigDecimal codLocalDestino = topVO.asBigDecimal("AD_CODLOCALRESERVA");

		if(codTopReserva != null && codLocalDestino != null) {
			BigDecimal nunotaOrig = pedidoVO.asBigDecimal("NUNOTA");

			AutomatizacaoProcessosHelper automatizacao = new AutomatizacaoProcessosHelper(null);
			automatizacao.removerReservaEstoqueMatriz(nunotaOrig, codTopReserva);
		}
	}

	@Override
	public void afterInsert(PersistenceEvent event) throws Exception {

	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {

	}

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {

	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {

	}
}
