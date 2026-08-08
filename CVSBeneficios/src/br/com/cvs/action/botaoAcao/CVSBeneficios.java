package br.com.cvs.action.botaoAcao;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import br.com.cvs.ocr.OCRHelper;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class CVSBeneficios implements AcaoRotinaJava {

    private static final String IMAGE_FOLDER = "/img";
    private static final String OUTPUT_FILE = "/cvs_complete_results.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        System.out.println("=== INICIO CVS BENEFICIOS - OCR PROCESSING ===");

        if (ctx.getLinhas() == null || ctx.getLinhas().length == 0) {
            ctx.setMensagemRetorno("Nenhum registro selecionado!");
            return;
        }

        try {
            String imageFolder = System.getProperty("user.dir") + IMAGE_FOLDER;
            System.out.println("Processando informacoes da pasta: " + imageFolder);

            OCRHelper helper = new OCRHelper();
            List<OCRHelper.DeliveryInfo> results = helper.processAllImages(imageFolder);

            processResults(results, ctx);

        } catch (Exception e) {
            String errorMsg = "Erro no processamento OCR: " + e.getMessage();
            ctx.setMensagemRetorno(errorMsg);
            System.err.println(errorMsg);
            e.printStackTrace();
        }

        System.out.println("=== FIM CVS BENEFICIOS - OCR PROCESSING ===");
    }

    private void processResults(List<OCRHelper.DeliveryInfo> results, ContextoAcao ctx) throws IOException {
        long successCount = results.stream()
                                 .filter(OCRHelper.DeliveryInfo::hasData)
                                 .count();

        String info = results.stream()
                           .filter(OCRHelper.DeliveryInfo::hasData)
                           .map(this::formatResultInfo)
                           .collect(Collectors.joining("\n"));

        saveCompleteResults(results);

        String message = String.format(
            "Informacoes extraidas! %d/%d imagens processadas com sucesso.\n\n%s",
            successCount, results.size(), info
        );

        ctx.setMensagemRetorno(message);
        System.out.println(message);
    }

    private String formatResultInfo(OCRHelper.DeliveryInfo result) {
        return result.getFileName() + ":\n" +
               "  Periodo: " + result.getPeriodo() + "\n" +
               "  Mes Referencia: " + result.getMesReferencia() + "\n" +
               "  Pedido: " + result.getPedido() + "\n" +
               "  Empresa: " + result.getEmpresa() + "\n" +
               "  Colaborador: " + result.getColaborador() + "\n" +
               "  Endereco: " + result.getEndereco() + "\n" +
               "  Nome da Cesta: " + result.getNomeCesta() + "\n" +
               "  Qtde: " + result.getQtde() + "\n" +
               "  Emissao: " + result.getEmissao() + "\n" +
               "  NF-e: " + result.getNfe() + "\n" +
               "  Matric: " + result.getMatric() + "\n" +
               "  CEP: " + result.getCep() + "\n" +
               "  Guia: " + result.getGuia() + "\n" +
               "  Codigo de Barras: " + result.getCodigoBarras() + "\n";
    }

    private void saveCompleteResults(List<OCRHelper.DeliveryInfo> results) {
        try {
            String outputFile = System.getProperty("user.dir") + OUTPUT_FILE;

            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write("=== RESULTADOS COMPLETOS - CVS BENEFICIOS ===\n");
                writer.write("Data: " + LocalDateTime.now().format(DATE_FORMATTER) + "\n");
                writer.write("Total de imagens: " + results.size() + "\n\n");

                results.forEach(result -> {
                    try {
                        writer.write("=== " + result.getFileName() + " ===\n");
                        writer.write("Periodo: " + result.getPeriodo() + "\n");
                        writer.write("Mes Referencia: " + result.getMesReferencia() + "\n");
                        writer.write("Pedido: " + result.getPedido() + "\n");
                        writer.write("Empresa: " + result.getEmpresa() + "\n");
                        writer.write("Colaborador: " + result.getColaborador() + "\n");
                        writer.write("Endereco: " + result.getEndereco() + "\n");
                        writer.write("Nome da Cesta: " + result.getNomeCesta() + "\n");
                        writer.write("Qtde: " + result.getQtde() + "\n");
                        writer.write("Emissao: " + result.getEmissao() + "\n");
                        writer.write("NF-e: " + result.getNfe() + "\n");
                        writer.write("Matric: " + result.getMatric() + "\n");
                        writer.write("CEP: " + result.getCep() + "\n");
                        writer.write("Guia: " + result.getGuia() + "\n");
                        writer.write("Codigo de Barras: " + result.getCodigoBarras() + "\n");
                        writer.write("\n" + "==================================================\n\n");
                    } catch (IOException e) {
                        System.err.println("Erro ao escrever dados: " + e.getMessage());
                    }
                });
            }

            System.out.println("Resultados salvos em: " + outputFile);

        } catch (IOException e) {
            System.err.println("Erro ao salvar resultados: " + e.getMessage());
        }
    }
}
