package br.com.sankhya.action.botaoacao;

import br.com.sankhya.action.funcoes.EnviaPedido;
import br.com.sankhya.action.funcoes.RegistraLOG;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import org.json.JSONArray;
import org.json.JSONObject;

public class InserirPedidoMegleo
implements AcaoRotinaJava {
    private String cepOrigem;
    private String cepDestino;
    private String tokenMegleo;
    private String pesoGlobal;
    private String comprimentoGlobal;
    private String alturaGlobal;
    private String larguraGlobal;
    private String vlrPedido;
    private String paramOrder;
    private String paramVlrFrete;
    private String cnpj_destinatario;
    private BigDecimal quantidade;
    private BigDecimal nunota;
    private JSONArray volumes = new JSONArray();
    private JSONArray cargas = new JSONArray();

    public void doAction(ContextoAcao contexto) throws Exception {
        block17: {
            conteudoLog = new StringBuilder();
            tituloLog = "Vis\u00e3o Geral";
            nunotaLog = null;
            enviaPedido = new EnviaPedido();
            try {
                System.out.println("\n\n\n\n\n\nMegleo - --------------------------------------------------------------------------\n\n\n\n\n");
                conteudoLog.append("In\u00edcio da execu\u00e7\u00e3o do bot\u00e3o de a\u00e7\u00e3o de integra\u00e7\u00e3o Megleo.\n");
                System.out.println("Megleo - #1 Acionado o botao de acao para insercao de pedido.");
                conteudoLog.append("#1 Bot\u00e3o acionado para inser\u00e7\u00e3o de pedido.\n");
                token = null;
                pedidoMegleo = null;
                sql_volumes = null;
                this.paramOrder = contexto.getParam("ORDER") != null ? contexto.getParam("ORDER").toString() : null;
                this.paramVlrFrete = contexto.getParam("COBRAFRETE") != null ? contexto.getParam("COBRAFRETE").toString().trim() : "N";
                conteudoLog.append("Par\u00e2metro de atualiza\u00e7\u00e3o de frete: ").append(this.paramVlrFrete).append("\n");
                conteudoLog.append("Par\u00e2metro de ordena\u00e7\u00e3o: ").append(this.paramOrder).append("\n");
                mensagemRetorno = new StringBuffer();
                existeTokenMeg = new JSONObject(enviaPedido.getConfigMegleo(true));
                if ("false".equals(existeTokenMeg.get("result"))) {
                    contexto.setMensagemRetorno("Adicione um token na tela de configura\u00e7\u00e3o megleo");
                    conteudoLog.append("Token n\u00e3o encontrado. Processo encerrado.\n");
                    return;
                }
                dados_config = new JSONObject(existeTokenMeg.get("dados").toString());
                this.tokenMegleo = token = dados_config.get("TOKEN").toString();
                System.out.println("Megleo - #2 Configura\u00e7\u00f5es: " + existeTokenMeg);
                conteudoLog.append("Token e configura\u00e7\u00f5es carregadas com sucesso.\n");
                linhasSelecionadas = contexto.getLinhas();
                System.out.println("Megleo - #3 Iniciada a busca de notas.");
                conteudoLog.append("Notas selecionadas: ").append(linhasSelecionadas.length).append("\n");
                for (Registro linha : linhasSelecionadas) {
                    id = linha.getCampo("NUNOTA").toString();
                    nunotaLog = this.nunota = new BigDecimal(id);
                    conteudoLog.append("----------------------------------------------------------\n");
                    conteudoLog.append("Processando nota NUNOTA = ").append(this.nunota).append("\n");
                    System.out.println("Megleo - #4 Iniciado processamento da nota de N\u00ba \u00danico " + id);
                    retornoCriarPedido = new JSONObject(enviaPedido.criaPedMeg(id, token, dados_config, true, this.paramOrder, this.paramVlrFrete));
                    if ("false".equals(retornoCriarPedido.get("result").toString())) {
                        mensagemRetorno.append("\n Pedido ").append(id).append(" n\u00e3o foi inserido na Megleo :\n").append(retornoCriarPedido.get("dados"));
                        conteudoLog.append("Erro ao inserir pedido ").append(id).append(": ").append(retornoCriarPedido.get("dados")).append("\n");
                        continue;
                    }
                    conteudoLog.append("Pedido ").append(id).append(" inserido com sucesso na Megleo.\n");
                }
                mensagemRetorno.append("\nConclu\u00eddo com sucesso.");
                contexto.setMensagemRetorno(mensagemRetorno.toString());
                conteudoLog.append("Processo conclu\u00eddo com sucesso.\n");
                ** try [egrp 3[TRYBLOCK] [1 : 643->663)] {
            }
            catch (Exception e) {
                sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                erroCompleto = sw.toString();
                conteudoLog.append("Erro durante execu\u00e7\u00e3o: ").append(e.getMessage()).append("\n").append(erroCompleto).append("\n");
                contexto.setMensagemRetorno("Erro durante execu\u00e7\u00e3o: " + e.getMessage());
                break block17;
            }
            finally {
                try {
                    new RegistraLOG().insereRegistroTransacaoAutomatica(tituloLog, conteudoLog.toString(), nunotaLog);
                }
                catch (Exception logEx) {
                    System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
                }
            }
lbl-1000:

 {
                new RegistraLOG().insereRegistroTransacaoAutomatica(tituloLog, conteudoLog.toString(), nunotaLog);
            }
lbl63:

            catch (Exception logEx) {
                System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
            }
 {
            }
        }
    }
}
