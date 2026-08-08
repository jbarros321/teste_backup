package br.com.sankhya.industria.manutencao.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;

import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

import com.sankhya.dao.client.vo.FieldVO;
import com.sankhya.model.apps.servico.OrdemServicoSF;
import com.sankhya.model.apps.servico.OrdemServicoSFBean.OSEntityBatchOperation;
import com.sankhya.model.apps.servico.OrdemServicoSFHome;
import com.sankhya.model.entities.bmp.CabecalhoLocal;
import com.sankhya.model.entities.bmp.CabecalhoLocalHome;
import com.sankhya.model.entities.bmp.ItemLocal;
import com.sankhya.model.entities.bmp.ItemLocalHome;
import com.sankhya.model.entities.pk.CabecalhoPK;
import com.sankhya.model.entities.pk.ItemPK;
import com.sankhya.model.entities.vo.CabecalhoVO;
import com.sankhya.model.entities.vo.ItemVO;
import com.sankhya.util.ServiceLocator;
import com.sankhya.util.TimeUtils;

public class OrdemServicoAPICover {

	private static OrdemServicoSF getOsFacade(AuthenticationInfo userAuthentication) throws Exception {
		InitialContext inicialCtx = new InitialContext();
		OrdemServicoSFHome osHome = (OrdemServicoSFHome) inicialCtx.lookup("ejb/OrdemServicoSFRemote");
		return osHome.create(userAuthentication);
	}

	private static CabecalhoVO loadCabecalho(BigDecimal numOs) throws Exception {
		CabecalhoLocalHome osLH = (CabecalhoLocalHome) ServiceLocator.getHome(CabecalhoLocalHome.JNDI_NAME, CabecalhoLocalHome.class);
		CabecalhoLocal osL = osLH.findByPrimaryKey(CabecalhoPK.getInstance(numOs.intValue()));
		return (CabecalhoVO) osL.getValueObject();
	}

	private static ItemVO loadItem(BigDecimal numOs, BigDecimal numItem) throws Exception {
		ItemLocalHome itemLH = (ItemLocalHome) ServiceLocator.getHome(ItemLocalHome.JNDI_NAME, ItemLocalHome.class);
		ItemLocal itemL = itemLH.findByPrimaryKey(ItemPK.getInstance(numOs.intValue(), numItem.intValue()));
		return (ItemVO) itemL.getValueObject();
	}

	private static AuthenticationInfo assertAuthentication(BigDecimal executante) throws Exception {
		AuthenticationInfo userAuthentication = AuthenticationInfo.getCurrentOrNull();

		if (userAuthentication == null) {
			if (executante == null) {
				throw new Exception("Executante no informado.");
			} else {
				userAuthentication = new AuthenticationInfo("", executante, null, 0);
			}
		} else {
			if (executante != null && !executante.equals(userAuthentication.getUserID())) {
				throw new Exception("Executante informado diferente do usurio logado.");
			}
		}

		return userAuthentication;
	}

	public static void salvarApontamento(BigDecimal numOs, BigDecimal numItem, Collection<IntervaloCover> intervalos, BigDecimal codUsu) throws Exception {

		OrdemServicoSF osFacade = null;

		try {
			AuthenticationInfo userAuthentication = assertAuthentication(codUsu);

			if (numItem == null) {
				numItem = OrdemServicoAPI.getItemAbertoUsuario(numOs, codUsu, false);
			}

			osFacade = getOsFacade(userAuthentication);
			osFacade.setOrdemServicoAtiva(numOs.intValue());

			CabecalhoVO cabVO = loadCabecalho(numOs);
			ItemVO itemVO = loadItem(numOs, numItem);
			if (itemVO.inicexec.getData() != null || itemVO.hrinicial.getData() != null || itemVO.hrfinal.getData() != null) {
				throw new Exception("O item " + numItem + " da OS " + numOs + " j possui apontamento.");
			}

			List<OSEntityBatchOperation> osDataList = new ArrayList<OSEntityBatchOperation>();

			boolean firstInterval = true;
			for (IntervaloCover i : intervalos) {
				Map<String, Object> dataCab = new HashMap<String, Object>();
				dataCab.put("NUMOS", numOs);

				Map<String, Object> dataIte = new HashMap<String, Object>();
				fillItemMap(itemVO, dataIte);
				dataIte.putAll(i.properties);

				if (!firstInterval) {
					dataIte.put("NUMITEM", null);
				} else {
					firstInterval = false;
				}

				SimpleDateFormat sdf = new SimpleDateFormat("HHmm");

				dataIte.put("INICEXEC", TimeUtils.clearTime(i.inicio));
				dataIte.put("HRINICIAL", new BigDecimal(sdf.format(i.inicio)));
				dataIte.put("HRFINAL", new BigDecimal(sdf.format(i.fim)));
				dataIte.put("INTERVALO", TimeUtils.minutes2Time(i.pausaMinutos));
				dataIte.put("SOLUCAO", i.descricao);

				Map<String, Object> extraActions = new HashMap<String, Object>();
				extraActions.put("encaminhar", "true");
				extraActions.put("codExecEnc", codUsu);
				extraActions.put("codParcEnc", cabVO.codparc.getData());
				extraActions.put("codProdEnc", itemVO.codprod.getData());
				extraActions.put("codServEnc", itemVO.codserv.getData());

				OSEntityBatchOperation osData = new OSEntityBatchOperation();
				osData.setDataCab(dataCab);
				osData.setDataIte(dataIte);
				osData.setActions(extraActions);
				osData.setStopIterationOnError(true);

				osDataList.add(osData);
			}

			osFacade.incluirAlterarOrdemDeServicoBatch(osDataList);
		} finally {
			if (osFacade != null) {
				osFacade.remove();
			}
		}
	}

	private static void fillItemMap(ItemVO item, Map<String, Object> map) throws Exception {

		Field[] fs = ItemVO.class.getFields();

		for (Field f : fs) {
			String propertyName = f.getName().toUpperCase();
			if (FieldVO.class.isAssignableFrom(f.getType())) {
				map.put(propertyName, ((FieldVO) f.get(item)).getDataAsObject());
			}
		}
	}

	public static class IntervaloCover {

		Timestamp	inicio;
		Timestamp	fim;
		BigDecimal	pausaMinutos;
		String		descricao;
		Map<String, Object> properties;

		public IntervaloCover(Timestamp inicio, Timestamp fim, BigDecimal pausaMinutos, String descricao) {
			this.inicio = inicio;
			this.fim = fim;
			this.pausaMinutos = pausaMinutos;
			this.descricao = descricao;
			this.properties = new HashMap<String, Object>();
		}

		public void addProperty(String field, Object value){
			this.properties.put(field, value);
		}
	}
}
