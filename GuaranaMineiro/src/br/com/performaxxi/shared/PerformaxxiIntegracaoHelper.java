package br.com.performaxxi.shared;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sankhya.util.SQLUtils;
import com.sankhya.util.StringUtils;

import br.com.sankhya.commons.controller.util.DateUtil;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class PerformaxxiIntegracaoHelper {

    private static final String SQL_FILE = "sql/query.sql";

    private static final Set<String> CAMPOS_CONTEXTO = new HashSet<>(Arrays.asList(
        "PERIODO_INI", "PERIODO_FIN", "P_CODEMP", "P_CODVEND",
        "P_CODPARC", "P_TIPO", "P_ORDEMCARGA", "P_PENDENTE", "P_CODVEICULO", "DEBUG", "INCLUIR_ENVIADOS"
    ));

    private static final Map<String, String> MAPEAMENTO_CAMPOS = Stream.of(
        new String[][]{
 {"TIPO", "tipo"}, {"ID_PEDIDO", "nNota"}, {"N_NOTA", "numNota"},
 {"COD_VENDEDOR", "codVendedor"}, {"APELIDO", "apelido"}, {"ID_CLIENTE", "idCliente"},
 {"CLIENTE", "cliente"}, {"ENDERECO", "endereco"}, {"CIDADE", "cidade"},
 {"UF", "uf"}, {"CEP", "cep"}, {"PESO", "peso"}, {"VOLUME", "volume"},
 {"VLRNOTA", "vlrNota"}, {"TELEFONE", "telefone"}, {"EMAIL", "email"},
 {"BAIRRO", "bairro"}, {"JANELA_INICIO", "janelaInicio"},
 {"JANELA_FIM", "janelaFim"}, {"TIPO_DO_CLIENTE", "tipoCliente"},
 {"INSTRUCOES_ENTREGA", "instrucoesEntrega"}, {"Nome_do_Contato", "nomeContato"},
 {"ID_Transportadora", "idTransportadora"}, {"Nome_Transportadora", "nomeTransportadora"},
 {"CPF_OU_CNPJ", "cpfCnpj"}, {"Quantidade_de_Itens", "quantidadeItens"},
 {"Bandeira", "bandeira"}, {"Comentarios", "comentarios"}, {"CODIGO_REMESSA", "codigoRemessa"},
 {"DATA_LIMITE_ATENDIMENTO", "dataLimiteAtendimento"}
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static final String QUERY_SQL = loadQueryFile(SQL_FILE);

    public static class ResultadoIntegracao {
        private final int sucessos;
        private final int erros;
        private final PerformaxxiAPI.RespostaAPI respostaAPI;

        public ResultadoIntegracao(int sucessos, int erros) {
            this.sucessos = sucessos;
            this.erros = erros;
            this.respostaAPI = null;
        }

        public ResultadoIntegracao(int sucessos, int erros, PerformaxxiAPI.RespostaAPI respostaAPI) {
            this.sucessos = sucessos;
            this.erros = erros;
            this.respostaAPI = respostaAPI;
        }

        public int getSucessos() { return sucessos; }
        public int getErros() { return erros; }
        public PerformaxxiAPI.RespostaAPI getRespostaAPI() { return respostaAPI; }
    }

    public static class PedidoData {
        public String tipo, nNota, numNota, codVendedor, apelido, idCliente, cliente,
               endereco, cidade, uf, cep, peso, volume, vlrNota, telefone, email,
               bairro, tempoAtendimento, prioridadeCliente, janelaInicio, janelaFim,
               janelaInicio2, janelaFim2, territorios, tipoCliente, instrucoesEntrega,
               nomeContato, idTransportadora, nomeTransportadora, quantidadeItens, bandeira,
               cpfCnpj, comentarios, codigoRemessa, dataLimiteAtendimento, dataPedido;
        public Timestamp data;

        public Integer valorResposta;
        public Boolean sucessoResposta;
    }

    public static Map<String, Object> extrairParametros(ContextoAcao ctx) {
        Map<String, Object> params = new HashMap<>();

        CAMPOS_CONTEXTO.forEach(campo -> {
            Object valor = processarValorParametro(campo, extrairValorCampo(ctx, campo));
            if (valor != null) {
                params.put(campo, valor);
            }
        });

        return params;
    }

    public static List<PedidoData> executarQuery(Map<String, Object> params) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rs = null;

        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.setReuseStatements(true);

            String sqlContent = construirQueryDinamica(QUERY_SQL, params);
            sql.getSqlBuf().setLength(0);
            sql.getSqlBuf().append(sqlContent);

            definirParametrosSQL(sql, params);
            rs = sql.executeQuery();

            return processarResultados(rs, params);

        } finally {
            if (rs != null) rs.close();
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }
    }

    public static List<PedidoData> filtrarPedidosNaoEnviados(List<PedidoData> pedidos, String tabelaLog) throws Exception {
        if (pedidos == null || pedidos.isEmpty()) {
            return pedidos;
        }

        List<String> numerosPedidos = pedidos.stream()
            .map(p -> p.numNota)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (numerosPedidos.isEmpty()) return pedidos;

        try {
            JdbcWrapper jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();

            String inClause = SQLUtils.buildINClauseByValues("NUMERO_PEDIDO", numerosPedidos);
            String sql = "SELECT DISTINCT NUMERO_PEDIDO FROM " + tabelaLog +
                        " WHERE STATUS = 'SUCESSO' AND TIPO_REGISTRO = 'PEDIDO' AND " + inClause;

            NativeSql nativeSql = new NativeSql(jdbc);
            nativeSql.getSqlBuf().setLength(0);
            nativeSql.getSqlBuf().append(sql);

            ResultSet rs = nativeSql.executeQuery();
            Set<String> pedidosEnviados = new HashSet<>();

            while (rs.next()) {
                pedidosEnviados.add(rs.getString("NUMERO_PEDIDO"));
            }
            rs.close();

            return pedidos.stream()
                .filter(p -> p.numNota != null && !pedidosEnviados.contains(p.numNota))
                .collect(Collectors.toList());

        } catch (Exception e) {
            return pedidos;
        }
    }

    public static List<PerformaxxiAPI.PedidoPerformaxxi> converterParaPedidosAPI(List<PedidoData> pedidos) {
        return pedidos.stream()
            .map(PerformaxxiIntegracaoHelper::converterParaPedidoPerformaxxi)
            .collect(Collectors.toList());
    }

    public static List<PerformaxxiAPI.PedidoPerformaxxi> converterParaPedidosAPISimples(List<PedidoData> pedidos) {
        return pedidos.stream()
            .map(PerformaxxiIntegracaoHelper::converterParaPedidoPerformaxxi)
            .collect(Collectors.toList());
    }

    public static void registrarPedidosEnviados(List<PedidoData> pedidos, String numeroLote,
            String status, String mensagemRetorno, String tabelaLog) throws Exception {

        if (pedidos == null || pedidos.isEmpty()) return;

        System.out.println("[PERFORMAXXI] Registrando " + pedidos.size() + " pedidos com status: " + status);
        System.out.println("[PERFORMAXXI] Lote: " + numeroLote);
        System.out.println("[PERFORMAXXI] Mensagem: " + mensagemRetorno);

        try {
            for (PedidoData pedido : pedidos) {
                if (pedido.numNota != null) {
                    JapeFactory.dao(tabelaLog).create()
                        .set("NUMERO_LOTE", numeroLote)
                        .set("STATUS", status)
                        .set("MENSAGEM", ("Pedido " + pedido.numNota + " processado").toCharArray())
                        .set("TIPO_REGISTRO", "PEDIDO")
                        .set("NUMERO_PEDIDO", pedido.numNota)
                        .set("ID_PEDIDO", pedido.nNota != null ? new java.math.BigDecimal(pedido.nNota) : null)
                        .set("MENSAGEM_RETORNO", mensagemRetorno != null ? mensagemRetorno.toCharArray() : null)
                        .set("USUARIO", "SISTEMA")
                        .save();

                    System.out.println("[PERFORMAXXI] Pedido " + pedido.numNota + " registrado com status " + status);
                }
            }
        } catch (Exception e) {
            PerformaxxiAPI.logError("ERRO ao registrar pedidos na tabela de log: " + e.getMessage(), e);
            throw e;
        }
    }

    public static void registrarExecucao(String numeroLote, String status, String mensagem,
            int totalPedidos, int sucessos, int erros, long tempoExecucao, String parametros, String tabelaLog) throws Exception {
        try {
            JapeFactory.dao(tabelaLog).create()
                .set("NUMERO_LOTE", numeroLote)
                .set("STATUS", status)
                .set("MENSAGEM", mensagem.toCharArray())
                .set("TIPO_REGISTRO", "EXECUCAO")
                .set("TOTAL_PEDIDOS", new java.math.BigDecimal(totalPedidos))
                .set("SUCESSOS", new java.math.BigDecimal(sucessos))
                .set("ERROS", new java.math.BigDecimal(erros))
                .set("TEMPO_EXECUCAO", new java.math.BigDecimal((int) tempoExecucao))
                .set("PARAMETROS", parametros != null ? parametros.toCharArray() : null)
                .set("USUARIO", "SISTEMA")
                .set("DATA_INICIO", new java.sql.Timestamp(System.currentTimeMillis()))
                .set("DATA_FIM", new java.sql.Timestamp(System.currentTimeMillis()))
                .save();

            System.out.println("[PERFORMAXXI] Execucao registrada - Lote: " + numeroLote + " | Status: " + status + " | Sucessos: " + sucessos + " | Erros: " + erros);
        } catch (Exception e) {
            PerformaxxiAPI.logError("ERRO ao registrar execucao: " + e.getMessage(), e);
            throw e;
        }
    }

    public static String criarListaRegistrosEnviados(List<PedidoData> pedidos) {
        if (pedidos == null || pedidos.isEmpty()) {
            return "<p>Nenhum registro enviado.</p>";
        }

        StringBuilder html = new StringBuilder(1024);

        final String TABLE_STYLE = "width: 100%; border-collapse: collapse; font-size: 13px;";
        final String HEADER_STYLE = "background-color: #e8f5e8;";
        final String CELL_STYLE = "border: 1px solid #ddd; padding: 10px; text-align: left;";
        final String DATA_CELL_STYLE = "border: 1px solid #ddd; padding: 8px;";
        final String BOLD_CELL_STYLE = DATA_CELL_STYLE + " font-weight: bold;";

        html.append(StringUtils.joinElements(
            "<table style='", TABLE_STYLE, "'>",
            "<thead>",
            "<tr style='", HEADER_STYLE, "'>",
            "<th style='", CELL_STYLE, "'>ID_PEDIDO</th>",
            "<th style='", CELL_STYLE, "'>Cliente</th>",
            "<th style='", CELL_STYLE, "'>Valor</th>",
            "<th style='", CELL_STYLE, "'>Sucesso</th>",
            "</tr>",
            "</thead>"
        ));
        html.append("<tbody>");

        pedidos.forEach(pedido -> {
            html.append(StringUtils.joinElements(
                "<tr>",
                "<td style='", BOLD_CELL_STYLE, "'>",
                Optional.ofNullable(pedido.nNota).orElse("-"),
                "</td>",
                "<td style='", DATA_CELL_STYLE, "'>",
                Optional.ofNullable(pedido.cliente).orElse("-"),
                "</td>",
                "<td style='", DATA_CELL_STYLE, "'>",
                Optional.ofNullable(pedido.valorResposta).map(String::valueOf).orElse("-"),
                "</td>",
                "<td style='", DATA_CELL_STYLE, "'>",
                Optional.ofNullable(pedido.sucessoResposta).map(s -> s ? "Sim" : "Não").orElse("-"),
                "</td>",
                "</tr>"
            ));
        });

        html.append(StringUtils.joinElements("</tbody>", "</table>"));

        return html.toString();
    }

    private static String loadQueryFile(String fileName) {
        try (InputStream in = PerformaxxiIntegracaoHelper.class.getClassLoader().getResourceAsStream(fileName)) {
            if (in == null) {
                throw new RuntimeException("Arquivo nao encontrado: " + fileName);
            }

            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar arquivo SQL: " + fileName, e);
        }
    }

    private static Object extrairValorCampo(ContextoAcao ctx, String campo) {
        try {
            if (ctx.getLinhas() == null || ctx.getLinhas().length == 0) {
                return null;
            }
            return ctx.getLinhas()[0].getCampo(campo);
        } catch (Exception e) {
            return null;
        }
    }

    private static Object processarValorParametro(String campo, Object valor) {
        return Stream.of("P_CODEMP", "P_CODVEND").anyMatch(campo::equals) ? processarListaParametro(valor, Integer.class)
            : Stream.of("PERIODO_INI", "PERIODO_FIN").anyMatch(campo::equals) ? processarDataParametro(valor)
            : processarCampoSimples(valor);
    }

    private static <T> List<T> processarListaParametro(Object valor, Class<T> tipo) {
        return Optional.ofNullable(valor)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .map(token -> converterTokenParaTipo(token, tipo))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()))
            .filter(list -> !list.isEmpty())
            .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private static <T> T converterTokenParaTipo(String token, Class<T> tipo) {
        switch (tipo.getSimpleName()) {
            case "Integer": return (T) Integer.valueOf(token);
            case "Long": return (T) Long.valueOf(token);
            case "Double": return (T) Double.valueOf(token);
            default: return (T) token;
        }
    }

    private static Object processarCampoSimples(Object valor) {
        return Optional.ofNullable(valor)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> (Object) s)
            .orElse(valor);
    }

    private static Object processarDataParametro(Object valor) {
        return Optional.ofNullable(valor)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(str -> {
                try {
                    return (Object) DateUtil.stringToTimestamp(str);
                } catch (Exception e) {
                    return (Object) str;
                }
            })
            .orElse(valor);
    }

    private static String construirQueryDinamica(String sqlContent, Map<String, Object> params) {
        return Stream.of(new String[][]{{"P_CODEMP", "CAB.CODEMP"}, {"P_CODVEND", "CAB.CODVEND"}})
            .reduce(sqlContent, (result, campoTabela) -> {
                Object valor = params.get(campoTabela[0]);
                if (valor != null) {
                    String substituicao = construirClausulaIN(campoTabela[1], valor);
                    String placeholder = String.format("(%s IN :%s OR :%s IS NULL)", campoTabela[1], campoTabela[0], campoTabela[0]);
                    return result.replace(placeholder, substituicao);
                }
                return result;
            }, (a, b) -> b);
    }

    private static String construirClausulaIN(String campo, Object valor) {
        if (valor instanceof List && !((List<?>) valor).isEmpty()) {
            return SQLUtils.buildINClauseByValues(campo, (List<?>) valor);
        } else if (valor != null) {
            return campo + " = " + valor;
        } else {
            return "1=1";
        }
    }

    private static void definirParametrosSQL(NativeSql sql, Map<String, Object> params) {
        Set<String> parametrosSubstituidos = new HashSet<>(Arrays.asList("P_CODEMP", "P_CODVEND"));
        params.entrySet().stream()
            .filter(entry -> !parametrosSubstituidos.contains(entry.getKey()))
            .forEach(entry -> {
                try {
                    sql.setNamedParameter(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    PerformaxxiAPI.logError("ERRO ao definir parametro '" + entry.getKey() + "': " + e.getMessage(), e);
                }
            });
    }

    private static List<PedidoData> processarResultados(ResultSet rs, Map<String, Object> params) throws Exception {
        List<PedidoData> pedidos = new ArrayList<>();
        int rowCount = 0;

        while (rs.next()) {
            rowCount++;
            try {
                PedidoData pedido = mapearPedido(rs, rowCount);
                pedidos.add(pedido);
            } catch (Exception e) {
                PerformaxxiAPI.logError("ERRO ao mapear linha " + rowCount + ": " + e.getMessage(), e);
                throw e;
            }
        }

        return pedidos;
    }

    private static PedidoData mapearPedido(ResultSet rs, int rowNumber) throws Exception {
        PedidoData p = new PedidoData();

        MAPEAMENTO_CAMPOS.forEach((coluna, campo) -> {
            try {
                p.getClass().getDeclaredField(campo).set(p, getStringSeguro(rs, coluna));
            } catch (Exception e) {  }
        });

        p.data = rs.getTimestamp("DATA");
        return p;
    }

    private static String getStringSeguro(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (Exception e) {
            return null;
        }
    }

    private static PerformaxxiAPI.PedidoPerformaxxi converterParaPedidoPerformaxxi(PedidoData pedido) {
        System.out.println("[PERFORMAXXI] DEBUG: Convertendo pedido " + pedido.numNota + " para API Performaxxi");
        System.out.println("[PERFORMAXXI] DEBUG: Dados do pedido - nomeContato: '" + pedido.nomeContato + "' (tamanho: " + (pedido.nomeContato != null ? pedido.nomeContato.length() : "null") + ")");

        PerformaxxiAPI.PedidoPerformaxxi pedidoAPI = new PerformaxxiAPI.PedidoPerformaxxi();

        pedidoAPI.numeroPedido = pedido.numNota;
        pedidoAPI.dataPedido = pedido.data != null ? formatarDataParaAPI(pedido.data) : "";
        pedidoAPI.pesoTotalPedido = parseNumeric(pedido.peso, Double.class, 0.0);
        pedidoAPI.volumeTotalPedido = parseNumeric(pedido.volume, Double.class, 0.0);
        pedidoAPI.valorTotalPedido = parseNumeric(pedido.vlrNota, Double.class, 0.0);
        pedidoAPI.detalhesCliente = new PerformaxxiAPI.DetalhesCliente();
        pedidoAPI.detalhesCliente.identificadorCliente = pedido.idCliente != null ? pedido.idCliente : "";
        pedidoAPI.detalhesCliente.nomeCliente = pedido.cliente != null ? pedido.cliente : "";
        pedidoAPI.detalhesCliente.endereco = pedido.endereco != null ? pedido.endereco : "";
        pedidoAPI.detalhesCliente.cidade = pedido.cidade != null ? pedido.cidade : "";
        pedidoAPI.detalhesCliente.estado = pedido.uf != null ? pedido.uf : "";
        pedidoAPI.detalhesCliente.CEP = pedido.cep != null ? pedido.cep : "";
        pedidoAPI.detalhesCliente.telefone = pedido.telefone != null ? pedido.telefone : "";
        pedidoAPI.detalhesCliente.telefone2 = "";
        pedidoAPI.detalhesCliente.telefone3 = "";
        pedidoAPI.detalhesCliente.email = pedido.email != null ? pedido.email : "";
        pedidoAPI.detalhesCliente.email2 = "";
        pedidoAPI.detalhesCliente.email3 = "";
        pedidoAPI.detalhesCliente.bairro = pedido.bairro != null ? pedido.bairro : "";
        pedidoAPI.detalhesCliente.CPFCNPJ = pedido.cpfCnpj != null ? pedido.cpfCnpj : "";
        pedidoAPI.detalhesCliente.tipoCliente = pedido.tipoCliente != null ? pedido.tipoCliente : "";
        pedidoAPI.detalhesCliente.territorio = pedido.territorios != null ? pedido.territorios : "";
        pedidoAPI.detalhesCliente.bandeira = pedido.bandeira != null ? pedido.bandeira : "";

        String prioridadeMapeada = mapearPrioridade(pedido.prioridadeCliente);
        pedidoAPI.detalhesCliente.prioridadeCliente = prioridadeMapeada;
        pedidoAPI.detalhesCliente.nomeContato = pedido.nomeContato != null ? pedido.nomeContato : "";
        pedidoAPI.comentarios = pedido.comentarios != null ? pedido.comentarios : "";
        pedidoAPI.identificadorTransportadora = pedido.idTransportadora != null ? pedido.idTransportadora : "";
        pedidoAPI.nomeTransportadora = pedido.nomeTransportadora != null ? pedido.nomeTransportadora : "";
        pedidoAPI.identificadorDeposito = "";
        pedidoAPI.nomeDeposito = "";
        pedidoAPI.instrucoesEntrega = pedido.instrucoesEntrega != null ? pedido.instrucoesEntrega : "";
        pedidoAPI.codigoRemessa = pedido.codigoRemessa != null ? pedido.codigoRemessa : "";
        pedidoAPI.dataHoraLimiteEntrega = pedido.dataLimiteAtendimento != null ? pedido.dataLimiteAtendimento : "";
        pedidoAPI.tempoAtendimentoPedido = 0;
        pedidoAPI.tipoEntrega = 0;

        pedidoAPI.codigosBarras = new java.util.ArrayList<>();

        pedidoAPI.janelaAtendimento = new PerformaxxiAPI.JanelaAtendimento("08:00", "23:00");

        if (pedido.janelaInicio != null && pedido.janelaFim != null && !pedido.janelaInicio.trim().isEmpty() && !pedido.janelaFim.trim().isEmpty()) {
            pedidoAPI.janelaAtendimento = new PerformaxxiAPI.JanelaAtendimento(pedido.janelaInicio, pedido.janelaFim);
        }
        if (pedido.janelaInicio2 != null && pedido.janelaFim2 != null && !pedido.janelaInicio2.trim().isEmpty() && !pedido.janelaFim2.trim().isEmpty()) {
            pedidoAPI.janelaAtendimento2 = new PerformaxxiAPI.JanelaAtendimento(pedido.janelaInicio2, pedido.janelaFim2);
        }

        return pedidoAPI;
    }

    private static String formatarDataParaAPI(Timestamp data) {
        if (data == null) return "";

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(data);
        } catch (Exception e) {
            PerformaxxiAPI.logError("ERRO ao formatar data: " + e.getMessage(), e);
            return "";
        }
    }

    private static <T extends Number> T parseNumeric(String s, Class<T> tipo, T defaultValue) {
        if (s == null || s.trim().isEmpty()) return defaultValue;

        try {
            String numericStr = s.trim();

            if (numericStr.contains(",") && numericStr.contains(".")) {
                String[] parts = numericStr.split(",");
                if (parts.length == 2) {
                    String integerPart = parts[0].replace(".", "");
                    String decimalPart = parts[1];
                    numericStr = integerPart + "." + decimalPart;
                }
            } else if (numericStr.contains(",") && !numericStr.contains(".")) {
                numericStr = numericStr.replace(",", ".");
            }

            switch (tipo.getSimpleName()) {
                case "Integer": return tipo.cast(Integer.parseInt(numericStr));
                case "Double": return tipo.cast(Double.parseDouble(numericStr));
                default: return defaultValue;
            }
        } catch (Exception e) {
            PerformaxxiAPI.logError("ERRO ao converter '" + s + "' para " + tipo.getSimpleName() + ": " + e.getMessage(), e);
            return defaultValue;
        }
    }

    private static String mapearPrioridade(String prioridade) {
        if (prioridade == null || prioridade.trim().isEmpty()) {
            return "N";
        }

        String prioridadeUpper = prioridade.trim().toUpperCase();

        switch (prioridadeUpper) {
            case "NORMAL":
            case "N":
            case "0":
                return "N";
            case "BAIXA":
            case "L":
            case "LOW":
            case "1":
                return "L";
            case "ALTA":
            case "H":
            case "HIGH":
            case "2":
                return "H";
            default:

                System.out.println("[PERFORMAXXI] Prioridade não reconhecida: '" + prioridade + "' - usando Normal (N)");
                return "N";
        }
    }

}
