package br.com.emfal.serasa.mapper;

import java.util.Map;

import br.com.emfal.serasa.util.SerasaUtils;

public class SerasaResponseMapper {

    public Map<String, Object> mapearScorePF(Map<String, Object> dadosSerasa) {
        Map<String, Object> resultado = SerasaUtils.criarResultadoSucesso(null);

        if (dadosSerasa != null && dadosSerasa.containsKey("score")) {
            resultado.put("score", dadosSerasa.get("score"));
            resultado.put("risco", dadosSerasa.get("risco"));
        } else {
            resultado.put("score", 0);
            resultado.put("risco", "INDEFINIDO");
        }

        return resultado;
    }

    public Map<String, Object> mapearScorePJ(Map<String, Object> dadosSerasa) {
        Map<String, Object> resultado = SerasaUtils.criarResultadoSucesso(null);

        if (dadosSerasa != null && dadosSerasa.containsKey("score")) {
            resultado.put("score", dadosSerasa.get("score"));
            resultado.put("risco", dadosSerasa.get("risco"));
        } else {
            resultado.put("score", 0);
            resultado.put("risco", "INDEFINIDO");
        }

        return resultado;
    }

    public Map<String, Object> mapearPendencias(Map<String, Object> dadosSerasa) {
        Map<String, Object> resultado = SerasaUtils.criarResultadoSucesso(null);
        resultado.put("total_pendencias", 0);
        resultado.put("valor_total", 0.0);
        return resultado;
    }

    public Map<String, Object> mapearAlertas(Map<String, Object> dadosSerasa) {
        Map<String, Object> resultado = SerasaUtils.criarResultadoSucesso(null);
        resultado.put("total_alertas", 0);
        resultado.put("alertas_criticos", 0);
        return resultado;
    }

    public Map<String, Object> mapearCadastrais(Map<String, Object> dadosSerasa) {
        Map<String, Object> resultado = SerasaUtils.criarResultadoSucesso(null);
        resultado.put("nome", "");
        resultado.put("situacao", "");
        return resultado;
    }

    public Map<String, Object> mapearRenda(Map<String, Object> dadosSerasa) {
        Map<String, Object> resultado = SerasaUtils.criarResultadoSucesso(null);
        resultado.put("renda_estimada", 0.0);
        resultado.put("faixa_renda", "");
        return resultado;
    }

    public Map<String, Object> mapearJudiciais(Map<String, Object> dadosSerasa) {
        Map<String, Object> resultado = SerasaUtils.criarResultadoSucesso(null);
        resultado.put("total_acoes", 0);
        resultado.put("valor_total", 0.0);
        return resultado;
    }

    public Map<String, Object> mapearResultadoCompleto(Map<String, Object> dadosSerasa) {
        Map<String, Object> resultado = SerasaUtils.criarResultadoSucesso(null);
        resultado.put("score_pf", mapearScorePF(dadosSerasa));
        resultado.put("score_pj", mapearScorePJ(dadosSerasa));
        resultado.put("pendencias", mapearPendencias(dadosSerasa));
        resultado.put("alertas", mapearAlertas(dadosSerasa));
        resultado.put("cadastrais", mapearCadastrais(dadosSerasa));
        resultado.put("renda", mapearRenda(dadosSerasa));
        resultado.put("judiciais", mapearJudiciais(dadosSerasa));
        return resultado;
    }
}
