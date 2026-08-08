package br.com.sankhya.flow.modelos.comercial.pedidoVenda;

import java.math.BigDecimal;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.extensions.flow.ContextoTarefa;
import br.com.sankhya.extensions.flow.TarefaJava;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;

import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

public class ServiceTaskConfirmaPedido implements TarefaJava {

	public void executar(ContextoTarefa contexto) throws Exception {

		BigDecimal nuNota = BigDecimalUtil.getBigDecimal(contexto.getCampo("NUNOTA"));

		if (nuNota ==  null) {

			throw new IllegalStateException("Nmero da nota no preenchido.");

		} else {
			try{
				BarramentoRegra bRegras = BarramentoRegra.build(CACHelper.class, "regrasConfirmacaoCAC.xml", AuthenticationInfo.getCurrent());
				ConfirmacaoNotaHelper.confirmarNota(nuNota, bRegras);
			} catch(Exception e){
				Registro pendencia = contexto.novaLinha("AD_PENDENCIASVENDA");
				pendencia.setCampo("IDINSTPRN", contexto.getIdInstanceProcesso());
				pendencia.setCampo("IDINSTTAR", BigDecimalUtil.ZERO_VALUE);
				pendencia.setCampo("TIPO", "C");
				pendencia.setCampo("DETALHESERRO", "Erro ao tentar confirmar:\n\n" + StringUtils.getNullAsEmpty(e.getMessage()));
				pendencia.save();
			}
		}

	}
}
