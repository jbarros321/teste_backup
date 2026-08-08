package br.com.petkids.neogrid.service.impl;

import java.sql.Timestamp;
import java.util.List;
import br.com.petkids.neogrid.exception.NeogridFileException;
import br.com.petkids.neogrid.exception.NeogridServiceException;
import br.com.petkids.neogrid.model.dto.VendedorDTO;
import br.com.petkids.neogrid.model.enums.TipoRelatorio;
import br.com.petkids.neogrid.repository.VendedoresRepository;
import br.com.petkids.neogrid.util.NeogridFormatter;
import br.com.petkids.neogrid.util.NeogridConstants;
import br.com.petkids.neogrid.util.NeogridLogFactory;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

public class VendedoresService extends AbstractNeogridService {
    private static final TipoRelatorio TIPO_RELATORIO = TipoRelatorio.VENDEDORES;
    private VendedoresRepository repository = new VendedoresRepository();

    @Override
    protected TipoRelatorio getTipoRelatorio() { return TIPO_RELATORIO; }

    @Override
    protected List<?> buscarDados(String cnpjFilial, String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) throws Exception {
        return repository.buscarVendedores(periodoIni, periodoFin);
    }

    @Override
    protected String gerarLinhaDados(Object dto) {
        try {
            VendedorDTO vendedor = (VendedorDTO) dto;
            String status = vendedor.getStatus() != null ? vendedor.getStatus().getValor() : "A";
            Timestamp dataDeslig = "A".equals(status) ? TimeUtils.getNow() : (vendedor.getDataDesligamento() != null ? vendedor.getDataDesligamento() : TimeUtils.getNow());
            return NeogridFormatter.criarLinha(NeogridConstants.TIPO_REGISTRO_DADOS, "Pessoa Fisica",
                NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(vendedor.getCodigoVendedor()), 20), "Pessoa Fisica",
                NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(vendedor.getCodigoSupervisor()), 20), "Pessoa Fisica",
                NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(vendedor.getCodigoGerente()), 20), status,
                NeogridFormatter.formatarData(dataDeslig));
        } catch (Exception e) {
            NeogridLogFactory.logErro("Erro ao gerar linha de vendedor", e);
            return null;
        }
    }

    @Override
    protected String gerarCabecalho(String cnpjFilial, String cnpjDestinatario, Timestamp dataHora, Timestamp periodoIni, Timestamp periodoFin) {
        return gerarCabecalhoPadrao(TIPO_RELATORIO, cnpjFilial, cnpjDestinatario, dataHora);
    }

    public String gerarArquivo(String cnpjFilial, String cnpjDestinatario, String caminhoExportacao)
            throws NeogridServiceException, NeogridFileException {
        return gerarArquivoBase(cnpjFilial, null, cnpjDestinatario, caminhoExportacao, null, null);
    }
}
