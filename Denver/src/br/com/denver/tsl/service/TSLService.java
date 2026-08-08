package br.com.denver.tsl.service;

import br.com.denver.tsl.model.dto.ExpedicaoDTO;
import br.com.denver.tsl.model.dto.RecebimentoDTO;
import br.com.denver.tsl.repository.ExpedicaoRepository;
import br.com.denver.tsl.repository.RecebimentoRepository;
import br.com.denver.tsl.util.FileGenerator;
import br.com.denver.tsl.util.TSLConstants;
import br.com.denver.tsl.util.TSLFormatter;

import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TSLService {
    private final RecebimentoRepository recebimentoRepository = new RecebimentoRepository();
    private final ExpedicaoRepository expedicaoRepository = new ExpedicaoRepository();

    public String gerarArquivoRecebimento(String caminhoExportacao, String lote) throws Exception {
        Set<RecebimentoDTO> recebimentos = recebimentoRepository.buscarRecebimentosPorLote(lote);
        if (recebimentos.isEmpty()) throw new Exception("Nenhum recebimento encontrado para os parâmetros informados.");

        for (RecebimentoDTO dto : recebimentos) {
            if (dto.getDataProducao() == null) {
                throw new IllegalStateException(
                    String.format("Data de Produção é obrigatória mas não foi informada. " +
                        "Nota Fiscal: %s, Item: %s, Produto: %s, Lote: %s",
                        dto.getNotaFiscal(), dto.getItemNotaFiscal(), dto.getCodigoProduto(), dto.getLote())
                );
            }
            if (dto.getDataVencimento() == null) {
                throw new IllegalStateException(
                    String.format("Data de Vencimento é obrigatória mas não foi informada. " +
                        "Nota Fiscal: %s, Item: %s, Produto: %s, Lote: %s",
                        dto.getNotaFiscal(), dto.getItemNotaFiscal(), dto.getCodigoProduto(), dto.getLote())
                );
            }
        }

        String cnpj = recebimentos.iterator().next().getCnpj();
        Set<String> linhas = recebimentos.stream().map(this::gerarLinhaRecebimento).filter(l -> l.length() == TSLConstants.TAMANHO_LINHA_REC_IN).collect(Collectors.toCollection(LinkedHashSet::new));
        String caminhoCompleto = caminhoExportacao + File.separator + FileGenerator.gerarNomeArquivo(TSLConstants.INTERFACE_REC_IN, cnpj);
        FileGenerator.gerarArquivo(linhas, caminhoCompleto);
        return caminhoCompleto;
    }

    public String gerarArquivoExpedicao(String caminhoExportacao, BigDecimal nunota) throws Exception {
        Set<ExpedicaoDTO> expedicoes = expedicaoRepository.buscarExpedicoesPorNunota(nunota);
        if (expedicoes.isEmpty()) throw new Exception("Nenhuma expedição encontrada para a nota informada.");

        for (ExpedicaoDTO dto : expedicoes) {
            if (dto.getDataFabricacaoDe() == null) {
                throw new IllegalStateException(
                    String.format("Data de Fabricação (DE) é obrigatória mas não foi informada. " +
                        "Pedido: %s, Item: %s, Produto: %s, Lote: %s",
                        dto.getNumeroPedido(), dto.getItemPedido(), dto.getCodigoProduto(), dto.getLote())
                );
            }
            if (dto.getDataFabricacaoAte() == null) {
                throw new IllegalStateException(
                    String.format("Data de Fabricação (ATÉ) é obrigatória mas não foi informada. " +
                        "Pedido: %s, Item: %s, Produto: %s, Lote: %s",
                        dto.getNumeroPedido(), dto.getItemPedido(), dto.getCodigoProduto(), dto.getLote())
                );
            }
        }

        String cnpj = expedicoes.iterator().next().getCnpj();
        Set<String> linhas = expedicoes.stream().map(this::gerarLinhaExpedicao).filter(l -> l.length() == TSLConstants.TAMANHO_LINHA_PED_IN).collect(Collectors.toCollection(LinkedHashSet::new));
        String caminhoCompleto = caminhoExportacao + File.separator + FileGenerator.gerarNomeArquivo(TSLConstants.INTERFACE_PED_IN, cnpj);
        FileGenerator.gerarArquivo(linhas, caminhoCompleto);
        return caminhoCompleto;
    }

    private String gerarLinhaRecebimento(RecebimentoDTO dto) {
        StringBuilder linha = new StringBuilder(TSLConstants.TAMANHO_LINHA_REC_IN);
        linha.append(TSLFormatter.formatarCnpj(dto.getCnpj())).append(TSLFormatter.formatarNotaFiscal(dto.getNotaFiscal()))
            .append(TSLFormatter.formatarItem(dto.getItemNotaFiscal())).append(TSLFormatter.formatarPalete(dto.getNumeroPalete()))
            .append(TSLFormatter.formatarCodigoProduto(dto.getCodigoProduto())).append(TSLFormatter.formatarIdentificadorCaixa(dto.getIdentificadorCaixa()))
            .append(TSLFormatter.formatarPeso(dto.getPesoCaixa())).append(TSLFormatter.formatarData(dto.getDataProducao()))
            .append(TSLFormatter.formatarData(dto.getDataVencimento())).append(TSLFormatter.formatarLote(dto.getLote()))
            .append(TSLFormatter.formatarInfoComplementar(dto.getInfoComplementar())).append(TSLFormatter.formatarValorUnitario(dto.getValorUnitario()));
        int falta = Math.max(0, TSLConstants.TAMANHO_LINHA_REC_IN - linha.length());
        linha.append(TSLFormatter.obterEspacosChar(falta), 0, falta);
        return linha.toString();
    }

    private String gerarLinhaExpedicao(ExpedicaoDTO dto) {
        StringBuilder linha = new StringBuilder(TSLConstants.TAMANHO_LINHA_PED_IN);
        linha.append(TSLFormatter.formatarCnpj(dto.getCnpj())).append(TSLFormatter.formatarOrdemFrete(dto.getOrdemFrete()))
            .append(TSLFormatter.formatarNumeroPedido(dto.getNumeroPedido())).append(TSLFormatter.formatarItem(dto.getItemPedido()))
            .append(TSLFormatter.formatarCodigoProduto(dto.getCodigoProduto())).append(TSLFormatter.formatarPalete(dto.getNumeroPalete()))
            .append(TSLFormatter.formatarQuantidade(dto.getQuantidade())).append(TSLFormatter.formatarPeso(dto.getPeso(), 19))
            .append(TSLFormatter.formatarData(dto.getDataFabricacaoDe())).append(TSLFormatter.formatarData(dto.getDataFabricacaoAte()))
            .append(TSLFormatter.formatarLote(dto.getLote())).append(TSLFormatter.formatarCnpjCliente(dto.getCnpjCliente()));
        int falta = Math.max(0, TSLConstants.TAMANHO_LINHA_PED_IN - linha.length());
        linha.append(TSLFormatter.obterEspacosChar(falta), 0, falta);
        return linha.toString();
    }
}
