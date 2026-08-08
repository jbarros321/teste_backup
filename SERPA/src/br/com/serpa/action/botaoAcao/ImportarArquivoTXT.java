package br.com.serpa.action.botaoAcao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import br.com.serpa.shared.SerpaTXTHelper;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.EntityFacadeFactory;

public class ImportarArquivoTXT implements AcaoRotinaJava {

    private EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
    private static final String TABELA_LOG = "AD_SERPALOG";

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        try {

            String caminhoArquivo = obterCaminhoArquivo(contexto);

            if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
                contexto.setMensagemRetorno(
                    "Caminho do arquivo não informado. " +
                    "Informe o parâmetro 'CAMINHO_ARQUIVO' na configuração do botão.");
                return;
            }

            File arquivo = new File(caminhoArquivo);
            if (!arquivo.exists() || !arquivo.isFile()) {
                contexto.setMensagemRetorno("Arquivo não encontrado: " + caminhoArquivo);
                return;
            }

            if (!caminhoArquivo.toLowerCase().endsWith(".txt")) {
                contexto.setMensagemRetorno("Arquivo deve ter extensão .txt");
                return;
            }

            ResultadoImportacao resultado = processarImportacao(arquivo);

            moverParaBackup(arquivo);

            SerpaTXTHelper.registrarLog(TABELA_LOG, "IMPORTACAO",
                resultado.getErros() > 0 ? "ERRO" : "SUCESSO",
                String.format("Arquivo: %s | Sucessos: %d | Erros: %d",
                    arquivo.getName(), resultado.getSucessos(), resultado.getErros()),
                contexto);

            String mensagem = String.format(
                "Importação concluída!\n\n" +
                "Arquivo: %s\n" +
                "Registros processados com sucesso: %d\n" +
                "Registros com erro: %d",
                arquivo.getName(), resultado.getSucessos(), resultado.getErros());

            if (!resultado.getMensagensErro().isEmpty()) {
                mensagem += "\n\nErros encontrados:\n" +
                    String.join("\n", resultado.getMensagensErro());
            }

            contexto.setMensagemRetorno(mensagem);

        } catch (Exception e) {

            try {
                SerpaTXTHelper.registrarLog(TABELA_LOG, "IMPORTACAO", "ERRO",
                    "Erro na importação: " + e.getMessage(), contexto);
            } catch (Exception logError) {
                System.err.println("Erro ao registrar log: " + logError.getMessage());
            }

            contexto.setMensagemRetorno("Erro ao importar arquivo TXT: " + e.getMessage());
            throw new Exception("Erro na importação: " + e.getMessage(), e);
        }
    }

    private String obterCaminhoArquivo(ContextoAcao contexto) {

        Object param = contexto.getParam("CAMINHO_ARQUIVO");
        if (param != null) {
            return param.toString();
        }

        return null;
    }

    private ResultadoImportacao processarImportacao(File arquivo) throws Exception {
        ResultadoImportacao resultado = new ResultadoImportacao();

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            int numeroLinha = 0;

            while ((linha = reader.readLine()) != null) {
                numeroLinha++;

                if (linha.trim().isEmpty()) {
                    continue;
                }

                try {

                    SerpaTXTHelper.processarLinhaTXT(linha, numeroLinha, dwfFacade);
                    resultado.incrementarSucesso();

                } catch (Exception e) {
                    resultado.adicionarErro("Linha " + numeroLinha + ": " + e.getMessage());
                    resultado.incrementarErro();
                }
            }

        } catch (IOException e) {
            throw new Exception("Erro ao ler arquivo: " + e.getMessage(), e);
        }

        return resultado;
    }

    private void moverParaBackup(File arquivo) {
        try {
            String backupDir = System.getProperty("java.io.tmpdir") + "/serpa_import/backup";
            File diretorioBackup = new File(backupDir);
            if (!diretorioBackup.exists()) {
                diretorioBackup.mkdirs();
            }

            File destino = new File(diretorioBackup,
                arquivo.getName() + "_" + System.currentTimeMillis());

            Files.move(arquivo.toPath(), destino.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {

            System.err.println("Erro ao mover arquivo para backup: " + e.getMessage());
        }
    }

    private static class ResultadoImportacao {
        private int sucessos = 0;
        private int erros = 0;
        private List<String> mensagensErro = new ArrayList<>();

        public void incrementarSucesso() {
            sucessos++;
        }

        public void incrementarErro() {
            erros++;
        }

        public void adicionarErro(String mensagem) {
            mensagensErro.add(mensagem);
        }

        public int getSucessos() {
            return sucessos;
        }

        public int getErros() {
            return erros;
        }

        public List<String> getMensagensErro() {
            return mensagensErro;
        }
    }
}
