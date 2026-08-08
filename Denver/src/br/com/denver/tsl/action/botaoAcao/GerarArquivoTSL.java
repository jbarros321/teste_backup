package br.com.denver.tsl.action.botaoAcao;

import br.com.denver.tsl.repository.RecebimentoRepository;
import br.com.denver.tsl.service.TSLService;
import br.com.denver.tsl.util.DownloadHelper;
import br.com.denver.tsl.util.TSLFormatter;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

public class GerarArquivoTSL implements AcaoRotinaJava {
    private static final String CAMINHO_EXPORTACAO_PADRAO = System.getProperty("java.io.tmpdir");

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        try {
            if (contexto.getLinhas() == null || contexto.getLinhas().length == 0) {
                contexto.setMensagemRetorno("Selecione uma nota de venda antes de acionar o botão de ação!");
                return;
            }
            BigDecimal nunota = (BigDecimal) contexto.getLinhas()[0].getCampo("NUNOTA");
            if (nunota == null) {
                contexto.setMensagemRetorno("Selecione uma nota de venda antes de acionar o botão de ação!");
                return;
            }

            Set<String> lotes = new RecebimentoRepository().buscarLotesPorNunota(nunota);
            new File(CAMINHO_EXPORTACAO_PADRAO).mkdirs();

            TSLService service = new TSLService();
            StringBuilder mensagem = new StringBuilder();
            Set<String> caminhosArquivos = new LinkedHashSet<>(2);

            if (!lotes.isEmpty()) {
                lotes.stream().filter(StringUtils::isNotEmpty).forEach(lote ->
                    processarInterfaceRecebimento(service::gerarArquivoRecebimento, CAMINHO_EXPORTACAO_PADRAO, lote, "Recebimento (Lote: " + lote + ")", mensagem, caminhosArquivos));
            }
            processarInterface(service::gerarArquivoExpedicao, CAMINHO_EXPORTACAO_PADRAO, nunota, "Expedição", mensagem, caminhosArquivos);

            if (caminhosArquivos.isEmpty()) {
                contexto.setMensagemRetorno(mensagem.length() == 0 ? "Nenhum arquivo foi gerado." : "Arquivos TSL gerados com sucesso!\n\n" + mensagem.toString());
            } else {
                try {
                    String nomeZip = "TSL_" + TSLFormatter.formatarTimestamp(TimeUtils.getNow()) + ".zip";
                    String nomeZipRetornado = DownloadHelper.criarZip(caminhosArquivos, nomeZip);
                    mensagem.append("\n\nArquivos compactados em ZIP: ").append(nomeZipRetornado).append("\n");
                    String scriptDownload = DownloadHelper.gerarScriptDownloadZip(nomeZipRetornado);
                    mensagem.append(StringUtils.isEmpty(scriptDownload) ? "\n[ERRO] Falha ao gerar script de download automático. Use o link manual abaixo.\n" : scriptDownload);
                } catch (Exception e) {
                    mensagem.append("\n[ERRO] Erro ao criar ZIP: ").append(StringUtils.isEmpty(e.getMessage()) ? e.getClass().getSimpleName() + (e.getCause() != null && e.getCause().getMessage() != null ? ": " + e.getCause().getMessage() : "") : e.getMessage()).append("\n");
                }
                contexto.setMensagemRetorno("Arquivos TSL gerados com sucesso!\n\n" + mensagem.toString());
            }
        } catch (Exception e) {
            contexto.setMensagemRetorno("Erro ao gerar arquivos TSL: " + StringUtils.getNullAsEmpty(e.getMessage()));
            throw e;
        }
    }

    @FunctionalInterface
    private interface InterfaceProcessor { String processar(String caminho, BigDecimal nunota) throws Exception; }

    @FunctionalInterface
    private interface InterfaceProcessorRecebimento { String processar(String caminho, String lote) throws Exception; }

    private void processarInterface(InterfaceProcessor processor, String caminho, BigDecimal nunota, String nome, StringBuilder mensagem, Set<String> caminhosArquivos) {
        try {
            String caminhoArquivo = processor.processar(caminho, nunota);
            if (StringUtils.isNotEmpty(caminhoArquivo)) {
                File arquivo = new File(caminhoArquivo);
                mensagem.append(nome).append(": ").append(arquivo.getName()).append("\n");
                if (arquivo.exists() && arquivo.isFile()) caminhosArquivos.add(caminhoArquivo);
            }
        } catch (Exception e) {
            mensagem.append("Erro ao gerar ").append(nome).append(": ").append(StringUtils.getNullAsEmpty(e.getMessage())).append("\n");
        }
    }

    private void processarInterfaceRecebimento(InterfaceProcessorRecebimento processor, String caminho, String lote, String nome, StringBuilder mensagem, Set<String> caminhosArquivos) {
        try {
            String caminhoArquivo = processor.processar(caminho, lote);
            if (StringUtils.isNotEmpty(caminhoArquivo)) {
                File arquivo = new File(caminhoArquivo);
                mensagem.append(nome).append(": ").append(arquivo.getName()).append("\n");
                if (arquivo.exists() && arquivo.isFile()) caminhosArquivos.add(caminhoArquivo);
            }
        } catch (Exception e) {
            mensagem.append("Erro ao gerar ").append(nome).append(": ").append(StringUtils.getNullAsEmpty(e.getMessage())).append("\n");
        }
    }
}
