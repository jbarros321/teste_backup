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
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BoletoHelper;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.FinanceiroVO;
import br.com.sankhya.modelcore.financeiro.util.AdiantamentoEmprestimoHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.util.ConcatenatePDF;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.SessionFile;
import com.sankhya.util.UIDGenerator;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class GerarBoletoTerceiros implements AcaoRotinaJava {
  private String MsgErro = "";

  public void doAction(ContextoAcao ctx) throws Exception {
    for (int i = 0; i < (ctx.getLinhas()).length; i++) {
      Registro line = ctx.getLinhas()[i];
      try {
        geraBoleto(ctx, line);
      } catch (Exception e) {
        this.MsgErro = String.valueOf(String.valueOf(this.MsgErro)) + e.getMessage() + " <br> ";
        e.printStackTrace();
      }
    }
  }

  private void gerarFinanceiro(ContextoAcao ctx, Registro line) throws Exception {
    ConcatenatePDF arquivos = new ConcatenatePDF();
    arquivos.setNumeration(false);
    byte[] arquivo = null;
    AuthenticationInfo authInfo = AuthenticationInfo.getCurrent();
    JapeSession.SessionHandle hnd = null;
    EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
    BigDecimal sequenciaTerc = (BigDecimal) line.getCampo("SEQ");
    BigDecimal numeroAdiant = (BigDecimal) line.getCampo("NUADIANT");
    BigDecimal codparc = (BigDecimal) line.getCampo("CODPARC");
    QueryExecutor queryBuscaDuplicata = ctx.getQuery();
    queryBuscaDuplicata.nativeSelect(
        "SELECT PL.CODDUPL " +
            "FROM AD_CTRLDUPL PL " +
            "WHERE PL.NUFIN = (" +
            "SELECT TERC.NUFINREC " +
            "FROM AD_CTRLDUPLTERC TERC " +
            "WHERE TERC.SEQ = " + sequenciaTerc + " AND TERC.CODPARC = " + codparc + ")");

    BigDecimal duplicata = null;
    if (queryBuscaDuplicata.next()) {
      duplicata = queryBuscaDuplicata.getBigDecimal("CODDUPL");
    }

    if (duplicata == null) {
      throw new Exception("Duplicata não encontrada para o SEQ: " + sequenciaTerc + " e CODPARC: " + codparc);
    }
    BigDecimal codusu = ctx.getUsuarioLogado();
    BigDecimal valor = (BigDecimal) line.getCampo("VALOR");
    BigDecimal codtipoper = new BigDecimal(1300);
    String historico = "Titulo Gerado atravda tela de Controle de duplicata c" + duplicata;
    if (numeroAdiant == null) {
      QueryExecutor queryCON = ctx.getQuery();
      queryCON.nativeSelect(
          " SELECT T.SEQ, C.CODPARC AS CODPARCDESP, FIN.CODEMP, T.CODPARC AS CODPARCREC, T.VALOR, T.DTVENC, FIN.CODCTABCOINT AS CODCTABCOINT_DESP,              FIN.CODTIPTIT, FIN.CODNAT, FIN.CODCENCUS, FIN.CODPROJ, T.CODCTABCOINT, FIN.DTNEG        FROM AD_CTRLDUPL C, AD_CTRLDUPLTERC T, TGFFIN FIN \t     WHERE C.CODDUPL = T.CODDUPL         AND C.NUFIN = FIN.NUFIN         AND C.CODDUPL = "
              +

              duplicata +
              "        AND T.SEQ = " + sequenciaTerc);
      System.out.println(queryCON.toString());
      while (queryCON.next()) {
        BigDecimal codParcDesp = queryCON.getBigDecimal("CODPARCDESP");
        BigDecimal codParcRec = queryCON.getBigDecimal("CODPARCREC");
        BigDecimal codemp = queryCON.getBigDecimal("CODEMP");
        BigDecimal codctabcoDesp = queryCON.getBigDecimal("CODCTABCOINT_DESP");
        Integer codctabcointDesp = Integer.valueOf(queryCON.getInt("CODCTABCOINT_DESP"));
        BigDecimal codctabcoint = queryCON.getBigDecimal("CODCTABCOINT");
        Integer codctabcointInt = Integer.valueOf(queryCON.getInt("CODCTABCOINT"));
        BigDecimal codtiptitDesp = queryCON.getBigDecimal("CODTIPTIT");
        BigDecimal codnat = queryCON.getBigDecimal("CODNAT");
        BigDecimal codcencus = queryCON.getBigDecimal("CODCENCUS");
        BigDecimal codproj = queryCON.getBigDecimal("CODPROJ");
        Timestamp dtvencTime = queryCON.getTimestamp("DTVENC");
        Timestamp dtnegTime = queryCON.getTimestamp("DTNEG");
        BigDecimal valorFin = queryCON.getBigDecimal("VALOR");
        BigDecimal seq = queryCON.getBigDecimal("SEQ");
        DynamicVO despesaVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("Financeiro",
            FinanceiroVO.class);
        despesaVO.setProperty("RECDESP", BigDecimal.ONE.negate());
        despesaVO.setProperty("CODEMP", codemp);
        despesaVO.setProperty("PROVISAO", "N");
        despesaVO.setProperty("CODTIPOPER", codtipoper);
        despesaVO.setProperty("CODPARC", codParcDesp);
        despesaVO.setProperty("HISTORICO", historico);
        despesaVO.setProperty("CODCTABCOINT", codctabcoDesp);
        despesaVO.setProperty("CODBCO", getCodigoBanco(codctabcointDesp.intValue()));
        despesaVO.setProperty("CODTIPTIT", codtiptitDesp);
        despesaVO.setProperty("CODNAT", codnat);
        despesaVO.setProperty("CODCENCUS", codcencus);
        despesaVO.setProperty("ORIGEM", "F");
        despesaVO.setProperty("CODPROJ", codproj);
        despesaVO.setProperty("DTNEG", dtnegTime);
        despesaVO.setProperty("DTVENC", dtvencTime);
        despesaVO.setProperty("DTVENCINIC", dtvencTime);
        despesaVO.setProperty("DESDOBRAMENTO", "0");
        despesaVO.setProperty("DESDOBDUPL", "ZZ");
        despesaVO.setProperty("TIPMARCCHEQ", "I");
        despesaVO.setProperty("TIPMULTA", "1");
        despesaVO.setProperty("TIPJURO", "1");
        despesaVO.setProperty("VLRDESDOB", valorFin);
        System.out.println("PONTO 5 TESTE - THIAGOBONATTI DEPOIS DE INCLUIR DESPESA: ");
        DynamicVO receitaVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("Financeiro",
            FinanceiroVO.class);
        receitaVO.setProperty("RECDESP", BigDecimal.ONE);
        receitaVO.setProperty("CODEMP", codemp);
        receitaVO.setProperty("PROVISAO", "N");
        receitaVO.setProperty("CODTIPOPER", codtipoper);
        receitaVO.setProperty("CODPARC", codParcRec);
        receitaVO.setProperty("HISTORICO", historico);
        receitaVO.setProperty("CODCTABCOINT", codctabcoint);
        receitaVO.setProperty("CODBCO", getCodigoBanco(codctabcointInt.intValue()));
        receitaVO.setProperty("CODTIPTIT", codtiptitDesp);
        receitaVO.setProperty("CODNAT", codnat);
        receitaVO.setProperty("CODCENCUS", codcencus);
        receitaVO.setProperty("ORIGEM", "F");
        receitaVO.setProperty("CODPROJ", codproj);
        receitaVO.setProperty("DTNEG", dtnegTime);
        receitaVO.setProperty("DTVENC", dtvencTime);
        receitaVO.setProperty("DTVENCINIC", dtvencTime);
        receitaVO.setProperty("DESDOBRAMENTO", "0");
        receitaVO.setProperty("DESDOBDUPL", "ZZ");
        receitaVO.setProperty("TIPMARCCHEQ", "I");
        receitaVO.setProperty("TIPMULTA", "1");
        receitaVO.setProperty("TIPJURO", "1");
        receitaVO.setProperty("VLRDESDOB", valorFin);
        System.out.println("PONTO 6 TESTE - THIAGOBONATTI DEPOIS DE INCLUIR RECEITA: ");
        try {
          hnd = JapeSession.open();
          AdiantamentoEmprestimoHelper helper = new AdiantamentoEmprestimoHelper();
          Collection<DynamicVO> titulosParcelamento = new ArrayList<>();
          titulosParcelamento.add(despesaVO);
          titulosParcelamento.add(receitaVO);
          BigDecimal numeroAcerto = helper.salvarParcelamento(titulosParcelamento, authInfo.getUserID());
          System.out.println("PONTO 8 TESTE - THIAGOBONATTI nro acerto: " + numeroAcerto);
          line.setCampo("NUADIANT", numeroAcerto);
          ctx.setMensagemRetorno("Financeiro Gerado com Sucesso!");
        } catch (Exception e) {
          MGEModelException.throwMe(e);
          continue;
        } finally {
          JapeSession.close(hnd);
        }
      }
    } else {
      ctx.setMensagemRetorno("Este Titulo jfoi registrado! ");
    }
  }

  private void geraBoleto(ContextoAcao ctx, Registro line) throws Exception {
    ConcatenatePDF arquivos = new ConcatenatePDF();
    arquivos.setNumeration(false);
    byte[] arquivo = null;
    BigDecimal sequenciaTerc = (BigDecimal) line.getCampo("SEQ");
    BigDecimal nroun = (BigDecimal) line.getCampo("NUUNICO");
    BigDecimal codparcd = (BigDecimal) line.getCampo("CODPARC");
    QueryExecutor queryBuscaDuplicata = ctx.getQuery();
    queryBuscaDuplicata.nativeSelect(
        "SELECT PL.CODDUPL " +
            "FROM AD_CTRLDUPL PL " +
            "WHERE PL.NUFIN = (" +
            "SELECT TERC.NUFINREC " +
            "FROM AD_CTRLDUPLTERC TERC " +
            "WHERE TERC.SEQ = " + sequenciaTerc + " AND TERC.CODPARC = " + codparcd + ")");

    BigDecimal duplicata = null;
    if (queryBuscaDuplicata.next()) {
      duplicata = queryBuscaDuplicata.getBigDecimal("CODDUPL");
    }

    if (duplicata == null) {
      throw new Exception("Duplicata não encontrada para o SEQ: " + sequenciaTerc + " e CODPARC: " + codparcd);
    }
    BigDecimal numeroAdiant = (BigDecimal) line.getCampo("NUADIANT");
    BigDecimal codusu = ctx.getUsuarioLogado();
    JdbcWrapper jdbc = JapeFactory.getEntityFacade().getJdbcWrapper();
    NativeSql nativeSql = new NativeSql(jdbc);
    System.out.println("[ENVIARBOLETO] - antes do update: ");
    System.out.println("[ENVIARBOLETO] - depois do update: ");
    QueryExecutor queryCON = ctx.getQuery();
    queryCON.nativeSelect(
        " SELECT PAR.EMAIL, PAR.RAZAOSOCIAL, EMP.RAZAOSOCIAL AS RAZAOEMP, Formatar_Cpf_Cnpj(EMP.CGC) AS CGC, SYSDATE AS DATA, FRE.NUFIN, FIN.CODCTABCOINT,  (SELECT NVL(MAX(SEQ),0) + 1 FROM AD_CTRLDUPLITE WHERE CODDUPL = "
            +
            duplicata +
            ") AS SEQUENCIA, TO_CHAR(SYSDATE,'DD/MM/YYYY') AS DATAFORMAT " +
            "\t  FROM TGFPAR PAR, TGFFIN FIN, TSIEMP EMP, AD_CTRLDUPLTERC T, TGFFRE FRE  " +
            "  WHERE PAR.CODPARC = FIN.CODPARC " +
            "    AND FIN.CODEMP = EMP.CODEMP " +
            "    AND FIN.RECDESP = 1 " +
            "    AND FIN.NUFIN = FRE.NUFIN " +
            "    AND FRE.SEQUENCIA = 2 " +
            "    AND FRE.NUACERTO = " + numeroAdiant +
            "    AND T.NUUNICO = " + nroun +
            "    AND T.SEQ = " + sequenciaTerc);
    System.out.println(queryCON.toString());
    while (queryCON.next()) {
      String email = queryCON.getString("EMAIL");
      String razaosocial = queryCON.getString("RAZAOSOCIAL");
      String razaoEmp = queryCON.getString("RAZAOEMP");
      String cnpj = queryCON.getString("CGC");
      Date data = (Date) queryCON.getDate("DATA");
      BigDecimal seq = queryCON.getBigDecimal("SEQUENCIA");
      String dataformat = queryCON.getString("DATAFORMAT");
      BigDecimal nuFin = queryCON.getBigDecimal("NUFIN");
      BigDecimal codCta = queryCON.getBigDecimal("CODCTABCOINT");
      String codCtabcoInt = queryCON.getString("CODCTABCOINT");
      if (codCtabcoInt == null) {
        System.out.println("[ENVIARBOLETO] - 1 - Para Gerar o Boleto a conta deve estar preenchida!" + codCtabcoInt);
        ctx.setMensagemRetorno("Para Gerar o Boleto a conta deve estar preenchida!");
        return;
      }
      System.out.println("[ENVIARBOLETO] - nufin receita: " + nuFin + " conta: " + codCta);
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
        Duplicata arquivo = arquivos.run().toByteArray();
        SessionFile fileReport = SessionFile.createSessionFile("Relatorios", "application/pdf", arquivo);
        String corpoemail = "<html> <html><head><title>Boleto</title></head> <body> <p>Prezado Cliente, <strong> " +
            razaosocial + "</strong></p>" +
            "<p>Segue em anexo Boleto<br>" + "Referente a duplicata <strong> " + duplicata +
            "</strong> emitida em " + dataformat + ".</p>" + "<p>Atenciosamente " + razaoEmp + "<br>" +
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
          nativeSql.executeUpdate(" INSERT INTO TMDAXM (CODFILA, NUANEXO) VALUES (" + ultCod + " , " +
              nuAnexoBoleto + ")");
          nativeSql.executeUpdate(
              " INSERT INTO AD_CTRLDUPLITE (CODPARC,CODDUPL, SEQ, CODUSUINC, DHINC, OBSERVACAO) VALUES (" + codparcd
                  + " , " +
                  duplicata + " , " + seq + "," + codusu +
                  " , SYSDATE , 'Boleto gerado e enviado para o cliente no email: " + email + " NUMERO UNICO: " + nuFin
                  +
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
      }
    }
  }

  private String getLinkBaixar(String descricao, String chave) {
    String url = "<a title=\"Visualizar Arquivo\" href=\"/mge/visualizadorArquivos.mge?chaveArquivo=" + chave +
        "\" target=\"_blank\"><u><b>" + descricao + "</b></u></a>";
    return url;
  }

  private Object getCodigoBanco(int codctabcoint) throws Exception {
    EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
    DynamicVO contaVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("ContaBancaria",
        Integer.valueOf(codctabcoint));
    return contaVO.asBigDecimal("CODBCO");
  }
}
