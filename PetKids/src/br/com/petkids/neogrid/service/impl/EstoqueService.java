package br.com.petkids.neogrid.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import br.com.petkids.neogrid.exception.NeogridFileException;
import br.com.petkids.neogrid.exception.NeogridServiceException;
import br.com.petkids.neogrid.model.dto.EstoqueDTO;
import br.com.petkids.neogrid.model.enums.TipoRelatorio;
import br.com.petkids.neogrid.repository.EstoqueRepository;
import br.com.petkids.neogrid.util.FileGenerator;
import br.com.petkids.neogrid.util.NeogridFormatter;
import br.com.petkids.neogrid.util.NeogridConstants;
import br.com.petkids.neogrid.util.NeogridLogFactory;
import br.com.petkids.neogrid.validation.NeogridValidator;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

public class EstoqueService extends AbstractNeogridService {
    private static final TipoRelatorio TIPO_RELATORIO = TipoRelatorio.ESTOQUE;
    private EstoqueRepository repository = new EstoqueRepository();

    @Override
    protected boolean precisaCnpjIndustria() {
        return true;
    }

    @Override
    protected TipoRelatorio getTipoRelatorio() { return TIPO_RELATORIO; }

    @Override
    protected List<?> buscarDados(String cnpjFilial, String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) throws Exception {
        NeogridValidator.validarParametrosComIndustria(TIPO_RELATORIO.getIdentificacao(), cnpjFilial, cnpjIndustria);
        return repository.buscarEstoque(cnpjIndustria, periodoIni, periodoFin);
    }

    @Override
    protected String gerarLinhaDados(Object dto) {
        try {
            EstoqueDTO estoque = (EstoqueDTO) dto;
            String codigoItem = estoque.getCodigoItem();
            if (codigoItem == null || codigoItem.trim().isEmpty()) {
                NeogridLogFactory.logErro("Código de item é obrigatório no estoque - registro será ignorado", null);
                return null;
            }
            String codigoItemLimpo = codigoItem.replaceAll("[^0-9]", "");
            if (codigoItemLimpo.isEmpty() || !codigoItemLimpo.matches("^[0-9]+$")) {
                NeogridLogFactory.logErro("Código de item inválido (deve conter apenas números): " + codigoItem + " - registro será ignorado", null);
                return null;
            }
            codigoItem = codigoItemLimpo;
            if (codigoItem.length() > 20) codigoItem = codigoItem.substring(0, 20);
            BigDecimal qtdEstoque = estoque.getQuantidadeEstoque() != null ? estoque.getQuantidadeEstoque().abs() : BigDecimal.ZERO;
            BigDecimal qtdTransito = estoque.getQuantidadeEstoqueTransito() != null ? estoque.getQuantidadeEstoqueTransito().abs() : BigDecimal.ZERO;
            Timestamp dataHoraEstoque = estoque.getDataHoraEstoque() != null ? estoque.getDataHoraEstoque() : TimeUtils.getNow();
            return NeogridFormatter.criarLinha(NeogridConstants.TIPO_REGISTRO_DADOS,
                NeogridFormatter.formatarDataHora(dataHoraEstoque), codigoItem,
                NeogridFormatter.formatarDecimal(qtdEstoque.doubleValue(), 2),
                NeogridFormatter.formatarDecimal(qtdTransito.doubleValue(), 2));
        } catch (Exception e) {
            NeogridLogFactory.logErro("Erro ao gerar linha de estoque", e);
            return null;
        }
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
            List<EstoqueDTO> estoques = repository.buscarEstoque(null, periodoIni, periodoFin);
            for (EstoqueDTO estoque : estoques) {
                String cnpjInd = estoque.getCnpjIndustria();
                if (cnpjInd != null && !cnpjInd.isEmpty()) {
                    String cnpjFormatado = NeogridFormatter.formatarCnpjCpf(cnpjInd);
                    if (cnpjFormatado != null && !cnpjFormatado.isEmpty() && cnpjFormatado.length() == 14) return cnpjFormatado;
                }
            }
            String cnpjBusca = repository.buscarPrimeiroCnpjIndustria(periodoIni, periodoFin);
            if (cnpjBusca != null && !cnpjBusca.isEmpty()) {
                cnpjBusca = NeogridFormatter.formatarCnpjCpf(cnpjBusca);
                if (cnpjBusca != null && !cnpjBusca.isEmpty() && cnpjBusca.length() == 14) return cnpjBusca;
            }
            throw new NeogridServiceException("Não foi possível determinar o CNPJ da indústria para o relatório de estoque.");
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
            List<String> linhas = new java.util.ArrayList<>(1000);
            Timestamp dataHora = TimeUtils.getNow();
            Timestamp periodoIniTS = periodoIni != null ? new Timestamp(periodoIni.getTime()) : null;
            Timestamp periodoFinTS = periodoFin != null ? new Timestamp(periodoFin.getTime()) : null;

            inicio = System.currentTimeMillis();
            String cnpjIndustriaFormatado = obterCnpjIndustria(cnpjIndustria, () -> {
                List<EstoqueDTO> estoques = repository.buscarEstoque(null, periodoIniTS, periodoFinTS);
                for (EstoqueDTO estoque : estoques) {
                    String cnpjInd = estoque.getCnpjIndustria();
                    if (cnpjInd != null && !cnpjInd.isEmpty()) {
                        String cnpj = NeogridFormatter.formatarCnpjCpf(cnpjInd);
                        if (cnpj != null && !cnpj.isEmpty() && cnpj.length() == 14) return cnpj;
                    }
                }
                String cnpj = repository.buscarPrimeiroCnpjIndustria(periodoIniTS, periodoFinTS);
                if (cnpj != null && !cnpj.isEmpty()) {
                    cnpj = NeogridFormatter.formatarCnpjCpf(cnpj);
                    if (cnpj != null && !cnpj.isEmpty() && cnpj.length() == 14) return cnpj;
                }
                throw new NeogridServiceException("Não foi possível determinar o CNPJ da indústria para o relatório de estoque.");
            });
            String cnpjDestinatarioFinal = normalizarCnpjKelcoParaPrincipal(cnpjIndustriaFormatado);
            String cnpjDestinatarioLimpo = NeogridFormatter.formatarCnpjCpf(cnpjDestinatarioFinal);
            if (cnpjDestinatarioLimpo != null && cnpjDestinatarioLimpo.equals(NeogridConstants.CNPJ_NEOGRID)) {
                throw new NeogridServiceException("CNPJ destinatário não pode ser o CNPJ da Neogrid. Deve ser o CNPJ da indústria para o relatório de estoque.");
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
            for (Object dto : dados) {
                String linha = gerarLinhaDados(dto);
                if (linha != null) {
                    linhas.add(linha);
                    linhasProcessadas++;
                }
            }
            long tempoProcessamento = System.currentTimeMillis() - inicio;
            NeogridLogFactory.logProcessamentoDados("Geração de linhas", linhasProcessadas, tempoProcessamento);

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
        } catch (NeogridServiceException | NeogridFileException e) {
            NeogridLogFactory.logErro("Erro ao gerar arquivo", e);
            throw e;
        } catch (Exception e) {
            NeogridLogFactory.logErro("Erro inesperado ao gerar arquivo", e);
            throw new NeogridServiceException("Erro ao gerar arquivo: " + e.getMessage(), e);
        }
    }

    public String gerarArquivo(String cnpjFilial, String cnpjIndustria, String cnpjDestinatario,
            String caminhoExportacao, java.util.Date periodoIni, java.util.Date periodoFin)
            throws NeogridServiceException, NeogridFileException {
        return gerarArquivoBase(cnpjFilial, cnpjIndustria, cnpjDestinatario, caminhoExportacao, periodoIni, periodoFin);
    }
}
