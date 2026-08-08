package br.com.serpa.shared;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;

public class SerpaTXTHelper {

    public static String formatarLinhaTXT(Registro registro) {
        try {

            StringBuilder linha = new StringBuilder();

            Object numNota = registro.getCampo("NUNOTA");
            Object codParc = registro.getCampo("CODPARC");
            Object dtEmissao = registro.getCampo("DTNEG");

            if (numNota != null) {
                linha.append(formatarCampo(numNota.toString(), 10, '0', true));
            }

            linha.append("|");

            if (codParc != null) {
                linha.append(formatarCampo(codParc.toString(), 15, ' ', false));
            }

            linha.append("|");

            if (dtEmissao != null) {
                linha.append(formatarData(dtEmissao));
            }

            return linha.toString();

        } catch (Exception e) {
            System.err.println("Erro ao formatar linha TXT: " + e.getMessage());
            return null;
        }
    }

    public static void processarLinhaTXT(String linha, int numeroLinha, EntityFacade facade) throws Exception {
        try {

            if (linha == null || linha.trim().isEmpty()) {
                return;
            }

            String[] campos = linha.split("\\|");

            if (campos.length < 2) {
                throw new Exception("Linha inválida: quantidade insuficiente de campos");
            }

            String numNotaStr = campos[0].trim();
            String codParcStr = campos[1].trim();

            if (numNotaStr.isEmpty()) {
                throw new Exception("Campo NUNOTA é obrigatório");
            }

            BigDecimal numNota = new BigDecimal(numNotaStr);
            BigDecimal codParc = new BigDecimal(codParcStr);

        } catch (Exception e) {
            throw new Exception("Erro ao processar linha " + numeroLinha + ": " + e.getMessage(), e);
        }
    }

    public static String formatarCampo(String valor, int tamanho, char caracter, boolean alinharEsquerda) {
        if (valor == null) {
            valor = "";
        }

        if (valor.length() > tamanho) {
            valor = valor.substring(0, tamanho);
        }

        StringBuilder campo = new StringBuilder();
        if (alinharEsquerda) {
            campo.append(valor);
            while (campo.length() < tamanho) {
                campo.append(caracter);
            }
        } else {
            while (campo.length() < tamanho - valor.length()) {
                campo.append(caracter);
            }
            campo.append(valor);
        }

        return campo.toString();
    }

    public static String formatarData(Object data) {
        if (data == null) {
            return formatarCampo("", 8, '0', false);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            if (data instanceof Date) {
                return sdf.format((Date) data);
            } else if (data instanceof java.sql.Date) {
                return sdf.format(new Date(((java.sql.Date) data).getTime()));
            } else if (data instanceof java.sql.Timestamp) {
                return sdf.format(new Date(((java.sql.Timestamp) data).getTime()));
            } else {
                return formatarCampo(data.toString(), 8, '0', false);
            }
        } catch (Exception e) {
            System.err.println("Erro ao formatar data: " + e.getMessage());
            return formatarCampo("", 8, '0', false);
        }
    }

    public static void registrarLog(String tabelaLog, String tipo, String status,
                                    String mensagem, ContextoAcao contexto) throws Exception {
        try {
            EntityFacade facade = EntityFacadeFactory.getDWFFacade();
            JdbcWrapper jdbc = facade.getJdbcWrapper();

            criarTabelaLogSeNecessario(tabelaLog, jdbc);

            String sql = "INSERT INTO " + tabelaLog +
                        " (DTLOG, TIPO, STATUS, MENSAGEM, USUARIO, SEQUENCIA) VALUES " +
                        " (SYSDATE, ?, ?, ?, ?, ?)";

            NativeSql nativeSql = new NativeSql(jdbc);
            nativeSql.setSql(sql);
            nativeSql.setParameter(1, tipo);
            nativeSql.setParameter(2, status);
            nativeSql.setParameter(3, mensagem);
            nativeSql.setParameter(4, contexto.getUsuarioLogado() != null ?
                contexto.getUsuarioLogado().toString() : "SISTEMA");
            nativeSql.setParameter(5, obterProximaSequencia(tabelaLog, jdbc));

            nativeSql.executeUpdate();

        } catch (Exception e) {
            System.err.println("Erro ao registrar log: " + e.getMessage());

        }
    }

    private static void criarTabelaLogSeNecessario(String tabelaLog, JdbcWrapper jdbc) throws Exception {
        try {
            String sqlCheck = "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = ?";
            NativeSql checkSql = new NativeSql(jdbc);
            checkSql.setSql(sqlCheck);
            checkSql.setParameter(1, tabelaLog.toUpperCase());

            BigDecimal count = (BigDecimal) checkSql.loadSingleResult();

            if (count.intValue() == 0) {

                String sqlCreate = "CREATE TABLE " + tabelaLog + " (" +
                    "DTLOG DATE DEFAULT SYSDATE, " +
                    "TIPO VARCHAR2(50), " +
                    "STATUS VARCHAR2(20), " +
                    "MENSAGEM VARCHAR2(4000), " +
                    "USUARIO VARCHAR2(100), " +
                    "SEQUENCIA NUMBER" +
                    ")";

                NativeSql createSql = new NativeSql(jdbc);
                createSql.setSql(sqlCreate);
                createSql.executeUpdate();
            }
        } catch (Exception e) {

        }
    }

    private static BigDecimal obterProximaSequencia(String tabelaLog, JdbcWrapper jdbc) throws Exception {
        try {
            String sql = "SELECT NVL(MAX(SEQUENCIA), 0) + 1 FROM " + tabelaLog;
            NativeSql nativeSql = new NativeSql(jdbc);
            nativeSql.setSql(sql);
            return (BigDecimal) nativeSql.loadSingleResult();
        } catch (Exception e) {
            return BigDecimal.ONE;
        }
    }
}
