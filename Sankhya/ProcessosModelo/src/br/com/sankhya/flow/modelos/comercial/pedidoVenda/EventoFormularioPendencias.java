package br.com.sankhya.flow.modelos.comercial.pedidoVenda;

import java.math.BigDecimal;
import java.sql.Timestamp;

import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import com.sankhya.util.BigDecimalUtil;

public class EventoFormularioPendencias implements EventoProcessoJava {

	public void executar(final ContextoEvento ctx) throws Exception {
		if ("L".equals(ctx.getCampo("TIPO"))) {
			atualizaLiberacao(ctx);
		}
	}

	private void atualizaLiberacao(final ContextoEvento ctx) throws Exception {

		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

		BigDecimal nuChave = BigDecimalUtil.getBigDecimal(ctx.getCampo("NUCHAVE"));
		String tabela = (String) ctx.getCampo("TABELA");
		BigDecimal evento = BigDecimalUtil.getBigDecimal(ctx.getCampo("EVENTO"));
		BigDecimal sequencia = BigDecimalUtil.getBigDecimal(ctx.getCampo("SEQUENCIA"));
		BigDecimal seqCascata = BigDecimalUtil.getBigDecimal(ctx.getCampo("SEQCASCATA"));
		BigDecimal nucll = BigDecimalUtil.getBigDecimal(ctx.getCampo("NUCLL"));

		PersistentLocalEntity libEntity = dwfFacade.findEntityByPrimaryKey(DynamicEntityNames.LIBERACAO_LIMITE, new Object[] { nuChave, tabela, evento, sequencia, seqCascata, nucll });

		boolean autorizado = "S".equals(ctx.getCampo("AUTORIZADO"));
		DynamicVO libVO = (DynamicVO) libEntity.getValueObject();
		libVO.setProperty("OBSLIB", String.valueOf((char []) ctx.getCampo("OBSERVACAO")));
		libVO.setProperty("REPROVADO", autorizado ? "N" : "S");

		if (autorizado) {
			libVO.setProperty("DHLIB", new Timestamp(System.currentTimeMillis()));
			libVO.setProperty("CODUSULIB", ctx.getUsuarioLogado());
			libVO.setProperty("VLRLIBERADO", libVO.getProperty("VLRATUAL"));
		} else {
			libVO.setProperty("DHLIB", null);
			libVO.setProperty("CODUSULIB", null);
			libVO.setProperty("VLRLIBERADO", BigDecimal.ZERO);
		}

		libEntity.setValueObject((EntityVO) libVO);
	}
}
