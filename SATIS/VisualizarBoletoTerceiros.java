package BotaoAcao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.modelcore.comercial.BoletoHelper;
import br.com.sankhya.util.ConcatenatePDF;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.SessionFile;
import com.sankhya.util.UIDGenerator;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

public class VisualizarBoletoTerceiros implements AcaoRotinaJava {

  public void doAction(ContextoAcao ctx) throws Exception {
    for (int i = 0; i < (ctx.getLinhas()).length; i++) {
        Registro line = ctx.getLinhas()[i];
        try {
            geraBoleto(ctx, line);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erro ao gerar boleto: " + e.getMessage());
        }
    }
  }

  private void geraBoleto(ContextoAcao ctx, Registro line) throws Exception {
    ConcatenatePDF arquivos = new ConcatenatePDF();
    arquivos.setNumeration(false);
    byte[] arquivo = null;
    
    BigDecimal numeroAdiant = (BigDecimal) line.getCampo("NUADIANT");
    if (numeroAdiant == null) {
      throw new Exception("Campo NUADIANT não preenchido.");
    }

    QueryExecutor queryCON = ctx.getQuery();
    // Query que identifica os títulos vinculados ao adiantamento
    queryCON.nativeSelect(
        " SELECT DISTINCT FIN.NUFIN, FIN.CODCTABCOINT, FIN.DHBAIXA " + 
        " FROM TGFFIN FIN " + 
        " WHERE (FIN.NUCOMPENS = " + numeroAdiant + " OR FIN.NUACERTO = " + numeroAdiant + " OR FIN.NUFIN = " + numeroAdiant + ") " + 
        "   AND FIN.RECDESP = 1 "
    );

    boolean sucesso = false;
    int titulosEncontrados = 0;
    
    while (queryCON.next()) {
      titulosEncontrados++;
      BigDecimal nuFin = queryCON.getBigDecimal("NUFIN");
      BigDecimal codCtabcoBig = BigDecimalUtil.getValueOrZero(queryCON.getBigDecimal("CODCTABCOINT"));
      Timestamp dhBaixa = queryCON.getTimestamp("DHBAIXA");

      System.out.println("Processando NUFIN: " + nuFin + " para ADIANT: " + numeroAdiant);

      if (codCtabcoBig.compareTo(BigDecimal.ZERO) == 0) {
        ctx.setMensagemRetorno("A conta bancária deve estar preenchida (Financeiro: " + nuFin + ")");
        return;
      }
      
      if (dhBaixa != null) {
        ctx.setMensagemRetorno("O título " + nuFin + " já está baixado e não permite mais a geração de boleto.");
        sucesso = true; // Marcamos como sucesso para não cair na msg de "não encontrado"
        continue;
      }

      try {
        BoletoHelper.ConfiguracaoBoleto cfg = new BoletoHelper.ConfiguracaoBoleto();
        cfg.setGerarNumeroBoleto(true);
        cfg.setUsaContaBcoFinanceiros(true);
        cfg.setFinanceirosSelecionados(Arrays.asList(new BigDecimal[] { nuFin }));
        cfg.setTipoSaidaBoleto(1);
        
        BoletoHelper boletoHelper = new BoletoHelper();
        boletoHelper.gerarBoleto(cfg);
        
        byte[] boleto = null;
        try {
            boleto = boletoHelper.getBoletosPDF();
        } catch (Exception eb) {
            System.out.println("BoletoHelper.getBoletosPDF falhou para " + nuFin + ": " + eb.getMessage());
        }
        
        if (boleto != null && boleto.length > 0) {
          arquivos.addPdfFile(boleto);
          sucesso = true;
        }
      } catch (Exception e2) {
        e2.printStackTrace();
        throw new Exception("Erro técnico para o título " + nuFin + ": " + e2.getMessage());
      }
    }
    
    if (titulosEncontrados == 0) {
        ctx.setMensagemRetorno("Nenhum título financeiro de receita encontrado para o Número de Adiantamento: " + numeroAdiant);
    } else if (sucesso && arquivos.getSize() > 0) {
        arquivo = arquivos.run().toByteArray();
        SessionFile fileReport = SessionFile.createSessionFile("RelatorioBoleto", "application/pdf", arquivo);
        String chaveSessaoArquivo = UIDGenerator.getNextID();
        ServiceContext.getCurrent().putHttpSessionAttribute(chaveSessaoArquivo, (Serializable) fileReport);
        
        ctx.setMensagemRetorno(
            String.format("%s", new Object[] { getLinkBaixar("Clique aqui para Visualizar o Boleto.", chaveSessaoArquivo) }));
    }
  }

  private String getLinkBaixar(String descricao, String chave) {
    String url = "<a title=\"Visualizar Arquivo\" href=\"/mge/visualizadorArquivos.mge?chaveArquivo=" + chave + 
                 "\" target=\"_blank\"><u><b>" + descricao + "</b></u></a>";
    return url;
  }
}
