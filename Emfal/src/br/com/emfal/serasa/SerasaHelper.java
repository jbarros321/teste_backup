package br.com.emfal.serasa;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import br.com.emfal.serasa.service.SerasaAPIService;
import br.com.emfal.serasa.util.SerasaUtils;

public class SerasaHelper {

    private final SerasaAPIService service = SerasaAPIService.getInstance();

    public Map<String, Object> consultarSerasa(BigDecimal codigoParceiro, String documento) {
        SerasaUtils.logInfo("=== INICIANDO CONSULTA SERASA ===");
        SerasaUtils.logInfo("Parceiro: " + codigoParceiro);
        SerasaUtils.logInfo("Documento: " + documento);

        return Optional.ofNullable(documento)
            .filter(SerasaUtils::isValidDocument)
            .map(doc -> service.consultarCompleta(doc))
            .orElse(SerasaUtils.criarResultadoErro("Documento invalido"));
    }

    public Map<String, Object> consultarScore(String documento) {
        return Optional.ofNullable(documento)
            .filter(SerasaUtils::isValidDocument)
            .map(doc -> {
                String tipo = SerasaUtils.determinarTipoDocumento(doc);
                return service.consultarScore(doc, tipo);
            })
            .orElse(SerasaUtils.criarResultadoErro("Documento invalido"));
    }

    public Map<String, Object> consultarPendencias(String documento) {
        return Optional.ofNullable(documento)
            .filter(SerasaUtils::isValidDocument)
            .map(service::consultarPendencias)
            .orElse(SerasaUtils.criarResultadoErro("Documento invalido"));
    }

    public Map<String, Object> consultarAlertas(String documento) {
        return Optional.ofNullable(documento)
            .filter(SerasaUtils::isValidDocument)
            .map(service::consultarAlertas)
            .orElse(SerasaUtils.criarResultadoErro("Documento invalido"));
    }

    public boolean autenticar() {
        return service.autenticar();
    }

    public boolean validarDocumento(String documento) {
        return SerasaUtils.isValidDocument(documento);
    }

    public String formatarDocumento(String documento) {
        if (SerasaUtils.determinarTipoDocumento(documento).equals("PF")) {
            return SerasaUtils.formatarCPF(documento);
        } else {
            return SerasaUtils.formatarCNPJ(documento);
        }
    }
}
