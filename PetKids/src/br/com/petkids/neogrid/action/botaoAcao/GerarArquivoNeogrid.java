package br.com.petkids.neogrid.action.botaoAcao;

import br.com.petkids.neogrid.exception.NeogridException;
import br.com.petkids.neogrid.service.NeogridService;
import br.com.petkids.neogrid.util.DownloadHelper;
import br.com.petkids.neogrid.util.NeogridFormatter;
import br.com.petkids.neogrid.validation.NeogridValidator;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GerarArquivoNeogrid implements AcaoRotinaJava, ScheduledAction {

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        try {
            String cnpjFilial = buscarCnpjFilial();
            if (cnpjFilial == null || cnpjFilial.trim().isEmpty()) {
                String erro = "CNPJ da filial não encontrado. Verifique se há empresas cadastradas no sistema.";
                contexto.setMensagemRetorno(erro);
                throw new NeogridException(erro);
            }
            Date periodoIni = obterParametroDataContexto(contexto, "PERIODO_INI", null);
            Date periodoFin = obterParametroDataContexto(contexto, "PERIODO_FIN", null);
            String mensagem = executarGeracao("TODOS", cnpjFilial, null, null, periodoIni, periodoFin, true);
            if (mensagem == null || mensagem.trim().isEmpty()) {
                mensagem = "Nenhum arquivo foi gerado.";
            }
            contexto.setMensagemRetorno("Arquivos Neogrid gerados com sucesso!\n\n" + mensagem);
        } catch (NeogridException e) {
            String msg = "Erro ao gerar arquivos Neogrid: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            contexto.setMensagemRetorno(msg);
            throw e;
        } catch (Exception e) {
            String msgErro = org.apache.commons.lang.exception.ExceptionUtils.getMessage(e);
            if (msgErro == null || msgErro.trim().isEmpty()) {
                msgErro = e.getClass().getSimpleName();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    msgErro += ": " + e.getCause().getMessage();
                }
            }
            String msg = "Erro ao gerar arquivos Neogrid: " + msgErro;
            contexto.setMensagemRetorno(msg);
            throw new Exception("Erro na geração: " + msgErro, e);
        }
    }

    @Override
    public void onTime(ScheduledActionContext ctx) {
        try {
            ctx.info("Iniciando geração automática de arquivos Neogrid...");
            String tipoRelatorio = obterParametroConfiguracao("petkids.neogrid.tipo.relatorio", "TODOS");
            String cnpjFilial = obterParametroConfiguracao("petkids.neogrid.cnpj.filial", null);
            String caminhoExportacao = obterParametroConfiguracao("petkids.neogrid.caminho.exportacao", null);
            if (cnpjFilial == null || cnpjFilial.trim().isEmpty()) {
                throw new Exception("CNPJ da filial não configurado. Configure o parâmetro 'petkids.neogrid.cnpj.filial'");
            }
            String mensagem = executarGeracao(tipoRelatorio, cnpjFilial, null, caminhoExportacao, null, null, false);
            ctx.info("Geração automática concluída com sucesso!\n" + mensagem);
        } catch (NeogridException e) {
            ctx.info("Erro ao gerar arquivos Neogrid: " + org.apache.commons.lang.exception.ExceptionUtils.getMessage(e));
            ctx.log("Stack trace completo:\n" + org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            ctx.info("Erro na geração automática: " + org.apache.commons.lang.exception.ExceptionUtils.getMessage(e));
            ctx.log("Stack trace completo:\n" + org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
        }
    }

    private String executarGeracao(String tipoRelatorio, String cnpjFilial, String cnpjIndustria,
            String caminhoExportacao, Date periodoIni, Date periodoFin, boolean habilitarDownload) throws NeogridException {
        String caminhoExportacaoFinal;
        try {
            caminhoExportacaoFinal = NeogridValidator.validarCaminhoExportacao(caminhoExportacao);
        } catch (br.com.petkids.neogrid.exception.NeogridValidationException e) {
            throw new NeogridException("Erro ao validar caminho de exportação: " + (e.getMessage() != null ? e.getMessage() : "Caminho inválido"), e);
        }
        final String cnpjFilialFinal = cnpjFilial;
        final String cnpjIndParam = (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty()) ? NeogridFormatter.formatarCnpjCpf(cnpjIndustria) : null;
        final Date periodoIniFinal = periodoIni;
        final Date periodoFinFinal = periodoFin;
        NeogridService service = new NeogridService();
        StringBuilder mensagem = new StringBuilder(500);
        List<String> caminhosArquivos = new ArrayList<>(5);
        String tipoUpper = tipoRelatorio.toUpperCase();

        if (tipoUpper.equals("TODOS") || tipoUpper.equals("VENDEDORES") || tipoUpper.equals("RELVEN")) {
            adicionarRelatorio(() -> service.gerarRelatorioVendedores(cnpjFilialFinal, caminhoExportacaoFinal), "Vendedores", habilitarDownload, caminhosArquivos, mensagem, caminhoExportacaoFinal);
        }
        if (tipoUpper.equals("TODOS") || tipoUpper.equals("CLIENTES") || tipoUpper.equals("RELCLI")) {
            adicionarRelatorio(() -> service.gerarRelatorioClientes(cnpjFilialFinal, caminhoExportacaoFinal), "Clientes", habilitarDownload, caminhosArquivos, mensagem, caminhoExportacaoFinal);
        }
        if (tipoUpper.equals("TODOS") || tipoUpper.equals("PRODUTOS") || tipoUpper.equals("RELPRO")) {
            adicionarRelatorioComIndustria(() -> service.gerarRelatorioProdutos(cnpjFilialFinal, cnpjIndParam, caminhoExportacaoFinal, periodoIniFinal, periodoFinFinal), "Produtos", habilitarDownload, caminhosArquivos, mensagem, caminhoExportacaoFinal);
        }
        if (tipoUpper.equals("TODOS") || tipoUpper.equals("VENDAS")) {
            adicionarRelatorioComIndustria(() -> service.gerarRelatorioVendas(cnpjFilialFinal, cnpjIndParam, caminhoExportacaoFinal, periodoIniFinal, periodoFinFinal), "Vendas", habilitarDownload, caminhosArquivos, mensagem, caminhoExportacaoFinal);
        }
        if (tipoUpper.equals("TODOS") || tipoUpper.equals("ESTOQUE") || tipoUpper.equals("RELEST")) {
            adicionarRelatorioComIndustria(() -> service.gerarRelatorioEstoque(cnpjFilialFinal, cnpjIndParam, caminhoExportacaoFinal, periodoIniFinal, periodoFinFinal), "Estoque", habilitarDownload, caminhosArquivos, mensagem, caminhoExportacaoFinal);
        }

        if (habilitarDownload && !caminhosArquivos.isEmpty()) {
            try {
                String nomeZip = "Neogrid_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".zip";
                String nomeZipRetornado = DownloadHelper.criarZip(caminhosArquivos, nomeZip);
                mensagem.append("\n\nArquivos compactados em ZIP: ").append(nomeZipRetornado).append("\n");

                String scriptDownload = DownloadHelper.gerarScriptDownloadZip(nomeZipRetornado);

                if (scriptDownload == null || scriptDownload.trim().isEmpty()) {
                    mensagem.append("\n[ERRO] Falha ao gerar script de download automático. Use o link manual abaixo.\n");
                } else {
                    mensagem.append(scriptDownload);
                }
            } catch (Exception e) {
                String mensagemErro = e.getMessage();
                if (mensagemErro == null || mensagemErro.trim().isEmpty()) {
                    mensagemErro = e.getClass().getSimpleName();
                    if (e.getCause() != null && e.getCause().getMessage() != null) {
                        mensagemErro += ": " + e.getCause().getMessage();
                    }
                }
                mensagem.append("\n[ERRO] Erro ao criar ZIP: ").append(mensagemErro).append("\n");
            }
        }

        return mensagem.toString();
    }

    private void adicionarRelatorio(java.util.concurrent.Callable<String> gerador, String nomeRelatorio,
            boolean habilitarDownload, List<String> caminhosArquivos, StringBuilder mensagem, String caminhoExportacao) {
        adicionarRelatorioComIndustria(gerador, nomeRelatorio, habilitarDownload, caminhosArquivos, mensagem, caminhoExportacao);
    }

    private void adicionarRelatorioComIndustria(java.util.concurrent.Callable<String> gerador, String nomeRelatorio,
            boolean habilitarDownload, List<String> caminhosArquivos, StringBuilder mensagem, String caminhoExportacao) {
        try {
            String arquivo = gerador.call();
            if (arquivo == null || arquivo.trim().isEmpty()) {
                mensagem.append("  [AVISO] ").append(nomeRelatorio.toLowerCase()).append(": Arquivo não gerado (sem dados ou erro interno)\n");
                return;
            }
            mensagem.append(nomeRelatorio).append(": ").append(arquivo).append("\n");
            if (habilitarDownload && caminhoExportacao != null && !caminhoExportacao.trim().isEmpty()) {
                String caminhoCompleto = caminhoExportacao + java.io.File.separator + arquivo;
                java.io.File arquivoVerificar = new java.io.File(caminhoCompleto);
                if (arquivoVerificar.exists() && arquivoVerificar.isFile()) {
                    caminhosArquivos.add(caminhoCompleto);
                }
            }
        } catch (br.com.petkids.neogrid.exception.NeogridValidationException e) {
            String prefixo = (e.getMessage() != null && e.getMessage().contains("Não há dados disponíveis"))
                ? "  [AVISO] " : "  [ERRO] Erro ao gerar ";
            String msgErro = e.getMessage() != null ? e.getMessage() : "Erro de validação";
            mensagem.append(prefixo).append(nomeRelatorio.toLowerCase()).append(": ").append(msgErro).append("\n");
        } catch (Exception e) {
            String msgErro = e.getMessage();
            if (msgErro == null || msgErro.trim().isEmpty()) {
                msgErro = e.getClass().getSimpleName();
                if (e.getCause() != null && e.getCause().getMessage() != null) {
                    msgErro += ": " + e.getCause().getMessage();
                }
            }
            mensagem.append("  [ERRO] Erro ao gerar ").append(nomeRelatorio.toLowerCase()).append(": ").append(msgErro).append("\n");
        }
    }

    private Date obterParametroDataContexto(ContextoAcao contexto, String nome, Date padrao) {
        try {
            Timestamp valor = (Timestamp) contexto.getParam(nome);
            return valor != null ? new Date(valor.getTime()) : padrao;
        } catch (Exception e) {
            return padrao;
        }
    }

    private String obterParametroConfiguracao(String nomeParametro, String padrao) {
        try {
            String valor = System.getProperty(nomeParametro);
            return (valor != null && !valor.trim().isEmpty()) ? valor : padrao;
        } catch (Exception e) {
            return padrao;
        }
    }

    private String buscarCnpjFilial() {
        JdbcWrapper jdbc = null;
        NativeSql sqlNative = null;
        ResultSet rs = null;
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();
            sqlNative = new NativeSql(jdbc);
            sqlNative.appendSql("SELECT REPLACE(REPLACE(REPLACE(REPLACE(EMP.CGC, '.', ''), '/', ''), '-', ''), ' ', '') AS CNPJ " +
                               "FROM TSIEMP EMP WHERE EMP.CGC IS NOT NULL AND ROWNUM = 1 ORDER BY EMP.CODEMP");
            rs = sqlNative.executeQuery();
            if (rs.next()) return rs.getString("CNPJ");
        } catch (Exception e) {
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception e) {}
            if (sqlNative != null) try { NativeSql.releaseResources(sqlNative); } catch (Exception e) {}
            if (jdbc != null) try { JdbcWrapper.closeSession(jdbc); } catch (Exception e) {}
        }
        return null;
    }
}
