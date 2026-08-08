package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoSnkUtil;
import br.com.sankhya.industria.manutencao.util.OrdemServicoAPICover;
import br.com.sankhya.industria.manutencao.util.OrdemServicoAPICover.IntervaloCover;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI.Intervalo;

public class IncluirApontamento implements EventoProcessoJava {

	private static final String	INCLUIR	= "I";
	private static final String	PAUSAR	= "P";
	private static final String	ALTERAR	= "A";

	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal codUsu = ManutencaoSnkUtil.getUsuarioUltimoItemOS(numOS);

			Timestamp dhInicial = (Timestamp) ctx.getCampo("DHINICIAL");
			Timestamp dhFinal = (Timestamp) ctx.getCampo("DHFINAL");

			if (dhInicial != null && dhFinal != null) {
				BigDecimal codusu = (BigDecimal) ctx.getCampo("CODUSU");
				BigDecimal pausa = (BigDecimal) ctx.getCampo("INTERVALO");
				Object sequencia = ctx.getCampo("SEQUENCIA");
				String chaveIntegracao = buildChaveIntegracao((BigDecimal) ctx.getCampo("IDINSTPRN"), (BigDecimal) ctx.getCampo("IDINSTTAR"), (BigDecimal) ctx.getCampo("SEQUENCIA"));

				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

				Timestamp inicio = new Timestamp(sdf.parse(sdf.format(dhInicial).toString()).getTime());
				Timestamp fim = new Timestamp(sdf.parse(sdf.format(dhFinal).toString()).getTime());
				String descricao = buildChaveSequencia(codusu, sequencia);

				String tipoAcao = ctx.getAcao();

				if (ALTERAR.equals(tipoAcao)) {
					BigDecimal numItem = NativeSql.getBigDecimal("NUMITEM", "TCSITE", "NUMOS = ? AND (AD_INTEGRACAOFLOW = ? OR (SOLUCAO = ? AND AD_INTEGRACAOFLOW IS NULL)) ", new Object[] { numOS, chaveIntegracao, buildChaveSequencia(codusu, sequencia) });
					if (numItem == null) {
						tipoAcao = INCLUIR;
					} else {
						Intervalo intervalo = new Intervalo(inicio, fim, pausa, descricao);
						OrdemServicoAPI.alterarApontamento(numOS, numItem, intervalo, codUsu);
					}
				}

				if (INCLUIR.equals(tipoAcao) || PAUSAR.equals(tipoAcao)) {
					BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

					Collection<IntervaloCover> intervalos = new ArrayList<IntervaloCover>();
					IntervaloCover intervalo = new IntervaloCover(inicio, fim, pausa, descricao);
					intervalo.addProperty("AD_INTEGRACAOFLOW", chaveIntegracao);
					intervalos.add(intervalo);
					OrdemServicoAPICover.salvarApontamento(numOS, numItem, intervalos, codUsu);
				}
			}
		}
	}

	private String buildChaveIntegracao(BigDecimal idinstprn, BigDecimal idinsttar, BigDecimal sequencia) {
		return String.format("FLOW:P%s:T%s:S%s", idinstprn, idinsttar, sequencia);
	}

	private String buildChaveSequencia(BigDecimal codUsu, Object sequencia) {
		return "APONTAMENTO FLOW: [" + codUsu + "]-[" + sequencia + "]";
	}
}
