package br.com.petkids.neogrid.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import br.com.petkids.neogrid.exception.NeogridFileException;
import br.com.petkids.neogrid.exception.NeogridServiceException;
import br.com.petkids.neogrid.model.dto.ProdutoDTO;
import br.com.petkids.neogrid.model.enums.TipoRelatorio;
import br.com.petkids.neogrid.repository.ProdutosRepository;
import br.com.petkids.neogrid.util.NeogridFormatter;
import br.com.petkids.neogrid.util.NeogridConstants;
import br.com.petkids.neogrid.util.NeogridLogFactory;
import br.com.petkids.neogrid.validation.NeogridValidator;
import com.sankhya.util.StringUtils;

public class ProdutosService extends AbstractNeogridService {
    private static final TipoRelatorio TIPO_RELATORIO = TipoRelatorio.PRODUTOS;
    private ProdutosRepository repository = new ProdutosRepository();

    @Override
    protected TipoRelatorio getTipoRelatorio() { return TIPO_RELATORIO; }

    @Override
    protected List<?> buscarDados(String cnpjFilial, String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) throws Exception {
        NeogridValidator.validarParametrosComIndustria(TIPO_RELATORIO.getIdentificacao(), cnpjFilial, cnpjIndustria);
        return repository.buscarProdutos(cnpjIndustria, periodoIni, periodoFin);
    }

    @Override
    protected String gerarLinhaDados(Object dto) {
        try {
            ProdutoDTO produto = (ProdutoDTO) dto;
            String codigoItem = NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(produto.getCodigoItem()), 20);
            String codigoProduto = NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(produto.getCodigoProduto()), 14);
            String tipoItem = produto.getTipoItem();
            if (tipoItem == null || tipoItem.isEmpty()) tipoItem = "01";
            BigDecimal qtdEmbalagem = produto.getQuantidadeEmbalagem();
            if (qtdEmbalagem == null || qtdEmbalagem.compareTo(BigDecimal.ZERO) == 0) qtdEmbalagem = BigDecimal.ONE;
            BigDecimal precoTabela = produto.getPrecoTabelaUnidade();
            if (precoTabela == null) precoTabela = BigDecimal.ZERO;
            String statusProduto = produto.getStatusProduto();
            if (statusProduto == null || statusProduto.isEmpty()) statusProduto = "01";
            return NeogridFormatter.criarLinha(NeogridConstants.TIPO_REGISTRO_DADOS, NeogridFormatter.formatarCnpjCpf(produto.getCnpjIndustria()), codigoItem,
                codigoProduto, tipoItem,
                NeogridFormatter.formatarDecimal(qtdEmbalagem.doubleValue(), 5), NeogridFormatter.formatarDecimal(precoTabela.doubleValue(), 2),
                NeogridFormatter.formatarAlfanumerico(produto.getDescricaoInterna(), 100), statusProduto);
        } catch (Exception e) {
            NeogridLogFactory.logErro("Erro ao gerar linha de produto", e);
            return null;
        }
    }

    @Override
    protected String gerarCabecalho(String cnpjFilial, String cnpjDestinatario, Timestamp dataHora, Timestamp periodoIni, Timestamp periodoFin) {
        return gerarCabecalhoPadrao(TIPO_RELATORIO, cnpjFilial, cnpjDestinatario, dataHora);
    }

    public String gerarArquivo(String cnpjFilial, String cnpjIndustria, String cnpjDestinatario,
            String caminhoExportacao, java.util.Date periodoIni, java.util.Date periodoFin)
            throws NeogridServiceException, NeogridFileException {
        return gerarArquivoBase(cnpjFilial, cnpjIndustria, cnpjDestinatario, caminhoExportacao, periodoIni, periodoFin);
    }
}
