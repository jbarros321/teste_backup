package br.com.petkids.neogrid.service.impl;

import java.sql.Timestamp;
import java.util.List;
import br.com.petkids.neogrid.exception.NeogridFileException;
import br.com.petkids.neogrid.exception.NeogridServiceException;
import br.com.petkids.neogrid.model.dto.ClienteDTO;
import br.com.petkids.neogrid.model.enums.TipoRelatorio;
import br.com.petkids.neogrid.repository.ClientesRepository;
import br.com.petkids.neogrid.util.NeogridFormatter;
import br.com.petkids.neogrid.util.NeogridConstants;
import br.com.petkids.neogrid.util.NeogridLogFactory;
import com.sankhya.util.StringUtils;

public class ClientesService extends AbstractNeogridService {
    private static final TipoRelatorio TIPO_RELATORIO = TipoRelatorio.CLIENTES;
    private ClientesRepository repository = new ClientesRepository();

    @Override
    protected TipoRelatorio getTipoRelatorio() { return TIPO_RELATORIO; }

    @Override
    protected List<?> buscarDados(String cnpjFilial, String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) throws Exception {
        return repository.buscarClientes(periodoIni, periodoFin);
    }

    @Override
    protected String gerarLinhaDados(Object dto) {
        try {
            ClienteDTO cliente = (ClienteDTO) dto;
            String codigoCliente;
            if (cliente.getTipoPessoa() != null && !"F".equals(cliente.getTipoPessoa()) &&
                cliente.getCgcCpf() != null && !cliente.getCgcCpf().trim().isEmpty()) {
                codigoCliente = NeogridFormatter.formatarCnpjCpf(cliente.getCgcCpf());
                if (codigoCliente == null || codigoCliente.isEmpty()) {
                    codigoCliente = NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(cliente.getCodigoCliente()), 20);
                }
            } else {
                codigoCliente = NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(cliente.getCodigoCliente()), 20);
            }
            String codigoSegmento = NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(cliente.getCodigoSegmento()), 3);
            if (codigoSegmento.isEmpty()) codigoSegmento = "169";
            String frequenciaVisita = NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(cliente.getFrequenciaVisita()), 2);
            if (frequenciaVisita.isEmpty()) frequenciaVisita = "04";
            String telefone = StringUtils.getNullAsEmpty(cliente.getTelefone());
            if (telefone == null || telefone.trim().isEmpty()) {
                telefone = "00000000000";
            } else {
                telefone = telefone.replaceAll("[^0-9]", "");
                if (telefone.isEmpty()) telefone = "00000000000";
            }
            telefone = NeogridFormatter.formatarAlfanumerico(telefone, 20);
            String uf = cliente.getUf();
            uf = formatarUF(uf);
            if (uf == null || uf.trim().isEmpty() || !uf.matches("^[A-Z]{2}$")) {
                uf = "DF";
                NeogridLogFactory.logErro("UF Cliente inválido ou vazio para cliente " + cliente.getCodigoCliente() + " - usando padrão DF", null);
            }
            return NeogridFormatter.criarLinha(NeogridConstants.TIPO_REGISTRO_DADOS,
                NeogridFormatter.formatarAlfanumerico(codigoCliente, 20),
                formatarCEP(cliente.getCep()), uf,
                NeogridFormatter.formatarAlfanumerico(cliente.getCidade(), 100),
                NeogridFormatter.formatarAlfanumerico(cliente.getEndereco(), 100),
                NeogridFormatter.formatarAlfanumerico(cliente.getBairro(), 100),
                NeogridFormatter.formatarAlfanumerico(cliente.getNomeCliente(), 100),
                codigoSegmento, frequenciaVisita, telefone,
                NeogridFormatter.formatarAlfanumerico(cliente.getContato(), 50));
        } catch (Exception e) {
            NeogridLogFactory.logErro("Erro ao gerar linha de cliente", e);
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
