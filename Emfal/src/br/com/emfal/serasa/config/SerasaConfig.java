package br.com.emfal.serasa.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import br.com.emfal.serasa.constants.SerasaConstants;
import br.com.emfal.serasa.util.SerasaUtils;

public class SerasaConfig {

    private static final SerasaConfig INSTANCE = new SerasaConfig();
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private volatile long cacheTimestamp = System.currentTimeMillis();
    private static final long CACHE_DURATION = 300_000;

    private SerasaConfig() {
    }

    public static SerasaConfig getInstance() {
        return INSTANCE;
    }

    public String obterConfiguracao(String chave) {
        return obterConfiguracao(chave, null);
    }

    public String obterConfiguracao(String chave, String valorPadrao) {
        if (!isCacheValido()) {
            limparCache();
        }
        return Optional.ofNullable(cache.get(chave))
            .filter(valor -> !valor.trim().isEmpty())
            .orElse(valorPadrao);
    }

    public void definirConfiguracao(String chave, String valor) {
        cache.put(chave, valor);
        SerasaUtils.logInfo("Configuracao definida: " + chave + " = " + valor);
    }

    public Map<String, String> obterCredenciais() {
        return new HashMap<>(cache);
    }

    public boolean validarConfiguracoes() {
        return Optional.of(obterConfiguracao(SerasaConstants.CLIENT_ID))
            .filter(id -> id != null && !id.trim().isEmpty())
            .flatMap(id -> Optional.of(obterConfiguracao(SerasaConstants.CLIENT_SECRET))
                .filter(secret -> secret != null && !secret.trim().isEmpty()))
            .map(secret -> {
                SerasaUtils.logInfo("Configuracoes validas!");
                return true;
            })
            .orElseGet(() -> {
                SerasaUtils.logErro("Configuracoes invalidas!");
                return false;
            });
    }

    public void limparCache() {
        cache.clear();
        cacheTimestamp = 0;
        SerasaUtils.logInfo("Cache de configuracoes limpo!");
    }

    private boolean isCacheValido() {
        return (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION;
    }
}
