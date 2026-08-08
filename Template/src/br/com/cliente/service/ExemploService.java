package br.com.cliente.service;

import br.com.cliente.model.dto.ExemploDTO;
import br.com.cliente.repository.ExemploRepository;
import br.com.cliente.util.FileGenerator;
import br.com.cliente.util.Formatter;
import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExemploService implements Service {
    private final ExemploRepository repository = new ExemploRepository();

    @Override
    public String gerarArquivo(String caminhoExportacao, BigDecimal nunota) throws Exception {
        Set<ExemploDTO> dados = repository.buscarDadosPorNunota(nunota);
        return Optional.of(dados).filter(d -> !d.isEmpty()).map(d -> {
            String cnpj = d.stream().findFirst().map(ExemploDTO::getCnpj).orElse("");
            Set<String> linhas = d.stream().map(this::gerarLinha).collect(Collectors.toCollection(() -> new LinkedHashSet<>(1024)));
            String caminhoCompleto = caminhoExportacao + File.separator + FileGenerator.gerarNomeArquivo("EXEMPLO", cnpj);
            try { FileGenerator.gerarArquivo(linhas, caminhoCompleto); return caminhoCompleto; }
            catch (Exception e) { throw new RuntimeException(e); }
        }).orElseThrow(() -> new Exception("Nenhum dado encontrado para os parâmetros informados."));
    }

    private String gerarLinha(ExemploDTO dto) {
        return Stream.of(
            Optional.ofNullable(dto.getCnpj()).orElse(""),
            Optional.ofNullable(dto.getCodigoProduto()).orElse(""),
            Optional.ofNullable(dto.getDescricao()).orElse(""),
            Optional.ofNullable(dto.getDataEmissao()).map(Formatter::formatarData).map(String::trim).orElse("")
        ).collect(Collectors.joining("|")); }
}
