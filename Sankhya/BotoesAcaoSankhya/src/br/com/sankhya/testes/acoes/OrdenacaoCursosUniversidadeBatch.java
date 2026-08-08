package br.com.sankhya.testes.acoes;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.modelcore.util.MGECoreParameter;

import com.google.gson.Gson;
import com.sankhya.util.StringUtils;

public class OrdenacaoCursosUniversidadeBatch implements AcaoRotinaJava{

	public void doAction(ContextoAcao contexto) throws Exception{
		BigDecimal codAluno = (BigDecimal) contexto.getLinhaPai().getCampo("ID");

		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(String.valueOf(codAluno.longValue() + 30).getBytes());

		String chave = StringUtils.toHexString(md5.digest());
		String enderecoServer = (String) MGECoreParameter.getParameter("br.com.sankhya.universidade.url.batch.actions");

        URL url = new URL(String.format("%s/ead/bib/ordenaCursos.php?alunoid=%s&register=%s", enderecoServer, codAluno.intValue(), chave));

        InputStream       inputStream = null;
        HttpURLConnection conn = null;
        try {
            conn = ( HttpURLConnection ) url.openConnection();
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();

            int statusHttp = conn.getResponseCode();

            if(statusHttp == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                contexto.mostraErro("Houve um erro de execuo no servio. Mensagem obtida: " + conn.getResponseMessage());
            }

            inputStream = conn.getInputStream();

            StringBuffer contentBuf = new StringBuffer();
            byte []      buf = new byte[ 1024 ];
            int          length;

            while((length = inputStream.read(buf)) > 0) {
                contentBuf.append(new String(buf, 0, length, "ISO-8859-1"));
            }

            Gson gson = new Gson();
            Resposta r = gson.fromJson(contentBuf.toString(), Resposta.class);

            if(r.erro){
            	contexto.mostraErro(r.motivo);
            }

        } catch(IOException ioException) {
            String strUrl = url.toString();

            if((strUrl != null) && (strUrl.indexOf("?") > -1)) {
                strUrl = strUrl.substring(0, strUrl.indexOf("?"));
            }

            StringBuffer buf = new StringBuffer();
            buf.append("No foi possvel copiar os cursos.");
            buf.append("\nVeja o erro:");
            buf.append(ioException.getMessage());
            ioException.printStackTrace();
            throw new Exception(buf.toString());
        } finally {
            if(inputStream != null) {
                inputStream.close();
            }
        }
	}

	public static class Resposta{
		public boolean erro;
		public String motivo;
	}
}
