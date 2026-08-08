package br.com.emfal.serasa.constants;

public class SerasaConstants {

    public static final String API_BASE_URL = "https:
    public static final String AUTH_ENDPOINT = "/oauth/token";
    public static final String SCORE_PF_ENDPOINT = "/score/pf";
    public static final String SCORE_PJ_ENDPOINT = "/score/pj";
    public static final String PENDENCIAS_ENDPOINT = "/pendencias";
    public static final String ALERTAS_ENDPOINT = "/alertas";
    public static final String CADASTRAIS_ENDPOINT = "/cadastrais";
    public static final String RENDA_ENDPOINT = "/renda";
    public static final String JUDICIAIS_ENDPOINT = "/judiciais";

    public static final String CLIENT_ID = "CLIENT_ID";
    public static final String CLIENT_SECRET = "CLIENT_SECRET";
    public static final String API_URL = "API_URL";
    public static final String TIMEOUT = "TIMEOUT";
    public static final String RETRY_COUNT = "RETRY_COUNT";
    public static final String LOG_LEVEL = "LOG_LEVEL";

    public static final String TIPO_PF = "PF";
    public static final String TIPO_PJ = "PJ";

    public static final String STATUS_SUCESSO = "SUCESSO";
    public static final String STATUS_ERRO = "ERRO";
    public static final String STATUS_BLOQUEIO = "BLOQUEIO";

    public static final String RISCO_BAIXO = "BAIXO";
    public static final String RISCO_MEDIO = "MEDIO";
    public static final String RISCO_ALTO = "ALTO";
    public static final String RISCO_MUITO_ALTO = "MUITO_ALTO";

    public static final String RECOMENDACAO_APROVADO = "APROVADO";
    public static final String RECOMENDACAO_APROVADO_RESTRICOES = "APROVADO_COM_RESTRICOES";
    public static final String RECOMENDACAO_ANALISE_MANUAL = "ANALISE_MANUAL";
    public static final String RECOMENDACAO_REPROVADO = "REPROVADO";

    public static final int TIMEOUT_DEFAULT = 30000;
    public static final int RETRY_COUNT_DEFAULT = 3;
    public static final int TOKEN_EXPIRATION_HOURS = 1;
    public static final int REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    public static final int SCORE_APROVADO = 800;
    public static final int SCORE_APROVADO_RESTRICOES = 600;
    public static final int SCORE_ANALISE_MANUAL = 400;

    public static final int PENALIZACAO_PENDENCIA = 10;
    public static final int PENALIZACAO_ALERTA = 5;
    public static final int PENALIZACAO_ACAO_JUDICIAL = 15;

}
