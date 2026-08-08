package br.com.petkids.neogrid.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import br.com.petkids.neogrid.exception.NeogridFileException;
import br.com.petkids.neogrid.exception.NeogridServiceException;
import br.com.petkids.neogrid.model.dto.ItemVendaDTO;
import br.com.petkids.neogrid.model.dto.VendaDTO;
import br.com.petkids.neogrid.model.enums.TipoFrete;
import br.com.petkids.neogrid.model.enums.TipoRelatorio;
import br.com.petkids.neogrid.repository.VendasRepository;
import br.com.petkids.neogrid.util.FileGenerator;
import br.com.petkids.neogrid.util.NeogridFormatter;
import br.com.petkids.neogrid.util.NeogridConstants;
import br.com.petkids.neogrid.util.NeogridLogFactory;
import br.com.petkids.neogrid.validation.NeogridValidator;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

public class VendasService extends AbstractNeogridService {
    private static final TipoRelatorio TIPO_RELATORIO = TipoRelatorio.VENDAS;
    private VendasRepository repository = new VendasRepository();

    @Override
    protected boolean precisaCnpjIndustria() {
        return true;
    }

    @Override
    protected TipoRelatorio getTipoRelatorio() { return TIPO_RELATORIO; }

    @Override
    protected List<?> buscarDados(String cnpjFilial, String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) throws Exception {
        NeogridValidator.validarParametrosComIndustria(TIPO_RELATORIO.getIdentificacao(), cnpjFilial, cnpjIndustria);
        return repository.buscarVendas(cnpjIndustria, periodoIni, periodoFin);
    }

    @Override
    protected String gerarLinhaDados(Object dto) {
        return gerarLinhaNotaFiscal((VendaDTO) dto);
    }

    @Override
    protected String gerarCabecalho(String cnpjFilial, String cnpjDestinatario, Timestamp dataHora, Timestamp periodoIni, Timestamp periodoFin) {
        String dataAtual = NeogridFormatter.formatarData(dataHora);
        return NeogridFormatter.criarLinha(NeogridConstants.TIPO_REGISTRO_CABECALHO, TIPO_RELATORIO.getIdentificacao(),
            TIPO_RELATORIO.getVersao(), gerarNumeroRelatorio(), NeogridFormatter.formatarDataHora(dataHora),
            periodoIni != null ? NeogridFormatter.formatarData(periodoIni) : dataAtual,
            periodoFin != null ? NeogridFormatter.formatarData(periodoFin) : dataAtual,
            NeogridFormatter.formatarCnpjCpf(cnpjFilial), NeogridFormatter.formatarCnpjCpf(cnpjDestinatario));
    }

    @Override
    protected String obterCnpjDestinatarioFinal(String cnpjIndustria, String cnpjDestinatario, Timestamp periodoIni, Timestamp periodoFin) {
        return null;
    }

    private String obterCnpjIndustriaParaNomeArquivo(String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) {
        String cnpj = obterCnpjIndustria(cnpjIndustria, () -> {
            if (cnpjIndustria != null && !cnpjIndustria.trim().isEmpty()) {
                String cnpjFormatado = NeogridFormatter.formatarCnpjCpf(cnpjIndustria);
                if (cnpjFormatado != null && !cnpjFormatado.isEmpty() && cnpjFormatado.length() == 14) return cnpjFormatado;
            }
            String cnpjBusca = repository.buscarPrimeiroCnpjIndustria(periodoIni, periodoFin);
            if (cnpjBusca != null && !cnpjBusca.isEmpty()) {
                cnpjBusca = NeogridFormatter.formatarCnpjCpf(cnpjBusca);
                if (cnpjBusca != null && !cnpjBusca.isEmpty() && cnpjBusca.length() == 14) return cnpjBusca;
            }
            throw new NeogridServiceException("Não foi possível determinar o CNPJ da indústria para o relatório de vendas.");
        });
        return normalizarCnpjKelcoParaPrincipal(cnpj);
    }

    private String normalizarCnpjKelcoParaPrincipal(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return cnpj;
        }
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        if ("89064885000104".equals(cnpjLimpo) ||
            "13809963000344".equals(cnpjLimpo) ||
            "08811119000822".equals(cnpjLimpo)) {
            return "89064885000104";
        }
        return cnpj;
    }

    @Override
    protected String gerarArquivoBase(String cnpjFilial, String cnpjIndustria, String cnpjDestinatario,
            String caminhoExportacao, java.util.Date periodoIni, java.util.Date periodoFin)
            throws NeogridServiceException, NeogridFileException {
        long inicioTotal = System.currentTimeMillis();
        try {
            long inicio = System.currentTimeMillis();
            caminhoExportacao = NeogridValidator.validarCaminhoExportacao(caminhoExportacao);
            NeogridLogFactory.logPerformance("Validação de caminho", System.currentTimeMillis() - inicio, getTipoRelatorio().getIdentificacao());

            NeogridLogFactory.iniciarLog(getTipoRelatorio().getIdentificacao(), null);
            List<String> linhas = new java.util.ArrayList<>(5000);
            Timestamp dataHora = TimeUtils.getNow();
            Timestamp periodoIniTS = periodoIni != null ? new Timestamp(periodoIni.getTime()) : null;
            Timestamp periodoFinTS = periodoFin != null ? new Timestamp(periodoFin.getTime()) : null;

            inicio = System.currentTimeMillis();
            String cnpjIndustriaFormatado = obterCnpjIndustria(cnpjIndustria, () -> {
                String cnpj = repository.buscarPrimeiroCnpjIndustria(periodoIniTS, periodoFinTS);
                if (cnpj != null && !cnpj.isEmpty()) {
                    cnpj = NeogridFormatter.formatarCnpjCpf(cnpj);
                    if (cnpj != null && !cnpj.isEmpty() && cnpj.length() == 14) return cnpj;
                }
                throw new NeogridServiceException("Não foi possível determinar o CNPJ da indústria para o relatório de vendas.");
            });
            String cnpjDestinatarioFinal = normalizarCnpjKelcoParaPrincipal(cnpjIndustriaFormatado);
            String cnpjDestinatarioLimpo = NeogridFormatter.formatarCnpjCpf(cnpjDestinatarioFinal);
            if (cnpjDestinatarioLimpo != null && cnpjDestinatarioLimpo.equals(NeogridConstants.CNPJ_NEOGRID)) {
                throw new NeogridServiceException("CNPJ destinatário não pode ser o CNPJ da Neogrid. Deve ser o CNPJ da indústria para o relatório de vendas.");
            }
            NeogridValidator.validarCnpjDestinatario(cnpjDestinatarioFinal, precisaCnpjIndustria());
            NeogridLogFactory.logPerformance("Obtenção CNPJ destinatário", System.currentTimeMillis() - inicio, getTipoRelatorio().getIdentificacao());

            linhas.add(gerarCabecalho(cnpjFilial, cnpjDestinatarioFinal, dataHora, periodoIniTS, periodoFinTS));

            inicio = System.currentTimeMillis();
            List<?> dados = buscarDados(cnpjFilial, cnpjIndustria, periodoIniTS, periodoFinTS);
            long tempoBusca = System.currentTimeMillis() - inicio;
            NeogridLogFactory.logResultadoConsulta("buscarDados", dados.size(), tempoBusca);
            NeogridValidator.validarDadosDisponiveis(dados.size(), getTipoRelatorio().getIdentificacao());

            inicio = System.currentTimeMillis();
            int linhasProcessadas = 0;
            int itensProcessados = 0;
            for (Object dto : dados) {
                VendaDTO venda = (VendaDTO) dto;
                String linhaNota = gerarLinhaNotaFiscal(venda);
                if (linhaNota != null) {
                    linhas.add(linhaNota);
                    linhasProcessadas++;
                    for (ItemVendaDTO item : venda.getItens()) {
                        String linhaItem = gerarLinhaItem(item);
                        if (linhaItem != null) {
                            linhas.add(linhaItem);
                            itensProcessados++;
                        }
                    }
                }
            }
            long tempoProcessamento = System.currentTimeMillis() - inicio;
            NeogridLogFactory.logProcessamentoDados("Geração de linhas", linhasProcessadas, tempoProcessamento);
            NeogridLogFactory.logPerformance("Processamento de itens", 0,
                String.format("Notas: %d | Itens: %d", linhasProcessadas, itensProcessados));

            inicio = System.currentTimeMillis();
            String cnpjIndustriaParaArquivo = obterCnpjIndustriaParaNomeArquivo(cnpjIndustria, periodoIniTS, periodoFinTS);
            String nomeArquivo = FileGenerator.gerarNomeArquivo(getTipoRelatorio().getIdentificacao(), cnpjFilial, cnpjIndustriaParaArquivo, dataHora);
            FileGenerator.gerarArquivo(linhas, caminhoExportacao + "/" + nomeArquivo);
            long tempoArquivo = System.currentTimeMillis() - inicio;
            NeogridLogFactory.logGeracaoArquivo(nomeArquivo, linhas.size(), tempoArquivo);

            long tempoTotal = System.currentTimeMillis() - inicioTotal;
            NeogridLogFactory.logPerformance("TOTAL - Geração completa", tempoTotal,
                String.format("Registros: %d | Busca: %d ms | Processamento: %d ms | Arquivo: %d ms",
                    dados.size(), tempoBusca, tempoProcessamento, tempoArquivo));

            NeogridLogFactory.finalizarLog(getTipoRelatorio().getIdentificacao(), true, linhas.size() - 1);
            return nomeArquivo;
        } catch (br.com.petkids.neogrid.exception.NeogridValidationException | NeogridServiceException | NeogridFileException e) {
            NeogridLogFactory.logErro("Erro ao gerar arquivo", e);
            throw e;
        } catch (Exception e) {
            NeogridLogFactory.logErro("Erro inesperado ao gerar arquivo", e);
            throw new NeogridServiceException("Erro ao gerar arquivo: " + e.getMessage(), e);
        }
    }

    private String gerarLinhaNotaFiscal(VendaDTO venda) {
        try {
            String serieNF = NeogridFormatter.formatarAlfanumerico(venda.getSerieNF(), 3);
            if (serieNF == null || serieNF.isEmpty()) serieNF = "001";
            BigDecimal diasPag = venda.getDiasPagamento() != null ? venda.getDiasPagamento() : BigDecimal.ZERO;
            BigDecimal metodoVenda = venda.getMetodoVenda() != null ? venda.getMetodoVenda() : BigDecimal.ONE;
            String ufEmissor = venda.getUfEmissor();
            ufEmissor = formatarUF(ufEmissor);
            if (ufEmissor == null || ufEmissor.trim().isEmpty() || !ufEmissor.matches("^[A-Z]{2}$")) {
                NeogridLogFactory.logErro("UF Emissor Mercadoria inválido ou vazio para nota " + venda.getNumeroNF() + " - usando padrão DF", null);
                ufEmissor = "DF";
            }
            String ufDestinatario = formatarUF(venda.getUfDestinatario());
            if (ufDestinatario == null || ufDestinatario.trim().isEmpty() || !ufDestinatario.matches("^[A-Z]{2}$")) {
                ufDestinatario = "DF";
            }
            return NeogridFormatter.criarLinha(NeogridConstants.TIPO_REGISTRO_DADOS,
                venda.getTipoFaturamento() != null ? venda.getTipoFaturamento().getValor() : "01",
                NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(venda.getNumeroNF()), 20), serieNF,
                venda.getTipoNF() != null ? venda.getTipoNF().getValor() : "01",
                venda.getDataEmissao() != null ? NeogridFormatter.formatarDataHora(venda.getDataEmissao()) : "",
                NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(venda.getCodigoVendedor()), 20),
                NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(venda.getCodigoCliente()), 20),
                ufEmissor, formatarCEP(venda.getCepEmissor()),
                ufDestinatario, formatarCEP(venda.getCepDestinatario()),
                venda.getTipoFrete() != null ? venda.getTipoFrete().getValor() : TipoFrete.FOB.getValor(),
                NeogridFormatter.formatarNumeroComZeros(diasPag.longValue(), 3),
                NeogridFormatter.formatarNumeroComZeros(metodoVenda.longValue(), 2));
        } catch (Exception e) {
            NeogridLogFactory.logErro("Erro ao gerar linha de nota fiscal", e);
            return null;
        }
    }

    private String gerarLinhaItem(ItemVendaDTO item) {
        try {
            String serieNF = NeogridFormatter.formatarAlfanumerico(item.getSerieNF(), 3);
            if (serieNF == null || serieNF.isEmpty()) serieNF = "001";
            return NeogridFormatter.criarLinha(NeogridConstants.TIPO_REGISTRO_ITENS,
                NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(item.getNumeroNF()), 20), serieNF,
                item.getTipoNF() != null ? item.getTipoNF().getValor() : "01",
                NeogridFormatter.formatarAlfanumerico(StringUtils.getNullAsEmpty(item.getCodigoItem()), 20),
                formatarDecimal(item.getQuantidade(), 5), formatarDecimal(item.getValorUnitario(), 2),
                (item.getBonificacao() != null && !item.getBonificacao().isEmpty()) ? item.getBonificacao() : "N",
                formatarDecimal(item.getValorTotalBruto(), 2), formatarDecimal(item.getValorTotalLiquido(), 2),
                formatarDecimal(item.getValorIPI(), 2), formatarDecimal(item.getValorPisConfins(), 2),
                formatarDecimal(item.getValorSubstituicaoTributaria(), 2), formatarDecimal(item.getValorICMS(), 2),
                formatarDecimal(item.getValorDescontos(), 2));
        } catch (Exception e) {
            NeogridLogFactory.logErro("Erro ao gerar linha de item", e);
            return null;
        }
    }

    private String formatarDecimal(BigDecimal valor, int casas) {
        return NeogridFormatter.formatarDecimal((valor != null ? valor : BigDecimal.ZERO).doubleValue(), casas);
    }

    public String gerarArquivo(String cnpjFilial, String cnpjIndustria, String cnpjDestinatario,
            String caminhoExportacao, java.util.Date periodoIni, java.util.Date periodoFin)
            throws NeogridServiceException, NeogridFileException {
        return gerarArquivoBase(cnpjFilial, cnpjIndustria, cnpjDestinatario, caminhoExportacao, periodoIni, periodoFin);
    }
}
