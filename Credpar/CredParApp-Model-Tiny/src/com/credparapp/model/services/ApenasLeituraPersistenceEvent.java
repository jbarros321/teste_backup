package com.credparapp.model.services;

import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.PersistenceEventAdapter;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;

public class ApenasLeituraPersistenceEvent extends PersistenceEventAdapter {

	@Override
	public void beforeDelete(PersistenceEvent event) throws Exception {
		throwException();
	}

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		throwException();
	}

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		ModifingFields mf = event.getModifingFields();
		if(mf.isModifing("ATIVO")) {
			DynamicVO vo = (DynamicVO) event.getVo();

			String ativo = vo.asString("ATIVO");

			if(!JapeSessionContext.containsProperty("credpar.ext.recarga") && "X".equals(ativo)) {
				throw new Exception("A opo de desativar s pode ser utilizada pela sincronizao automtica.");
			}

			DynamicVO oldVo = (DynamicVO) event.getOldVO();
			String ativoAnterior = oldVo.asString("ATIVO");
			if(!JapeSessionContext.containsProperty("credpar.ext.recarga") && "X".equals(ativoAnterior)) {
				throw new Exception("No  possvel modificar um registro que foi desativado pela sincronizao.");
			}
		}

		if(mf.size() > 2 || (!mf.isModifing("ATIVO") && (!mf.isModifing("DHULTATU") || !mf.isModifing("DHALTER")))) {
			throwException();
		}
	}

	private void throwException() throws Exception {
		if(!JapeSessionContext.containsProperty("credpar.ext.recarga")) {
			throw new Exception("Operao no permitida. Os registros dessa tela so apenas para leitura.");
		}
	}

}
