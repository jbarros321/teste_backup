package br.com.sankhya.flow.modelos.comercial.pedidoVenda;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.ejb.FinderException;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.NewTXBody;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class EventoFinalizaLiberacao implements EventoProcessoJava {

	public void executar (ContextoEvento ctx) throws Exception {

		EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

		for(Registro lib:ctx.getLinhasFormulario("AD_LIBERACOESVENDA")) {

			try {
				BigDecimal nuChave = (BigDecimal) lib.getCampo("NUCHAVE");
				String tabela = (String) lib.getCampo("TABELA");
				BigDecimal evento = (BigDecimal) lib.getCampo("EVENTO");
				BigDecimal sequencia = (BigDecimal) lib.getCampo("SEQUENCIA");
				BigDecimal seqCascata = (BigDecimal) lib.getCampo("SEQCASCATA");
				BigDecimal nucll = (BigDecimal) lib.getCampo("NUCLL");
				String obs = (String) lib.getCampo("OBSERVACAO");
				boolean reprovado = "N".equals(lib.getCampo("AUTORIZADO"));
				String descrEvento = NativeSql.getString("DESCRICAO", "VGFLIBEVE", "EVENTO = ?", new Object [] {evento});

				PersistentLocalEntity libEntity = dwfFacade.findEntityByPrimaryKey(DynamicEntityNames.LIBERACAO_LIMITE, new Object [] {nuChave, tabela, evento, sequencia, seqCascata, nucll});

				DynamicVO libVO = (DynamicVO) libEntity.getValueObject();
				if(libVO.getProperty("DHLIB") != null){
					continue;
				}

				libVO.setProperty("OBSLIB", obs);
				libVO.setProperty("DHLIB", new Timestamp(System.currentTimeMillis()));
				libVO.setProperty("CODUSULIB", ctx.getUsuarioLogado());
				libVO.setProperty("REPROVADO", reprovado ? "S" : "N");

				if(reprovado) {
					libVO.setProperty("VLRLIBERADO", BigDecimal.ZERO);
					Registro pendencia = ctx.novaLinha("AD_PENDENCIASVENDA");
					pendencia.setCampo("IDINSTPRN", ctx.getIdInstanceProcesso());
					pendencia.setCampo("IDINSTTAR", BigDecimalUtil.ZERO_VALUE);
					pendencia.setCampo("TIPO", "L");
					StringBuffer buf = new StringBuffer();
					buf.append("Solicitao reprovada: ");
					buf.append("\nEvento: ").append(evento).append(" - ").append(descrEvento);
					buf.append("\nValor solicitado: ").append(lib.getCampo("VLRATUAL"));
					buf.append("\nObservao do liberador: ").append(obs);
					pendencia.setCampo("DETALHESERRO",  buf.toString() );

					pendencia.save();
				} else {
					libVO.setProperty("VLRLIBERADO", lib.getCampo("VLRATUAL"));
				}

				libEntity.setValueObject((EntityVO) libVO);

			} catch(FinderException ignored) {}

		}
	}
}
