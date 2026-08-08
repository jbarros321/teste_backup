package br.com.sankhya.mgeserv.integracao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.sun.jmx.snmp.Timestamp;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;

public class IncluirOS implements AcaoRotinaJava {

	public void doAction(ContextoAcao contexto) throws Exception {
		Map<String, Object> cabecalho = new HashMap<String, Object>();
		Map<String, Object> item = new HashMap<String, Object>();

		cabecalho.put("CODPARC", new BigDecimal(9217));
		cabecalho.put("NUMCONTRATO", new BigDecimal(2307));
		cabecalho.put("DHCHAMADA", new Timestamp(System.currentTimeMillis()));
		cabecalho.put("CODCONTATO",  new BigDecimal(11));
		cabecalho.put("DESCRICAO", "Essa OS foi incluida para testar a integrao do mdulo de OS.");

		item.put("CODPROD", new BigDecimal(30611));
		item.put("PRIORIDADE", new BigDecimal(2));
		item.put("CODSERV", new BigDecimal(50605));
		item.put("CODOCOROS", new BigDecimal(6));
        item.put("CODSIT", new BigDecimal(3));
        item.put("COBRAR", "N");
        item.put("RETRABALHO", "S");
        item.put("DHPREVISTA",  new Timestamp(System.currentTimeMillis()));
        item.put("SOLUCAO", "Item inicial");

        BigDecimal codUsuLogado = new BigDecimal(1063);
        BigDecimal codExecutante = new BigDecimal(46);
        BigDecimal numOS = OrdemServicoAPI.incluirOrdemServico(cabecalho, item, codUsuLogado, codExecutante);

        contexto.setMensagemRetorno("OS: " + numOS);
	}

}
