package br.com.petkids.neogrid.service;

import br.com.petkids.neogrid.exception.NeogridException;
import br.com.petkids.neogrid.service.impl.VendedoresService;
import br.com.petkids.neogrid.service.impl.ClientesService;
import br.com.petkids.neogrid.service.impl.ProdutosService;
import br.com.petkids.neogrid.service.impl.VendasService;
import br.com.petkids.neogrid.service.impl.EstoqueService;

public class NeogridService {
    private static final String CNPJ_DESTINATARIO = "03887830009046";

    public String gerarRelatorioVendedores(String cnpjFilial, String caminhoExportacao) throws NeogridException {
        return new VendedoresService().gerarArquivo(cnpjFilial, CNPJ_DESTINATARIO, caminhoExportacao);
    }

    public String gerarRelatorioClientes(String cnpjFilial, String caminhoExportacao) throws NeogridException {
        return new ClientesService().gerarArquivo(cnpjFilial, CNPJ_DESTINATARIO, caminhoExportacao);
    }

    public String gerarRelatorioProdutos(String cnpjFilial, String cnpjIndustria, String caminhoExportacao, java.util.Date periodoIni, java.util.Date periodoFin) throws NeogridException {
        return new ProdutosService().gerarArquivo(cnpjFilial, cnpjIndustria, CNPJ_DESTINATARIO, caminhoExportacao, periodoIni, periodoFin);
    }

    public String gerarRelatorioVendas(String cnpjFilial, String cnpjIndustria, String caminhoExportacao, java.util.Date periodoIni, java.util.Date periodoFin) throws NeogridException {
        return new VendasService().gerarArquivo(cnpjFilial, cnpjIndustria, null, caminhoExportacao, periodoIni, periodoFin);
    }

    public String gerarRelatorioEstoque(String cnpjFilial, String cnpjIndustria, String caminhoExportacao, java.util.Date periodoIni, java.util.Date periodoFin) throws NeogridException {
        return new EstoqueService().gerarArquivo(cnpjFilial, cnpjIndustria, null, caminhoExportacao, periodoIni, periodoFin);
    }
}
