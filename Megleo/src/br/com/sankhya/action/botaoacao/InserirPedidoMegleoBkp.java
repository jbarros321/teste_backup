package br.com.sankhya.action.botaoacao;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InserirPedidoMegleoBkp
implements AcaoRotinaJava {
    public void doAction(ContextoAcao contexto) throws Exception {
        try {
            Registro[] linhasSelecionadas;
            String token = null;
            Object pedidoMegleo = null;
            Object sql_volumes = null;
            StringBuffer mensagemRetorno = new StringBuffer();
            JSONObject dados_config = null;
            JSONObject existeTokenMeg = new JSONObject(this.getConfigMegleo());
            if ("false".equals(existeTokenMeg.get("result"))) {
                contexto.setMensagemRetorno("Adicione um token na tela de configura\u00e7\u00e3o megleo");
                return;
            }
            if ("true".equals(existeTokenMeg.get("result"))) {
                dados_config = new JSONObject(existeTokenMeg.get("dados").toString());
                token = dados_config.get("TOKEN").toString();
            }
            for (Registro linha : linhasSelecionadas = contexto.getLinhas()) {
                JSONObject existePedMeg2;
                String id = linha.getCampo("NUNOTA").toString();
                JSONObject retornoCriarPedido = new JSONObject(this.criaPedMeg(contexto, id, token, dados_config));
                if ("false".equals(retornoCriarPedido.get("result").toString())) {
                    mensagemRetorno.append("\n Pedido " + id + " n\u00e3o foi inserido na megleo :\n" + retornoCriarPedido.get("dados"));
                    contexto.setMensagemRetorno(mensagemRetorno.toString());
                    continue;
                }
                if ("true".equals(retornoCriarPedido.get("result").toString()) && "false".equals((existePedMeg2 = new JSONObject(this.getPedMeg(id))).get("result").toString())) {
                    mensagemRetorno.append("\n Pedido " + id + " n\u00e3o foi inserido na megleo :\n" + existePedMeg2.get("dados"));
                    contexto.setMensagemRetorno(mensagemRetorno.toString());
                    continue;
                }
                contexto.setMensagemRetorno(mensagemRetorno.toString());
            }
            mensagemRetorno.append("\n Conclu\u00eddo");
            contexto.setMensagemRetorno(mensagemRetorno.toString());
            return;
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            mensagem.append("Erro base: " + e.getMessage() + sw.toString());
            StringBuffer mensagemRetorno = new StringBuffer();
            mensagemRetorno.append(mensagem.toString());
            contexto.setMensagemRetorno(mensagemRetorno.toString());
            return;
        }
    }

    private String getConfigMegleo() throws Exception {
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
                for (int i = 1; i <= columnCount; ++i) {
                    String columnName = metaData.getColumnName(i);
                    if ("sql_volumes".equalsIgnoreCase(columnName)) {
                        dados.put("sql_volumes", (Object)resultSet.getString("sql_volumes"));
                        continue;
                    }
                    if ("sql_endereco_coleta".equalsIgnoreCase(columnName)) {
                        dados.put("sql_endereco_coleta", (Object)resultSet.getString("sql_endereco_coleta"));
                        continue;
                    }
                    if ("sql_endereco_entrega".equalsIgnoreCase(columnName)) {
                        dados.put("sql_endereco_entrega", (Object)resultSet.getString("sql_endereco_entrega"));
                        continue;
                    }
                    if ("TOKEN".equalsIgnoreCase(columnName)) {
                        dados.put("TOKEN", (Object)resultSet.getString("TOKEN"));
                        continue;
                    }
                    if ("comprimento".equalsIgnoreCase(columnName)) {
                        dados.put("comprimento", (Object)resultSet.getString("comprimento"));
                        continue;
                    }
                    if ("largura".equalsIgnoreCase(columnName)) {
                        dados.put("largura", (Object)resultSet.getString("largura"));
                        continue;
                    }
                    if ("altura".equalsIgnoreCase(columnName)) {
                        dados.put("altura", (Object)resultSet.getString("altura"));
                        continue;
                    }
                    if ("peso".equalsIgnoreCase(columnName)) {
                        dados.put("peso", (Object)resultSet.getString("peso"));
                        continue;
                    }
                    if (!"qtd".equalsIgnoreCase(columnName)) continue;
                    dados.put("qtd", (Object)resultSet.getString("qtd"));
                }
                retorno.put("result", (Object)"true");
                retorno.put("dados", (Object)dados);
                return retorno.toString();
            }
            retorno.put("result", (Object)"false");
            retorno.put("dados", (Object)"Token n\u00e3o existe");
            return retorno.toString();
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            mensagem.append("Erro getTokenMegleo: " + e.getMessage() + sw.toString());
            JSONObject retorno = new JSONObject();
            retorno.put("result", (Object)"false");
            retorno.put("dados", (Object)mensagem.toString());
            return retorno.toString();
        }
    }

    private String getPedMeg(String id) throws Exception {
        try {
            JdbcWrapper jdbcWrapper = null;
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbcWrapper = dwfEntityFacade.getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbcWrapper);
            JSONObject retorno = new JSONObject();
            sql.appendSql("SELECT * FROM AD_PEDMEG WHERE NUNOTA = " + id);
            ResultSet resultSet = sql.executeQuery();
            if (resultSet.next()) {
                retorno.put("result", (Object)"true");
                JSONObject dados = new JSONObject();
                dados.put("CHAVERASTREIO", (Object)resultSet.getString("CHAVERASTREIO"));
                dados.put("ETAPA", (Object)resultSet.getString("ETAPA"));
                dados.put("NUNOTA", (Object)resultSet.getString("NUNOTA"));
                retorno.put("dados", (Object)dados.toString());
            } else {
                retorno.put("result", (Object)"false");
                retorno.put("dados", (Object)"Pedido megleo n\u00e3o existe");
            }
            return retorno.toString();
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            mensagem.append("Erro getPedMeg: " + e.getMessage() + sw.toString());
            JSONObject retorno = new JSONObject();
            retorno.put("result", (Object)"false");
            retorno.put("dados", (Object)mensagem.toString());
            return retorno.toString();
        }
    }

    private String getPedMegbyPk(String chave_rastreio) throws Exception {
        try {
            JdbcWrapper jdbcWrapper = null;
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbcWrapper = dwfEntityFacade.getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbcWrapper);
            JSONObject retorno = new JSONObject();
            sql.appendSql("SELECT * FROM AD_PEDMEG WHERE CHAVERASTREIO = '" + chave_rastreio + "'");
            ResultSet resultSet = sql.executeQuery();
            if (resultSet.next()) {
                retorno.put("result", (Object)"true");
                JSONObject dados = new JSONObject();
                dados.put("CHAVERASTREIO", (Object)resultSet.getString("CHAVERASTREIO"));
                dados.put("ETAPA", (Object)resultSet.getString("ETAPA"));
                dados.put("NUNOTA", (Object)resultSet.getString("NUNOTA"));
                retorno.put("dados", (Object)dados.toString());
            } else {
                retorno.put("result", (Object)"false");
                retorno.put("dados", (Object)"Pedido megleo n\u00e3o existe");
            }
            return retorno.toString();
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            mensagem.append("Erro getPedMegbyPK: " + e.getMessage() + sw.toString());
            JSONObject retorno = new JSONObject();
            retorno.put("result", (Object)"false");
            retorno.put("dados", (Object)mensagem.toString());
            return retorno.toString();
        }
    }

    private String metodoPost(String url, String token, String params) throws IOException, InterruptedException, JSONException {
        try {
            String line;
            String mUrl = "https:
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
            }
            con.setDoOutput(true);
            String bodyReq = new String(params.getBytes("UTF-8"));
            OutputStream os = con.getOutputStream();
            os.write(bodyReq.getBytes("UTF-8"));
            os.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String outPut = "";
            while ((line = br.readLine()) != null) {
                outPut = outPut + line;
            }
            con.disconnect();
            JSONObject response = new JSONObject(outPut);
            return response.toString();
        }
        catch (IOException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            mensagem.append("Erro: " + e.getMessage() + sw.toString());
            JSONObject retorno = new JSONObject();
            retorno.put("result", 0);
            retorno.put("dados", (Object)mensagem.toString());
            return retorno.toString();
        }
    }

    private String montaJsonPedido(ContextoAcao contexto, String id, JSONObject dados_config) throws Exception {
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
            sql.appendSql("select tgfcab.nunota,tgfcab.VLRNOTA, tgfcab.CODPARCTRANSP, tgfcab.cif_fob, TGFCAB.CODPARc, VLRFRETE,VLRFRETETOTAL, tsiemp.cgc as cnpj, tgfpar.email, tgfpar.telefone, tgfpar.nomeparc, tgfpar.tippessoa, tgfpar.cgc_cpf, tgfnfe.chavenfe from TGFCAB JOIN tgfpar ON TGFCAB.CODPARc = TGFPAR.codparc LEFT JOIN tgfnfe on tgfnfe.nunota = tgfcab.nunota JOIN tsiemp on tsiemp.CODEMP = TGFCAB.codemp where tgfcab.nunota = " + id);
            sqlTransp.appendSql("select tgfpar.CGC_CPF as cnpj_transportadora from TGFCAB JOIN tgfpar ON TGFCAB.CODPARCTRANSP = TGFPAR.codparc where tgfcab.nunota = " + id);
            if (dados_config.has("sql_endereco_coleta")) {
                String novaSqlEnderecoColeta = dados_config.get("sql_endereco_coleta").toString();
                sqlEnderecoColeta.appendSql(novaSqlEnderecoColeta);
                sqlEnderecoColeta.setNamedParameter("id", (Object)id);
            } else {
                sqlEnderecoColeta.appendSql("select tgfcab.nunota, tsiemp.cep as cep, tsiend.nomeend as logradouro,tsiemp.NUMEND as numero, tsibai.nomebai as bairro from TGFCAB JOIN tsiemp on tsiemp.CODEMP = TGFCAB.codemp JOIN tsiend on tsiend.codend = tsiemp.codend JOIN tsibai on tsibai.codbai = tsiemp.codbai where tgfcab.nunota = " + id);
            }
            if (dados_config.has("sql_endereco_entrega")) {
                String novaSqlEnderecoEntrega = dados_config.get("sql_endereco_entrega").toString();
                sqlEnderecoEntrega.appendSql(novaSqlEnderecoEntrega);
                sqlEnderecoEntrega.setNamedParameter("id", (Object)id);
            } else {
                sqlEndEntrega2.appendSql("select TGFCPL.CODPARC, TGFCPL.cepentrega  from TGFCPL  JOIN tgfpar ON tgfpar.CODPARC = TGFCPL.CODPARC  JOIN tgfcab on tgfcab.codparc = tgfpar.CODPARC  where tgfcab.nunota = " + id);
                ResultSet resultSetEndEntrega = sqlEndEntrega2.executeQuery();
                if (resultSetEndEntrega.next() && !"".equals(resultSetEndEntrega.getString("cepentrega")) && null != resultSetEndEntrega.getString("cepentrega")) {
                    sqlEnderecoEntrega.appendSql("select tgfcpl.codparc,tgfcpl.cepentrega as cep, tsiend.nomeend as logradouro,TGFCPL.NUMENTREGA as numero, tsibai.nomebai as bairro from TGFCPL JOIN tgfpar ON tgfpar.CODPARC = tgfcpl.CODPARC JOIN tgfcab on tgfcab.codparc = tgfpar.CODPARC JOIN tsiend on tsiend.codend = tgfcpl.codendentrega JOIN tsibai on tsibai.codbai = tgfcpl.codbaientrega where tgfcab.nunota = " + id);
                } else {
                    sqlEnderecoEntrega.appendSql("select tgfcab.nunota, tgfpar.cep as cep, tsiend.nomeend as logradouro,tgfpar.NUMEND as numero, tsibai.nomebai as bairro from TGFCAB JOIN tgfpar ON TGFCAB.CODPARc = TGFPAR.codparc JOIN tsiend on tsiend.codend = tgfpar.codend JOIN tsibai on tsibai.codbai = tgfpar.codbai where tgfcab.nunota = " + id);
                }
            }
            ResultSet resultColeta = sqlEnderecoColeta.executeQuery();
            ResultSet resultEntrega = sqlEnderecoEntrega.executeQuery();
            if (resultColeta.next()) {
                JSONObject endereco_entrada = new JSONObject();
                endereco_entrada.put("rua", (Object)resultColeta.getString("logradouro"));
                endereco_entrada.put("numero", (Object)resultColeta.getString("numero"));
                endereco_entrada.put("bairro", (Object)resultColeta.getString("bairro"));
                novo_pedido.put("endereco_coleta", (Object)endereco_entrada);
                novo_pedido.put("cep_origem", (Object)resultColeta.getString("cep"));
            } else {
                retorno.put("result", (Object)"false");
                retorno.put("dados", (Object)"Erro ao buscar endere\u00e7o de entrega!");
            }
            if (resultEntrega.next()) {
                JSONObject endereco_saida = new JSONObject();
                endereco_saida.put("rua", (Object)resultEntrega.getString("logradouro"));
                endereco_saida.put("numero", (Object)resultEntrega.getString("numero"));
                endereco_saida.put("bairro", (Object)resultEntrega.getString("bairro"));
                novo_pedido.put("endereco_entrega", (Object)endereco_saida);
                novo_pedido.put("cep_destino", (Object)resultEntrega.getString("cep"));
            } else {
                retorno.put("result", (Object)"false");
                retorno.put("dados", (Object)"Erro ao buscar endere\u00e7o de entrega!");
            }
            ResultSet resultSet = sql.executeQuery();
            if (resultSet.next()) {
                novo_pedido.put("cnpj", (Object)resultSet.getString("cnpj"));
                novo_pedido.put("vlrfrete", (Object)resultSet.getString("VLRFRETE"));
                novo_pedido.put("forma_coleta", (Object)"coleta");
                novo_pedido.put("forma_entrega", (Object)"entrega");
                novo_pedido.put("cupom", (Object)"");
                if (contexto.getParam("order") != null) {
                    novo_pedido.put("order_by", (Object)contexto.getParam("order").toString().trim());
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
                destinatario.put("tipo_pessoa", (Object)tipo_pessoa);
                destinatario.put("telefone", (Object)resultSet.getString("telefone"));
                if (resultSet.getString("email") != null) {
                    destinatario.put("email", (Object)resultSet.getString("email").replaceAll("\\s", ""));
                }
                novo_pedido.put("destinatario", (Object)destinatario);
                novo_pedido.put("chave_rastreio_embarcadora", (Object)("sankhya " + resultSet.getString("nunota")));
                novo_pedido.put("cnpj", (Object)resultSet.getString("cnpj"));
                novo_pedido.put("valor_nota_fiscal", (Object)resultSet.getString("VLRNOTA"));
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
                sql2.appendSql("select " + qtd + " as qtd, tgfpro.descrprod, tgfpro.compldesc, tgfpro.codprod, " + altura + " as altura, " + largura + " as largura, " + comprimento + " as comprimento, tgfpro.unidade, " + peso + " as peso from TGFite join tgfpro on tgfite.codprod = tgfpro.codprod where TGFite.nunota = " + id);
            }
            ResultSet resultSet2 = sql2.executeQuery();
            JSONArray volumes = new JSONArray();
            JSONArray cargas = new JSONArray();
            while (resultSet2.next()) {
                JSONObject volume = new JSONObject();
                volume.put("quantidade", Math.round(Float.parseFloat(resultSet2.getString("qtd"))));
                volume.put("peso", resultSet2.getString("peso") != null ? resultSet2.getString("peso") : Integer.valueOf(0));
                volume.put("comprimento", resultSet2.getString("comprimento") != null ? resultSet2.getString("comprimento") : Integer.valueOf(0));
                volume.put("largura", resultSet2.getString("largura") != null ? resultSet2.getString("largura") : Integer.valueOf(0));
                volume.put("altura", resultSet2.getString("altura") != null ? resultSet2.getString("altura") : Integer.valueOf(0));
                volume.put("un_medida", (Object)"cm");
                volumes.put((Object)volume);
                cargas.put((Object)resultSet2.getString("codprod"));
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
            novo_pedido.put("volume", (Object)volumes);
            novo_pedido.put("tipos_carga", (Object)cargas);
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
                JSONObject retorno = new JSONObject();
                retorno.put("result", (Object)"false");
                retorno.put("dados", (Object)mensagem.toString());
                return retorno.toString();
            }
        }
        NativeSql.releaseResources((NativeSql)sql);
        NativeSql.releaseResources((NativeSql)sqlEndEntrega2);
        JdbcWrapper.closeSession((JdbcWrapper)jdbc);
        return string;
    }

    private String criaPedMeg(ContextoAcao contexto, String id, String token, JSONObject dados_config) throws Exception {
        try {
            NativeSql sql2;
            NativeSql sql;
            JapeSession.SessionHandle hnd;
            JdbcWrapper jdbc;
            JSONObject retorno;
            block17: {
                BigDecimal nunota;
                String nova_etapa;
                BigDecimal valor_fatura_float;
                String transportadora_cnpj;
                String transportadora_nome;
                String prazo;
                String chave_cotacao;
                block16: {
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
                        response = this.metodoPost("/api/v1/pedidos/criar_simplificado", token, jsonPedido.get("dados").toString());
                        JSONresponse = new JSONObject(response);
                    } else if ("nota".equals(jsonPedido.get("tipo").toString())) {
                        response = this.metodoPost("/api/v1/pedidos/criar_sankhya", token, jsonPedido.get("dados").toString());
                        JSONresponse = new JSONObject(response);
                    }
                    if (JSONresponse.has("error")) {
                        retorno.put("result", (Object)"false");
                        retorno.put("dados", JSONresponse.get("error"));
                        return retorno.toString();
                    }
                    if (!JSONresponse.has("data")) {
                        retorno.put("result", (Object)"false");
                        retorno.put("dados", (Object)"Erro na comunica\u00e7\u00e3o com a Megleo");
                        return retorno.toString();
                    }
                    chave_cotacao = JSONresponse.getJSONObject("data").get("chave_cotacao").toString();
                    prazo = JSONresponse.getJSONObject("data").get("prazo").toString();
                    transportadora_nome = JSONresponse.getJSONObject("data").getJSONObject("transportadora").get("nome_fantasia").toString();
                    transportadora_cnpj = JSONresponse.getJSONObject("data").getJSONObject("transportadora").get("cnpj").toString();
                    String valor_fatura = JSONresponse.getJSONObject("data").get("valor_fatura").toString();
                    valor_fatura_float = new BigDecimal(JSONresponse.getJSONObject("data").get("valor_fatura").toString());
                    nova_etapa = null;
                    nova_etapa = "pedido".equals(jsonPedido.get("tipo").toString()) ? "ADICIONAR_NOTA_FISCAL" : "FINALIZADO";
                    jdbc = null;
                    hnd = null;
                    sql = null;
                    sql2 = null;
                    hnd = JapeSession.open();
                    EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
                    jdbc = dwfEntityFacade.getJdbcWrapper();
                    jdbc.openSession();
                    sql = new NativeSql(jdbc);
                    sql2 = new NativeSql(jdbc);
                    nunota = new BigDecimal(id);
                    sql.appendSql("select * from tgfpar where CGC_CPF = '" + transportadora_cnpj + "'");
                    ResultSet resultSet = sql.executeQuery();
                    if (resultSet.next()) {
                        final BigDecimal CODPARC = new BigDecimal(resultSet.getString("CODPARC"));
                        if ("pedido".equals(jsonPedido.get("tipo").toString())) {
                            final String tipfrete = "N";
                            final String cif_fob = "C";
                            hnd.execWithTX(new JapeSession.TXBlock(){

                                public void doWithTx() throws Exception {
                                    ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)JapeFactory.dao((String)"CabecalhoNota").prepareToUpdateByPK(new Object[]{nunota}).set("CODPARCTRANSP", (Object)CODPARC)).set("TIPFRETE", (Object)tipfrete)).set("CIF_FOB", (Object)cif_fob)).set("VLRFRETE", (Object)valor_fatura_float)).update();
                                }
                            });
                        }
                        break block16;
                    }
                    retorno.put("result", (Object)"false");
                    retorno.put("dados", (Object)("N\u00e3o h\u00e1 transportadora com CNPJ " + transportadora_cnpj + " correspondente \n"));
                    String CODPARC = retorno.toString();
                    JdbcWrapper.closeSession((JdbcWrapper)jdbc);
                    JapeSession.close((JapeSession.SessionHandle)hnd);
                    NativeSql.releaseResources((NativeSql)sql);
                    NativeSql.releaseResources((NativeSql)sql2);
                    return CODPARC;
                }
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    final String dataFormatada = LocalDateTime.now().format(formatter);
                    final String etapa = nova_etapa;
                    JSONObject existePedMeg2 = new JSONObject(this.getPedMegbyPk(chave_cotacao));
                    if ("false".equals(existePedMeg2.get("result").toString())) {
                        hnd.execWithTX(new JapeSession.TXBlock(){

                            public void doWithTx() throws Exception {
                                JapeWrapper insert1 = JapeFactory.dao((String)"AD_PEDMEG");
                                DynamicVO insertVO = ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)insert1.create().set("CHAVERASTREIO", (Object)chave_cotacao)).set("ETAPA", (Object)etapa)).set("NUNOTA", (Object)nunota)).set("TRANSPORTADORA", (Object)transportadora_nome)).set("CNPJ_TRANSPORTADORA", (Object)transportadora_cnpj)).set("VALOR_FRETE", (Object)valor_fatura_float)).set("DATA_CRIACAO", (Object)dataFormatada)).set("PRAZO", (Object)prazo)).save();
                            }
                        });
                        break block17;
                    }
                    if (!"true".equals(existePedMeg2.get("result").toString())) break block17;
                    hnd.execWithTX(new JapeSession.TXBlock(){

                        public void doWithTx() throws Exception {
                            ((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)((FluidUpdateVO)JapeFactory.dao((String)"AD_PEDMEG").prepareToUpdateByPK(new Object[]{chave_cotacao}).set("ETAPA", (Object)etapa)).set("NUNOTA", (Object)nunota)).set("TRANSPORTADORA", (Object)transportadora_nome)).set("CNPJ_TRANSPORTADORA", (Object)transportadora_cnpj)).set("VALOR_FRETE", (Object)valor_fatura_float)).set("DATA_CRIACAO", (Object)dataFormatada)).set("PRAZO", (Object)prazo)).update();
                        }
                    });
                }
                catch (Exception e) {
                    String string;
                    try {
                        JdbcWrapper.closeSession(jdbc);
                        JapeSession.close((JapeSession.SessionHandle)hnd);
                        NativeSql.releaseResources(sql);
                        NativeSql.releaseResources(sql2);
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        retorno.put("result", (Object)"false");
                        retorno.put("dados", (Object)("Erro insercao pedido " + chave_cotacao + ": " + e.getMessage() + "\n"));
                        string = retorno.toString();
                    }
                    catch (Throwable throwable) {
                        JdbcWrapper.closeSession(jdbc);
                        JapeSession.close((JapeSession.SessionHandle)hnd);
                        NativeSql.releaseResources(sql);
                        NativeSql.releaseResources(sql2);
                        throw throwable;
                    }
                    JdbcWrapper.closeSession((JdbcWrapper)jdbc);
                    JapeSession.close((JapeSession.SessionHandle)hnd);
                    NativeSql.releaseResources((NativeSql)sql);
                    NativeSql.releaseResources((NativeSql)sql2);
                    return string;
                }
            }
            JdbcWrapper.closeSession((JdbcWrapper)jdbc);
            JapeSession.close((JapeSession.SessionHandle)hnd);
            NativeSql.releaseResources((NativeSql)sql);
            NativeSql.releaseResources((NativeSql)sql2);
            retorno.put("result", (Object)"true");
            return retorno.toString();
        }
        catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            StringBuffer mensagem = new StringBuffer();
            e.printStackTrace(pw);
            mensagem.append("Erro criaPedMeg: " + e.getMessage() + sw.toString());
            JSONObject retorno = new JSONObject();
            retorno.put("result", (Object)"false");
            retorno.put("dados", (Object)mensagem.toString());
            return retorno.toString();
        }
    }
}
