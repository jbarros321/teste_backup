package br.com.cvs.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

public class OCRHelper {

    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png"};

    private static final Pattern PEDIDO = Pattern.compile("(?:Pedido|PEDIDO):\\s*(\\d{7})");
    private static final Pattern EMPRESA = Pattern.compile("(?:Empresa|EMPRESA):\\s*([A-Z\\s]+(?:LTDA|ME|EIRELI|TRANSPORTES|URBANOS|CONSORCIO))");
    private static final Pattern COLABORADOR = Pattern.compile("(?:Colaborador|COLABORADOR):\\s*([A-Z\\s]{10,50})");
    private static final Pattern ENDERECO = Pattern.compile("(?:Endereco|ENDERECO|Endereo):\\s*(RUA?\\s+[A-Z\\s\\d\\-]{10,100})");
    private static final Pattern CESTA = Pattern.compile("(?:Nome\\s*da\\s*Cesta|NOME\\s*DA\\s*CESTA):\\s*([A-Z]{3,20})");
    private static final Pattern EMISSAO = Pattern.compile("(?:Emissao|EMISSAO):\\s*(\\d{2}/\\d{2}/\\d{4})");
    private static final Pattern NFE = Pattern.compile("(?:NF-e|NFE|NFe):\\s*(\\d{7})");
    private static final Pattern MATRIC = Pattern.compile("(?:Matric|MATRIC)\\.?:\\s*(\\d{4,5})");
    private static final Pattern CEP = Pattern.compile("(?:Cep|CEP):\\s*(\\d{5}-\\d{3})");
    private static final Pattern GUIA = Pattern.compile("(?:Guia|GUIA):\\s*(\\d{3}-[A-Za-z]{2})");
    private static final Pattern CODIGO_BARRAS = Pattern.compile("(?:Codigo\\s*de\\s*Barras|CODIGO\\s*DE\\s*BARRA):\\s*(\\d{7,8})");
    private static final Pattern CODIGO_BARRAS_FALLBACK = Pattern.compile("\\b(\\d{7,8})\\b");
    private static final Pattern PERIODO = Pattern.compile("(?:Periodo|PERIODO):\\s*(\\d+)\\s*a\\s*(\\d+)");
    private static final Pattern MES = Pattern.compile("(?:Mes\\s*Referencia|MES\\s*REFERENCIA):\\s*(\\d+)\\s*/\\s*(\\d{4})");
    private static final Pattern QTDE = Pattern.compile("(?:Qtde|QTDE)\\.?:\\s*(\\d+)");
    private static final Pattern DATE = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b");

    private final ITesseract tesseract;

    public OCRHelper() {
        this.tesseract = new Tesseract();
        this.tesseract.setLanguage("por");
        this.tesseract.setPageSegMode(6);
        this.tesseract.setOcrEngineMode(3);
        this.tesseract.setVariable("user_defined_dpi", "300");

        String tessdataPath = System.getProperty("user.dir") + "/tessdata";
        new File(tessdataPath).mkdirs();
        this.tesseract.setDatapath(tessdataPath);
    }

    public DeliveryInfo extractAllInfo(String imagePath) {
        try {
            System.out.println("Processando: " + new File(imagePath).getName());

            String text = Stream.of(
                extractText(imagePath, this::preprocessImage),
                extractText(imagePath, this::preprocessImageAlternative),
                extractText(imagePath, this::preprocessImageGeneric)
            )
            .filter(t -> t != null && !t.trim().isEmpty())
            .findFirst()
            .orElse("");

            DeliveryInfo info = analyzeText(text);
            info.setFileName(new File(imagePath).getName());

            System.out.println("Informacoes extraidas com sucesso");
            return info;
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            return new DeliveryInfo();
        }
    }

    private String extractText(String imagePath, java.util.function.Function<BufferedImage, BufferedImage> processor) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            return tesseract.doOCR(processor.apply(image));
        } catch (Exception e) {
            return "";
        }
    }

    private BufferedImage preprocessImage(BufferedImage original) {
        return processImage(original, rgb -> (rgb[0] * 0.299 + rgb[1] * 0.587 + rgb[2] * 0.114) > 140 ? 255 : 0);
    }

    private BufferedImage preprocessImageAlternative(BufferedImage original) {
        return processImage(original, rgb -> (rgb[0] * 0.2126 + rgb[1] * 0.7152 + rgb[2] * 0.0722) > 160 ? 255 : 0);
    }

    private BufferedImage preprocessImageGeneric(BufferedImage original) {
        return processImage(original, rgb -> (rgb[0] + rgb[1] + rgb[2]) / 3 > 150 ? 255 : 0);
    }

    private BufferedImage processImage(BufferedImage original, java.util.function.Function<int[], Integer> processor) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage processed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = original.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int gray = processor.apply(new int[]{r, g, b});
                int newRgb = (gray << 16) | (gray << 8) | gray;
                processed.setRGB(x, y, newRgb);
            }
        }
        return processed;
    }

    private String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) return "";

        return Arrays.stream(text.split("\\n"))
            .map(line -> removeAccents(line))
            .map(line -> line.replaceAll("[^\\w\\s\\-\\.,:/()]", " "))
            .map(line -> line.replaceAll("\\s+", " "))
            .map(line -> line.replaceAll("\\bl\\b", "1").replaceAll("\\bO\\b", "0").replaceAll("\\bI\\b", "1"))
            .collect(Collectors.joining(" "))
            .trim();
    }

    private String removeAccents(String text) {
        return text.replaceAll("[]", "a")
                  .replaceAll("[]", "e")
                  .replaceAll("[]", "i")
                  .replaceAll("[]", "o")
                  .replaceAll("[]", "u")
                  .replaceAll("[]", "c")
                  .replaceAll("[]", "n")
                  .replaceAll("[]", "A")
                  .replaceAll("[]", "E")
                  .replaceAll("[]", "I")
                  .replaceAll("[]", "O")
                  .replaceAll("[]", "U")
                  .replaceAll("[]", "C")
                  .replaceAll("[]", "N");
    }

    public List<DeliveryInfo> processAllImages(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Pasta nao encontrada: " + folderPath);
            return Arrays.asList();
        }

        File[] imageFiles = folder.listFiles((dir, name) ->
            Arrays.stream(IMAGE_EXTENSIONS).anyMatch(ext -> name.toLowerCase().endsWith(ext))
        );

        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("Nenhuma imagem encontrada");
            return Arrays.asList();
        }

        System.out.println("=== EXTRACAO COMPLETA DE INFORMACOES ===");
        System.out.println("Encontradas " + imageFiles.length + " imagens\n");

        return Arrays.stream(imageFiles)
            .map(file -> extractAllInfo(file.getAbsolutePath()))
            .collect(Collectors.toList());
    }

    private DeliveryInfo analyzeText(String text) {
        DeliveryInfo info = new DeliveryInfo();
        text = cleanText(text);

        extractField(text, PEDIDO, info::setPedido, this::isValidPedido);
        extractField(text, EMPRESA, info::setEmpresa, this::isValidEmpresa);
        extractField(text, COLABORADOR, info::setColaborador, this::isValidColaborador);
        extractField(text, ENDERECO, info::setEndereco, this::isValidEndereco);
        extractField(text, CESTA, info::setNomeCesta, this::isValidCesta);
        extractField(text, EMISSAO, info::setEmissao, this::isValidEmissao);
        extractField(text, NFE, info::setNfe, this::isValidNfe);
        extractField(text, MATRIC, info::setMatric, this::isValidMatric);
        extractField(text, CEP, info::setCep, this::isValidCep);
        extractField(text, GUIA, info::setGuia, this::isValidGuia);
        extractCodigoBarras(text, info);
        extractField(text, PERIODO, s -> info.setPeriodo(s), s -> true);
        extractField(text, MES, s -> info.setMesReferencia(s), s -> true);
        extractField(text, QTDE, info::setQtde, s -> true);

        if (info.getEmissao().isEmpty()) {
            extractEmissaoFallback(text, info);
        }

        if (info.getQtde().isEmpty()) {
            info.setQtde("1");
        }

        return info;
    }

    private void extractField(String text, Pattern pattern, java.util.function.Consumer<String> setter, java.util.function.Predicate<String> validator) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            if (validator.test(value)) {
                setter.accept(value);
                System.out.println("Campo extrado: " + value);
            }
        }
    }

    private void extractCodigoBarras(String text, DeliveryInfo info) {

        Matcher matcher = CODIGO_BARRAS.matcher(text);
        if (matcher.find()) {
            String codigo = matcher.group(1).trim();
            if (isValidCodigoBarras(codigo)) {
                info.setCodigoBarras(codigo);
                System.out.println("Codigo de Barras extraido: " + codigo);
                return;
            }
        }

        matcher = CODIGO_BARRAS_FALLBACK.matcher(text);
        while (matcher.find()) {
            String codigo = matcher.group(1).trim();
            if (isValidCodigoBarras(codigo) && !codigo.equals(info.getPedido()) && !codigo.equals(info.getNfe())) {
                info.setCodigoBarras(codigo);
                System.out.println("Codigo de Barras extraido (fallback): " + codigo);
                return;
            }
        }
    }

    private boolean isValidCodigoBarras(String codigo) {
        return codigo != null && codigo.matches("\\d{7,8}") && !codigo.startsWith("0");
    }

    private void extractEmissaoFallback(String text, DeliveryInfo info) {
        Matcher matcher = DATE.matcher(text);
        while (matcher.find()) {
            String data = matcher.group(1);
            if (isValidEmissao(data)) {
                info.setEmissao(data);
                System.out.println("Emissao extraida (fallback): " + data);
                return;
            }
        }
    }

    private boolean isValidPedido(String pedido) {
        return pedido != null && pedido.matches("\\d{7}") && !pedido.startsWith("0");
    }

    private boolean isValidEmpresa(String empresa) {
        return empresa != null && empresa.length() > 15 && empresa.length() < 100 &&
               (empresa.contains("TRANSPORTES") || empresa.contains("LTDA") || empresa.contains("EIRELI") || empresa.contains("ME"));
    }

    private boolean isValidColaborador(String colaborador) {
        return colaborador != null && colaborador.length() > 10 && colaborador.length() < 50 &&
               !colaborador.contains("VIACAO") && !colaborador.contains("TRANSPORTES") &&
               !colaborador.contains("GATUSA") && !colaborador.contains("KBPX") &&
               !colaborador.contains("CONSORCIO") && colaborador.matches(".*[A-Z]{3,}.*");
    }

    private boolean isValidEndereco(String endereco) {
        return endereco != null && endereco.length() > 15 && endereco.length() < 100 &&
               (endereco.contains("RUA") || endereco.contains("AV") || endereco.contains("R.")) &&
               endereco.matches(".*\\d+.*");
    }

    private boolean isValidCesta(String cesta) {
        return cesta != null && cesta.length() >= 3 && cesta.length() <= 20 && cesta.matches("[A-Z]+");
    }

    private boolean isValidEmissao(String emissao) {
        return emissao != null && emissao.matches("\\d{2}/\\d{2}/\\d{4}") &&
               !emissao.startsWith("00/") && !emissao.endsWith("/0000");
    }

    private boolean isValidNfe(String nfe) {
        return nfe != null && nfe.matches("\\d{7}") && !nfe.startsWith("0");
    }

    private boolean isValidMatric(String matric) {
        return matric != null && matric.matches("\\d{4,5}") && !matric.startsWith("0") && Integer.parseInt(matric) > 100;
    }

    private boolean isValidCep(String cep) {
        if (cep == null || !cep.matches("\\d{5}-\\d{3}")) return false;
        String[] parts = cep.split("-");
        return Integer.parseInt(parts[0]) > 100 && Integer.parseInt(parts[1]) > 0;
    }

    private boolean isValidGuia(String guia) {
        return guia != null && guia.length() >= 5 && guia.length() <= 10 && guia.matches("\\d{3}-[A-Za-z]{2}");
    }

    public static class DeliveryInfo {
        private String fileName = "";
        private String periodo = "";
        private String mesReferencia = "";
        private String pedido = "";
        private String empresa = "";
        private String colaborador = "";
        private String endereco = "";
        private String nomeCesta = "";
        private String qtde = "";
        private String emissao = "";
        private String nfe = "";
        private String matric = "";
        private String cep = "";
        private String guia = "";
        private String codigoBarras = "";

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getPeriodo() { return periodo; }
        public void setPeriodo(String periodo) { this.periodo = periodo; }
        public String getMesReferencia() { return mesReferencia; }
        public void setMesReferencia(String mesReferencia) { this.mesReferencia = mesReferencia; }
        public String getPedido() { return pedido; }
        public void setPedido(String pedido) { this.pedido = pedido; }
        public String getEmpresa() { return empresa; }
        public void setEmpresa(String empresa) { this.empresa = empresa; }
        public String getColaborador() { return colaborador; }
        public void setColaborador(String colaborador) { this.colaborador = colaborador; }
        public String getEndereco() { return endereco; }
        public void setEndereco(String endereco) { this.endereco = endereco; }
        public String getNomeCesta() { return nomeCesta; }
        public void setNomeCesta(String nomeCesta) { this.nomeCesta = nomeCesta; }
        public String getQtde() { return qtde; }
        public void setQtde(String qtde) { this.qtde = qtde; }
        public String getEmissao() { return emissao; }
        public void setEmissao(String emissao) { this.emissao = emissao; }
        public String getNfe() { return nfe; }
        public void setNfe(String nfe) { this.nfe = nfe; }
        public String getMatric() { return matric; }
        public void setMatric(String matric) { this.matric = matric; }
        public String getCep() { return cep; }
        public void setCep(String cep) { this.cep = cep; }
        public String getGuia() { return guia; }
        public void setGuia(String guia) { this.guia = guia; }
        public String getCodigoBarras() { return codigoBarras; }
        public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

        public boolean hasData() {
            return !periodo.isEmpty() || !mesReferencia.isEmpty() || !pedido.isEmpty() ||
                   !empresa.isEmpty() || !colaborador.isEmpty() || !endereco.isEmpty() ||
                   !nomeCesta.isEmpty() || !qtde.isEmpty() || !emissao.isEmpty() ||
                   !nfe.isEmpty() || !matric.isEmpty() || !cep.isEmpty() || !guia.isEmpty() ||
                   !codigoBarras.isEmpty();
        }

        @Override
        public String toString() {
            return String.format("=== %s ===\nPeriodo: %s\nMes Referencia: %s\nPedido: %s\nEmpresa: %s\nColaborador: %s\nEndereco: %s\nNome da Cesta: %s\nQtde: %s\nEmissao: %s\nNF-e: %s\nMatric: %s\nCEP: %s\nGuia: %s\nCodigo de Barras: %s\n",
                fileName, periodo, mesReferencia, pedido, empresa, colaborador, endereco, nomeCesta, qtde, emissao, nfe, matric, cep, guia, codigoBarras);
        }
    }
}
