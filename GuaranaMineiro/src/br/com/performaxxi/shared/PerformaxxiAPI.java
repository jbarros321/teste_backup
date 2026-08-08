package br.com.performaxxi.shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class PerformaxxiAPI {

    private static final Gson gson = new Gson();
    private static final String CONTENT_TYPE = "application/json; charset=UTF-8";
    private static final String USER_AGENT = "Sankhya-Integracao-Performaxxi/1.0";
    private static final SimpleDateFormat DATE_FORMAT_API = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static class Config {
        public static final String API_BASE_URL_PRODUCAO = "https:
        public static final String API_BASE_URL_HOMOLOGACAO = "https:

        public static final String AMBIENTE_ATIVO = "HOMOLOGACAO";

        public static final String API_USERNAME = "GuaranaMineiroTI";
        public static final String API_PASSWORD = "R3!Vhc@mFuYU1pbmVpcm86";

        public static final String ENDPOINT_ENVIOPEDIDOS = "/API.REST/importacao/pedidos";
        public static final String ENDPOINT_ENVIOMENSAGEM = "/API.REST/rota/enviomensagemrota";
        public static final String ENDPOINT_COMPROVANTES = "/API.REST/entrega/comprovantes/veiculo";

        public static final int TIMEOUT_CONEXAO = 30000;
        public static final int TIMEOUT_LEITURA = 60000;

        public static final int MAX_PEDIDOS_POR_LOTE = 100;
        public static final boolean LOG_DETALHADO = true;
    }

    public static class PedidoPerformaxxi {
        public String numeroPedido;
        public String dataPedido;
        public String identificadorEmbalagem;
        public String descricaoEmbalagem;
        public String comentarios;
        public String identificadorTransportadora;
        public String nomeTransportadora;
        public String identificadorDeposito;
        public String nomeDeposito;
        public String instrucoesEntrega;
        public String codigoRemessa;
        public String dataHoraLimiteEntrega;
        public int quantidadeItem;
        public int tipoEntrega;
        public int tempoAtendimentoPedido;
        public double pesoTotalPedido;
        public double volumeTotalPedido;
        public double valorTotalPedido;
        public double freteTotalPedido;
        public JanelaAtendimento janelaAtendimento;
        public JanelaAtendimento janelaAtendimento2;
        public DetalhesCliente detalhesCliente;
        public List<CodigoBarras> codigosBarras;
    }

    public static class JanelaAtendimento {
        public String inicioJanelaAtendimento;
        public String fimJanelaAtendimento;

        public JanelaAtendimento(String inicio, String fim) {
            this.inicioJanelaAtendimento = inicio;
            this.fimJanelaAtendimento = fim;
        }
    }

    public static class DetalhesCliente {
        public String identificadorCliente;
        public String nomeCliente;
        public String endereco;
        public String cidade;
        public String estado;
        public String CEP;
        public String nomeContato;
        public String tipoCliente;
        public String telefone;
        public String telefone2;
        public String telefone3;
        public String territorio;
        public String bandeira;
        public String CPFCNPJ;
        public String bairro;
        public int tempoAtendimento;
        public String email;
        public String email2;
        public String email3;
        public double latitude;
        public double longitude;
        public String prioridadeCliente;
    }

    public static class CodigoBarras {
        public String numero;
        public String setor;
        public String numeroCaixa;
        public String descricao;
    }

    public static class LotePedidos {
        public String numeroLote;
        public List<PedidoPerformaxxi> listaPedidos;

        public LotePedidos(List<PedidoPerformaxxi> pedidos, String numeroLote) {
            this.listaPedidos = pedidos;
            this.numeroLote = numeroLote;
        }
    }

    public static class RespostaAPI {
        public int Valor;
        public boolean Sucesso;
        public String MensagemErro;
        public String CodigoErro;

        public int getValor() { return Valor; }
        public boolean isSucesso() { return Sucesso; }
        public String getMensagemErro() { return MensagemErro; }
        public String getCodigoErro() { return CodigoErro; }

        public int valor;
        public boolean sucesso;
        public String mensagemErro;
        public String codigoErro;
    }

    public static RespostaAPI enviarPedidos(List<PedidoPerformaxxi> pedidos, String numeroLote) throws Exception {
        if (pedidos == null || pedidos.isEmpty()) {
            throw new IllegalArgumentException("Lista de pedidos nao pode ser nula ou vazia");
        }

        if (isNullOrEmpty(numeroLote)) {
            throw new IllegalArgumentException("Numero do lote e obrigatorio");
        }

        System.out.println("[PERFORMAXXI] Processando " + pedidos.size() + " pedidos antes do envio...");

        pedidos = corrigirEValidarPedidos(pedidos);

        System.out.println("[PERFORMAXXI] Processamento concluido. Pedidos finais: " + pedidos.size());

        validarCamposFinais(pedidos);

        LotePedidos lote = new LotePedidos(pedidos, numeroLote);
        String jsonPayload = gson.toJson(lote);

        System.out.println("[PERFORMAXXI] Enviando " + pedidos.size() + " pedidos validados para Performaxxi (lote: " + numeroLote + ")");
        System.out.println("[PERFORMAXXI] DEBUG: Payload JSON COMPLETO:");
        System.out.println("==========================================");
        System.out.println(jsonPayload);
        System.out.println("==========================================");

        for (int i = 0; i < pedidos.size(); i++) {
            PedidoPerformaxxi pedido = pedidos.get(i);
            System.out.println("[PERFORMAXXI] ===== DEBUG PEDIDO " + (i+1) + " =====");
            System.out.println("PEDIDO PRINCIPAL:");
            System.out.println("  numeroPedido: '" + pedido.numeroPedido + "' (tamanho: " + (pedido.numeroPedido != null ? pedido.numeroPedido.length() : "null") + ")");
            System.out.println("  dataPedido: '" + pedido.dataPedido + "' (tamanho: " + (pedido.dataPedido != null ? pedido.dataPedido.length() : "null") + ")");
            System.out.println("  quantidadeItem: " + pedido.quantidadeItem);
            System.out.println("  pesoTotalPedido: " + pedido.pesoTotalPedido);
            System.out.println("  volumeTotalPedido: " + pedido.volumeTotalPedido);
            System.out.println("  valorTotalPedido: " + pedido.valorTotalPedido);
            System.out.println("  tipoEntrega: " + pedido.tipoEntrega);
            System.out.println("  tempoAtendimentoPedido: " + pedido.tempoAtendimentoPedido);
            System.out.println("  comentarios: '" + pedido.comentarios + "' (tamanho: " + (pedido.comentarios != null ? pedido.comentarios.length() : "null") + ")");
            System.out.println("  identificadorTransportadora: '" + pedido.identificadorTransportadora + "' (tamanho: " + (pedido.identificadorTransportadora != null ? pedido.identificadorTransportadora.length() : "null") + ")");
            System.out.println("  nomeTransportadora: '" + pedido.nomeTransportadora + "' (tamanho: " + (pedido.nomeTransportadora != null ? pedido.nomeTransportadora.length() : "null") + ")");
            System.out.println("  identificadorDeposito: '" + pedido.identificadorDeposito + "' (tamanho: " + (pedido.identificadorDeposito != null ? pedido.identificadorDeposito.length() : "null") + ")");
            System.out.println("  nomeDeposito: '" + pedido.nomeDeposito + "' (tamanho: " + (pedido.nomeDeposito != null ? pedido.nomeDeposito.length() : "null") + ")");
            System.out.println("  instrucoesEntrega: '" + pedido.instrucoesEntrega + "' (tamanho: " + (pedido.instrucoesEntrega != null ? pedido.instrucoesEntrega.length() : "null") + ")");
            System.out.println("  codigoRemessa: '" + pedido.codigoRemessa + "' (tamanho: " + (pedido.codigoRemessa != null ? pedido.codigoRemessa.length() : "null") + ")");
            System.out.println("  dataHoraLimiteEntrega: '" + pedido.dataHoraLimiteEntrega + "' (tamanho: " + (pedido.dataHoraLimiteEntrega != null ? pedido.dataHoraLimiteEntrega.length() : "null") + ")");
            System.out.println("  freteTotalPedido: " + pedido.freteTotalPedido);

            if (pedido.janelaAtendimento != null) {
                System.out.println("  - janelaAtendimento.inicio: " + pedido.janelaAtendimento.inicioJanelaAtendimento);
                System.out.println("  - janelaAtendimento.fim: " + pedido.janelaAtendimento.fimJanelaAtendimento);
            }

            if (pedido.janelaAtendimento2 != null) {
                System.out.println("  - janelaAtendimento2.inicio: " + pedido.janelaAtendimento2.inicioJanelaAtendimento);
                System.out.println("  - janelaAtendimento2.fim: " + pedido.janelaAtendimento2.fimJanelaAtendimento);
            }

            if (pedido.detalhesCliente != null) {
                System.out.println("DETALHES DO CLIENTE:");
                System.out.println("  identificadorCliente: '" + pedido.detalhesCliente.identificadorCliente + "' (tamanho: " + (pedido.detalhesCliente.identificadorCliente != null ? pedido.detalhesCliente.identificadorCliente.length() : "null") + ")");
                System.out.println("  nomeCliente: '" + pedido.detalhesCliente.nomeCliente + "' (tamanho: " + (pedido.detalhesCliente.nomeCliente != null ? pedido.detalhesCliente.nomeCliente.length() : "null") + ")");
                System.out.println("  endereco: '" + pedido.detalhesCliente.endereco + "' (tamanho: " + (pedido.detalhesCliente.endereco != null ? pedido.detalhesCliente.endereco.length() : "null") + ")");
                System.out.println("  cidade: '" + pedido.detalhesCliente.cidade + "' (tamanho: " + (pedido.detalhesCliente.cidade != null ? pedido.detalhesCliente.cidade.length() : "null") + ")");
                System.out.println("  estado: '" + pedido.detalhesCliente.estado + "' (tamanho: " + (pedido.detalhesCliente.estado != null ? pedido.detalhesCliente.estado.length() : "null") + ")");
                System.out.println("  CEP: '" + pedido.detalhesCliente.CEP + "' (tamanho: " + (pedido.detalhesCliente.CEP != null ? pedido.detalhesCliente.CEP.length() : "null") + ")");
                System.out.println("  nomeContato: '" + pedido.detalhesCliente.nomeContato + "' (tamanho: " + (pedido.detalhesCliente.nomeContato != null ? pedido.detalhesCliente.nomeContato.length() : "null") + ")");
                System.out.println("  tipoCliente: '" + pedido.detalhesCliente.tipoCliente + "' (tamanho: " + (pedido.detalhesCliente.tipoCliente != null ? pedido.detalhesCliente.tipoCliente.length() : "null") + ")");
                System.out.println("  telefone: '" + pedido.detalhesCliente.telefone + "' (tamanho: " + (pedido.detalhesCliente.telefone != null ? pedido.detalhesCliente.telefone.length() : "null") + ")");
                System.out.println("  telefone2: '" + pedido.detalhesCliente.telefone2 + "' (tamanho: " + (pedido.detalhesCliente.telefone2 != null ? pedido.detalhesCliente.telefone2.length() : "null") + ")");
                System.out.println("  telefone3: '" + pedido.detalhesCliente.telefone3 + "' (tamanho: " + (pedido.detalhesCliente.telefone3 != null ? pedido.detalhesCliente.telefone3.length() : "null") + ")");
                System.out.println("  territorio: '" + pedido.detalhesCliente.territorio + "' (tamanho: " + (pedido.detalhesCliente.territorio != null ? pedido.detalhesCliente.territorio.length() : "null") + ")");
                System.out.println("  bandeira: '" + pedido.detalhesCliente.bandeira + "' (tamanho: " + (pedido.detalhesCliente.bandeira != null ? pedido.detalhesCliente.bandeira.length() : "null") + ")");
                System.out.println("  CPFCNPJ: '" + pedido.detalhesCliente.CPFCNPJ + "' (tamanho: " + (pedido.detalhesCliente.CPFCNPJ != null ? pedido.detalhesCliente.CPFCNPJ.length() : "null") + ")");
                System.out.println("  bairro: '" + pedido.detalhesCliente.bairro + "' (tamanho: " + (pedido.detalhesCliente.bairro != null ? pedido.detalhesCliente.bairro.length() : "null") + ")");
                System.out.println("  tempoAtendimento: " + pedido.detalhesCliente.tempoAtendimento);
                System.out.println("  email: '" + pedido.detalhesCliente.email + "' (tamanho: " + (pedido.detalhesCliente.email != null ? pedido.detalhesCliente.email.length() : "null") + ")");
                System.out.println("  email2: '" + pedido.detalhesCliente.email2 + "' (tamanho: " + (pedido.detalhesCliente.email2 != null ? pedido.detalhesCliente.email2.length() : "null") + ")");
                System.out.println("  email3: '" + pedido.detalhesCliente.email3 + "' (tamanho: " + (pedido.detalhesCliente.email3 != null ? pedido.detalhesCliente.email3.length() : "null") + ")");
                System.out.println("  latitude: " + pedido.detalhesCliente.latitude);
                System.out.println("  longitude: " + pedido.detalhesCliente.longitude);
                System.out.println("  prioridadeCliente: '" + pedido.detalhesCliente.prioridadeCliente + "' (tamanho: " + (pedido.detalhesCliente.prioridadeCliente != null ? pedido.detalhesCliente.prioridadeCliente.length() : "null") + ")");
            }

            if (pedido.codigosBarras != null && !pedido.codigosBarras.isEmpty()) {
                System.out.println("  - CODIGOS_BARRAS: " + pedido.codigosBarras.size() + " itens");
                for (int j = 0; j < pedido.codigosBarras.size(); j++) {
                    CodigoBarras cb = pedido.codigosBarras.get(j);
                    System.out.println("    [" + (j+1) + "] numero: " + cb.numero + ", setor: " + cb.setor + ", numeroCaixa: " + cb.numeroCaixa + ", descricao: " + cb.descricao);
                }
            }
        }

        return fazerChamadaHTTP(Config.ENDPOINT_ENVIOPEDIDOS, jsonPayload);
    }

    private static List<PedidoPerformaxxi> corrigirEValidarPedidos(List<PedidoPerformaxxi> pedidos) {
        if (pedidos == null || pedidos.isEmpty()) {
            return pedidos;
        }

        System.out.println("[PERFORMAXXI] Iniciando correcao e validacao de " + pedidos.size() + " pedidos...");

        System.out.println("[PERFORMAXXI] Enviando todos os " + pedidos.size() + " pedidos sem eliminacao de duplicados");

        for (PedidoPerformaxxi pedido : pedidos) {
            corrigirPedido(pedido);
        }

        gerarCodigosRemessaUnicos(pedidos);

        System.out.println("[PERFORMAXXI] Correcao e validacao concluida. Pedidos finais: " + pedidos.size());
        return pedidos;
    }

    private static void corrigirPedido(PedidoPerformaxxi pedido) {
        System.out.println("[PERFORMAXXI] DEBUG: Corrigindo pedido " + pedido.numeroPedido);
        pedido.dataPedido = corrigirFormatoData(pedido.dataPedido);

        if (pedido.quantidadeItem <= 0) {
            pedido.quantidadeItem = 1;
        }
        if (pedido.tipoEntrega <= 0) {
            pedido.tipoEntrega = 1;
        }
        if (pedido.tempoAtendimentoPedido <= 0) {
            pedido.tempoAtendimentoPedido = 30;
        }

        if (pedido.identificadorEmbalagem == null) {
            pedido.identificadorEmbalagem = "PADRAO";
        }
        if (pedido.descricaoEmbalagem == null) {
            pedido.descricaoEmbalagem = "Embalagem Padrao";
        }
        if (pedido.identificadorTransportadora == null) {
            pedido.identificadorTransportadora = "TRANSP001";
        }
        if (pedido.nomeTransportadora == null) {
            pedido.nomeTransportadora = "Transportadora Padrao";
        }
        if (pedido.identificadorDeposito == null) {
            pedido.identificadorDeposito = "DEP001";
        }
        if (pedido.nomeDeposito == null) {
            pedido.nomeDeposito = "Deposito Central";
        }
        if (pedido.instrucoesEntrega == null) {
            pedido.instrucoesEntrega = "Entregar no horario comercial";
        }

        if (pedido.comentarios != null && pedido.comentarios.length() > 500) {
            pedido.comentarios = pedido.comentarios.substring(0, 500);
        }

        if (pedido.instrucoesEntrega != null && pedido.instrucoesEntrega.length() > 500) {
            pedido.instrucoesEntrega = pedido.instrucoesEntrega.substring(0, 500);
        }

        if (pedido.nomeTransportadora != null && pedido.nomeTransportadora.length() > 100) {
            pedido.nomeTransportadora = pedido.nomeTransportadora.substring(0, 100);
        }

        if (pedido.nomeDeposito != null && pedido.nomeDeposito.length() > 100) {
            pedido.nomeDeposito = pedido.nomeDeposito.substring(0, 100);
        }

        if (pedido.dataHoraLimiteEntrega == null) {
            try {
                Date dataPedido = DATE_FORMAT_API.parse(pedido.dataPedido);
                Date dataLimite = new Date(dataPedido.getTime() + (7 * 24 * 60 * 60 * 1000L));
                pedido.dataHoraLimiteEntrega = DATETIME_FORMAT.format(dataLimite);
            } catch (Exception e) {
                pedido.dataHoraLimiteEntrega = DATETIME_FORMAT.format(new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)));
            }
        }

        if (pedido.janelaAtendimento == null) {
            pedido.janelaAtendimento = new JanelaAtendimento("08:00", "18:00");
        }

        if (pedido.detalhesCliente != null) {
            corrigirDetalhesCliente(pedido.detalhesCliente);
        }
    }

    private static void corrigirDetalhesCliente(DetalhesCliente cliente) {
        System.out.println("[PERFORMAXXI] DEBUG: Corrigindo detalhes do cliente " + cliente.identificadorCliente);
        System.out.println("[PERFORMAXXI] DEBUG: nomeContato original: '" + cliente.nomeContato + "' (tamanho: " + (cliente.nomeContato != null ? cliente.nomeContato.length() : "null") + ")");

        if (cliente.tempoAtendimento <= 0) {
            cliente.tempoAtendimento = 30;
        }

        if (cliente.nomeContato == null) {
            cliente.nomeContato = cliente.nomeCliente;
        }
        if (cliente.tipoCliente == null) {
            cliente.tipoCliente = "PESSOA_JURIDICA";
        }
        if (cliente.territorio == null) {
            cliente.territorio = cliente.estado;
        }
        if (cliente.bandeira == null) {
            cliente.bandeira = "PADRAO";
        }
        if (cliente.bairro == null) {
            cliente.bairro = "Centro";
        }
        if (cliente.prioridadeCliente == null) {
            cliente.prioridadeCliente = "NORMAL";
        }

        if (cliente.nomeContato != null && cliente.nomeContato.length() > 50) {
            System.out.println("[PERFORMAXXI] DEBUG: Truncando nomeContato de " + cliente.nomeContato.length() + " para 50 caracteres");
            cliente.nomeContato = cliente.nomeContato.substring(0, 50);
        }

        if (cliente.nomeCliente != null && cliente.nomeCliente.length() > 100) {
            cliente.nomeCliente = cliente.nomeCliente.substring(0, 100);
        }

        if (cliente.endereco != null && cliente.endereco.length() > 200) {
            cliente.endereco = cliente.endereco.substring(0, 200);
        }

        if (cliente.cidade != null && cliente.cidade.length() > 50) {
            cliente.cidade = cliente.cidade.substring(0, 50);
        }

        if (cliente.bairro != null && cliente.bairro.length() > 50) {
            cliente.bairro = cliente.bairro.substring(0, 50);
        }

        if (cliente.telefone != null && cliente.telefone.length() > 20) {
            cliente.telefone = cliente.telefone.substring(0, 20);
        }

        if (cliente.email != null && cliente.email.length() > 100) {
            cliente.email = cliente.email.substring(0, 100);
        }

        System.out.println("[PERFORMAXXI] DEBUG: nomeContato final: '" + cliente.nomeContato + "' (tamanho: " + (cliente.nomeContato != null ? cliente.nomeContato.length() : "null") + ")");

        if (cliente.endereco == null || cliente.endereco.trim().isEmpty() ||
            cliente.endereco.contains("<SEM ENDERECO>") || cliente.endereco.contains("S/N")) {
            String cidade = cliente.cidade != null ? cliente.cidade : "Cidade";
            String estado = cliente.estado != null ? cliente.estado : "Estado";
            cliente.endereco = "Centro, " + cidade + ", " + estado;
        }

        if ((cliente.latitude == 0.0 && cliente.longitude == 0.0) ||
            (cliente.latitude == -23.5505 && cliente.longitude == -46.6333)) {
            cliente.latitude = -19.9167;
            cliente.longitude = -43.9345;
        }
    }

    private static String corrigirFormatoData(String data) {
        if (data == null || data.trim().isEmpty()) {
            return DATE_FORMAT_API.format(new Date());
        }

        try {
            if (data.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return data;
            }

            if (data.endsWith(".0")) {
                data = data.substring(0, data.length() - 2);
            }

            if (data.contains(" ")) {
                return data.substring(0, 10);
            }

            return data;
        } catch (Exception e) {
            return DATE_FORMAT_API.format(new Date());
        }
    }

    private static void validarCamposFinais(List<PedidoPerformaxxi> pedidos) {
        System.out.println("[PERFORMAXXI] Validação final de campos antes da serialização JSON...");

        for (PedidoPerformaxxi pedido : pedidos) {
            if (pedido.detalhesCliente != null) {

                if (pedido.detalhesCliente.nomeContato != null && pedido.detalhesCliente.nomeContato.length() > 50) {
                    pedido.detalhesCliente.nomeContato = pedido.detalhesCliente.nomeContato.substring(0, 50);
                }

                if (pedido.detalhesCliente.nomeCliente != null && pedido.detalhesCliente.nomeCliente.length() > 100) {
                    pedido.detalhesCliente.nomeCliente = pedido.detalhesCliente.nomeCliente.substring(0, 100);
                }

                if (pedido.detalhesCliente.endereco != null && pedido.detalhesCliente.endereco.length() > 200) {
                    pedido.detalhesCliente.endereco = pedido.detalhesCliente.endereco.substring(0, 200);
                }
            }

            if (pedido.comentarios != null && pedido.comentarios.length() > 500) {
                pedido.comentarios = pedido.comentarios.substring(0, 500);
            }

            if (pedido.instrucoesEntrega != null && pedido.instrucoesEntrega.length() > 500) {
                pedido.instrucoesEntrega = pedido.instrucoesEntrega.substring(0, 500);
            }
        }

        System.out.println("[PERFORMAXXI] Validação final concluída.");
    }

    private static void gerarCodigosRemessaUnicos(List<PedidoPerformaxxi> pedidos) {
        long timestamp = System.currentTimeMillis();
        int contador = 1;

        for (PedidoPerformaxxi pedido : pedidos) {
            if (pedido.codigoRemessa == null || pedido.codigoRemessa.trim().isEmpty()) {
                pedido.codigoRemessa = "REM_" + timestamp + "_" + contador;
                contador++;
            }
        }
    }

    public static RespostaComprovantes fazerChamadaHTTPGET(String endpoint) throws Exception {
        String urlCompleta = obterUrlCompleta(endpoint);

        System.out.println("[PERFORMAXXI] DEBUG: [CONSULTA] Consultando comprovantes via API Performaxxi...");
        System.out.println("[PERFORMAXXI] DEBUG: [NET] URL: " + urlCompleta);
        System.out.println("[PERFORMAXXI] DEBUG: [USER] Usuario: " + Config.API_USERNAME);
        System.out.println("[PERFORMAXXI] DEBUG: [ENV] Ambiente: " + Config.AMBIENTE_ATIVO + " | URL: " + obterUrlBaseAmbiente() + " | Usuario: " + Config.API_USERNAME);

        URL url = new URL(urlCompleta);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Authorization", criarHeaderAutenticacao());
            connection.setConnectTimeout(Config.TIMEOUT_CONEXAO);
            connection.setReadTimeout(Config.TIMEOUT_LEITURA);

            int responseCode = connection.getResponseCode();
            System.out.println("[PERFORMAXXI] Resposta da API Performaxxi - Codigo: " + responseCode);

            String responseBody;
            if (responseCode >= 200 && responseCode < 300) {
                responseBody = lerResposta(connection.getInputStream());
            } else {
                responseBody = lerResposta(connection.getErrorStream());
            }

            System.out.println("[PERFORMAXXI] DEBUG: Corpo da resposta: " + responseBody);

            if (responseBody.trim().startsWith("<!DOCTYPE") || responseBody.trim().startsWith("<html")) {
                RespostaComprovantes resposta = new RespostaComprovantes();
                resposta.Sucesso = false;
                resposta.MensagemErro = "Endpoint nao encontrado (404)";
                resposta.Valor = new java.util.ArrayList<>();
                return resposta;
            }

            RespostaComprovantes resposta = gson.fromJson(responseBody, RespostaComprovantes.class);
            return resposta;

        } finally {
            connection.disconnect();
        }
    }

    public static RespostaAPI fazerChamadaHTTP(String endpoint, String jsonPayload) throws Exception {
        String urlCompleta = obterUrlCompleta(endpoint);

        System.out.println("[PERFORMAXXI] DEBUG: [BUSCA] Validando conectividade com API Performaxxi...");
        System.out.println("[PERFORMAXXI] DEBUG: [NET] URL: " + urlCompleta);
        System.out.println("[PERFORMAXXI] DEBUG: [USER] Usuario: " + Config.API_USERNAME);
        System.out.println("[PERFORMAXXI] DEBUG: [ENV] Ambiente: " + Config.AMBIENTE_ATIVO + " | URL: " + obterUrlBaseAmbiente() + " | Usuario: " + Config.API_USERNAME);

        URL url = new URL(urlCompleta);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Authorization", criarHeaderAutenticacao());
            connection.setDoOutput(true);
            connection.setConnectTimeout(Config.TIMEOUT_CONEXAO);
            connection.setReadTimeout(Config.TIMEOUT_LEITURA);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("[PERFORMAXXI] Resposta da API Performaxxi - Codigo: " + responseCode);

            String responseBody;
            if (responseCode >= 200 && responseCode < 300) {
                responseBody = lerResposta(connection.getInputStream());
            } else {
                responseBody = lerResposta(connection.getErrorStream());
            }

            System.out.println("[PERFORMAXXI] DEBUG: Corpo da resposta: " + responseBody);

            RespostaAPI resposta = gson.fromJson(responseBody, RespostaAPI.class);

            if (resposta != null) {
                resposta.valor = resposta.Valor;
                resposta.sucesso = resposta.Sucesso;
                resposta.mensagemErro = resposta.MensagemErro;
                resposta.codigoErro = resposta.CodigoErro;
            }

            return resposta;

        } finally {
            connection.disconnect();
        }
    }

    private static String criarHeaderAutenticacao() {
        String credentials = Config.API_USERNAME + ":" + Config.API_PASSWORD;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }

    private static String lerResposta(java.io.InputStream inputStream) throws IOException {
        if (inputStream == null) return "";

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private static String obterUrlBaseAmbiente() {
        return "HOMOLOGACAO".equals(Config.AMBIENTE_ATIVO) ?
               Config.API_BASE_URL_HOMOLOGACAO : Config.API_BASE_URL_PRODUCAO;
    }

    private static String obterUrlCompleta(String endpoint) {
        return obterUrlBaseAmbiente() + endpoint;
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String createSuccessMessageWithRecords(String message, int count, long timestamp, String records) {
        return "<div style='color: green; font-weight: bold;'>" + message + "</div>" +
               "<p><strong>Pedidos enviados:</strong> " + count + "</p>" +
               "<p><strong>Timestamp:</strong> " + new Date(timestamp) + "</p>" +
               "<div style='margin-top: 10px;'>" + records + "</div>";
    }

    public static String createErrorMessage(String message, long timestamp) {
        return "<div style='color: red; font-weight: bold;'>" + message + "</div>" +
               "<p><strong>Timestamp:</strong> " + new Date(timestamp) + "</p>";
    }

    public static String createWarningMessage(String message) {
        return "<div style='color: orange; font-weight: bold;'>" + message + "</div>";
    }

    public static String createInfoMessage(String message) {
        return "<div style='color: blue; font-weight: bold;'>" + message + "</div>";
    }

    public static String tratarErroPerformaxxi(Exception e) {
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("401") || message.contains("autenticacao")) {
                return "ERRO DE AUTENTICACAO - Verifique as credenciais da API Performaxxi";
            }
            if (message.contains("403")) {
                return "ERRO DE AUTORIZACAO - Usuario sem permissao para acessar a API";
            }
            if (message.contains("404")) {
                return "ERRO DE ENDPOINT - URL da API nao encontrada";
            }
            if (message.contains("500")) {
                return "ERRO INTERNO DA API - Tente novamente em alguns minutos";
            }
            if (message.contains("timeout")) {
                return "ERRO DE TIMEOUT - Verifique a conectividade com a internet";
            }
        }
        return null;
    }

    public static void logError(String message, Exception e) {
        System.err.println("[PERFORMAXXI ERROR] " + message);
        if (e != null) {
            e.printStackTrace();
        }
    }

    public static void logInfo(String message) {
        System.out.println("[PERFORMAXXI INFO] " + message);
    }

    public static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    public static String generateEventCorrelationId() {
        return "EVT_" + System.currentTimeMillis();
    }

    public static String converterParaRecebimentoPerformaxxi(int nufin, int codParc, double valorRecebido,
            String dataRecebimento, int recdesp, int ordemCarga, String nomeCliente,
            int codEmp, int codTipoOperBaixa, String dataTipoOperBaixa) {

        Map<String, Object> recebimento = new HashMap<>();
        recebimento.put("nufin", nufin);
        recebimento.put("codParc", codParc);
        recebimento.put("valorRecebido", valorRecebido);
        recebimento.put("dataRecebimento", dataRecebimento);
        recebimento.put("recdesp", recdesp);
        recebimento.put("ordemCarga", ordemCarga);
        recebimento.put("nomeCliente", nomeCliente);
        recebimento.put("codEmp", codEmp);
        recebimento.put("codTipoOperBaixa", codTipoOperBaixa);
        recebimento.put("dataTipoOperBaixa", dataTipoOperBaixa);

        return gson.toJson(recebimento);
    }

    public static RespostaMensagemRota fazerChamadaHTTPMensagemRota(String endpoint, String jsonPayload) throws Exception {
        String urlCompleta = obterUrlCompleta(endpoint);

        System.out.println("[PERFORMAXXI] DEBUG: [MENSAGEM] Enviando mensagem via API Performaxxi...");
        System.out.println("[PERFORMAXXI] DEBUG: [NET] URL: " + urlCompleta);
        System.out.println("[PERFORMAXXI] DEBUG: [USER] Usuario: " + Config.API_USERNAME);
        System.out.println("[PERFORMAXXI] DEBUG: [ENV] Ambiente: " + Config.AMBIENTE_ATIVO + " | URL: " + obterUrlBaseAmbiente() + " | Usuario: " + Config.API_USERNAME);

        URL url = new URL(urlCompleta);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Authorization", criarHeaderAutenticacao());
            connection.setDoOutput(true);
            connection.setConnectTimeout(Config.TIMEOUT_CONEXAO);
            connection.setReadTimeout(Config.TIMEOUT_LEITURA);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("[PERFORMAXXI] Resposta da API Performaxxi - Codigo: " + responseCode);

            String responseBody;
            if (responseCode >= 200 && responseCode < 300) {
                responseBody = lerResposta(connection.getInputStream());
            } else {
                responseBody = lerResposta(connection.getErrorStream());
            }

            System.out.println("[PERFORMAXXI] DEBUG: Corpo da resposta: " + responseBody);

            RespostaMensagemRota resposta = gson.fromJson(responseBody, RespostaMensagemRota.class);
            return resposta;

        } finally {
            connection.disconnect();
        }
    }

    public static RespostaMensagemRota enviarMensagemRota(String idRastreador, String data,
            String classe, String conjunto, String idCliente, String mensagem) throws Exception {

        if (isNullOrEmpty(idRastreador) || isNullOrEmpty(data) || isNullOrEmpty(classe) ||
            isNullOrEmpty(conjunto) || isNullOrEmpty(idCliente) || isNullOrEmpty(mensagem)) {
            throw new IllegalArgumentException("Todos os parametros sao obrigatorios: idRastreador, data, classe, conjunto, idCliente, mensagem");
        }

        MensagemRota mensagemRota = new MensagemRota(idRastreador, data, classe, conjunto, idCliente, mensagem);
        String jsonPayload = gson.toJson(mensagemRota);

        System.out.println("[PERFORMAXXI] Enviando mensagem para rota:");
        System.out.println("[PERFORMAXXI] Veiculo: " + idRastreador + ", Data: " + data);
        System.out.println("[PERFORMAXXI] Classe: " + classe + ", Conjunto: " + conjunto);
        System.out.println("[PERFORMAXXI] Cliente: " + idCliente + ", Mensagem: " + mensagem);
        System.out.println("[PERFORMAXXI] DEBUG: Payload JSON: " + jsonPayload);

        return fazerChamadaHTTPMensagemRota(Config.ENDPOINT_ENVIOMENSAGEM, jsonPayload);
    }

    public static RespostaMensagemRota enviarMensagemRecebimento(String jsonRecebimento) throws Exception {
        System.out.println("[PERFORMAXXI] Enviando recebimento como mensagem de rota: " + jsonRecebimento);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> recebimentoMap = gson.fromJson(jsonRecebimento, Map.class);

            String idRastreador = "REC_" + recebimentoMap.getOrDefault("ordemCarga", System.currentTimeMillis());

            String dataRecebimento = (String) recebimentoMap.get("dataRecebimento");
            String dataRota = dataRecebimento;
            if (dataRecebimento != null && dataRecebimento.contains("T")) {
                dataRota = dataRecebimento.substring(0, 10);
            } else if (dataRecebimento == null) {
                dataRota = DATE_FORMAT_API.format(new Date());
            }

            String classe = "RECEBIMENTO";
            String conjunto = "FINANCEIRO";
            String idCliente = String.valueOf(recebimentoMap.getOrDefault("codParc", "0"));

            String mensagem = jsonRecebimento;

            System.out.println("[PERFORMAXXI] Dados para mensagem de rota:");
            System.out.println("  ID Rastreador: " + idRastreador);
            System.out.println("  Data: " + dataRota);
            System.out.println("  Classe: " + classe);
            System.out.println("  Conjunto: " + conjunto);
            System.out.println("  ID Cliente: " + idCliente);
            System.out.println("  Mensagem: " + mensagem);

            return enviarMensagemRota(idRastreador, dataRota, classe, conjunto, idCliente, mensagem);

        } catch (Exception e) {
            System.err.println("[PERFORMAXXI] Erro ao enviar recebimento como mensagem de rota: " + e.getMessage());
            e.printStackTrace();

            RespostaMensagemRota respostaErro = new RespostaMensagemRota();
            respostaErro.Sucesso = false;
            respostaErro.MensagemErro = "Erro ao processar recebimento: " + e.getMessage();
            respostaErro.CodigoErro = "PROCESSING_ERROR";
            return respostaErro;
        }
    }

    public static List<Object> consultarComprovantesEntrega(String data, String idRastreador, String classe, String conjunto) throws Exception {

        if (isNullOrEmpty(data) || isNullOrEmpty(idRastreador) || isNullOrEmpty(classe) || isNullOrEmpty(conjunto)) {
            throw new IllegalArgumentException("Todos os parametros sao obrigatorios: data, idRastreador, classe, conjunto");
        }

        String endpoint = String.format("%s/%s/%s/%s/%s", Config.ENDPOINT_COMPROVANTES, data, idRastreador, classe, conjunto);

        System.out.println("[PERFORMAXXI] Consultando comprovantes para veiculo: " + idRastreador);
        System.out.println("[PERFORMAXXI] Data: " + data + ", Classe: " + classe + ", Conjunto: " + conjunto);

        RespostaComprovantes resposta = fazerChamadaHTTPGET(endpoint);

        if (resposta.isSucesso() && resposta.getValor() != null) {
            return new java.util.ArrayList<>(resposta.getValor());
        }

        return new java.util.ArrayList<>();
    }

    public static class RespostaComprovantes {
        public boolean Sucesso;
        public String MensagemErro;
        public List<ComprovanteEntrega> Valor;

        public boolean isSucesso() { return Sucesso; }
        public String getMensagemErro() { return MensagemErro; }
        public List<ComprovanteEntrega> getValor() { return Valor; }
    }

    public static class ComprovanteEntrega {
        public String codigoEntrega;
        public String identificadorCliente;
        public String urlAcesso;
    }

    public static class MensagemRota {
        public String idRastreador;
        public String data;
        public String classe;
        public String conjunto;
        public String idCliente;
        public String mensagem;

        public MensagemRota(String idRastreador, String data, String classe, String conjunto,
                           String idCliente, String mensagem) {
            this.idRastreador = idRastreador;
            this.data = data;
            this.classe = classe;
            this.conjunto = conjunto;
            this.idCliente = idCliente;
            this.mensagem = mensagem;
        }
    }

    public static class RespostaMensagemRota {
        public boolean Sucesso;
        public String MensagemErro;
        public String CodigoErro;

        public boolean isSucesso() { return Sucesso; }
        public String getMensagemErro() { return MensagemErro; }
        public String getCodigoErro() { return CodigoErro; }
    }
}
