package br.com.fgf.action.botaoAcao;

import br.com.fgf.service.ContatoService;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Optional;

public class IncluirContatos implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        try {
            BigDecimal codparc = obterCodparc(contexto);
            if (codparc == null) {
                contexto.setMensagemRetorno("CODPARC não foi informado. Selecione um parceiro ou informe o código como parâmetro.");
                return;
            }

            String contatos = obterContatos(contexto);
            if (StringUtils.isEmpty(contatos)) {
                contexto.setMensagemRetorno("CONTATOS não foi informado. Preencha o campo CONTATOS com os dados para importação.");
                return;
            }

            ContatoService service = new ContatoService();
            ContatoService.ResultadoProcessamento resultado = service.processarContatos(codparc, contatos);
            
            contexto.setMensagemRetorno(resultado.getMensagem());
        } catch (Exception e) {
            String mensagemErro = Optional.ofNullable(e.getMessage())
                .filter(StringUtils::isNotEmpty)
                .orElse(e.getClass().getSimpleName());
            contexto.setMensagemRetorno("Erro ao processar contatos: " + mensagemErro);
            throw e;
        }
    }

    private BigDecimal obterCodparc(ContextoAcao contexto) {
        Object codparcParam = contexto.getParam("CODPARC");
        if (codparcParam != null) {
            return BigDecimalUtil.getBigDecimal(codparcParam);
        }

        if (contexto.getLinhas() != null && contexto.getLinhas().length > 0) {
            Registro linha = contexto.getLinhas()[0];
            Object codparcLinha = linha.getCampo("CODPARC");
            if (codparcLinha != null) {
                return BigDecimalUtil.getBigDecimal(codparcLinha);
            }
        }

        return null;
    }

    private String obterContatos(ContextoAcao contexto) {
        Object contatosParam = contexto.getParam("CONTATOS");
        return Optional.ofNullable(contatosParam)
            .map(Object::toString)
            .map(String::trim)
            .orElse("");
    }
}
