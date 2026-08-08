package br.com.denver.tsl.action.botaoAcao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ImportarItensTXT implements AcaoRotinaJava {

    private EntityFacade facade = EntityFacadeFactory.getDWFFacade();
    private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        BigDecimal nunota = obterNunota(contexto);
        if (nunota == null) {
            contexto.setMensagemRetorno("Selecione uma nota de venda antes de acionar o botão de ação!");
            return;
        }
        File arquivoTmp = obterArquivo(contexto);
        if (arquivoTmp == null || !arquivoTmp.exists()) {
            contexto.setMensagemRetorno(gerarInterfaceUpload(contexto, nunota));
            return;
        }
        try {
            contexto.setMensagemRetorno(gerarMensagemResultado(processarArquivo(arquivoTmp, nunota)));
        } finally {
            if (arquivoTmp.exists()) arquivoTmp.delete();
        }
    }

    private BigDecimal obterNunota(ContextoAcao contexto) {
        try {
            if (contexto.getLinhas() != null && contexto.getLinhas().length > 0) {
                Object nunotaObj = contexto.getLinhas()[0].getCampo("NUNOTA");
                return nunotaObj instanceof BigDecimal ? (BigDecimal) nunotaObj : BigDecimalUtil.valueOf(nunotaObj.toString());
            }
        } catch (Exception ignored) {}
        return null;
    }

    private File obterArquivo(ContextoAcao contexto) throws Exception {
        Object paramArquivo = contexto.getParam("CAMINHO_ARQUIVO");
        if (paramArquivo == null) return null;

        String caminhoArquivo = paramArquivo.toString();
        File arquivo = new File(caminhoArquivo);
        if (!arquivo.exists() || !arquivo.isFile()) return null;

        if (caminhoArquivo.startsWith(TMP_DIR)) return arquivo;

        File arquivoTmp = new File(TMP_DIR, "import_" + System.currentTimeMillis() + ".txt");
        try (InputStream is = new BufferedInputStream(new FileInputStream(arquivo));
             OutputStream os = new BufferedOutputStream(new FileOutputStream(arquivoTmp))) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) os.write(buffer, 0, len);
        }
        return arquivoTmp;
    }

    private ResultadoProcessamento processarArquivo(File arquivo, BigDecimal nunota) throws Exception {
        ResultadoProcessamento resultado = new ResultadoProcessamento();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(arquivo), "UTF-8"))) {
            String linha;
            int numLinha = 0;
            while ((linha = reader.readLine()) != null) {
                numLinha++;
                if (numLinha == 1 || StringUtils.isEmpty(linha)) continue;
                try {
                    processarLinha(linha, nunota);
                    resultado.sucessos++;
                } catch (Exception e) {
                    resultado.erros++;
                    resultado.mensagensErro.add("Linha " + numLinha + ": " + e.getMessage());
                }
            }
        }
        return resultado;
    }

    private void processarLinha(String linha, BigDecimal nunota) throws Exception {
        String[] campos = linha.split(";");
        if (campos.length < 15) throw new Exception("Linha inválida: campos insuficientes");
        BigDecimal codprod = buscarCodprodPorRefforn(campos[14].trim());
        if (codprod == null) throw new Exception("Produto não encontrado para código de barras: " + campos[14].trim());
        DynamicVO itemVO = (DynamicVO) facade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
        itemVO.setProperty("NUNOTA", nunota);
        itemVO.setProperty("CODPROD", codprod);
        itemVO.setProperty("QTDNEG", BigDecimalUtil.valueOf(campos[4].trim()));
        itemVO.setProperty("CONTROLE", formatarDataProducao(campos[1].trim()));
        itemVO.setProperty("AD_DATAPRODUCAO", campos[1].trim());
        itemVO.setProperty("AD_DATAVALIDADE", campos[11].trim());
        itemVO.setProperty("AD_QTDBRUTO", BigDecimalUtil.valueOf(campos[5].trim()));
        Collection<PrePersistEntityState> itens = new ArrayList<>();
        itens.add(PrePersistEntityState.build(facade, DynamicEntityNames.ITEM_NOTA, itemVO));
        new CACHelper().incluirAlterarItem(nunota, AuthenticationInfo.getCurrent(), itens, true);
    }

    private BigDecimal buscarCodprodPorRefforn(String refforn) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        try {
            jdbc = facade.getJdbcWrapper();
            jdbc.openSession();
            sql = new NativeSql(jdbc);
            sql.appendSql("SELECT CODPROD FROM TGFPRO WHERE REFFORN = :REFFORN AND ROWNUM = 1");
            sql.setNamedParameter("REFFORN", refforn);
            ResultSet rs = sql.executeQuery();
            return rs.next() ? rs.getBigDecimal("CODPROD") : null;
        } finally {
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private String formatarDataProducao(String dataStr) {
        return StringUtils.isNotEmpty(dataStr) && dataStr.length() == 8 ? dataStr : "";
    }

    private String gerarInterfaceUpload(ContextoAcao contexto, BigDecimal nunota) {
        StringBuilder html = new StringBuilder(1000);
        html.append("<div id=\"importarItensContainer\" style=\"padding:20px;font-family:Arial,sans-serif;max-width:600px;margin:0 auto;\">");
        html.append("<h3 style=\"color:#333;margin-top:0;border-bottom:2px solid #4CAF50;padding-bottom:10px;\">Importar Itens de Arquivo TXT</h3>");
        html.append("<div style=\"background-color:#f5f5f5;padding:15px;border-radius:5px;margin:15px 0;\">");
        html.append("<p style=\"margin:0 0 10px 0;color:#666;\"><strong>Nota Fiscal:</strong> ").append(nunota).append("</p>");
        html.append("<p style=\"margin:0;color:#666;font-size:12px;\">Selecione um arquivo TXT com os itens a serem importados. O arquivo deve ter formato CSV separado por ponto e vírgula (;).</p>");
        html.append("</div>");
        html.append("<div style=\"margin:20px 0;\">");
        html.append("<label for=\"arquivoInput\" style=\"display:block;margin-bottom:8px;font-weight:bold;color:#333;\">Selecionar Arquivo:</label>");
        html.append("<input type=\"file\" id=\"arquivoInput\" accept=\".txt,.csv\" style=\"width:100%;padding:8px;border:2px dashed #ccc;border-radius:4px;background-color:#fff;cursor:pointer;\" />");
        html.append("<p id=\"nomeArquivo\" style=\"margin:8px 0 0 0;color:#666;font-size:12px;display:none;\"></p>");
        html.append("</div>");
        html.append("<div style=\"margin:20px 0;text-align:center;\">");
        html.append("<button id=\"btnUpload\" onclick=\"fazerUpload()\" style=\"padding:12px 30px;background-color:#4CAF50;color:white;border:none;border-radius:4px;font-size:14px;font-weight:bold;cursor:pointer;box-shadow:0 2px 4px rgba(0,0,0,0.2);\" disabled>Carregar e Processar Arquivo</button>");
        html.append("</div>");
        html.append("<div id=\"statusDiv\" style=\"margin:15px 0;padding:10px;border-radius:4px;display:none;\"></div>");
        html.append("<script type=\"text/javascript\">(function(){");
        html.append("var a=document.getElementById('arquivoInput'),b=document.getElementById('btnUpload'),c=document.getElementById('nomeArquivo'),d=document.getElementById('statusDiv'),e=null;");
        html.append("a.addEventListener('change',function(f){e=f.target.files[0];if(e){c.textContent='Arquivo selecionado: '+e.name+' ('+formatarTamanho(e.size)+')';c.style.display='block';c.style.color='#4CAF50';b.disabled=false;d.style.display='none';}else{c.style.display='none';b.disabled=true;}});");
        html.append("function formatarTamanho(bytes){if(bytes===0)return '0 Bytes';var k=1024,s=['Bytes','KB','MB'],i=Math.floor(Math.log(bytes)/Math.log(k));return Math.round(bytes/Math.pow(k,i)*100)/100+' '+s[i];}");
        html.append("window.fazerUpload=function(){if(!e){mostrarStatus('Por favor, selecione um arquivo primeiro.','error');return;}b.disabled=true;b.textContent='Carregando...';mostrarStatus('Fazendo upload do arquivo...','info');var f=new FormData();f.append('file',e);f.append('nunota','").append(nunota).append("');var x=new XMLHttpRequest();x.open('POST','/mge/uploadTempFile.mge',true);");
        html.append("x.onload=function(){if(x.status===200){try{var r=JSON.parse(x.responseText);if(r.success&&r.fileName){mostrarStatus('Arquivo carregado com sucesso! Processando...','success');setTimeout(function(){executarAcaoComArquivo(r.fileName);},500);}else{mostrarStatus('Erro ao fazer upload: '+(r.message||'Resposta inválida'),'error');resetarBotao();}}catch(err){mostrarStatus('Erro ao processar resposta: '+err.message,'error');resetarBotao();}}else{mostrarStatus('Erro ao fazer upload. Status: '+x.status,'error');resetarBotao();}};");
        html.append("x.onerror=function(){mostrarStatus('Erro de conexão ao fazer upload.','error');resetarBotao();};x.send(f);};");
        html.append("function resetarBotao(){b.disabled=false;b.textContent='Carregar e Processar Arquivo';}");
        html.append("function executarAcaoComArquivo(n){try{var s=null;if(typeof SkApplicationInstance!=='undefined'&&SkApplicationInstance&&typeof SkApplicationInstance.acionarBotao==='function')s=SkApplicationInstance;else if(typeof window!=='undefined'&&window.SkApplicationInstance&&typeof window.SkApplicationInstance.acionarBotao==='function')s=window.SkApplicationInstance;else if(typeof window!=='undefined'&&window.parent&&window.parent!==window&&typeof window.parent.SkApplicationInstance!=='undefined'&&typeof window.parent.SkApplicationInstance.acionarBotao==='function')s=window.parent.SkApplicationInstance;else if(typeof window!=='undefined'&&window.top&&window.top!==window&&typeof window.top.SkApplicationInstance!=='undefined'&&typeof window.top.SkApplicationInstance.acionarBotao==='function')s=window.top.SkApplicationInstance;");
        html.append("if(s){var t='").append(escaparJavaScript(TMP_DIR)).append("';var p=t+(t.endsWith('/')||t.endsWith('\\\\')?'':'/')+n;var i=").append(obterIdBotao(contexto)).append(";if(i>0){s.acionarBotao({tipo:'java',idBotao:i,parametros:{'CAMINHO_ARQUIVO':p}});}else{mostrarStatus('Erro: ID do botão não encontrado. Tente clicar no botão novamente após o upload.','error');resetarBotao();}}else{mostrarStatus('Erro: Não foi possível encontrar a instância do Sankhya para executar a ação.','error');resetarBotao();}}catch(err){mostrarStatus('Erro ao executar ação: '+err.message,'error');resetarBotao();}}");
        html.append("function mostrarStatus(m,t){d.style.display='block';d.style.backgroundColor=t==='success'?'#d4edda':t==='error'?'#f8d7da':'#d1ecf1';d.style.color=t==='success'?'#155724':t==='error'?'#721c24':'#0c5460';d.style.border='1px solid '+(t==='success'?'#c3e6cb':t==='error'?'#f5c6cb':'#bee5eb');d.textContent=m;}");
        html.append("})();</script></div>");
        return html.toString();
    }

    private String gerarMensagemResultado(ResultadoProcessamento resultado) {
        StringBuilder html = new StringBuilder(500);
        html.append("<div style=\"padding:20px;font-family:Arial,sans-serif;max-width:600px;margin:0 auto;\">");
        html.append("<h3 style=\"color:#333;margin-top:0;border-bottom:2px solid #4CAF50;padding-bottom:10px;\">Processamento Concluído</h3>");
        html.append("<div style=\"background-color:#d4edda;padding:15px;border-radius:5px;border:1px solid #c3e6cb;margin:15px 0;\">");
        html.append("<p style=\"margin:0 0 10px 0;color:#155724;font-weight:bold;\">✓ Processamento finalizado com sucesso!</p>");
        html.append("<p style=\"margin:5px 0;color:#155724;\"><strong>Sucessos:</strong> ").append(resultado.sucessos).append("</p>");
        html.append("<p style=\"margin:5px 0;color:#155724;\"><strong>Erros:</strong> ").append(resultado.erros).append("</p></div>");
        if (resultado.erros > 0 && !resultado.mensagensErro.isEmpty()) {
            html.append("<div style=\"background-color:#fff3cd;padding:15px;border-radius:5px;border:1px solid #ffeaa7;margin:15px 0;\">");
            html.append("<p style=\"margin:0 0 10px 0;color:#856404;font-weight:bold;\">Erros encontrados:</p>");
            html.append("<ul style=\"margin:0;padding-left:20px;color:#856404;\">");
            resultado.mensagensErro.forEach(erro -> html.append("<li>").append(escaparHTML(erro)).append("</li>"));
            html.append("</ul></div>");
        }
        return html.append("</div>").toString();
    }

    private String escaparHTML(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String escaparJavaScript(String texto) {
        return texto == null ? "" : texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
    }

    private int obterIdBotao(ContextoAcao contexto) {
        try {
            java.lang.reflect.Method getIdBotao = contexto.getClass().getMethod("getIdBotao");
            Object id = getIdBotao.invoke(contexto);
            return id != null ? ((Number) id).intValue() : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static class ResultadoProcessamento {
        int sucessos, erros;
        Set<String> mensagensErro = new LinkedHashSet<>();
    }
}
