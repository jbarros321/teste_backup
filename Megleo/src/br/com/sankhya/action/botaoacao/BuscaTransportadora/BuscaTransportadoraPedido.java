package br.com.sankhya.action.botaoacao.BuscaTransportadora;

import br.com.sankhya.action.funcoes.RegistraLOG;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class BuscaTransportadoraPedido {
    private String transportadora_cnpj;
    private String chave_cotacao;
    private String nomeparcTransp;
    private BigDecimal codparcTransp;
    private BigDecimal vlrFrete;
    private BigDecimal qtdDias;
    private BigDecimal nunota;
    private JapeWrapper cotacaoDAO = JapeFactory.dao((String)"AD_COTMEG");

    public void buscaTransportadora(String token, String chave_cotacao, String vlrPedido, String cepOrigem, String cepDestino, JSONArray volumeBuscaTransportadora, BigDecimal varNunota, JSONArray cargas, String cnpj_destinatario) throws Exception {
        int i;
        StringBuilder conteudoLog = new StringBuilder();
        String tituloLog = "Chamada das Cota\u00e7\u00f5es";
        BigDecimal nunotaLog = null;
        this.chave_cotacao = chave_cotacao;
        this.nunota = varNunota;
        JSONObject json = new JSONObject();
        json.put("cep_origem", (Object)cepOrigem);
        json.put("cep_destino", (Object)cepDestino);
        json.put("valor_nota_fiscal", (Object)vlrPedido);
        json.put("volume", (Object)volumeBuscaTransportadora);
        StringBuilder params = new StringBuilder();
        params.append("cep_origem=").append(json.getString("cep_origem"));
        params.append("&cep_destino=").append(json.getString("cep_destino"));
        params.append("&valor_nota_fiscal=").append(json.getString("valor_nota_fiscal"));
        for (i = 0; i < volumeBuscaTransportadora.length(); ++i) {
            JSONObject volume = volumeBuscaTransportadora.getJSONObject(i);
            params.append("&volume[").append(i).append("][quantidade]=").append(volume.getString("quantidade"));
            params.append("&volume[").append(i).append("][peso]=").append(volume.getString("peso"));
            params.append("&volume[").append(i).append("][comprimento]=").append(volume.getString("comprimento"));
            params.append("&volume[").append(i).append("][largura]=").append(volume.getString("largura"));
            params.append("&volume[").append(i).append("][altura]=").append(volume.getString("altura"));
            params.append("&volume[").append(i).append("][sku]=").append(volume.getString("sku"));
            params.append("&volume[").append(i).append("][ncm]=").append(volume.getString("ncm"));
            params.append("&volume[").append(i).append("][embalagem]=").append(volume.getString("embalagem"));
        }
        for (i = 0; i < cargas.length(); ++i) {
            params.append("&tipos_carga[]=").append(cargas.getString(i));
        }
        params.append("&correlacao_tipos_carga=").append("sankhya");
        params.append("&cnpj_destinatario=").append(cnpj_destinatario);
        System.out.println("Megleo - #16 Par\u00e2metros da consulta: " + params.toString());
        conteudoLog.append("Par\u00e2metros da consulta: " + params.toString() + "\n");
        String urlWithParams = "https:
        String bearerToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvYXBwLm1lZ2xlby5jb20uYnJcL2ludGVncmFjb2VzXC9zYW5raHlhXC9tb2RhbF9ub3ZvX3Rva2VuX3NhbmtoeWEiLCJpYXQiOjE3MTg5MDU3MjksIm5iZiI6MTcxODkwNTcyOSwianRpIjoiZVhxRWtTdG1pVXFHUW5RSiIsInN1YiI6NTg4LCJwcnYiOiIzMDMwMTA4YzRhOTRkYTA0NWRkMGQ3N2ZhMDE5ZmQyZTk3MDI3ZjAxIn0.-R1FHMwYgrKoQ26dLd65BYeYX5moBLuczaXvbbEip08";
        urlWithParams = urlWithParams.concat(params.toString());
        URL obj = new URL(urlWithParams);
        HttpURLConnection connection = (HttpURLConnection)obj.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        int responseCode = connection.getResponseCode();
        System.out.println("Megleo - #20 Token API: " + token);
        System.out.println("Megleo - #21 Endere\u00e7o da API: " + urlWithParams);
        System.out.println("Megleo - #22 Chamada da API: " + connection.toString());
        System.out.println("Megleo - #23 C\u00f3digo de Resposta da API: " + responseCode);
        conteudoLog.append("Token API: " + token + "\n\n");
        conteudoLog.append("Endere\u00e7o da API: " + urlWithParams + "\n\n");
        conteudoLog.append("Chamada da API: " + connection.toString() + "\n\n");
        conteudoLog.append("C\u00f3digo de Resposta da API: " + responseCode + "\n\n");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));){
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            String responseString = response.toString().trim();
            System.out.println("Megleo - #17 Resposta da API: " + responseString);
            conteudoLog.append("Resposta da API: " + responseString + "\n");
            JdbcWrapper jdbc = null;
            Object hnd = null;
            NativeSql sql = null;
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbc = dwfEntityFacade.getJdbcWrapper();
            jdbc.openSession();
            sql = new NativeSql(jdbc);
            sql.executeUpdate("UPDATE TGFCAB SET AD_NUCOTMEG = NULL WHERE NUNOTA = " + this.nunota + " ");
            System.out.println("Megleo - # Atualiza TGFCAB, removendo Nro Cotacao Megleo");
            conteudoLog.append("Atualiza TGFCAB, removendo Nro Cotacao Megleo\n");
            sql.executeUpdate("DELETE FROM AD_COTMEG WHERE CHAVERASTREIO = '" + chave_cotacao + "'");
            System.out.println("Megleo - # Limpa os registros da tabela AD_COTMEG");
            conteudoLog.append("Limpa os registros da tabela AD_COTMEG\n");
            if (responseString.startsWith("[")) {
                JSONArray jsonResponse = new JSONArray(responseString);
                StringBuilder formattedResponse = new StringBuilder();
                for (int i2 = 0; i2 < jsonResponse.length(); ++i2) {
                    JSONObject item = jsonResponse.getJSONObject(i2);
                    formattedResponse.append(item.toString(2)).append("\n");
                    this.transportadora_cnpj = item.get("transportadora_cnpj").toString();
                    this.qtdDias = new BigDecimal(item.get("dias_normal").toString());
                    this.vlrFrete = new BigDecimal(item.get("valor_fatura").toString());
                    System.out.println("Megleo - #18 Solicitada inser\u00e7\u00e3o na tabela AD_COTMEG");
                    this.insereTabela();
                    System.out.println("Megleo - #19 Linha inserida na tabela AD_COTMEG");
                }
            }
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

    private void insereTabela() throws Exception {
        JdbcWrapper jdbc = null;
        JapeSession.SessionHandle hnd = null;
        NativeSql sql = null;
        try {
            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            jdbc = dwfEntityFacade.getJdbcWrapper();
            jdbc.openSession();
            sql = new NativeSql(jdbc);
            sql.appendSql("SELECT CODPARC, NOMEPARC FROM TGFPAR PAR WHERE CODPARC = (SELECT MAX(CODPARC) FROM TGFPAR WHERE CGC_CPF = '" + this.transportadora_cnpj + "' AND ATIVO = 'S') ");
            ResultSet rsUltimoNunico = sql.executeQuery("SELECT MAX(NUNICO) AS ULTNUNICO FROM AD_COTMEG");
            BigDecimal ultimoNunico = new BigDecimal(0);
            if (rsUltimoNunico.next()) {
                ultimoNunico = rsUltimoNunico.getBigDecimal("ULTNUNICO");
            }
            if (ultimoNunico == null) {
                ultimoNunico = new BigDecimal(0);
            }
            ultimoNunico = ultimoNunico.add(new BigDecimal(1));
            ResultSet rsBuscaCodparc = sql.executeQuery();
            if (rsBuscaCodparc.next()) {
                this.codparcTransp = rsBuscaCodparc.getBigDecimal("CODPARC");
                this.nomeparcTransp = rsBuscaCodparc.getString("NOMEPARC");
                NativeSql sql2 = new NativeSql(jdbc);
                sql2.executeUpdate("UPDATE TGFCAB SET AD_NUCOTMEG = NULL WHERE NUNOTA = (SELECT NUNOTA FROM AD_PEDMEG WHERE CHAVERASTREIO = '" + this.chave_cotacao + "')");
                ResultSet rsBuscaNunota = sql2.executeQuery("SELECT NUNOTA FROM AD_PEDMEG WHERE CHAVERASTREIO = '" + this.chave_cotacao + "'");
                while (rsBuscaNunota.next()) {
                    this.nunota = rsBuscaNunota.getBigDecimal("NUNOTA");
                }
                String queryInsert = "INSERT INTO AD_COTMEG (NUNICO,CODPARC, CHAVERASTREIO, VLRFRETE, QTDDIAS, NOMETRANSP, NUNOTA) VALUES (" + ultimoNunico + "," + this.codparcTransp + ",'" + this.chave_cotacao + "', " + this.vlrFrete + "," + this.qtdDias + ",'" + this.nomeparcTransp + "'," + this.nunota + ")";
                sql2.executeUpdate(queryInsert);
            }
        }
        catch (Throwable throwable) {
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);
            NativeSql.releaseResources(sql);
            throw throwable;
        }
        JdbcWrapper.closeSession((JdbcWrapper)jdbc);
        JapeSession.close(hnd);
        NativeSql.releaseResources((NativeSql)sql);
    }
}
