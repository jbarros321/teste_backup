package br.com.sankhya.portfolio.venda.base;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sankhya.util.SQLUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

public class ImportadorVendaBaseHelper {

	private static final BigDecimal COD_FLUXO_PADRAO = new BigDecimal(30);

	private JdbcWrapper jdbc;
	private NativeSql delRepostasPesquisas;
	private NativeSql updPesquisasComoNaoAplicavel;
	private NativeSql sqlProcedimentosJaContratado;
	private NativeSql sqlPerguntaMestraJaContratado;
	private NativeSql sqlPerguntasDadosBasicos;
	private NativeSql sqlPerguntaDadosBasicosRecur;
	private BigDecimal numOS;
	private BigDecimal codParc;
	private BigDecimal codUsu;
	private Map<BigDecimal, BigDecimal> pesquisasByQuestionario;

	public ImportadorVendaBaseHelper(BigDecimal numOS, BigDecimal codParc, BigDecimal codUsu, JdbcWrapper jdbc)
			throws Exception {
		this.jdbc = jdbc;

		this.codParc = codParc;
		this.numOS = numOS;
		this.pesquisasByQuestionario = new HashMap<BigDecimal, BigDecimal>();

		sqlProcedimentosJaContratado = new NativeSql(jdbc);
		sqlProcedimentosJaContratado.setReuseStatements(true);
		sqlProcedimentosJaContratado.loadSql(this.getClass(),
				"ImportarVendaBaseHelper_queProcedimentosJaContratado.sql");

		updPesquisasComoNaoAplicavel = new NativeSql(jdbc);
		updPesquisasComoNaoAplicavel.loadSql(this.getClass(),
				"ImportarVendaBaseHelper_updPesquisasComoNaoAplicavel.sql");

		delRepostasPesquisas = new NativeSql(jdbc);
		delRepostasPesquisas.loadSql(this.getClass(), "ImportarVendaBaseHelper_delRepostasPesquisas.sql");

		sqlPerguntaMestraJaContratado = new NativeSql(jdbc);
		sqlPerguntaMestraJaContratado.loadSql(this.getClass(), "ImportarVendaBaseHelper_quePerguntasMestra.sql");

		sqlPerguntasDadosBasicos = new NativeSql(jdbc);
		sqlPerguntasDadosBasicos.loadSql(this.getClass(), "ImportarVendaBaseHelper_quePerguntasDadosBasicos.sql");

		sqlPerguntaDadosBasicosRecur = new NativeSql(jdbc);
		sqlPerguntaDadosBasicosRecur.loadSql(this.getClass(),
				"ImportarVendaBaseHelper_quePerguntasDadosBasicosRecur.sql");

	}

	public void importar() throws Exception {
		List<RespostaContrato> respostaContratoList = fetchRepostasContratoAtivo();
		List<RespostaContratoMestra> respostaContratoMestraList = fetchRespostasMestraPrincipal(respostaContratoList);

		List<RespostaDadosBasicos> respostaDadosBasicosList = fetchRespostasDadosBasicos();

		createOrUpdatePesquisas();
		responderPesquisas(respostaContratoList);
		responderPesquisasMestra(respostaContratoMestraList);
		responderPesquisasDadosBasicos(respostaDadosBasicosList);
	}

	private void responderPesquisasDadosBasicos(List<RespostaDadosBasicos> respostaDadosBasicosList) throws Exception {

		for (RespostaDadosBasicos resp : respostaDadosBasicosList) {
			doResponderDadosBasicos(resp);
		}

	}

	private void doResponderDadosBasicos(RespostaDadosBasicos resp) throws Exception {
		BigDecimal nuPesq = pesquisasByQuestionario.get(resp.getCodQuest());

		if (nuPesq == null) {
			throw new IllegalStateException("Resposta [CodPerg: " + resp.getCodPerg() + ", CodResp: "
					+ resp.getCodRespNaoOrVendaBase() + "] importada do contrato sem pesquisa para ser aplicada.");
		}

		JapeWrapper dao = JapeFactory.dao("RespostaPerguntaPesquisa");

		BigDecimal CODRESP = resp.getCodRespNaoOrVendaBase() != null ?  resp.getCodRespNaoOrVendaBase()  : resp.getCodRespTipoVendaContrato();

		dao.create().set("NUPESQ", nuPesq).set("CODPERG", resp.getCodPerg())
				.set("CODRESP", CODRESP).set("CODUSU", codUsu).set("NOTA", BigDecimal.ZERO)
				.set("DHALTER", TimeUtils.getNow()).set("PROIBELIMPEZA", "S").save();

	}

	private List<RespostaDadosBasicos> fetchRespostasDadosBasicos() throws Exception {
		List<RespostaDadosBasicos> respostaDadosBasicosList = new ArrayList<RespostaDadosBasicos>();

		sqlPerguntasDadosBasicos.setNamedParameter("CODPARC", this.codParc);

		sqlPerguntasDadosBasicos.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

		ResultSet resultDadosBasicos = sqlPerguntasDadosBasicos.executeQuery();

		while (resultDadosBasicos.next()) {
			RespostaDadosBasicos resp = new RespostaDadosBasicos();
			resp.setCodQuest(resultDadosBasicos.getBigDecimal("CODQUEST"));
			resp.setCodPerg(resultDadosBasicos.getBigDecimal("CODPERG"));
			resp.setCodRespNaoOrVendaBase(resultDadosBasicos.getBigDecimal("CODRESP"));
			resp.setCodRespTipoVendaContrato(resultDadosBasicos.getBigDecimal("CODRESP_TIPO_VENDA_CONTRATO"));

			if (resp.getCodRespNaoOrVendaBase() != null) {
				if (!respostaDadosBasicosList.contains(resp)) {
					respostaDadosBasicosList.add(resp);
					pesquisasByQuestionario.put(resp.getCodQuest(), null);
					recursiveFetchDadosBasicos(respostaDadosBasicosList, resp);
				}

			} else if (resp.getCodRespTipoVendaContrato() != null) {

				if (!respostaDadosBasicosList.contains(resp)) {
					respostaDadosBasicosList.add(resp);
					pesquisasByQuestionario.put(resp.getCodQuest(), null);
				}

				recursiveFetchDadosBasicos(respostaDadosBasicosList, resp);
			}

			else {

				recursiveFetchDadosBasicos(respostaDadosBasicosList, resp);
			}
		}

		return respostaDadosBasicosList;
	}

	private void recursiveFetchDadosBasicos(List<RespostaDadosBasicos> respostaDadosBasicosList,
			RespostaDadosBasicos resp) throws Exception {

		sqlPerguntaDadosBasicosRecur.setNamedParameter("CODPERGMESTRE", resp.getCodPerg());
		ResultSet result = sqlPerguntaDadosBasicosRecur.executeQuery();

		while (result.next()) {
			RespostaDadosBasicos respDadosBasicos = new RespostaDadosBasicos();
			if(resp.getCodQuest() != null) {
				respDadosBasicos.setCodQuest(resp.getCodQuest());
			}else {
				respDadosBasicos.setCodQuest(result.getBigDecimal("CODQUEST"));
			}
			respDadosBasicos.setCodPerg(result.getBigDecimal("CODPERGDEP"));
			respDadosBasicos.setCodRespNaoOrVendaBase(result.getBigDecimal("CODRESP"));

			if (respDadosBasicos.getCodRespNaoOrVendaBase() != null) {
				if (!respostaDadosBasicosList.contains(respDadosBasicos)) {
					respostaDadosBasicosList.add(respDadosBasicos);
					pesquisasByQuestionario.put(respDadosBasicos.getCodQuest(), null);
				}
			} else {
				recursiveFetchDadosBasicos(respostaDadosBasicosList, respDadosBasicos);
			}

		}

	}

	private void responderPesquisasMestra(List<RespostaContratoMestra> respostaContratoMestraList) throws Exception {
		for (RespostaContratoMestra resp : respostaContratoMestraList) {
			doResponderPesquisaMestra(resp);
		}

	}

	private void doResponderPesquisaMestra(RespostaContratoMestra resp) throws Exception {

		BigDecimal nuPesq = pesquisasByQuestionario.get(resp.getCodQuest());

		JapeWrapper dao = JapeFactory.dao("RespostaPerguntaPesquisa");

		dao.create().set("NUPESQ", nuPesq).set("CODPERG", resp.getCodPerg()).set("CODRESP", resp.getCodRespSim())
				.set("CODUSU", codUsu).set("NOTA", BigDecimal.ZERO).set("DHALTER", TimeUtils.getNow())
				.set("PROIBELIMPEZA", "S").save();

	}

	private void responderPesquisas(List<RespostaContrato> respostaContratoList) throws Exception {
		for (RespostaContrato resp : respostaContratoList) {
			doResponderPesquisa(resp);
		}

		updatePesquisasComoAplicavel();
	}

	private void doResponderPesquisa(RespostaContrato resp) throws Exception {

		BigDecimal nuPesq = pesquisasByQuestionario.get(resp.getCodQuest());

		if (nuPesq == null) {
			throw new IllegalStateException("Resposta [CodPerg: " + resp.getCodPerg() + ", CodResp: "
					+ resp.getCodRespContratado() + "] importada do contrato sem pesquisa para ser aplicada.");
		}

		JapeWrapper dao = JapeFactory.dao("RespostaPerguntaPesquisa");

		dao.create().set("NUPESQ", nuPesq).set("CODPERG", resp.getCodPerg()).set("CODRESP", resp.getCodRespContratado())
				.set("CODUSU", codUsu).set("NOTA", BigDecimal.ZERO).set("DHALTER", TimeUtils.getNow())
				.set("PROIBELIMPEZA", "S").save();

		dao.create().set("NUPESQ", nuPesq).set("CODPERG", resp.getCodPerg()).set("CODRESP", resp.getCodRespNao())
				.set("CODUSU", codUsu).set("NOTA", BigDecimal.ZERO).set("DHALTER", TimeUtils.getNow())
				.set("PROIBELIMPEZA", "S").save();
	}

	private List<RespostaContrato> fetchRepostasContratoAtivo() throws Exception {

		sqlProcedimentosJaContratado.setNamedParameter("CODPARC", codParc);
		sqlProcedimentosJaContratado.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

		ResultSet rsProcedimentosJaContratado = sqlProcedimentosJaContratado.executeQuery();

		List<RespostaContrato> respostaContratoList = new ArrayList<RespostaContrato>();

		while (rsProcedimentosJaContratado.next()) {
			RespostaContrato resp = new RespostaContrato();
			resp.setCodPerg(rsProcedimentosJaContratado.getBigDecimal("CODPERG"));
			resp.setCodQuest(rsProcedimentosJaContratado.getBigDecimal("CODQUEST"));
			resp.setCodRespContratado(rsProcedimentosJaContratado.getBigDecimal("CODRESP_CONTRATADO"));
			resp.setCodRespNao(rsProcedimentosJaContratado.getBigDecimal("CODRESP_NAO"));

			pesquisasByQuestionario.put(resp.getCodQuest(), null);

			respostaContratoList.add(resp);
		}

		if (codParc == null) {
			throw new IllegalStateException("Parceiro no encontrado para Venda Base - NP.");
		}

		if (respostaContratoList.isEmpty()) {
			throw new IllegalStateException(
					"Parceiro (" + codParc + ") no possui contrato no modelo do Novo Portflio.");
		}

		return respostaContratoList;
	}

	private List<RespostaContratoMestra> fetchRespostasMestraPrincipal(List<RespostaContrato> respostaContratoList)
			throws Exception {

		List<RespostaContratoMestra> respostaContratoMestraList = new ArrayList<RespostaContratoMestra>();

		for (RespostaContrato rp : respostaContratoList) {
			sqlPerguntaMestraJaContratado.setNamedParameter("CODPERGDEP", rp.getCodPerg());

			ResultSet result = sqlPerguntaMestraJaContratado.executeQuery();

			while (result.next()) {
				RespostaContratoMestra resp = new RespostaContratoMestra();
				resp.setCodPerg(result.getBigDecimal("CODPERGMESTRE"));
				resp.setCodRespSim(result.getBigDecimal("CODRESP"));
				resp.setCodQuest(result.getBigDecimal("CODQUEST"));

				if (!respostaContratoMestraList.contains(resp)) {
					respostaContratoMestraList.add(resp);
				}

			}

		}

		return respostaContratoMestraList;
	}

	private boolean existsFluxoDiagnosticoPadrao() throws Exception {

		NativeSql query = new NativeSql(jdbc);
		query.appendSql("SELECT 1 FROM TCSFXO WHERE NUMOS = :NUMOS AND CODFLD = :CODFLD");
		query.setNamedParameter("NUMOS", numOS);
		query.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

		ResultSet rs = query.executeQuery();

		if (rs.next()) {
			return true;
		}

		return false;
	}

	private void createFluxoDiagnosticoPadrao() throws Exception {

		JapeWrapper dao = JapeFactory.dao("FluxoDiagnosticoOS");

		dao.create().set("NUMOS", numOS).set("CODFLD", COD_FLUXO_PADRAO).set("AD_MAHA", "N").save();
	}

	private void updatePesquisasComoAplicavel() throws Exception {
		NativeSql updPesquisasComoAplicavel = new NativeSql(jdbc);
		updPesquisasComoAplicavel.setReuseStatements(true);
		updPesquisasComoAplicavel.appendSql("UPDATE TCSPOS SET APLICAVEL = 'S' WHERE ");
		updPesquisasComoAplicavel.appendSql(SQLUtils.buildINClauseByValues("NUPESQ", pesquisasByQuestionario.values()));

		updPesquisasComoAplicavel.executeUpdate();
	}

	private void updatePesquisasComoNaoAplicavel() throws Exception {

		updPesquisasComoNaoAplicavel.setNamedParameter("NUMOS", numOS);
		updPesquisasComoNaoAplicavel.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

		updPesquisasComoNaoAplicavel.executeUpdate();

		NativeSql.releaseResources(updPesquisasComoNaoAplicavel);
	}

	private void deleteRepostasPesquisas() throws Exception {

		delRepostasPesquisas.setNamedParameter("NUMOS", numOS);
		delRepostasPesquisas.setNamedParameter("CODFLD", COD_FLUXO_PADRAO);

		delRepostasPesquisas.executeUpdate();

		NativeSql.releaseResources(delRepostasPesquisas);
	}

	private void createPesquisas() throws Exception {

		for (Entry<BigDecimal, BigDecimal> entry : pesquisasByQuestionario.entrySet()) {
			BigDecimal codQuest = entry.getKey();
			BigDecimal nuPesq = doCreatePesquisa(codQuest);

			entry.setValue(nuPesq);
		}

	}

	private BigDecimal doCreatePesquisa(BigDecimal codQuest) throws Exception {

		Timestamp now = new Timestamp(System.currentTimeMillis());

		JapeWrapper daoPesquisa = JapeFactory.dao("Pesquisa");

		DynamicVO pesquisaVO = daoPesquisa.create().set("DTAPLICACAO", now).set("DHALTER", now).set("CODUSU", codUsu)
				.set("TIPOPESQ", "DP").set("CODQUEST", codQuest).set("NUPLA", BigDecimal.ZERO)
				.set("HORAAPLICACAO", BigDecimal.ZERO).save();

		BigDecimal nuPesq = pesquisaVO.asBigDecimal("NUPESQ");

		JapeWrapper daoPesquisaOS = JapeFactory.dao("PesquisaOrdemServico");

		daoPesquisaOS.create().set("NUPESQ", nuPesq).set("NUMOS", numOS).set("NUMITEM", BigDecimal.ZERO)
				.set("CODFLD", COD_FLUXO_PADRAO).set("APLICAVEL", "N").save();

		return nuPesq;
	}

	private void createOrUpdatePesquisas() throws Exception {

		if (!existsFluxoDiagnosticoPadrao()) {
			createFluxoDiagnosticoPadrao();
		}

		updatePesquisasComoNaoAplicavel();
		deleteRepostasPesquisas();
		createPesquisas();
	}
}
