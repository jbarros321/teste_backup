package br.com.sankhya.maha2sw;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.HashSet;

import com.google.gson.Gson;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.SQLUtils;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.model.services.DiagnosticoSPBean.Questionario;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class RequestAPI implements AcaoRotinaJava {
	@Override
	public void doAction(ContextoAcao ctx) throws Exception {
		String message = "";
		Boolean error = Boolean.valueOf(false);
		JdbcWrapper jdbc = null;
		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

			BigDecimal numOS = ((BigDecimal) ctx.getLinhaPai().getCampo("NUMOS"));
			BigDecimal codParc = BigDecimalUtil.getValueOrZero(((BigDecimal) ctx.getLinhaPai().getCampo("CODPARC")));

			NativeSql query = new NativeSql(jdbc);
			String cnpj = null;
			if(codParc.compareTo(BigDecimal.ZERO) == 0){
				BigDecimal codProsp = BigDecimalUtil.getValueOrZero(((BigDecimal) ctx.getLinhaPai().getCampo("CODPAP")));
				ResultSet rs = query.executeQuery("SELECT CGC_CPF FROM TCSPAP WHERE CODPAP = " + codProsp);
				if(rs.next()){
					cnpj = rs.getString("CGC_CPF");
				}
				rs.close();
			} else {
				ResultSet rs = query.executeQuery("SELECT CGC_CPF FROM TGFPAR WHERE CODPARC = " + codParc);
				if(rs.next()){
					cnpj = rs.getString("CGC_CPF");
				}
				rs.close();
			}
			if(cnpj == null){
				ctx.mostraErro("CNPJ no informado para o Prospect / Parceiro.");
			}
			NativeSql.releaseResources(query);

			MahaRequestResult request = execMahaRequest(cnpj);
			if (request.getResponseCode() == 200) {
				try {

					request.getEmpresa().getDiagnostico().traverseProcedimento(new ProcedimentoCallBackWithCtx(ctx, jdbc));

				} catch (Exception e) {
					e.printStackTrace();

					error = Boolean.valueOf(true);
					if ((e.getMessage() != null) && (e.getMessage().length() > 0)) {
						message = e.getMessage();
					} else {
						message = "A execuo da rotina java retornou uma exceo sem mensagem de erro!";
					}
				}
				if (!error.booleanValue()) {
					message = "Importao realizada com sucesso!";
				}
			} else {
				message = "Impossvel importar respostas do maha. Aconteceu uma falha na requisio.";
				System.out.println(request.getMessage());
			}
			ctx.setMensagemRetorno(message);
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}

	private static synchronized MahaRequestResult execMahaRequest(String cnpj) {
		try {
			URL url = new URL("https:

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("GET");
			conn.setRequestProperty("Authorization", "Token token=9bd75b85a7b200714d7ebb59fb4055e4");
			conn.setRequestProperty("Content-type", "application/json; charset=utf-8");
			conn.setRequestProperty("Accept", "*.*");

			int code = conn.getResponseCode();
			if (code == 200) {
				char[] buffer = new char[1024];
				Reader reader = new InputStreamReader(conn.getInputStream());
				StringBuffer jsonStream = new StringBuffer();
				int readen = 0;
				while ((readen = reader.read(buffer)) > -1) {
					jsonStream.append(buffer, 0, readen);
				}
				Gson g = new Gson();
				MahaRequestResult request = g.fromJson(jsonStream.toString(), MahaRequestResult.class);
				request.setResponseCode(code);

				return request;
			}
			if (code == 406) {
				MahaRequestResult request = new MahaRequestResult();
				request.setMessage("Voce deve finalizar o diagnstico Maha primeiro.");
				request.setResponseCode(code);
				return request;
			}
			if (code == 404) {
				MahaRequestResult request = new MahaRequestResult();
				request.setMessage("O diagnstico Maha ainda no foi feito para o CNPJ informado.");
				request.setResponseCode(code);
				return request;
			}
			MahaRequestResult request = new MahaRequestResult();
			request.setResponseCode(code);
			request.setMessage(conn.getResponseMessage());
			return request;
		} catch (MalformedURLException e) {
			MahaRequestResult request = new MahaRequestResult();
			e.printStackTrace();
			request.setMessage(e.getMessage());
			request.setResponseCode(418);
			return request;
		} catch (IOException e) {
			MahaRequestResult request = new MahaRequestResult();
			e.printStackTrace();
			request.setMessage(e.getMessage());
			request.setResponseCode(418);
			return request;
		}
	}
}
