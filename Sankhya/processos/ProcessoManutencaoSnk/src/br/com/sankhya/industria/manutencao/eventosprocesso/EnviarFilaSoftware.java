package br.com.sankhya.industria.manutencao.eventosprocesso;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoEvento;
import br.com.sankhya.extensions.flow.EventoProcessoJava;
import br.com.sankhya.industria.manutencao.util.ManutencaoConstants;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;

public class EnviarFilaSoftware implements EventoProcessoJava {
	public void executar(ContextoEvento ctx) throws Exception {
		Registro [] linhas = ctx.getLinhasFormulario("AD_TWFSOLIMAN");

		if (linhas.length > 0) {
			Registro linha = linhas[0];

			BigDecimal celula = (BigDecimal) linha.getCampo("CODCELPROD");
			BigDecimal produto = (BigDecimal) linha.getCampo("CODPROD");
			BigDecimal grupoProduto = NativeSql.getBigDecimal("CODGRUPOPROD", "TGFPRO", "CODPROD = ?", new Object[] { produto });
			BigDecimal proxFila = null;

			if(celula.equals(new BigDecimal(25)) || celula.equals(new BigDecimal(65)) || celula.equals(new BigDecimal(113)) ) {
				proxFila = ManutencaoConstants.FILA_SOFTWARE_PDV;
			}

			else if(celula.equals(new BigDecimal(8)) || celula.equals(new BigDecimal(21)) ) {
					if (grupoProduto.equals(ManutencaoConstants.GRU_LINHA_G_JV) || grupoProduto.equals(ManutencaoConstants.GRU_LINHA_G_SK)) {
						proxFila = ManutencaoConstants.FILA_SOFTWARE_RH;
					}else {
						proxFila = ManutencaoConstants.FILA_SOFTWARE_PESSOAL_PLUS;
					}
			}else {
				proxFila = ManutencaoConstants.FILA_SOFTWARE;
			}

			BigDecimal numOS = (BigDecimal) linha.getCampo("NUMOS");
			BigDecimal codUsu = ctx.getUsuarioLogado();
			BigDecimal numItem = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);

			Map<String, Object> props = new HashMap<String, Object>();
			props.put("CODSERV", new BigDecimal(50506));
			props.put("CODSIT", new BigDecimal(1));

			OrdemServicoAPI.encaminhaFila(numOS, numItem, codUsu, proxFila, props);
		}
	}
}
