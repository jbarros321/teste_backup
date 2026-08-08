package br.com.monteccer.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class DanfeHelper {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static class ResultadoDanfe {
        private final boolean sucesso;
        private final String mensagem;
        private final byte[] arquivoZip;
        private final String nomeArquivo;

        public ResultadoDanfe(boolean sucesso, String mensagem, byte[] arquivoZip, String nomeArquivo) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
            this.arquivoZip = arquivoZip;
            this.nomeArquivo = nomeArquivo;
        }

        public boolean isSucesso() { return sucesso; }
        public String getMensagem() { return mensagem; }
        public byte[] getArquivoZip() { return arquivoZip; }
        public String getNomeArquivo() { return nomeArquivo; }
    }

    public static ResultadoDanfe gerarDanfeZip(List<BigDecimal> notas) throws Exception {
        if (notas == null || notas.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma nota encontrada!");
        }

        List<BigDecimal> notasValidas = notas.stream()
            .filter(nunota -> nunota != null && nunota.compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());

        if (notasValidas.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma nota valida encontrada!");
        }

        byte[] zipBytes = gerarZipDanfe(notasValidas);
        String nomeArquivo = gerarNomeArquivoZip(notasValidas);

        String mensagem = "DANFEs gerados e disponibilizados para download! (" + notasValidas.size() + " notas processadas)";
        return new ResultadoDanfe(true, mensagem, zipBytes, nomeArquivo);
    }

    private static byte[] gerarZipDanfe(List<BigDecimal> notas) throws Exception {
        try (ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(zipBaos)) {

            int notasProcessadas = 0;
            int notasComErro = 0;

            for (BigDecimal nota : notas) {
                if (nota != null && nota.compareTo(BigDecimal.ZERO) > 0) {
                    try {
                        processarNotaParaZip(nota, zipOut);
                        notasProcessadas++;
                    } catch (Exception e) {
                        notasComErro++;
                        System.out.println("DEBUG: Erro ao processar nota " + nota + ": " + e.getMessage());
                    }
                }
            }

            zipOut.finish();
            zipOut.flush();

            byte[] result = zipBaos.toByteArray();
            System.out.println("DEBUG: ZIP gerado - " + notasProcessadas + " notas processadas, " + notasComErro + " com erro, tamanho total: " + result.length + " bytes");
            return result;
        }
    }

    private static void processarNotaParaZip(BigDecimal nunota, ZipOutputStream zipOut) throws Exception {
        System.out.println("DEBUG: Processando nota " + nunota + " para ZIP");

        Optional<byte[]> pdfBytesOpt = Optional.ofNullable(gerarPdfDanfe(nunota));
        pdfBytesOpt.filter(bytes -> bytes.length > 0).ifPresent(pdfBytes -> {
            try {
                zipOut.putNextEntry(new ZipEntry("DANFE_" + nunota + ".pdf"));
                zipOut.write(pdfBytes);
                zipOut.closeEntry();
                System.out.println("DEBUG: PDF DANFE adicionado ao ZIP - tamanho: " + pdfBytes.length + " bytes");
            } catch (IOException e) {
                System.out.println("DEBUG: Erro ao adicionar PDF ao ZIP: " + e.getMessage());
            }
        });
    }

    private static byte[] gerarPdfDanfe(BigDecimal nunota) throws Exception {
        System.out.println("DEBUG: Gerando PDF DANFE para nota: " + nunota);

        TipoNota tipoNota = identificarTipoNota(nunota);
        System.out.println("DEBUG: Tipo de nota identificado: " + tipoNota);

        if (tipoNota == TipoNota.NFSE) {
            try {
                byte[] pdfBytes = gerarPdfDanfeNFSeAlternativo(nunota);
                if (pdfBytes != null && pdfBytes.length > 0) {
                    return pdfBytes;
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Mtodo alternativo falhou, usando mtodo padro: " + e.getMessage());
            }
        }

        System.setProperty("pre.visualizar.documento", "1");
        System.setProperty("print.service.active", "false");

        SessionHandle sessionHandle = JapeSession.open();
        try {

            String classeImpressao = obterClasseImpressao(tipoNota);
            Class<?> impressaoNotaHelperClass = Class.forName(classeImpressao);
            Object impressaoNotaHelper = impressaoNotaHelperClass.newInstance();

            java.lang.reflect.Method inicializaNotaMethod = impressaoNotaHelperClass.getMethod("inicializaNota", BigDecimal.class);
            inicializaNotaMethod.invoke(impressaoNotaHelper, nunota);

            java.lang.reflect.Method validacoesMethod = impressaoNotaHelperClass.getMethod("validacoesImpressaoNota");
            validacoesMethod.invoke(impressaoNotaHelper);

            java.lang.reflect.Method gerarJasperPrintAnexoMethod = impressaoNotaHelperClass.getMethod("gerarJasperPrintAnexo");
            Object jasperPrintCollection = gerarJasperPrintAnexoMethod.invoke(impressaoNotaHelper);

            System.out.println("DEBUG: gerarJasperPrintAnexo retornou: " + (jasperPrintCollection != null ? jasperPrintCollection.getClass().getName() : "null"));

            if (jasperPrintCollection instanceof Collection) {
                Collection<?> collection = (Collection<?>) jasperPrintCollection;
                System.out.println("DEBUG: Collection com " + collection.size() + " itens");

                if (!collection.isEmpty()) {
                    Object firstItem = collection.iterator().next();
                    System.out.println("DEBUG: Primeiro item tipo: " + firstItem.getClass().getName());

                    System.out.println("DEBUG: Tentando extrair PDF do primeiro item para nota " + nunota);
                    byte[] pdfBytes = extrairPdfDeDynamicVO(firstItem, tipoNota);
                    if (pdfBytes != null && pdfBytes.length > 0) {
                        System.out.println("DEBUG: PDF gerado com sucesso para nota " + nunota + " - tamanho: " + pdfBytes.length + " bytes");
                        return pdfBytes;
                    } else {
                        System.out.println("DEBUG: Falha na extracao do PDF para nota " + nunota);
                    }
                } else {
                    System.out.println("DEBUG: Nota " + nunota + " nao tem modelos de impressao configurados - Collection vazia");
                }
            }

            System.out.println("DEBUG: Nota " + nunota + " nao gerou PDF - sem modelos de impressao ou falha na extracao");
            return null;
        } finally {
            JapeSession.close(sessionHandle);
        }
    }

    private static byte[] extrairPdfDeDynamicVO(Object dynamicVO, TipoNota tipoNota) throws Exception {
        System.out.println("DEBUG: Extraindo PDF de DynamicVO - tipo: " + dynamicVO.getClass().getName());

        try {
            java.lang.reflect.Method asBlobMethod = dynamicVO.getClass().getMethod("asBlob", String.class);
            byte[] jasperPrintBytes = (byte[]) asBlobMethod.invoke(dynamicVO, "CONTEUDO");

            if (jasperPrintBytes != null && jasperPrintBytes.length > 0) {
                System.out.println("DEBUG: JasperPrint obtido - tamanho: " + jasperPrintBytes.length + " bytes");

                try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(jasperPrintBytes);
                     java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais)) {

                    Object jasperPrint = ois.readObject();
                    System.out.println("DEBUG: JasperPrint deserializado - tipo: " + jasperPrint.getClass().getName());

                    Class<?> jasperExportManagerClass = Class.forName("net.sf.jasperreports.engine.JasperExportManager");
                    java.lang.reflect.Method exportToPdfMethod = jasperExportManagerClass.getMethod("exportReportToPdf", jasperPrint.getClass());
                    byte[] pdfBytes = (byte[]) exportToPdfMethod.invoke(null, jasperPrint);

                    if (pdfBytes != null && pdfBytes.length > 0) {
                        System.out.println("DEBUG: PDF gerado via JasperExportManager - tamanho: " + pdfBytes.length + " bytes");
                        return pdfBytes;
                    }
                }
            }
            System.out.println("Primeira tentativa: asBlob");
        } catch (Exception e) {
            System.out.println("DEBUG: Falha no padrao asBlob->JasperPrint->PDF: " + e.getMessage());
        }

        try {
            java.lang.reflect.Method getPropertyMethod = dynamicVO.getClass().getMethod("getProperty", String.class);
            Object conteudo = getPropertyMethod.invoke(dynamicVO, "CONTEUDO");

            if (conteudo instanceof byte[]) {
                byte[] jasperPrintBytes = (byte[]) conteudo;
                System.out.println("DEBUG: CONTEUDO obtido via getProperty - tamanho: " + jasperPrintBytes.length + " bytes");

                try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(jasperPrintBytes);
                     java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais)) {

                    Object jasperPrint = ois.readObject();

                    Class<?> jasperExportManagerClass = Class.forName("net.sf.jasperreports.engine.JasperExportManager");
                    java.lang.reflect.Method exportToPdfMethod = jasperExportManagerClass.getMethod("exportReportToPdf", jasperPrint.getClass());
                    byte[] pdfBytes = (byte[]) exportToPdfMethod.invoke(null, jasperPrint);

                    if (pdfBytes != null && pdfBytes.length > 0) {
                        System.out.println("DEBUG: PDF gerado via getProperty->JasperPrint->PDF - tamanho: " + pdfBytes.length + " bytes");
                        return pdfBytes;
                    }
                }
            }
            System.out.println("Segunda tentativa: getProperty");
        } catch (Exception e) {
            System.out.println("DEBUG: Falha no padrao getProperty->JasperPrint->PDF: " + e.getMessage());
        }

        return null;
    }

    private static String gerarNomeArquivoZip(List<BigDecimal> notas) {
        return "DANFEs_" + LocalDateTime.now().format(FORMATO_DATA) + ".zip";
    }

    public enum TipoNota {
        NFE("NFe - Nota Fiscal Eletrnica"),
        NFSE("NFS-e - Nota Fiscal de Servio Eletrnica"),
        DESCONHECIDO("Tipo no identificado");

        private final String descricao;

        TipoNota(String descricao) {
            this.descricao = descricao;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    private static TipoNota identificarTipoNota(BigDecimal nunota) throws Exception {
        JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);

        try {
            jdbc.openSession();

            sql.appendSql("SELECT C.STATUSNFSE, C.NUMNFSE, ");
            sql.appendSql("(SELECT COUNT(*) FROM TGFNFSE WHERE NUNOTA = C.NUNOTA) AS TEM_NFSE ");
            sql.appendSql("FROM TGFCAB C ");
            sql.appendSql("WHERE C.NUNOTA = " + nunota);

            try (ResultSet rs = sql.executeQuery()) {
                if (rs.next()) {
                    String statusNfse = rs.getString("STATUSNFSE");
                    String numNfse = rs.getString("NUMNFSE");
                    int temNfse = rs.getInt("TEM_NFSE");

                    System.out.println("DEBUG: STATUSNFSE = " + statusNfse + ", NUMNFSE = " + numNfse + ", TEM_NFSE = " + temNfse);

                    if (temNfse > 0) {
                        return TipoNota.NFSE;
                    }

                    if (numNfse != null && !numNfse.trim().isEmpty()) {
                        return TipoNota.NFSE;
                    }

                    if (statusNfse != null && !statusNfse.trim().isEmpty()) {
                        return TipoNota.NFSE;
                    }

                }

                return TipoNota.NFE;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Erro ao identificar tipo de nota: " + e.getMessage());
            return TipoNota.DESCONHECIDO;
        } finally {
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private static String obterClasseImpressao(TipoNota tipoNota) {
        switch (tipoNota) {
            case NFSE:

                return "br.com.sankhya.modelcore.comercial.ImpressaoNotaHelpper";
            case NFE:
            case DESCONHECIDO:
            default:
                return "br.com.sankhya.modelcore.comercial.ImpressaoNotaHelpper";
        }
    }

    public static byte[] gerarPdfDanfeNFSeAlternativo(BigDecimal nunota) throws Exception {
        System.out.println("DEBUG: Tentando gerar PDF DANFE NFS-e usando NFSeHelpper para nota: " + nunota);

        SessionHandle sessionHandle = JapeSession.open();
        try {

            Class<?> nfseHelperClass = Class.forName("br.com.sankhya.modelcore.comercial.nfse.NFSeHelpper");
            Object nfseHelper = nfseHelperClass.newInstance();

            java.util.Collection<BigDecimal> notas = new java.util.ArrayList<>();
            notas.add(nunota);

            java.lang.reflect.Method imprimeNFSeMethod = nfseHelperClass.getMethod("imprimeNFSe", Collection.class);
            Object impressaoNotaHelper = imprimeNFSeMethod.invoke(nfseHelper, notas);

            if (impressaoNotaHelper != null) {
                Class<?> impressaoNotaHelperClass = impressaoNotaHelper.getClass();

                java.lang.reflect.Method gerarJasperPrintAnexoMethod = impressaoNotaHelperClass.getMethod("gerarJasperPrintAnexo");
                Object jasperPrintCollection = gerarJasperPrintAnexoMethod.invoke(impressaoNotaHelper);

                if (jasperPrintCollection instanceof Collection) {
                    Collection<?> collection = (Collection<?>) jasperPrintCollection;
                    if (!collection.isEmpty()) {
                        Object firstItem = collection.iterator().next();
                        return extrairPdfDeDynamicVO(firstItem, TipoNota.NFSE);
                    }
                }
            }

            System.out.println("DEBUG: NFSeHelpper no retornou dados vlidos");
            return null;

        } catch (ClassNotFoundException e) {
            System.out.println("DEBUG: NFSeHelpper no encontrado");
            return null;
        } catch (Exception e) {
            System.out.println("DEBUG: Erro ao usar NFSeHelpper: " + e.getMessage());
            return null;
        } finally {
            JapeSession.close(sessionHandle);
        }
    }
}
