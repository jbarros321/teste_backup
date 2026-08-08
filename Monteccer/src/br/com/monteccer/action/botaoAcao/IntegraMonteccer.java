package br.com.monteccer.action.botaoAcao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.monteccer.helper.DanfeHelper;
import br.com.monteccer.helper.DanfeHelper.ResultadoDanfe;
import br.com.monteccer.util.DownloadHelper;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class IntegraMonteccer implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        System.out.println("=== INICIO INTEGRA MONTECCER ===");

        Optional<Map<String, Object>> parametrosOpt = obterParametros(ctx);
        if (!parametrosOpt.isPresent()) {
            String erro = "ERRO: Parametros invalidos ou ausentes. Informe: TIPO, DATA_INICIAL, DATA_FINAL, TIPO_MOVIMENTO";
            System.out.println(erro);
            ctx.setMensagemRetorno(erro);
            return;
        }

        Map<String, Object> parametros = parametrosOpt.get();
        System.out.println("DEBUG: Parametros validados - TIPO: " + parametros.get("TIPO") +
                          ", DATA_INICIAL: " + parametros.get("DATA_INICIAL") +
                          ", DATA_FINAL: " + parametros.get("DATA_FINAL") +
                          ", TIPO_MOVIMENTO: " + parametros.get("TIPO_MOVIMENTO"));

        List<BigDecimal> notas = consultarNotasPorParametros(parametros);
        System.out.println("DEBUG: Consulta executada - " + notas.size() + " notas encontradas");

        if (notas.isEmpty()) {
            String mensagem = "Nenhuma nota encontrada para os parametros informados";
            System.out.println(mensagem);
            ctx.setMensagemRetorno(mensagem);
            return;
        }

        List<BigDecimal> notasValidas = notas.stream()
            .filter(n -> n != null && n.compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());

        if (notasValidas.isEmpty()) {
            String erro = "ERRO: Todas as notas encontradas sao invalidas (null ou zero)";
            System.out.println(erro);
            ctx.setMensagemRetorno(erro);
            return;
        }

        System.out.println("DEBUG: " + notasValidas.size() + " notas validas para processamento");

        try {
            ResultadoDanfe resultado = DanfeHelper.gerarDanfeZip(notasValidas);
            System.out.println("DEBUG: Processamento DANFE - Sucesso: " + resultado.isSucesso() +
                              ", Arquivo: " + (resultado.getArquivoZip() != null ? resultado.getArquivoZip().length + " bytes" : "null"));

            if (resultado.isSucesso() && resultado.getArquivoZip() != null && resultado.getArquivoZip().length > 0) {
                salvarArquivoEGerarChave(resultado.getArquivoZip(), resultado.getNomeArquivo());
                String mensagem = String.format("%s\nDownload iniciado automaticamente!\nArquivo: %s\nTamanho: %d KB",
                    resultado.getMensagem(), resultado.getNomeArquivo(), resultado.getArquivoZip().length / 1024);
                String script = DownloadHelper.gerarScriptDownloadZip(resultado.getNomeArquivo());
                System.out.println("DEBUG: Download configurado para arquivo: " + resultado.getNomeArquivo());
                ctx.setMensagemRetorno(mensagem + "\n" + script);
            } else {
                String erro = "ERRO: Falha no processamento - " + resultado.getMensagem();
                System.out.println(erro);
                ctx.setMensagemRetorno(erro);
            }
        } catch (IllegalArgumentException e) {
            String erro = "ERRO: " + e.getMessage();
            System.out.println(erro);
            ctx.setMensagemRetorno(erro);
        } catch (Exception e) {
            String erro = "ERRO: Falha ao processar DANFEs: " + e.getMessage();
            System.out.println(erro);
            e.printStackTrace();
            ctx.setMensagemRetorno(erro);
        }

        System.out.println("=== FIM INTEGRA MONTECCER ===");
    }

    private Optional<Map<String, Object>> obterParametros(ContextoAcao ctx) {
        try {
            System.out.println("DEBUG: Iniciando validacao de parametros");

            Object tiObj = ctx.getParam("TIPO");
            Object tmObj = ctx.getParam("TIPO_MOVIMENTO");
            Object diObj = ctx.getParam("DATA_INICIAL");
            Object dfObj = ctx.getParam("DATA_FINAL");
            Object emObj = ctx.getParam("EMPRESA");
            Object paObj = ctx.getParam("PARCEIRO");

            System.out.println("DEBUG: Parametros brutos - TIPO: " + tiObj +
                              ", DATA_INICIAL: " + diObj + ", DATA_FINAL: " + dfObj +
                              ", TIPO_MOVIMENTO: " + tmObj + ", EMPRESA: " + emObj + ", PARCEIRO: " + paObj);

            if (tiObj == null || diObj == null || dfObj == null || tmObj == null) {
                System.out.println("ERRO: Parametros obrigatorios nao informados (TIPO, DATA_INICIAL, DATA_FINAL, TIPO_MOVIMENTO)");
                return Optional.empty();
            }

            String tm = converterTipoMovimento(tmObj);
            if (tm == null || tm.trim().isEmpty()) {
                System.out.println("ERRO: TIPO_MOVIMENTO invalido: " + tmObj);
                return Optional.empty();
            }

            if (!tm.matches("[VDETA]")) {
                System.out.println("ERRO: TIPO_MOVIMENTO deve ser V (Venda), D (Devolucao), E (Devolucao de compra), T (Transferencia) ou A (Todos). Recebido: " + tm);
                return Optional.empty();
            }

            Date di, df;
            try {
                di = (Date) diObj;
                df = (Date) dfObj;
            } catch (ClassCastException e) {
                System.out.println("ERRO: DATA_INICIAL ou DATA_FINAL devem ser do tipo Date. " + e.getMessage());
                return Optional.empty();
            }

            if (di.after(df)) {
                System.out.println("ERRO: DATA_INICIAL nao pode ser posterior a DATA_FINAL");
                return Optional.empty();
            }

            Integer empresa = Optional.ofNullable(emObj)
                .map(this::parseInteger)
                .filter(e -> e > 0)
                .orElse(null);

            if (emObj != null && empresa == null) {
                System.out.println("ERRO: EMPRESA deve ser um numero inteiro positivo. Recebido: " + emObj);
                return Optional.empty();
            }

            Integer parceiro = Optional.ofNullable(paObj)
                .map(this::parseInteger)
                .filter(p -> p > 0)
                .orElse(null);

            if (paObj != null && parceiro == null) {
                System.out.println("ERRO: PARCEIRO deve ser um numero inteiro positivo. Recebido: " + paObj);
                return Optional.empty();
            }

            String ti = tiObj.toString().trim();
            if (ti.isEmpty()) {
                System.out.println("ERRO: TIPO nao pode ser vazio");
                return Optional.empty();
            }

            if (!ti.matches("[013]")) {
                System.out.println("ERRO: TIPO deve ser 0 (NF-e), 1 (NFS-e) ou 3 (Ambos). Recebido: " + ti);
                return Optional.empty();
            }

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("TIPO", ti);
            parametros.put("TIPO_MOVIMENTO", tm.trim());
            parametros.put("DATA_INICIAL", di);
            parametros.put("DATA_FINAL", df);
            Optional.ofNullable(empresa).ifPresent(e -> parametros.put("EMPRESA", e));
            Optional.ofNullable(parceiro).ifPresent(p -> parametros.put("PARCEIRO", p));

            System.out.println("DEBUG: Parametros validados com sucesso");
            return Optional.of(parametros);

        } catch (Exception e) {
            System.err.println("ERRO: Falha na validacao de parametros: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private Integer parseInteger(Object obj) {
        try {
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            }
            return Integer.parseInt(obj.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<BigDecimal> consultarNotasPorParametros(Map<String, Object> parametros) throws Exception {
        System.out.println("DEBUG: Iniciando consulta SQL para buscar notas");

        JdbcWrapper jdbc = null;
        NativeSql sql = null;

        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            if (jdbc == null) {
                throw new Exception("ERRO: Nao foi possivel obter JdbcWrapper");
            }

            jdbc.openSession();
            sql = new NativeSql(jdbc);

            String query = construirQuery(parametros);
            sql.appendSql(query);
            configurarParametros(sql, parametros);

            System.out.println("DEBUG: SQL montado - TIPO: " + parametros.get("TIPO") +
                              ", DATA_INICIAL: " + parametros.get("DATA_INICIAL") +
                              ", DATA_FINAL: " + parametros.get("DATA_FINAL") +
                              ", TIPO_MOVIMENTO: " + parametros.get("TIPO_MOVIMENTO"));

            try (ResultSet rs = sql.executeQuery()) {
                List<BigDecimal> notas = rsToStream(rs).collect(Collectors.toList());
                System.out.println("DEBUG: Consulta executada - " + notas.size() + " notas retornadas");

                List<BigDecimal> notasValidas = notas.stream()
                    .filter(nunota -> nunota != null && nunota.compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());

                if (notas.size() != notasValidas.size()) {
                    System.out.println("AVISO: " + (notas.size() - notasValidas.size()) + " notas invalidas filtradas");
                }

                return notasValidas;
            }
        } catch (Exception e) {
            System.err.println("ERRO na consulta SQL: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (sql != null) NativeSql.releaseResources(sql);
                if (jdbc != null) JdbcWrapper.closeSession(jdbc);
            } catch (Exception e) {
                System.err.println("AVISO: Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    private String construirQuery(Map<String, Object> parametros) {
        String tipo = (String) parametros.get("TIPO");
        StringBuilder query = new StringBuilder("SELECT DISTINCT CAB.NUNOTA FROM TGFCAB CAB ");

        switch (tipo) {
            case "0":
                query.append("INNER JOIN TGFNFE NFE ON NFE.NUNOTA = CAB.NUNOTA ");
                query.append("WHERE CAB.DTNEG BETWEEN :DATA_INICIAL AND :DATA_FINAL ");
                query.append("AND CAB.NUNOTA IS NOT NULL AND CAB.NUNOTA > 0 AND NFE.NUNOTA IS NOT NULL ");
                break;
            case "1":
                query.append("INNER JOIN TGFNFSE NFSE ON NFSE.NUNOTA = CAB.NUNOTA ");
                query.append("WHERE CAB.DTNEG BETWEEN :DATA_INICIAL AND :DATA_FINAL ");
                query.append("AND CAB.NUNOTA IS NOT NULL AND CAB.NUNOTA > 0 AND NFSE.NUNOTA IS NOT NULL ");
                break;
            case "3":
                query.append("LEFT JOIN TGFNFE NFE ON NFE.NUNOTA = CAB.NUNOTA ");
                query.append("LEFT JOIN TGFNFSE NFSE ON NFSE.NUNOTA = CAB.NUNOTA ");
                query.append("WHERE CAB.DTNEG BETWEEN :DATA_INICIAL AND :DATA_FINAL ");
                query.append("AND CAB.NUNOTA IS NOT NULL AND CAB.NUNOTA > 0 ");
                query.append("AND (NFE.NUNOTA IS NOT NULL OR NFSE.NUNOTA IS NOT NULL) ");
                break;
            default:
                throw new IllegalArgumentException("TIPO invalido: " + tipo + " (deve ser 0=NF-e, 1=NFS-e, 3=Ambos)");
        }

        String tipoMovimento = (String) parametros.get("TIPO_MOVIMENTO");
        if (!"A".equals(tipoMovimento)) {
            query.append("AND CAB.TIPMOV = :TIPO_MOVIMENTO ");
        }

        if (parametros.containsKey("EMPRESA")) {
            query.append("AND CAB.CODEMP = :EMPRESA ");
        }

        if (parametros.containsKey("PARCEIRO")) {
            query.append("AND CAB.CODPARC = :PARCEIRO ");
        }

        query.append("ORDER BY CAB.NUNOTA");
        return query.toString();
    }

    private void configurarParametros(NativeSql sql, Map<String, Object> parametros) {
        sql.setNamedParameter("DATA_INICIAL", parametros.get("DATA_INICIAL"));
        sql.setNamedParameter("DATA_FINAL", parametros.get("DATA_FINAL"));

        String tipoMovimento = (String) parametros.get("TIPO_MOVIMENTO");
        if (!"A".equals(tipoMovimento)) {
            sql.setNamedParameter("TIPO_MOVIMENTO", tipoMovimento);
        }

        Optional.ofNullable(parametros.get("EMPRESA")).ifPresent(e -> sql.setNamedParameter("EMPRESA", e));
        Optional.ofNullable(parametros.get("PARCEIRO")).ifPresent(p -> sql.setNamedParameter("PARCEIRO", p));
    }

    private Stream<BigDecimal> rsToStream(ResultSet rs) throws java.sql.SQLException {
        Stream.Builder<BigDecimal> builder = Stream.builder();
        int contador = 0;

        while (rs.next()) {
            contador++;
            BigDecimal nunota = rs.getBigDecimal("NUNOTA");

            if (nunota == null) {
                System.out.println("AVISO: NUNOTA null encontrada na linha " + contador);
                continue;
            }

            if (nunota.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("AVISO: NUNOTA invalida (<=0): " + nunota + " na linha " + contador);
                continue;
            }

            builder.add(nunota);
        }

        System.out.println("DEBUG: ResultSet processado - " + contador + " linhas lidas");
        return builder.build();
    }

    private String converterTipoMovimento(Object obj) {
        if (obj == null) {
            System.out.println("DEBUG: TIPO_MOVIMENTO null recebido");
            return null;
        }

        String s = obj.toString().trim();
        System.out.println("DEBUG: TIPO_MOVIMENTO original: '" + s + "'");

        if (s.isEmpty()) {
            System.out.println("DEBUG: TIPO_MOVIMENTO vazio");
            return null;
        }

        if (s.matches("\\d+")) {
            try {
                int numero = Integer.parseInt(s);
                if (numero >= 1 && numero <= 5) {
                    String[] tipos = {"V", "D", "E", "T", "A"};
                    String resultado = tipos[numero - 1];
                    System.out.println("DEBUG: TIPO_MOVIMENTO convertido de " + numero + " para " + resultado);
                    return resultado;
                } else {
                    System.out.println("DEBUG: TIPO_MOVIMENTO numerico invalido: " + numero + " (deve ser 1, 2, 3, 4 ou 5)");
                    return null;
                }
            } catch (NumberFormatException e) {
                System.out.println("DEBUG: Erro ao converter TIPO_MOVIMENTO numerico: " + e.getMessage());
                return null;
            }
        }

        if (s.matches("[VDETA]")) {
            System.out.println("DEBUG: TIPO_MOVIMENTO alfanumerico valido: " + s);
            return s;
        }

        System.out.println("DEBUG: TIPO_MOVIMENTO invalido: " + s + " (deve ser V, D, E, T, A ou 1, 2, 3, 4, 5)");
        return null;
    }

    private void salvarArquivoEGerarChave(byte[] bytes, String nome) {
        if (bytes == null || bytes.length == 0) {
            System.err.println("ERRO: Arquivo ZIP vazio ou null - nao foi possivel salvar");
            return;
        }

        if (nome == null || nome.trim().isEmpty()) {
            System.err.println("ERRO: Nome do arquivo invalido - nao foi possivel salvar");
            return;
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir == null || tmpDir.trim().isEmpty()) {
            System.err.println("ERRO: Diretorio temporario nao configurado");
            return;
        }

        String caminhoCompleto = tmpDir + java.io.File.separator + nome.trim();
        System.out.println("DEBUG: Salvando arquivo ZIP - Caminho: " + caminhoCompleto + ", Tamanho: " + bytes.length + " bytes");

        try (FileOutputStream fos = new FileOutputStream(caminhoCompleto)) {
            fos.write(bytes);
            fos.flush();
            System.out.println("DEBUG: Arquivo ZIP salvo com sucesso");
        } catch (IOException e) {
            System.err.println("ERRO: Falha ao salvar arquivo ZIP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
