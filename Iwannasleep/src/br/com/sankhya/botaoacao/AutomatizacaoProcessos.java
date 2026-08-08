package br.com.sankhya.botaoacao;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.botaoacao.AutomatizacaoProcessosHelper.MensagemErro;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.CanceledTransactionException;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class AutomatizacaoProcessos implements AcaoRotinaJava, ScheduledAction {

	@Override
	public void onTime(ScheduledActionContext ctx) {
		String errorInfo = executar();

		System.out.println("Executando automao de pedidos I Wanna Sleep...");

		if (errorInfo != null) {
			System.out.println("Erro na execuo da automao de pedidos I Wanna Sleep:");
			System.out.println(errorInfo);
			ctx.info(errorInfo);
		}
	}

	@Override
	public void doAction(ContextoAcao contexto) throws Exception {
		String errorInfo = executar();

		if (errorInfo != null) {
			contexto.mostraErro(errorInfo);
		}
	}

	private String executar() {
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();
			hnd.setPriorityLevel(JapeSession.LOW_PRIORITY);
			hnd.setCanTimeout(false);

			AutomatizacaoProcessosHelper automatizacaoHelper = new AutomatizacaoProcessosHelper(hnd);
			automatizacaoHelper.gerarDocumentosMatriz();
			automatizacaoHelper.confirmarReservasEstoque();
			automatizacaoHelper.cancelarReservasEstoqueAntigas();

			if ( ! automatizacaoHelper.getMensagensErro().isEmpty()) {
				final String msgErro = montarCorpoMensagem(automatizacaoHelper.getMensagensErro());

				hnd.execWithTX(new JapeSession.TXBlock() {
					public void doWithTx() throws Exception {
						try {
							notificarFalhaViaEmail(msgErro);
						} catch (Exception e) {
							CanceledTransactionException cancelEx = new CanceledTransactionException();
							cancelEx.initCause(e);
							throw cancelEx;
						}
					}
				});

				return msgErro;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}

		return null;
	}

	private void notificarFalhaViaEmail(String msgErro) throws Exception {
		JdbcWrapper jdbc = null;

		try {
			EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

			jdbc = dwfFacade.getJdbcWrapper();
			jdbc.openSession();

			String hashMsg = gerarMD5(msgErro);

			if(verificarMensagemEnviada(hashMsg, jdbc)) {
				return;
			}

			Set<String> emailList = montarListaDestinatarios(jdbc);

			for(String email : emailList) {
				DynamicVO tmdfmgVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.FILA_MSG);

				tmdfmgVO.setProperty("EMAIL", email.trim());
				tmdfmgVO.setProperty("ASSUNTO", "Falha na automao de Pedidos - " + hashMsg);
				tmdfmgVO.setProperty("MENSAGEM", msgErro.toCharArray());
				tmdfmgVO.setProperty("MAXTENTENVIO", new BigDecimal(3));
				tmdfmgVO.setProperty("STATUS", "Pendente");
				tmdfmgVO.setProperty("CODCON", BigDecimal.ZERO);
				tmdfmgVO.setProperty("CODMSG", null);
				tmdfmgVO.setProperty("TIPOENVIO", "E");

				dwfFacade.createEntity(DynamicEntityNames.FILA_MSG, (EntityVO) tmdfmgVO);
			}
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}

	private String gerarMD5(String texto) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(texto.getBytes());

		String chave = StringUtils.toHexString(md5.digest());

		return chave;
	}

	public static void main(String[] a) {
		Timestamp time = new Timestamp(TimeUtils.getToday());
		System.out.println(time);
	}

	private boolean verificarMensagemEnviada(String hashMsg, JdbcWrapper jdbc) throws Exception {
		NativeSql sqlMsgEnviada = new NativeSql(jdbc);

		sqlMsgEnviada.appendSql("SELECT 1 FROM TMDFMG WHERE ASSUNTO LIKE :HASHMSG AND DTENTRADA >= :DTHOJE");
		sqlMsgEnviada.setNamedParameter("HASHMSG", "%" + hashMsg + "%");
		sqlMsgEnviada.setNamedParameter("DTHOJE", new Timestamp(TimeUtils.getToday()));

		ResultSet rsMsgEnviada = sqlMsgEnviada.executeQuery();

		if(rsMsgEnviada.next()) {
			return true;
		}

		return false;
	}

	private Set<String> montarListaDestinatarios(JdbcWrapper jdbc) throws Exception {
		Set<String> emailList = new HashSet<String>();

		NativeSql sqlEmailList = new NativeSql(jdbc);

		sqlEmailList.appendSql(" SELECT TOP.AD_EMAILAUTOMACAO ");
		sqlEmailList.appendSql(" FROM  ");
		sqlEmailList.appendSql(" 	TGFTOP TOP ");
		sqlEmailList.appendSql(" WHERE TOP.DHALTER = (SELECT MAX(TOP2.DHALTER) FROM TGFTOP TOP2 WHERE TOP2.CODTIPOPER = TOP.CODTIPOPER) ");
		sqlEmailList.appendSql(" 	AND TOP.AD_GERANF = 'S' ");
		sqlEmailList.appendSql(" 	AND TOP.AD_EMAILAUTOMACAO IS NOT NULL ");

		ResultSet rsEmailList = sqlEmailList.executeQuery();

		while(rsEmailList.next()) {
			emailList.add(rsEmailList.getString(1));
		}

		return emailList;
	}

	private String montarCorpoMensagem(List<MensagemErro> listaErros) {
		StringBuilder msgErro = new StringBuilder();

		for(MensagemErro msg : listaErros) {
			msgErro.append(msg.mensagem);
		}

		msgErro.append("Voc recebeu este e-mail por estar cadastrado nas TOP de gerao automtica de pedidos I Wanna Sleep.<br><br>");
		msgErro.append("Caso no deseje receber mais essas mensagens solicite ao administrador.");

		return msgErro.toString();
	}
}
