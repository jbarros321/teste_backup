package br.com.satyacode.primeiro_curso;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class LancamentoCabecalhoBT implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        System.out.println("LancamentoCabecalhoBT - Inicio");

        BigDecimal codCurso = BigDecimalUtil.valueOf((String)contexto.getParam("P_CODCUR"));
        Timestamp dataInciial = (Timestamp)contexto.getParam("P_DTINICIO");
        Timestamp dataFinal = (Timestamp)contexto.getParam("P_DTFIM");
        String codParceiro = (String)contexto.getParam("P_CODPARC");

        System.out.println("Infomracao do Parametro de Parceiro: " + codParceiro);

        if (StringUtils.isEmpty(codParceiro)){
            contexto.mostraErro("O Parametro 'P_CODPARC' não pode ser nulo ");
        }

        BigDecimal codParceiroBigDecimal = null;
        if (StringUtils.isNotEmpty(codParceiro)){
            codParceiroBigDecimal = BigDecimalUtil.valueOf(codParceiro);
        }

        Registro cabecalho = contexto.novaLinha("AD_TRECAB");
        cabecalho.setCampo("CODCUR", codCurso);
        cabecalho.setCampo("DTINICIO", dataInciial );
        cabecalho.setCampo("DTFIM", dataFinal );
        cabecalho.setCampo("CODUSU",  contexto.getUsuarioLogado());
        cabecalho.setCampo("CODPARC", codParceiroBigDecimal);
        cabecalho.save();

        Object nrounico = cabecalho.getCampo("NROUNICO");

        StringBuilder mensagem = new StringBuilder();
        mensagem.append("Lançamento realizado com sucesso!").append(" Nro. Unico: ").append(nrounico);

        contexto.setMensagemRetorno("Lancamento realizado com sucesso!  Nro Unico: " + nrounico);

        System.out.println("LancamentoCabecalhoBT - Fim");
    }
}
