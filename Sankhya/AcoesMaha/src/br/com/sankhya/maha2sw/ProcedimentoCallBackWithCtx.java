package br.com.sankhya.maha2sw;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

public class ProcedimentoCallBackWithCtx implements Diagnostico.ProcedimentoCallBack {

	private static final BigDecimal 			CODIGO_FLUXO 	= new BigDecimal(30);

	private NativeSql							sqlQuest;
	private NativeSql							updTCSPOS;

	private NativeSql							sqlPerguntaMestre;
	private NativeSql							sqlPerguntasRespostaMaha;
	private NativeSql							sqlPerguntasRespostaSimples;
	private NativeSql							sqlPerguntasComRespostaUnica;

	private ContextoAcao						ctx;
	private JdbcWrapper							jdbc;
	private BigDecimal							numOS;
	private Map<BigDecimal, RespostaPergunta>	respostasMahaPorPergunta;
	private Map<BigDecimal, RespostaPergunta>	respostasSimplesPorPergunta;

	public ProcedimentoCallBackWithCtx(ContextoAcao ctx, JdbcWrapper jdbc) throws Exception {
		this.ctx = ctx;
		this.jdbc = jdbc;

		respostasMahaPorPergunta = new HashMap<BigDecimal, RespostaPergunta>();
		respostasSimplesPorPergunta = new HashMap<BigDecimal, RespostaPergunta>();
		numOS = ((BigDecimal) ctx.getLinhaPai().getCampo("NUMOS"));

		sqlQuest = new NativeSql(jdbc);
		sqlQuest.setReuseStatements(true);
		sqlQuest.appendSql("SELECT DISTINCT PES.NUPESQ FROM TPQPES PES, TCSPOS POS, AD_TPQQMH QMH ");
		sqlQuest.appendSql("WHERE POS.NUMOS = :NUMOS AND POS.CODFLD = :CODFLD AND POS.NUPESQ = PES.NUPESQ ");
		sqlQuest.appendSql("AND QMH.CODQUEST = PES.CODQUEST AND QMH.CODMAHA = :CODMAHA ");

		updTCSPOS = new NativeSql(jdbc);
		updTCSPOS.setReuseStatements(true);
		updTCSPOS.appendSql("UPDATE TCSPOS SET APLICAVEL = 'S' WHERE NUPESQ = :NUPESQ");

		sqlPerguntasRespostaSimples = new NativeSql(jdbc);
		sqlPerguntasRespostaSimples.setReuseStatements(true);
		sqlPerguntasRespostaSimples.loadSql(this.getClass(), "Questionario_quePerguntasRespostaSimples.sql");

		sqlPerguntasComRespostaUnica = new NativeSql(jdbc);
		sqlPerguntasComRespostaUnica.setReuseStatements(true);
		sqlPerguntasComRespostaUnica.loadSql(this.getClass(), "Questionario_quePerguntasComRespostaUnica.sql");

		sqlPerguntasRespostaMaha = new NativeSql(jdbc);
		sqlPerguntasRespostaMaha.setReuseStatements(true);
		sqlPerguntasRespostaMaha.loadSql(this.getClass(), "Questionario_quePerguntasRespostaMaha.sql");

		sqlPerguntaMestre = new NativeSql(jdbc);
		sqlPerguntaMestre.setReuseStatements(true);
		sqlPerguntaMestre.loadSql(this.getClass(), "Questionario_quePerguntaMestre.sql");
	}

	public void onStart() throws Exception {

		QueryExecutor query = ctx.getQuery();

		StringBuilder sqlFluxoOS = new StringBuilder();
		sqlFluxoOS.append("SELECT 1 FROM TCSFXO WHERE NUMOS = {NUMOS} AND CODFLD = {CODFLD} ");
		query.setParam("NUMOS", numOS);
		query.setParam("CODFLD", CODIGO_FLUXO);
		query.nativeSelect(sqlFluxoOS.toString());

		if (!query.next()) {
			Registro tcsfxo = ctx.novaLinha("TCSFXO");
			tcsfxo.setCampo("NUMOS", numOS);
			tcsfxo.setCampo("CODFLD", CODIGO_FLUXO);
			tcsfxo.setCampo("AD_MAHA", "S");
			tcsfxo.save();
		}
		query.reset();

		NativeSql updAplicavel = new NativeSql(jdbc);
		updAplicavel.appendSql("UPDATE TCSPOS SET APLICAVEL = 'N' ");
		updAplicavel.appendSql("WHERE NUMOS = :NUMOS AND CODFLD = :CODFLD ");
		updAplicavel.appendSql("AND NOT EXISTS (SELECT 1 FROM TPQPES PES, TPQQUE QUE ");
		updAplicavel.appendSql("				WHERE TCSPOS.NUPESQ = PES.NUPESQ ");
		updAplicavel.appendSql("				AND PES.CODQUEST = QUE.CODQUEST ");
		updAplicavel.appendSql("				AND QUE.DESCRQUEST = 'Informaes Gerais') ");
		updAplicavel.setNamedParameter("NUMOS", numOS);
		updAplicavel.setNamedParameter("CODFLD", CODIGO_FLUXO);
		updAplicavel.executeUpdate();
		NativeSql.releaseResources(updAplicavel);

		NativeSql delTPQRPE = new NativeSql(jdbc);
		delTPQRPE.appendSql("DELETE FROM TPQRPE  ");
		delTPQRPE.appendSql("WHERE NUPESQ IN (SELECT DISTINCT NUPESQ FROM TCSPOS POS WHERE POS.NUMOS = :NUMOS) ");
		delTPQRPE.appendSql("AND TEXTO IS NULL ");
		delTPQRPE.appendSql("AND NOT EXISTS (SELECT 1 FROM TPQPES PES, TPQQUE QUE ");
		delTPQRPE.appendSql("				WHERE PES.NUPESQ = TPQRPE.NUPESQ ");
		delTPQRPE.appendSql("				AND PES.CODQUEST = QUE.CODQUEST ");
		delTPQRPE.appendSql("				AND QUE.DESCRQUEST = 'Informaes Gerais') ");
		delTPQRPE.setNamedParameter("NUMOS", numOS);
		delTPQRPE.setNamedParameter("CODFLD", CODIGO_FLUXO);
		delTPQRPE.executeUpdate();
		NativeSql.releaseResources(delTPQRPE);

		StringBuilder sqlCodQuest = new StringBuilder();
		sqlCodQuest.append("SELECT CODQUEST FROM TCSQXF ");
		sqlCodQuest.append("WHERE CODFLD = {CODFLD} AND CODQUEST NOT IN ");
		sqlCodQuest.append(" (SELECT DISTINCT PES.CODQUEST FROM TPQPES PES, TCSPOS POS ");
		sqlCodQuest.append("  WHERE POS.NUMOS = {NUMOS} AND POS.CODFLD = {CODFLD} AND POS.NUPESQ = PES.NUPESQ) ");
		query.setParam("NUMOS", numOS);
		query.setParam("CODFLD", CODIGO_FLUXO);
		query.nativeSelect(sqlCodQuest.toString());

		while (query.next()) {
			BigDecimal codQuest = query.getBigDecimal(1);
			createPesquisa(codQuest);
		}

		query.reset();
	}

	private void createPesquisa(BigDecimal codQuest) throws Exception {
		Registro tpqpes = ctx.novaLinha("TPQPES");
		tpqpes.setCampo("DTAPLICACAO", new Timestamp(System.currentTimeMillis()));
		tpqpes.setCampo("DHALTER", new Timestamp(System.currentTimeMillis()));
		tpqpes.setCampo("CODUSU", ctx.getUsuarioLogado());
		tpqpes.setCampo("TIPOPESQ", "DP");
		tpqpes.setCampo("CODQUEST", codQuest);
		tpqpes.setCampo("NUPLA", Integer.valueOf(0));
		tpqpes.setCampo("HORAAPLICACAO", Integer.valueOf(0));
		tpqpes.save();

		BigDecimal nuPesq = (BigDecimal) tpqpes.getCampo("NUPESQ");

		Registro tcspos = ctx.novaLinha("TCSPOS");
		tcspos.setCampo("NUPESQ", nuPesq);
		tcspos.setCampo("NUMOS", numOS);
		tcspos.setCampo("NUMITEM", Integer.valueOf(0));
		tcspos.setCampo("CODFLD", CODIGO_FLUXO);
		tcspos.setCampo("APLICAVEL", "N");
		tcspos.save();

		processaPerguntasComRespostaUnica(nuPesq);
	}

	private void processaPerguntasComRespostaUnica(BigDecimal nuPesq) throws Exception {
		sqlPerguntasComRespostaUnica.setNamedParameter("NUPESQ", nuPesq);

		ResultSet rset = null;

		try {
			rset = sqlPerguntasComRespostaUnica.executeQuery();

			while (rset.next()) {
				BigDecimal codPerg = rset.getBigDecimal("CODPERG");
				BigDecimal codResp = rset.getBigDecimal("CODRESP");

				insertRespostaQuestionario(nuPesq, codPerg, codResp);
			}
		} finally {
			if (rset != null) {
				rset.close();
			}
		}
	}

	public void onFinish() throws Exception {

		for(RespostaPergunta respPergunta : respostasMahaPorPergunta.values()) {
			insertRespostaQuestionario(respPergunta.nuPesq, respPergunta.codPerg, respPergunta.codResp, true);
		}

		for(RespostaPergunta respPergunta : respostasSimplesPorPergunta.values()) {
			insertRespostaQuestionario(respPergunta.nuPesq, respPergunta.codPerg, respPergunta.codResp, true);
		}
	}

	public void onProcesso(Processo processo) throws Exception {
		sqlQuest.setNamedParameter("CODMAHA", Integer.valueOf(processo.getCodigoSankhya()));
		sqlQuest.setNamedParameter("NUMOS", numOS);
		sqlQuest.setNamedParameter("CODFLD", CODIGO_FLUXO);

		ResultSet rset = null;

		try {
			rset = sqlQuest.executeQuery();

			if (rset.next()) {
				updTCSPOS.setNamedParameter("NUPESQ", rset.getBigDecimal("NUPESQ"));
				updTCSPOS.executeUpdate();
			}
		} finally {
			if (rset != null) {
				rset.close();
			}
		}
	}

	public void onAtividade(Atividade ativ, Processo processo) throws Exception {

	}

	public void onProcedimento(Procedimento procedimento, Atividade ativ, Processo processo) throws Exception {
		ModoExecucao modoExecucao = new ModoExecucao(procedimento.getModo());

		processaRespostasMaha(procedimento.getCodigoSankhya(), modoExecucao);
		processaRepostasSimples(procedimento.getCodigoSankhya(), modoExecucao);
	}

	private void processaRespostasMaha(int codigoMaha, ModoExecucao modo) throws Exception {

		sqlPerguntasRespostaMaha.setNamedParameter("NUMOS", numOS);
		sqlPerguntasRespostaMaha.setNamedParameter("CODMAHA", codigoMaha);
		sqlPerguntasRespostaMaha.setNamedParameter("RESPOSTA", modo.getRespostaMaha());

		ResultSet rset = null;

		try {
			rset = sqlPerguntasRespostaMaha.executeQuery();

			while (rset.next()) {
				BigDecimal nuPesq = rset.getBigDecimal("NUPESQ");
				BigDecimal codPerg = rset.getBigDecimal("CODPERG");
				BigDecimal codResp = rset.getBigDecimal("CODRESP");

				if( ! respostasMahaPorPergunta.containsKey(codPerg)) {

					respostasMahaPorPergunta.put(codPerg, new RespostaPergunta(nuPesq, codPerg, codResp));
				} else if(ModoExecucao.RESP_MAHA_SIM.equals(modo.getRespostaMaha())) {

					respostasMahaPorPergunta.put(codPerg, new RespostaPergunta(nuPesq, codPerg, codResp));
				}
			}
		} finally {
			if (rset != null) {
				rset.close();
			}
		}
	}

	private void processaRepostasSimples(int codigoMaha, ModoExecucao modo) throws Exception {

		sqlPerguntasRespostaSimples.setNamedParameter("NUMOS", numOS);
		sqlPerguntasRespostaSimples.setNamedParameter("CODMAHA", codigoMaha);
		sqlPerguntasRespostaSimples.setNamedParameter("RESPOSTA", modo.getRespostaSimples());

		boolean responderPerguntasMestres = ModoExecucao.RESP_SIM.equals(modo.getRespostaSimples());

		ResultSet rset = null;
		List<RespostaPergunta> perguntasMestreList = new ArrayList<RespostaPergunta>();

		try {
			rset = this.sqlPerguntasRespostaSimples.executeQuery();

			while (rset.next()) {
				BigDecimal nuPesq = rset.getBigDecimal("NUPESQ");
				BigDecimal codPerg = rset.getBigDecimal("CODPERG");
				BigDecimal codResp = rset.getBigDecimal("CODRESP");
				BigDecimal codPergMestre = rset.getBigDecimal("CODPERGMESTRE");
				BigDecimal codRespMestre = rset.getBigDecimal("CODRESPMESTRE");

				if( ! respostasSimplesPorPergunta.containsKey(codPerg)) {

					RespostaPergunta respPergunta = new RespostaPergunta(nuPesq, codPerg, codResp);
					respostasSimplesPorPergunta.put(respPergunta.codPerg, respPergunta);
				}

				if(responderPerguntasMestres && codPergMestre != null) {
					RespostaPergunta respPerguntaMestre = new RespostaPergunta(nuPesq, codPergMestre, codRespMestre);
					perguntasMestreList.add(respPerguntaMestre);

					respostasSimplesPorPergunta.put(respPerguntaMestre.codPerg, respPerguntaMestre);
				}
			}
		} finally {
			if (rset != null) {
				rset.close();
			}
		}

		for(RespostaPergunta pergunta : perguntasMestreList) {
			processaRespostaPerguntaMestre(pergunta, "  ");
		}
	}

	private void processaRespostaPerguntaMestre(RespostaPergunta pergunta, String tab) throws Exception {

		RespostaPergunta perguntaMestre = getPerguntaMestre(pergunta);

		if(perguntaMestre != null) {

			respostasSimplesPorPergunta.put(perguntaMestre.codPerg, perguntaMestre);
			processaRespostaPerguntaMestre(perguntaMestre, tab + "  ");
		}
	}

	private RespostaPergunta getPerguntaMestre(RespostaPergunta pergunta) throws Exception {
		sqlPerguntaMestre.setNamedParameter("CODPERG", pergunta.codPerg);

		ResultSet rset = null;
		RespostaPergunta perguntaMestre = null;

		try {
			rset = sqlPerguntaMestre.executeQuery();

			if (rset.next()) {
				BigDecimal codPergMestre = rset.getBigDecimal("CODPERGMESTRE");
				BigDecimal codRespMestre = rset.getBigDecimal("CODRESPMESTRE");

				perguntaMestre = new RespostaPergunta(pergunta.nuPesq, codPergMestre, codRespMestre);
			}
		} finally {
			if (rset != null) {
				rset.close();
			}
		}

		return perguntaMestre;
	}

	private void insertRespostaQuestionario(BigDecimal nuPesq, BigDecimal codPerg, BigDecimal codResp) throws Exception {
		insertRespostaQuestionario(nuPesq, codPerg, codResp, false);
	}

	private void insertRespostaQuestionario(BigDecimal nuPesq, BigDecimal codPerg, BigDecimal codResp, boolean deleteIfExists) throws Exception {
		SessionHandle hnd = null;

        try {
            hnd = JapeSession.open();

            JapeWrapper dao = JapeFactory.dao("RespostaPerguntaPesquisa");

            if(deleteIfExists) {
            	dao.delete(nuPesq, codPerg, codResp);
            }

            dao.create()
	        		.set("NUPESQ", nuPesq)
	        		.set("CODPERG", codPerg)
	        		.set("CODRESP", codResp)
	        		.set("CODUSU", ctx.getUsuarioLogado())
	        		.set("NOTA", BigDecimal.ZERO)
	        		.set("DHALTER", TimeUtils.getNow())
	        		.set("PROIBELIMPEZA", "S")
	        		.save();

        } finally {
            JapeSession.close(hnd);
        }
	}

	private static class RespostaPergunta {

		private BigDecimal 	nuPesq;
		private BigDecimal 	codPerg;
		private BigDecimal	codResp;

		public RespostaPergunta(BigDecimal nuPesq, BigDecimal codPerg, BigDecimal codResp){
			this.nuPesq = nuPesq;
			this.codPerg = codPerg;
			this.codResp = codResp;
		}
	}
}
