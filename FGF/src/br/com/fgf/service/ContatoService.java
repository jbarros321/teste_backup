package br.com.fgf.service;

import br.com.fgf.repository.ContatoRepository;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class ContatoService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private final ContatoRepository repository;

    public ContatoService() {
        this.repository = new ContatoRepository();
    }

    public ResultadoProcessamento processarContatos(BigDecimal codparc, String contatos) throws Exception {
        if (codparc == null) {
            throw new IllegalArgumentException("CODPARC não pode ser nulo");
        }

        if (StringUtils.isEmpty(contatos)) {
            throw new IllegalArgumentException("CONTATOS não pode ser vazio");
        }

        if (!contatos.contains(br.com.fgf.util.Constants.SEPARADOR_EMAIL)) {
            throw new IllegalArgumentException("O campo CONTATOS deve conter o separador '" + br.com.fgf.util.Constants.SEPARADOR_EMAIL + "' para separar os campos de cada contato.");
        }

        Set<String> emailsExistentes = repository.buscarEmailsExistentes(codparc);
        
        String[] linhas = contatos.split("\\r?\\n");
        int contadorCriados = 0;
        int contadorIgnorados = 0;
        int linhaNumero = 0;
        StringBuilder mensagens = new StringBuilder(200);

        for (String linha : linhas) {
            linhaNumero++;
            String linhaTrim = linha.trim();
            
            if (linhaTrim.isEmpty()) {
                continue;
            }

            if (!linhaTrim.contains(br.com.fgf.util.Constants.SEPARADOR_EMAIL)) {
                mensagens.append("\nLinha ").append(linhaNumero).append(" ignorada: não contém o separador '").append(br.com.fgf.util.Constants.SEPARADOR_EMAIL).append("'");
                contadorIgnorados++;
                continue;
            }

            String[] campos = linhaTrim.split(br.com.fgf.util.Constants.SEPARADOR_EMAIL);
            if (campos.length == 0) {
                mensagens.append("\nLinha ").append(linhaNumero).append(" ignorada: nenhum campo encontrado");
                contadorIgnorados++;
                continue;
            }

            String email = campos[0].trim();
            if (!isEmailValido(email)) {
                mensagens.append("\nLinha ").append(linhaNumero).append(" ignorada: e-mail inválido (").append(email).append(")");
                contadorIgnorados++;
                continue;
            }

            String emailUpper = email.toUpperCase();
            if (emailsExistentes.contains(emailUpper)) {
                mensagens.append("\nLinha ").append(linhaNumero).append(" ignorada: e-mail já existe (").append(email).append(")");
                contadorIgnorados++;
                continue;
            }

            String nomeContato = campos.length > 1 && !StringUtils.isEmpty(campos[1].trim()) 
                ? campos[1].trim() 
                : email;

            try {
                BigDecimal codcontato = repository.obterProximoCodContato();
                repository.criarContato(codcontato, codparc, email, nomeContato);
                emailsExistentes.add(emailUpper);
                contadorCriados++;
            } catch (Exception e) {
                contadorIgnorados++;
                mensagens.append("\nLinha ").append(linhaNumero).append(" - Erro ao criar contato: ").append(e.getMessage());
            }
        }

        if (contadorCriados == 0 && contadorIgnorados == 0) {
            return new ResultadoProcessamento(0, 0, "Nenhuma linha válida encontrada no campo CONTATOS.");
        }

        String mensagem = String.format("Processamento concluído: %d contato(s) criado(s), %d ignorado(s).%s", 
            contadorCriados, contadorIgnorados, mensagens.toString());
        
        return new ResultadoProcessamento(contadorCriados, contadorIgnorados, mensagem);
    }

    private boolean isEmailValido(String email) {
        return Optional.ofNullable(email)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .filter(s -> EMAIL_PATTERN.matcher(s).matches())
            .isPresent();
    }

    public static class ResultadoProcessamento {
        private final int contadoresCriados;
        private final int contadoresIgnorados;
        private final String mensagem;

        public ResultadoProcessamento(int contadoresCriados, int contadoresIgnorados, String mensagem) {
            this.contadoresCriados = contadoresCriados;
            this.contadoresIgnorados = contadoresIgnorados;
            this.mensagem = mensagem;
        }

        public int getContadoresCriados() { return contadoresCriados; }
        public int getContadoresIgnorados() { return contadoresIgnorados; }
        public String getMensagem() { return mensagem; }
    }
}
