package br.com.sankhya.flow.tools.migracao.actbtn;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.flow.tools.migracao.MigracaoUtil;

public class MigrarInstancias implements AcaoRotinaJava {

	public void doAction(ContextoAcao ctx) throws Exception {
		Registro plano = ctx.getLinhaPai();

		if(plano == null){
			for(Registro registro:ctx.getLinhas()){
				migraPlano(registro, null);
			}
		} else {
			List<String> instancias = new ArrayList<String>();
			for(Registro registro:ctx.getLinhas()){
				instancias.add(registro.getCampo("IDINSTPRN").toString());
			}
			migraPlano(plano, instancias);
		}
	}

	private void migraPlano(Registro plano, List<String> instancias) throws Exception{
		BigDecimal nuPlano = (BigDecimal) plano.getCampo("NUPLANO");
		BigDecimal codPrnAtual = (BigDecimal) plano.getCampo("CODPRNATUAL");
		BigDecimal versaoAtual = (BigDecimal) plano.getCampo("VERSAOATUAL");
		BigDecimal codPrnNovo = (BigDecimal) plano.getCampo("CODPRNNOVO");
		BigDecimal versaoNovo =  (BigDecimal) plano.getCampo("VERSAONOVO");

		if(codPrnAtual.compareTo(codPrnNovo) != 0){
			throw new IllegalStateException("Ainda no preparado pra migrar de processo");
		}
		if(instancias == null){
			MigracaoUtil.migrarInstancias(nuPlano, codPrnAtual, versaoAtual, codPrnNovo, versaoNovo);
		} else {
			MigracaoUtil.migrarInstancias(nuPlano, codPrnAtual, versaoAtual, codPrnNovo, versaoNovo, instancias);
		}
	}

}
