package br.com.cvs.test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.com.cvs.ocr.OCRHelper;

public class OCRTest {

    private static final String IMAGE_FOLDER = "/img";
    private static final String OUTPUT_FILE = "/ocr_complete_results.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("=== HELPER UNICO DE OCR ===");
        System.out.println("Extracao completa de informacoes do comprovante de entrega\n");

        try {
            String imageFolder = System.getProperty("user.dir") + IMAGE_FOLDER;
            System.out.println("Pasta: " + imageFolder);

            OCRHelper helper = new OCRHelper();
            List<OCRHelper.DeliveryInfo> results = helper.processAllImages(imageFolder);

            displayResults(results);
            saveResults(results);

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== CONCLUIDO ===");
    }

    private static void displayResults(List<OCRHelper.DeliveryInfo> results) {
        System.out.println("\n=== RESULTADOS DETALHADOS ===");
        results.forEach(info -> {
            System.out.println(info.toString());
            System.out.println("---");
        });

        long withData = results.stream()
                              .filter(OCRHelper.DeliveryInfo::hasData)
                              .count();

        System.out.println("\n=== RESUMO ===");
        System.out.println("Total de imagens: " + results.size());
        System.out.println("Com dados extraidos: " + withData);
        System.out.println("Taxa de sucesso: " + String.format("%.1f%%", (withData * 100.0 / results.size())));
    }

    private static void saveResults(List<OCRHelper.DeliveryInfo> results) {
        try {
            String outputFile = System.getProperty("user.dir") + OUTPUT_FILE;

            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(outputFile), java.nio.charset.StandardCharsets.ISO_8859_1)) {
                writer.write("=== EXTRACAO COMPLETA - COMPROVANTE DE ENTREGA ===\n");
                writer.write("Data: " + LocalDateTime.now().format(DATE_FORMATTER) + "\n\n");

                results.forEach(info -> {
                    try {
                        writer.write("=== " + info.getFileName() + " ===\n");
                        writer.write("Periodo: " + info.getPeriodo() + "\n");
                        writer.write("Mes Referencia: " + info.getMesReferencia() + "\n");
                        writer.write("Pedido: " + info.getPedido() + "\n");
                        writer.write("Empresa: " + info.getEmpresa() + "\n");
                        writer.write("Colaborador: " + info.getColaborador() + "\n");
                        writer.write("Endereco: " + info.getEndereco() + "\n");
                        writer.write("Nome da Cesta: " + info.getNomeCesta() + "\n");
                        writer.write("Qtde: " + info.getQtde() + "\n");
                        writer.write("Emissao: " + info.getEmissao() + "\n");
                        writer.write("NF-e: " + info.getNfe() + "\n");
                        writer.write("Matric: " + info.getMatric() + "\n");
                        writer.write("CEP: " + info.getCep() + "\n");
                        writer.write("Guia: " + info.getGuia() + "\n");
                        writer.write("Codigo de Barras: " + info.getCodigoBarras() + "\n");
                        writer.write("\n" + "==================================================\n\n");
                    } catch (IOException e) {
                        System.err.println("Erro ao escrever dados: " + e.getMessage());
                    }
                });
            }

            System.out.println("Resultados salvos em: " + outputFile);

        } catch (IOException e) {
            System.err.println("Erro ao salvar: " + e.getMessage());
        }
    }
}
