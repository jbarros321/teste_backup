package br.com.petkids.neogrid.util;

import org.apache.commons.lang.exception.ExceptionUtils;
import java.util.Map;

public class NeogridLogFactory {
    private static final boolean LOGGING_ENABLED = true;
    private static final boolean PERFORMANCE_LOGGING_ENABLED = true;

    public static void iniciarLog(String tipoRelatorio, java.math.BigDecimal codUsuario) {
        if (!LOGGING_ENABLED) return;
        System.out.println(String.format("%s Iniciando geração de relatório: %s | Usuário: %s | Data/Hora: %s",
            NeogridConstants.LOG_PREFIX, tipoRelatorio, codUsuario, new java.util.Date()));
    }

    public static void logSucesso(String mensagem) {
        if (!LOGGING_ENABLED) return;
        System.out.println(String.format("%s %s | Categoria: SUCESSO | Status: OK", NeogridConstants.LOG_PREFIX, mensagem));
    }

    public static void logAviso(String mensagem) {
        if (!LOGGING_ENABLED) return;
        System.out.println(String.format("%s %s | Categoria: AVISO | Status: WARN", NeogridConstants.LOG_PREFIX, mensagem));
    }

    public static void logErro(String mensagem, Exception e) {
        if (!LOGGING_ENABLED) return;
        if (e == null) {
            System.err.println(String.format("%s ERRO: %s | Exceção: null", NeogridConstants.LOG_PREFIX, mensagem));
            return;
        }
        String mensagemErro = ExceptionUtils.getMessage(e);
        if (mensagemErro == null) {
            mensagemErro = e.getClass().getSimpleName();
        }
        System.err.println(String.format("%s ERRO: %s | Exceção: %s",
            NeogridConstants.LOG_PREFIX, mensagem, mensagemErro));
        if (e.getCause() != null) {
            String causa = ExceptionUtils.getMessage(e.getCause());
            if (causa != null) {
                System.err.println(String.format("%s Causa: %s", NeogridConstants.LOG_PREFIX, causa));
            }
        }
        try {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            if (stackTrace != null && !stackTrace.trim().isEmpty()) {
                System.err.println(String.format("%s Stack Trace:\n%s", NeogridConstants.LOG_PREFIX, stackTrace));
            }
        } catch (Exception ex) {
            System.err.println(String.format("%s Erro ao obter stack trace: %s", NeogridConstants.LOG_PREFIX, ex.getMessage()));
        }
    }

    public static void finalizarLog(String tipoRelatorio, boolean sucesso, int quantidadeRegistros) {
        if (!LOGGING_ENABLED) return;
        System.out.println(String.format("%s Finalizando geração de relatório: %s | Status: %s | Registros: %d | Data/Hora: %s",
            NeogridConstants.LOG_PREFIX, tipoRelatorio, sucesso ? "SUCESSO" : "ERRO", quantidadeRegistros, new java.util.Date()));
    }

    public static void logPerformance(String etapa, long tempoMs, String detalhes) {
        if (!PERFORMANCE_LOGGING_ENABLED) return;
        System.out.println(String.format("%s [PERFORMANCE] %s: %d ms | %s",
            NeogridConstants.LOG_PREFIX, etapa, tempoMs, detalhes));
    }

    public static void logConsultaSQL(String metodo, String sql, Map<String, Object> parametros) {
        if (!PERFORMANCE_LOGGING_ENABLED) return;
        System.out.println(String.format("%s [SQL] Método: %s", NeogridConstants.LOG_PREFIX, metodo));
        if (sql != null && sql.length() > 200) {
            System.out.println(String.format("%s [SQL] Query: %s...", NeogridConstants.LOG_PREFIX, sql.substring(0, 200)));
        } else if (sql != null) {
            System.out.println(String.format("%s [SQL] Query: %s", NeogridConstants.LOG_PREFIX, sql));
        }
        if (parametros != null && !parametros.isEmpty()) {
            System.out.println(String.format("%s [SQL] Parâmetros: %s", NeogridConstants.LOG_PREFIX, parametros));
        }
    }

    public static void logResultadoConsulta(String metodo, int quantidadeRegistros, long tempoExecucaoMs) {
        if (!PERFORMANCE_LOGGING_ENABLED) return;
        System.out.println(String.format("%s [SQL] Resultado: %d registros encontrados em %d ms | Método: %s",
            NeogridConstants.LOG_PREFIX, quantidadeRegistros, tempoExecucaoMs, metodo));
    }

    public static void logProcessamentoDados(String etapa, int quantidade, long tempoMs) {
        if (!PERFORMANCE_LOGGING_ENABLED) return;
        System.out.println(String.format("%s [PROCESSAMENTO] %s: %d registros processados em %d ms",
            NeogridConstants.LOG_PREFIX, etapa, quantidade, tempoMs));
    }

    public static void logGeracaoArquivo(String nomeArquivo, int linhas, long tempoMs) {
        if (!PERFORMANCE_LOGGING_ENABLED) return;
        System.out.println(String.format("%s [ARQUIVO] Gerado: %s | Linhas: %d | Tempo: %d ms",
            NeogridConstants.LOG_PREFIX, nomeArquivo, linhas, tempoMs));
    }
}
