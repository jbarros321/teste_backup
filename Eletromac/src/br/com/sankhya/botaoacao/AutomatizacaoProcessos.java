package br.com.sankhya.botaoacao;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;

public class AutomatizacaoProcessos implements AcaoRotinaJava, ScheduledAction {
  public void onTime(ScheduledActionContext ctx) {
    String errorInfo = executar();
    logIndex("Executando automacao transferEletromac...");
    if (errorInfo != null) {
      logIndex("Erro na execuda automacao transferEletromac:");
      logIndex(errorInfo);
      ctx.info(errorInfo);
    }
  }

  public void doAction(ContextoAcao contexto) throws Exception {
    String errorInfo = executar();
    if (errorInfo != null)
      contexto.mostraErro(errorInfo);
  }

  private String executar() {
    logIndex("----------------------------------");
    logIndex(" Gerando transferencia automatica ");
    logIndex("----------------------------------");
    JapeSession.SessionHandle hnd = null;
    try {
      hnd = JapeSession.open();
      JapeSession.putProperty("jape.session.no.query.timeout", Boolean.TRUE);
      hnd.setCanTimeout(false);
      AutomatizacaoProcessosHelper automatizacaoHelper = new AutomatizacaoProcessosHelper(hnd);
      automatizacaoHelper.notasPendentes();
      if (automatizacaoHelper.existeNotas().booleanValue()) {
        logIndex("Gerando Lote");
        logIndex("-------------");
        automatizacaoHelper.gerarLoteNotas();
        logIndex("Gerado Lote");
        logIndex("-----------");
      } else {
        logIndex("Gerando transferencia");
        logIndex("---------------------");
        automatizacaoHelper.gerarTransferencia();
        automatizacaoHelper.confirmarNotas();
        logIndex("Gerado transferencia");
        logIndex("--------------------");
      }
    } catch (Exception e) {
      logIndex("Erro na chamada de transfer");
      e.printStackTrace();
    } finally {
      logIndex("-------------------------------------");
      logIndex(" Finalizado transferautom");
      logIndex("-------------------------------------");
      JapeSession.close(hnd);
    }
    return null;
  }

  @SuppressWarnings("unused")
private void notificarFalhaViaEmail(String msgErro) throws Exception {
    JdbcWrapper jdbc = null;
    try {
      EntityFacade dwfFacade = EntityFacade.getSingleton();

      jdbc = dwfFacade.getJdbcWrapper();
      jdbc.openSession();
      String hashMsg = gerarMD5(msgErro);
      if (verificarMensagemEnviada(hashMsg, jdbc))
        return;
      Set<String> emailList = montarListaDestinatarios(jdbc);
      for (String email : emailList) {
        DynamicVO tmdfmgVO = (DynamicVO)dwfFacade.getDefaultValueObjectInstance("MSDFilaMensagem");
        tmdfmgVO.setProperty("EMAIL", email.trim());
        tmdfmgVO.setProperty("ASSUNTO", "Falha na automacao Pedidos - " + hashMsg);
        tmdfmgVO.setProperty("MENSAGEM", msgErro.toCharArray());
        tmdfmgVO.setProperty("MAXTENTENVIO", new BigDecimal(3));
        tmdfmgVO.setProperty("STATUS", "Pendente");
        tmdfmgVO.setProperty("CODCON", BigDecimal.ZERO);
        tmdfmgVO.setProperty("CODMSG", null);
        tmdfmgVO.setProperty("TIPOENVIO", "E");
        dwfFacade.createEntity("MSDFilaMensagem", (EntityVO)tmdfmgVO);
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
    sqlMsgEnviada.appendSql(" SELECT 1 FROM TMDFMG WHERE ASSUNTO LIKE :HASHMSG AND DTENTRADA >= :DTHOJE ");
    sqlMsgEnviada.setNamedParameter("HASHMSG", "%" + hashMsg + "%");
    sqlMsgEnviada.setNamedParameter("DTHOJE", new Timestamp(TimeUtils.getToday()));
    ResultSet rsMsgEnviada = sqlMsgEnviada.executeQuery();
    if (rsMsgEnviada.next())
      return true;
    return false;
  }

  private Set<String> montarListaDestinatarios(JdbcWrapper jdbc) throws Exception {
    Set<String> emailList = new HashSet<>();
    NativeSql sqlEmailList = new NativeSql(jdbc);
    sqlEmailList.appendSql(" SELECT EMAIL FROM TSIUSU WHERE NOMEUSU = 'MARCELO' ");
    ResultSet rsEmailList = sqlEmailList.executeQuery();
    while (rsEmailList.next())
      emailList.add(rsEmailList.getString(1));
    return emailList;
  }

  public static void logIndex(String msg) {
    System.out.println(msg);
  }
}
