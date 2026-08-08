package br.com.sankhya.industria.manutencao.eventoprogramados;

import java.math.BigDecimal;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoConstants;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;

public class BloqueioEnvioServiceDeskManutencaoSK implements EventoProgramavelJava {
	public void beforeInsert(PersistenceEvent event) throws Exception {
		DynamicVO vo = (DynamicVO) event.getVo();
		BigDecimal remetente = vo.asBigDecimalOrZero("CODUSUREM");
		BigDecimal servico = vo.asBigDecimalOrZero("CODSERV");
		BigDecimal destinatario = vo.asBigDecimalOrZero("CODUSU");
		String nomeDestinatario = NativeSql.getString("NOMEUSU", "TSIUSU", "CODUSU = ?", new Object[] { destinatario });

		boolean ehTesteEntrada = destinatario.compareTo(ManutencaoConstants.FILA_TESTE_ENTRADA) == 0;
		boolean ehServicoIndTeste = servico.compareTo(ManutencaoConstants.SERV_IND_TESTE) == 0;
		boolean ehIndCorrecaoErro = servico.compareTo(ManutencaoConstants.SERV_IND_CORRECAO_ERRO) == 0;

		BigDecimal numOS = vo.asBigDecimal("NUMOS");
		if(ManutencaoSnkUtil.ehUsuarioSD(remetente) && ManutencaoSnkUtil.temSoliman(numOS)) {
			if (ehIndCorrecaoErro || (ehServicoIndTeste && !ehTesteEntrada)) {
				throw new IllegalStateException("Destinatrio invalido (" + destinatario + " - " + nomeDestinatario + "). Para enviar OS de manuteno para a indstria utilize a fila \"46 - TESTE DE ENTRADA\".");
			}
		}
	}

	public void beforeUpdate(PersistenceEvent event) throws Exception {
	}

	public void beforeDelete(PersistenceEvent event) throws Exception {
	}

	public void afterInsert(PersistenceEvent event) throws Exception {
	}

	public void afterUpdate(PersistenceEvent event) throws Exception {
	}

	public void afterDelete(PersistenceEvent event) throws Exception {
	}

	public void beforeCommit(TransactionContext tranCtx) throws Exception {
	}
}
