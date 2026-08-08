package BotaoAcao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.comercial.BoletoHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.util.ConcatenatePDF;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.SessionFile;
import com.sankhya.util.StringUtils;
import com.sankhya.util.UIDGenerator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;

public class GerarBoleto implements AcaoRotinaJava {
  private String MsgErro = "";

  public void doAction(ContextoAcao ctx) throws Exception {
    for (int i = 0; i < (ctx.getLinhas()).length; i++) {
      Registro line = ctx.getLinhas()[i];
      try {
        geraBoleto(ctx, line);
      } catch (Exception e) {
        this.MsgErro = String.valueOf(String.valueOf(String.valueOf(String.valueOf(this.MsgErro)))) + e.getMessage()
            + " <br> ";
        e.printStackTrace();
      }
    }
  }

  private void geraBoleto(ContextoAcao ctx, Registro line) throws Exception {
    ConcatenatePDF arquivos = new ConcatenatePDF();
    arquivos.setNumeration(false);
    byte[] arquivo = null;
    BigDecimal nuFin = (BigDecimal) line.getCampo("NUFIN");
    BigDecimal duplicata = (BigDecimal) line.getCampo("CODDUPL");
    BigDecimal numnota = (BigDecimal) line.getCampo("NUMNOTA");
    BigDecimal codparc = (BigDecimal) line.getCampo("CODPARC");
    BigDecimal codusu = ctx.getUsuarioLogado();
    String tipo = StringUtils.getNullAsEmpty((String) line.getCampo("TIPO"));
    String status = (String) line.getCampo("STATUS");
    BigDecimal codCtabcoBig = BigDecimalUtil.getValueOrZero((BigDecimal) line.getCampo("CODCTABCOINT"));
    if (codCtabcoBig.compareTo(new BigDecimal(0)) == 0) {
      System.out.println("[ENVIARBOLETO] - 1 - Para Gerar o Boleto a conta deve estar preenchida!" + codCtabcoBig);
      ctx.setMensagemRetorno("Para Gerar o Boleto a conta deve estar preenchida!");
      return;
    }
    System.out.println("[ENVIARBOLETO] - 3 - Para Gerar o Boleto a conta deve estar preenchida!" + codCtabcoBig);
    System.out.println("Aqui. Status: " + status);
    System.out.println("Aqui. Tipo: " + tipo);
    if (tipo.isEmpty())
      tipo = "P";
    System.out.println("[ENVIARBOLETO] - TIpo: " + tipo);
    if (tipo.equals("T")) {
      System.out.println("[ENVIARBOLETO] - 1 - TIpo: " + tipo);
      ctx.setMensagemRetorno("Só pode gerar boleto para Tipo Proprio!");
    }
    if (status.equals("F"))
      ctx.setMensagemRetorno("Titulo jfoi baixado não pode gerar Boleto!");
    if (status.equals("C"))
      ctx.setMensagemRetorno("Duplicada CANCELADA não pode gerar Boleto!");

    if (status.equals("A") && tipo.equals("P") && codCtabcoBig.compareTo(new BigDecimal(0)) > 0) {
      System.out.println("[ENVIARBOLETO] - 2- TIpo: " + tipo);
      QueryExecutor queryCON = ctx.getQuery();
      queryCON.nativeSelect(
          " SELECT EMAIL, RAZAOSOCIAL, RAZAOEMP, CGC, DATA, SEQUENCIA + ROWNUM AS SEQUENCIA, DATAFORMAT FROM ( SELECT DISTINCT EMAIL, RAZAOSOCIAL, RAZAOEMP, CGC, DATA, (SELECT NVL(MAX(SEQ),0) FROM AD_CTRLDUPLITE WHERE CODDUPL = "
              +
              duplicata + ") AS SEQUENCIA, DATAFORMAT FROM (" +
              " SELECT PAR.EMAIL, PAR.RAZAOSOCIAL, EMP.RAZAOSOCIAL AS RAZAOEMP, Formatar_Cpf_Cnpj(EMP.CGC) AS CGC, SYSDATE AS DATA,  "
              +
              " TO_CHAR(SYSDATE,'DD/MM/YYYY') AS DATAFORMAT " +
              "\t  FROM TGFPAR PAR, TGFFIN FIN, TSIEMP EMP " + "  WHERE PAR.CODPARC = FIN.CODPARC " +
              "    AND FIN.CODEMP = EMP.CODEMP " + "    AND FIN.NUFIN = " + nuFin + "\t  UNION ALL " +
              " SELECT CTT.EMAIL, PAR.RAZAOSOCIAL, EMP.RAZAOSOCIAL AS RAZAOEMP, Formatar_Cpf_Cnpj(EMP.CGC) AS CGC, SYSDATE AS DATA, "
              +
              " TO_CHAR(SYSDATE,'DD/MM/YYYY') AS DATAFORMAT " +
              "\t FROM TGFPAR PAR, TGFFIN FIN, TSIEMP EMP, TGFCTT CTT " +
              "  WHERE PAR.CODPARC = FIN.CODPARC " +
              "  AND FIN.CODEMP = EMP.CODEMP AND CTT.CODPARC = PAR.CODPARC " +
              " AND  NVL(AD_RECEMAILDUPL,'N') = 'S' AND FIN.NUFIN = " + nuFin + ") )");
      System.out.println(queryCON.toString());
      while (queryCON.next()) {
        String email = queryCON.getString("EMAIL");
        String razaosocial = queryCON.getString("RAZAOSOCIAL");
        String razaoEmp = queryCON.getString("RAZAOEMP");
        String cnpj = queryCON.getString("CGC");
        Date data = (Date) queryCON.getDate("DATA");
        BigDecimal seq = queryCON.getBigDecimal("SEQUENCIA");
        String dataformat = queryCON.getString("DATAFORMAT");
        try {
          BoletoHelper.ConfiguracaoBoleto cfg = new BoletoHelper.ConfiguracaoBoleto();
          cfg.setGerarNumeroBoleto(true);
          cfg.setUsaContaBcoFinanceiros(true);
          cfg.setFinanceirosSelecionados(Arrays.asList(new BigDecimal[] { nuFin }));
          cfg.setTipoSaidaBoleto(1);
          BoletoHelper boletoHelper = new BoletoHelper();
          boletoHelper.gerarBoleto(cfg);
          byte[] boleto = boletoHelper.getBoletosPDF();
          arquivos.addPdfFile(boleto);
          arquivo = arquivos.run().toByteArray();
          SessionFile fileReport = SessionFile.createSessionFile("Relatorios", "application/pdf", arquivo);
          String corpoemail = "<html> <html><head><title>Boleto</title></head> <body> <p>Prezado Cliente, <strong> " +
              razaosocial + "</strong></p>" +
              "<p>Segue em anexo Boleto emitida em " + dataformat + "<br>" + "Referente a nota: <strong> " + numnota +
              "</strong> .</p>" + "<p>Atenciosamente " + razaoEmp + "<br>" +
              cnpj + "</p>" + "</body>" + "</html>";
          char[] mensagem = corpoemail.toCharArray();
          String assunto = "Boleto Satis";
          JapeSession.SessionHandle hnd = null;
          try {
            hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            EntityVO entityVO = dwfFacade.getDefaultValueObjectInstance("MSDFilaMensagem");
            DynamicVO dynamicVO = (DynamicVO) entityVO;
            dynamicVO.setProperty("ASSUNTO", assunto);
            dynamicVO.setProperty("DTENTRADA", data);
            dynamicVO.setProperty("STATUS", "Pendente");
            dynamicVO.setProperty("EMAIL", email);
            dynamicVO.setProperty("TENTENVIO", new BigDecimal(3));
            dynamicVO.setProperty("MENSAGEM", mensagem);
            dynamicVO.setProperty("NUCHAVE", duplicata);
            dynamicVO.setProperty("TIPOENVIO", "E");
            dynamicVO.setProperty("MAXTENTENVIO", new BigDecimal(3));
            dynamicVO.setProperty("CODSMTP", new BigDecimal(27));
            dynamicVO.setProperty("CODCON", new BigDecimal(0));
            PersistentLocalEntity createEntity = dwfFacade.createEntity("MSDFilaMensagem", entityVO);
            DynamicVO save = (DynamicVO) createEntity.getValueObject();
            BigDecimal ultCod = save.asBigDecimal("CODFILA");
            dwfFacade = EntityFacadeFactory.getDWFFacade();
            entityVO = dwfFacade.getDefaultValueObjectInstance("AnexoMensagem");
            dynamicVO = (DynamicVO) entityVO;
            dynamicVO.setProperty("NOMEARQUIVO", "Boleto.pdf");
            dynamicVO.setProperty("TIPO", "application/pdf");
            dynamicVO.setProperty("ANEXO", boleto);
            createEntity = dwfFacade.createEntity("AnexoMensagem", entityVO);
            save = (DynamicVO) createEntity.getValueObject();
            BigDecimal nuAnexoBoleto = save.asBigDecimal("NUANEXO");
            JdbcWrapper jdbc = JapeFactory.getEntityFacade().getJdbcWrapper();
            NativeSql nativeSql = new NativeSql(jdbc);
            nativeSql.executeUpdate(" INSERT INTO TMDAXM (CODFILA, NUANEXO) VALUES (" + ultCod + " , " +
                nuAnexoBoleto + ")");
            nativeSql.executeUpdate(
                " INSERT INTO AD_CTRLDUPLITE (CODPARC,CODDUPL, SEQ, CODUSUINC, DHINC, OBSERVACAO) VALUES (" + codparc
                    + " , " +
                    duplicata + " , " + seq + "," + codusu +
                    " , SYSDATE , 'Boleto gerado e enviado para o cliente no email: " + email +
                    " ')");
          } finally {
            JapeSession.close(hnd);
          }
          String chaveSessaoArquivo = UIDGenerator.getNextID();
          ServiceContext.getCurrent().putHttpSessionAttribute(chaveSessaoArquivo, (Serializable) fileReport);
          ctx.setMensagemRetorno(
              String.format("%s", new Object[] { getLinkBaixar("Clique aqui para Visualizar.", chaveSessaoArquivo) }));
        } catch (Exception e2) {
          e2.printStackTrace();
          System.out.println("Fim ---- thiago bonatti - erro boleto");
          ctx.setMensagemRetorno("Erro ao gerar Boleto: " + e2.getMessage());
        }
      }
    }
  }

  public static void hexToBinary(InputStream is, OutputStream os) {
    Reader reader = new BufferedReader(new InputStreamReader(is));
    try {
      char[] buffer = new char[2];
      while (reader.read(buffer) != -1)
        os.write((Character.digit(buffer[0], 16) << 4) + Character.digit(buffer[1], 16));
    } catch (IOException e) {
      System.err.println("An error occurred");
    }
  }

  private String getLinkBaixar(String descricao, String chave) {
    String url = "<a title=\"Visualizar Arquivo\" href=\"/mge/visualizadorArquivos.mge?chaveArquivo=" + chave +
        "\" target=\"_blank\"><u><b>" + descricao + "</b></u></a>";
    return url;
  }
}
