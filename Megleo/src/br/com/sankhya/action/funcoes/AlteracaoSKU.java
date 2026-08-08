package br.com.sankhya.action.funcoes;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.UUID;
import org.json.JSONObject;

public class AlteracaoSKU {

    public void enviaAlteracaoAPI(DynamicVO produtoVO) throws Exception {
        BigDecimal codprod = produtoVO.asBigDecimal("CODPROD");
        String ncm = produtoVO.asString("NCM");
        String embalagem = produtoVO.asString("CODVOL");
        JapeWrapper configDAO = JapeFactory.dao((String)"AD_CONFIGMEG");
        DynamicVO configVO = configDAO.findOne("ID = 1");
        JapeWrapper empresaDAO = JapeFactory.dao((String)"Empresa");
        DynamicVO empresaVO = empresaDAO.findOne("CODEMP = (SELECT MIN (CODEMP) FROM TSIEMP)");
        String cnpj = empresaVO.asString("CGC");
        String token = configVO.asString("TOKEN");
        System.out.println("Megleo - Iniciando atualiza\u00e7\u00e3o de produto via API.");
        String boundary = UUID.randomUUID().toString();
        String lineFeed = "\r\n";
        String urlWithParams = "https:
        URL obj = new URL(urlWithParams);
        HttpURLConnection connection = (HttpURLConnection)obj.openConnection();
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("CNPJ", cnpj);
        System.out.println("Megleo - #1 Conexao da API: " + connection.getRequestProperties());
        OutputStream outputStream = connection.getOutputStream();
        PrintWriter writer = new PrintWriter((Writer)new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
        String[][] fields = new String[][]{{"quantidade", "1"}, {"ncm", ncm}, {"embalagem", embalagem}, {"sku", codprod.toString()}, {"correlacao", "sankhya"}};
        String dadosEnviados = "";
        for (String[] field : fields) {
            writer.append("--").append(boundary).append(lineFeed);
            writer.append("Content-Disposition: form-data; name=\"").append(field[0]).append("\"").append(lineFeed);
            writer.append("Content-Type: text/plain; charset=UTF-8").append(lineFeed);
            writer.append(lineFeed).append(field[1]).append(lineFeed);
            dadosEnviados = dadosEnviados.concat("\n" + field[0] + " : " + field[1]);
            System.out.println("Megleo - #2 Body: " + dadosEnviados);
        }
        writer.append("--").append(boundary).append("--").append(lineFeed);
        writer.flush();
        writer.close();
        System.out.println("Megleo - #3 Token API: " + token);
        System.out.println("Megleo - #4 Endere\u00e7o da API: " + urlWithParams);
        String tipo = "";
        String resposta = "";
        try {
            String inputLine;
            int responseCode = connection.getResponseCode();
            System.out.println("C\u00f3digo de resposta: " + responseCode);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println("Megleo - #4 C\u00f3digo de Resposta da API: " + responseCode);
            resposta = response.toString();
            System.out.println("Resposta: " + resposta);
            JSONObject json = new JSONObject(response.toString());
            boolean result = false;
            if (json.has("result")) {
                result = json.getBoolean("result");
            }
            String msg = "";
            if (json.has("msg")) {
                msg = json.getString("msg");
            }
            if (responseCode == 200) {
                if (result) {
                    resposta = msg;
                    tipo = "E";
                } else {
                    tipo = "F";
                }
            } else {
                tipo = "F";
            }
        }
        catch (SocketTimeoutException e) {
            System.out.println("A requisi\u00e7\u00e3o excedeu o tempo limite de 5 segundos.");
        }
        catch (IOException e) {
            System.out.println("Erro de I/O: " + e.getMessage());
        }
        finally {
            ((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)((FluidCreateVO)JapeFactory.dao((String)"AD_SKUMegleo").create().set("CODPROD", (Object)codprod)).set("TIPO", (Object)tipo)).set("RESPOSTA", (Object)resposta)).set("ENVIO", (Object)dadosEnviados)).set("DHENVIO", (Object)new Timestamp(System.currentTimeMillis()))).save();
        }
    }
}
