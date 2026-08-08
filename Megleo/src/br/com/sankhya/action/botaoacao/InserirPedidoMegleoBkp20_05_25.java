package br.com.sankhya.action.botaoacao;

import br.com.sankhya.action.botaoacao.BuscaTransportadora.BuscaTransportadoraPedido;
import br.com.sankhya.action.funcoes.RegistraLOG;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InserirPedidoMegleoBkp20_05_25
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
                existeTokenMeg = new JSONObject(this.getConfigMegleo());
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
                    retornoCriarPedido = new JSONObject(this.criaPedMeg(contexto, id, token, dados_config));
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
                ** try [egrp 3[TRYBLOCK] [1 : 623->643)] {
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
                    new RegistraLOG().insereRegistro(tituloLog, conteudoLog.toString(), nunotaLog);
                }
                catch (Exception logEx) {
                    System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
                }
            }
lbl-1000:

 {
                new RegistraLOG().insereRegistro(tituloLog, conteudoLog.toString(), nunotaLog);
            }
lbl62:

            catch (Exception logEx) {
                System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
            }
 {
            }
        }
    }

    public String getConfigMegleo() throws Exception {
        StringBuilder conteudoLog = new StringBuilder();
        String tituloLog = "Configura\u00e7\u00e3o Megleo";
        BigDecimal nunotaLog = this.nunota;
        try {
            JdbcWrapper jdbcWrapper = null;
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbcWrapper = dwfEntityFacade.getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbcWrapper);
            JSONObject retorno = new JSONObject();
            sql.appendSql("SELECT * FROM AD_CONFIGMEG");
            ResultSet resultSet = sql.executeQuery();
            if (resultSet.next()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                JSONObject dados = new JSONObject();
                block36: for (int i = 1; i <= columnCount; ++i) {
                    String columnName = metaData.getColumnName(i);
                    switch (columnName.toLowerCase()) {
                        case "sql_volumes": {
                            dados.put("sql_volumes", (Object)resultSet.getString("sql_volumes"));
                            continue block36;
                        }
                        case "sql_endereco_coleta": {
                            dados.put("sql_endereco_coleta", (Object)resultSet.getString("sql_endereco_coleta"));
                            continue block36;
                        }
                        case "sql_endereco_entrega": {
                            dados.put("sql_endereco_entrega", (Object)resultSet.getString("sql_endereco_entrega"));
                            continue block36;
                        }
                        case "token": {
                            dados.put("TOKEN", (Object)resultSet.getString("TOKEN"));
                            continue block36;
                        }
                        case "comprimento": {
                            dados.put("comprimento", (Object)resultSet.getString("comprimento"));
                            continue block36;
                        }
                        case "largura": {
                            dados.put("largura", (Object)resultSet.getString("largura"));
                            continue block36;
                        }
                        case "altura": {
                            dados.put("altura", (Object)resultSet.getString("altura"));
                            continue block36;
                        }
                        case "peso": {
                            dados.put("peso", (Object)resultSet.getString("peso"));
                            continue block36;
                        }
                        case "qtd": {
                            dados.put("qtd", (Object)resultSet.getString("qtd"));
                        }
                    }
                }
                conteudoLog.append("Dados de configura\u00e7\u00e3o carregados com sucesso: ").append(dados.toString()).append("\n");
                System.out.println("Megleo - #15 Dados de Configura\u00e7\u00e3o: " + dados.toString());
                retorno.put("result", (Object)"true");
                retorno.put("dados", (Object)dados);
                String string = retorno.toString();
                return string;
            }
            conteudoLog.append("Nenhum registro encontrado na tabela de configura\u00e7\u00e3o AD_CONFIGMEG\n");
            System.out.println("Megleo - #14 Sem registros na tabela AD_CONFIGMEG");
            retorno.put("result", (Object)"false");
            retorno.put("dados", (Object)"Token n\u00e3o existe");
            String string = retorno.toString();
            return string;
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            conteudoLog.append("Erro ao obter configura\u00e7\u00e3o Megleo: ").append(e.getMessage()).append("\n").append(sw.toString());
            mensagem.append("Erro getTokenMegleo: " + e.getMessage() + sw.toString());
            JSONObject retorno = new JSONObject();
            retorno.put("result", (Object)"false");
            retorno.put("dados", (Object)mensagem.toString());
            String string = retorno.toString();
            return string;
        }
        finally {
            try {
                new RegistraLOG().insereRegistro(tituloLog, conteudoLog.toString(), nunotaLog);
            }
            catch (Exception logEx) {
                System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
            }
        }
    }

    public String getPedMeg(String id) throws Exception {
        StringBuilder conteudoLog = new StringBuilder();
        String tituloLog = "Consulta Pedido Megleo";
        BigDecimal nunotaLog = new BigDecimal(id);
        try {
            JdbcWrapper jdbcWrapper = null;
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbcWrapper = dwfEntityFacade.getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbcWrapper);
            JSONObject retorno = new JSONObject();
            sql.appendSql("SELECT * FROM AD_PEDMEG WHERE NUNOTA = " + id);
            conteudoLog.append("Consulta SQL executada: SELECT * FROM AD_PEDMEG WHERE NUNOTA = ").append(id).append("\n");
            ResultSet resultSet = sql.executeQuery();
            if (resultSet.next()) {
                retorno.put("result", (Object)"true");
                JSONObject dados = new JSONObject();
                dados.put("CHAVERASTREIO", (Object)resultSet.getString("CHAVERASTREIO"));
                dados.put("ETAPA", (Object)resultSet.getString("ETAPA"));
                dados.put("NUNOTA", (Object)resultSet.getString("NUNOTA"));
                retorno.put("dados", (Object)dados.toString());
                conteudoLog.append("Registro encontrado: ").append(dados.toString()).append("\n");
            } else {
                retorno.put("result", (Object)"false");
                retorno.put("dados", (Object)"Pedido megleo n\u00e3o existe");
                conteudoLog.append("Nenhum registro encontrado para NUNOTA = ").append(id).append("\n");
            }
            String string = retorno.toString();
            return string;
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            conteudoLog.append("Erro na execu\u00e7\u00e3o da consulta: ").append(e.getMessage()).append("\n").append(sw.toString());
            mensagem.append("Erro getPedMeg: " + e.getMessage() + sw.toString());
            JSONObject retorno = new JSONObject();
            retorno.put("result", (Object)"false");
            retorno.put("dados", (Object)mensagem.toString());
            String string = retorno.toString();
            return string;
        }
        finally {
            try {
                new RegistraLOG().insereRegistro(tituloLog, conteudoLog.toString(), nunotaLog);
            }
            catch (Exception logEx) {
                System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
            }
        }
    }

    public String getPedMegbyPk(String chave_rastreio) throws Exception {
        StringBuilder conteudoLog = new StringBuilder();
        String tituloLog = "Consulta Pedido por ChaveRastreio";
        BigDecimal nunotaLog = null;
        try {
            JdbcWrapper jdbcWrapper = null;
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbcWrapper = dwfEntityFacade.getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbcWrapper);
            JSONObject retorno = new JSONObject();
            sql.appendSql("SELECT * FROM AD_PEDMEG WHERE CHAVERASTREIO = '" + chave_rastreio + "'");
            conteudoLog.append("Consulta SQL executada: SELECT * FROM AD_PEDMEG WHERE CHAVERASTREIO = '").append(chave_rastreio).append("'\n");
            ResultSet resultSet = sql.executeQuery();
            if (resultSet.next()) {
                retorno.put("result", (Object)"true");
                JSONObject dados = new JSONObject();
                dados.put("CHAVERASTREIO", (Object)resultSet.getString("CHAVERASTREIO"));
                dados.put("ETAPA", (Object)resultSet.getString("ETAPA"));
                dados.put("NUNOTA", (Object)resultSet.getString("NUNOTA"));
                retorno.put("dados", (Object)dados.toString());
                conteudoLog.append("Registro encontrado: ").append(dados.toString()).append("\n");
                try {
                    nunotaLog = new BigDecimal(resultSet.getString("NUNOTA"));
                }
                catch (Exception exception) {}
            } else {
                retorno.put("result", (Object)"false");
                retorno.put("dados", (Object)"Pedido megleo n\u00e3o existe");
                conteudoLog.append("Nenhum registro encontrado para CHAVERASTREIO = ").append(chave_rastreio).append("\n");
            }
            String string = retorno.toString();
            return string;
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            conteudoLog.append("Erro na execu\u00e7\u00e3o da consulta: ").append(e.getMessage()).append("\n").append(sw.toString());
            mensagem.append("Erro getPedMegbyPK: " + e.getMessage() + sw.toString());
            JSONObject retorno = new JSONObject();
            retorno.put("result", (Object)"false");
            retorno.put("dados", (Object)mensagem.toString());
            String string = retorno.toString();
            return string;
        }
        finally {
            try {
                new RegistraLOG().insereRegistro(tituloLog, conteudoLog.toString(), nunotaLog);
            }
            catch (Exception logEx) {
                System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
            }
        }
    }

    public String metodoPost(String url, String token, String params) throws IOException, InterruptedException, JSONException {
        StringBuilder conteudoLog = new StringBuilder();
        String tituloLog = "Chamada POST Megleo";
        BigDecimal nunotaLog = this.nunota;
        try {
            String line;
            String mUrl = "https:
            conteudoLog.append("URL requisitada: ").append(mUrl).append("\n");
            URL urlAuh = new URL(mUrl);
            HttpURLConnection con = (HttpURLConnection)urlAuh.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + token);
            JSONObject JSONParams = new JSONObject(params);
            if (JSONParams.has("cnpj")) {
                String cnpj = JSONParams.get("cnpj").toString();
                con.setRequestProperty("cnpj", cnpj);
                conteudoLog.append("CNPJ informado no header: ").append(cnpj).append("\n");
            }
            con.setDoOutput(true);
            String bodyReq = new String(params.getBytes("UTF-8"));
            conteudoLog.append("Body enviado: ").append(bodyReq).append("\n");
            OutputStream os = con.getOutputStream();
            os.write(bodyReq.getBytes("UTF-8"));
            os.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder outPut = new StringBuilder();
            while ((line = br.readLine()) != null) {
                outPut.append(line);
            }
            con.disconnect();
            conteudoLog.append("Resposta recebida: ").append(outPut.toString()).append("\n");
            JSONObject response = new JSONObject(outPut.toString());
            String string = response.toString();
            return string;
        }
        catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            mensagem.append("Erro: " + e.getMessage() + sw.toString());
            conteudoLog.append("Erro na chamada POST: ").append(e.getMessage()).append("\n").append(sw.toString());
            JSONObject retorno = new JSONObject();
            retorno.put("result", 0);
            retorno.put("dados", (Object)mensagem.toString());
            String string = retorno.toString();
            return string;
        }
        finally {
            try {
                new RegistraLOG().insereRegistro(tituloLog, conteudoLog.toString(), nunotaLog);
            }
            catch (Exception logEx) {
                System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
            }
        }
    }

    public String montaJsonPedido(ContextoAcao contexto, String id, JSONObject dados_config) throws Exception {
        StringBuilder conteudoLog = new StringBuilder();
        String tituloLog = "Consulta Pedido por ChaveRastreio";
        BigDecimal nunotaLog = this.nunota;
        try {
            String string;
            JdbcWrapper jdbc = null;
            NativeSql sql = null;
            NativeSql sql2 = null;
            NativeSql sqlTransp = null;
            NativeSql sqlEndEntrega2 = null;
            NativeSql sqlNota = null;
            NativeSql sqlEnderecoColeta = null;
            NativeSql sqlEnderecoEntrega = null;
            try {
                EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
                jdbc = dwfEntityFacade.getJdbcWrapper();
                jdbc.openSession();
                sql = new NativeSql(jdbc);
                sqlEndEntrega2 = new NativeSql(jdbc);
                sqlNota = new NativeSql(jdbc);
                JSONObject novo_pedido = new JSONObject();
                sqlEnderecoColeta = new NativeSql(jdbc);
                sqlEnderecoEntrega = new NativeSql(jdbc);
                sql2 = new NativeSql(jdbc);
                sqlTransp = new NativeSql(jdbc);
                JSONObject retorno = new JSONObject();
                retorno.put("result", (Object)"true");
                sql.appendSql("select tgfcab.nunota,tgfcab.VLRNOTA, tgfcab.CODPARCTRANSP as codparctransp, tgfcab.DTPREVENT AS DTPREVENT, tgfcab.cif_fob, TGFCAB.CODPARc, VLRFRETE,VLRFRETETOTAL, tsiemp.cgc as cnpj, tgfpar.email, tgfpar.telefone, tgfpar.nomeparc, tgfpar.tippessoa, tgfpar.cgc_cpf, tgfnfe.chavenfe from TGFCAB JOIN tgfpar ON TGFCAB.CODPARc = TGFPAR.codparc LEFT JOIN tgfnfe on tgfnfe.nunota = tgfcab.nunota JOIN tsiemp on tsiemp.CODEMP = TGFCAB.codemp  where tgfcab.nunota = " + id);
                sqlTransp.appendSql("select tgfpar.CGC_CPF as cnpj_transportadora from TGFCAB JOIN tgfpar ON TGFCAB.CODPARCTRANSP = TGFPAR.codparc where tgfcab.nunota = " + id);
                if (dados_config.has("sql_endereco_coleta")) {
                    String novaSqlEnderecoColeta = dados_config.get("sql_endereco_coleta").toString();
                    sqlEnderecoColeta.appendSql(novaSqlEnderecoColeta);
                    sqlEnderecoColeta.setNamedParameter("id", (Object)id);
                } else {
                    sqlEnderecoColeta.appendSql("select tgfcab.nunota, tsiemp.cep as cep, tsiend.nomeend as logradouro,tsiemp.NUMEND as numero, tsibai.nomebai as bairro, tgfpar.complemento  from TGFCAB JOIN tsiemp on tsiemp.CODEMP = TGFCAB.codemp JOIN tsiend on tsiend.codend = tsiemp.codend JOIN tsibai on tsibai.codbai = tsiemp.codbai INNER JOIN tgfpar on tgfpar.codparc = tgfcab.codparc where tgfcab.nunota = " + id);
                }
                System.out.println("");
                if (dados_config.has("sql_endereco_entrega")) {
                    String novaSqlEnderecoEntrega = dados_config.get("sql_endereco_entrega").toString();
                    sqlEnderecoEntrega.appendSql(novaSqlEnderecoEntrega);
                    sqlEnderecoEntrega.setNamedParameter("id", (Object)id);
                } else {
                    sqlEndEntrega2.appendSql("select TGFCPL.CODPARC, TGFCPL.cepentrega  from TGFCPL  JOIN tgfpar ON tgfpar.CODPARC = TGFCPL.CODPARC  JOIN tgfcab on tgfcab.codparc = tgfpar.CODPARC  where tgfcab.nunota = " + id);
                    ResultSet resultSetEndEntrega = sqlEndEntrega2.executeQuery();
                    if (resultSetEndEntrega.next() && !"".equals(resultSetEndEntrega.getString("cepentrega")) && null != resultSetEndEntrega.getString("cepentrega")) {
                        sqlEnderecoEntrega.appendSql("select tgfcpl.codparc,tgfcpl.cepentrega as cep, tsiend.nomeend as logradouro,TGFCPL.NUMENTREGA as numero, tsibai.nomebai as bairro, tgfpar.complemento from TGFCPL JOIN tgfpar ON tgfpar.CODPARC = tgfcpl.CODPARC JOIN tgfcab on tgfcab.codparc = tgfpar.CODPARC JOIN tsiend on tsiend.codend = tgfcpl.codendentrega JOIN tsibai on tsibai.codbai = tgfcpl.codbaientrega where tgfcab.nunota = " + id);
                    } else {
                        sqlEnderecoEntrega.appendSql("SELECT CAB.NUNOTA,\nCASE WHEN PAR.ENTREGAENDCONTATO = 'S' THEN (SELECT CEP FROM TGFCTT WHERE CODPARC = CAB.CODPARC AND CODCONTATO = CODCONTATOENTREGA) ELSE PAR.CEP END as cep, \nENDE.NOMEEND as logradouro,\nCASE WHEN PAR.ENTREGAENDCONTATO = 'S' THEN (SELECT NUMEND FROM TGFCTT WHERE CODPARC = CAB.CODPARC AND CODCONTATO = CODCONTATOENTREGA) ELSE PAR.NUMEND END as numero, \nBAI.NOMEBAI as bairro, PAR.complemento  \nFROM TGFCAB CAB INNER JOIN TGFPAR PAR ON CAB.CODPARC = PAR.CODPARC \n                INNER JOIN TSIEND ENDE ON ENDE.CODEND = CASE WHEN PAR.ENTREGAENDCONTATO = 'S' THEN (SELECT CODEND FROM TGFCTT WHERE CODPARC = CAB.CODPARC AND CODCONTATO = CODCONTATOENTREGA) ELSE PAR.CODEND END\n                INNER JOIN TSIBAI BAI ON  BAI.CODBAI = CASE WHEN PAR.ENTREGAENDCONTATO = 'S' THEN (SELECT CODBAI FROM TGFCTT WHERE CODPARC = CAB.CODPARC AND CODCONTATO = CODCONTATOENTREGA) ELSE PAR.CODBAI END\n                WHERE CAB.NUNOTA =  " + id);
                    }
                }
                ResultSet resultColeta = sqlEnderecoColeta.executeQuery();
                ResultSet resultEntrega = sqlEnderecoEntrega.executeQuery();
                if (resultColeta.next()) {
                    JSONObject endereco_entrada = new JSONObject();
                    this.cepOrigem = resultColeta.getString("cep");
                    endereco_entrada.put("rua", (Object)resultColeta.getString("logradouro"));
                    endereco_entrada.put("numero", (Object)resultColeta.getString("numero"));
                    endereco_entrada.put("bairro", (Object)resultColeta.getString("bairro"));
                    endereco_entrada.put("complemento", (Object)resultColeta.getString("complemento"));
                    novo_pedido.put("endereco_coleta", (Object)endereco_entrada);
                    novo_pedido.put("cep_origem", (Object)this.cepOrigem);
                } else {
                    retorno.put("result", (Object)"false");
                    retorno.put("dados", (Object)"Erro ao buscar endere\u00e7o de entrega!");
                }
                if (resultEntrega.next()) {
                    JSONObject endereco_saida = new JSONObject();
                    this.cepDestino = resultEntrega.getString("cep");
                    endereco_saida.put("rua", (Object)resultEntrega.getString("logradouro"));
                    endereco_saida.put("numero", (Object)resultEntrega.getString("numero"));
                    endereco_saida.put("bairro", (Object)resultEntrega.getString("bairro"));
                    endereco_saida.put("complemento", (Object)resultEntrega.getString("complemento"));
                    novo_pedido.put("endereco_entrega", (Object)endereco_saida);
                    novo_pedido.put("cep_destino", (Object)this.cepDestino);
                } else {
                    retorno.put("result", (Object)"false");
                    retorno.put("dados", (Object)"Erro ao buscar endere\u00e7o de entrega!");
                }
                ResultSet resultSet = sql.executeQuery();
                if (resultSet.next()) {
                    novo_pedido.put("vlrfrete", (Object)resultSet.getString("VLRFRETE"));
                    novo_pedido.put("forma_coleta", (Object)"coleta");
                    novo_pedido.put("forma_entrega", (Object)"entrega");
                    novo_pedido.put("cupom", (Object)"");
                    if (this.paramOrder != null) {
                        novo_pedido.put("order_by", (Object)this.paramOrder.trim());
                    }
                    JSONObject destinatario = new JSONObject();
                    String tipo_pessoa = null;
                    if (resultSet.getString("tippessoa").equalsIgnoreCase("F")) {
                        tipo_pessoa = "pessoa_fisica";
                        destinatario.put("cpf", (Object)resultSet.getString("cgc_cpf").trim());
                        destinatario.put("nome", (Object)resultSet.getString("nomeparc"));
                    } else {
                        tipo_pessoa = "pessoa_juridica";
                        destinatario.put("cnpj", (Object)resultSet.getString("cgc_cpf"));
                        destinatario.put("razao_social", (Object)resultSet.getString("nomeparc"));
                    }
                    this.cnpj_destinatario = resultSet.getString("cgc_cpf");
                    destinatario.put("tipo_pessoa", (Object)tipo_pessoa);
                    destinatario.put("telefone", (Object)resultSet.getString("telefone"));
                    if (resultSet.getString("email") != null) {
                        destinatario.put("email", (Object)resultSet.getString("email").replaceAll("\\s", ""));
                    }
                    novo_pedido.put("destinatario", (Object)destinatario);
                    novo_pedido.put("chave_rastreio_embarcadora", (Object)("sankhya " + resultSet.getString("nunota")));
                    novo_pedido.put("cnpj", (Object)resultSet.getString("cnpj"));
                    novo_pedido.put("valor_nota_fiscal", (Object)resultSet.getString("VLRNOTA"));
                    this.vlrPedido = resultSet.getString("VLRNOTA");
                    BigDecimal codparcTranspCab = resultSet.getBigDecimal("CODPARCTRANSP");
                    BigDecimal vlrFreteTranspCab = resultSet.getBigDecimal("VLRFRETE");
                    Timestamp dtPrevisaoEntregaCAB = resultSet.getTimestamp("DTPREVENT");
                    String cnpjTransCab = "";
                    String razaoSocialTransCab = "";
                    NativeSql sqlBuscaDadosTransportador = new NativeSql(jdbc);
                    ResultSet rsBuscaDadosTransportador = sqlBuscaDadosTransportador.executeQuery("SELECT CGC_CPF, RAZAOSOCIAL FROM TGFPAR WHERE CODPARC = " + codparcTranspCab);
                    if (rsBuscaDadosTransportador.next()) {
                        cnpjTransCab = rsBuscaDadosTransportador.getString("CGC_CPF");
                        razaoSocialTransCab = rsBuscaDadosTransportador.getString("RAZAOSOCIAL");
                    }
                    if (cnpjTransCab != null) {
                        novo_pedido.put("cnpj_transportadora_erp", (Object)cnpjTransCab);
                        novo_pedido.put("nome_transportadora_erp", (Object)razaoSocialTransCab);
                    }
                    if (vlrFreteTranspCab != null) {
                        novo_pedido.put("valor_transportadora_erp", (Object)vlrFreteTranspCab);
                    }
                    if (dtPrevisaoEntregaCAB != null) {
                        LocalDate dataCampo = dtPrevisaoEntregaCAB.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        LocalDate dataAtual = LocalDate.now();
                        int prazoTranspCab = (int)ChronoUnit.DAYS.between(dataCampo, dataAtual);
                        novo_pedido.put("prazo_transportadora_erp", prazoTranspCab);
                    }
                    retorno.put("nunota", (Object)resultSet.getString("nunota"));
                } else {
                    retorno.put("result", (Object)"false");
                    retorno.put("dados", (Object)"nunota vazio");
                }
                String comprimento = "tgfpro.espessura";
                String largura = "tgfpro.largura";
                String altura = "tgfpro.altura";
                String qtd = "TGFite.QTDNEG";
                String peso = "tgfpro.pesobruto";
                String nova_sql = null;
                if (dados_config.has("comprimento")) {
                    comprimento = "TGFite." + dados_config.get("comprimento").toString();
                }
                if (dados_config.has("altura")) {
                    altura = "TGFite." + dados_config.get("altura").toString();
                }
                if (dados_config.has("largura")) {
                    largura = "TGFite." + dados_config.get("largura").toString();
                }
                if (dados_config.has("qtd")) {
                    qtd = "TGFite." + dados_config.get("qtd").toString();
                }
                if (dados_config.has("peso")) {
                    peso = "TGFite." + dados_config.get("peso").toString();
                }
                if (dados_config.has("sql_volumes")) {
                    nova_sql = dados_config.get("sql_volumes").toString();
                    sql2.appendSql(nova_sql);
                    sql2.setNamedParameter("id", (Object)id);
                } else {
                    String query = "SELECT " + qtd + " AS QTD, TGFPRO.DESCRPROD AS DESCRPROD, TGFPRO.COMPLDESC AS COMPLDESD, TGFPRO.CODPROD AS CODPROD, TGFPRO.NCM AS NCM, TGFITE.CODVOL AS CODVOL, " + altura + " * CASE WHEN TGFPRO.UNIDADE = 'MM' THEN 0.1 WHEN TGFPRO.UNIDADE = 'M' THEN 100 ELSE 1 END AS ALTURA, " + largura + " * CASE WHEN TGFPRO.UNIDADE = 'MM' THEN 0.1 WHEN TGFPRO.UNIDADE = 'M' THEN 100 ELSE 1 END AS LARGURA, " + comprimento + " * CASE WHEN TGFPRO.UNIDADE = 'MM' THEN 0.1 WHEN TGFPRO.UNIDADE = 'M' THEN 100 ELSE 1 END AS COMPRIMENTO, " + peso + " AS PESO FROM TGFITE INNER JOIN TGFPRO ON TGFPRO.CODPROD = TGFITE.CODPROD WHERE TGFITE.NUNOTA = " + id;
                    sql2.appendSql(query);
                }
                ResultSet resultSet2 = sql2.executeQuery();
                this.volumes = new JSONArray();
                this.cargas = new JSONArray();
                while (resultSet2.next()) {
                    JSONObject volume = new JSONObject();
                    this.quantidade = new BigDecimal(Math.round(Float.parseFloat(resultSet2.getString("qtd"))));
                    this.pesoGlobal = resultSet2.getString("peso") != null ? resultSet2.getString("peso") : "0";
                    this.comprimentoGlobal = resultSet2.getString("comprimento") != null ? resultSet2.getString("comprimento") : "0";
                    this.larguraGlobal = resultSet2.getString("largura") != null ? resultSet2.getString("largura") : "0";
                    this.alturaGlobal = resultSet2.getString("altura") != null ? resultSet2.getString("altura") : "0";
                    volume.put("quantidade", (Object)this.quantidade.toString());
                    volume.put("peso", (Object)this.pesoGlobal);
                    volume.put("comprimento", (Object)this.comprimentoGlobal);
                    volume.put("largura", (Object)this.larguraGlobal);
                    volume.put("altura", (Object)this.alturaGlobal);
                    volume.put("un_medida", (Object)"cm");
                    volume.put("sku", (Object)resultSet2.getString("codprod"));
                    volume.put("ncm", (Object)resultSet2.getString("NCM"));
                    volume.put("embalagem", (Object)resultSet2.getString("CODVOL"));
                    this.volumes.put((Object)volume);
                    this.cargas.put((Object)resultSet2.getString("codprod"));
                }
                sqlNota.appendSql("SELECT tgfnfe.chavenfe, tgfnfe.xml, tsiemp.cgc as cnpj   from tgfnfe  JOIN tgfcab on tgfnfe.nunota = TGFCAB.nunota  JOIN tsiemp on tsiemp.CODEMP = TGFCAB.codemp  where tgfnfe.nunota = " + id);
                ResultSet resultSetNota = sqlNota.executeQuery();
                if (resultSetNota.next()) {
                    novo_pedido.put("nota_fiscal", (Object)resultSetNota.getString("xml"));
                    retorno.put("tipo", (Object)"nota");
                    ResultSet resultSetTranps = sqlTransp.executeQuery();
                    if (resultSetTranps.next()) {
                        if (resultSetTranps.getString("cnpj_transportadora") == null) {
                            retorno.put("result", (Object)"false");
                            retorno.put("dados", (Object)"Transportadora n\u00e3o selecionada");
                        } else {
                            novo_pedido.put("cnpj_transportadora", (Object)resultSetTranps.getString("cnpj_transportadora"));
                        }
                    } else {
                        retorno.put("result", (Object)"false");
                        retorno.put("dados", (Object)"Transportadora n\u00e3o selecionada");
                    }
                } else {
                    retorno.put("tipo", (Object)"pedido");
                }
                JSONObject existePedMeg = new JSONObject(this.getPedMeg(id));
                if ("true".equals(existePedMeg.get("result").toString())) {
                    JSONObject dados_pedmeg = new JSONObject(existePedMeg.get("dados").toString());
                    novo_pedido.put("chave_cotacao", (Object)dados_pedmeg.get("CHAVERASTREIO").toString());
                }
                novo_pedido.put("volume", (Object)this.volumes);
                novo_pedido.put("tipos_carga", (Object)this.cargas);
                novo_pedido.put("correlacao_tipos_carga", (Object)"sankhya");
                if ("true".equals(retorno.get("result").toString())) {
                    retorno.put("dados", (Object)novo_pedido.toString());
                }
                retorno.put("id", (Object)id);
                string = retorno.toString();
            }
            catch (Throwable throwable) {
                try {
                    NativeSql.releaseResources(sql);
                    NativeSql.releaseResources(sqlEndEntrega2);
                    JdbcWrapper.closeSession((JdbcWrapper)jdbc);
                    throw throwable;
                }
                catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    StringBuffer mensagem = new StringBuffer();
                    e.printStackTrace(pw);
                    mensagem.append("Erro em montaJsonPedido: " + e.getMessage() + sw.toString());
                    conteudoLog.append("Erro na chamada POST: ").append(e.getMessage()).append("\n").append(sw.toString());
                    JSONObject retorno = new JSONObject();
                    retorno.put("result", (Object)"false");
                    retorno.put("dados", (Object)mensagem.toString());
                    String string2 = retorno.toString();
                    return string2;
                }
            }
            NativeSql.releaseResources((NativeSql)sql);
            NativeSql.releaseResources((NativeSql)sqlEndEntrega2);
            JdbcWrapper.closeSession((JdbcWrapper)jdbc);
            return string;
        }
        finally {
            try {
                new RegistraLOG().insereRegistro(tituloLog, conteudoLog.toString(), nunotaLog);
            }
            catch (Exception logEx) {
                System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
            }
        }
    }

    public String criaPedMeg(ContextoAcao contexto, String id, String token, JSONObject dados_config) throws Exception {
        NativeSql sql2;
        NativeSql sql;
        JapeSession.SessionHandle hnd;
        JdbcWrapper jdbc;
        String chave_cotacao;
        JSONObject retorno;
        StringBuilder conteudoLog = new StringBuilder();
        String tituloLog = "Cria Pedido";
        BigDecimal nunotaLog = this.nunota;
        try {
            String response;
            JSONObject jsonPedido = new JSONObject(this.montaJsonPedido(contexto, id, dados_config));
            retorno = new JSONObject();
            JSONObject JSONresponse = new JSONObject();
            retorno.put("jsonPedido", (Object)(jsonPedido.toString() + "\n"));
            if ("false".equals(jsonPedido.get("result").toString())) {
                retorno.put("result", (Object)"false");
                retorno.put("dados", jsonPedido.get("dados"));
                return retorno.toString();
            }
            if (!"true".equals(jsonPedido.get("result").toString())) return retorno.toString();
            if ("pedido".equals(jsonPedido.get("tipo").toString())) {
                response = this.metodoPost("/api/v1_2/pedidos/criar_simplificado", token, jsonPedido.get("dados").toString());
                JSONresponse = new JSONObject(response);
            } else if ("nota".equals(jsonPedido.get("tipo").toString())) {
                response = this.metodoPost("/api/v1_2/pedidos/criar_sankhya", token, jsonPedido.get("dados").toString());
                JSONresponse = new JSONObject(response);
            }
            if (JSONresponse.has("error")) {
                retorno.put("result", (Object)"false");
                retorno.put("dados", JSONresponse.get("error"));
                return retorno.toString();
            }
            if (JSONresponse.has("data")) {
                chave_cotacao = JSONresponse.getJSONObject("data").get("chave_cotacao").toString();
                String prazo = "";
                if (JSONresponse.getJSONObject("data").has("prazo")) {
                    prazo = JSONresponse.getJSONObject("data").get("prazo").toString();
                }
                String transportadora_nome = "";
                String transportadora_cnpj = "";
                if (JSONresponse.getJSONObject("data").has("transportadora")) {
                    transportadora_nome = JSONresponse.getJSONObject("data").getJSONObject("transportadora").get("nome_fantasia").toString();
                    transportadora_cnpj = JSONresponse.getJSONObject("data").getJSONObject("transportadora").get("cnpj").toString();
                    transportadora_cnpj = transportadora_cnpj.trim().replaceAll("\\D", "");
                }
                String valor_fatura = "";
                BigDecimal valor_fatura_float = new BigDecimal(0);
                if (JSONresponse.getJSONObject("data").has("valor_fatura")) {
                    valor_fatura = JSONresponse.getJSONObject("data").get("valor_fatura").toString();
                    valor_fatura_float = new BigDecimal(JSONresponse.getJSONObject("data").get("valor_fatura").toString());
                }
                String nova_etapa = null;
                nova_etapa = "pedido".equals(jsonPedido.get("tipo").toString()) ? "ADICIONAR_NOTA_FISCAL" : "FINALIZADO";
                jdbc = null;
                hnd = null;
                sql = null;
                sql2 = null;
                try {
                    hnd = JapeSession.open();
                    EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
                    jdbc = dwfEntityFacade.getJdbcWrapper();
                    jdbc.openSession();
                    sql = new NativeSql(jdbc);
                    sql2 = new NativeSql(jdbc);
                    final BigDecimal nunota = new BigDecimal(id);
                    sql.appendSql("select * from tgfpar where CGC_CPF = '" + transportadora_cnpj + "' AND ATIVO = 'S'");
                    ResultSet resultSet = sql.executeQuery();
                    System.out.println("Megleo - #5 Transportador recebido: CNPJ " + transportadora_cnpj);
                    if (!resultSet.next()) {
                        retorno.put("result", (Object)"false");
                        retorno.put("dados", (Object)("N\u00e3o h\u00e1 transportadora com CNPJ " + transportadora_cnpj + " correspondente (Sankhya)\n"));
                        System.out.println("Megleo - #8 N\u00e3o foram encontrados parceiros com este CNPJ.");
                        String codparc = retorno.toString();
                        return codparc;
                    }
                    final BigDecimal codparc = resultSet.getBigDecimal("CODPARC");
                    if (codparc == null) {
                        retorno.put("result", (Object)"false");
                        retorno.put("dados", (Object)("c " + transportadora_cnpj + " correspondente (Sankhya). \n"));
                        System.out.println("Megleo - #6 N\u00e3o foram encontrados parceiros com este CNPJ.");
                        String string = retorno.toString();
                        return string;
                    }
                    if ("pedido".equals(jsonPedido.get("tipo").toString())) {
                        final String tipfrete = "N";
                        final String cif_fob = "C";
                        BigDecimal vlrFreteUpdateCAB = valor_fatura_float;
                        if (this.paramVlrFrete.equalsIgnoreCase("N") || this.paramVlrFrete.equalsIgnoreCase("D")) {
                            vlrFreteUpdateCAB = new BigDecimal(0);
                            System.out.println("Megleo - # Informado do par\u00e2metro de Atualizar Valor do Frete com valor NAO. Frete foi zerado.");
                        }
                        final BigDecimal finalValor_fatura_float = vlrFreteUpdateCAB;
                        hnd.execWithTX(new JapeSession.TXBlock(){

                            public void doWithTx() throws Exception {
                                ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)JapeFactory.dao((String)"CabecalhoNota").prepareToUpdateByPK(new Object[]{nunota}).set("CODPARCTRANSP", (Object)codparc)).set("TIPFRETE", (Object)tipfrete)).set("CIF_FOB", (Object)cif_fob)).set("VLRFRETE", (Object)finalValor_fatura_float)).update();
                                System.out.println("Megleo - #7 Nota atualizada com os valores recebidos. \nC\u00f3d. Parceiro: " + codparc + "\nTipo de Frete: " + tipfrete + "\nCIF_FOB: " + cif_fob + "\nValor do Frete: " + finalValor_fatura_float);
                            }
                        });
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    final String dataFormatada = LocalDateTime.now().format(formatter);
                    final String etapa = nova_etapa;
                    JSONObject existePedMeg2 = new JSONObject(this.getPedMegbyPk(chave_cotacao));
                    if ("false".equals(existePedMeg2.get("result").toString())) {
                        final String finalTransportadora_nome = transportadora_nome;
                        final String finalTransportadora_cnpj = transportadora_cnpj;
                        final BigDecimal finalValor_fatura_float1 = valor_fatura_float;
                        final String finalPrazo = prazo;
                        hnd.execWithTX(new JapeSession.TXBlock(){

                            public void doWithTx() throws Exception {
                                JapeWrapper insert1 = JapeFactory.dao((String)"AD_PEDMEG");
                                DynamicVO insertVO = ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)insert1.create().set("CHAVERASTREIO", (Object)chave_cotacao)).set("ETAPA", (Object)etapa)).set("NUNOTA", (Object)nunota)).set("TRANSPORTADORA", (Object)finalTransportadora_nome)).set("CNPJ_TRANSPORTADORA", (Object)finalTransportadora_cnpj)).set("VALOR_FRETE", (Object)finalValor_fatura_float1)).set("DATA_CRIACAO", (Object)dataFormatada)).set("PRAZO", (Object)finalPrazo)).set("ATUALIZA_FRETE", (Object)InserirPedidoMegleoBkp20_05_25.this.paramVlrFrete)).save();
                                System.out.println("Megleo - #9 Criado registro na tabela de Pedidos Megleo. Chave de cota\u00e7\u00e3o: " + chave_cotacao);
                            }
                        });
                        return retorno.toString();
                    } else {
                        if (!"true".equals(existePedMeg2.get("result").toString())) return retorno.toString();
                        final String finalTransportadora_nome1 = transportadora_nome;
                        final String finalTransportadora_cnpj1 = transportadora_cnpj;
                        final BigDecimal finalValor_fatura_float2 = valor_fatura_float;
                        final String finalPrazo1 = prazo;
                        hnd.execWithTX(new JapeSession.TXBlock(){

                            public void doWithTx() throws Exception {
                                ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)JapeFactory.dao((String)"AD_PEDMEG").prepareToUpdateByPK(new Object[]{chave_cotacao}).set("ETAPA", (Object)etapa)).set("NUNOTA", (Object)nunota)).set("TRANSPORTADORA", (Object)finalTransportadora_nome1)).set("CNPJ_TRANSPORTADORA", (Object)finalTransportadora_cnpj1)).set("VALOR_FRETE", (Object)finalValor_fatura_float2)).set("DATA_CRIACAO", (Object)dataFormatada)).set("PRAZO", (Object)finalPrazo1)).set("ATUALIZA_FRETE", (Object)InserirPedidoMegleoBkp20_05_25.this.paramVlrFrete)).update();
                                System.out.println("Megleo - #10 Criado registro na tabela de Pedidos Megleo. Chave de cota\u00e7\u00e3o: " + chave_cotacao);
                            }
                        });
                    }
                    return retorno.toString();
                }
                catch (Exception e) {
                    NativeSql.releaseResources(sql);
                    NativeSql.releaseResources(sql2);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    retorno.put("result", (Object)"false");
                    retorno.put("dados", (Object)("Erro insercao pedido " + chave_cotacao + ": " + e.getMessage() + "\n"));
                    System.out.println("Megleo - #11 Erro Inser\u00e7\u00e3o de Pedido.");
                    String string = retorno.toString();
                    try {
                        new RegistraLOG().insereRegistro(tituloLog, conteudoLog.toString(), nunotaLog);
                        return string;
                    }
                    catch (Exception logEx) {
                        System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
                    }
                    return string;
                }
            }
            retorno.put("result", (Object)"false");
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            mensagem.append("Erro criaPedMeg: " + e.getMessage() + sw.toString());
            conteudoLog.append("Erro na chamada POST: ").append(e.getMessage()).append("\n").append(sw.toString());
            JSONObject retorno2 = new JSONObject();
            retorno2.put("result", (Object)"false");
            retorno2.put("dados", (Object)mensagem.toString());
            System.out.println("Megleo - #13 Erro ao criar pedido.");
            String string = retorno2.toString();
            return string;
        }
        retorno.put("dados", (Object)"Erro na comunica\u00e7\u00e3o com a Megleo");
        System.out.println("Megleo - #12 Erro na comunica\u00e7\u00e3o com a Megleo.");
        return retorno.toString();
        finally {
            BuscaTransportadoraPedido buscaTransportadoraPedido = new BuscaTransportadoraPedido();
            System.out.println("Megleo - #15 Iniciando a busca de transportadoras...");
            buscaTransportadoraPedido.buscaTransportadora(this.tokenMegleo, chave_cotacao, this.vlrPedido, this.cepOrigem, this.cepDestino, this.volumes, this.nunota, this.cargas, this.cnpj_destinatario);
            retorno.put("result", (Object)"true");
            JdbcWrapper.closeSession((JdbcWrapper)jdbc);
            JapeSession.close((JapeSession.SessionHandle)hnd);
            NativeSql.releaseResources((NativeSql)sql);
            NativeSql.releaseResources((NativeSql)sql2);
        }
        finally {
            try {
                new RegistraLOG().insereRegistro(tituloLog, conteudoLog.toString(), nunotaLog);
            }
            catch (Exception logEx) {
                System.out.println("Erro ao registrar log no banco: " + logEx.getMessage());
            }
        }
    }

    public static String getCallerClassNameAndLineNumber() {
        int callerDepth = 3;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            return caller.getClassName() + ":" + caller.getLineNumber();
        }
        return "N\u00e3o foi poss\u00edvel determinar a classe e a linha do chamador";
    }
}
