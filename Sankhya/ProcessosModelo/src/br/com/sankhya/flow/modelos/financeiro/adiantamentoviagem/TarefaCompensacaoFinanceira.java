package br.com.sankhya.flow.modelos.financeiro.adiantamentoviagem;

import java.math.BigDecimal;

import br.com.sankhya.callservicesnk.SWServiceInvoker;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoTarefa;
import br.com.sankhya.extensions.flow.TarefaJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.ws.ServiceContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TarefaCompensacaoFinanceira implements TarefaJava{

	@Override
	public void executar(final ContextoTarefa contexto) throws Exception {
		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();
			JapeSession.getCurrentSession().execWithAutonomousTX(new JapeSession.NewTXBody() {
				public Object run() throws Exception {
					executarInterno(contexto);
					return null;
				}
			});
		} finally {
			JapeSession.close(hnd);
		}
	}

	private void executarInterno(ContextoTarefa contexto) throws Exception {

		ServiceContext srvCtx = ServiceContext.getCurrent();
		String user = srvCtx != null ? null : "SUP";
		String pass = srvCtx != null ? null : "";
		String address = (String) contexto.getParametroSistema("ENDACESSEXTWGE");
		if(address == null) {
			throw new IllegalStateException("Para prosseguir é necess�rio preencher adequadamente o parâmetro ENDACESSEXTWGE. Contate o implantador/administrador do sistema.");
		}
		address = address.replace("/mge", "");
		SWServiceInvoker si = new SWServiceInvoker(address, user, pass);

		Registro formulario[] = contexto.getLinhasFormulario("AD_SOLICITACAOADIANTAMENTO");

		if(formulario.length > 0) {

			Object nuFinReceita = formulario[0].getCampo("NUFIN_AD_R");
			Object nuFinDespesa = formulario[0].getCampo("NUFIN_AC_D");
			Object topDesp = contexto.getCampo("CODTIPOPERDES");
			Object topRec = contexto.getCampo("CODTIPOPERREC");
			BigDecimal vlrDesp;
			BigDecimal vlrRec;
			BigDecimal vlrAcerto;
			String resultadoAcerto;

			NativeSql query = null;
			try {

				vlrDesp = NativeSql.getBigDecimal("VLRDESDOB", "TGFFIN", "NUFIN = " + nuFinDespesa);
				vlrRec = NativeSql.getBigDecimal("VLRDESDOB", "TGFFIN", "NUFIN = " + nuFinReceita);

				if(vlrDesp.compareTo(vlrRec) == 1) {

					vlrAcerto = vlrRec;
					resultadoAcerto = "Valor com despesas de viagem foi maior que o valor do adiantamento. Você receberá um REEMBOLSO de R$" + vlrDesp.subtract(vlrRec);

				}else if(vlrRec.compareTo(vlrDesp) == 1) {

					vlrAcerto = vlrRec;
					resultadoAcerto = "Valor com despesas de viagem foi menor que o valor do adiantamento. Você deverá DEVOLVER R$" + vlrRec.subtract(vlrDesp);

				}else {
					vlrAcerto = new BigDecimal(0);
					resultadoAcerto = "Valor com despesas de viagem foi igual ao valor do adiantamento. Não será necessário reembolso nem devolução.";
				}

				JsonParser jsonParser = new JsonParser();
				JsonObject body = null;
				body = (JsonObject) jsonParser.parse("{ \"params\": { \"chave\": \"br.com.sankhya.mgefin.mov.CompensacaoFinanceira_prefs\", \"tipo\": \"T\", \"receitas\": { \"nuFin\": [{ \"$\": " + nuFinReceita + " } ] }, \"despesas\": { \"nuFin\": [{ \"$\": " + nuFinDespesa + " } ] }, \"vlrAcerto\": " + vlrAcerto + ", \"topDesp\": " + topDesp + ", \"topRec\": " + topRec + " }, \"clientEventList\": { \"clientEvent\": [{ \"$\": \"br.com.sankhya.mgecom.event.tem.boleto.rapido\" } ] } }");

				contexto.setCampo("RESULTADOACERTO", resultadoAcerto);

				si.login();
				if(srvCtx != null) {
					si.setSessionID(srvCtx.getHttpSessionId());
				}

				JsonObject response = si.callJson("CompensacaoFinanceiraSP.acertar", "mgefin", body);
				SWServiceInvoker.printDocumentJson(response);

				si.logout();

			} finally {
				NativeSql.releaseResources(query);
			}

		}

	}

}
