package br.com.sankhya.industria.manutencao;

import java.math.BigDecimal;
import java.sql.ResultSet;

import com.sankhya.util.BigDecimalUtil;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.industria.manutencao.eventoprogramados.BloqueioAlteracaoOSManutencaoSk;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.servicos.util.RelacionamentoUsuarioUtils;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.workflow.model.engine.DefaultProcessEngineService;
import br.com.sankhya.workflow.model.helper.ListaTarefaHelper;

public class AtribuirDesatribuirTarefa implements AcaoRotinaJava {

	public void doAction(ContextoAcao ctx) throws Exception {

		Registro[] registro = ctx.getLinhas();
		if(registro.length == 0 ) {
			throw new IllegalArgumentException("Selecione uma tarefa para excutar a ao.");
		}else {
			Registro tarefa = registro[0];
			if(tarefa.getCampo("DHCONCLUSAO") != null) {
				throw new IllegalArgumentException("Esta tarefa j foi finalizada, por isto no pode ter seu dono alterado.");
			}

			Object idInstTarefa = tarefa.getCampo("IDINSTTAR");
			Object idInstPrn = tarefa.getCampo("IDINSTPRN");
			BigDecimal donoAnterior = BigDecimalUtil.getBigDecimal(tarefa.getCampo("CODUSUDONO"));
			BigDecimal novoDono = BigDecimalUtil.getBigDecimal(ctx.getParam("NOVODONO"));
			BigDecimal codPrn = NativeSql.getBigDecimal("CODPRN", "TWFIPRN", "IDINSTPRN = ? ", new Object[] { idInstPrn });
			BigDecimal codPrnMan = BigDecimal.valueOf(MGECoreParameter.getParameterAsInt("CODPRNMANSNK"));
			boolean ehProcessoManutenacao = codPrn.compareTo(codPrnMan) == 0;

			if (ehProcessoManutenacao && !verificarLider(ctx.getUsuarioLogado())) {
				throw new IllegalArgumentException("Somente os lderes (QAL/PO/SL) podem utilizar a ao 'Atribuir/desatribuir dono' em solicitaes do processo '219 - SOLICITAO DE MANUTENO'.");
			}

			if (donoAnterior != null) {
				if (donoAnterior.equals(novoDono)) {
					throw new Exception("Novo dono (" + novoDono + ") da tarefa deve ser diferente do dono anterior (" + donoAnterior + ").");
				}

				if (ehProcessoManutenacao) {
					BigDecimal codUsuLog = ctx.getUsuarioLogado();
					if (codUsuLog.intValue() != donoAnterior.intValue() && !RelacionamentoUsuarioUtils.ehGerenteServicos(codUsuLog, donoAnterior)) {
						throw new Exception("Somente gerentes podem alterar itens(sub-OS) de seus subordinados, o executante ("+ donoAnterior +") no consta nessa lista. Verifique a tela \"Relacionamento entre Usurios\".");
					}

					JapeSession.putProperty(BloqueioAlteracaoOSManutencaoSk.IGNORAR_VALIDACAO_FLOW, true);
					JapeSession.putProperty("APONTAMENTO_GESTOR_DONO_ANTERIOR", donoAnterior);
				}

				ListaTarefaHelper listaTarefaHelper = new ListaTarefaHelper();
				listaTarefaHelper.setApontamentoExecTarefa(BigDecimalUtil.getBigDecimal(idInstPrn), BigDecimalUtil.getBigDecimal(idInstTarefa), false);

				DefaultProcessEngineService.getInstance().claimTask(idInstTarefa.toString(), null);
			}

			if(registro.length > 1) {
				throw new Exception("Selecione APENAS uma tarefa para executar a ao 'Atribuir/desatribuir dono'.");
			}

			if(registro.length == 0){
				throw new Exception("Selecione uma tarefa para 'Atribuir/desatribuir dono'.");
			}

			if (novoDono != null) {
				JapeSession.putProperty(BloqueioAlteracaoOSManutencaoSk.IGNORAR_VALIDACAO_FLOW, true);
				DefaultProcessEngineService.getInstance().claimTask(idInstTarefa.toString(), novoDono.toString());
			}
		}
	}

	private boolean verificarLider(BigDecimal usuarioLogado) throws Exception{
		boolean ehLider = Boolean.FALSE;
		JdbcWrapper jdbc = null;
		NativeSql query = null;
		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
			query = new NativeSql(jdbc);

			query.appendSql(" SELECT ");
			query.appendSql(" 	CODUSU ");
			query.appendSql(" FROM ");
			query.appendSql(" 	AD_TSILCP L ");
			query.appendSql(" WHERE ");
			query.appendSql(" 	CODUSU = :CODUSU ");
			query.appendSql(" UNION ");
			query.appendSql(" SELECT ");
			query.appendSql("    CODUSU ");
			query.appendSql(" FROM ");
			query.appendSql("    AD_MEMBCELULA ");
			query.appendSql(" WHERE ");
			query.appendSql(" 	 PAPEL = 0 ");
			query.appendSql("    AND ATUACAO <> 'I' ");
			query.appendSql("    AND CODUSU = :CODUSU ");

			query.setNamedParameter("CODUSU", usuarioLogado);

			ResultSet rs = query.executeQuery();

			ehLider = rs.next();

			rs.close();
		} finally {
			NativeSql.releaseResources(query);
			JdbcWrapper.closeSession(jdbc);
		}

		return ehLider;
	}
}
