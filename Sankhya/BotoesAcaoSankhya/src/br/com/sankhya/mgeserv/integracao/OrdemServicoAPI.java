package br.com.sankhya.mgeserv.integracao;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

import javax.naming.InitialContext;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import com.sankhya.dao.client.vo.FieldVO;
import com.sankhya.model.apps.servico.OrdemServicoSF;
import com.sankhya.model.apps.servico.OrdemServicoSFHome;
import com.sankhya.model.entities.bmp.CabecalhoLocal;
import com.sankhya.model.entities.bmp.CabecalhoLocalHome;
import com.sankhya.model.entities.bmp.ItemLocal;
import com.sankhya.model.entities.bmp.ItemLocalHome;
import com.sankhya.model.entities.pk.CabecalhoPK;
import com.sankhya.model.entities.pk.ItemPK;
import com.sankhya.model.entities.vo.CabecalhoVO;
import com.sankhya.model.entities.vo.ItemVO;
import com.sankhya.model.util.Constants;
import com.sankhya.util.ServiceLocator;

public class OrdemServicoAPI{

	private static void setFieldValueIfNotNull(FieldVO f, Object value){
		if(value != null){
			f.setData(value);
		}
	}

	public static BigDecimal incluirOrdemServicoPorModelo(BigDecimal osModelo, BigDecimal itemModelo, Map<String, Object> cabecalho, Map<String, Object> item, BigDecimal codUsuLogado, BigDecimal codExecutante) throws Exception{

		JapeSession.SessionHandle hnd = null;
		BigDecimal numOs = null;
		OrdemServicoSF osFacade = null;
		AuthenticationInfo oldAI = AuthenticationInfo.getCurrentOrNull();
		try {
			hnd = JapeSession.open();

			CabecalhoVO newCabVO;
			ItemVO newItemVO;

			if(osModelo == null){
				newCabVO = new CabecalhoVO();
				newItemVO = new ItemVO();
			} else {
				newCabVO = loadCabecalho(numOs);
				newItemVO = loadItem(osModelo, itemModelo);
			}

			newCabVO.descricao.setData(cabecalho.get("DESCRICAO"));

			newCabVO.codatend.setData(codUsuLogado);
			setFieldValueIfNotNull(newCabVO.codatend, cabecalho.get("CODATEND"));

			newCabVO.codusuresp.setData(codUsuLogado);
			setFieldValueIfNotNull(newCabVO.codusuresp, cabecalho.get("CODUSURESP"));

			newCabVO.codususolicitante.setData(codUsuLogado);
			setFieldValueIfNotNull(newCabVO.codususolicitante, cabecalho.get("CODUSUSOLICITANTE"));

			setFieldValueIfNotNull(newCabVO.dtprevista, cabecalho.get("DTPREVISTA"));
			setFieldValueIfNotNull(newCabVO.numcontrato, cabecalho.get("NUMCONTRATO"));
			setFieldValueIfNotNull(newCabVO.nuFap, cabecalho.get("NUFAP"));
			setFieldValueIfNotNull(newCabVO.numEtapa, cabecalho.get("NUMETAPA"));
			setFieldValueIfNotNull(newCabVO.nunota, cabecalho.get("NUNOTA"));
			setFieldValueIfNotNull(newCabVO.tipo, cabecalho.get("TIPO"));
			setFieldValueIfNotNull(newCabVO.codparc, cabecalho.get("CODPARC"));
			setFieldValueIfNotNull(newCabVO.codcontato, cabecalho.get("CODCONTATO"));

			newCabVO.dhchamada.setData(new Timestamp(System.currentTimeMillis()));
			newCabVO.numos.setData(null);
			newCabVO.dtfechamento.setData(null);
			newCabVO.dhFechamentoSLA.setData(null);
			newCabVO.temposla.setData(null);
			newCabVO.possuiSla.setData("N");
			newCabVO.situacao.setData("P");
			newCabVO.codusufech.setData(null);
			newCabVO.nomemodelo.setData(null);

			preparaItemVo(newItemVO, item);
			newItemVO.codusu.setData(codExecutante);

			osFacade = getOsFacade(new AuthenticationInfo("", codUsuLogado, null, 0));

			osFacade.incluirOrdemDeServico(newCabVO, newItemVO);

			CabecalhoVO newOSvo = osFacade.getCabecalhoCorrente();
			osFacade.remove();

			numOs = newOSvo.numos.getData();

		} finally {
			if(osFacade != null){
				osFacade.remove();
			}
			if(oldAI != null){
				oldAI.makeCurrent();
			}
			JapeSession.close(hnd);
		}

		return numOs;
	}

	private static OrdemServicoSF getOsFacade(AuthenticationInfo userAuthentication) throws Exception{
		InitialContext inicialCtx = new InitialContext();
		OrdemServicoSFHome osHome = (OrdemServicoSFHome) inicialCtx.lookup("ejb/OrdemServicoSFRemote");
		return osHome.create(userAuthentication);
	}

	private static void preparaItemVo(ItemVO newItemVO, Map<String, Object> item){
		newItemVO.numos.setData(item.get("NUMOS"));
		newItemVO.numitem.setData(item.get("NUMITEM"));
		newItemVO.numEtapa.setData(item.get("NUMETAPA"));
		newItemVO.dhprevista.setData(new Timestamp(System.currentTimeMillis()));
		setFieldValueIfNotNull(newItemVO.codproj, item.get("CODPROJ"));
		setFieldValueIfNotNull(newItemVO.codusu, item.get("CODUSU"));
		setFieldValueIfNotNull(newItemVO.codserv, item.get("CODSERV"));
		setFieldValueIfNotNull(newItemVO.codprod, item.get("CODPROD"));
		setFieldValueIfNotNull(newItemVO.codSit, item.get("CODSIT"));
		setFieldValueIfNotNull(newItemVO.codocoros, item.get("CODOCOROS"));
		setFieldValueIfNotNull(newItemVO.prioridade, item.get("PRIORIDADE"));
		setFieldValueIfNotNull(newItemVO.solucao, item.get("SOLUCAO"));
		newItemVO.cobrar.setData("S".equals(item.get("COBRAR")));
		newItemVO.retrabalho.setData("S".equals(item.get("RETRABALHO")));

		int codClassificacao = 0;
		if(!newItemVO.cobrar.getData()){
			codClassificacao = newItemVO.retrabalho.getData() ? Constants.NAO_CONFORMIDADE_SANKHYA : Constants.CONFORMIDADE;
		} else if (!newItemVO.retrabalho.getData()){
			codClassificacao = Constants.NAO_CONFORMIDADE_CLIENTE;
		}

		newItemVO.tipConformidade = codClassificacao;
	}

	public static BigDecimal incluirOrdemServico(Map<String, Object> cabecalho, Map<String, Object> item, BigDecimal codUsuLogado, BigDecimal codExecutante) throws Exception{
		return incluirOrdemServicoPorModelo(null, null, cabecalho, item, codUsuLogado, codExecutante);
	}

	private static CabecalhoVO loadCabecalho(BigDecimal numOs) throws Exception{
		CabecalhoLocalHome osLH = (CabecalhoLocalHome) ServiceLocator.getHome(CabecalhoLocalHome.JNDI_NAME, CabecalhoLocalHome.class);
		CabecalhoLocal osL = osLH.findByPrimaryKey(CabecalhoPK.getInstance(numOs.intValue()));
		return (CabecalhoVO) osL.getValueObject();
	}

	private static ItemVO loadItem(BigDecimal numOs, BigDecimal numItem) throws Exception{
		ItemLocalHome itemLH = (ItemLocalHome) ServiceLocator.getHome(ItemLocalHome.JNDI_NAME, ItemLocalHome.class);
		ItemLocal itemL = itemLH.findByPrimaryKey(ItemPK.getInstance(numOs.intValue(), numItem.intValue()));
		return (ItemVO) itemL.getValueObject();
	}

	private static AuthenticationInfo assertAuthentication(BigDecimal executante) throws Exception{

		return new AuthenticationInfo("", executante, null, 0);

	}

	public static void encaminhaFila(BigDecimal numOs, BigDecimal itemFechar, BigDecimal executante, BigDecimal codFila) throws Exception{

		OrdemServicoSF osFacade = null;

		try {

			AuthenticationInfo ai = assertAuthentication(executante);
			ItemVO item = loadItem(numOs, itemFechar);

			item.codusu.setData(codFila);
			osFacade = getOsFacade(ai);
			osFacade.setOrdemServicoAtiva(numOs.intValue());
			osFacade.alterarOrdemDeServico(loadCabecalho(numOs), item);

		} finally{
			if(osFacade != null){
				osFacade.remove();
			}
		}
	}

	public static void tiraDaFila(BigDecimal numOs, BigDecimal codFila, BigDecimal executante) throws Exception{

		OrdemServicoSF osFacade = null;

		try {

			AuthenticationInfo userAuthentication = assertAuthentication(executante);
			BigDecimal itemFila = getItemAbertoFila(numOs, codFila);

			ItemVO item = loadItem(numOs, itemFila);
			item.codusu.setData(executante);
			osFacade = getOsFacade(userAuthentication);
			osFacade.setOrdemServicoAtiva(numOs.intValue());
			osFacade.alterarOrdemDeServico(loadCabecalho(numOs), item);
		} finally{
			if(osFacade != null){
				osFacade.remove();
			}
		}
	}

	private static BigDecimal getItemAbertoFila(BigDecimal numOs, BigDecimal codFila) throws Exception {

		BigDecimal numItem = null;

		NativeSql query = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());
		query.appendSql(" SELECT ");
		query.appendSql("    NUMITEM ");
		query.appendSql(" FROM ");
		query.appendSql(" 	TCSITE ");
		query.appendSql(" WHERE ");
		query.appendSql(" 	NUMOS = :NUMOS AND ");
		query.appendSql(" 	CODUSU = :CODUSU AND ");
		query.appendSql(" 	INICEXEC IS NULL AND ");
		query.appendSql(" 	HRINICIAL IS NULL AND ");
		query.appendSql(" 	HRFINAL IS NULL AND ");
		query.appendSql(" 	EXISTS(SELECT 1 FROM TCSRUS RUS WHERE RUS.CODUSU = TCSITE.CODUSU AND TIPO = 'F') ");
		query.appendSql(" ORDER BY ");
		query.appendSql(" 	NUMITEM DESC ");

		query.setNamedParameter("NUMOS", numOs);
		query.setNamedParameter("CODUSU", codFila);

		ResultSet rs = query.executeQuery();
		if(rs.next()){
			numItem = rs.getBigDecimal("NUMITEM");
		}

		if(numItem != null){
			throw new Exception("No existe um item aberto para o usurio " + codFila + " ou ele no  uma fila.");
		}

		return numItem;
	}

	public static void salvarApontamento(BigDecimal numOs, Map<String, Object> itemTemplate, Collection<Intervalo> intervalos){

	}

	public static class Intervalo{
		long inicio;
		long fim;
		long pausa;
	}
}
