package br.com.cliente.action.botaoAcao;
import br.com.cliente.service.ExemploService;
import br.com.cliente.util.DownloadHelper;
import br.com.cliente.util.Formatter;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import com.sankhya.util.TimeUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class PersonalizacaoSankhya implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        BigDecimal nunota = Optional.ofNullable(contexto.getLinhas()).filter(l -> l.length > 0).map(l -> (BigDecimal) l[0].getCampo("NUNOTA")).orElse(null);
        if (nunota == null) { contexto.setMensagemRetorno("Selecione um registro com NUNOTA."); return; }
        String tmpDir = System.getProperty("java.io.tmpdir");
        new File(tmpDir).mkdirs();
        String caminhoArquivo = new ExemploService().gerarArquivo(tmpDir, nunota);
        Set<String> arquivos = new LinkedHashSet<>(1);
        arquivos.add(caminhoArquivo);
        String nomeZip = "EXEMPLO_" + Formatter.formatarTimestamp(TimeUtils.getNow()) + ".zip";
        String zipRetornado = DownloadHelper.criarZip(arquivos, nomeZip);
        contexto.setMensagemRetorno("Arquivo gerado: " + new File(caminhoArquivo).getName() + "\n\n" + DownloadHelper.gerarScriptDownloadZip(zipRetornado));
    }
}
