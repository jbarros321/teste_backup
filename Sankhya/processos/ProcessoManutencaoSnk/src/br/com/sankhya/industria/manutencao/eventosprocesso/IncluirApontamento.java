package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI.Intervalo;

public class IncluirApontamento implements EventoProcessoJava {

	private static final String	INCLUIR	= "I";
	private static final String	PAUSAR	= "P";
	private static final String	ALTERAR	= "A";
	private static final String	INICIAR	= "S";

	public void executar(ContextoEvento ctx) throws Exception {
		Registro[] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal codUsu = ctx.getUsuarioLogado();

			BigDecimal donoAnterior = JapeSession.getPropertyAsBigDecimal("APONTAMENTO_GESTOR_DONO_ANTERIOR");
			if (donoAnterior != null) {
				codUsu = donoAnterior;
			}

			Timestamp dhInicial = (Timestamp) ctx.getCampo("DHINICIAL");
			Timestamp dhFinal = (Timestamp) ctx.getCampo("DHFINAL");

			if (dhInicial != null) {
				BigDecimal codusu = (BigDecimal) ctx.getCampo("CODUSU");
				BigDecimal pausa = (BigDecimal) ctx.getCampo("INTERVALO");
				Object sequencia = ctx.getCampo("SEQUENCIA");
				String chaveIntegracao = buildChaveIntegracao((BigDecimal) ctx.getCampo("IDINSTPRN"), (BigDecimal) ctx.getCampo("IDINSTTAR"), (BigDecimal) ctx.getCampo("SEQUENCIA"));

				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

				Timestamp inicio = new Timestamp(sdf.parse(sdf.format(dhInicial).toString()).getTime());
				Timestamp fim = dhFinal == null ? null : new Timestamp(sdf.parse(sdf.format(dhFinal).toString()).getTime());
				String descricao = buildChaveSequencia(codusu, sequencia);

				BigDecimal numItem = NativeSql.getBigDecimal("NUMITEM", "TCSITE", "NUMOS = ? AND (AD_INTEGRACAOFLOW = ? OR (SOLUCAO = ? AND AD_INTEGRACAOFLOW IS NULL)) ", new Object[] { numOS, chaveIntegracao, buildChaveSequencia(codusu, sequencia) });
				if(numItem != null){
					try {
						OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

						Map<String, Object> dataIte = new HashMap<String, Object>();

						SimpleDateFormat sdfTime = new SimpleDateFormat("HHmm");

						dataIte.put("HRINICIAL", new BigDecimal(sdfTime.format(inicio)));
						dataIte.put("INICEXEC", TimeUtils.clearTime(inicio));

						if(fim != null){
							dataIte.put("HRFINAL", new BigDecimal(sdfTime.format(fim)));
						}

						if(pausa != null){
							dataIte.put("INTERVALO", pausa);
						}

						OrdemServicoAPI.alterarItem(numOS, numItem, codusu, dataIte);
					} catch (Exception e) {
						Intervalo intervalo = new Intervalo(inicio, fim, pausa, descricao);
						OrdemServicoAPI.alterarApontamento(numOS, numItem, intervalo, codUsu);
					}
				} else {
					numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

					Collection<Intervalo> intervalos = new ArrayList<Intervalo>();
					Intervalo intervalo = new Intervalo(inicio, fim, pausa, descricao);
					intervalo.addProperty("AD_INTEGRACAOFLOW", chaveIntegracao);
					intervalos.add(intervalo);
					OrdemServicoAPI.salvarApontamento(numOS, numItem, intervalos, codUsu);
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
