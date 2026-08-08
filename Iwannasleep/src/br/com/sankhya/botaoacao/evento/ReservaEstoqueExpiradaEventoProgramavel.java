package br.com.sankhya.botaoacao.evento;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ReservaEstoqueExpiradaEventoProgramavel implements EventoProgramavelJava {

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		DynamicVO tgfvarVO = (DynamicVO) event.getVo();
		BigDecimal nunotaOrig = tgfvarVO.asBigDecimal("NUNOTAORIG");
		BigDecimal nunotaDest = tgfvarVO.asBigDecimal("NUNOTA");

		DynamicVO notaOrigVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, new Object[] { nunotaOrig });
		DynamicVO notaDestVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, new Object[] { nunotaDest });

		if("S".equals(notaOrigVO.asString("AD_RESERVAEXPIROU"))) {
			DynamicVO topVO = (DynamicVO) notaOrigVO.getProperty("TipoOperacao");

			BigDecimal codTopReserva = topVO.asBigDecimal("AD_CODTIPOPERRESERVA");
			BigDecimal codLocalDestino = topVO.asBigDecimal("AD_CODLOCALRESERVA");

			if(codTopReserva != null && codLocalDestino != null) {

				if("V".equals(notaDestVO.asString("TIPMOV"))) {
					throw new IllegalStateException("Esse pedido expirou a sua reserva de estoque. No ser possvel fatur-lo.");
				}
			}
		}
	}

	@Override
	public void afterUpdate(PersistenceEvent event) throws Exception {

	}

	@Override
	public void beforeDelete(PersistenceEvent event) throws Exception {

	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {

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
