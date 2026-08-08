package br.com.serpa.action.botaoAcao;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.serpa.shared.SerpaTXTHelper;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;

public class ExportarArquivoTXT implements AcaoRotinaJava {

    private EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
    private static final String TABELA_LOG = "AD_SERPALOG";

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        try {

            validarContexto(contexto);

            Registro[] linhas = contexto.getLinhas();
            if (linhas == null || linhas.length == 0) {
                contexto.setMensagemRetorno("Nenhum registro selecionado para exportação.");
                return;
            }

            String nomeArquivo = gerarNomeArquivo();
            String caminhoCompleto = obterCaminhoExportacao() + "/" + nomeArquivo;

            int totalRegistros = processarExportacao(linhas, caminhoCompleto);

            SerpaTXTHelper.registrarLog(TABELA_LOG, "EXPORTACAO", "SUCESSO",
                "Arquivo exportado: " + nomeArquivo + " | Registros: " + totalRegistros, contexto);

            contexto.setMensagemRetorno(
                String.format("Arquivo TXT exportado com sucesso!\n\n" +
                    "Arquivo: %s\n" +
                    "Registros exportados: %d\n" +
                    "Localização: %s",
                    nomeArquivo, totalRegistros, caminhoCompleto));

        } catch (Exception e) {

            try {
                SerpaTXTHelper.registrarLog(TABELA_LOG, "EXPORTACAO", "ERRO",
                    "Erro na exportação: " + e.getMessage(), contexto);
            } catch (Exception logError) {
                System.err.println("Erro ao registrar log: " + logError.getMessage());
            }

            contexto.setMensagemRetorno("Erro ao exportar arquivo TXT: " + e.getMessage());
            throw new Exception("Erro na exportação: " + e.getMessage(), e);
        }
    }

    private void validarContexto(ContextoAcao contexto) throws Exception {
        if (contexto == null) {
            throw new Exception("Contexto da ação é obrigatório");
        }
    }

    private int processarExportacao(Registro[] linhas, String caminhoArquivo) throws Exception {
        try (FileWriter writer = new FileWriter(caminhoArquivo, false)) {
            int totalRegistros = 0;

            for (Registro registro : linhas) {

                String linhaTXT = SerpaTXTHelper.formatarLinhaTXT(registro);

                if (linhaTXT != null && !linhaTXT.isEmpty()) {
                    writer.write(linhaTXT);
                    writer.write(System.lineSeparator());
                    totalRegistros++;
                }
            }

            writer.flush();
            return totalRegistros;

        } catch (IOException e) {
            throw new Exception("Erro ao escrever arquivo: " + e.getMessage(), e);
        }
    }

    private String gerarNomeArquivo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return "SERPA_EXPORT_" + timestamp + ".txt";
    }

    private String obterCaminhoExportacao() {

        String caminho = System.getProperty("java.io.tmpdir") + "/serpa_export";

        java.io.File diretorio = new java.io.File(caminho);
        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }

        return caminho;
    }
}
