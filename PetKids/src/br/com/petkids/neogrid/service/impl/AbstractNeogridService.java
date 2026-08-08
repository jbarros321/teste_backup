package br.com.petkids.neogrid.service.impl;

import br.com.petkids.neogrid.exception.NeogridFileException;
import br.com.petkids.neogrid.exception.NeogridServiceException;
import br.com.petkids.neogrid.model.enums.TipoRelatorio;
import br.com.petkids.neogrid.util.FileGenerator;
import br.com.petkids.neogrid.util.NeogridConstants;
import br.com.petkids.neogrid.util.NeogridFormatter;
import br.com.petkids.neogrid.util.NeogridLogFactory;
import br.com.petkids.neogrid.validation.NeogridValidator;
import com.sankhya.util.TimeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import java.sql.Timestamp;
import java.util.List;

public abstract class AbstractNeogridService {
    protected abstract TipoRelatorio getTipoRelatorio();
    protected abstract List<?> buscarDados(String cnpjFilial, String cnpjIndustria, Timestamp periodoIni, Timestamp periodoFin) throws Exception;
    protected abstract String gerarLinhaDados(Object dto) throws Exception;
    protected abstract String gerarCabecalho(String cnpjFilial, String cnpjDestinatario, Timestamp dataHora, Timestamp periodoIni, Timestamp periodoFin);

    protected String gerarArquivoBase(String cnpjFilial, String cnpjIndustria, String cnpjDestinatario,
            String caminhoExportacao, java.util.Date periodoIni, java.util.Date periodoFin)
            throws NeogridServiceException, NeogridFileException {
        try {
            caminhoExportacao = NeogridValidator.validarCaminhoExportacao(caminhoExportacao);
            List<String> linhas = new java.util.ArrayList<>();
            Timestamp dataHora = TimeUtils.getNow();
            Timestamp periodoIniTS = periodoIni != null ? new Timestamp(periodoIni.getTime()) : null;
            Timestamp periodoFinTS = periodoFin != null ? new Timestamp(periodoFin.getTime()) : null;
            String cnpjDestinatarioFinal = obterCnpjDestinatarioFinal(cnpjIndustria, cnpjDestinatario, periodoIniTS, periodoFinTS);
            NeogridValidator.validarCnpjDestinatario(cnpjDestinatarioFinal, precisaCnpjIndustria());
            linhas.add(gerarCabecalho(cnpjFilial, cnpjDestinatarioFinal, dataHora, periodoIniTS, periodoFinTS));
            List<?> dados = buscarDados(cnpjFilial, cnpjIndustria, periodoIniTS, periodoFinTS);
            NeogridValidator.validarDadosDisponiveis(dados.size(), getTipoRelatorio().getIdentificacao());
            for (Object dto : dados) {
                String linha = gerarLinhaDados(dto);
                if (linha != null) linhas.add(linha);
            }
            String nomeArquivo = FileGenerator.gerarNomeArquivo(getTipoRelatorio().getIdentificacao(), cnpjFilial, null, dataHora);
            FileGenerator.gerarArquivo(linhas, caminhoExportacao + "/" + nomeArquivo);
            return nomeArquivo;
        } catch (NeogridServiceException | NeogridFileException e) {
            throw e;
        } catch (Exception e) {
            throw new NeogridServiceException("Erro ao gerar arquivo: " + ExceptionUtils.getMessage(e), e);
        }
    }

    protected boolean precisaCnpjIndustria() {
        return false;
    }

    protected String obterCnpjDestinatarioFinal(String cnpjIndustria, String cnpjDestinatario, Timestamp periodoIni, Timestamp periodoFin) {
        return cnpjDestinatario;
    }

    protected String gerarNumeroRelatorio() {
        return String.format("REL%015d", System.currentTimeMillis() % 1000000000000L);
    }

    protected String formatarUF(String uf) {
        if (uf == null || uf.trim().isEmpty()) return "DF";
        uf = uf.trim().toUpperCase().replaceAll("\\s+", "");
        if (uf.matches("^[0-9]+$") || uf.length() != 2) return "DF";
        if (uf.length() > 2) uf = uf.substring(0, 2);
        if (uf.matches("^[A-Z]{2}$")) return uf;
        return "DF";
    }

    protected String formatarCEP(String cep) {
        if (cep == null) return "00000000";
        cep = NeogridFormatter.formatarCnpjCpf(cep);
        if (cep.length() > 8) return cep.substring(0, 8);
        if (cep.length() < 8) {
            try {
                return String.format("%08d", Long.parseLong(cep.isEmpty() ? "0" : cep));
            } catch (NumberFormatException e) {
                return "00000000";
            }
        }
        return cep;
    }

    protected String obterCnpjIndustria(String cnpjIndustria, java.util.function.Supplier<String> buscarPrimeiro) {
        if (cnpjIndustria != null && !cnpjIndustria.isEmpty()) {
            String cnpj = NeogridFormatter.formatarCnpjCpf(cnpjIndustria);
            if (cnpj != null && !cnpj.isEmpty() && cnpj.length() == 14) return cnpj;
        }
        try {
            String cnpj = buscarPrimeiro.get();
            if (cnpj == null || cnpj.isEmpty()) throw new NeogridServiceException("Não foi possível determinar o CNPJ da indústria.");
            cnpj = NeogridFormatter.formatarCnpjCpf(cnpj);
            if (cnpj == null || cnpj.isEmpty() || cnpj.length() != 14) throw new NeogridServiceException("CNPJ da indústria inválido: " + cnpj);
            return cnpj;
        } catch (NeogridServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new NeogridServiceException("Erro ao buscar CNPJ da indústria: " + e.getMessage(), e);
        }
    }

    protected String gerarCabecalhoPadrao(TipoRelatorio tipoRelatorio, String cnpjFilial, String cnpjDestinatario, Timestamp dataHora) {
        return NeogridFormatter.criarLinha(NeogridConstants.TIPO_REGISTRO_CABECALHO, tipoRelatorio.getIdentificacao(),
            tipoRelatorio.getVersao(), gerarNumeroRelatorio(), NeogridFormatter.formatarDataHora(dataHora),
            NeogridFormatter.formatarCnpjCpf(cnpjFilial), NeogridFormatter.formatarCnpjCpf(cnpjDestinatario));
    }
}
